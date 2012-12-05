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
import java.util.UUID;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectedThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final int mBufferLength;
	protected final InputStream mmInStream;
	protected final OutputStream mmOutStream;

	private int bufferlength = 128;
	private byte[] rawbuffer;
	private byte[] buffer;
	private int bufferIndex;
	private int bufferLast;
	private int available;
	private final String TAG = "System.out";

	private BtSerial mBtSerial;

	public ConnectedThread(BluetoothSocket socket, int bufferLength,
			BtSerial mBtSerial) {
		this.mBtSerial = mBtSerial;
		mmSocket = socket;

		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		mBufferLength = bufferLength;

		// Get the input and output streams, using temp objects because
		// member streams are final
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;

		buffer = new byte[mBufferLength]; // buffer store for the stream
		// Log.i(TAG, "started");
	}

	@Override
	public void run() {
		// Log.i(TAG, "ConnectedThread running");

		// Keep listening to the InputStream until an exception occurs
		while (true) {
			try {
				//String outputMessage = mmInStream.available() + " bytes available";
				//Log.i(TAG, outputMessage);
				// Read from the InputStream
				while (mmInStream.available() > 0) {

					synchronized (buffer) {
						if (bufferLast == buffer.length) {
							byte temp[] = new byte[bufferLast << 1];
							System.arraycopy(buffer, 0, temp, 0, bufferLast);
							buffer = temp;
						}
						buffer[bufferLast++] = (byte) mmInStream.read();
					}
					btSerialEvent();
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				break;
			}
		}
	}

	public void btSerialEvent() {
		mBtSerial.btSerialEvent();
		// Log.i(TAG, "btSerialEvent called from ConnectedThread");
	}

	/* Call this from the main Activity to send data to the remote device */
	public void write(byte[] bytes) {
		try {
			for(int i=0; i<bytes.length; i++) {
				mmOutStream.write(bytes[i] & 0xFF);	
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the next byte in the buffer as an int (0-255);
	 * 
	 * @return int value of the next byte in the buffer
	 */
	public int read() {
		if (bufferIndex == bufferLast)
			return -1;

		synchronized (buffer) {
			int outgoing = buffer[bufferIndex++] & 0xff;
			if (bufferIndex == bufferLast) { // rewind
				bufferIndex = 0;
				bufferLast = 0;
			}
			return outgoing;
		}
	}

	/**
	 * Returns the whole byte buffer.
	 * 
	 * @return
	 */
	public byte[] readBytes() {
		if (bufferIndex == bufferLast)
			return null;

		synchronized (buffer) {
			int length = bufferLast - bufferIndex;
			byte outgoing[] = new byte[length];
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

			bufferIndex = 0; // rewind
			bufferLast = 0;
			return outgoing;
		}
	}

	/**
	 * Returns the available number of bytes in the buffer, and copies the
	 * buffer contents to the passed byte[]
	 * 
	 * @param buffer
	 * @return
	 */
	public int readBytes(byte outgoing[]) {
		if (bufferIndex == bufferLast)
			return 0;

		synchronized (buffer) {
			int length = bufferLast - bufferIndex;
			if (length > outgoing.length)
				length = outgoing.length;
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

			bufferIndex += length;
			if (bufferIndex == bufferLast) {
				bufferIndex = 0; // rewind
				bufferLast = 0;
			}
			return length;
		}
	}

	/**
	 * Returns a byte buffer until the byte interesting. If the byte interesting
	 * doesn't exist in the current buffer, null is returned.
	 * 
	 * @param interesting
	 * @return
	 */
	public byte[] readBytesUntil(int interesting) {
		if (bufferIndex == bufferLast)
			return null;
		byte what = (byte) interesting;

		synchronized (buffer) {
			int found = -1;
			for (int k = bufferIndex; k < bufferLast; k++) {
				if (buffer[k] == what) {
					found = k;
					break;
				}
			}
			if (found == -1)
				return null;

			int length = found - bufferIndex + 1;
			byte outgoing[] = new byte[length];
			System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

			bufferIndex += length;
			if (bufferIndex == bufferLast) {
				bufferIndex = 0; // rewind
				bufferLast = 0;
			}
			return outgoing;
		}
	}

	//
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
		if (bufferIndex == bufferLast)
			return -1;
		synchronized (buffer) {
			int outgoing = buffer[bufferLast - 1];
			bufferIndex = 0;
			bufferLast = 0;
			return outgoing;
		}
	}

	/**
	 * Reads a byte from the buffer as char.
	 * 
	 * @return
	 */
	public char readChar() {
		if (bufferIndex == bufferLast)
			return (char) (-1);
		return (char) last();
	}

	/**
	 * Returns the last byte in the buffer as char.
	 * 
	 * @return
	 */
	public char lastChar() {
		if (bufferIndex == bufferLast)
			return (char) (-1);
		return (char) last();
	}

	public int available() {
		return (bufferLast - bufferIndex);
	}

	/**
	 * Ignore all the bytes read so far and empty the buffer.
	 */
	public void clear() {
		bufferLast = 0;
		bufferIndex = 0;
	}

	/* Call this from the main Activity to shutdown the connection */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}