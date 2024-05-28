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
package blackhole.client.graphicsEngine.java2d;

import blackhole.client.graphicsEngine.GameTexture;
import blackhole.networkData.updateData.TextureUpdate;
import blackhole.utils.Debug;
import java.awt.Image;

/**
 * Implementation of {@link GameTexture} for use with {@link J2DDrawer}
 * @author fabian.baer2
 */
public class J2DGameTexture extends GameTexture implements J2DGameDrawable {

	/**
	 * the image data
	 */
	private Image image;
	
	/**
	 * Creates a new {@code J2DGameTexture} with the specified data and path
	 * @param tex the image data
	 * @param path the path (used as name, {@link GameTexture#getName()})
	 */
	public J2DGameTexture(Image tex, String path) {
		if (tex == null) {
			Debug.log("null texture created: " + path);
		}
        setData(tex);
        setName(path);
	}
	
	/**
	 * Creates a new {@code J2DGameTexture} from the given {@link TextureUpdate}
	 * @param backend the {@link J2DGraphicsBackend}
	 * @param update the update
	 */
	public J2DGameTexture(J2DGraphicsBackend backend, TextureUpdate update) {
		this(backend.getImage(update.name), update.name);
        setOffset(update.offset);
        setRotOffset(update.rotationOffset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setData(Object d) {
		if (d instanceof Image) {
			image = (Image) d;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getData() {
		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return image.getWidth(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return image.getHeight(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getDrawData() {
		return image;
	}
}
