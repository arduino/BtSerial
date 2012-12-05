/* LightProbe
 * 
 * This sketch is intended to captute data from two photoresistors
 * attached to an Arduino through analog pins and sent to an Android
 * device over a Bluetooth serial connection.
 * 
 * August 2012 Joshua Albers
 */

import ketai.sensors.*;
import cc.arduino.btserial.*;
import android.os.Environment;
import android.content.Intent;
import android.net.Uri;

ArrayList devices;
BtSerial bt;

String remoteAddress;

boolean registered = false;
PFont f1;
PFont f2;

int leftValue = 0;
int rightValue = 0;

String filePrefix = "LightProbe";
String outputFilename = "";
BufferedWriter out;

void setup() {
  orientation(LANDSCAPE);
  size(displayWidth, displayHeight);  
  f1 = createFont("Droid Sans", 50, true);
  f2 = createFont("Droid Sans", 35, true);
  
  bt = new BtSerial(this); //create the BtSerial object that will handle the connection
  println(bt.list(true)); //Display a list of devices the paired devices
  remoteAddress = bt.list()[3]; //on my test device, the Arduino is the third paired device
  
  //build the output filename
  outputFilename = createFile(filePrefix + "_" + nf(year(), 4) + nf(month(), 2) + nf(day(), 2) + nf(hour(), 2) + nf(minute(), 2) + nf(second(), 2) + ".csv");
  print (outputFilename);

  try {
    out = new BufferedWriter(new FileWriter(outputFilename, true), 4096);
    println(" output file open");
  } 
  catch (Exception e) {
    //e.printStackTrace();
  }
}

void draw() {
  textFont(f2);
  float textHeight = textAscent() + textDescent();

  if (!bt.isConnected()) { //if not connected, don't display the probe values
    background(0);
    fill(255);
    String[] pairedDevices = bt.list();
    textAlign(CENTER);
    String statusText = "Connecting to " + remoteAddress;
    print(statusText);
    text(statusText, width/2, height/2);
    try {
      bt.connect(remoteAddress);
    }
    catch(Exception ex) {
      println(" connection failed");
    }
  } 
  else {
    background(64);
    fill(map(leftValue, 0, 1023, 0, 255));
    rect(0, textHeight + 10, width/2, height);
    fill(map(rightValue, textHeight + 10, 1023, 0, 255));
    rect(width/2, textHeight+10, width, height);
  }

  fill(255);
  getPhoneLocation();

  if (locationReady) {
    textAlign(LEFT);
    text("Lat: " + latitude, 5, textHeight);
    text("Lon: " + longitude, displayWidth/2, textHeight);
  } 
  else {
    textAlign(CENTER);
    text("Please enable device location", 5, textHeight);
  }
}

void newData(BtSerial bt) {    
  // if there are incoming bytes, make sure at least 10 bytes (a complete message)
  // is in the buffer.
  if (bt.available() >= 10) {
    
    // if so, get the current timestamp from the phone
    String messageTime = getNowUTC();
    // and read the buffer.
    String message = bt.readStringUntil(';');
    
    if (message.length() == 9) {
      String[] fields = split(message, ',');
      
      if (fields.length == 2) {
        leftValue = int(fields[0]);
        rightValue = int(fields[1]);
        
        //build the output message
        String outputMessage = messageTime + "," + message + "," + longitude + "," + latitude + "\n";
        
        //save the output message
        try {
          out.write(outputMessage);           
        }
        catch(Exception ex) {
          println(ex);
        }
      }
    }
  }
}

void pause() {
  // disconnect on pause so you can reconnect on Resume:
  if (bt != null) {
    bt.disconnect();
    println("Pause; isConnected() = " + bt.isConnected());
  }

  // and close the file so it doesn't get stuck open
  try {
    out.close();
    refreshFiles();
    println("output file closed");
  }
  catch(Exception ex) {
    println(ex);
  }
}

void resume() {
  println("Resume");
  
  // refresh the location if necessary
  if (location == null) location = new KetaiLocation(this);
  
  // reopen the output file
  try {
    out = new BufferedWriter(new FileWriter(outputFilename, true), 4096);
    println("output file open");
  } 
  catch (Exception e) {
    //e.printStackTrace();
  }
}

