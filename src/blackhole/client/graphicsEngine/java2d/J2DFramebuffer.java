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

import blackhole.client.game.ClientManager;
import blackhole.client.graphicsEngine.AbstractFramebuffer;
import blackhole.client.graphicsEngine.GameTexture;
import blackhole.utils.Debug;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.VolatileImage;

/**
 * Implementation of {@link AbstractFramebuffer} for use with {@link J2DDrawer}
 * @author fabian.baer2
 */
public class J2DFramebuffer implements AbstractFramebuffer {

	/**
	 * the buffer data
	 */
	private VolatileImage data;
	
	/**
	 * the texture created from this framebuffer
	 */
	private J2DGameTexture texture;

	/**
	 * whether the framebuffer is ready for use
	 */
	private boolean ready;

	/**
	 * Creates a new {@code J2DFramebuffer} with the specified dimensions
	 * @param w the width
	 * @param h the height
	 */
	protected J2DFramebuffer(int w, int h) {
		try {
			GraphicsConfiguration gfxConf
					= ((J2DGameWindow) ClientManager.getInstance().
							getGraphicsBackend().getWindow()).
							getGraphicsConfiguration();
			data = gfxConf.createCompatibleVolatileImage(w, h, Transparency.TRANSLUCENT);
			texture = new J2DGameTexture(data, "FBO: " + w + "x" + h);

			ready = true;
		} catch (Exception e) {
			Debug.logError("failed to create FBO");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return data.getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return data.getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getData() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameTexture toTexture() {
		return texture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReady() {
		return ready;
	}

	/**
	 * sets the framebuffer's state to be not ready
	 */
	protected void disable() {
		ready = false;
	}
}
