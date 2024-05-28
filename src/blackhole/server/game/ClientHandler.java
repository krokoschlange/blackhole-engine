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
package blackhole.server.game;

import blackhole.common.GameObject;
import blackhole.utils.Debug;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handler for {@link Client} objects on the server
 * @author fabian
 */
public class ClientHandler {

	/**
	 * A class to hold the singleton instance of {@code ClientHandler}
	 */
	private static class SingletonHelper {

		private static final ClientHandler INSTANCE = new ClientHandler();
	}

	/**
	 * A list of all connected {@link Client}s
	 */
	private CopyOnWriteArrayList<Client> clients;

	/**
	 * Creates a new {@code ClientHandler}
	 */
	private ClientHandler() {
		clients = new CopyOnWriteArrayList<>();
	}

	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static ClientHandler getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Adds a new {@link Client}
	 * @param client the {@link Client} to add
	 */
	public void addClient(Client client) {
		clients.add(client);
	}

	/**
	 * Removes a {@link Client}
	 * @param client the {@link Client} to remove
	 */
	public void removeClient(Client client) {
		clients.remove(client);
	}

	/**
	 * Causes all {@link Client}s to send updates to the clients
	 */
	public void update() {
		//Debug.log(clients.size() + "");
		for (GameObject obj : GameManager.getInstance().getObjectHandler().getObjects()) {
			obj.lockUpdate();
		}
		for (Client client : clients) {
			client.updateClient();
		}
	}
}
