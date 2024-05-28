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

import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Vector;

/**
 * A {@link BoundingArea} that describes a circle. Since circles cannot be used
 * with the separating axis theorem test, it is simplified to a triangle that is
 * always placed in such a way that it would behave just like a circle (i.e. the
 * base of the triangle is always a tangent on the circle at the point with the
 * closest distance to the circle.
 * @author fabian
 */
public class Circle extends BoundingArea {

	/**
	 * The radius of the circle
	 */
	double radius;

	/**
	 * Creates a new {@code Circle} with the given radius and container.
	 * @param r the radius
	 * @param pS the container
	 */
	public Circle(double r, BoundingAreaContainer pS) {
		super(pS);
		radius = r;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRotation(double rot) {
		//It's round. Why would you bother rotate it?
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFurthestPointIDInDirection(Vector dir) {
		//nonsense, we don't have point ids here
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getFurthestPointInDirection(Vector dir) {
		Vector p = Vector.multiply(dir.normalized(), radius);
		p.add(position);
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getImpactingPart(Vector collDir, BoundingArea other) {
		/*Vector dir = Vector.subtract(getObject().getPosition(), other.getObject().getPosition()).normalized();
		Polygon impacted = other.getImpactedPart(dir, this);

		int bestPoint = impacted.getFurthestPointIDInDirection(dir);
		int bestEdge = bestPoint;

		double edgeDot = Vector.dot(impacted.getEdge(bestEdge).normalized(), dir);
		if (bestEdge != 0) {
			int otherEdge = bestEdge - 1;
			double otherDot = Vector.dot(impacted.getEdge(otherEdge).normalized(), dir);
			if (Math.abs(otherDot) < Math.abs(edgeDot)) {
				edgeDot = otherDot;
				bestEdge = otherEdge;
			}
		}

		Vector diff = Vector.subtract(getObject().getPosition(), impacted.getPoint(bestEdge));

		double dot = Vector.dot(diff, impacted.getEdge(bestEdge).normalized());
		Vector nearestPointDir;

		if (dot > 0 && dot < impacted.getEdge(bestEdge).magnitude()) {
			Vector normal = impacted.getEdge(bestEdge).getNormal();
			normal.multiply(-radius);
			nearestPointDir = normal;
		} else {
			nearestPointDir = diff;
			nearestPointDir.multiply(radius / nearestPointDir.magnitude());
		}*/
		Vector objdir = Vector.subtract(getPosition(), other.getPosition());

		Polygon imp = other.getImpactedPart(objdir, this);
		int furthest = imp.getFurthestPointIDInDirection(objdir);

		Vector dir;

		Vector diff = Vector.subtract(getPosition(), imp.getPoint(furthest));
		Vector edge = imp.getEdge(furthest).normalized();

		if (Vector.dot(diff, edge) > 0 && Vector.dot(diff, edge) < imp.getEdge(furthest).magnitude()) {
			dir = Vector.multiply(edge.getNormal(), -radius);
		} else {
			diff = Vector.subtract(getPosition(), imp.previousPoint(furthest));
			edge = imp.previousEdge(furthest).normalized();
			if (Vector.dot(diff, edge) > 0 && Vector.dot(diff, edge) < imp.previousEdge(furthest).magnitude()) {
				dir = Vector.multiply(edge.getNormal(), -radius);
			} else {
				dir = Vector.subtract(imp.getPoint(furthest), getPosition());
				dir.normalize();
				dir.multiply(radius);
			}
		}

		Vector normal = dir.getNormal();
		normal.multiply(-radius);

		Vector otherPoint1 = Vector.add(normal, getPosition());
		Vector otherPoint2 = Vector.add(Vector.multiply(normal, -1), getPosition());
		dir.add(getPosition());

		Vector[] newPoints = new Vector[3];

		newPoints[0] = otherPoint1;
		newPoints[1] = dir;
		newPoints[2] = otherPoint2;

		return new Polygon(newPoints, false, getContainer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getImpactedPart(Vector collDir, BoundingArea other) {
		Vector middle = Vector.subtract(other.getPosition(), getPosition());
		middle.normalize();
		middle.multiply(radius);

		Vector normal = middle.getNormal();
		normal.multiply(radius);
		Vector back = Vector.multiply(middle, -1);
		back.add(getPosition());
		middle.add(getPosition());
		Vector right = Vector.add(middle, normal);
		normal.multiply(-1);
		Vector left = Vector.add(middle, normal);

		Vector[] p = {back, right, left, back};

		return new Polygon(p, false, getContainer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getBestEdgeDotProduct(Vector collDir) {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getCollisionEdges(BoundingArea other) {
		/*Vector objdir = Vector.subtract(getPosition(), other.getPosition());

        Polygon imp = other.getImpactedPart(objdir, this);
        int furthest = imp.getFurthestPointIDInDirection(objdir);

        Vector dir;

        Vector diff = Vector.subtract(getPosition(), imp.getPoint(furthest));
        Vector edge = imp.getEdge(furthest).normalized();

        if (Vector.dot(diff, edge) > 0 && Vector.dot(diff, edge) < imp.getEdge(furthest).magnitude()) {
            dir = Vector.multiply(edge.getNormal(), -radius);
        } else {
            diff = Vector.subtract(getPosition(), imp.previousPoint(furthest));
            edge = imp.previousEdge(furthest).normalized();
            if (Vector.dot(diff, edge) > 0 && Vector.dot(diff, edge) < imp.previousEdge(furthest).magnitude()) {
                dir = Vector.multiply(edge.getNormal(), -radius);
            } else {
                dir = Vector.subtract(imp.getPoint(furthest), getPosition());
                dir.normalize();
                dir.multiply(radius);
            }
        }*/

		//dir = Vector.subtract(imp.getPoint(furthest), getObject().getPosition());
		/*dir.normalize();
		dir.multiply(radius);*/
 /*Vector normal = dir.getNormal();
        normal.multiply(-1);

		Vector p1 = Vector.add(Vector.multiply(dir, -1), getPosition());
        dir.add(getPosition());
        Vector p3 = Vector.add(Vector.multiply(normal, -1), dir);//getObject().getPosition());

        Vector p2 = Vector.add(dir, normal);

        Vector[] p = {p1, p3, p2, p1};
        return new Polygon(p, false, getContainer());*/
		Polygon otherPoly = other.getFullPolygonRepresentation(this);
		Vector[] otherEdges = otherPoly.getEdges();

		int smallestDistEdge = -1;
		double smallestDist = -1;
		for (int i = 0; i < otherEdges.length; i++) {
			Vector diff = Vector.subtract(getPosition(), otherPoly.getPoint(i));
			double dist = Math.abs(otherEdges[i].getNormal().dot(diff));
			double dot = otherEdges[i].normalized().dot(diff);

			if (smallestDist < 0 || smallestDist > dist) {
				if (dot > 0 && dot < otherEdges[i].magnitude()) {
					smallestDist = dist;
					smallestDistEdge = i;
				}
			}
		}
		Vector dir;
		if (smallestDistEdge == -1) {
			Vector smallestDistDir = null;
			smallestDist = -1;
			
			Vector[] otherPoints = otherPoly.getPoints();

			for (int i = 0; i < otherPoints.length; i++) {
				Vector diff = Vector.subtract(getPosition(), otherPoints[i]);
				double dist = diff.magnitude();
				if (smallestDist < 0 || smallestDist > dist) {
					smallestDist = dist;
					smallestDistDir = diff.normalize().multiply(-radius);
				}
			}
			
			dir = smallestDistDir;
		} else {
			dir = otherEdges[smallestDistEdge].getNormal().multiply(-radius);
		}
		
		Vector normal = dir.getNormal();
        normal.multiply(-1);

		Vector p1 = Vector.add(Vector.multiply(dir, -1), getPosition());
        dir.add(getPosition());
        Vector p3 = Vector.add(Vector.multiply(normal, -1), dir);//getObject().getPosition());

        Vector p2 = Vector.add(dir, normal);

        Vector[] p = {p1, p3, p2, p1};
        return new Polygon(p, false, getContainer());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon getFullPolygonRepresentation(BoundingArea other) {
		return getImpactingPart(null, other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPosition(Vector v) {
		position.set(v);
	}

}
