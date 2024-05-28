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
package blackhole.server.physicsEngine.core;

import blackhole.common.GameObject;
import blackhole.common.ObjectStrategy;
import blackhole.server.game.ServerObject;
import blackhole.server.game.ServerObjectHandler;
import blackhole.server.physicsEngine.boundingAreas.BoundingArea;
import blackhole.server.physicsEngine.boundingAreas.BoundingAreaContainer;
import blackhole.server.physicsEngine.collision.Contact;
import blackhole.server.physicsEngine.constraints.Constraint;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;
import java.util.ArrayList;

/**
 * A strategy that allows a {@link GameObject} to be handled by a
 * {@link PhysicsHandler}, i.e. makinag a {@link GameObject} follow the laws of
 * physics.
 * @author fabian
 */
public class PhysicsStrategy implements ObjectStrategy, BoundingAreaContainer {

	/**
	 * The mass assigned to immovable objects
	 */
	final static double staticObjectMass = 1e20;

	/**
	 * The {@link GameObject} that this strategy modifies
	 */
	private GameObject object;

	/**
	 * The mass of the object
	 */
	private double mass;
	
	/**
	 * The moment of inertia of the object
	 */
	private double momentOfInertia;
	
	/**
	 * The resulting force vector of the current game step
	 */
	private Vector resultantForce;
	
	/**
	 * The resulting torque of the current game step
	 */
	private double resultantTorque;
	
	/**
	 * The collisionboxes of the object
	 */
	private ArrayList<BoundingArea> collisionboxes;
	
	/**
	 * The {@link PhysicsHandler} that handles the {@code PhysicsStrategy}
	 */
	private PhysicsHandler physicsHandler;
	
	/**
	 * Whether the object should be immovable
	 */
	private boolean staticObject;
	
	/**
	 * The bounciness of the object
	 */
	private double bounciness;
	
	/**
	 * The friction coefficient of the object
	 */
	private double friction;
	
	/**
	 * temporary velocity matrix for constraint solving
	 */
	private Matrix tmpConstraintVel;

	/**
	 * The collision layers the object is on. Objects can only collide if they
	 * are on at least one common collision layer.
	 */
	private ArrayList<Integer> layers;

	/**
	 * Creates a new {@code PhysicsStrategy}
	 */
	public PhysicsStrategy() {
		resultantForce = new Vector();
		resultantTorque = 0;
		resultantForce = new Vector();
		staticObject = false;
		bounciness = 0;
		friction = 0;
		tmpConstraintVel = new Matrix(3, 1, 0);
		collisionboxes = new ArrayList<>();
		mass = 1;
		momentOfInertia = 1;

		layers = new ArrayList<>();
		layers.add(0);
	}

	/**
	 * Sets the {@link PhysicsHandler} that should handle the strategy
	 * @param handler the {@link PhysicsHandler} that should handle the strategy
	 */
	public void setPhysicsHandler(PhysicsHandler handler) {
		physicsHandler = handler;
	}

	/**
	 * Returns the {@link PhysicsHandler} that should handle the strategy
	 * @return the {@link PhysicsHandler} that should handle the strategy
	 */
	public PhysicsHandler getPhysicsHandler() {
		return physicsHandler;
	}

	/**
	 * Sets the moment of inertia of the object
	 * @param I the new moment of inertia
	 */
	public void setMomentOfInertia(double I) {
		momentOfInertia = I;
	}

	/**
	 * Returns the moment of inertia of the object
	 * @return the moment of inertia of the object
	 */
	public double getMomentOfInertia() {
		return momentOfInertia;
	}

	/**
	 * Sets the mass of the object
	 * @param m the new mass
	 */
	public void setMass(double m) {
		mass = m;
	}

	/**
	 * Returns the mass of the object
	 * @return the mass of the object
	 */
	public double getMass() {
		if (getStatic()) {
			return staticObjectMass;
		}
		return mass;
	}

