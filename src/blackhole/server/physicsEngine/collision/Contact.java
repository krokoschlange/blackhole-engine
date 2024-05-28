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

import blackhole.common.GameObject;
import blackhole.server.physicsEngine.constraints.Constraint;
import blackhole.server.physicsEngine.constraints.ConstraintSolver;
import blackhole.server.physicsEngine.constraints.ContactConstraint;
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Debug;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;

/**
 * Describes a contact between two {@link PhysicsStrategy}s
 * @author fabian
 */
public class Contact {

	/**
	 * the first {@link PhysicsStrategy}
	 */
    private PhysicsStrategy a;
	
	/**
	 * the point of the first {@link PhysicsStrategy} that is the furthest in
	 * the second {@link PhysicsStrategy}
	 */
    private Vector aPoint;
	
	/**
	 * the second {@link PhysicsStrategy}
	 */
    private PhysicsStrategy b;
	
	/**
	 * the point of the second {@link PhysicsStrategy} that is the furthest
	 * inside the first {@link PhysicsStrategy}
	 */
    private Vector bPoint;
	
	/**
	 * the collision normal
	 */
    private Vector normal;
	
	/**
	 * the constraint that keeps the objects apart
	 */
    private Constraint normalCon;
	
	/**
	 * the constraint affecting tangential movement (friction)
	 */
    private Constraint tangentCon;
	
	/**
	 * stabilization factor
	 */
    private double baumgarte = 0.2;
	
	/**
	 * slop value for penetration
	 */
    private double penSlop = 0.05;
	
	/**
	 * slop value for bouncing
	 */
    private double resSlop = 1;

	/**
	 * Creates a new {@code Contact with the given parameters}
	 * @param stratA the first {@link PhysicsStrategy}
	 * @param stratB the second {@link PhysicsStrategy}
	 * @param aP the first {@link PhysicsStrategy}'s contact point
	 * @param bP the second {@link PhysicsStrategy}'s contact point
	 * @param n the collision normal
	 * @param dtime the time between game steps
	 * @param solver the constraint solver to be used
	 */
    public Contact(PhysicsStrategy stratA, PhysicsStrategy stratB, Vector aP, Vector bP, Vector n, double dtime, ConstraintSolver solver) {
        a = stratA;
        b = stratB;
		GameObject objA = stratA.getObject();
		GameObject objB = stratB.getObject();
        aPoint = aP;
        bPoint = bP;
        normal = n;
        normalCon = new ContactConstraint(a, b, aPoint, bPoint, normal, dtime,
                baumgarte, penSlop, resSlop, this);
        Vector tangent = normal.getNormal();
        Matrix tJ = new Matrix(new double[][]{
            {
                Vector.multiply(tangent, -1).x(),
                Vector.multiply(tangent, -1).y(),
                Vector.cross(Vector.subtract(objA.getPosition(), aPoint), tangent),
                tangent.x(),
                tangent.y(),
                Vector.cross(Vector.subtract(bPoint, objB.getPosition()), tangent)
            }
        });
        tangentCon = new Constraint(tJ, new Matrix(1, 1, 0), a, b);
        solver.addConstraint(normalCon);
        solver.addConstraint(tangentCon);
    }

	/**
	 * Returns the first {@link PhysicsStrategy}
	 * @return the first {@link PhysicsStrategy}
	 */
    public PhysicsStrategy getA() {
        return a;
    }

	/**
	 * Returns the second {@link PhysicsStrategy}
	 * @return the second {@link PhysicsStrategy}
	 */
    public PhysicsStrategy getB() {
        return b;
    }

	/**
	 * Returns the point of the first {@link PhysicsStrategy} that is the
	 * furthest in the second {@link PhysicsStrategy}
	 * @return the first {@link PhysicsStrategy}'s contact point
	 */
    public Vector getAPoint() {
        return aPoint;
    }

	/**
	 * Returns the point of the second {@link PhysicsStrategy} that is the
	 * furthest in the first {@link PhysicsStrategy}
	 * @return the second {@link PhysicsStrategy}'s contact point
	 */
    public Vector getbPoint() {
        return bPoint;
    }

	/**
	 * Returns the collision normal
	 * @return the collision normal
	 */
    public Vector getNormal() {
        return normal;
    }

	/**
	 * Returns the constraint affecting movement along the collision normal (the
	 * constraint that keeps the objects apart)
	 * @return the constraint that keeps the objects apart
	 */
    public Constraint getNormalConstraint() {
        return normalCon;
    }

	/**
	 * Returns the constraint affecting movement along the collision tangent
	 * (friction)
	 * @return the friction constraint
	 */
    public Constraint getTangentConstraint() {
        return tangentCon;
    }

    /*public boolean isSimilar(Contact other) {
        return false;
    }*/

	/**
	 * Limits the friction constraints effects according to both of the
	 * {@link PhysicsStrategy}'s friction values (otherwise the friction would
	 * be infinite)
	 * @param clamp a factor that describes the normal force of the contact
	 * (since friction is dependent on the normal force)
	 */
    public void setTangentClamp(double clamp) {
        double frictionCoefficient = (a.getFriction() + b.getFriction()) / 2;
        tangentCon.setClampTop(Math.abs(clamp) * frictionCoefficient, true);
        tangentCon.setClampBottom(-Math.abs(clamp) * frictionCoefficient, true);
    }
}
