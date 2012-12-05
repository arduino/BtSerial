BtSerial Library for Processing for Android

This library gives you access to a Bluetooth Serial port on Android devices that have Bluetooth. It's structured using Processing's Serial library API, so those familiar with Serial should be able to do the same things with this as they can with Serial.

There are some differences:
* You have to enable Bluetooth on your device.
* You have to pair with the device you want to talk to in advance.
* It is recommended to disconnect the Bluetooth connection on stop() and pause() and reconnect on resume() in order to prevent connection errors

This library was based on SweetBlue, a Bluetooth library for Processing and Arduino, by Andreas Goransson & David Cuartielles at 1scale1.se. For connections between Processing for Android and Arduino without having to write your own Arduino firmware, see https://github.com/1scale1/sweetbt. It was also based in Ben Fry's Serial library for Processing(http://code.google.com/p/processing/), and on Google's BluetoothChatService example for Android (http://developer.android.com/resources/samples/BluetoothChat).
Thanks to Bonifaz Kaufman, developer of Amarino (http://code.google.com/p/amarino) for many good ideas as well.
BtSerial was refined and expanded by Joshua Albers (http://joshuaalbers.com) as part of Google Summer of Code 2012.


BtSerial Copyright 2011, 2012 Andreas Goransson & David Cuartielles & Tom Igoe & Joshua Albers
Version 0.2.0
August 2012
