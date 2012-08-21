/**
 * Serial Duplex 
 * by Tom Igoe. 
 * 
 * Sends a byte out the serial port when you type a key
 * listens for bytes received, and displays their value. 
 * This is just a quick application for testing serial data
 * in both directions.
 *
 * Adapted for BtSerial by Joshua Albers
 */

import cc.arduino.btserial.*;

BtSerial bt;      // The serial port
int whichKey = -1;  // Variable to hold keystoke values
int inByte = -1;    // Incoming serial data
String remoteAddress; //hardware address for the device being connected to
byte outByte = -1;

void setup() {
  size(400, 300);
  // create a font with the third font available to the system:
  PFont myFont = createFont(PFont.list()[2], 14);
  textFont(myFont);

  bt = new BtSerial(this);

  println(bt.list(true)); //get list of paired devices (with extended information)
  remoteAddress = bt.list()[0]; //get only the hardware address for the specific entry

  bt.connect(remoteAddress); // connect to the device
}

void draw() {
  background(0);
//  if (bt.isConnected()) {
//    if (bt.available() > 0) {
//      inByte = bt.read();
//    }
//  }
  if (bt.isConnected()) if (bt.available() > 0) background(128);
  text("Last Received: " + inByte, 10, 130);
  text("Last Sent: " + whichKey, 10, 100);
}

void newData(BtSerial bt){
  inByte = bt.read();
}

void mousePressed() {
  // Send the keystroke out:
  outByte++;
  outByte = (byte)(outByte % 255);
  bt.write(outByte);
  whichKey = outByte;
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
    bt.connect(remoteAddress);
    if (bt.isConnected()) println("Bluetooth reconnected");
    else println("connection failed");
  }
}

