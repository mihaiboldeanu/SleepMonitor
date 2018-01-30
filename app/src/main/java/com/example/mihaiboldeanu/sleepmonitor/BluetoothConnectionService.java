package com.example.mihaiboldeanu.sleepmonitor;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Mihai Boldeanu on 20.01.2018.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";
    private static final String appName  = "SleepMonitor";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    private Context mContext;

    public BluetoothConnectionService( Context mContext) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = mContext;
        start();
    }

    /**
     * This thread runs while waiting for a connection.
     * It behaves like a server-side client. It runs until a connection
     * is accepted (or until canceled)
     */
    private class AcceptThread extends Thread{
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "Accept thread: Setting up server using" + MY_UUID_INSECURE);
            }catch(IOException e){
                Log.d(TAG, "Accept thread: IOException" + e.getMessage());

            }
            mmServerSocket = tmp;
        }
        public void run(){
            Log.d(TAG,"run: AccpetThread runnning");
            BluetoothSocket socket = null;
            // Blocking call will only return after conecetion
            Log.d(TAG,"run: RFCOM server start...");
            try {
                socket = mmServerSocket.accept();
            }catch(IOException e){
                Log.d(TAG, "run: IOException" + e.getMessage());
            }
            if (socket == null){
                connected(socket,mmDevice);
            }
            Log.i(TAG,"End AcceptThread");

        }
        public void cancel(){
            Log.d(TAG,"cancel: Canceling AcceptThread");
            try{
                mmServerSocket.close();
            }catch (IOException e){
                Log.d(TAG,"cancel: Close of AcceptThread ServerSocket failed"+e.getMessage());

            }
        }
    }

    /***
     * This is a thread that runs while attempting to make an outgoing connection with
     * a device. It runs straight through; the connection either succeds or fails
     */
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device,UUID uuid){
            Log.d(TAG,"ConnectThread: Started");
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run(){
            BluetoothSocket tmp = null;
            Log.d(TAG,"Run:mConnectThread");

            // Get a BluetoothSocket for a connection with the
            // given bluetooth device

            try {
                Log.d(TAG,"ConnectThread: Trying to create a Insecure RFCOMMSocket using uuid"+ MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG,"ConnectThread: Could not create InsecureRFCOMMSocket " + e.getMessage());
            }
            mmSocket = tmp;

            //Always cancel discovery because it will slow down connection
            mBluetoothAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket

            // This is a blocking call and will return ona a succesful
            // connection or exception

            try {
                mmSocket.connect();
                Log.d(TAG,"run: ConnectThreead connected");
            } catch (IOException e) {
                // Close Socket
                try {
                    mmSocket.close();
                    Log.d(TAG,"run: CoClosed socket");
                } catch (IOException e1) {
                    Log.e(TAG,"mConnectThread: run : Unable to close connection in socket "+ e1.getMessage());
                }
                Log.d(TAG,"run: ConnectThread: Could not connect to UUID  "+ MY_UUID_INSECURE );
            }
            connected(mmSocket,mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed"+ e.getMessage());
            }
        }
    }



    /**
     * Start the server listening for data from client. Specificali start AcceptThread
     * to begin a session in listening (server) mode. Called by Activity onResume()
     */
    public synchronized void start(){
        Log.d(TAG,"Strat");
        // Cancel any thread attempting to connect
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }
    /***
     * AcceptThread starts and sits waiting for a connection
     * Then ConnectThread starts and attempts to make a connection with
     * the other devices AcceptThread.
     */
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startClient: Started");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth","Please wait ...",true);

        mConnectThread = new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    /**
     * Finaly the ConnectedThread class which is responsible for maintaing the BT conection
     * sending and receiveing data thou stream
     */
   private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG,"ConnectedThread: Starting");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut  = null;

            // dismiss the progress dialog when connection is established
            mProgressDialog.dismiss();
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run(){
            byte[] buffer = new byte[1024];// buffer to store for the stream

            int bytes;// bytes returned from read()
            //Keep listening to the InputStream until exception
            while (true){
                //Read from InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer,0,bytes);
                    Log.d(TAG,"InputStream: "+incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage",incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    Log.d(TAG,"run: error reading from the device "+ e.getMessage());
                    break;
                }


            }
        }
        // Call this from the main activity to send data to the remote device
        public void write(byte[] bytes){


            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"write: Writting to the device "+ text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG,"write: error writting to the device "+ e.getMessage());
            }

        }

        //Call this from the main activity to shuddown the connection
        public void cancel(){
            try {
                mmSocket.close();
            }catch (IOException e){}
        }
   }
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
    Log.d(TAG,"connected: Starting");
    mConnectedThread = new ConnectedThread(mmSocket);
    mConnectedThread.start();
   }
    /**
     * Write to the Connected Thread in an unsyncronos manner
     * @param out the bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out){
        // Tmp Conected thread
        ConnectedThread r;
        Log.d(TAG,"write: Write Called");
        mConnectedThread.write(out);

    }
}


