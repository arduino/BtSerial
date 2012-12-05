/**
 * Serial Duplex 
 * by Tom Igoe.
 * Adapted from the Serial library example for BtSerial by Joshua Albers
 *
 * Intended to be used to connect to a device running SerialDuplexServer
 * 
 * Sends an incrementing byte value out the serial port when
 * you tap the screen.
 * Listens for bytes received, and displays their value. 
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
  PFont myFont = createFont(PFont.list()[2], (float(displayWidth)*.04));
  textFont(myFont);

  bt = new BtSerial(this);

  println(bt.list(true)); //get list of paired devices (with extended information)
  remoteAddress = bt.list()[0]; //get only the hardware address for the specific entry

  bt.connect(remoteAddress); // connect to the device
}

void draw() {
  background(0);
  text("Connected to: " + bt.getRemoteName() +" [" + bt.getRemoteAddress() + "]", 10, 100);
  text("Last Sent: " + whichKey, 10, 150);
  text("Last Received: " + inByte, 10, 200);
}

void btSerialEvent(BtSerial bt){
  inByte = bt.read();
}

void mousePressed() {
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