	/**
	 * Returns the list of collisionboxes the object has
	 * @return the list of collisionboxes the object has
	 */
	public ArrayList<BoundingArea> getCollisionboxes() {
		return collisionboxes;
	}

	/**
	 * Adds a new collisionbox to the object
	 * @param collbox the collisionbox to add
	 */
	public void addCollisionbox(BoundingArea collbox) {
		if (!getCollisionboxes().contains(collbox)) {
			getCollisionboxes().add(collbox);
		}
	}

	/**
	 * Removes a new collisionbox from the object
	 * @param collbox the collisionbox to remove
	 */
	public void removeCollisionbox(BoundingArea collbox) {
		if (getCollisionboxes().contains(collbox)) {
			getCollisionboxes().remove(collbox);
		}
	}

	/**
	 * Sets whether the object should be immovable
	 * @param st whether the object should be immovable
	 */
	public void setStatic(boolean st) {
		staticObject = st;
	}

	/**
	 * Returns true if the object is immovable
	 * @return true if the object is immovable
	 */
	public boolean getStatic() {
		return staticObject;
	}

	/**
	 * Sets the bounciness of the object
	 * @param bounce the new bounciness
	 */
	public void setBounciness(double bounce) {
		bounciness = bounce;
	}

	/**
	 * Returns the bounciness of the object
	 * @return the bounciness of the object
	 */
	public double getBounciness() {
		return bounciness;
	}

	/**
	 * Sets the friction of the object
	 * @param fric the new friction
	 */
	public void setFriction(double fric) {
		friction = fric;
	}

	/**
	 * Returns the friction of the object
	 * @return the friction of the object
	 */
	public double getFriction() {
		return friction;
	}

	/**
	 * Sets the temporary velocity matrix
	 * @param vel the new temporary velocity matrix
	 * 
	 * @see #tmpConstraintVel
	 */
	public void setTmpConstraintVel(Matrix vel) {
		tmpConstraintVel = vel;
	}

	/**
	 * Returns the temporary velocity matrix
	 * @return the temporary velocity matrix
	 * 
	 * @see #tmpConstraintVel
	 */
	public Matrix getTmpConstraintVel() {
		return tmpConstraintVel;
	}

	/**
	 * Returns the resulting force vector of the current game step
	 * @return the resulting force vector of the current game step
	 */
	public Vector getResultantForce() {
		return resultantForce;
	}

	/**
	 * Sets the resulting force vector of the current game step
	 * @param f the new resulting force vector
	 */
	public void setResultantForce(Vector f) {
		resultantForce = f;
	}

	/**
	 * Returns the resulting torque of the current game step
	 * @return the resulting torque of the current game step
	 */
	public double getResultantTorque() {
		return resultantTorque;
	}

	/**
	 * Sets the resulting torque of the current game step
	 * @param t the new resulting torque
	 */
	public void setResultantTorque(double t) {
		resultantTorque = t;
	}

	/**
	 * Returns the object the strategy modifies
	 * @return the object the strategy modifies
	 */
	@Override
	public GameObject getObject() {
		return object;
	}
	
	/**
	 * Sets the object the strategy modifies
	 * @param obj the object the strategy modifies
	 */
	@Override
	public void setObject(GameObject obj) {
		object = obj;
	}

	/**
	 * Updates the resulting force and torque according to the given force
	 * @param force the force to apply
	 */
	public void applyForce(Force force) {
		getResultantForce().add(force.getForce());
		setResultantTorque(getResultantTorque() + Vector.cross(force.getPoint(), force.getForce()));
	}

	/**
	 * Adds the torque to the resulting torque.
	 * @param torque the torque to apply
	 */
	public void applyTorque(double torque) {
		setResultantTorque(getResultantTorque() + torque);
	}

	/**
	 * moves and rotates the collisionboxes according to the object's rotation
	 */
	public void updateCollisionboxes() {
		for (int i = 0; i < getCollisionboxes().size(); i++) {
			getCollisionboxes().get(i).setPosition(getObject().getPosition());
			getCollisionboxes().get(i).setRotation(getObject().getRotation());
		}
	}

