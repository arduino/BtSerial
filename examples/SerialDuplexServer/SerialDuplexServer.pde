/**
 * Serial Duplex 
 * by Tom Igoe.
 * Adapted from the Serial library example for BtSerial by Joshua Albers
 *
 * Intended to be used to connect to a device running SerialDuplex 
 * 
 * Sends a byte out the serial port when you type a key
 * listens for bytes received, and displays their value. 
 * This is just a quick application for testing serial data
 * in both directions.
 *
 */

import cc.arduino.btserial.*;

BtSerial bt;      // the Bluetooth serial connection
int whichKey = -1;  // Variable to hold keystoke values
int inByte = -1;    // Incoming serial data
String remoteAddress; //hardware address for the device being connected to
byte outByte = -1;

void setup() {
  size(displayWidth, displayHeight);
  // create a font with the third font available to the system:
  PFont myFont = createFont(PFont.list()[2], (displayWidth * .04));
  textFont(myFont);

  bt = new BtSerial(this);

  println(bt.list(true)); //get list of paired devices (with extended information)

  bt.listen(); // listen for incoming connections from an Android device
}

void draw() {
  background(0);

  text("Connected to: " + bt.getRemoteName() +" [" + bt.getRemoteAddress() + "]", 10, 100);
  text("Last Sent: " + whichKey, 10, 150);
  text("Last Received: " + inByte, 10, 200);

  if (bt.isConnected()) {
    if (bt.available() > 0) {
      inByte = bt.read();
    }
  }
}

void btSerialEvent(BtSerial bt) {
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
    println("Bluetooth reconnected");
  }
}

