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
 * A class for doing matrix calculations. An instance of this class represents
 * an n * m matrix. It supports common matrix calculations like adding,
 * multiplying, transposing, inverting etc.
 *
 * @author fabian
 */
public class Matrix implements Serializable {

	/**
	 * The underlying array containing the elements in the matrix. The inner
	 * arrays contain the rows.
	 */
	private double[][] M;

	/**
	 * Create a new 3 * 3 matrix filled with 0s.
	 */
	public Matrix() {
		this(3, 3, 0);
	}

	/**
	 * Create a new m * n matrix filled with 0s.
	 *
	 * @param m height of the matrix
	 * @param n width of the matrix
	 */
	public Matrix(int m, int n) {
		this(m, n, 0);
	}

	/**
	 * Create a new m * n Matrix filled with x's
	 *
	 * @param m height of the matrix
	 * @param n width of the matrix
	 * @param x number to fill all elements of the matrix with
	 */
	public Matrix(int m, int n, double x) {
		M = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				set(i, j, x);
			}
		}
	}

	/**
	 * Create a new matrix and fill it with given values
	 *
	 * @param values the values to fill the matrix with
	 */
	public Matrix(double[][] values) {
		set(values);
	}

	/**
	 * Get the height of the matrix
	 *
	 * @return the height of the matrix
	 */
	public int getHeight() {
		return M.length;
	}

	/**
	 * Get the width of the matrix
	 *
	 * @return the width of the matrix
	 */
	public int getWidth() {
		return M[0].length;
	}

	/**
	 * Get an element of the matrix
	 *
	 * @param i the row of the element
	 * @param j the column of the element
	 * @return the element at the i-th row and the j-th column
	 */
	public double get(int i, int j) {
		return M[i][j];
	}

	/**
	 * Set the element at the i-th row and the j-th column of the matrix
	 *
	 * @param i the row of the element
	 * @param j the column of the element
	 * @param x the value of the element
	 */
	public void set(int i, int j, double x) {
		M[i][j] = x;
	}

	/**
	 * Replace the underlying array (i.e. all elements)
	 *
	 * @param arr the new array (it does not need to be of the same size as the
	 * old one, if it isn't the size of the matrix changes)
	 */
	public void set(double[][] arr) {
		M = arr;
	}

	/**
	 * Replace the underlying array by a copy of the underlying array of another
	 * matrix
	 *
	 * @param m the other matrix to copy the array from
	 */
	public void set(Matrix m) {
		M = new double[m.getHeight()][m.getWidth()];
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				M[i][j] = m.get(i, j);
			}
		}
	}

	/**
	 * Add another matrix to this one. The elements of this matrix are modified!
	 *
	 * @param n the second summand
	 * @return itself after the other matrix has been added or {@code null}
	 * if the matrices have different sizes
	 */
	public Matrix add(Matrix n) {
		Matrix ret = Matrix.add(this, n);
		if (ret != null) {
			set(ret);
		}
		return this;
	}

	/**
	 * Add two matrices together. Does not modify the elements of either matrix.
	 *
	 * @param m the first summand
	 * @param n the second summand
	 * @return a new {@code Matrix} object representing the sum of
	 * {@code m} and {@code n} or {@code null} if the matrices
	 * have different sizes
	 */
	public static Matrix add(Matrix m, Matrix n) {
		if (m.getHeight() == n.getHeight() && m.getWidth() == n.getWidth()) {
			Matrix ret = new Matrix(m.getHeight(), m.getWidth());
			for (int i = 0; i < m.getHeight(); i++) {
				for (int j = 0; j < m.getWidth(); j++) {
					ret.set(i, j, m.get(i, j) + n.get(i, j));
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Subtracts another matrix from this one. The elements of this matrix are
	 * modified!
	 *
	 * @param n the subtrahend
	 * @return itself after the other matrix has been subtracted or
	 * {@code null} if the matrices have different sizes
	 */
	public Matrix subtract(Matrix n) {
		Matrix ret = Matrix.subtract(this, n);
		if (ret != null) {
			set(ret);
		}
		return this;
	}

	/**
	 * Subtract a matrix from another one. Does not modify the elements of
	 * either matrix.
	 *
	 * @param m the minuend
	 * @param n the subtrahend
	 * @return a new {@code Matrix} object representing the difference of
	 * {@code m} and {@code n} or {@code null} if the matrices
	 * have different sizes
	 */
	public static Matrix subtract(Matrix m, Matrix n) {
		if (m.getHeight() == n.getHeight() && m.getWidth() == n.getWidth()) {
			Matrix ret = new Matrix(m.getHeight(), m.getWidth());
			for (int i = 0; i < m.getHeight(); i++) {
				for (int j = 0; j < m.getWidth(); j++) {
					ret.set(i, j, m.get(i, j) - n.get(i, j));
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Multiply another matrix with this one. The elements of this matrix are
	 * modified!
	 *
	 * @param n the second factor
	 * @return itself after being multiplied with the other matrix or
	 * {@code null} if this matrix's width is not equal to the other
	 * matrix's height
	 */
	public Matrix multiply(Matrix n) {
		Matrix ret = Matrix.multiply(this, n);
		if (ret != null) {
			set(ret);
		}
		return this;
	}

	/**
	 * Multiply two matrices. Does not modify the elements of either matrix.
	 *
	 * @param m the first factor
	 * @param n the second factor
	 * @return a new {@code Matrix} object representing the product of
	 * {@code m} and {@code n} or {@code null} if m's width is
	 * not equal to n's height
	 */
	public static Matrix multiply(Matrix m, Matrix n) {
		if (m.getWidth() == n.getHeight()) {
			Matrix ret = new Matrix(m.getHeight(), n.getWidth());
			for (int i = 0; i < m.getHeight(); i++) {
				for (int j = 0; j < n.getWidth(); j++) {
					double element = 0;
					for (int k = 0; k < n.getHeight(); k++) {
						element += m.get(i, k) * n.get(k, j);
					}
					ret.set(i, j, element);
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Multiply this matrix with a scalar
	 *
	 * @param d the scalar to multpily this matrix with
	 * @return itself after being multiplied with {@code d}
	 */
	public Matrix multiply(double d) {
		Matrix ret = Matrix.multiply(this, d);
		if (ret != null) {
			set(ret);
		}
		return this;
	}

	/**
	 * Multpily a matrix with a scalar. The original {@code Matrix} object
	 * will not be modified.
	 *
	 * @param m the matrix to be multplied
	 * @param d the scalar to multiply the matrix with
	 * @return a new {@code Matrix} object representing the product of
	 * {@code m} and {@code d}
	 */
	public static Matrix multiply(Matrix m, double d) {
		Matrix ret = new Matrix(m.getHeight(), m.getWidth());
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				ret.set(i, j, m.get(i, j) * d);
			}
		}
		return ret;
	}

	/**
	 * Transpose this matrix.
	 *
	 * @return itself after being transposed
	 */
	public Matrix transpose() {
		set(Matrix.transposed(this));
		return this;
	}

	/**
	 * Transpose a matrix. The original {@code Matrix} object will not be
	 * modified.
	 *
	 * @param m the matrix to be transposed
	 * @return a new {@code Matrix} object representing the transpose of
	 * {@code m}
	 */
	public static Matrix transposed(Matrix m) {
		Matrix ret = new Matrix(m.getWidth(), m.getHeight());
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				ret.set(j, i, m.get(i, j));
			}
		}
		return ret;
	}

	/**
	 * Get a submatrix of this matrix (a matrix where the i-th row and the j-th
	 * column are missing)
	 *
	 * @param i the row to remove
	 * @param j the column to remove
	 * @return a new {@code Matrix} object representing a submatrix of
	 * {@code m}, where the i-th row and the j-th column are missing
	 */
	public Matrix getSubmatrix(int i, int j) {
		Matrix min = new Matrix(getHeight() - 1, getWidth() - 1);
		for (int k = 0; k < getHeight(); k++) {
			if (k != i) {
				for (int l = 0; l < getWidth(); l++) {
					if (l != j) {
						int y;
						if (k < i) {
							y = k;
						} else {
							y = k - 1;
						}

						int x;
						if (l < j) {
							x = l;
						} else {
							x = l - 1;
						}

						min.set(y, x, get(k, l));
					}
				}
			}

		}
		return min;
	}

	/**

	 * Calculate the determinant of this matrix.
	 * @deprecated very slow, use {@link #fastDeterminant()} instead!
	 * @return the determinant of this matrix
	 */
	public double determinant() {
		Debug.logWarn("Matrix.determinant() is deprecated and very slow");
		if (getWidth() != getHeight()) {
			return 0;
		}
		switch (getWidth()) {
			case 1:
				return get(0, 0);
			case 2:
				return get(0, 0) * get(1, 1) - get(0, 1) * get(1, 0);
			default:
				double det = 0;
				for (int j = 0; j < getWidth(); j++) {
					Matrix min = getSubmatrix(0, j);

					det += Math.pow(-1, j + 0) * get(0, j) * min.determinant();
				}
				return det;
		}
	}

	/**
	 * Calculate the determinant of this matrix. This method uses LU
	 * decomposition instead of laplace's formula to compute the dtereminant
	 * which makes it a lot faster. It has no drawbacks over
	 * {@code dterminant()}.
	 *
	 * @return the determinant of this matrix
	 */
	public double fastDeterminant() {
		if (getWidth() != getHeight()) {
			return 0;
		}
		Matrix tmp = new Matrix();
		tmp.set(this);
		double det = 1;
		for (int i = 0; i < getWidth() - 1; i++) {
			if (tmp.get(i, i) == 0) {
				boolean solved = false;
				for (int k = 0; k < getHeight(); k++) {
					if (tmp.get(k, i) != 0) {
						tmp.swapRows(k, i);
						solved = true;
						det *= -1;
						break;
					}
				}
				if (!solved) {
					return 0;
				}
			}
			for (int j = i + 1; j < getHeight(); j++) {
				if (tmp.get(j, i) != 0) {
					double fac = -tmp.get(i, i) / tmp.get(j, i);
					det *= 1 / fac;
					tmp.multiplyRow(j, fac);
					tmp.addRow(i, j);
				}
			}
		}
		for (int i = 0; i < getHeight(); i++) {
			det *= tmp.get(i, i);
		}
		return det;
	}

	/**
	 * Multiply a row of this matrix with a scalar. Used by
	 * {@code fastDeterminant()}.
	 *
	 * @param row the row to be multiplied
	 * @param x the factor to multiply with
	 */
	public void multiplyRow(int row, double x) {
		for (int i = 0; i < getWidth(); i++) {
			set(row, i, get(row, i) * x);
		}
	}

	/**
	 * Add a row of this matrix to another row of this matrix. Used by
	 * {@code fastDeterminant()}.
	 *
	 * @param a the row to be added
	 * @param b the row to be added to
	 */
	public void addRow(int a, int b) {
		for (int i = 0; i < getWidth(); i++) {
			set(b, i, get(b, i) + get(a, i));
		}
	}

	/**
	 * Swap two rows of this matrix.Used by {@code fastDeterminant()}.
	 *
	 * @param a the first row
	 * @param b the second row
	 */
	public void swapRows(int a, int b) {
		double[] tmp = M[a];
		M[a] = M[b];
		M[b] = tmp;
	}

	/**
	 * Get a minor of this matrix (determinant of a submatrix).
	 *
	 * @param i the row that is missing in the submatrix
	 * @param j the column that is missing in the submatrix
	 * @return the minor (determinant of the submatrix that is obtained by
	 * removing the i-th row and the j-th column from this matrix)
	 */
	public double minor(int i, int j) {
		return getSubmatrix(i, j).fastDeterminant();
	}

	/**
	 * Get a cofactor of this matrix (minor multiplied by either 1 or -1,
	 * depending on which row and which column got removed)
	 *
	 * @param i the row that is missing in the submatrix
	 * @param j the column that is missing in the submatrix
	 * @return the cofactor
	 */
	public double cofactor(int i, int j) {
		return Math.pow(-1, i + j) * minor(i, j);
	}

	/**
	 * Invert this matrix (set it to be its inverse)
	 *
	 * @return itself after being inverted
	 */
	public Matrix invert() {
		set(getInverse(this));
		return this;
	}

	/**
	 * Get the inverse of a matrix
	 *
	 * @param m the matrix to get the inverse from
	 * @return a new {@code Matrix} object representing the inverse of
	 * {@code m}
	 */
	public static Matrix getInverse(Matrix m) {
		if (m.getWidth() == 1) {
			return new Matrix(1, 1, 1 / m.get(0, 0));
		}

		double det = m.fastDeterminant();
		if (det == 0) {
			Debug.log("DET 0!");
			return null;
		}
		Matrix inv = getAdjugate(m);
		inv.multiply(1 / det);
		return inv;
	}

	/**
	 * Adjugate this matrix (set it to be its adjugate)
	 *
	 * @return itself after being adjugated
	 */
	public Matrix adjugate() {
		set(getAdjugate(this));
		return this;
	}

	/**
	 * Get the adjugate of a matrix (the matrix of cofactors)
	 *
	 * @param m the matrix to get the adjugate from
	 * @return a new {@code Matrix} object representing the adjugate of
	 * {@code m}
	 */
	public static Matrix getAdjugate(Matrix m) {
		Matrix adj = new Matrix(m.getHeight(), m.getWidth());
		for (int i = 0; i < m.getHeight(); i++) {
			for (int j = 0; j < m.getWidth(); j++) {
				adj.set(i, j, m.cofactor(i, j));
			}
		}
		adj.transpose();
		return adj;
	}

	/**
	 * Get a {@code String} representation of this matrix, e.g.:<br>
	 * {@code |       0.0        1.0|}<br>
	 * {@code |       5.0        4.2|}
	 *
	 * @return the {@code String} representation of this matrix
	 */
	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < getHeight(); i++) {
			str = str + "|";
			for (int j = 0; j < getWidth(); j++) {
				String num;
				if (get(i, j) < 1000) {
					num = String.format("%10.3f", get(i, j));
				} else {
					num = String.format("%10.2e", get(i, j));
				}
				str = str + num + " ";
			}
			str = str.substring(0, str.length() - 1) + "|\n";
		}
		return str;
	}

	/**
	 * check if the elements in both Matrices are the same (threshold 0.000001)
	 * @param other the Matrix object to compare to
	 * @return true if the elements in both Matrices are the same
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Matrix) {
			Matrix otherM = (Matrix) other;
			if (getWidth() == otherM.getWidth() && getHeight() == otherM.getHeight()) {
				for (int i = 0; i < getHeight(); i++) {
					for (int j = 0; j < getWidth(); j++) {
						if (Math.abs(get(i, j) - otherM.get(i, j)) > 0.000001) {
							return false;
						}
					}
				}
				return true;
			}
		}
		return false;
	}

}
