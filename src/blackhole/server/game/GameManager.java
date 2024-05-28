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

import blackhole.utils.Debug;
import blackhole.utils.Settings;
import java.io.File;
import java.net.URISyntaxException;

/**
 * The main class controlling the game server.
 *
 * @author fabian
 */
public class GameManager {

	/**
	 * A class to hold the singleton instance of {@code GameManager}
	 */
	private static class SingletonHelper {

		private static final GameManager INSTANCE = new GameManager();
	}

	/**
	 * The {@code Thread} in which {@link ServerObject}s are updated, the game
	 * loop
	 */
	private Thread loop;

	/**
	 * Whether the game loop is running
	 */
	private boolean running;

	/**
	 * The currently used {@link ServerObjectHandler}
	 */
	private static ServerObjectHandler objectHandler;

	/**
	 * The base base path to which resource paths are relative
	 */
	private String basePath;

	/**
	 * The game scale of the server, i.e. the factor textures will be scaled
	 * down by without affecting position
	 */
	private double gameScale;

	/**
	 * Returns the singleton instance
	 *
	 * @return the singleton instance
	 */
	public static GameManager getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Creates a new {@code GameManager}
	 */
	private GameManager() {
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
		gameScale = 1;
		if (Settings.getProperty("game_scale") != null) {
			gameScale = Double.parseDouble(Settings.getProperty("game_scale"));
		}
	}

	/**
	 * Sets the object handler to be used
	 * @param handler the new handler
	 */
	public void setObjectHandler(ServerObjectHandler handler) {
		if (objectHandler != null) {
			ServerObjectHandler oldHandler = objectHandler;
			objectHandler = null;
			oldHandler.cleanUp();
			// send update to make sure all objects on the clients get removed
			ClientHandler.getInstance().update();
		}
		if (handler != null) {
			handler.init();
		}
		objectHandler = handler;

	}

	/**
	 * Returns the currently used {@link ServerObjectHandler}
	 * @return the currently used {@link ServerObjectHandler}
	 */
	public ServerObjectHandler getObjectHandler() {
		return objectHandler;
	}

	/**
	 * Returns the game scale used on the server
	 * @return the game scale used on the server
	 */
	public double getGameScale() {
		return gameScale;
	}

	/**
	 * Starts the server game loop
	 */
	public void gameLoop() {
		running = true;
		loop = new Thread("server thread") {
			public void run() {
				double updateSpeed = Double.parseDouble(Settings.getProperty("update_speed"));
				double updateTime = 1e9 / updateSpeed;
				double now;
				double last = System.nanoTime();
				boolean skip = false;

				while (running && !loop.isInterrupted()) {
					now = System.nanoTime();

					if (!skip) {
						objectHandler.gameStep((now - last) / 1e9);
						ClientHandler.getInstance().update();
						last = now;
					}

					long sleepTime = (long) ((updateTime - (System.nanoTime() - now)) / 1e6);
					skip = false;
					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							running = false;
						}
					} else if (sleepTime < -updateTime) {
						skip = true;
					}
				}
				Debug.log("Game stopped.");
			}
		};
		loop.setName("server thread");
		loop.start();

	}

	/**
	 * Returns true if the game loop is running
	 * @return true if the game loop is running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * stops the game loop
	 */
	public void killGame() {
		running = false;
		loop.interrupt();
	}

	/**
	 * Returns the base path of the game
	 *
	 * @return the base path of the game, as a {@code String}
	 */
	public String getBasePath() {
		return basePath;
	}
}
