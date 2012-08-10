/**
 * you can put a one sentence description of your library here.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package cc.arduino.btserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/* TODO */
// ** available()
// ** read()
// ** connect()
// ** readChar()
// ** readBytes()
// readBytesUntil()
// readString()
// readStringUntil()
// ** buffer()
// bufferUntil()
// ** last()
// ** lastChar()
// ** list()
// ** write()
// ** clear()
// ** stop()
// btSerialEvent()

//public class BtSerial implements Runnable {

public class BtSerial {

	/* PApplet context */
	private Context ctx;

	public final static String VERSION = "##version##";

	/* Bluetooth */
	private BluetoothAdapter mAdapter;
	private BluetoothDevice mDevice;
	private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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

	/* Debug variables */
	public static boolean DEBUG = true;
	public static String DEBUGTAG = "##name## ##version## Debug message: ";

	private final String TAG = "System.out";

	public BtSerial(Context ctx) {
		this.ctx = ctx;
		welcome();

		/* Init the adapter */
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "Handler running");
				mAdapter = BluetoothAdapter.getDefaultAdapter();
			}
		});
		Log.i(TAG, "started");
	}

	/**
	 * Returns the status of the connection.
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Returns whether the adapter is enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		if (mAdapter != null)
			return mAdapter.isEnabled();
		else
			return false;
	}

	/**
	 * Returns the list of bonded devices.
	 * 
	 * @return
	 */
	public String[] list() {
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
				list.addElement(thisDevice.getAddress());
			}
		} catch (UnsatisfiedLinkError e) {
			// errorMessage("devices", e);
		} catch (Exception e) {
			// errorMessage("devices", e);
		}

		String outgoing[] = new String[list.size()];
		list.copyInto(outgoing);
		return outgoing;
	}

	/*
	 * Some stubs for future implementation:
	 */
	public void startDiscovery() {
		// this method will start a separate thread to handle discovery
	}

	public void pairWith(String thisAddress) {
		// this method will pair with a device given a MAC address
	}

	public boolean discoveryComplete() {
		// this method will return whether discovery is complete,
		// so the user can then list devices
		return false;
	}

	public String getName() {
		if (mDevice != null)
			return mDevice.getName();
		else
			return "no device connected";
	}

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
				mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
				mSocket.connect();

				// Start the thread to manage the connection and perform
				// transmissions
				mConnectedThread = new ConnectedThread(mSocket, bufferlength);
				mConnectedThread.start();

				Log.i(TAG, "Connected to device " + mDevice.getName() + " ["
						+ mDevice.getAddress() + "]");
				// Set the status
				connected = true;
				return connected;
			} catch (IOException e) {
				Log.i(TAG, "Couldn't get a connection");
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
	 * Returns the available number of bytes in the buffer.
	 * 
	 * @return
	 */
	public int available() {
		return mConnectedThread.available();
	}

	/**
	 * 
	 */
	private void welcome() {
		Log.i(TAG, "##name## ##version## by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	// @Override
	// public void run() {
	// Log.i(TAG, "BTSerial running");
	// /* Init the buffer */
	// buffer = new byte[bufferlength];
	// rawbuffer = new byte[bufferlength];
	//
	// /* Set the connected state */
	// connected = true;
	//
	// while (connected) {
	// /* Read the available bytes into the buffer */
	// rawbuffer= mConnectedThread.read();
	// available = mConnectedThread.available();
	// String output = Integer.toString(available);
	// Log.i(TAG, output);
	// /* Clone the raw buffer */
	// buffer = rawbuffer.clone();
	// }
	// }

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
	 * @param buffer
	 * @return
	 */
	public int readBytes(byte outgoing[]) {
		mConnectedThread.readBytes(outgoing);
		return outgoing.length;
	}

	/**
	 * Returns a bytebuffer until the byte b. If the byte b doesn't exist in the
	 * current buffer, null is returned.
	 * 
	 * @param b
	 * @return
	 */
	public byte[] readBytesUntil(byte interesting) {
		return mConnectedThread.readBytesUntil(interesting);
	}

	/**
	 * TODO
	 * 
	 * @param b
	 * @param buffer
	 */
	public void readBytesUntil(byte b, byte[] buffer) {
		Log.i(TAG, "Will do a.s.a.p.");
	}

	/**
	 * Returns the next byte in the buffer as a char, if nothing is there it
	 * returns -1.
	 * 
	 * @return
	 */
	public char readChar() {
		return (char) read();
	}

	/**
	 * Returns the buffer as a string.
	 * 
	 * @return
	 */
	public String readString() {
		String returnstring = new String(readBytes());
		return returnstring;
	}

	/**
	 * Returns the buffer as string until character c.
	 * 
	 * @param c
	 * @return
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
	 * @return
	 */
	public int buffer(int bytes) {
		bufferlength = bytes;

		buffer = new byte[bytes];
		rawbuffer = buffer.clone();

		return bytes;
	}

	/**
	 * Returns the last byte in the buffer.
	 * 
	 * @return
	 */
	public int last() {
		return buffer[buffer.length - 1];
	}

	/**
	 * Returns the last byte in the buffer as char.
	 * 
	 * @return
	 */
	public char lastChar() {
		return (char) buffer[buffer.length - 1];
	}

	/**
	 * Clears the byte buffer.
	 */
	public void clear() {
		buffer = new byte[bufferlength];
		mConnectedThread.clear();
	}

	/**
	 * Disconnects the bluetooth socket.
	 * 
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
				Log.i(TAG, "disconnected.");
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
