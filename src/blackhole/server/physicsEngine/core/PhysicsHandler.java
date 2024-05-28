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

import blackhole.common.ObjectHandler;
import blackhole.server.game.ServerObjectHandler;
import blackhole.server.physicsEngine.boundingAreas.BoundingArea;
import blackhole.server.physicsEngine.collision.Collision;
import blackhole.server.physicsEngine.collision.CollisionDetector;
import blackhole.server.physicsEngine.constraints.Constraint;
import blackhole.server.physicsEngine.constraints.ConstraintSolver;
import blackhole.server.physicsEngine.collision.Contact;
import blackhole.utils.Debug;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The main handler for the physics engine. It manages {@link PhysicsStrategy}s,
 * collision detection and constraint solving.
 * @author fabian
 */
public class PhysicsHandler {

	/**
	 * A list containing all {@link PhysicsStrategy}s managed by the handler
	 */
	private CopyOnWriteArrayList<PhysicsStrategy> objects;
	
	/**
	 * The collision detector used by the handler
	 */
	private CollisionDetector collDetect;
	
	/**
	 * The constraint solver used by the handler
	 */
	private ConstraintSolver constSolver;

	/**
	 * Creates a new {@code PhysicsHandler}
	 */
	public PhysicsHandler() {
		objects = new CopyOnWriteArrayList<>();
		collDetect = new CollisionDetector();
		constSolver = new ConstraintSolver();
	}

	/**
	 * Called on each game step by {@link ObjectHandler#gameStep(double)}.
	 * Calculates the new velocities and positions of all the
	 * {@link PhysicsStrategy}s
	 * @param dtime the time since the last call
	 */
	public void step(double dtime) {
		//integrate velocities of objects
		for (PhysicsStrategy obj : objects) {
			obj.integrateVelocities(dtime);
		}

		/*
		The physics strategies are on layers. Only objects that have at least
		one common layer can collide. finishedLayers contains all layers where
		all collisions already have been determined
		*/
		ArrayList<Integer> finishedLayers = new ArrayList<>();
		for (int o = 0; o < objects.size(); o++) {
			ArrayList<Integer> objLayers = getObjects().get(o).getLayers();
			for (int l = 0; l < objLayers.size(); l++) {
				//the current layer
				int layer = objLayers.get(l);
				if (!finishedLayers.contains(layer)) {
					//layer not yet finished, calculate collisions
					finishedLayers.add(layer);

					//find all objects on the current layer
					ArrayList<BoundingArea> bAs = new ArrayList<>();
					for (int o2 = 0; o2 < getObjects().size(); o2++) {
						PhysicsStrategy obj = getObjects().get(o2);
						if (obj.getLayers().contains(layer)) {
							//update collisionboxes first
							obj.updateCollisionboxes();
							if (!obj.getCollisionboxes().isEmpty()) {
								//add all collisionboxes to a list
								for (int i = 0; i < obj.getCollisionboxes().size(); i++) {
									bAs.add(obj.getCollisionboxes().get(i));
								}
							}
						}
					}
					BoundingArea[] bAArray = new BoundingArea[bAs.size()];
					bAs.toArray(bAArray);
					
					//broad physe collision detection
					Collision[] maybes = collDetect.broadPhase(bAArray);

					//for all the possible collidiing object pairs
					for (Collision broad : maybes) {
						PhysicsStrategy a = (PhysicsStrategy) broad.getImpacted().getContainer();
						PhysicsStrategy b = (PhysicsStrategy) broad.getImpacting().getContainer();
						//if they are not both immovable
						if (!(a.getStatic() && b.getStatic())) {
							//narrow physe collision detection
							Collision coll = collDetect.collide(broad.getImpacted(), broad.getImpacting());
							
							//if the objects are actually colliding
							if (coll != null) {
								a = (PhysicsStrategy) coll.getImpacted().getContainer();
								b = (PhysicsStrategy) coll.getImpacting().getContainer();
								Vector normal = coll.getMTV().normalized();

								Vector aPoint = null;
								Vector bPoint = null;
								
								//create new contact objects for all contact points
								for (int i = coll.getImpactedManifold().length - 1; i > -1; i--) {
									if (i < coll.getImpactingManifold().length) {
										if (coll.getImpactedManifold()[i] != null && coll.getImpactingManifold()[i] != null) {
											aPoint = coll.getImpactedManifold()[i];
											bPoint = coll.getImpactingManifold()[i];
											Contact contact = new Contact(a, b, aPoint, bPoint, normal, dtime, constSolver);
											a.onCollision(contact);
											b.onCollision(contact);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		//solve all constraints
		constSolver.solve(dtime);
		for (PhysicsStrategy obj : objects) {
			obj.integratePosition(dtime);
		}
	}

	/**
	 * Adds a new {@link Constraint} to the constraint solver
	 * @param c the constraint to add
	 */
	public void addConstraint(Constraint c) {
		constSolver.addConstraint(c);
	}

	/**
	 * Removes a {@link Constraint} from the constraint solver
	 * @param c the constraint to remove
	 */
	public void removeConstraint(Constraint c) {
		constSolver.removeConstraint(c);
	}

	/**
	 * Adds a new {@link PhysicsStrategy} to the handler
	 * @param obj the physics strategy to add
	 */
	public void addObject(PhysicsStrategy obj) {
		if (!objects.contains(obj)) {
			objects.add(obj);
		}
	}

	/**
	 * Removes a {@link PhysicsStrategy} from the handler
	 * @param obj the physics strategy to remove
	 */
	public void removeObject(PhysicsStrategy obj) {
		obj.setPhysicsHandler(null);
		objects.remove(obj);
	}

	/**
	 * Returns a list of all currently managed {@link PhysicsStrategy}s
	 * @return a list of all currently managed {@link PhysicsStrategy}s
	 */
	public CopyOnWriteArrayList<PhysicsStrategy> getObjects() {
		return objects;
	}
}
