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
package blackhole.client.graphicsEngine;

import blackhole.utils.Vector;

/**
 * Describes a 2D graphic that can be rendered
 *
 * @author fabian
 */
public abstract class GameDrawable {

	/**
	 * An offset, in game coordinates
	 */
	private Vector offset;

	/**
	 * A rotation offset
	 */
	private double rotOffset;

	/**
	 * an identifier for the {@link GameDrawable}, usually contains the path to
	 * the image file (if the drawable is a single image)
	 */
	private String name;

	/**
	 * Creates a new {@code GameDrawable}
	 */
	public GameDrawable() {
		offset = new Vector();
		name = "";
	}

	public void setOffset(Vector off) {
		if (off != null) {
			offset = off;
		}

	}

	public Vector getOffset() {
		return offset;
	}

	public void setRotOffset(double rOff) {
		rotOffset = rOff;
	}

	public double getRotOffset() {
		return rotOffset;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	/**
	 * Returns the width of the {@code GameDrawable}, in pixels
	 * @return the width of the {@code GameDrawable}
	 */
	public abstract int getWidth();

	/**
	 * Returns the height of the {@code GameDrawable}, in pixels
	 * @return the height of the {@code GameDrawable}
	 */
	public abstract int getHeight();

	/**
	 * Returns the data that should be drawn at that moment
	 * @return the data that should be drawn at that moment
	 */
	public abstract Object getDrawData();
}
