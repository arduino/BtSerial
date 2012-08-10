/**
 * Blah Blah Blah
 */

package cc.arduino.btserial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

	public ConnectedThread(BluetoothSocket socket, int bufferLength) {
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
		Log.i(TAG, "started");
	}

	@Override
	public void run() {
		Log.i(TAG, "ConnectedThread running");
		int bytes; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs
		while (true) {
			try {
				// Read from the InputStream
				while (mmInStream.available() > 0) {
					// String outputMessage = mmInStream.available() +
					// " bytes available";
					// Log.i(TAG, outputMessage);
					synchronized (buffer) {
						if (bufferLast == buffer.length) {
							byte temp[] = new byte[bufferLast << 1];
							System.arraycopy(buffer, 0, temp, 0, bufferLast);
							buffer = temp;
						}
						buffer[bufferLast++] = (byte) mmInStream.read();
					}
				}
			} catch (IOException e) {
				break;
			}
		}
	}

	/* Call this from the main Activity to send data to the remote device */
	public void write(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// /**
	// * Return a byte array of anything that's in the serial buffer.
	// * Not particularly memory/speed efficient, because it creates
	// * a byte array on each read, but it's easier to use than
	// * readBytes(byte b[]) (see below).
	// */
	// public byte[] read() {
	// if (bufferIndex == bufferLast) return null;
	//
	// synchronized (buffer) {
	// int length = bufferLast - bufferIndex;
	// byte outgoing[] = new byte[length];
	// System.arraycopy(buffer, bufferIndex, outgoing, 0, length);
	//
	// bufferIndex = 0; // rewind
	// bufferLast = 0;
	// return outgoing;
	// }
	// }

	/**
	 * Returns the next byte in the buffer as an int (0-255);
	 * 
	 * @return
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

	// /**
	// * Returns a number between 0 and 255 for the next byte that's
	// * waiting in the buffer.
	// * Returns -1 if there was no byte (although the user should
	// * first check available() to see if things are ready to avoid this)
	// */
	// public int readByte() {
	// if (bufferIndex == bufferLast) return -1;
	//
	// synchronized (buffer) {
	// int outgoing = buffer[bufferIndex++] & 0xff;
	// if (bufferIndex == bufferLast) { // rewind
	// bufferIndex = 0;
	// bufferLast = 0;
	// }
	// return outgoing;
	// }
	// }

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
	 * Returns a bytebuffer until the byte b. If the byte b doesn't exist in the
	 * current buffer, null is returned.
	 * 
	 * @param b
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

	public String readStringUntil(int interesting) {
		byte b[] = readBytesUntil(interesting);
		if (b == null)
			return null;
		return new String(b);
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