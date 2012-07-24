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
		} catch (IOException e) { }

		mmInStream = tmpIn;
		mmOutStream = tmpOut;	

		buffer = new byte[mBufferLength];  // buffer store for the stream
		Log.i(TAG, "started");
	}

	@Override
	public void run() {
		Log.i(TAG, "running");
		int bytes; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs
		while (true) {
			try {
				// Read from the InputStream
				while (mmInStream.available() > 0) {
					String outputMessage = mmInStream.available() + " bytes available";
					Log.i(TAG, outputMessage);
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

	
	/**
	 * Return a byte array of anything that's in the serial buffer.
	 * Not particularly memory/speed efficient, because it creates
	 * a byte array on each read, but it's easier to use than
	 * readBytes(byte b[]) (see below).
	 */
	public byte[] read() {
	    if (bufferIndex == bufferLast) return null;

	    synchronized (buffer) {
	      int length = bufferLast - bufferIndex;
	      byte outgoing[] = new byte[length];
	      System.arraycopy(buffer, bufferIndex, outgoing, 0, length);

	      bufferIndex = 0;  // rewind
	      bufferLast = 0;
	      return outgoing;
	    }
	}
	
//	/**
//	   * Returns a number between 0 and 255 for the next byte that's
//	   * waiting in the buffer.
//	   * Returns -1 if there was no byte (although the user should
//	   * first check available() to see if things are ready to avoid this)
//	   */
//	  public int readByte() {
//	    if (bufferIndex == bufferLast) return -1;
//
//	    synchronized (buffer) {
//	      int outgoing = buffer[bufferIndex++] & 0xff;
//	      if (bufferIndex == bufferLast) {  // rewind
//	        bufferIndex = 0;
//	        bufferLast = 0;
//	      }
//	      return outgoing;
//	    }
//	  }


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