/** 
 * BtSerial  Serial display
 Lists paired devices and connects to the first one on the list.
 Displays an incoming string from the serial connection.
 If it gets a newline, it clears the string
 
 
 created 27 June 2011
 modified 3 July 2011
 by Tom Igoe
 
 */

import cc.arduino.btserial.*;


// instance of the library:
BtSerial bt;
String inString = "";    // string for incoming data

void setup() {
  // Setup Fonts:
  String[] fontList = PFont.list();
  PFont androidFont = createFont(fontList[0], 24, true);
  textFont(androidFont, 24);
  textAlign(CENTER);

  // instantiate the library:
  bt = new BtSerial( this );
  // get a list of paired devices:
  String[] pairedDevices = bt.list();
  
  if (pairedDevices.length > 0) {
    println(pairedDevices);
    // open a connection to the first one:
    bt.connect( pairedDevices[0] );
    if (bt.isConnected()) {
      bt.write("Hello there!");
    }
  } 
  else {
    text("Couldn't get any paired devices", 10, height/2);
  }
}

void draw() {
  // black with a nice light blue text:
  background(0);
  fill(#D3C7FE);
  char inByte = 0;    // byte you'll read from serial connection

  // if you're connected, check for any incoming bytes:
  if ( bt != null ) {
    if ( bt.isConnected() ) {
      // put the connected device's name on the screen:
      text("connected to" + bt.getName(), 10, screenHeight/3);
      // if there are incoming bytes available, read one:
      if (bt.available() > 0) {
        inByte = char(bt.read());
        // if you get a newline, clear the string:
        if (inByte == '\n') {
          inString = "";
        } 
        else {    // otherwise add the byte to the string
          inString += inByte;
        }
      }
      // display the latest center screen:
      text(inString, screenWidth/2, screenHeight/2);
    }
  }
}

// disconnect on pause so you can reconnect:
void pause() {
  if (bt != null) {
    bt.disconnect();
  }
}


