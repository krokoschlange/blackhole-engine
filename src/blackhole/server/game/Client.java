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

import blackhole.client.game.input.InputEvent;
import blackhole.client.game.input.InputEventListener;
import blackhole.client.game.input.KeyPosition;
import blackhole.common.GameObject;
import blackhole.networkData.ClientDataUpdate;
import blackhole.networkData.NetworkUpdate;
import blackhole.networkData.ObjectRemoved;
import blackhole.networkData.ObjectSpawn;
import blackhole.networkData.ObjectUpdate;
import blackhole.networkData.ServerDataUpdate;
import blackhole.networkData.UnloadUpdate;
import blackhole.server.network.ClientSocket;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * Representation of the state of the game on a client
 * @author fabian
 */
public class Client {

	/**
	 * All active controls
	 */
	private ArrayList<String> activeControls;
	
	/**
	 * The mouse wheel position
	 */
	private double mouseWheel;
	
	/**
	 * The mouse position, in window coordinates
	 */
	private Vector mousePosition;
	
	/**
	 * the mouse position in game coordinates
	 */
	private Vector mouseGamePosition;
	
	/**
	 * The game window size in pixels
	 */
	private Vector windowSize;
	
	/**
	 * The camera position in game coordinates
	 */
	private Vector cameraPosition;
	
	/**
	 * The camera size in game coordinates scaled by the game scale
	 */
	private Vector cameraSize;
	
	/**
	 * the camera rotation
	 */
	private double cameraRotation;

	/**
	 * A list of {@link InputEventListener}s that listen to input events of
	 * the client
	 */
	private CopyOnWriteArrayList<InputEventListener> inputListeners;

	/**
	 * the range around the camera in which objects are sent to the client.
	 * {@code objectSendingRange} is a factor that is multpilied with the camera
	 * diagonal to get the actual range (e.g. a value of 4 means that objects
	 * within a range of 4 times the camera diagonal around the camera position
	 * are sent to the client)
	 */
	private double objectSendingRange;
	
	/**
	 * the IDs of all objects that are loaded on the client
	 */
	private CopyOnWriteArrayList<Integer> loadedObjects;
	//private ArrayList<long[]> unloadingObjects;

	/**
	 * the {@link ClientSocket} used to communicate with the client
	 */
	private ClientSocket socket;

	/**
	 * Creates a new client with the specified {@link ClientSocket}
	 * @param sock the {@link ClientSocket} to be used for communication
	 */
	public Client(ClientSocket sock) {
		socket = sock;
		loadedObjects = new CopyOnWriteArrayList<>();
		//unloadingObjects = new ArrayList<>();

		inputListeners = new CopyOnWriteArrayList<>();

		activeControls = new ArrayList<>();
		mousePosition = new Vector();
		mouseGamePosition = new Vector();
		mouseWheel = 0;
		windowSize = new Vector();
		cameraPosition = new Vector();
		cameraSize = new Vector();
		cameraRotation = 0;

		ClientHandler.getInstance().addClient(this);
		if (Settings.getProperty("object_sending_distance") != null) {
			objectSendingRange = Integer.parseInt(Settings.getProperty("object_sending_range"));
		} else {
			objectSendingRange = 4;
		}
		GameManager.getInstance().getObjectHandler().clientConnect(this);

		ServerDataUpdate updt = new ServerDataUpdate();
		
		updt.gameScale = GameManager.getInstance().getGameScale();
		socket.sendUpdate(updt);
	}

	/**
	 * removes the {@code Client} from the game
	 */
	public void disconnect() {
		ClientHandler.getInstance().removeClient(this);
		GameManager.getInstance().getObjectHandler().clientDisconnect(this);
	}

	/**
	 * Sets the state of a control
	 * @param ctrl the name
	 * @param state the state
	 */
	public void setControl(String ctrl, boolean state) {
		if (state && activeControls.indexOf(ctrl) == -1) {
			activeControls.add(ctrl);
		} else if (activeControls.indexOf(ctrl) != -1) {
			activeControls.remove(ctrl);
		}
	}
	
