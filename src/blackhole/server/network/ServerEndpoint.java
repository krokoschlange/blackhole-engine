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

import blackhole.utils.Debug;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

/**
 * The server networking endpoint. Handles connection requests by clients.
 *
 * @author fabian
 */
public class ServerEndpoint {

	/**
	 * A class to hold the singleton instance of {@code ServerEndpoint}
	 */
	private static class SingletonHelper {

		private static final ServerEndpoint INSTANCE;

		static {
			ServerEndpoint inst = null;
			try {
				inst = new ServerEndpoint();
			} catch (IOException e) {
				Debug.logError("FATAL: ServerEndpoint creation failed");
			}
			INSTANCE = inst;
		}
	}

	/**
	 * The {@link Selector} used by the endpoint
	 */
	private Selector selector;

	/**
	 * The {@link SocketChannel} used by the endpoint
	 */
	private ServerSocketChannel serverSocket;

	/**
	 * List of clients that are connected
	 */
	private ArrayList<ClientSocket> clients;

	/**
	 * The thread that runs the networking loop.
	 */
	private Thread loop;

	/**
	 * Creates a new {@code ServerEndpoint}
	 *
	 * @throws IOException if something goes wrong
	 */
	private ServerEndpoint() throws IOException {
		selector = Selector.open();
		serverSocket = ServerSocketChannel.open();
		serverSocket.configureBlocking(false);

		clients = new ArrayList<>();

		int ops = serverSocket.validOps();
		serverSocket.register(selector, ops);
	}

	/**
	 * Returns the singleton instance
	 *
	 * @return the singleton instance
	 */
	public static ServerEndpoint getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Binds the server to a port and a wildcart ip address
	 *
	 * @param port the port
	 * @return true if successful
	 */
	public boolean bind(int port) {
		/*InetAddress address = null;

		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface ni = e.nextElement();
			Debug.log(ni.getName());
			Enumeration<InetAddress> adrs = ni.getInetAddresses();
			while (adrs.hasMoreElements()) {
				InetAddress addr = adrs.nextElement();
				if (!addr.isLinkLocalAddress()
						&& !addr.isLoopbackAddress()
						&& addr instanceof Inet4Address) {
					Debug.log(addr.getHostAddress());
					address = addr;
				} else {
					Debug.log(addr.getHostAddress() + "(fail)");
				}
			}
		}

		if (address != null) {
			return bind(new InetSocketAddress(port));
		}*/
		return bind(new InetSocketAddress(port));
	}

	/**
	 * Binds the server to the given address
	 *
	 * @param addr the address
	 * @return true if successful
	 */
	public boolean bind(InetSocketAddress addr) {
		try {
			serverSocket.bind(addr);
			return true;
		} catch (IOException e) {
			Debug.logError("ServerEndpoint binding failed");
			return false;
		}
	}

	/**
	 * Binds the server to the given address and port
	 *
	 * @param addr the ip address
	 * @param port the port
	 * @return true if successful
	 */
	public boolean bind(String addr, int port) {
		return bind(new InetSocketAddress(addr, port));
	}

	/**
	 * Starts the server network loop
	 */
	public void start() {
		loop = new Thread("server network thread") {
			@Override
			public void run() {
				while (!isInterrupted()) {
					try {
						selector.select();
					} catch (IOException e) {
						Debug.logError("ServerEndpoint IOException (during selection): " + e.getMessage());
					}

					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();

					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();

						try {
							if (key.isAcceptable()) {
								SocketChannel client = serverSocket.accept();
								client.configureBlocking(false);
								client.register(selector, SelectionKey.OP_READ);
								clients.add(new ClientSocket(client));
							}
							if (key.isReadable()) {
								SocketChannel schannel = (SocketChannel) key.channel();
								ClientSocket socket = getClientSocketBySocket(schannel);
								ByteBuffer buffer = ByteBuffer.allocate(4);
								int bytesRead = schannel.read(buffer);
								if (bytesRead > 0) {
									int bytesToCome = buffer.getInt(0);
									ByteBuffer data = ByteBuffer.allocate(bytesToCome);
									schannel.read(data);

									if (socket != null) {
										socket.receiveData(data);
									}
								} else {
									if (socket != null) {
										socket.disconnect();
									}
									//schannel.close();
									key.cancel();
								}
							}
						} catch (IOException e) {
							SocketChannel schannel = (SocketChannel) key.channel();
							ClientSocket socket = getClientSocketBySocket(schannel);
							socket.disconnect();
							//schannel.close();
							key.cancel();
						}

						iterator.remove();
					}
				}
			}
		};
		loop.setName("server network thread");
		loop.start();
	}

	/**
	 * Stops the server network loop
	 */
	public void stop() {
		loop.interrupt();
	}

	/**
	 * Returns the {@link ClientSocket} that uses the given
	 * {@link SocketChannel}
	 *
	 * @param schannel the {@link SocketChannel}
	 * @return the {@link ClientSocket} that uses the given
	 * {@link SocketChannel}
	 */
	public ClientSocket getClientSocketBySocket(SocketChannel schannel) {
		ClientSocket socket = null;
		for (ClientSocket s : clients) {
			if (s.getSocketChannel().equals(schannel)) {
				socket = s;
				break;
			}
		}
		if (socket == null) {
			Debug.logError("Could not find Client by Socket");
		}
		return socket;
	}

	/**
	 * Removes the given client from the data handling
	 * @param client the client to remove
	 */
	public void removeClient(ClientSocket client) {
		clients.remove(client);
	}
}
