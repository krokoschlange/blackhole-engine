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
package blackhole.server.physicsEngine.collision;

import blackhole.server.game.ServerObjectHandler;
import blackhole.server.physicsEngine.boundingAreas.AABB;
import blackhole.server.physicsEngine.boundingAreas.BoundingArea;
import blackhole.server.physicsEngine.boundingAreas.Polygon;
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Debug;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Calculates both broad and narrow phase collisions between
 * {@link BoundingArea}s
 *
 * @author fabian
 */
public class CollisionDetector {

	/**
	 * Class to mark the beginning or end of an AABB when projected onto an axis
	 * of the coordinate system.
	 */
	private static class AABBMarker {

		/**
		 * where the marker is
		 */
		private double marker;

		/**
		 * if it is the minimum or maximum value of the AABB (e.g. minimum is
		 * the left value when projected on the x-axis, as it is smaller that
		 * the right end of the AABB)
		 */
		private boolean minimum;

		/**
		 * the AABB that this marker marks
		 */
		private AABB aabb;

		/**
		 * Creates a new {@code AABBMarker}
		 */
		public AABBMarker(double m, boolean min, AABB a) {
			marker = m;
			aabb = a;
			minimum = min;
		}

		/**
		 * Returns the value
		 *
		 * @return the value
		 */
		public double getMarker() {
			return marker;
		}

		/**
		 * Returns the AABB
		 *
		 * @return the AABB
		 */
		public AABB getAABB() {
			return aabb;
		}

		/**
		 * Returns true if the marker is the beginning of the AABB's projection
		 * on a coordinate axis
		 * @return true if the marker is the beginning of the AABB's projection
		 * on a coordinate axis
		 */
		public boolean getMinimum() {
			return minimum;
		}
	}

	/**
	 * Creates a new {@code CollisionDetector}
	 */
	public CollisionDetector() {
	}

