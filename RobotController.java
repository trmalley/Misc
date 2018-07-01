package com.gameofrobots.robotcontroller1;

import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.os.CountDownTimer;


public class RobotController extends AppCompatActivity {

    Button btnForward, btnReverse, btnLeft, btnRight, btnShoot, btnBoost, btnHeal, btnShield;
    ProgressBar HealthBar;
    ConstraintLayout layout;
    TextView HealthStatus;
    TextView inTest;
    String address;
    int buff_1, buff_2, buff_3;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public Robot robo1 = new Robot();
    int timesShot=0;
    boolean ROBOT_DEAD = false;


    /*
        These Variables are not used.
        Can be used for Maketplace Implementation

    Marketplace m1 = new Marketplace();
    Item speed = new Item(1);
    Item heal = new Item(2);
    Item sheild = new Item(3);


    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //receive the address of the bluetooth device
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        //gets ints from previous activity
        buff_1 = Integer.parseInt(newint.getStringExtra("BUFF_1"));
        buff_2 = Integer.parseInt(newint.getStringExtra("BUFF_2"));
        buff_3 = Integer.parseInt(newint.getStringExtra("BUFF_3"));

        setContentView(R.layout.activity_robot_controller);

        //Can put code here for address to make background color
        // ex If address = 98:D3:31:FC:75:59 then robot is rogue and background color is red

        //call the widgtes
        btnForward = (Button)findViewById(R.id.idForward);
        btnReverse = (Button)findViewById(R.id.idReverse);
        btnLeft = (Button)findViewById(R.id.idLeft);
        btnRight = (Button)findViewById(R.id.idRight);
        btnShoot = (Button)findViewById(R.id.idShoot);
        btnBoost = (Button)findViewById(R.id.btnBoost);
        btnHeal = (Button)findViewById(R.id.btnHeal);
        btnShield = (Button)findViewById(R.id.btnShield);
        HealthStatus = (TextView)findViewById(R.id.idHealthStatus);
        HealthStatus.setText("Health: %" + robo1.getHealth());

        layout = (ConstraintLayout) findViewById(R.id.RobotController);

        //if rogue, red background. if aezul, blue background.
        if(address.contains("98:76:B6:00:3B:6A")) {
            layout.setBackgroundDrawable( getResources().getDrawable(R.drawable.robot_red_final));
        } else if(address.contains("98:D3:31:FB:7F:4B")) {
            layout.setBackgroundDrawable( getResources().getDrawable(R.drawable.robot_blue_final));
        } else {
            layout.setBackgroundDrawable( getResources().getDrawable(R.drawable.robot_white));
        }
       // HealthBar = (ProgressBar) findViewById((R.id.idHealthBar));
       // HealthBar.setMax(100);

        

        btnBoost.setText("Speed (" + buff_1 + ")");
        btnHeal.setText("Heal (" + buff_2 + ")");
        btnShield.setText("Shield (" + buff_3 + ")");



        new ConnectBT().execute();//Starts bluetooth connection

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {


            btnForward.setOnTouchListener(f_onTouchListener);
            btnReverse.setOnTouchListener(b_onTouchListener);
            btnLeft.setOnTouchListener(l_onTouchListener );
            btnRight.setOnTouchListener(r_onTouchListener );
            btnShoot.setOnClickListener(sh_onClickListener );
            btnBoost.setOnClickListener(boost_onClickListener);
            btnHeal.setOnClickListener(heal_onClickListener);
            btnShield.setOnClickListener(shield_onClickListener);

            progress = ProgressDialog.show(RobotController.this, "Connecting...", "Please wait!!!");  //show a progress dialog

        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {




            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection

                    //Get input and output streams from the socket

                    mmOutputStream = btSocket.getOutputStream();
                    mmInputStream = btSocket.getInputStream();

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
            beginListenForData();
        }

    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    void beginListenForData(){
        System.out.println("Begin Listen For Data");
        if(ROBOT_DEAD){
            robo1.resetConnection();
        }
        final Handler handler = new Handler();
        final byte delimiter = 35; //ASCII for #
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable(){

            public void run(){
                //System.out.println("State RUN: " + workerThread.getState());used to check state of robot
                while(!Thread.currentThread().isInterrupted() && !stopWorker){

                    try{
                        int bytesAvailable = mmInputStream.available();
                        //InputStream mmInputStream = btSocket.getInputStream();
                        //inTest.setText("INPUT: " + bytesAvailable);
                        if(bytesAvailable>0){
                            //inTest.setText("Bytes Available");
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");

                                    readBufferPosition = 0;

                                    handler.post(new Runnable()
                                    {

                                        public void run()
                                        {
                                            /*
                                            Possible messages from arduino
                                                r = arduino ready
                                                k = Robot has tilted and is dead
                                                h = robot has been shot
                                            */

                                            System.out.println(data);
                                            System.out.println(data.length());

