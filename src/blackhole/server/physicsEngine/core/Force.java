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
package blackhole.server.physicsEngine.core;

import blackhole.utils.Vector;

/**
 * Describes a physical Force acting on an object
 * @author fabian
 */
public class Force {

	/**
	 * The force vector
	 */
    private Vector force;
	
	/**
	 * the point shere the force acts on, relative to the object
	 */
    private Vector point;

	/**
	 * Creates a new {@code Force} wih the given parameters
	 * @param f the force vector
	 * @param p the point it acts on, relative to the object
	 */
    public Force(Vector f, Vector p) {
        force = f;
        point = p;
    }

	/**
	 * Returns the point the force acts on, relative to the object
	 * @return the point the force acts on, relative to the object
	 */
    public Vector getPoint() {
        return point;
    }

	/**
	 * Returns the force vector
	 * @return the force vector
	 */
    public Vector getForce() {
        return force;
    }
}
