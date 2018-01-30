package com.example.mihaiboldeanu.sleepmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR);


            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button_start);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                enableBluetooth();
                goToLogin();
            }
        });
    }
    public void goToLogin() {

        // Go to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void enableBluetooth(){
        if (mBluetoothAdapter ==null){
            Log.d(TAG, "enableDisable: Does not have BT capabiliteis");
            return ;
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(mBroadcastReceiver1,BTIntent);
        }
    }

}
