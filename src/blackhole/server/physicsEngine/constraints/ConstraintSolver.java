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

import blackhole.server.physicsEngine.core.PhysicsStrategy;
import blackhole.utils.Debug;
import java.util.ArrayList;
import java.util.Iterator;
import blackhole.utils.Matrix;
import blackhole.utils.Vector;

/**
 * Solves {@link Constraint}s. For an explanation of the algorithm, see
 * <a href="http://allenchou.net/2013/12/game-physics-constraints-sequential-impulse/">
 * http://allenchou.net/2013/12/game-physics-constraints-sequential-impulse/</a>
 * @author fabian
 */
public class ConstraintSolver {

	/**
	 * A list containing all constraints that have to be solved
	 */
    private ArrayList<Constraint> constraints;

	/**
	 * Creates a new {@code ConstraintSolver}
	 */
    public ConstraintSolver() {
        constraints = new ArrayList<>();
    }

	/**
	 * Solves all constraints
	 * @param dtime time since the last call
	 */
    public void solve(double dtime) {
		//the maximum temporary lagrange multiplier of all constaints that
		//occurs in the current solving iteration
        double maxLagMul = 100;
		
		//when maxLagMul gets smaller that this value, the changes in the
		//entire system are considered too small and therfore we can say that
		//we have solved all constraints (we will never solve them exactly since
		//this is an iterative approach)
        double requiredAccuracy = 0.1 * Math.pow(10, -dtime);
		
		//solve while we have not achieved the required accuracy
		//also, there is a maximum of iterations we allow to make sure we don't
		//get stuck in this loop for too long, even if that means physics
		//glitches
        for (int iterations = 0; iterations < 100 && maxLagMul > requiredAccuracy; iterations++) {
            Iterator<Constraint> iter = constraints.iterator();
            maxLagMul = -1;
			
			//solve all constraints for the current iteration
            while (iter.hasNext()) {
                Constraint con = iter.next();
                con.calculateLagMul();
                con.clampLagMultiplier();
                con.applyTmpLagMul();
                Matrix lagMul = con.getTmpLagMul();
                for (int i = 0; i < lagMul.getHeight(); i++) {
                    if (Math.abs(lagMul.get(i, 0)) > maxLagMul) {
                        maxLagMul = Math.abs(lagMul.get(i, 0));
                    }
                }
            }
        }
		
		//apply the constraints
        Iterator<Constraint> iter = constraints.iterator();
        while (iter.hasNext()) {
            Constraint con = iter.next();
            con.apply();

            Matrix lagMulSum = con.getLagMulSum();
            for (int i = 0; i < lagMulSum.getHeight(); i++) {
                if (con.getClampTop() != null && Math.abs(lagMulSum.get(i, 0) - 0.001) > con.getClampTop()) {
                    con.getObjectA().constraintBroke(con);
                    con.getObjectB().constraintBroke(con);
                    break;
                }
            }

            con.getObjectA().constraintRemoved(con);
            con.getObjectB().constraintRemoved(con);
        }

		//remove all constraints
		//support for constraints that last longer than one game step is
		//possible, but not implemented (yet)
        constraints.clear();
    }

	/**
	 * Adds a {@link Constraint} to the solver
	 * @param c the constraint
	 */
    public void addConstraint(Constraint c) {
        constraints.add(c);
    }

	/**
	 * Removes a {@link Constraint} from the solver
	 * @param c the constraint
	 */
    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }
}
