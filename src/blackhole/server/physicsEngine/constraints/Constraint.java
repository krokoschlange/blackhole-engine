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
import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;

/**
 * Constrains the movement of {@link PhysicsStrategy}s.
 * @author fabian
 */
public class Constraint {

	/**
	 * The jacobian matrix describing the constraint
	 */
    private Matrix jacobian;
	
	/**
	 * A bias term
	 */
    private Matrix bias;
	
	/**
	 * The first {@link PhysicsStrategy}
	 */
    private PhysicsStrategy stratA;
	
	/**
	 * The second {@link PhysicsStrategy}
	 */
    private PhysicsStrategy stratB;
	
	/**
	 * The lagrange multiplier (proportional to the force the constraint
	 * generates)
	 */
    private Matrix lagMultiplierSum;
	
	/**
	 * Whether to limit the lagrange multiplier to a maximum
	 */
    private boolean clampTop;
	
	/**
	 * Whether to limit the lagrange multiplier to a minimum
	 */
    private boolean clampBottom;
	
	/**
	 * The upper lagrange multiplier limit
	 */
    private double clampTopVal;
	
	/**
	 * The bottom lagrange multiplier limit
	 */
    private double clampBottomVal;
	
	/**
	 * temporary lagrange multiplier calculated in one iteration of the solving
	 * algorithm
	 */
    private Matrix tmpLagMultiplier;
	
	/**
	 * The mass matrix of the two {@link PhysicsStrategy}s
	 */
    private Matrix mass;

	/**
	 * Creates a new {@code Constraint} according to the given paramters
	 * @param J the jacobian that describes the constraint
	 * @param bi the bias term
	 * @param a the first {@link PhysicsStrategy}
	 * @param b the second {@link PhysicsStrategy}
	 */
    public Constraint(Matrix J, Matrix bi, PhysicsStrategy a, PhysicsStrategy b) {
        jacobian = J;
        bias = bi;
        stratA = a;
        stratB = b;
        lagMultiplierSum = new Matrix(J.getHeight(), 1, 0);
        tmpLagMultiplier = new Matrix(J.getHeight(), 1, 0);
        clampTop = false;
        clampBottom = false;
        clampTopVal = 0;
        clampBottomVal = 0;
        mass = new Matrix(new double[][]{
            {stratA.getMass(), 0, 0, 0, 0, 0},
            {0, stratA.getMass(), 0, 0, 0, 0},
            {0, 0, stratA.getMomentOfInertia(), 0, 0, 0},
            {0, 0, 0, stratB.getMass(), 0, 0},
            {0, 0, 0, 0, stratB.getMass(), 0},
            {0, 0, 0, 0, 0, stratB.getMomentOfInertia()}
        });
        stratA.constraintCreated(this);
        stratB.constraintCreated(this);
    }

	/**
	 * Sets the jacobian of the constraint
	 * @param J the new jacobian
	 */
    public void setJacobians(Matrix J) {
        jacobian = J;
    }

	/**
	 * Returns the jacobian of the constraint
	 * @return the jacobian of the constraint
	 */
    public Matrix getJacobians() {
        return jacobian;
    }

	/**
	 * Sets the bias term of the constraint
	 * @param bi the new bias term
	 */
    public void setBias(Matrix bi) {
        bias = bi;
    }

	/**
	 * Returns the bias term of the constraint
	 * @return the bias term of the constraint
	 */
    public Matrix getBias() {
        return bias;
    }

	/**
	 * Sets the first object that the constraint influences
	 * @param a the object
	 */
    public void setObjectA(PhysicsStrategy a) {
        stratA = a;
    }

	/**
	 * Returns the first object that the constraint influences
	 * @return the first object that the constraint influences
	 */
    public PhysicsStrategy getObjectA() {
        return stratA;
    }

	/**
	 * Sets the second object that the constraint influences
	 * @param b the object
	 */
    public void setObjectB(PhysicsStrategy b) {
        stratB = b;
    }

	/**
	 * Returns the second object that the constraint influences
	 * @return the second object that the constraint influences
	 */
    public PhysicsStrategy getObjectB() {
        return stratB;
    }

	/**
	 * Returns the upper limit of the lagrange multiplier value for the
	 * constraint or {@code null} if ther is none
	 * @return the upper limit of the lagrange multiplier
	 */
    public Double getClampTop() {
        if (clampTop) {
            return clampTopVal;
        }
        return null;
    }

	/**
	 * Returns the bottom limit of the lagrange multiplier value for the
	 * constraint or {@code null} if ther is none
	 * @return the upper limit of the lagrange multiplier
	 */
    public Double getClampBottom() {
        if (clampBottom) {
            return clampBottomVal;
        }
        return null;
    }

