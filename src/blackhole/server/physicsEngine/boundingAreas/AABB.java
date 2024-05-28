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

import blackhole.utils.Vector;

/**
 * Describes an axis aligned bounding box (a rectangle where all the sides are
 * parallel to the axis in the coordinate system)
 * @author fabian
 */
public class AABB {

	/**
	 * The point with the maximum x and y-coordinates
	 */
    private Vector maximum;
	
	/**
	 * The point with the minimum x and y-coordinates
	 */
    private Vector minimum;
	
	/**
	 * The {@link BoundingArea} that this AABB surrounds
	 */
    private BoundingArea boundingArea;

	/**
	 * Creates a new {@code AABB} with the given parameters
	 * @param p1 the first corner point
	 * @param p2 the corner point on the opposite side
	 * @param bA the {@link BoundingArea}
	 */
    public AABB(Vector p1, Vector p2, BoundingArea bA) {
        double x1 = p1.x() > p2.x() ? p1.x() : p2.x();
        double x2 = p1.x() < p2.x() ? p1.x() : p2.x();
        double y1 = p1.y() > p2.y() ? p1.y() : p2.y();
        double y2 = p1.y() < p2.y() ? p1.y() : p2.y();

        maximum = new Vector(x1, y1);
        minimum = new Vector(x2, y2);

        boundingArea = bA;
    }

	/**
	 * Returns the point with the maximum x and y-coordinates
	 * @return the point with the maximum x and y-coordinates
	 */
    public Vector getMaximum() {
        return maximum;
    }

	/**
	 * Returns the point with the minimum x and y-coordinates
	 * @return the point with the minimum x and y-coordinates
	 */
    public Vector getMinimum() {
        return minimum;
    }

	/**
	 * Returns an array containing the maximum and minimum x coordinates
	 * @return an array containing the maximum and minimum x coordinates
	 */
    public double[] getXValues() {
        return new double[]{maximum.x(), minimum.x()};
    }

	/**
	 * Returns an array containing the maximum and minimum y coordinates
	 * @return an array containing the maximum and minimum y coordinates
	 */
    public double[] getYValues() {
        return new double[]{maximum.y(), minimum.y()};
    }

	/**
	 * Returns the {@link BoundingArea} associated with this {@code AABB}
	 * @return the {@link BoundingArea} associated with this {@code AABB}
	 */
    public BoundingArea getBoundingArea() {
        return boundingArea;
    }
}
