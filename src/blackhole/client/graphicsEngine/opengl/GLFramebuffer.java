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
package blackhole.client.graphicsEngine.opengl;

import blackhole.client.graphicsEngine.GameTexture;
import blackhole.client.graphicsEngine.AbstractFramebuffer;
import blackhole.utils.Debug;
import com.jogamp.opengl.FBObject;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;

/**
 * Implementation of {@link AbstractFramebuffer} for use with {@link GLDrawer}
 *
 * @author fabian.baer2
 */
public class GLFramebuffer implements AbstractFramebuffer {

	/**
	 * The OpenGL framebuffer object
	 */
	private FBObject fbo;
	
	/**
	 * The image connected to this framebuffer
	 */
	private Texture texture;
	
	/**
	 * the texture created from this framebuffer
	 */
	private GLGameTexture gameTexture;

	/**
	 * Creates a new {@code J2DFramebuffer} with the specified dimensions
	 * @param gl the opengl object
	 * @param width the width
	 * @param height the height
	 */
	protected GLFramebuffer(GL gl, int width, int height) {
		fbo = new FBObject();
		try {
			fbo.init(gl, width, height, 0);

			/*FBObject.TextureAttachment texAtt
                    = FBObject.createColorTextureAttachment(gl, true, width, height);/*,
                            GL.GL_LINEAR, GL.GL_LINEAR, GL.GL_CLAMP_TO_EDGE,
                            GL.GL_CLAMP_TO_EDGE);
            fbo.attachColorbuffer(gl, 0, texAtt);*/
			FBObject.TextureAttachment texAtt = fbo.attachTexture2D(gl, 0, true);
			texture = new Texture(texAtt.getName(),
					GL.GL_TEXTURE_2D, width, height, width, height, false);
			gameTexture = new GLGameTexture(texture, "FBO: " + width + "x" + height);
		} catch (GLException e) {
			Debug.logError("failed to create FBO");
		}
	}

	/**
	 * Returns the opengl framebuffer object
	 * @return the opengl framebuffer object
	 */
	protected FBObject getFBObject() {
		return fbo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return fbo.getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return fbo.getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Texture getData() {
		return texture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLGameTexture toTexture() {
		return gameTexture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReady() {
		return fbo.isInitialized();
	}
}
