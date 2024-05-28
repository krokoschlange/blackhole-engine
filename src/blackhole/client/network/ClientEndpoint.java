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
package blackhole.client.network;

import blackhole.client.game.ClientManager;
import blackhole.client.game.ClientObjectHandler;
import blackhole.client.game.input.InputHandler;
import blackhole.client.graphicsEngine.Camera;
import blackhole.networkData.ClientDataUpdate;
import blackhole.networkData.NetworkUpdate;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Manages the connection to a server.
 *
 * @author fabian
 */
public class ClientEndpoint implements Serializable {

	/**
	 * A class to hold the singleton instance of {@code ClientEndpoint}
	 */
	private static class SingletonHelper {

		private static final ClientEndpoint INSTANCE = new ClientEndpoint();
	}

	/**
	 * The {@link SocketChannel} used by the endpoint
	 */
	private SocketChannel socket;

	/**
	 * The {@link Selector} used by the endpoint
	 */
	private Selector selector;

	/**
	 * The {@link Thread} where data is received
	 */
	private Thread loop;

	/**
	 * whether the endpoint is currently connected to a server
	 */
	private boolean isConnected;

	/**
	 * A buffer that will read the size of a package (an integer)
	 */
	private ByteBuffer bytesToComeBfr;

	/**
	 * A 256 byte buffer. The instance is kept to reduce unnecessary memory
	 * allocations.
	 */
	private ByteBuffer bfr256;

	/**
	 * A 1024 byte buffer. The instance is kept to reduce unnecessary memory
	 * allocations.
	 */
	private ByteBuffer bfr1024;

	/**
	 * A 4096 byte buffer. The instance is kept to reduce unnecessary memory
	 * allocations.
	 */
	private ByteBuffer bfr4096;

	/**
	 * Bytes still not read, used if the message has only been read partially
	 */
	private int bytesInMessage;
	
	/**
	 * The size of the current message in bytes
	 */
	private int messageSize;

	/**
	 * currently selected buffer to read messages into
	 */
	private ByteBuffer currentBuffer;

	/**
	 * Creates a new {@code ClientEndpoint}
	 */
	private ClientEndpoint() {
		bytesToComeBfr = ByteBuffer.allocate(4);
		bfr256 = ByteBuffer.allocate(256);
		bfr1024 = ByteBuffer.allocate(1024);
		bfr4096 = ByteBuffer.allocate(4096);
	}

	/**
	 * Returns the singleton instance
	 *
	 * @return the singleton instance
	 */
	public static ClientEndpoint getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Starts receiving data if connected and runs until it is disconnected,
	 * loses connection or crashes
	 */
	public void connectionLoop() {
		// if a connection is present
		if (socket.isConnected()) {
			//start a new thread
			loop = new Thread("client network thread") {
				@Override
				public void run() {
					//run (almost) forever
					while (!loop.isInterrupted() && isConnected) {
						connectionStep();
					}
				}

			};
			loop.setName(
					"client network thread");
			loop.start();
		}
	}

