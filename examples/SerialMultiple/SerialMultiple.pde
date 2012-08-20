/**
 * Many Serial Ports
 * 
 * Read data from the multiple Serial Ports
 *
 * Adapted for BtSerial with Processing Android by Joshua Albers
 */


import cc.arduino.btserial.*;

BtSerial[] bt = new BtSerial[1];  // Create list of objects from BtSerial class
byte[] inByte = new byte[1];      // Create list of containers to store received data

void setup() {
  size(400, 300);

  bt[0] = new BtSerial(this);
  //bt[1] = new BtSerial(this);

  // print a list of the paired Bluetooth devices with names;
  println(bt[0].list(true));
  // On my machine, the first and third ports in the list
  // were the serial ports that my microcontrollers were 
  // attached to.
  // Open whatever ports ares the ones you're using.

  // get the devices' addresses:
  String deviceOne = bt[0].list()[4];
  //String deviceTwo = bt[0].list()[3];
  // open the ports:
  print("connecting 1 ");
  bt[0].connect(deviceOne);
  println("done");

//  print("connecting 2 ");
//  bt[1].connect(deviceTwo);
//  println("done");
}


void draw() {
  //println(bt[0].isConnected() + " " + bt[0].available() + " " + bt[1].isConnected() + " " + bt[1].available());
  for (int i=0; i<bt.length; i++) {
    if (bt[i].available() > 0) {
      inByte[i] = (byte)bt[i].read();
      println("Got " + inByte[i] + " from device " + bt[i].getName());
    }
  }

  // clear the screen:
  background(0);
  // use the latest byte from port 0 for the first circle
  fill(inByte[0]);
  ellipse(width/3, height/2, 40, 40);
  // use the latest byte from port 1 for the second circle
//  fill(iByte[1]);
//  ellipse(2*width/3, height/2, 40, 40);
}

//void serialEvent(Serial thisPort) {
//  // variable to hold the number of the port:
//  int portNumber = -1;
//
//  // iterate over the list of ports opened, and match the 
//  // one that generated this event:
//  for (int p = 0; p < myPorts.length; p++) {
//    if (thisPort == myPorts[p]) {
//      portNumber = p;
//    }
//  }
//  // read a byte from the port:
//  int inByte = thisPort.read();
//  // put it in the list that holds the latest data from each port:
//  dataIn[portNumber] = inByte;
//  // tell us who sent what:
//  println("Got " + inByte + " from serial port " + portNumber);
//}

/*
The following Wiring/Arduino code runs on both microcontrollers that
 were used to send data to this sketch:
 
 void setup()
 {
 // start serial port at 9600 bps:
 Serial.begin(9600);
 }
 
 void loop() {
 // read analog input, divide by 4 to make the range 0-255:
 int analogValue = analogRead(0)/4; 
 Serial.print(analogValue, BYTE);
 // pause for 10 milliseconds:
 delay(10);                 
 }
 
 
 */