	/**
	 * Adds an event listener to the client
	 * @param listener the listener to add
	 */
	public void addEventListener(InputEventListener listener) {
		inputListeners.add(listener);
	}
	
	/**
	 * Removes an event listener from the client
	 * @param listener the listener to remove
	 */
	public void removeEventListener(InputEventListener listener) {
		inputListeners.remove(listener);
	}

	/**
	 * Dispatches an {@link InputEvent} to all registered
	 * {@link InputEventListener}s
	 * @param event the event to dispatch
	 */
	public void dispatchEvent(InputEvent event) {
		Iterator<InputEventListener> it = inputListeners.iterator();
		while (it.hasNext()) {
			it.next().handleEvent(event);
		}
	}

	/**
	 * Returns true if the given control is active
	 * @param ctrl the control
	 * @return true if it is active
	 */
	public boolean isControlActive(String ctrl) {
		return activeControls.indexOf(ctrl) != -1;
	}

	/**
	 * Returns a list of all active controls
	 * @return a list of all active controls
	 */
	public ArrayList<String> getActiveControls() {
		return activeControls;
	}

	/**
	 * Returns the mouse wheel rotation
	 * @return the mouse wheel rotation
	 */
	public double getMouseWheelRotation() {
		return mouseWheel;
	}

	/**
	 * Returns the mouse position in window coordinates
	 * @return the mouse position in window coordinates
	 */
	public Vector getMousePosition() {
		return mousePosition;
	}

	/**
	 * Returns the mouse position in game coordinates
	 * @return the mouse position in game coordinates
	 */
	public Vector getMouseGamePosition() {
		return mouseGamePosition;
	}

	/**
	 * Returns the camera position
	 * @return the camera position
	 */
	public Vector getCameraPosition() {
		return cameraPosition;
	}

	/**
	 * Returns the camera size
	 * @return the camera size
	 */
	public Vector getCameraSize() {
		return cameraSize;
	}

	/**
	 * Returns the camera rotation
	 * @return the camera rotation
	 */
	public double getCameraRotation() {
		return cameraRotation;
	}

	/**
	 * Recalculates the mouse position in game coordinates
	 */
	public void calculateMouseGamePos() {
		double gameScale = GameManager.getInstance().getGameScale();
		Vector mousePos = Vector.subtract(mousePosition, Vector.divide(windowSize, 2)).multiply(new Vector(1, -1));
		Vector windowCamRatio = Vector.divide(cameraSize, windowSize);
		mousePos.multiply(windowCamRatio).divide(gameScale);
		mouseGamePosition = mousePos.rotate(-cameraRotation).add(cameraPosition);
	}

	
	/**
	 * Updates the {@code Client} object according to the given update
	 * @param update the update
	 */
	public void update(NetworkUpdate update) {
		switch (update.updateType) {
			case NetworkUpdate.CLIENT_DATA:
				ClientDataUpdate dataUpdate = (ClientDataUpdate) update;
				if (dataUpdate.unloadDistance != null) {
					objectSendingRange = dataUpdate.unloadDistance;
				}
				if (dataUpdate.windowSize != null) {
					windowSize = dataUpdate.windowSize;

					calculateMouseGamePos();
				}
				if (dataUpdate.camPos != null) {
					cameraPosition = dataUpdate.camPos;

					calculateMouseGamePos();
				}
				if (dataUpdate.camRot != null) {
					cameraRotation = dataUpdate.camRot;

					calculateMouseGamePos();
				}
				if (dataUpdate.camSize != null) {
					cameraSize = dataUpdate.camSize;

					calculateMouseGamePos();
				}
				if (dataUpdate.mousePos != null) {
					Vector deltaPos = Vector.subtract(dataUpdate.mousePos, mousePosition);
					mousePosition = dataUpdate.mousePos;

					calculateMouseGamePos();

					InputEvent event = new InputEvent(mousePosition, deltaPos);
					dispatchEvent(event);
				}
				if (dataUpdate.mouseScroll != null) {
					double delta = dataUpdate.mouseScroll - mouseWheel;
					mouseWheel = dataUpdate.mouseScroll;

					InputEvent event = new InputEvent(mouseWheel, delta, mousePosition);
					dispatchEvent(event);
				}
				if (dataUpdate.inputs != null) {
					BiConsumer<String, Boolean> iterator = (String name, Boolean state) -> {
						setControl(name, state);
						InputEvent event = new InputEvent(new String[]{name}, 0, KeyPosition.STANDARD, state);
						dispatchEvent(event);
					};
					dataUpdate.inputs.forEach(iterator);
				}
				break;
			case NetworkUpdate.OBJECT_UPDATE:
				ObjectUpdate objectUpdate = (ObjectUpdate) update;
				GameObject obj = GameManager.getInstance().getObjectHandler().getObjectByID(objectUpdate.objectID);
				obj.update(objectUpdate);
				break;
			case NetworkUpdate.UNLOAD_UPDATE:
				UnloadUpdate unloadUpdate = (UnloadUpdate) update;
				if (loadedObjects.contains(unloadUpdate.objectID)) {
					loadedObjects.remove(loadedObjects.indexOf(unloadUpdate.objectID));
				}
				break;
			default:
				break;
		}
	}