	private void connectionStep() {
		try {
			//select keys
			selector.select();

			//if something happened
			if (selector.isOpen()) {
				//get the keys
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = keys.iterator();

				//iterate over them
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();

					//abort if key is not readable
					if (!key.isReadable()) {
						return;
					}

					//read a new message if we have finished the last one
					if (bytesInMessage == 0) {
						//clear all buffers
						bytesToComeBfr.clear();
						bfr256.clear();
						bfr1024.clear();
						bfr4096.clear();

						//read how many bytes to expect
						int bytesRead = socket.read(bytesToComeBfr);

						//abort if we can't read anything
						if (bytesRead <= 0) {
							return;
						}
						//see how many bytes we can expect
						messageSize = bytesToComeBfr.getInt(0);

						//abort if the data doesn't makes any sense
						if (messageSize <= 0) {
							messageSize = 0;
							return;
						}

						//select a buffer that fits the incoming message
						currentBuffer = null;// = ByteBuffer.allocate(bytesToCome);
						if (messageSize <= 256) {
							currentBuffer = bfr256;
						} else if (messageSize <= 1024) {
							currentBuffer = bfr1024;
						} else if (messageSize <= 4096) {
							currentBuffer = bfr4096;
						} else if (messageSize < 100000000) {
							//very big message, we need a big buffer
							Debug.log("big msg: " + messageSize + "B");
							currentBuffer = ByteBuffer.allocate(messageSize);
						} /*else {
							Debug.log("big msg: " + messageSize + "B");
							currentBuffer = ByteBuffer.allocate(messageSize);
						}*/
						//abort if we don't have a buffer (we should)
						if (currentBuffer == null) {
							return;
						}

						//read the message
						currentBuffer.position(currentBuffer.limit() - messageSize);
						bytesInMessage = messageSize;
					}

					int dataRead = socket.read(currentBuffer);
					
					bytesInMessage -= dataRead;
					
					if (bytesInMessage <= 0) {
						bytesInMessage = 0;
						currentBuffer.position(currentBuffer.limit() - messageSize);
						currentBuffer.compact();
						currentBuffer.position(0);

						//convert it to a stream
						byte[] bytes = currentBuffer.array();
						ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
						ObjectInputStream ois = new ObjectInputStream(bais);

						try {
							//get the update from the stream
							NetworkUpdate update = (NetworkUpdate) ois.readObject();
							/*if (bytesToCome > 4096) {
								Debug.log(update);
							}*/
							//handle it
							handleClientUpdate(update);
						} catch (ClassNotFoundException e) {
							Debug.logError("Data deserialization failed: ClassNotFoundException; " + e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			Debug.logError("Data deserialization failed:");
			Debug.logError(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Tries to connect to a server until successful
	 *
	 * @param address the address of the server
	 * @param port the port of the server
	 */
	public void forceConnect(String address, int port) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				boolean connected = false;
				while (!connected) {
					connected = connect(address, port);
				}
				connectionLoop();
			}
		};
		thread.start();
	}

	/**
	 * Tries to connect to a server
	 *
	 * @param address the address of the server
	 * @param port the port of the server
	 * @return true if successful
	 */
	public boolean connect(String address, int port) {
		try {
			//initialize everything
			if (socket == null) {
				socket = SocketChannel.open();
			}
			if (selector == null) {
				selector = Selector.open();
			}
			socket.configureBlocking(false);
			//connect
			if (!socket.connect(new InetSocketAddress(address, port))) {
				//not immediately connected, we need to wait
				socket.register(selector, SelectionKey.OP_CONNECT);
			}
			boolean connected = false;

			//wait until we can finish the connection
			while (!connected) {
				selector.select();
				if (selector.isOpen()) {
					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();

					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						if (key.isConnectable()) {
							//finish the connection
							connected = socket.finishConnect();
						}
					}
				}
			}
			//set socket channel up for reading
			socket.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			Debug.logError("Connect failed");
			e.printStackTrace();
			return false;
		}

		//send an initial update
		ClientDataUpdate initialUpdate = new ClientDataUpdate();
		Camera cam = ClientManager.getInstance().getServerObjectHandler().getCamera();
		initialUpdate.camPos = cam.getPosition();
		initialUpdate.camRot = cam.getRotation();
		initialUpdate.camSize = cam.getSize();
		initialUpdate.mousePos = InputHandler.getInstance().getMouseControl().getMousePosition();
		initialUpdate.mouseScroll = InputHandler.getInstance().getMouseControl().getWheelRotation();
		//AbstractGameWindow win = ClientManager.getInstance().getGraphicsBackend().getWindow();
		//initialUpdate.windowSize = new Vector(win.getWidth(), win.getHeight());
		initialUpdate.windowSize = ClientManager.getInstance().getServerObjectHandler().getPanel().size;
		initialUpdate.unloadDistance = Double.parseDouble(Settings.getProperty("object_sending_range"));
		initialUpdate.unloadTime = Double.parseDouble(Settings.getProperty("object_unload_time"));

		sendData(initialUpdate);
		ClientManager.getInstance().onConnect();
		isConnected = true;
		return true;
	}

	/**
	 * Disconnects the endpoint from the server
	 */
	public void disconnect() {
		try {
			loop.interrupt();
			selector.close();
			socket.close();
			socket = null;
			selector = null;
		} catch (IOException e) {
			Debug.logError("Disconnect failed");
		}
		Debug.log("Disconnected");
		ClientManager.getInstance().onDisconnect();
		isConnected = false;
	}

	/**
	 * Returns true if connected to a server
	 *
	 * @return true if connected to a server
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Sends an update to the server
	 *
	 * @param data the update to send
	 */
	public void sendData(NetworkUpdate data) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			ByteBuffer dataBuffer = ByteBuffer.wrap(baos.toByteArray());
			int dataSize = dataBuffer.capacity();
			ByteBuffer intBuffer = ByteBuffer.allocate(4);
			intBuffer.putInt(dataSize);
			intBuffer.position(0);
			ByteBuffer buffer = ByteBuffer.allocate(dataSize + 4);
			buffer.put(intBuffer).put(dataBuffer);
			buffer.position(0);
			socket.write(buffer);
		} catch (IOException e) {
			Debug.logError("Could not send data: IOException");
		} catch (NotYetConnectedException e) {
			Debug.logError("Could not send data: not connected");
		}
	}

	/**
	 * Handles an update received from the server
	 *
	 * @param update the update received
	 */
	public void handleClientUpdate(NetworkUpdate update) {
		ClientObjectHandler handler = ClientManager.getInstance().getServerObjectHandler();
		if (update != null) {
			handler.handleUpdate(update);
		} else {
			Debug.log("received null update");
		}
		/*if (null != update.updateType) {
			switch (update.updateType) {
				case objectUpdate:
					ServerSentObject object = (ServerSentObject) handler.getObjectByID(((ClientObjectUpdate) update).objectID);
					if (object != null) {
						object.update((ClientObjectUpdate) update);
					}
					break;
				case objectSpawn:

					ClientObjectSpawnUpdate spawnUpdate = (ClientObjectSpawnUpdate) update;
					/*try {
                        Class<?> clazz = Class.forName(spawnUpdate.clientObjectClass);
                        Constructor<?> cons = clazz.getConstructor();
                        ClientObject obj = (ClientObject) cons.newInstance();
                        obj.setHandler(handler, spawnUpdate.objectID);
                        obj.activate();
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        Debug.logError("Could not spawn sent object");
                        e.printStackTrace();
                    }*
					handler.addServerSentObject(spawnUpdate);
					break;
				case objectRemove:
					ClientObject obj = handler.getObjectByID(((ClientObjectRemovalUpdate) update).objectID);
					if (obj != null) {
						obj.remove();
					}
					break;
				case serverData:
					ClientServerDataUpdate serverDataUpdate = (ClientServerDataUpdate) update;
					ClientManager.getInstance().getServerObjectHandler().setScale(serverDataUpdate.gameScale);
					break;
				default:
					break;
			}
		}*/
	}
}
