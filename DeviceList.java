package com.gameofrobots.robotcontroller1;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


public class DeviceList extends AppCompatActivity {

    Button btnPaired, btnBack;
    ListView devicelist;
    private BluetoothAdapter myBluetooth = null;
    private Set <BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    int buff_1, buff_2, buff_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent newint = getIntent();
        buff_1 = Integer.parseInt(newint.getStringExtra("BUFF_1"));
        buff_2 = Integer.parseInt(newint.getStringExtra("BUFF_2"));
        buff_3 = Integer.parseInt(newint.getStringExtra("BUFF_3"));


        btnPaired = findViewById(R.id.btConnect);
        btnBack = findViewById(R.id.btnBack);
        devicelist = findViewById(R.id.listView);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null){//Check Bluetooth
            //Error Message Device Does Not Have Bluetooth Adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available",
                    Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }else{
            if(myBluetooth.isEnabled()){
                //Do Noting Bluetooth Is On
            }
            else{
                //Bluetooth Off Ask User To Turn On Bluetooth
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
            }

        }//End Bluetooth Check

        msg("Press and hold on the \"i\" icon's to learn more!");

        btnPaired.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                pairedDevicesList(); //Method to be called
            }
        });

        btnBack.setOnClickListener(back_onClickListener);



    }//end on create

    private void pairedDevicesList(){
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if(pairedDevices.size()>0){
            for(BluetoothDevice bt : pairedDevices){
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get Devices Name And Addresse

            }
        }else{
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method Called When The Device From The List Is Clicked
    }

    View.OnClickListener back_onClickListener;
    {
        back_onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent i = new Intent(DeviceList.this,SetUp.class);
                startActivity(i);
            }

        };
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Make an intent to start next activity.
            Intent i = new Intent(DeviceList.this, RobotController.class);
            //Change the activity.
            ///System.out.println("Address: " + address);
            i.putExtra(EXTRA_ADDRESS, address); //this will be received at RobotController (class) Activity
            i.putExtra("BUFF_1", String.valueOf(buff_1));
            i.putExtra("BUFF_2", String.valueOf(buff_2));
            i.putExtra("BUFF_3", String.valueOf(buff_3));
            startActivity(i);
        }
    };



}
