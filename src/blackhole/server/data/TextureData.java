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
package blackhole.server.data;

import blackhole.networkData.updateData.TextureUpdate;
import blackhole.utils.Vector;

/**
 * Describes a texture on the server as there is no need for image data
 *
 * @author fabian
 */
public class TextureData implements Cloneable {

	/**
	 * The name (usually path) of the texture
	 */
	private String name;

	/**
	 * offset of the texture, in game coordinates
	 */
	private Vector offset;

	/**
	 * rotation offset of the texture
	 */
	private double rotationOffset;

	/**
	 * Creates a new {@code TextureData} with the given name/path
	 * @param n the name or path
	 */
	public TextureData(String n) {
		name = n;
		offset = new Vector();
		rotationOffset = 0;
	}

	/**
	 * Sets the texure's offset
	 * @param off the new offset
	 */
	public void setOffset(Vector off) {
		offset = off;
	}

	/**
	 * Returns the texture's offset
	 * @return the texture's offset
	 */
	public Vector getOffset() {
		return offset;
	}

	/**
	 * Sets the texture's rotation offset
	 * @param rotOff the new rotation offset
	 */
	public void setRotationOffset(double rotOff) {
		rotationOffset = rotOff;
	}

	/**
	 * Returns the texture's rotation offset
	 * @return the texture's rotation offset
	 */
	public double getRotationOffset() {
		return rotationOffset;
	}

	/**
	 * Returns a {@link TextureUpdate} describing the texture
	 * @return a {@link TextureUpdate} describing the texture
	 */
	public TextureUpdate getUpdateData() {
		TextureUpdate update = new TextureUpdate();
		update.name = name;
		update.offset = offset;
		update.rotationOffset = rotationOffset;

		return update;
	}
}