                                            if(data.contains("h")){
                                                workerThread.interrupt();
                                                robo1.takeDamage(10);
                                            }
                                            //if(data.equals("k")){
                                            //  msg("ROBOT FLIPPED!");
                                            //}


                                        }

                                    });


                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }

                }
            }
        });

        workerThread.start();
    }


    OnTouchListener f_onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN)
                robo1.forward();
            else if (action == MotionEvent.ACTION_UP)
                robo1.stop();
            return false;
        }
    };

    OnTouchListener b_onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN)
                robo1.reverse();
            else if (action == MotionEvent.ACTION_UP)
                robo1.stop();
            return false;
        }
    };

    OnTouchListener l_onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN)
                robo1.left();
            else if (action == MotionEvent.ACTION_UP)
                robo1.stop();
            return false;
        }
    };


    OnTouchListener r_onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN)
                robo1.right();
            else if (action == MotionEvent.ACTION_UP)
                robo1.stop();
            return false;
        }
    };

    /*
    OnTouchListener sh_onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if(action == MotionEvent.ACTION_BUTTON_PRESS)
                robo1.shoot();
            return false;
        }
    };
    */

    OnClickListener sh_onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            robo1.shoot();


        }
    };

    OnClickListener boost_onClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            btnBoost.setClickable(false);
            btnBoost.setEnabled(false);
            robo1.speed();
            btnBoost.setText("Speed (" + buff_1 + ")"); //Set button to decrement buff variable

        }

    };

    OnClickListener heal_onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            robo1.heal(50);
            //btnHeal.setEnabled(false);
            //new Reminder(5);
            btnHeal.setText("Heal (" + buff_2 + ")");
            //

        }
    };

        OnClickListener shield_onClickListener = new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                btnShield.setEnabled(false);
                btnShield.setClickable(false);
                robo1.shield();
                btnShield.setText("Shield (" + buff_3 + ")");
            }
        };
    /*
    END On Click Listeners
     */


    class Robot{

        public int ROBOT_HEALTH;
        private final int MAX_HEALTH = 100;

        public Robot(){
            ROBOT_HEALTH = 100;
        }

        private int getHealth(){
            return ROBOT_HEALTH;
        }

        private void forward(){
            if(btSocket!=null){
                try{
                    mmOutputStream.write("w".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void reverse(){
            if(btSocket!=null){
                try{
                    btSocket.getOutputStream().write("s".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void left(){
            if(btSocket!=null){
                try{
                    btSocket.getOutputStream().write("a".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void right(){
            if(btSocket!=null){
                try{
                    btSocket.getOutputStream().write("d".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void stop(){
            if(btSocket!=null){
                try{
                    btSocket.getOutputStream().write("q".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void shoot(){
            if(btSocket!=null){
                try{
                    btSocket.getOutputStream().write("e".toString().getBytes());
                }
                catch(IOException e){
                    msg("ERROR");
                }
            }
        }

        private void takeDamage(int dmg){
            System.out.println("taking damage");
            ROBOT_HEALTH -= dmg;
            //System.out.println("ROBOT IS TAKING DAMAGE");
            HealthStatus.setText("Health: %" + robo1.getHealth());


            //workerThread.start();
            //msg("Robot Health" + robo1.getHealth() + "\n");
            if(this.getHealth() <= 0){
                //GAME OVER ROBOT DEAD
                msg("ROBOT DEAD");
                ROBOT_DEAD = true;
                resetConnection();
            }
            beginListenForData();
        }


        private void speed(){
            if(buff_1>0){
                buff_1--;
                if(btSocket!=null){
                    try{
                        mmOutputStream.write("i".toString().getBytes()); //activates shield
                        new CountDownTimer(10000,1000){
                            public void onTick(long millisUnitlFinished){
                                //Nothing needed each tick
                                //Maybe display a message to user with time left
                            }
                            public void onFinish(){
                                try {
                                    mmOutputStream.write("I".toString().getBytes());//deactivates shield after 10 seconds
                                }catch(IOException e){
                                    msg("ERROR");
                                }
                                btnBoost.setClickable(true);
                                btnBoost.setEnabled(true);
                            }
                        }.start();
                    }
                    catch(IOException e){
                        msg("ERROR");
                    }
                    //System.out.println("Timer ran successfully and boost is done");
                }
            }else if(buff_1<=0){ //No Buffs Left
                msg("EMPTY!");
                btnBoost.setClickable(false);
                btnBoost.setEnabled(false);
            }
        }

        /*
Heal Method
Will heal robot
 */
        private void heal(int in){
            if(buff_2>0) {
                ROBOT_HEALTH += in;
                buff_2 --;
                btnHeal.setText("Heal (" + buff_2 + ")");
            }else{
                msg("EMPTY!");
                btnHeal.setClickable(false);
                btnHeal.setEnabled(false);
            }
            if(ROBOT_HEALTH > MAX_HEALTH){
                ROBOT_HEALTH = MAX_HEALTH;
            }
            HealthStatus.setText("Health: %" + robo1.getHealth());
        }

        private void shield(){
            if(buff_3>0){
                buff_3--;
                if(btSocket!=null){
                    try{
                        mmOutputStream.write("o".toString().getBytes()); //activates shield
                        new CountDownTimer(10000,1000){
                            public void onTick(long millisUnitlFinished){
                                //Nothing needed each tick
                                //Maybe display a message to user with time left
                            }
                            public void onFinish(){
                                try {
                                    mmOutputStream.write("O".toString().getBytes());//deactivates shield after 10 seconds
                                }catch(IOException e){
                                    msg("ERROR");
                                }
                                btnShield.setClickable(true);
                                btnShield.setEnabled(true);
                            }
                        }.start();
                        //System.out.println("Timer ran successfully and shield is done");
                    }
                    catch(IOException e){
                        msg("ERROR");
                    }
                }
            }else if(buff_3<=0){
                msg("EMPTY!");
                btnShield.setClickable(false);
                btnShield.setEnabled(false);
            }
        }

        private void resetConnection(){
            if (mmInputStream != null) {
                try {mmInputStream.close();} catch (Exception e) {}
                mmInputStream = null;
            }

            if (mmOutputStream != null) {
                try {mmOutputStream.close();} catch (Exception e) {}
                mmOutputStream = null;
            }

            if (btSocket != null) {
                try {btSocket.close();} catch (Exception e) {}
                btSocket = null;
            }

            Intent i = new Intent(RobotController.this, SetUp.class);

            startActivity(i);
        }

    }//END Robot Class


}//END MAIN

















