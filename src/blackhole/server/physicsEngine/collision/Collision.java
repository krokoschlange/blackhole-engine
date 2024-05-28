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

import blackhole.server.physicsEngine.boundingAreas.BoundingArea;
import blackhole.utils.Vector;

/**
 * Describes a collision between two {@link BoundingArea}s
 *
 * @author fabian
 */
public class Collision {

	/**
	 * The {@link BoundingArea} that got impacted on
	 */
	private BoundingArea impactedArea;

	/**
	 * The {@link BoundingArea} that impacted on {@link #impactedArea}
	 */
	private BoundingArea impactingArea;

	/**
	 * the collision points on {@link #impactedArea}
	 */
	private Vector[] impactedPoints;

	/**
	 * the collision points on {@link #impactingArea}
	 */
	private Vector[] impactingPoints;

	/**
	 * the minimum translation vector (the vector with the smallest magnitude
	 * that, if one would translate one of the objects by that vector, it would
	 * separate them)
	 */
	private Vector mtv;

	/**
	 * Creates a new {@code Collision} with the given parameters
	 *
	 * @param a the {@link BoundingArea} that got impacted on
	 * @param b the impacting {@link BoundingArea}
	 * @param v the minimum translation vector
	 * @param aP collision points in {@code a}
	 * @param bP collision points in {@code b}
	 */
	public Collision(BoundingArea a, BoundingArea b, Vector v, Vector[] aP, Vector[] bP) {
		impactedArea = a;
		impactingArea = b;
		mtv = v;
		impactedPoints = aP;
		impactingPoints = bP;
	}

	/**
	 * Returns the {@link BoundingArea} that got impacted on
	 *
	 * @return the {@link BoundingArea} that got impacted on
	 */
	public BoundingArea getImpacted() {
		return impactedArea;
	}

	/**
	 * Returns the {@link BoundingArea} that impacted on {@link #impactedArea}
	 *
	 * @return the {@link BoundingArea} that impacted on {@link #impactedArea}
	 */
	public BoundingArea getImpacting() {
		return impactingArea;
	}

	/**
	 * Returns {@code true} if the two {@link BoundingArea}s of the two
	 * {@code Collision}s are the same
	 *
	 * @param other the other {@code Collision}
	 * @return {@code true} if the two {@link BoundingArea}s of the two
	 * {@code Collision}s are the same
	 */
	public boolean isEqual(Collision other) {
		return (impactedArea == other.getImpacted() && impactingArea == other.getImpacting())
				|| (impactedArea == other.getImpacting() && impactingArea == other.getImpacted());
	}

	/**
	 * Returns the collision points on {@link #impactedArea}
	 * @return the collision points on {@link #impactedArea}
	 */
	public Vector[] getImpactedManifold() {
		return impactedPoints;
	}

	/**
	 * Returns the collision points on {@link #impactingArea}
	 * @return the collision points on {@link #impactingArea}
	 */
	public Vector[] getImpactingManifold() {
		return impactingPoints;
	}

	/**
	 * Returns the minimum translation vector
	 * @return the minimum translation vector
	 */
	public Vector getMTV() {
		return mtv;
	}

}