	/**
	 * Sets the upper limit of the lagrange multiplier
	 * @param val the limit
	 * @param clamp whther to use that limit
	 */
    public void setClampTop(double val, boolean clamp) {
        clampTop = clamp;
        clampTopVal = val;
    }

	/**
	 * Sets the bottom limit of the lagrange multiplier
	 * @param val the limit
	 * @param clamp whther to use that limit
	 */
    public void setClampBottom(double val, boolean clamp) {
        clampBottom = clamp;
        clampBottomVal = val;
    }

	/**
	 * Returns the temporary lagrange multiplier of the current solving
	 * iteration
	 * @return the temporary lagrange multiplier
	 */
    public Matrix getTmpLagMul() {
        return tmpLagMultiplier;
    }

	/**
	 * Returns the lagrange multiplier
	 * @return the lagrange multiplier
	 */
    public Matrix getLagMulSum() {
        return lagMultiplierSum;
    }

	/**
	 * Calculates the temporary lagrange multiplier for one iteration
	 */
    public void calculateLagMul() {
        Matrix vel = new Matrix(new double[][]{
            {stratA.getTmpConstraintVel().get(0, 0)},
            {stratA.getTmpConstraintVel().get(1, 0)},
            {stratA.getTmpConstraintVel().get(2, 0)},
            {stratB.getTmpConstraintVel().get(0, 0)},
            {stratB.getTmpConstraintVel().get(1, 0)},
            {stratB.getTmpConstraintVel().get(2, 0)}
        });
        tmpLagMultiplier = Matrix.multiply(
                Matrix.getInverse(
                        Matrix.multiply(
                                Matrix.multiply(jacobian,
                                        Matrix.getInverse(mass)),
                                Matrix.transposed(jacobian))),
                Matrix.subtract(
                        Matrix.multiply(
                                Matrix.multiply(jacobian, -1),
                                vel),
                        bias));
    }

	/**
	 * Applies the temporary lagrange multiplier to the velocities of the
	 * objects
	 */
    public void applyTmpLagMul() {
        Matrix deltaVel = Matrix.multiply(
                Matrix.multiply(
                        Matrix.getInverse(mass),
                        Matrix.transposed(jacobian)),
                tmpLagMultiplier);

        Matrix aTmpVel = stratA.getTmpConstraintVel();
        Matrix bTmpVel = stratB.getTmpConstraintVel();
        stratA.setTmpConstraintVel(new Matrix(new double[][]{
            {aTmpVel.get(0, 0) + deltaVel.get(0, 0)},
            {aTmpVel.get(1, 0) + deltaVel.get(1, 0)},
            {aTmpVel.get(2, 0) + deltaVel.get(2, 0)}
        }));
        stratB.setTmpConstraintVel(new Matrix(new double[][]{
            {bTmpVel.get(0, 0) + deltaVel.get(3, 0)},
            {bTmpVel.get(1, 0) + deltaVel.get(4, 0)},
            {bTmpVel.get(2, 0) + deltaVel.get(5, 0)}
        }));
    }

	/**
	 * Applies the solved constraint to the objects
	 */
    public void apply() {
        Matrix aTmpVel = stratA.getTmpConstraintVel();
        Matrix bTmpVel = stratB.getTmpConstraintVel();
		
		GameObject objA = stratA.getObject();
		GameObject objB = stratB.getObject();

        objA.setVelocity(new Vector(aTmpVel.get(0, 0),
                aTmpVel.get(1, 0)));
        objA.setAngularVelocity(aTmpVel.get(2, 0));
        objB.setVelocity(new Vector(bTmpVel.get(0, 0),
                bTmpVel.get(1, 0)));
        objB.setAngularVelocity(bTmpVel.get(2, 0));
    }

	/**
	 * limits the lagrange multiplier
	 */
    public void clampLagMultiplier() {
        Matrix lagMulSum2 = new Matrix(tmpLagMultiplier.getHeight(), 1);
        for (int i = 0; i < tmpLagMultiplier.getHeight(); i++) {
            lagMulSum2.set(i, 0, lagMultiplierSum.get(i, 0));
        }
        lagMultiplierSum.add(tmpLagMultiplier);
        for (int i = 0; i < lagMultiplierSum.getHeight(); i++) {
            lagMultiplierSum.set(i, 0, clampTop
                    ? Math.min(lagMultiplierSum.get(i, 0), clampTopVal)
                    : lagMultiplierSum.get(i, 0));
            lagMultiplierSum.set(i, 0, clampBottom
                    ? Math.max(lagMultiplierSum.get(i, 0), clampBottomVal)
                    : lagMultiplierSum.get(i, 0));
        }

        tmpLagMultiplier = Matrix.subtract(lagMultiplierSum, lagMulSum2);
    }
}
