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

import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.client.graphicsEngine.AbstractDrawer;
import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.client.soundEngine.PositionalSound;
import blackhole.common.GameObject;
import blackhole.common.ObjectHandler;
import blackhole.networkData.NetworkUpdate;
import blackhole.networkData.ObjectRemoved;
import blackhole.networkData.ObjectSpawn;
import blackhole.networkData.ObjectUpdate;
import blackhole.networkData.ServerDataUpdate;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class extending {@link ObjectHandler} for use on the client. Adds handling
 * of a camera and a panel where the contents of the handler are drawn to.
 * @author fabian
 */
public abstract class ClientObjectHandler extends ObjectHandler {

	/**
	 * The camera of this handler
	 */
	private final Camera camera;
	
	/**
	 * The panel inside the game window that this handler gets drawn to
	 */
	private HandlerPanel panel;
	
	/**
	 * The ID that will be given to the next object that is added to the handler
	*/
	private int nextObjectID;
	
	/**
	 * The draw position of the object handler. Handlers with a higher draw
	 * position are being drawn later (i.e. on top)
	 */
	private int drawPosition;

	/**
	 * The game scale of the object handler. This describes how big 1 unit in
	 * the game world is as an amount of pixels of the texture of an object with
	 * scale 1
	 * e.g.: if it is 10 then 1 unit in the game world is equal to the size of 
	 * 10 pixels in the texture of an object that has a scale if 1
	 * 
	 * This is useful to have smaller coordinates.
	 */
	private double scale;

	/**
	 * A list of sounds that are currently playing
	 */
	private CopyOnWriteArrayList<PositionalSound> sounds;

	/**
	 * Creates a new, empty {@code ClientObjectHandler} and sets its scale to
	 * the one specified in the settings or 1
	 */
	public ClientObjectHandler() {
		camera = new Camera();
		AbstractGameWindow win = ClientManager.getInstance().getGraphicsBackend().getWindow();
		camera.setHeight(win.getHeight());
		camera.setWidth(win.getWidth());

		nextObjectID = -1;
		drawPosition = 0;

		sounds = new CopyOnWriteArrayList<>();

		scale = 1;
		if (Settings.getProperty("game_scale") != null) {
			scale = Double.parseDouble(Settings.getProperty("game_scale"));
		}
	}

	public Camera getCamera() {
		return camera;
	}

	public HandlerPanel getPanel() {
		return panel;
	}

	public void setPanel(HandlerPanel pan) {
		panel = pan;
	}

	public void setScale(double s) {
		scale = s;
	}

	public double getScale() {
		return scale;
	}

	/**
	 * Handles an update received from the server
	 * @param update the update to be handled
	 */
	@Override
	public void handleUpdate(NetworkUpdate update) {
		switch (update.updateType) {
			case NetworkUpdate.OBJECT_SPAWN:
				addServerSentObject((ObjectSpawn) update);
				break;
			case NetworkUpdate.OBJECT_UPDATE: {
				ObjectUpdate objUpdate = (ObjectUpdate) update;
				GameObject obj = getObjectByID(objUpdate.objectID);
				if (obj != null) {
					obj.update(objUpdate);
				}
				break;
			}
			case NetworkUpdate.OBJECT_REMOVAL: {
				GameObject obj = getObjectByID(((ObjectRemoved) update).objectID);
				if (obj != null) {
					obj.remove();
				}
				break;
			}
			case NetworkUpdate.SERVER_DATA: {
				ServerDataUpdate sdata = (ServerDataUpdate) update;
				setScale(sdata.gameScale);
				break;
			}
			default:
				break;
		}
	}

	/**
	 * Initializes and adds a server-sent object
	 * @param update the update containing information about the object
	 */
	public void addServerSentObject(ObjectSpawn update) {
		try {
			Class<?> clazz = update.objectClass;
			ClientObject obj = (ClientObject) clazz.newInstance();
			obj.setHandler(this, update.objectID);

			ServerSentStrategy ssoStrat = new ServerSentStrategy();
			obj.setUnloadStrategy(ssoStrat);
			//obj.addUpdateStrategy(new GameObjectUpdateStrategy());
			obj.getDefaultUpdateStrategy();
			obj.addUpdateStrategy(ssoStrat);

			if (clazz == ClientObject.class) {
				UpdateTextureDrawStrategy utdStrat = new UpdateTextureDrawStrategy();
				obj.setDrawStrategy(utdStrat);
				obj.addUpdateStrategy(utdStrat);
			}

			obj.activate();
			addObject(obj);
		} catch (InstantiationException | IllegalAccessException e) {
			Debug.logError("Could not spawn sent object");
			e.printStackTrace();
		}
	}

