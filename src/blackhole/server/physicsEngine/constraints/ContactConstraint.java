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
package blackhole.server.physicsEngine.constraints;

import blackhole.common.GameObject;
import blackhole.server.physicsEngine.collision.Contact;
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Debug;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;

/**
 * A {@link Constraint} that keeps colliding objects apart.
 * @author fabian
 */
public class ContactConstraint extends Constraint {

	/**
	 * The contact between the two colliding objects
	 */
    private Contact contact;

	/**
	 * Creates a new {@code ContactConstraint} with the given paramters
	 * @param a the first object
	 * @param b the first object
	 * @param aPoint the first {@link PhysicsStrategy}'s contact point
	 * @param bPoint the second {@link PhysicsStrategy}'s contact point
	 * @param normal the collision normal
	 * @param dtime the time between game steps
	 * @param bS baumgarte stabilization factor
	 * @param pSlop slop value for penetration
	 * @param rSlop slop value for bouncing
	 * @param con the contact
	 */
    public ContactConstraint(PhysicsStrategy a, PhysicsStrategy b, Vector aPoint, Vector bPoint, Vector normal, double dtime, double bS, double pSlop, double rSlop, Contact con) {
        super(new Matrix(1, 6, 0), new Matrix(1, 1, 0), a, b);
        contact = con;
		
		GameObject objA = a.getObject();
		GameObject objB = b.getObject();
		
        Matrix J = new Matrix(new double[][]{
            {
                Vector.multiply(normal, -1).x(),
                Vector.multiply(normal, -1).y(),
                Vector.cross(Vector.subtract(objA.getPosition(), aPoint), normal),
                normal.x(),
                normal.y(),
                Vector.cross(Vector.subtract(bPoint, objB.getPosition()), normal)}
        });
        setJacobians(J);
        double depth = Vector.subtract(bPoint, aPoint).magnitude();
        double bias = (-(bS / dtime)) * Math.max(depth - pSlop, 0); //baumgarte Stabilization
        double restitutionCoefficient = (a.getBounciness() + b.getBounciness()) / 2;
        Vector aPointLever = Vector.subtract(aPoint, objA.getPosition());
        Vector aPointVel = Vector.add((Vector) aPointLever.normalized().rotate(Math.PI / 2).multiply(aPointLever.magnitude() * objA.getAngularVelocity()), objA.getVelocity());
        Vector bPointLever = Vector.subtract(bPoint, objB.getPosition());
        Vector bPointVel = Vector.add(objB.getVelocity(), (Vector) bPointLever.normalized().rotate(Math.PI / 2).multiply(bPointLever.magnitude() * objB.getAngularVelocity()));
        double restitiutionBias = restitutionCoefficient * Math.max(0, Vector.dot(Vector.subtract(aPointVel, bPointVel), normal) - rSlop); //bouncing
        bias -= restitiutionBias;
        setBias(new Matrix(1, 1, bias));
        setClampTop(0, false);
        setClampBottom(0, true);
    }

	/**
	 * {@inheritDoc}
	 * This also clamps the contact's tangential force (friction)
	 */
    @Override
    public void clampLagMultiplier() {
        super.clampLagMultiplier();
        contact.setTangentClamp(getLagMulSum().get(0, 0));

    }
}