	/**
	 * Does a broad phase collision detection on a large number of objects.
	 * Returns a list of object pairs that might collide (has false positives
	 * but guarantees no false negatives)
	 *
	 * @param objects the list of {@link BoundingArea}s to check for collisions
	 * @return an array of {@link Collision}s that describe pairs of objects
	 * that might collide
	 */
	public Collision[] broadPhase(BoundingArea[] objects) {
		//get axis aligned bounding boxes for all bounding areas
		AABB[] aabbs = new AABB[objects.length];
		for (int i = 0; i < objects.length; i++) {
			aabbs[i] = objects[i].getAABB();
		}

		//arrays for markers
		AABBMarker[] x = new AABBMarker[aabbs.length * 2];
		AABBMarker[] y = new AABBMarker[aabbs.length * 2];

		//add all markers from the AABBs
		for (int i = 0; i < aabbs.length; i++) {
			AABB aabb = aabbs[i];
			Vector max = aabb.getMaximum();
			Vector min = aabb.getMinimum();
			x[i * 2] = new AABBMarker(max.x(), false, aabb);
			x[i * 2 + 1] = new AABBMarker(min.x(), true, aabb);
			y[i * 2] = new AABBMarker(max.y(), false, aabb);
			y[i * 2 + 1] = new AABBMarker(min.y(), true, aabb);
		}

		//sort the markers
		Arrays.sort(x, new Comparator<AABBMarker>() {
			@Override
			public int compare(AABBMarker t, AABBMarker t1) {
				if (t.getMarker() > t1.getMarker()) {
					return 1;
				} else if (t.getMarker() < t1.getMarker()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		Arrays.sort(y, new Comparator<AABBMarker>() {
			@Override
			public int compare(AABBMarker t, AABBMarker t1) {
				if (t.getMarker() > t1.getMarker()) {
					return 1;
				} else if (t.getMarker() < t1.getMarker()) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		ArrayList<Collision> xCollisions = new ArrayList<>();
		ArrayList<Collision> yCollisions = new ArrayList<>();

		//when two AABBs overlap on both coordinate axis, then we consider them
		//as colliding (only in the broad phase), if the don't overlap on at
		//least one axis, the objects cannot collide
		//
		//first we check for overlaps along the x-axis
		for (int i = 0; i < x.length - 1; i++) {
			AABBMarker currentMarker = x[i];
			if (currentMarker.getMinimum()) {
				int j = i + 1;
				AABBMarker otherMarker = x[j];
				while (otherMarker.getAABB() != currentMarker.getAABB()) {
					if (currentMarker.getAABB().getBoundingArea().getContainer()
							!= otherMarker.getAABB().getBoundingArea().getContainer()
							|| (currentMarker.getAABB().getBoundingArea().getContainer() == null
							&& otherMarker.getAABB().getBoundingArea().getContainer() == null)) {
						boolean alreadyExists = false;
						Collision collision = new Collision(currentMarker.getAABB().getBoundingArea(), otherMarker.getAABB().getBoundingArea(), null, null, null);
						for (Collision coll : xCollisions) {
							if (collision.isEqual(coll)) {
								alreadyExists = true;
								break;
							}
						}
						if (!alreadyExists) {
							xCollisions.add(collision);
						}
					}

					j++;
					otherMarker = x[j];
				}
			}
		}

		//then we check for overlaps along the y-axis
		for (int i = 0; i < y.length - 1; i++) {
			AABBMarker currentMarker = y[i];
			if (currentMarker.getMinimum()) {
				int j = i + 1;
				AABBMarker otherMarker = y[j];
				while (otherMarker.getAABB() != currentMarker.getAABB()) {
					boolean alreadyExists = false;
					Collision collision = new Collision(currentMarker.getAABB().getBoundingArea(), otherMarker.getAABB().getBoundingArea(), null, null, null);
					for (Collision coll : yCollisions) {

						if (collision.isEqual(coll)) {
							alreadyExists = true;
							break;
						}
					}
					if (!alreadyExists) {
						yCollisions.add(collision);
					}

					j++;
					otherMarker = y[j];
				}
			}
		}

		//and then we check for pairs of objects that overlap on both axis
		ArrayList<Collision> collisions = new ArrayList<>();
		for (Collision xColl : xCollisions) {
			for (Collision yColl : yCollisions) {
				if (xColl.isEqual(yColl)) {
					collisions.add(yColl);
				}
			}
		}

		Collision[] c = new Collision[collisions.size()];
		return collisions.toArray(c);
	}

	/**
	 * Checks if two {@link BoundingArea}s collide, and if they do, calculates
	 * the collision points and the collision normal.
	 * This uses the seperating axis theorem which states that two shapes don't
	 * overlap if there is an axis that separates them. For convex shapes we
	 * only need to test the edges of both shapes as a separating axis.
	 * 
	 * For a detailed description, see
	 * <a href="http://www.dyn4j.org/2010/01/sat/">
	 * http://www.dyn4j.org/2010/01/sat/</a>
	 * and
	 * <a href="http://www.dyn4j.org/2011/11/contact-points-using-clipping/">
	 * http://www.dyn4j.org/2011/11/contact-points-using-clipping/</a>
	 * @param a the first {@link BoundingArea}
	 * @param b the second {@link BoundingArea}
	 * @return a {@link Collision} object describing the collision between the
	 * given {@link BoundingArea}s or {@code null} if there is no collision
	 */
	public Collision collide(BoundingArea a, BoundingArea b) {
		Polygon collMeshA = a.getCollisionEdges(b);
		Polygon collMeshB = b.getCollisionEdges(a);

		//calculate a minimum translation vector from a's edges
		Vector mtvA = null;
		for (int i = 0; i < collMeshA.getEdges().length; i++) {
			boolean noCollision = true;
			double maximumPenetration = 0;
			for (Vector point : collMeshB.getPoints()) {
				Vector diff = Vector.subtract(point, collMeshA.getPoint(i));
				double inFront = Vector.dot(diff, collMeshA.getEdge(i).getNormal());

				if (inFront <= 0) {
					noCollision = false;
					if (inFront < maximumPenetration) {
						maximumPenetration = inFront;
					}
				}
			}
			if (noCollision) {
				return null;
			} else if (mtvA == null || Math.abs(maximumPenetration) < mtvA.magnitude()) {
				mtvA = Vector.multiply(collMeshA.getEdge(i).getNormal(), Math.abs(maximumPenetration));
			}
		}

		//calculate a minimum translation vector from b's edges
		Vector mtvB = null;
		for (int i = 0; i < collMeshB.getEdges().length; i++) {
			boolean noCollision = true;
			double maximumPenetration = 0;
			for (Vector point : collMeshA.getPoints()) {
				Vector diff = Vector.subtract(point, collMeshB.getPoint(i));
				double inFront = Vector.dot(diff, collMeshB.getEdge(i).getNormal());

				if (inFront <= 0) {
					noCollision = false;
					if (inFront < maximumPenetration) {
						maximumPenetration = inFront;
					}
				}
			}
			if (noCollision) {
				return null;
			} else if (mtvB == null || Math.abs(maximumPenetration) < mtvB.magnitude()) {
				mtvB = Vector.multiply(collMeshB.getEdge(i).getNormal(), Math.abs(maximumPenetration));
			}
		}

		//use the smaller minimum translation vector
		Vector mtv;
		if (mtvA.magnitude() < mtvB.magnitude()) {
			mtv = mtvA;
		} else {
			mtv = Vector.multiply(mtvB, -1);
		}

		double bestEdgeA = a.getBestEdgeDotProduct(mtv);
		double bestEdgeB = b.getBestEdgeDotProduct(Vector.multiply(mtv, -1));

		Polygon impacted;
		Polygon impacting;

		boolean aImpacted = false;

		if (bestEdgeA < bestEdgeB) {
			aImpacted = true;
			impacted = a.getImpactedPart(mtv, b);
			impacting = b.getImpactingPart(Vector.multiply(mtv, -1), a);
		} else {
			impacting = a.getImpactingPart(mtv, b);
			impacted = b.getImpactedPart(Vector.multiply(mtv, -1), a);
		}

		//clip the impacting shape
		impacting = clip(impacting, impacted.getPoint(0), impacted.getEdge(0));

		impacting = clip(impacting, impacted.getPoint(2), impacted.getEdge(2));

		//clip it again; what is left are the collision points
		ArrayList<Vector> finalImpactingPoints = new ArrayList<>();
		for (int i = 0; i < impacting.getPoints().length; i++) {
			Vector diff = Vector.subtract(impacting.getPoint(i), impacted.getPoint(1));
			if (Vector.dot(impacted.getEdge(1).getNormal(), diff) < 0) {
				finalImpactingPoints.add(impacting.getPoint(i));
			}
		}

		double depth = 0;
		for (int i = 0; i < finalImpactingPoints.size(); i++) {
			Vector diff = Vector.subtract(finalImpactingPoints.get(i), impacted.getPoint(1));
			double newDepth = Vector.dot(impacted.getEdge(1).getNormal(), diff);
			if (depth > newDepth) {
				depth = newDepth;
			}
		}

		//also clip the impacted shape; what is left are the collision points
		ArrayList<Vector> finalImpactedPoints = new ArrayList<>();
		for (int i = 0; i < finalImpactingPoints.size(); i++) {
			Vector diff = Vector.subtract(finalImpactingPoints.get(i), impacted.getPoint(1));
			double newDepth = Vector.dot(impacted.getEdge(1).getNormal(), diff);
			if (depth - newDepth < 0.05) {
				Vector idPoint = Vector.add(finalImpactingPoints.get(i), Vector.multiply(impacted.getEdge(1).getNormal(), -newDepth));
				finalImpactedPoints.add(idPoint); //TODO
			} else {
				finalImpactedPoints.add(null);
			}
		}

		Vector[] impactedManifold = new Vector[finalImpactedPoints.size()];
		Vector[] impactingManifold = new Vector[finalImpactingPoints.size()];
		impactedManifold = finalImpactedPoints.toArray(impactedManifold);
		impactingManifold = finalImpactingPoints.toArray(impactingManifold);

		BoundingArea finalImpacted = aImpacted ? a : b;
		BoundingArea finalImpacting = aImpacted ? b : a;
		mtv = !aImpacted ? Vector.multiply(mtv, -1) : mtv;

		return new Collision(finalImpacted, finalImpacting, mtv, impactedManifold, impactingManifold);
	}

	/**
	 * Clips a polygon with an axis
	 * @param pol the polygon
	 * @param p a point on the axis
	 * @param a the direction of the axis
	 * @return the clipped polygon
	 */
	public Polygon clip(Polygon pol, Vector p, Vector a) {
		Vector normal = a.getNormal();
		ArrayList<Vector> newPoints = new ArrayList<>();

		boolean closed = pol.getPoints().length == pol.getEdges().length;

		for (int i = 0; i < pol.getEdges().length; i++) {
			Vector start = pol.getPoint(i);
			Vector end = pol.nextPoint(i);

			double startDot = Vector.dot(Vector.subtract(start, p), normal);
			double endDot = Vector.dot(Vector.subtract(end, p), normal);

			if (startDot > 0 && endDot < 0) {
				Vector b = pol.getEdge(i).normalized();
				Vector q = start;
				double s = (b.x() * (q.y() - p.y()) + b.y() * (p.x() - q.x())) / (b.x() * a.y() - a.x() * b.y());
				Vector newPoint = Vector.add(p, Vector.multiply(a, s));
				newPoints.add(newPoint);

				if (!closed && i == pol.getEdges().length - 1) {
					newPoints.add(end);
				}

			} else if (startDot < 0 && endDot > 0) {
				Vector b = pol.getEdge(i).normalized();
				Vector q = start;
				double s = (b.x() * (q.y() - p.y()) + b.y() * (p.x() - q.x())) / (b.x() * a.y() - a.x() * b.y());
				Vector newPoint = Vector.add(p, Vector.multiply(a, s));
				newPoints.add(start);
				newPoints.add(newPoint);

			} else if (startDot < 0 && endDot < 0) {
				newPoints.add(start);
				if (!closed && i == pol.getEdges().length - 1) {
					newPoints.add(end);
				}
			}
		}

		Vector[] newPointArray = new Vector[newPoints.size()];
		return new Polygon(newPoints.toArray(newPointArray), closed, pol.getContainer());
	}
}
