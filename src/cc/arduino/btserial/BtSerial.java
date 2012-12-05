/**
 * ##project.name##
 * ##library.sentence##
 *
 * ##copyright##
 *
 * This file is part of BtSerial.
 * 
 *   BtSerial is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   BtSerial is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 * 
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with BtSerial.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##library.prettyVersion##
 */

package cc.arduino.btserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.lang.reflect.*;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/* TODO */
// ** buffer()
// bufferUntil()
// btSerialEvent()

public class BtSerial {

	/* PApplet context */
	private Context ctx;

	public final static String VERSION = "##library.prettyVersion##";

	/* Bluetooth */
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private UUID uuidSpp = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// private UUID uuidSecure = UUID
	// .fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	// private UUID uuidInecure = UUID
	// .fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

	/* Socket & streams for BT communication */
	private BluetoothSocket mSocket;
	private ConnectedThread mConnectedThread;
	private boolean connected = false;

	/* Buffer */
	private int bufferlength = 128;
	private int available = 0;
	private byte[] buffer;
	private byte[] rawbuffer;
	private int bufferIndex;
	private int bufferLast;

	Method btSerialEventMethod;

	/* Debug variables */
	public static boolean DEBUG = true;
	public static String DEBUGTAG = "##library.name## " + VERSION
			+ " Debug message: ";

	private final String TAG = "System.out";

