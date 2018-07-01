#include "IRLibAll.h"

#include <IRLibDecodeBase.h>  //We need both the coding and
#include <IRLibSendBase.h>
 

#include <IRLib_P01_NEC.h>   
#include <IRLibCombo.h>   

#include <IRLibRecv.h>
#include <IRLibRecvPCI.h> 
/*
 * CODE UNTESTED BUT COMPLETE 
 * 
 */

//L293D Motor Driver
//Motor A
const int motorPin1  = 9;  // Pin 14 of L293
const int motorPin2  = 10;  // Pin 10 of L293
//Motor B
const int motorPin3  = 6; // Pin  7 of L293
const int motorPin4  = 5;  // Pin  2 of L293

int motorSpeed = 180;

char rxChar= 0;         // RXcHAR holds the received command.

boolean speedBoost = false;
boolean shield = false;


//#define SONY 2
     
//Create a receiver object to listen on pin 2
IRrecvPCI myReceiver(2);

// Create a sender object tied to pin 3
IRsend mySender;
     
//Create a decoder object 
IRdecode myDecoder;   

//Pins and state for tilt switch
//const int tiltPin = 8;
//int tiltState = 0;
//int prevTiltState = 0;

//Number of times code has looped


//This will run only one time.
void setup(){
    Serial.begin(9600);   // Open serial port (9600 bauds).
    
    //Delay to allow initialization of Serial connection
    delay(3000);
         
    //Sets motor pins as outputs
    pinMode(motorPin1, OUTPUT);
    pinMode(motorPin2, OUTPUT);
    pinMode(motorPin3, OUTPUT);
    pinMode(motorPin4, OUTPUT);

    //Sets tilt switch pin as output
   // pinMode(tiltPin, INPUT);

    myReceiver.enableIRIn(); // enable receiver
    //Serial.write("AT+NAME ");
    //Sends message to app stating its ready
    Serial.println("r#");
  
}

void forward(){
  //This code will make the bot move forward
    checkBoost();
    analogWrite(motorPin1, motorSpeed);
    analogWrite(motorPin2, 0);
    analogWrite(motorPin3, 0);
    analogWrite(motorPin4, motorSpeed);      
}

void backwards(){
    //This code will make the bot move backwards
    checkBoost();
    analogWrite(motorPin1, 0);
    analogWrite(motorPin2, motorSpeed);
    analogWrite(motorPin3, motorSpeed);
    analogWrite(motorPin4, 0);
}

void left(){
      //This code will make the bot move left
    checkBoost();
    analogWrite(motorPin1, 0);
    analogWrite(motorPin2, motorSpeed);
    analogWrite(motorPin3, 0);
    analogWrite(motorPin4, motorSpeed);
}

void right(){
    //This code will make the bot move right
    checkBoost();
    analogWrite(motorPin1, motorSpeed);
    analogWrite(motorPin2, 0);
    analogWrite(motorPin3, motorSpeed);
    analogWrite(motorPin4, 0);
}

void stopBot(){
      //And this code will stop motors
    analogWrite(motorPin1, 0);
    analogWrite(motorPin2, 0);
    analogWrite(motorPin3, 0);
    analogWrite(motorPin4, 0); 
}

void shoot(){
 
  mySender.send(NEC,0x61a0f00f,0); //Sends signal via IR Blaster
  myReceiver.enableIRIn(); // Re-enable receiver
   Serial.println("Code Sent");
  
}

void robotDead(){
  stopBot();
  Serial.println("k#");
  Serial.end();
}

void checkBoost(){
  if(speedBoost){
    motorSpeed = 255;
  }else{
    motorSpeed = 180;
  }
} //end checkboost

void loop(){
//Read state of tilt switch    
//tiltState = digitalRead(tiltPin);

 myReceiver.enableIRIn();      //Enable Receiver
 //disable IR receiver for sheild buff myReceiver.disableIRIn();
 
    
//if(tiltState != prevTiltState && (cycle > 1)){
//  robotDead();
//}

    //Listen for serial Input
    if (Serial.available() >0){          // Check receive buffer.
    rxChar = Serial.read();            // Save character received. 
    Serial.flush();

   //Check char and make decision
    switch(rxChar){
      case 'w':
      forward();
      delay(10);
      break;
      case 'a':
      left();
      delay(10);
      break;
      case 'd':
      right();
      delay(10);
      break;
      case 's':
      backwards();
      delay(10);
      break;
      case'e':
      shoot();
      delay(10);
      break;
      case 'i':
      speedBoost = true;
      break;
      case 'I':
      speedBoost = false;
      case 'o':
      shield = true;
      break;
      case 'O':
      shield = false;
      break;      
      case 'q':
      stopBot();

      checkBoost();
    }
  }//End Serial Listener
  
      //signal received robot shot
      if (myReceiver.getResults()) {
        //only shoot if robot does not have shield enabled
        if(shield == false){
          
        myDecoder.decode();           //Decode it
        //Serial.println("Code Received");
        //myDecoder.dumpResults(false);  //Now print results. Use false for less detail
        //`Serial.println(myDecoder.value);
        if(myDecoder.value == 1637937167){
          Serial.println("h#");
        }
        myReceiver.enableIRIn();      //Restart receiver   
        }   
      }
  


  //All code has run and cycle is restarting   
  //Serial.println("Cycle#");
}





