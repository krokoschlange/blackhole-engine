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
package blackhole.utils;

import java.io.Serializable;

/**
 * A class representing a two-dimensional mathematical vector. It is implemented
 * as a subclass of {@code Matrix} to allow operations involving objects of
 * both classes, e.g. multiplying a matrix with a vector.
 *
 * @author fabian
 */
public class Vector extends Matrix implements Serializable {

    /**
     * Create a new vector with the elements {@code x} and {@code y}
     *
     * @param x the first element
     * @param y the second element
     */
    public Vector(double x, double y) {
        super(2, 1);
        set(0, 0, x);
        set(1, 0, y);
    }

    /**
     * Create a new vector and copy the elements from another vector
     *
     * @param v the vector to copy the elements from
     */
    public Vector(Vector v) {
        this(v.x(), v.y());
    }

    /**
     * Create a new vector and initialize both elements with 0
     */
    public Vector() {
        this(0, 0);
    }

    /**
     * Get the x (first) element of the vector
     *
     * @return the x (first) element of the vector
     */
    public double x() {
        return get(0, 0);
    }

    /**
     * Get the y (second) element of the vector
     *
     * @return the y (second) element of the vector
     */
    public double y() {
        return get(1, 0);
    }

    /**
     * Set both elements of the vector
     *
     * @param x the new x value
     * @param y the new y value
     */
    public void set(double x, double y) {
        set(0, 0, x);
        set(1, 0, y);
    }

    /**
     * Add two vectors together without modifying them
     *
     * @param a the first summand
     * @param b the second summand
     * @return a new {@code Vector} object representing the sum of the two
     * vectors
     */
    public static Vector add(Vector a, Vector b) {
        return new Vector(a.x() + b.x(), a.y() + b.y());
    }

    /**
     * Add another vector to this one
     *
     * @param b the second summand
     * @return itself after the second vector has been added
     */
    public Vector add(Vector b) {
        set(x() + b.x(), y() + b.y());
        return this;
    }

    /**
     * Subtract two vectors from each other without modifying them
     *
     * @param a the minuend
     * @param b the subtrahend
     * @return a new {@code Vector} object representing the difference of
     * the two vectors
     */
    public static Vector subtract(Vector a, Vector b) {
        return new Vector(a.x() - b.x(), a.y() - b.y());
    }

    /**
     * Subtract another vector from this one
     *
     * @param b the subtrahend
     * @return itself after the second vector has been subtracted
     */
    public Vector subtract(Vector b) {
        set(x() - b.x(), y() - b.y());
        return this;
    }

    /**
     * Multiply a vector with a scalar without modifying the vector
     *
     * @param a the vector to be multipied
     * @param b the scalar to be multiplied with
     * @return a new {@code Vector} object representing the product of
     * {@code a} and {@code b}
     */
    public static Vector multiply(Vector a, double b) {
        return new Vector(a.x() * b, a.y() * b);
    }

    /**
     * Multiply this vector with a scalar
     *
     * @param b the scalar to be multiplied with
     * @return itself after being multiplied with the scalar
     */
    @Override
    public Vector multiply(double b) {
        set(x() * b, y() * b);
        return this;
    }

    /**
     * "Multiply" a vector with another vector without modifying them. The
     * elements are multiplied element-wise.
     *
     * @param a the first factor
     * @param b the second factor
     * @return a new {@code Vector} object representing the "product" of
     * the two vectors
     */
    public static Vector multiply(Vector a, Vector b) {
        return new Vector(a.x() * b.x(), a.y() * b.y());
    }

    /**
     * "Multiply" this vector with another one. The elements are multiplied
     * element-wise.
     *
     * @param b the second factor
     * @return itself after being "multiplied" with the other vector
     */
    public Vector multiply(Vector b) {
        set(x() * b.x(), y() * b.y());
        return this;
    }

    /**
     * Divide a vector by a scalar without modifying the vector
     *
     * @param a the vector to be divided
     * @param b the scalar to be divided by
     * @return a new {@code Vector} object representing the quotient of
     * {@code a} and {@code b}
     */
    public static Vector divide(Vector a, double b) {
        return new Vector(a.x() / b, a.y() / b);
    }