	public BtSerial(Context ctx) {
		this.ctx = ctx;
		welcome();

		Looper.prepare(); // this line is necessary to get BtSerial to
							// initialize on my Nook Color running Cyanogenmod,
							// but it is not required on my Galaxy Nexus running
							// ICS. I don't get it.

		try {
			mAdapter = BluetoothAdapter.getDefaultAdapter();
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}
		// Log.i(TAG, "BluetoothAdapter started");

		// reflection to check whether host applet has a call for
		// public void serialEvent(processing.serial.Serial)
		// which would be called each time an event comes in
		try {
			btSerialEventMethod = ctx.getClass().getMethod("btSerialEvent",
					new Class[] { BtSerial.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
	}

	/*
	 * Callback triggered whenever there is data in the buffer.
	 */

	public void btSerialEvent() {
		if (btSerialEventMethod != null) {
			try {
				btSerialEventMethod.invoke(ctx, new Object[] { this });
				// Log.i(TAG, "btSerialEvent called from BtSerial");
			} catch (Exception e) {
				String msg = "error, disabling btSerialEvent() for "
						+ mDevice.getName();
				Log.e(TAG, msg);
				e.printStackTrace();
				btSerialEventMethod = null;
			}
		}
	}

	/**
	 * Returns the status of the connection.
	 * 
	 * @return true or false
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Returns whether the Bluetooth dapter is enabled.
	 * 
	 * @return true of false
	 */
	public boolean isEnabled() {
		if (mAdapter != null)
			return mAdapter.isEnabled();
		else
			return false;
	}

	/**
	 * Returns a list of bonded (paired) devices.
	 * 
	 * @param info
	 *            flag to control display of additional information (device
	 *            names and types)
	 * @return String array
	 */
	public String[] list(boolean info) {
		Vector<String> list = new Vector<String>();
		Set<BluetoothDevice> devices;

		try {
			devices = mAdapter.getBondedDevices();
			// convert the devices 'set' into an array so that we can
			// perform string functions on it
			Object[] deviceArray = devices.toArray();
			// step through it and assign each device in turn to
			// remoteDevice and then print it's name
			for (int i = 0; i < devices.size(); i++) {
				BluetoothDevice thisDevice = mAdapter
						.getRemoteDevice(deviceArray[i].toString());
				String element = thisDevice.getAddress();
				if (info) {
					element += ","
							+ thisDevice.getName()
							+ ","
							+ thisDevice.getBluetoothClass()
									.getMajorDeviceClass(); // extended
															// information
				}
				list.addElement(element);
			}
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
		}

		String outgoing[] = new String[list.size()];
		list.copyInto(outgoing);
		return outgoing;
	}

	/**
	 * Returns a list of hardware (MAC) addresses of bonded (paired) devices.
	 * 
	 * @return String array
	 */

	public String[] list() {
		return list(false);
	}

	/**
	 * Returns the name of the connected remote device It not connected, returns
	 * "-1"
	 */

	public String getRemoteName() {
		if (connected) {
			String info = mDevice.getName();
			return (info);
		} else {
			return ("-1");
		}
	}

	/**
	 * Returns the name of the connected remote device It not connected, returns
	 * "-1"
	 */

	public String getRemoteAddress() {
		if (connected) {
			String info = mDevice.getAddress();
			return (info);
		} else {
			return ("-1");
		}
	}

	/*
	 * Some stubs for future implementation:
	 */
	// public void startDiscovery() {
	// // this method will start a separate thread to handle discovery
	// }
	//
	// public void pairWith(String thisAddress) {
	// // this method will pair with a device given a MAC address
	// }
	//
	// public boolean discoveryComplete() {
	// // this method will return whether discovery is complete,
	// // so the user can then list devices
	// return false;
	// }

	/**
	 * Returns the name of the currently connected device.
	 * 
	 * @return String
	 */

	public String getName() {
		if (mDevice != null)
			return mDevice.getName();
		else
			return "no device connected";
	}

	/**
	 * Connects to a Bluetooth device.
	 * 
	 * The connect() method will attempt to determine what type of device is
	 * currently specified by mac and will select one of the following Service
	 * Profile UUIDs accordingly.
	 * <p>
	 * Currently only Android-to-serial modem (Arduino) and Android- to-serial
	 * port (computer) connections are supported.
	 * <p>
	 * 
	 * @param mac
	 *            - hardware (MAC) address of the remote device
	 * @return boolean flag for if connection was successful
	 */

	public synchronized boolean connect(String mac) {
		/* Before we connect, make sure to cancel any discovery! */
		if (mAdapter.isDiscovering()) {
			mAdapter.cancelDiscovery();
			Log.i(TAG, "Cancelled ongoing discovery");
		}

		/* Make sure we're using a real bluetooth address to connect with */
		if (BluetoothAdapter.checkBluetoothAddress(mac)) {
			/* Get the remote device we're trying to connect to */
			mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
			/* Create the RFCOMM sockets */
			try {

				mSocket = mDevice.createRfcommSocketToServiceRecord(uuidSpp);
				// Log.i(TAG, "connecting to uncategorized");

				mSocket.connect();

				// Start the thread to manage the connection and perform
				// transmissions
				mConnectedThread = new ConnectedThread(mSocket, bufferlength,
						this);
				mConnectedThread.start();

				Log.i(TAG, "Connected to device " + mDevice.getName() + " ["
						+ mDevice.getAddress() + "]");
				// Set the status
				connected = true;
				return connected;
			} catch (IOException e) {
				Log.i(TAG, "Couldn't get a connection");
				Log.e(TAG, e.getMessage());
				connected = false;
				return connected;
			}

		} else {
			Log.i(TAG, "Address is not Bluetooth, please verify MAC.");
			connected = false;
			return connected;
		}
	}

	/**
	 * Opens a BluetoothServerSocket to listen for connections Primarily
	 * intended for Android-to-Android connections using UUID
	 * fa87c0d0-afac-11de-8a39-0800200c9a66
	 * 
	 * @return
	 */
	public synchronized void listen() {
		AcceptThread listenThread = new AcceptThread();
		listenThread.start();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 * 
	 * Based on the Android BluetoothChat example
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			// Create a new listening server socket
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(
						"SerialPortProfile", uuidSpp);
			} catch (IOException e) {
				Log.e(TAG, "Socket listen() failed", e);
			}
			mServerSocket = tmp;
		}

		public void run() {

			mSocket = null;

			// Listen to the server socket if we're not connected
			while (!isConnected()) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					mSocket = mServerSocket.accept();
					mDevice = mSocket.getRemoteDevice();
				} catch (IOException e) {
					Log.e(TAG, "Socket accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (mSocket != null) {
					synchronized (BtSerial.this) {
						if (!isConnected()) {
							try {
								// Situation normal. Start the connected thread.
								mConnectedThread = new ConnectedThread(mSocket,
										bufferlength, BtSerial.this);
								mConnectedThread.start();
								Log.i(TAG,
										"Connected to device "
												+ mDevice.getName() + " ["
												+ mDevice.getAddress() + "]");
								// Set the status
								connected = true;
							} catch (Exception ex) {
								Log.i(TAG, "Couldn't get a connection");
								Log.e(TAG, ex.getMessage());
								connected = false;
							}

						} else {
							try {
								mSocket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
		}

		public void cancel() {
			try {
				mServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Socket close() of server failed", e);
			}
		}
	}

	/**
	 * Returns the available number of bytes in the buffer.
	 * 
	 * @return
	 */
	public int available() {
		return mConnectedThread.available();
	}

	/**
	 * Displays the Library welcome message
	 */
	private void welcome() {
		Log.i(TAG, "##library.name## " + VERSION + " by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	/**
	 * Writes a byte[] buffer to the output stream.
	 * 
	 * @param buffer
	 */
	public void write(byte[] buffer) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (!connected)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(buffer);
	}

	/**
	 * Writes a String to the output stream.
	 * 
	 * @param thisString
	 */
	public void write(String thisString) {
		byte[] thisBuffer = thisString.getBytes();
		write(thisBuffer);
	}

	/**
	 * Writes a String to the output stream.
	 * 
	 * @param thisInt
	 */
	public void write(int thisInt) {
		byte[] thisBuffer = { (byte) thisInt };
		write(thisBuffer);
	}

	/**
	 * Returns the next byte in the buffer as an int (0-255);
	 * 
	 * @return
	 */
	public int read() {
		return mConnectedThread.read();
	}

	/**
	 * Returns the whole byte buffer.
	 * 
	 * @return
	 */
	public byte[] readBytes() {
		return mConnectedThread.readBytes();
	}

	/**
	 * Returns the available number of bytes in the buffer, and copies the
	 * buffer contents to the passed byte[]
	 * 
	 * @param outgoing
	 *            []
	 * @return
	 */
	public int readBytes(byte outgoing[]) {
		mConnectedThread.readBytes(outgoing);
		return outgoing.length;
	}

	/**
	 * Returns a byte buffer until the byte interesting. If the byte interesting
	 * doesn't exist in the current buffer, null is returned.
	 * 
	 * @param interesting
	 * @return array of bytes retreived from buffer
	 */
	public byte[] readBytesUntil(byte interesting) {
		return mConnectedThread.readBytesUntil(interesting);
	}

	// /**
	// * TODO
	// *
	// * @param b
	// * @param buffer
	// */
	// public void readBytesUntil(byte b, byte[] buffer) {
	// Log.i(TAG, "Will do a.s.a.p.");
	// }

	/**
	 * Read the next byte in the buffer as a char
	 * 
	 * @return next byte in the buffer as a char; if nothing is there it returns
	 *         -1.
	 */
	public char readChar() {
		return (char) read();
	}

	/**
	 * Returns the buffer as a string.
	 * 
	 * @return contents of the buffer as a String
	 */
	public String readString() {
		String returnstring = new String(readBytes());
		return returnstring;
	}

	/**
	 * Returns the buffer as string until character c.
	 * 
	 * @param c
	 *            - character to read until
	 * @return String data read before encountering c
	 */
	public String readStringUntil(char c) {
		/* Get the buffer as string */
		String stringbuffer = readString();

		int index;
		/* Make sure that the character exists in the string */
		if ((index = stringbuffer.indexOf(c)) > 0) {
			return stringbuffer.substring(0, index);
		} else {
			return null;
		}
	}

	/**
	 * Sets the number of bytes to buffer.
	 * 
	 * @param bytes
	 *            new size of the buffer
	 * @return
	 */
	public int buffer(int bytes) {
		return mConnectedThread.buffer(bytes);
	}

	/**
	 * Returns the last byte in the buffer.
	 * 
	 * @return the last byte in the buffer
	 * @see char()
	 */
	public int last() {
		return buffer[buffer.length - 1];
	}

	/**
	 * Returns the last byte in the buffer as char.
	 * 
	 * @return the last char in the buffer.
	 * @see last()
	 */
	public char lastChar() {
		return (char) buffer[buffer.length - 1];
	}

	/**
	 * Clears the byte buffer.
	 */
	public void clear() {
		mConnectedThread.clear();
	}

	/**
	 * Disconnects the Bluetooth socket.
	 * 
	 * This should be called in the pause() and stop() methods inside the sketch
	 * in order to ensure that the socket is properly closed when the sketch is
	 * not running. The connection should be re-established in a resume() method
	 * if the sketch loses and then regains focus.
	 * 
	 * @see connect()
	 */
	public synchronized void disconnect() {
		if (connected) {
			try {
				// kill the connected thread if it's running:
				if (mConnectedThread != null) {
					mConnectedThread.cancel();
					mConnectedThread = null;
				}

				/* Close the socket */
				mSocket.close();

				/* Set the connected state */
				connected = false;
				/* If it successfully closes I guess we just return a success? */
				// return 0;
				// Log.i(TAG, "disconnected.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i(TAG, "whoops! disconnect() encountred an error.");
				e.printStackTrace();
				/* Otherwise we'll go ahead and say "no, this didn't work well!" */
				// return 1;
			}
		}
	}

	/**
	 * Kills the main thread. Shouldn't stop when the connection disconnects.
	 * 
	 * @return
	 */
	public void stop() {

	}
}
