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
package blackhole.common;

import blackhole.networkData.NetworkUpdate;
import blackhole.server.physicsEngine.core.PhysicsHandler;
import blackhole.utils.Debug;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A container for handling {@link GameObject}s
 * @author fabian
 */
public abstract class ObjectHandler {

	/**
	 * List of active {@link GameObject}s
	 */
	private CopyOnWriteArrayList<GameObject> objects;
	
	/**
	 * The {@link PhysicsHandler} that does the physics calculations for the
	 * objects in this handler
	 */
	private PhysicsHandler physics;

	/**
	 * Constructs a new, empty ObjectHandler
	 */
	public ObjectHandler() {
		objects = new CopyOnWriteArrayList<>();
	}
	
	public void setPhysicsHandler(PhysicsHandler handler) {
        physics = handler;
    }

    public PhysicsHandler getPhysicsHandler() {
        return physics;
    }

	/**
	 * Adds a {@link GameObject} to the active object list
	 * @param obj the object to be added
	 */
	public void addObject(GameObject obj) {
		if (!objects.contains(obj)) {
			objects.add(obj);
		}
	}

	/**
	 * Removes a {@link GameObject} from the active object list
	 * @param obj the object to be removed
	 */
	public void removeObject(GameObject obj) {
		if (objects.contains(obj)) {
			obj.setHandler(null);
			objects.remove(obj);
		}
	}

	/**
	 * Returns the object that has the specified ID
	 * @param id the ID of the object that should be returned
	 * @return the object that has the specified ID
	 */
	public GameObject getObjectByID(Integer id) {
		if (id == null) { // don't need to search for that!
			return null;
		}
		for (GameObject obj : objects) {
			if (obj.getID() == id) {
				return obj;
			}
		}
		return null;
	}
	
	/**
	 * Removes a {@link GameObject} from the active object list
	 * @param id the ID of the object to be removed
	 */
	public void removeObject(int id) {
		if (objects.indexOf(getObjectByID(id)) != -1) {
			objects.remove(getObjectByID(id));
		}
	}

	/**
	 * Returns a list of all {@link GameObject}s in this handler
	 * @return 
	 */
	public CopyOnWriteArrayList<GameObject> getObjects() {
		return objects;
	}

	/**
	 * Returns an array of all {@link GameObject}s in the specified radius around the given point
	 * @param pos the center of the circle to be searched for objects
	 * @param radius the radius of the circle to be searched for objects
	 * @return an array of {@link GameObject}s
	 */
	public GameObject[] getObjectsInRadius(Vector pos, double radius) {
		ArrayList<GameObject> objs = new ArrayList<>();
		for (int i = 0; i < getObjects().size(); i++) {
			if (Vector.subtract(pos, getObjects().get(i).getRealPosition()).magnitude() < radius) {
				objs.add(getObjects().get(i));
			}
		}
		GameObject[] objArr = new GameObject[objs.size()];
		return objs.toArray(objArr);
	}
	
	/**
	 * Removes all {@link GameObject}s from this handler
	 */
	public void clear() {
		Iterator<GameObject> it = getObjects().iterator();
		while (it.hasNext()) {
			it.next().remove();
		}
		Debug.log("CLEAR");
		getObjects().clear();
	}
	
	/**
	 * Handles a {@link NetworkUpdate}
	 * @param update the update to be handled
	 */
	public abstract void handleUpdate(NetworkUpdate update);

	/**
	 * Returns a new, unused ID for an object
	 * (different object handlers may return the same ID at the same time)
	 * @return a new, unused ID for an object
	 */
	public abstract int getNewObjectID();

	/**
	 * Initializes this handler. This method is called before the handler has to
	 * process the first game step.
	 */
	public abstract void init();
	
	/**
	 * Called when the handler is no longer used. Called after the handler has
	 * to process its last game step.
	 */
	public abstract void cleanUp();

	/**
	 * Called on every game step
	 * @param dtime time since the last call
	 */
	public abstract void step(double dtime);

	/**
	 * Called on every game step. Used by the engine. Use {@link #step(double)}
	 * for overriding what to do on each game step instead.
	 * @param dtime time since the last call
	 */
	public void gameStep(double dtime) {
		step(dtime);
		if (getPhysicsHandler() != null) {
            getPhysicsHandler().step(dtime);
        }
	}
}
