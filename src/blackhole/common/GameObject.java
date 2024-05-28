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

import blackhole.client.game.ClientObject;
import blackhole.networkData.ObjectUpdate;
import blackhole.server.physicsEngine.core.PhysicsHandler;
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

/**
 * Class representing an object inside the game world
 *
 * @author fabian
 */
public abstract class GameObject {

	/**
	 * Whether the object is rendered
	 */
	private boolean isVisible;

	/**
	 * the position of the object If the object has a parent this is the
	 * relative position to the parent
	 */
	private Vector position;

	/**
	 * the rotation of the object If the object has a parent this is the
	 * relative rotation to the parent
	 */
	private double rotation;

	/**
	 * the velocity of the object If the object has a parent this is the
	 * relative velocity to the parent
	 */
	private Vector velocity;

	/**
	 * the angular velocity of the object If the object has a parent this is the
	 * relative angular velocity to the parent
	 */
	private double angularVelocity;

	/**
	 * the scale of the object If the object has a parent this is the relative
	 * scale to the parent
	 */
	private Vector scale;

	/**
	 * Whether to interpolate the position between render frames
	 */
	private boolean interpolate;

	/**
	 * The render engine draws the scene in layers. This list contains all the
	 * layers the object wishes to have its {@link ClientObject#draw(int)}
	 * method called
	 */
	private ArrayList<Integer> drawPositions;

	/**
	 * The parent object or {@code null} if the object has no parent
	 */
	private GameObject parent;

	/**
	 * The {@link ObjectHandler} that handles the object
	 */
	private ObjectHandler objectHandler;

	/**
	 * The ID associated with the object. This is set when setting an object
	 * handler as the IDs are distributed by the handlers.
	 */
	private int id;

	/**
	 * List of attributes that should be included in the next update
	 *
	 * @see #addToUpdate(java.lang.String)
	 */
	private ArrayList<String> nextUpdateData;
	/**
	 * This field is used to copy {@link #nextUpdateData} when an update is
	 * created in order to avoid {@link ConcurrentModificationException}s
	 */
	private ArrayList<String> updateData;

	/**
	 * List of {@link UpdateStrategy}s that handle the updating of the object
	 */
	private ArrayList<UpdateStrategy> updateStrategies;

	/**
	 * An optional physics Strategy that allows the object to be modified by the
	 * physics engine
	 */
	private PhysicsStrategy physics;

	/**
	 * Initializes a new {@code GameObject} to default values
	 */
	public GameObject() {
		position = new Vector();
		rotation = 0;
		velocity = new Vector();
		angularVelocity = 0;
		scale = new Vector(1, 1);
		isVisible = false;
		parent = null;
		objectHandler = null;
		drawPositions = new ArrayList<>();
		drawPositions.add(0);

		updateData = new ArrayList<>();
		nextUpdateData = new ArrayList<>();

		updateStrategies = new ArrayList<>();
	}

	public Vector getPosition() {
		return position;
	}

	/**
	 * Returns the actual position of the object inside the game world In
	 * contrast to {@link #getPosition()} this method takes into account that
	 * the object may have a parent which makes its position relative to the
	 * parent's
	 *
	 * @return the actual position of the object inside the game world
	 */
	public Vector getRealPosition() {
		if (getParent() != null) {
			return Vector.add(Vector.rotate(getPosition(), getParent().getRealRotation()), getParent().getRealPosition());
		}
		return getPosition();
	}

	public void setPosition(double x, double y) {
		position.set(x, y);
	}

	public void setPosition(Vector pos) {
		position = pos;
	}

	public double getRotation() {
		return rotation;
	}

	/**
	 * Returns the actual rotation of the object inside the game world In
	 * contrast to {@link #getRotation()} this method takes into account that
	 * the object may have a parent which makes its rotation relative to the
	 * parent's
	 *
	 * @return the actual rotation of the object inside the game world
	 */
	public double getRealRotation() {
		if (getParent() != null) {
			return getRotation() + getParent().getRealRotation();
		}
		return getRotation();
	}

	public void setRotation(double rot) {
		rotation = rot;
	}

	public Vector getScale() {
		return scale;
	}

	/**
	 * Returns the actual scale of the object inside the game world In contrast
	 * to {@link #getScale()} this method takes into account that the object may
	 * have a parent which makes its scale relative to the parent's
	 *
	 * @return the actual scale of the object inside the game world
	 */
	public Vector getRealScale() {
		if (getParent() != null) {
			return Vector.multiply(getScale(), getParent().getRealScale());
		}
		return getScale();

	}

	public void setScale(double x, double y) {
		scale.set(x, y);
	}

	public void setScale(Vector sc) {
		scale = sc;
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector vel) {
		velocity = vel;
	}