	/**
	 * Calculates what updates need to be sent to the client and sends them
	 */
	public void updateClient() {
		// if we have an object handler
		if (GameManager.getInstance().getObjectHandler() != null) {
			//iterate over all objects on server
			CopyOnWriteArrayList<GameObject> objects = GameManager.getInstance().getObjectHandler().getObjects();
			for (GameObject obj : objects) {
				//check only if som other object happened to end up on the server
				//(should be avoided)
				if (obj instanceof ServerObject) {
					ServerObject sobj = (ServerObject) obj;
					Vector objPos = sobj.getRealPosition();
					Vector dist = Vector.subtract(cameraPosition, objPos);
					double distance = dist.magnitude();
					double cameraRadius = cameraSize.magnitude() / GameManager.getInstance().getGameScale();
					double objectRadius = sobj.getRealScale().magnitude();

					//load the object if
					// 1.: it is not server-only
					boolean loadObject = !sobj.getServerOnly()
							// and
							// 2.: it is not including/excluding clients from updates
							&& (!(sobj.getExcludeClients() || sobj.getIncludeClients())
							// or
							// the object is excluding clients from updates but
							// not this one
							|| (sobj.getExcludeClients() && !sobj.getClientExcluded(this))
							// the object wants this client to receive updates
							|| (sobj.getIncludeClients() && sobj.getClientIncluded(this)));

					// check if we are in the sending range (or sending it no matter the range)
					// and if we are actually loading it (see previous variable)
					if ((objectRadius + cameraRadius * objectSendingRange > distance || sobj.getAlwaysLoaded()) && loadObject) {
						// if we haven't already loaded it
						if (!loadedObjects.contains(obj.getID())) {
							//make sure we remember that we already loaded this object
							loadedObjects.add(obj.getID());

							// generate a spawn update
							ObjectSpawn update = new ObjectSpawn(obj.getID(), null);
							update.objectClass = sobj.getClientObjectClass(this);
							socket.sendUpdate(update);

							// and an initialization update
							ObjectUpdate objUpdate = sobj.getUpdateAll();
							if (objUpdate != null && objUpdate.data != null) {
								socket.sendUpdate(objUpdate);
							}
						}
					}

				}
			}
			
			// loop through all the objects we have loaded
			for (int id : loadedObjects) {
				// check if they still exist
				GameObject obj = GameManager.getInstance().getObjectHandler().getObjectByID(id);
				if (obj == null) {
					// if not, send a removal update
					ObjectRemoved update = new ObjectRemoved(id);
					socket.sendUpdate(update);
				} else {
					// otherwise update them
					ObjectUpdate update = obj.getUpdate();
					if (update != null && update.data != null) {
						socket.sendUpdate(update);
					}
				}
			}
		} // if we don't have an object handler
		else {
			// send removal updates for all objects we still have loaded.
			// This is to make sure that all objects get removed on the client
			// if the server switches its object handler / removes it
			for (int i = 0; i < loadedObjects.size(); i++) {
				ObjectRemoved update = new ObjectRemoved(loadedObjects.get(i));
				socket.sendUpdate(update);
			}
		}
	}
}
