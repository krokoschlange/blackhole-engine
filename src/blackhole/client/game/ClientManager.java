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
package blackhole.client.game;

import blackhole.client.game.input.InputHandler;
import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.client.graphicsEngine.opengl.GLGraphicsBackend;
import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.client.graphicsEngine.java2d.J2DGraphicsBackend;
import blackhole.client.network.ClientEndpoint;
import blackhole.client.soundEngine.SoundCore;
import blackhole.common.GameObject;
import blackhole.networkData.ClientDataUpdate;
import blackhole.networkData.ObjectUpdate;
import blackhole.server.game.GameManager;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The main class controlling the game client
 *
 * @author fabian
 */
public class ClientManager {

	/**
	 * A class to hold the singleton instance of {@code ClientManager}
	 */
	private static class SingletonHelper {

		private static final ClientManager INSTANCE = new ClientManager();
	}

	/**
	 * Stores the {@link ClientObjectHandler} that handles server-sent objects
	 */
	private ClientObjectHandler serverObjectHandler;

	/**
	 * Stores {@link ClientObjectHandler}s which are client-side only
	 */
	private CopyOnWriteArrayList<ClientObjectHandler> objectHandlers;

	/**
	 * The client's {@link GraphicsBackend}
	 */
	private GraphicsBackend graphics;

	/**
	 * The {@code Thread} in which {@link ClientObject}s are updated
	 */
	private Thread updateLoop;

	/**
	 * If this client is still running
	 */
	private boolean running;

	/**
	 * The base path of the client.
	 */
	private String basePath;

	/**
	 * Stores the current server object handler's panel's size. This is used to
	 * check if it changed and if it did, inform the server about it
	 */
	private Vector ssoHandlerPanelSize;

	/**
	 * Creates a new, empty {@code ClientManager}
	 */
	private ClientManager() {
		objectHandlers = new CopyOnWriteArrayList<>();
	}

	/**
	 * Get the singleton instance
	 *
	 * @return The singleton instance of this class
	 */
	public static ClientManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Returns the client's {@link GraphicsBackend}
	 *
	 * @return the client's {@link GraphicsBackend}
	 */
	public GraphicsBackend getGraphicsBackend() {
		return graphics;
	}

