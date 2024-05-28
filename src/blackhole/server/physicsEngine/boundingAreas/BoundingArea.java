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
package blackhole.server.physicsEngine.boundingAreas;

import blackhole.common.GameObject;
import blackhole.server.game.ServerObject;
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Vector;
import java.io.Serializable;

/**
 * A class describing a convex 2D Shape with utility functions for collision
 * detection.
 * @author fabian
 */
public abstract class BoundingArea implements Serializable {

	/**
	 * The object that contains the {@code BoundingArea}
	 */
	private transient BoundingAreaContainer container;
	
	/**
	 * The position of the {@code BoundingArea}
	 */
	protected Vector position;
	
	/**
	 * The rotation of the {@code BoundingArea}
	 */
	protected double rotation;

	/**
	 * Creates a new {@code BoundingArea} with a reference to the given
	 * {@link BoundingAreaContainer}
	 * @param obj 
	 */
	public BoundingArea(BoundingAreaContainer obj) {
		container = obj;
		position = new Vector();
		rotation = 0;

	}

	/**
	 * Returns the object that contains the {@code BoundingArea}
	 * @return the object that contains the {@code BoundingArea}
	 */
	public BoundingAreaContainer getContainer() {
		return container;
	}

	/**
	 * Sets the object that contains the {@code BoundingArea}
	 * @param cont the new container
	 */
	public void setContainer(BoundingAreaContainer cont) {
		container = cont;
	}

	/**
	 * Returns the position of the {@code BoundingArea}
	 * @return the position of the {@code BoundingArea}
	 */
	public Vector getPosition() {
		return position;
	}

	/**
	 * Returns the rotation of the {@code BoundingArea}
	 * @return the rotation of the {@code BoundingArea}
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * Sets the rotation of the {@code BoundingArea}
	 * @param rot the new rotation
	 */
	public abstract void setRotation(double rot);

	/**
	 * Sets the position of the {@code BoundingArea}
	 * @param v the new position
	 */
	public abstract void setPosition(Vector v);

	/**
	 * Returns the ID of the point that is the furthest along the given axis
	 * @param dir the direction of the axis
	 * @return the ID of the point that is the furthest along the given axis
	 */
	public abstract int getFurthestPointIDInDirection(Vector dir);

	/**
	 * Returns the point that is the furthest along the given axis
	 * @param dir the direction of the axis
	 * @return the point that is the furthest along the given axis
	 */
	public abstract Vector getFurthestPointInDirection(Vector dir);

	/**
	 * Finds the furthest point in the given direction and calculates the dot
	 * product with both adjacent edges. Returns the smaller one of these dot
	 * products.
	 * @param collDir the direction
	 * @return see above
	 */
	public abstract double getBestEdgeDotProduct(Vector collDir);

	/**
	 * Returns a polygon that should be used to determine the exact collision
	 * point. This method should return a polygon that can be used when another
	 * {@code BoundingArea} collides with this one in a way such that a corner
	 * of this {@code BoundingArea} overlaps with an edge of the other one.
	 * @param collDir the collision normal
	 * @param other the other {@code BoundingArea}
	 * @return a polygon used for collision point calculation
	 */
	public abstract Polygon getImpactingPart(Vector collDir, BoundingArea other);

	/**
	 * Returns a polygon that should be used to determine the exact collision
	 * point. This method should return a polygon that can be used when another
	 * {@code BoundingArea} collides with this one in a way such that a corner
	 * of the other {@code BoundingArea} overlaps with an edge of this one.
	 * @param collDir the collision normal
	 * @param other the other {@code BoundingArea}
	 * @return a polygon used for collision point calculation
	 */
	public abstract Polygon getImpactedPart(Vector collDir, BoundingArea other);

	/**
	 * Returns a polygon that describes which axis have to be tested with the
	 * separating axis theorem test in order to be sure that the two
	 * {@code BoundingArea}s are (not) colliding
	 * @param other the other {@code BoundingArea}
	 * @return polygon that describes which axis have to be tested
	 */
	public abstract Polygon getCollisionEdges(BoundingArea other);
	
	/**
	 * Used by {@link Circle} to determine a simplified polygon that
	 * behaves just like a circle in the current situation
	 * @param other the other {@code BoundingArea}
	 * @return a polygon
	 */
	public abstract Polygon getFullPolygonRepresentation(BoundingArea other);

	/**
	 * Returns an axis aligned bounding box that completely encloses the
	 * {@code BoundingArea}
	 * @return an AABB around the {@code BoundingArea}
	 */
	public AABB getAABB() {
		double xMax = getFurthestPointInDirection(new Vector(1, 0)).x();
		double xMin = getFurthestPointInDirection(new Vector(-1, 0)).x();
		double yMax = getFurthestPointInDirection(new Vector(0, 1)).y();
		double yMin = getFurthestPointInDirection(new Vector(0, -1)).y();

		return new AABB(new Vector(xMax, yMax), new Vector(xMin, yMin), this);
	}
}