	public double getAngularVelocity() {
		return angularVelocity;
	}

	public void setAngularVelocity(double alpha) {
		angularVelocity = alpha;
	}

	/**
	 * Sets the draw position to be only one value instead of a list, i.e. it
	 * clears the list of draw positions and adds this value
	 *
	 * @param p the value the draw position should be set to
	 */
	public void setDrawPosition(int p) {
		drawPositions.clear();
		drawPositions.add(p);
	}

	public ArrayList<Integer> getDrawPositions() {
		return drawPositions;
	}

	public void setDrawPositions(ArrayList<Integer> pos) {
		drawPositions = pos;
	}

	/**
	 * Adds a draw postion
	 *
	 * @param p the draw position to be added
	 */
	public void addDrawPosition(int p) {
		if (!drawPositions.contains(p)) {
			drawPositions.add(p);
		}
	}

	/**
	 * Removes a draw postion
	 *
	 * @param p the draw position to be removed
	 */
	public void removeDrawPosition(int p) {
		if (drawPositions.contains(p)) {
			drawPositions.remove(drawPositions.indexOf(p));
		}
	}

	public void setVisible(boolean state) {
		isVisible = state;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public boolean getInterpolate() {
		return interpolate;
	}

	public void setInterpolate(boolean inter) {
		interpolate = inter;
	}

	public GameObject getParent() {
		return parent;
	}

	/**
	 * Sets the parent to be the object with the id {@code parentID} or none if
	 * {@code parentID == null}. {@code keepGlobalTransform} specifies whether
	 * the transform of the object shoul be adjusted in order to make it look
	 * the same after the parent is added
	 *
	 * @param obj the parent object to be set
	 * @param keepGlobalTransform whether the global transform of the child
	 * should stay the same
	 *
	 * @deprecated not properly implemented
	 */
	public void setParent(GameObject obj, boolean keepGlobalTransform) {
		if (keepGlobalTransform) {
			if (obj != null) {
				setPosition(Vector.subtract(getPosition(), obj.getPosition()));
				setScale(Vector.divide(getRealScale(), obj.getRealScale()));
				setRotation(getRotation() - obj.getRotation());
			} else if (getParent() != null) {
				GameObject currentParentObj = getParent();
				if (currentParentObj != null) {
					setPosition(Vector.add(getPosition(), currentParentObj.getPosition()));
					setScale(Vector.multiply(getRealScale(), currentParentObj.getRealScale()));
					setRotation(getRotation() + currentParentObj.getRotation());
				}
			}
		}
		parent = obj;
	}

	public void setParent(GameObject obj) {
		setParent(obj, false);
	}

	/**
	 * Sets the object handler and id of the object. The ID is provided by the
	 * handler
	 *
	 * @param handler the handler to be used
	 */
	public void setHandler(ObjectHandler handler) {
		objectHandler = handler;
		if (handler != null) {
			id = objectHandler.getNewObjectID();
		}
	}

	public ObjectHandler getHandler() {
		return objectHandler;
	}

	public int getID() {
		return id;
	}

	public void setID(int i) {
		id = i;
	}

	/**
	 * Use the velocity and angular velocity of the object to calcuate a new
	 * position and rotation after dtime seconds passed
	 *
	 * @param dtime the time difference
	 */
	public void integratePosition(double dtime) {
		if (getPhysicsStrategy() == null) {
			if (getVelocity().magnitude() > 0.00001) {
				setPosition(getPosition().add(Vector.multiply(getVelocity(), dtime)));
			}
			if (Math.abs(getAngularVelocity()) > 0.00001) {
				setRotation(getRotation() + getAngularVelocity() * dtime);
			}
		} else {
			getPhysicsStrategy().integratePosition(dtime);
		}
	}

	/**
	 * Specifies that the property specified by {@code str} should be included
	 * in the next update
	 *
	 * @param str the property to be included in the next update
	 */
	public void addToUpdate(String str) {
		if (nextUpdateData.indexOf(str) == -1) {
			nextUpdateData.add(str);
		}
	}

	/**
	 * Copies all properties which should be included in the next update until
	 * now. This means no new properties can be added to the next update. Calls
	 * to {@link #addToUpdate(java.lang.String)} will result in the specified
	 * property to be added to the update after the next one.
	 */
	public void lockUpdate() {
		updateData = new ArrayList<>(nextUpdateData);
		nextUpdateData.clear();
	}

	public ArrayList<String> getUpdateData() {
		return updateData;
	}

	public ArrayList<UpdateStrategy> getUpdateStrategies() {
		return updateStrategies;
	}

	/**
	 * Adds a new {@link UpdateStrategy} to the object
	 *
	 * @param strat the {@link UpdateStrategy} to be added
	 */
	public void addUpdateStrategy(UpdateStrategy strat) {
		if (!updateStrategies.contains(strat)) {
			strat.activate(this);
			updateStrategies.add(strat);
		}
	}

	/**
	 * Removes a new {@link UpdateStrategy} from the object
	 *
	 * @param strat the {@link UpdateStrategy} to be removed
	 */
	public void removeUpdateStrategy(UpdateStrategy strat) {
		updateStrategies.remove(strat);
		strat.remove();
	}

	/**
	 * Returns the first instance of a {@link GameObjectUpdateStrategy} that is
	 * found within the object's {@link UpdateStrategy}s or creates a new one
	 * and returns it if none is found
	 *
	 * @return a {@link GameObjectUpdateStrategy}
	 */
	public GameObjectUpdateStrategy getDefaultUpdateStrategy() {
		for (int i = 0; i < updateStrategies.size(); i++) {
			if (updateStrategies.get(i) instanceof GameObjectUpdateStrategy) {
				return (GameObjectUpdateStrategy) updateStrategies.get(i);
			}
		}
		GameObjectUpdateStrategy strat = new GameObjectUpdateStrategy();
		addUpdateStrategy(strat);
		return strat;
	}

	/**
	 * Returns an {@link ObjectUpdate} that contains all the properties
	 * specified by {@link #addToUpdate(java.lang.String)} up to the last call
	 * of {@link #lockUpdate()}
	 *
	 * @return a new {@link ObjectUpdate}
	 */
	public ObjectUpdate getUpdate() {
		ObjectUpdate update = new ObjectUpdate(getID());
		update.data = new HashMap<>();
		for (int i = 0; i < updateStrategies.size(); i++) {
			update = updateStrategies.get(i).getUpdate(update);
		}
		if (update.data.isEmpty()) {
			return null;
		}
		return update;
	}

	/**
	 * Returns an {@link ObjectUpdate} that contains all the properties that all
	 * of the object's {@link UpdateStrategy}s know about
	 *
	 * @return a new {@link ObjectUpdate}
	 */
	public ObjectUpdate getUpdateAll() {
		ObjectUpdate update = new ObjectUpdate(getID());
		update.data = new HashMap<>();
		for (int i = 0; i < updateStrategies.size(); i++) {
			update = updateStrategies.get(i).getUpdateAll(update);
		}
		if (update.data.isEmpty()) {
			return null;
		}
		return update;
	}

	/**
	 * Use an {@link ObjectUpdate} to update properties of this object, using
	 * its {@link UpdateStrategy}s
	 *
	 * @param update the update containing new values for the object's
	 * properties
	 */
	public void update(ObjectUpdate update) {
		for (int i = 0; i < updateStrategies.size(); i++) {
			updateStrategies.get(i).update(update);
		}
	}

	/**
	 * Sets the object's physics strategy and sets its {@link PhysicsHandler} to
	 * the object's handler's {@link PhysicsHandler}
	 *
	 * @param pS the physics strategy
	 */
	public void setPhysicsStrategy(PhysicsStrategy pS) {
		if (physics != null) {
			physics.remove();
		}
		physics = pS;
		if (physics != null && getHandler() != null) {
			physics.setPhysicsHandler(getHandler().getPhysicsHandler());
			//physics.activate(this);
		}
	}

	public PhysicsStrategy getPhysicsStrategy() {
		return physics;
	}

	/**
	 * Adds the object to its {@link ObjectHandler}s handling routine If its
	 * {@link PhysicsStrategy} is not {@code null}, its {@link PhysicsStrategy}
	 * is also added to the object's handler's {@link PhysicsHandler}'s physics
	 * calculation routine
	 */
	public void activate() {
		if (getHandler() != null) {
			init();
			getHandler().addObject(this);
			if (physics != null) {
				physics.activate(this);
			}
		}
	}

	/**
	 * Removes the object from its {@link ObjectHandler}s handling routine If
	 * its {@link PhysicsStrategy} is not {@code null}, its
	 * {@link PhysicsStrategy} is also removed from the object's handler's
	 * {@link PhysicsHandler}'s physics calculation routine
	 */
	public void remove() {
		if (getHandler() != null) {
			getHandler().removeObject(this);
			if (physics != null) {
				physics.remove();
			}
		}
	}

	/**
	 * Method called every game step Can be used to specify actions the object
	 * has to do
	 *
	 * @param dtime the time since the last call
	 */
	public abstract void step(double dtime);

	/**
	 * Method called when the object is activated It is made sure that this
	 * method is called before the first call to {@link #step(double)} Can be
	 * used to initialize this object as its handler and id are now set (unlike
	 * in the constructor)
	 */
	public abstract void init();
}
