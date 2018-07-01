package com.gameofrobots.robotcontroller1;

/**
 * Created by Thomas on 2017-11-24.
 */


import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import java.io.IOException;



public class SetUp extends AppCompatActivity  {

    int buff_1;
    int buff_2;
    int buff_3;

    public static String BUFF_1,BUFF_2,BUFF_3 = "Buffs chosen by user";


    NumberPicker pickSpeed, pickHealth, pickShield;
    Button readyToPair;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_up);
        //makes screen landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        //Call Widgets
        pickSpeed = (NumberPicker)findViewById(R.id.idPickSpeed);
        pickHealth = (NumberPicker)findViewById(R.id.idPickHealth);
        pickShield = (NumberPicker)findViewById(R.id.idPickShield);
        readyToPair = (Button)findViewById(R.id.idReadyToPair);

        //set Min and Max values for Spinners
        /*
        A user may only choose 6 buffs for now
        Eventually a full marketplace will be implemented and allow users
        to collect points and continuously purchase buffs
         */
        pickSpeed.setMinValue(0);
        pickSpeed.setMaxValue(6);
        pickHealth.setMinValue(0);
        pickHealth.setMaxValue(6);
        pickShield.setMinValue(0);
        pickShield.setMaxValue(6);

        //pickSpeed.setWrapSelectorWheel(false); //This will remove the number wrap (number above number picker)

        //By default ready to pair is diabled unless there is 6 or less boosts selected
        readyToPair.setEnabled(false);
        readyToPair.setClickable(false);


        pickSpeed.setOnValueChangedListener(speed_onValueChange);
        pickHealth.setOnValueChangedListener(health_onValueChange);
        pickShield.setOnValueChangedListener(shield_onValueChange);
        readyToPair.setOnClickListener(ready_onClickListener);
    }//end on create


    NumberPicker.OnValueChangeListener speed_onValueChange = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
            buff_1 = newVal;
            checkBuffs(buff_1,buff_2,buff_3);
        }
    };

    NumberPicker.OnValueChangeListener health_onValueChange = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
            buff_2 = newVal;
            checkBuffs(buff_1,buff_2,buff_3);
        }
    };

    NumberPicker.OnValueChangeListener shield_onValueChange = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
            buff_3 = newVal;
            checkBuffs(buff_1,buff_2,buff_3);
        }
    };


    View.OnClickListener ready_onClickListener;
    {
        ready_onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                checkBuffs(buff_1,buff_2,buff_3);
                Intent i = new Intent(SetUp.this,DeviceList.class);
                i.putExtra("BUFF_1", String.valueOf(buff_1));
                i.putExtra("BUFF_2", String.valueOf(buff_2));
                i.putExtra("BUFF_3", String.valueOf(buff_3));
                startActivity(i);
            }

        };
    }


    private void checkBuffs(int buff_1, int buff_2, int buff_3){
        //make sure next button is disabled
        readyToPair.setEnabled(false);
        readyToPair.setClickable(false);

        int buffsTotal = 0;
        buffsTotal += buff_1;
        buffsTotal += buff_2;
        buffsTotal += buff_3;

        if(buffsTotal == 0){
            readyToPair.setEnabled(false);
            readyToPair.setClickable(false);
        }
        if(buffsTotal <=6){//Can only proceed to pair if 6 or less boosts are chosen
            readyToPair.setEnabled(true);
            readyToPair.setClickable(true);
        }
    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


}//End Main