	/**
	 * Removes an object specified in an object removal update
	 * @param update the update containing information about which object is to
	 * be removed
	 */
	public void serverSentObjectRemoval(ObjectRemoved update) {
		removeObject(update.objectID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNewObjectID() {
		nextObjectID--;
		return nextObjectID;
	}

	public CopyOnWriteArrayList<PositionalSound> getSounds() {
		return sounds;
	}

	/**
	 * Adds a sound to this handler
	 * @param snd the sound to be added
	 */
	public void addSound(PositionalSound snd) {
		if (!sounds.contains(snd)) {
			sounds.add(snd);
		}
	}

	/**
	 * Removes a sound from this handler
	 * @param snd the sound to be removed
	 */
	public void removeSound(PositionalSound snd) {
		if (sounds.contains(snd)) {
			sounds.remove(snd);
		}
	}

	/**
	 * @param dP the new draw position
	 * 
	 * @see #drawPosition
	 */
	public void setDrawPosition(int dP) {
		drawPosition = dP;
	}

	/**
	 * @see #drawPosition
	 */
	public int getDrawPosition() {
		return drawPosition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void init();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void step(double dtime);

	
	/**
	 * Called when the game winow is resized. May be overridden, e.g. to resize
	 * the handler's panel
	 * @param newWidth the new window width
	 * @param newHeigth the new window height
	 */
	public void windowResized(int newWidth, int newHeigth) {

	}

	/**
	 * {@inheritDoc}
	 * 
	 * This implementation also updates the camera and calls all the
	 * {@link ClientObject}'s unload methods
	 */
	@Override
	public void gameStep(double dtime) {
		super.gameStep(dtime);
		camera.step(dtime);
		for (GameObject obj : getObjects()) {
			obj.step(dtime);
			if (obj.getHandler() == this) {
				((ClientObject) obj).unload(dtime);
			}
		}
	}

	/**
	 * Called before each call to {@link #draw()}. Calls
	 * {@link ClientObject#onDraw(double)} and
	 * {@link ClientObject#integratePosition(double)}
	 * @param dtime 
	 */
	public void onDraw(double dtime) {
		for (GameObject obj : getObjects()) {
			if (obj instanceof ClientObject) {
				ClientObject cobj = (ClientObject) obj;
				cobj.onDraw(dtime);
				if (cobj.getInterpolate() && cobj.getPhysicsStrategy() == null) {
					cobj.integratePosition(dtime);
				}
			}
		}
	}

	/**
	 * Draws the contents of the object handler i.e. all the
	 * {@link ClientObject}s that the handler handles.
	 * 
	 * First draws the {@link HandlerPanel} (a rectangle with the color of the
	 * panel), the collects all draw positions from all objects, removes
	 * multiple occurences of the same draw position, sorts them and then calls
	 * each object's {@link ClientObject#draw(int)} method for each draw
	 * position
	 */
	public void draw() {
		HandlerPanel pan = getPanel();
		//only draw if we have a panel
		if (pan != null) {
			int x = (int) pan.position.x();
			int y = (int) pan.position.y();
			int xSize = (int) pan.size.x();
			int ySize = (int) pan.size.y();
			Color color = pan.background;

			//draw the panel
			GraphicsBackend backend = ClientManager.getInstance().getGraphicsBackend();
			AbstractDrawer drawer = backend.getDrawer();
			AbstractGameWindow window = backend.getWindow();

			float[] c = color.getComponents(null);

			Vector halfWinSize = new Vector(window.getWidth(),
					window.getHeight()).divide(2);

			Vector topLeft = new Vector(panel.position).subtract(halfWinSize);
			Vector topRight = new Vector(x + xSize, y).subtract(halfWinSize);
			Vector bottomLeft = new Vector(x, y + ySize).subtract(halfWinSize);
			Vector bottomRight = new Vector(x + xSize, y + ySize).subtract(halfWinSize);
			drawer.drawPolygon(c[0], c[1], c[2], c[3], true, topLeft, bottomLeft, bottomRight, topRight);

			//collect all draw positions
			ArrayList<Integer> drawPosns = new ArrayList<>();
			for (int i = 0; i < getObjects().size(); i++) {
				ArrayList<Integer> posns = getObjects().get(i).getDrawPositions();
				for (int j = 0; j < posns.size(); j++) {
					if (!drawPosns.contains(posns.get(j))) {
						drawPosns.add(posns.get(j));
					}
				}
			}

			//sort the draw positions
			Integer[] dPArr = new Integer[drawPosns.size()];
			dPArr = drawPosns.toArray(dPArr);
			Arrays.sort(dPArr, new Comparator<Integer>() {
				@Override
				public int compare(Integer t, Integer t1) {
					if (t == null) {
						return -1;
					} else if (t1 == null) {
						return 1;
					}
					return t - t1;
				}
			});
			
			//draw everything
			for (int i = 0; i < dPArr.length; i++) {
				for (int j = 0; j < getObjects().size(); j++) {
					GameObject obj = getObjects().get(j);
					if (obj instanceof ClientObject) {
						ClientObject cobj = (ClientObject) obj;
						try {
							cobj.draw(dPArr[i]);
						} catch (NullPointerException e) {

						}
					}
				}
			}
		}

	}

}
