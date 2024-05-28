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
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Vector;

/**
 * A {@link BoundingArea} describing any convex polygon
 * @author fabian
 */
public class Polygon extends BoundingArea {

	/**
	 * An array of corner points. These are shifted and rotated together with
	 * the {@code Polygon}.
	 */
	private Vector[] points;
	
	/**
	 * An array of edges such that
	 * {@code edges[i]} is the vector from {@code points[i]} to
	 * {@code points[i + 1]}.
	 * These are rotated together with the {@code Polygon}.
	 */
	private Vector[] edges;

	/**
	 * an array of corner points that is used as the basis for translations and
	 * rotations.
	 */
	private Vector[] origPoints;

	/**
	 * Creates a new, closed {@code Polygon} with the given points and container
	 * @param p the points
	 * @param pS the container
	 */
	public Polygon(Vector[] p, BoundingAreaContainer pS) {
		this(p, true, pS);
	}

	/**
	 * Creates a new {@code Polygon} with the given points and container
	 * @param p the points
	 * @param closed whether the polygon should be closed
	 * @param pS the container
	 */
	public Polygon(Vector[] p, boolean closed, BoundingAreaContainer pS) {
		super(pS);
		origPoints = p;
		points = new Vector[origPoints.length];
		if (closed) {
			edges = new Vector[origPoints.length];
		} else {
			edges = new Vector[Math.max(origPoints.length - 1, 0)];
		}
		updatePointsAndEdges();
	}

	/**
	 * Returns the array of points
	 * @return the array of points
	 */
	public Vector[] getPoints() {
		return points;
	}

	/**
	 * Returns the array of edges
	 * @return the array of edges
	 */
	public Vector[] getEdges() {
		return edges;
	}

	/**
	 * Returns the point with the given ID
	 * @param id the ID
	 * @return the point with the given ID
	 */
	public Vector getPoint(int id) {
		return getPoints()[id];
	}

	/**
	 * Returns the edge with the given ID
	 * @param id the ID
	 * @return the edge with the given ID
	 */
	public Vector getEdge(int id) {
		return getEdges()[id];
	}

	/**
	 * Returns the next point after the point with the given ID
	 * @param i the ID
	 * @return the next point after the point with the given ID
	 */
	public Vector nextPoint(int i) {
		i = i == getPoints().length - 1 ? 0 : i + 1;
		return getPoint(i);
	}

	/**
	 * Returns the ID of the next point after the point with the given ID.
	 * Usually returns {@code i + 1} but loops around to zero at the end of the
	 * point array.
	 * @param i the ID
	 * @return the ID of the next point after the point with the given ID
	 */
	public int nextPointID(int i) {
		i = i == getPoints().length - 1 ? 0 : i + 1;
		return i;
	}

	/**
	 * Returns the previous point before the point with the given ID
	 * @param i the ID
	 * @return the previous point before the point with the given ID
	 */
	public Vector previousPoint(int i) {
		i = i == 0 ? getPoints().length - 1 : i - 1;
		return getPoint(i);
	}

	/**
	 * Returns the ID of the previous point before the point with the given ID.
	 * Usually returns {@code i - 1} but loops around to the end of the point
	 * array for {@code i == 0}
	 * @param i the ID
	 * @return the ID of the previous point before the point with the given ID
	 */
	public int previousPointID(int i) {
		i = i == 0 ? getPoints().length - 1 : i - 1;
		return i;
	}

	/**
	 * Returns the next edge after the edge with the given ID
	 * @param i the ID
	 * @return the next edge after the edge with the given ID
	 */
	public Vector nextEdge(int i) {
		i = i == getEdges().length - 1 ? 0 : i + 1;
		return getEdge(i);
	}

	/**
	 * Returns the ID of the next edge after the edge with the given ID.
	 * Usually returns {@code i + 1} but loops around to zero at the end of the
	 * edge array.
	 * @param i the ID
	 * @return the ID of the next edge after the edge with the given ID
	 */
	public int nextEdgeID(int i) {
		i = i == getEdges().length - 1 ? 0 : i + 1;
		return i;
	}

	/**
	 * Returns the previous edge before the edge with the given ID
	 * @param i the ID
	 * @return the previous edge before the edge with the given ID
	 */
	public Vector previousEdge(int i) {
		i = i == 0 ? getEdges().length - 1 : i - 1;
		return getEdge(i);
	}

	/**
	 * Returns the ID of the previous edge before the edge with the given ID.
	 * Usually returns {@code i - 1} but loops around to the end of the edge
	 * array for {@code i == 0}
	 * @param i the ID
	 * @return the ID of the previous edge before the edge with the given ID
	 */
	public int previousEdgeID(int i) {
		i = i == 0 ? getEdges().length - 1 : i - 1;
		return i;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRotation(double rot) {
		rotation = rot;
		updatePointsAndEdges();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Also updates all points and edges in the polygon.
	 */
	@Override
	public int getFurthestPointIDInDirection(Vector dir) {

		double bestDot = 0;
		int bestID = -1;
		for (int i = 0; i < getPoints().length; i++) {
			double dot = Vector.dot(getPoint(i), dir.normalized());
			//Debug.log(dot);
			if (dot >= bestDot || bestID == -1) {
				bestDot = dot;
				bestID = i;
			}
		}
		return bestID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getFurthestPointInDirection(Vector dir) {
		int id = getFurthestPointIDInDirection(dir);
		if (id == -1) {
			return null;
		}
		return getPoint(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getImpactingPart(Vector collDir, BoundingArea other) {
		int furthestPoint = getFurthestPointIDInDirection(collDir);
		return new Polygon(new Vector[]{previousPoint(furthestPoint),
			getPoint(furthestPoint), nextPoint(furthestPoint)}, false,
				getContainer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getImpactedPart(Vector collDir, BoundingArea other) {
		int furthestPoint = getFurthestPointIDInDirection(collDir);

		double firstDot = Vector.dot(previousEdge(furthestPoint).normalized(), collDir.normalized());
		double secondDot = Vector.dot(getEdge(furthestPoint).normalized(), collDir.normalized());
		int bestEdge = Math.abs(firstDot) < Math.abs(secondDot) ? previousEdgeID(furthestPoint) : furthestPoint;

		Vector[] p = {previousPoint(bestEdge), getPoint(bestEdge), nextPoint(bestEdge), nextPoint(nextPointID(bestEdge))};

		return new Polygon(p, false, getContainer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBestEdgeDotProduct(Vector collDir) {
		int bestPoint = getFurthestPointIDInDirection(collDir);

		double a = Vector.dot(previousEdge(bestPoint).normalized(), collDir.normalized());
		double b = Vector.dot(Vector.multiply(getEdge(bestPoint).normalized(), -1), collDir.normalized());
		return a < b ? a : b;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getCollisionEdges(BoundingArea other) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getFullPolygonRepresentation(BoundingArea other) {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPosition(Vector v) {
		position = v;
		updatePointsAndEdges();
	}

	/**
	 * Updates all points and edges according to the current rotation and
	 * position of the {@code Polygon}
	 */
	public void updatePointsAndEdges() {
		for (int i = 0; i < origPoints.length; i++) {
			points[i] = Vector.rotate(origPoints[i], rotation).add(position);
		}
		for (int i = 0; i < points.length; i++) {
			if (i != points.length - 1) {
				edges[i] = Vector.subtract(points[i + 1], points[i]);
			} else {
				if (points.length == edges.length) {
					edges[i] = Vector.subtract(points[0], points[i]);
				}
			}
		}
	}
}
