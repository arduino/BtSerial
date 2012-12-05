/**
 * Simple Read
 * Adapted from the Serial library example for BtSerial by Joshua Albers 
 * 
 * Read data from the Bluetooth device and change the color of a rectangle
 * when a switch connected to a Wiring or Arduino board is pressed and released.
 * This example works with the Wiring / Arduino program that follows below.
 *
 */


import cc.arduino.btserial.*;

BtSerial bt;  // Create object from BtSerial class
String remoteAddress; // MAC address of the device to which the Android will connect
int val;      // Data received from the Bluetooth serial port

void setup() {
  size(displayWidth, displayHeight);
  rectMode(CENTER);
  
  bt = new BtSerial(this); //create the BtSerial object that will handle the connection
  println(bt.list(true)); //get list of paired devices (with extended information)
  remoteAddress = bt.list()[3]; //get only the hardware address for the specific entry

  bt.connect(remoteAddress);
}

void draw() {
  
  background(255);             // Set background to white
  if (val == 0) {              // If the serial value is 0,
    fill(0);                   // set fill to black
  }  
  else {                       // If the serial value is not 0,
    fill(204);                 // set fill to light gray
  }

  rect(width/2, height/2, width/3, width/3); // draw a square in the center of the screen
}

void btSerialEvent(BtSerial bt) {
  val = bt.read(); //update val whenever new data is received
}

void pause() {
  if (bt != null) {
    bt.disconnect();
  }
  println("Bluetooth disconnected");
}

void stop() {
  if (bt != null) {
    bt.disconnect();
  }
  println("Bluetooth disconnected");
}

void resume() {
  if (bt != null) {
    while (!bt.isConnected ()) {
      bt.connect(remoteAddress);
    }
    println("Bluetooth reconnected");
  }
}



/*

 // Wiring / Arduino Code
 // Code for sensing a switch status and writing the value to the serial port.
 //
 // Adapted for BtSerial with Processing Android by Joshua Albers
 
 #include <SoftwareSerial.h>
 
 SoftwareSerial bluetooth(8,9);  // Serial Bluetooth Modem connected with TX to pin 8 and RX to pin 9
 
 int switchPin = 4;                       // Switch connected to pin 4
 
 void setup() {
 pinMode(switchPin, INPUT);             // Set pin 0 as an input
 bluetooth.begin(115200);               // Start serial communication at 115200 bps
 }
 
 void loop() {
 if (digitalRead(switchPin) == HIGH) {  // If switch is ON,
 bluetooth.write(byte(1));               // send 1 to Processing
 } else {                               // If the switch is not ON,
 bluetooth.write(byte(0));               // send 0 to Processing
 }
 delay(100);                            // Wait 100 milliseconds
 }
 
 */