	/**
	 * Initialize a game with settings imported from the specified file. This
	 * method sets this client's base path to be the path to the executable.
	 *
	 * @param settingsfile the relative path to the file that the settings
	 * should be read from (relative to the client's base path)
	 * @return true if successful
	 *
	 * @see #getBasePath()
	 */
	public boolean loadGame(String settingsfile) {
		try {
			basePath = new File(GameManager.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getPath();
		} catch (URISyntaxException e) {
			Debug.logError("FATAL: COULD NOT FIND BASE PATH");
			return false;
		}
		loadSettings(settingsfile);
		setUpWindow();
		return true;
	}

	/**
	 * Initialize a game with settings imported from the specified file and a
	 * base path
	 *
	 * @param settingsfile the relative path to the file that the settings
	 * should be read from (relative to the client's base path)
	 * @param bP the client's base path
	 * @return true if successful
	 *
	 * @see #getBasePath()
	 */
	public boolean loadGame(String settingsfile, String bP) {
		basePath = bP;
		loadSettings(settingsfile);
		setUpWindow();
		return true;
	}

	/**
	 * Sets up the Settings class with settings imported from the specified file
	 *
	 * @param filename the relative path to the file that the settings should be
	 * read from (relative to the client's base path)
	 *
	 * @see #getBasePath()
	 */
	private void loadSettings(String filename) {
		Settings.setup(basePath + filename);
	}

	/**
	 * Sets up the game window
	 */
	private void setUpWindow() {
		String title = Settings.getProperty("title");
		int width = Integer.parseInt(Settings.getProperty("width"));
		int height = Integer.parseInt(Settings.getProperty("height"));
		int bufferSize = Integer.parseInt(Settings.getProperty("buffer_size"));

		String gbSetting = Settings.getProperty("graphics_backend");
		if (gbSetting != null && gbSetting.equals("gl")) {
			graphics = new GLGraphicsBackend(false);
		} else if (gbSetting != null && gbSetting.equals("glawt")) {
			graphics = new GLGraphicsBackend(true);
		} else {
			graphics = new J2DGraphicsBackend();
		}
		AbstractGameWindow window = graphics.getWindow();
		window.setTitle(title);
		window.setWidth(width);
		window.setHeight(height);
		window.start();

	}

	/**
	 * Sets the {@link ClientObjectHandler} that should handle objects that are
	 * sent from the server
	 */
	public void setServerObjectHandler(ClientObjectHandler handler) {
		if (serverObjectHandler != null) {
			ClientObjectHandler oldHandler = serverObjectHandler;
			serverObjectHandler = null;
			oldHandler.cleanUp();
		}
		if (handler != null) {
			handler.init();
		}
		serverObjectHandler = handler;

	}

	/**
	 * Returns the {@link ClientObjectHandler} that currently handles objects
	 * that were sent from the server
	 *
	 * @return the {@link ClientObjectHandler} that currently handles
	 * server-sent objects
	 */
	public ClientObjectHandler getServerObjectHandler() {
		return serverObjectHandler;
	}

	/**
	 * Adds a {@link ClientObjectHandler} to the client if it has not already
	 * been added.
	 *
	 * @param handler the {@link ClientObjectHandler} to be added
	 */
	public void addObjectHandler(ClientObjectHandler handler) {
		if (!objectHandlers.contains(handler)) {
			handler.init();
			objectHandlers.add(handler);
		}
	}

	/**
	 * Remove a {@link ClientObjectHandler} from the client
	 *
	 * @param handler the {@link ClientObjectHandler} to be removed
	 */
	public void removeObjectHandler(ClientObjectHandler handler) {
		objectHandlers.remove(handler);
		handler.cleanUp();
	}

	/**
	 * This method is called when the {@link ClientEndpoint} connects to a
	 * server. Can be overridden.
	 */
	public void onConnect() {

	}

	/**
	 * This method is called when the {@link ClientEndpoint} disconnects from a
	 * server. Can be overridden.
	 */
	public void onDisconnect() {

	}

	/**
	 * Called when the game window changed its size. This method calls the
	 * {@link ClientObjectHandler#windowResized(int, int)} method of all active
	 * {@link ClientObjectHandler}s
	 *
	 * @param newW the new window width
	 * @param newH the new window height
	 */
	public void windowResized(int newW, int newH) {
		if (serverObjectHandler != null) {
			serverObjectHandler.windowResized(newW, newH);
		}
		for (int i = 0; i < objectHandlers.size(); i++) {
			objectHandlers.get(i).windowResized(newW, newH);
		}
	}

	/**
	 * Start the client update Thread
	 */
	public void clientLoop() {
		running = true;
		updateLoop = new Thread() {
			@Override
			public void run() {
				//update speed, in frames per second
				double updateSpeed = Double.parseDouble(Settings.getProperty("client_update_speed"));

				//frame time (time available per frame)
				double updateTime = 1e9 / updateSpeed;

				//the current time
				double now;

				//the time of the last update
				double last = System.nanoTime();

				//whether we should skip a frame (if we are too slow)
				boolean skip = false;

				while (running && !Thread.interrupted()) {
					now = System.nanoTime();

					if (!skip) {
						//calculate the game step in all ClientObjectHandlers
						if (getServerObjectHandler() != null) {
							serverObjectHandler.gameStep((now - last) / 1e9);
						}
						for (ClientObjectHandler handler : objectHandlers) {
							handler.gameStep((now - last) / 1e9);
						}

						//update sound positions
						SoundCore.getInstance().updateSounds((now - last) / 1e9);

						//send updates to the server
						if (ClientEndpoint.getInstance().isConnected()) {
							//input update (mouse & keyboard)
							InputHandler.getInstance().sendUpdate();

							if (serverObjectHandler != null) {
								/* update containing data about the client
								e.g. the camera position
								 */
								ClientDataUpdate updt = serverObjectHandler.getCamera().getUpdate();
								if (!serverObjectHandler.getPanel().size.equals(ssoHandlerPanelSize)) {
									if (updt == null) {
										updt = new ClientDataUpdate();
									}
									ssoHandlerPanelSize = new Vector(serverObjectHandler.getPanel().size);
									updt.windowSize = ssoHandlerPanelSize;
								}
								if (updt != null) {
									ClientEndpoint.getInstance().sendData(updt);
								}

								//object updates from the server object handler
								CopyOnWriteArrayList<GameObject> objects = serverObjectHandler.getObjects();
								for (GameObject obj : objects) {
									obj.lockUpdate();
									ObjectUpdate update = obj.getUpdate();
									if (update != null && update.data != null) {
										ClientEndpoint.getInstance().sendData(update);
									}
								}
							}
						}

						last = now;
					}

					//sleep if we have too much time to save cpu time
					long sleepTime = (long) ((updateTime - (System.nanoTime() - now)) / 1e6);
					skip = false;
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							break;
						}
					} else if (sleepTime < -updateTime) {
						// if we are more than 1 frame behind we skip the next one to catch up
						skip = true;
					}
				}
				Debug.log("Game died\n RIP Game 2020 - 2020");
			}
		};
		updateLoop.setName("client update thread");
		updateLoop.start();

	}

	/**
	 * Draw all objects in the active {@link ClientObjectHandler}s in this
	 * client
	 *
	 * @param dtime the time since the last frame
	 */
	public void draw(double dtime) {
		// sort the ClientObjectHandlers in the correct drawing order
		ClientObjectHandler[] sorted;
		if (serverObjectHandler != null) {
			sorted = new ClientObjectHandler[objectHandlers.size() + 1];
			sorted = objectHandlers.toArray(sorted);
			sorted[sorted.length - 1] = serverObjectHandler;
		} else {
			sorted = new ClientObjectHandler[objectHandlers.size()];
			sorted = objectHandlers.toArray(sorted);
		}

		Arrays.sort(sorted, new Comparator<ClientObjectHandler>() {
			@Override
			public int compare(ClientObjectHandler t, ClientObjectHandler t1) {
				return t.getDrawPosition() - t1.getDrawPosition();
			}
		});
		//draw them
		for (ClientObjectHandler handler : sorted) {
			handler.onDraw(dtime);
			handler.draw();
		}
	}

	/**
	 * Returns true if the client is still running and updating the
	 * {@link ClientObject}s
	 *
	 * @return true if the client is still running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Stop the client update thread
	 */
	public void killClient() {
		if (updateLoop != null) {
			updateLoop.interrupt();
		}
		running = false;
	}

	/**
	 * Called by the window when it is closed
	 */
	public void windowClosed() {
		killClient();
	}

	/**
	 * Returns the base path of the running game
	 *
	 * @return the base path of the running game, as a {@code String}
	 */
	public String getBasePath() {
		return basePath;
	}
}