    /**
     * Multiply this vector by a scalar
     *
     * @param b the scalar to be divided by
     * @return itself after being divided by the scalar
     */
    public Vector divide(double b) {
        set(x() / b, y() / b);
        return this;
    }

    /**
     * "Divide" a vector by another vector without modifying them. The elements
     * are divided element-wise.
     *
     * @param a the dividend
     * @param b the divisor
     * @return a new {@code Vector} object representing the "quotient" of
     * the two vectors
     */
    public static Vector divide(Vector a, Vector b) {
        return new Vector(a.x() / b.x(), a.y() / b.y());
    }

    /**
     * "Divide" this vector by another one. The elements are divided
     * element-wise.
     *
     * @param b the divisor
     * @return itself after being "divided" by the other vector
     */
    public Vector divide(Vector b) {
        set(x() / b.x(), y() / b.y());
        return this;
    }

    /**
     * Get the dot product of two vectors
     *
     * @param a the first vector
     * @param b the second factor
     * @return the dot product of the two
     */
    public static double dot(Vector a, Vector b) {
        return a.x() * b.x() + a.y() * b.y();
    }

    /**
     * Get the dot product of this vector with another one
     *
     * @param b the other vector
     * @return the dot product of this vector with another one
     */
    public double dot(Vector b) {
        return x() * b.x() + y() * b.y();
    }

    /**
     * Get the cross product of two vectors
     *
     * @param a the first vector
     * @param b the second factor
     * @return the cross product of the two
     */
    public static double cross(Vector a, Vector b) {
        return a.x() * b.y() - a.y() * b.x();
    }

    /**
     * Get the cross product of this vector with another one
     *
     * @param b the other vector
     * @return the cross product of this vector with another one
     */
    public double cross(Vector b) {
        return x() * b.y() - y() * b.x();
    }

    /**
     * Get a normalized version of this vector
     *
     * @param a the vector to get the normalized version from
     * @return a new {@code Vector} object representing a normalized
     * version of the original vector
     */
    public static Vector normalized(Vector a) {
        return Vector.divide(a, a.magnitude());
    }

    /**
     * Get a normalized version of this vector
     *
     * @return a new {@code Vector} object representing a normalized
     * version of the original vector
     */
    public Vector normalized() {
        return Vector.divide(this, magnitude());
    }

    /**
     * Normalize this vector (divide it by its length). This method modifies the
     * original {@code Vector} object!
     *
     * @return itself after it has been normalized
     */
    public Vector normalize() {
        divide(magnitude());
        return this;
    }

    /**
     * Get the magnitude (lenght) of this vector
     *
     * @return the magnitude of this vector
     */
    public double magnitude() {
        return Math.sqrt(x() * x() + y() * y());
    }

    /**
     * Rotate this vector counter-clockwise. This modifies the original {@code Vector}
     * object!
     *
     * @param rot the angle (in radians)
     * @return itself after being rotated
     */
    public Vector rotate(double rot) {
        double newX = Math.cos(rot) * x() - Math.sin(rot) * y();
        double newY = Math.sin(rot) * x() + Math.cos(rot) * y();
        set(newX, newY);
        return this;
    }

    /**
     * Rotate a vector counter-clockwise.
     *
     * @param v the vector to be rotated
     * @param rot the angle (in radians)
     * @return a new {@code Vector} object representing a rotated version
     * of the original one
     */
    public static Vector rotate(Vector v, double rot) {
        double newX = Math.cos(rot) * v.x() - Math.sin(rot) * v.y();
        double newY = Math.sin(rot) * v.x() + Math.cos(rot) * v.y();
        return new Vector(newX, newY);
    }

    /**
     * Get a normal of this vector (a vector that is orthogonal and on the right
	 * side of this one, i.e. rotated 90 degrees clockwise) with magnitude 1
     *
     * @return an orthogonal vector to this one
     */
    public Vector getNormal() {
        Vector normal = new Vector(x(), y());
        normal.rotate(-Math.PI / 2);
        normal.normalize();
        return normal;
    }

    /**
     * Get a {@code String} representation of this vector, e.g.:<br>
     * {@code (4.2|0.0)}
     *
     * @return  {@code String} representation of this vector
     */
    @Override
    public String toString() {
        return "(" + x() + " | " + y() + ")";
    }
}
