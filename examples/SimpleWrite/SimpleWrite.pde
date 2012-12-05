/**
 * Simple Write.
 * Adapted from the Serial library example for BtSerial by Joshua Albers 
 *
 * Intended to be used to connect to a device running SerialDuplexServer
 * 
 * Check if the mouse is over a rectangle and writes the status to the serial port. 
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
  background(255);
  if (mouseOverRect() == true) {  // If mouse is over square,
    fill(204);                    // change color and
    bt.write('H');              // send an H to indicate mouse is over square
  } 
  else {                        // If mouse is not over square,
    fill(0);                      // change color and
    bt.write('L');              // send an L otherwise
  }
  rect(width/2, height/2, 200, 200);         // Draw a square
}

boolean mouseOverRect() { // Test if mouse is over square
  return ((mouseX >= (width/2-100)) && (mouseX <= (width/2+100)) && (mouseY >= (height/2-100)) && (mouseY <= (height/2+100)));
}


/*
// Wiring/Arduino code:
// Read data from the serial and turn ON or OFF a light depending on the value

// Adapted for BtSerial with Processing Android by Joshua Albers

#include <SoftwareSerial.h>

SoftwareSerial bluetooth(8,9);  // Serial Bluetooth Modem connected with TX to pin 8 and RX to pin 9

char val; // Data received from the serial port
int ledPin = 13; // Set the pin to digital I/O 13

void setup() {
  pinMode(ledPin, OUTPUT); // Set pin as OUTPUT
  bluetooth.begin(115200);               // Start serial communication at 115200 bps
}

void loop() {
  if (bluetooth.available()) { // If data is available to read,
    val = bluetooth.read(); // read it and store it in val
  }
  if (val == 'H') { // If H was received
    digitalWrite(ledPin, HIGH); // turn the LED on
    bluetooth.flush();
  } 
  else {
    digitalWrite(ledPin, LOW); // Otherwise turn it OFF
    bluetooth.flush();
  }
  delay(100); // Wait 100 milliseconds for next reading
}

 */