	/**
	 * Returns the collision layers the object is on
	 * @return the collision layers the object is on
	 */
	public ArrayList<Integer> getLayers() {
		return layers;
	}

	/**
	 * Sets the collision layers the object is on
	 * @param l the new collision layers
	 */
	public void setLayers(ArrayList<Integer> l) {
		if (l != null) {
			layers = l;
		}
	}

	/**
	 * Adds the object to a collision layer
	 * @param l the collision layer
	 */
	public void addLayer(int l) {
		if (!layers.contains(l)) {
			layers.add(l);
		}
	}

	/**
	 * Removes the object to from collision layer
	 * @param l the collision layer
	 */
	public void removeLayer(int l) {
		if (layers.contains(l)) {
			layers.remove(layers.indexOf(l));
		}
	}

	/**
	 * Updates the velocities according to the resulting torque and force
	 * @param dtime the time since the last call
	 */
	public void integrateVelocities(double dtime) {
		if (!getStatic()) {
			Vector acceleration = Vector.divide(getResultantForce(), getMass());
			double angularAcceleration = getResultantTorque() / getMomentOfInertia();

			if (acceleration.magnitude() > 0.00001) {
				getObject().setVelocity(getObject().getVelocity().add(Vector.multiply(acceleration, dtime)));
			}
			if (Math.abs(angularAcceleration) > 0.00001) {
				getObject().setAngularVelocity(getObject().getAngularVelocity() + angularAcceleration * dtime);
			}
		} else {
			if (getObject().getVelocity().magnitude() > 0) {
				getObject().setVelocity(new Vector());
			}
			if (Math.abs(getObject().getAngularVelocity()) > 0) {
				getObject().setAngularVelocity(0);
			}
		}
		setResultantForce(new Vector());
		setResultantTorque(0);

		getTmpConstraintVel().set(new double[][]{
			{getObject().getVelocity().x()},
			{getObject().getVelocity().y()},
			{getObject().getAngularVelocity()}
		});
	}

	/**
	 * Updates the position and rotation according to the velocity and angular
	 * velocity
	 * @param dtime the time since the last call
	 */
	public void integratePosition(double dtime) {
		if (!getStatic()) {
			if (getObject().getVelocity().magnitude() > 0.00001) {
				getObject().setPosition(getObject().getPosition().add(Vector.multiply(getObject().getVelocity(), dtime)));
			}
			if (Math.abs(getObject().getAngularVelocity()) > 0.00001) {
				getObject().setRotation(getObject().getRotation() + getObject().getAngularVelocity() * dtime);
			}
		}
	}

	/**
	 * Called when the object collides with another object
	 * @param contact the contact information
	 */
	public void onCollision(Contact contact) {

	}

	/**
	 * Called when a constraint involving the object is created
	 * @param constraint the constraint
	 */
	public void constraintCreated(Constraint constraint) {

	}

	/**
	 * Called when a constraint involving the object is removed
	 * @param constraint the constraint
	 */
	public void constraintRemoved(Constraint constraint) {

	}

	/**
	 * Called when a constraint involving the object breaks (the constraint
	 * force would need to be bigger than its maximum)
	 * @param constraint the constraint
	 */
	public void constraintBroke(Constraint constraint) {

	}

	/**
	 * Adds the strategy to the set {@link PhysicsHandler}
	 * @param obj the game object the strategy should modify
	 */
	@Override
	public void activate(GameObject obj) {
		object = obj;
		if (getPhysicsHandler() != null) {
			getPhysicsHandler().addObject(this);
		}
	}

	/**
	 * Removes the strategy from the {@link PhysicsHandler}
	 */
	@Override
	public void remove() {
		object = null;
		if (getPhysicsHandler() != null) {
			getPhysicsHandler().removeObject(this);
		}
	}
}
