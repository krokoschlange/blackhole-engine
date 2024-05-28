/* 
 * The MIT License
 *
 * Copyright 2020 fabian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package blackhole.server.network;

import blackhole.networkData.NetworkUpdate;
import blackhole.server.game.Client;
import blackhole.utils.Debug;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * UTility class to send data to a client and receive data from it
 * @author fabian
 */
public class ClientSocket {

	/**
	 * the {@link SocketChannel} that is used to send and receive data
	 */
    private SocketChannel socket;
	
	/**
	 * the {@link Client} that handles received data
	 */
    private Client client;
	
	/**
	 * Creates a new {@code ClientSocket} using the given {@link SocketChannel}
	 * @param schannel the {@link SocketChannel} to use
	 */
    public ClientSocket(SocketChannel schannel) {
        socket = schannel;
        client = new Client(this);
    }

	/**
	 * Sends the given {@link NetworkUpdate} to the client
	 * @param update the update to send
	 */
    public void sendUpdate(NetworkUpdate update) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(update);

            byte[] data = baos.toByteArray();
            ByteBuffer buffer;

            /*bfr1024.clear();
			bfr4096.clear();
			if (data.length + 4 <= 1024) {
				buffer = bfr1024;
			} else if (data.length + 4 <= 4096) {
				buffer = bfr4096;
			} else {
				
			}*/
            buffer = ByteBuffer.allocate(data.length + 4);
            //int dataSize = objBuffer.capacity();
            ByteBuffer intBuffer = ByteBuffer.allocate(4);

            /*intBuffer.putInt(data.length);
			intBuffer.position(0);
			if (dataSize > 4096) {
				Debug.log("sending big msg: " + dataSize + "B");
			}*/
            //ByteBuffer buffer = ByteBuffer.allocate(dataSize + 4);
            buffer.putInt(data.length);
            buffer.put(data);
            buffer.position(0);

            socket.write(buffer);
        } catch (ClosedChannelException e) {
			disconnect();
		}catch (IOException e) {
            Debug.logWarn("Client Update send failed");
			e.printStackTrace();
        } 
    }

	/**
	 * Reconstructs a {@link NetworkUpdate} from the given data and lets the
	 * {@link Client} associated with the {@code ClientSocket} handle the
	 * update
	 * @param buffer the data
	 */
    public void receiveData(ByteBuffer buffer) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
            ObjectInputStream ois = new ObjectInputStream(bais);
            try {
                NetworkUpdate data = (NetworkUpdate) ois.readObject();
                client.update(data);
            } catch (ClassNotFoundException e) {
                Debug.logError("Data reception failed: Class not found");
            }
        } catch (IOException e) {
            Debug.logError("Data reception failed: IOException (server)");
            e.printStackTrace();
        }
    }

	/**
	 * Returns the {@link SocketChannel} used by the {@code ClientSocket}
	 * @return the {@link SocketChannel} used by the {@code ClientSocket}
	 */
    public SocketChannel getSocketChannel() {
        return socket;
    }

	/**
	 * Closes the connection to the client and informs the associated
	 * {@link Client} about it
	 */
    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            Debug.logWarn("Disconnect Failed");
        }
        client.disconnect();
		ServerEndpoint.getInstance().removeClient(this);
    }
}
