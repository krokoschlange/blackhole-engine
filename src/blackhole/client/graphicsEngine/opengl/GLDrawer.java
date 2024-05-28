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

import blackhole.client.graphicsEngine.GameDrawable;
import blackhole.client.graphicsEngine.AbstractFramebuffer;
import blackhole.client.graphicsEngine.AbstractDrawer;
import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Implementation of {@link AbstractDrawer} that uses OpenGL to
 * draw.
 * @author fabian.baer2
 */
public class GLDrawer implements AbstractDrawer {

	/**
	 * the associated {@link GLGraphicsBackend}
	 */
	private GLGraphicsBackend backend;

	/**
	 * The opengl object used for drawing
	 */
	private GL gl;

	/**
	 * The framebuffer currently used for drawing or null if drawing to the
	 * window directly
	 */
	private GLFramebuffer drawFramebuffer;

	
	/**
	 * The {@link GLStringDrawer} used to draw text
	 */
	private GLStringDrawer stringDrawer;
	
	/**
	 * Creates a new {@code GLDrawer} associated with the given
	 * {@link GLGraphicsBackend}
	 * @param glgb the graphics backend
	 */
	public GLDrawer(GLGraphicsBackend glgb) {
		backend = glgb;
		//textRenderers = new HashMap<>();
	}

	/**
	 * Returns the currently used opengl object
	 * @return the currently used opengl object
	 */
	protected GL getGL() {
		return gl;
	}

	/**
	 * Sets the opengl object to use
	 * @param g the new opengl object
	 */
	protected void setGL(GL g) {
		gl = g;
	}
	
	/**
	 * Returns the {@link GLGraphicsBackend} associated with the drawer
	 * @return the {@link GLGraphicsBackend} associated with the drawer
	 */
	protected GLGraphicsBackend getBackend() {
		return backend;
	}

	/**
	 * Initializes the text drawer. Called when the window is created.
	 */
	protected void init() {
		String stringDrawerSetting = Settings.getProperty("gl_string_drawer");
		if (stringDrawerSetting != null
				&& stringDrawerSetting.equals("curve")) {
			GLCurveStringDrawer curveStringDrawer = new GLCurveStringDrawer();
			curveStringDrawer.init(gl);
			stringDrawer = curveStringDrawer;
		} else {
			stringDrawer = new GLAWTStringDrawer();
		}
	}

	/**
	 * called when the window is disposed
	 */
	protected void dispose() {

	}

	/**
	 * transforms the currently used opengl object according to the
	 * given parameters
	 * @param relPos the position relative to the camera
	 * @param rot the rotation
	 * @param camRot the camera's rotation
	 * @param camScale the scaling of the camera relative to the
	 * {@link HandlerPanel} that will be drawn into
	 * @param panelOffset the position of the panel
	 * @param panelSize the size of the panel
	 * @param windowSize the size of the game window
	 */
	private void transform(Vector relPos, double rot, double camRot, Vector camScale,
			Vector panelOffset, Vector panelSize, Vector windowSize) {
		GL2 gl2 = gl.getGL2();
		gl2.glLoadIdentity();

		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl2.glScaled(2. / windowSize.x(), 2. / windowSize.y(), 1);
		gl2.glTranslated(-windowSize.x() / 2, windowSize.y() / 2, 0);
		gl2.glTranslated(panelSize.x() / 2, -panelSize.y() / 2, 0);
		gl2.glTranslated(panelOffset.x(), -panelOffset.y(), 0);
		gl2.glScaled(camScale.x(), camScale.y(), 1);
		gl2.glRotated(Math.toDegrees(-camRot), 0, 0, 1);
		gl2.glTranslated(relPos.x(), relPos.y(), 0);
		gl2.glRotated(Math.toDegrees(rot), 0, 0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getDrawbufferSize() {
		return getDrawbufferSize(drawFramebuffer);
	}

	/**
	 * Returns the dimensions of the given framebuffer or the window dimensions
	 * if given {@code null}
	 * @param fb the framebuffer or null if the window's dimensions should be
	 * returned
	 * @return the dimensions of the given framebuffer
	 */
	protected Vector getDrawbufferSize(AbstractFramebuffer fb) {
		if (fb == null) {
			int w = backend.getWindow().getWidth();
			int h = backend.getWindow().getHeight();
			return new Vector(w, h);
		} else {
			return new Vector(fb.getWidth(), fb.getHeight());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawTexture(GameDrawable drawable, double alpha, Vector pos, double rot,
			Vector scale, Camera cam, HandlerPanel panel,
			double gameScale) {
		if (drawable == null || !(drawable instanceof GLGameDrawable)) {
			return;
		}

		GLGameDrawable glGameDrawable = (GLGameDrawable) drawable;

		GL2 gl2 = gl.getGL2();

		Vector panelSize = null;
		Vector panelPos = null;

		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		if (panel != null) {
			panelSize = panel.size;
			panelPos = panel.position;
		} else {
			panelSize = fbSize;
			panelPos = new Vector();
		}

		Vector imagePos = Vector.add(pos,
				Vector.rotate(drawable.getOffset(), rot)
		);
		Vector relativePosition = (Vector) Vector.subtract(imagePos,
				cam.getPosition()
		).multiply(gameScale);

		Vector cameraScale = new Vector(panelSize.x() * 1.0 / cam.getWidth(),
				panelSize.y() * 1.0 / cam.getHeight()
		);

		float scaledWidth = (float) (drawable.getWidth() * scale.x());
		float scaledHeight = (float) (drawable.getHeight() * scale.y());

		double radius = Math.sqrt(scaledWidth * scaledWidth
				+ scaledHeight * scaledHeight) / 2;

		if (relativePosition.magnitude() - radius
				> cam.getSize().magnitude() / 2 * gameScale) {
			return;
		}

		/*Debug.log("SCISSOR: " + (int) panelPos.x() + ", " +
				(int) (fbSize.y() - panelPos.y() - panelSize.y()) + ", " +
		(int) panelSize.x() + ", " + (int) panelSize.y());
		 */
		gl2.glScissor((int) panelPos.x(), (int) (fbSize.y() - panelPos.y() - panelSize.y()),
				(int) panelSize.x(), (int) panelSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		transform(relativePosition, rot + drawable.getRotOffset(), cam.getRotation(), cameraScale,
				panelPos, panelSize, fbSize);

		Texture texture = glGameDrawable.getDrawData();

		texture.enable(gl2);
		texture.bind(gl2);
		gl2.glEnable(GL.GL_SCISSOR_TEST);
		gl2.glDisable(GL.GL_DEPTH_TEST);
		gl2.glDisable(GL.GL_STENCIL_TEST);
		gl2.glEnable(GL.GL_BLEND);
		gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);

		gl2.glColor4f(1, 1, 1, (float) alpha);

		gl2.glBegin(GL2.GL_QUADS);

		gl2.glTexCoord2f(0.0f, 0.0f);
		gl2.glVertex2f(-0.5f * scaledWidth, -0.5f * scaledHeight);

		gl2.glTexCoord2f(1.0f, 0.0f);
		gl2.glVertex2f(0.5f * scaledWidth, -0.5f * scaledHeight);

		gl2.glTexCoord2f(1.0f, 1.0f);
		gl2.glVertex2f(0.5f * scaledWidth, 0.5f * scaledHeight);

		gl2.glTexCoord2f(0.0f, 1.0f);
		gl2.glVertex2f(-0.5f * scaledWidth, 0.5f * scaledHeight);

		gl2.glEnd();

		texture.disable(gl2);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawTexture(GameDrawable drawable, double alpha, double x, double y,
			double x2, double y2) {
		if (drawable == null || !(drawable instanceof GLGameDrawable)) {
			return;
		}

		GLGameDrawable glGameDrawable = (GLGameDrawable) drawable;

		GL2 gl2 = gl.getGL2();

		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		gl2.glLoadIdentity();
		gl2.glScaled(2. / fbSize.x(), 2. / fbSize.y(), 1);

		Texture texture = glGameDrawable.getDrawData();

		gl2.glEnable(GL.GL_SCISSOR_TEST);
		gl2.glDisable(GL.GL_DEPTH_TEST);
		gl2.glDisable(GL.GL_STENCIL_TEST);
		gl2.glEnable(GL.GL_BLEND);
		gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		texture.enable(gl2);
		texture.bind(gl2);

		gl2.glColor4f(1, 1, 1, (float) alpha);

		gl2.glBegin(GL2.GL_QUADS);

		gl2.glTexCoord2f(0.0f, 0.0f);
		gl2.glVertex2f((float) x, (float) -y2);

		gl2.glTexCoord2f(1.0f, 0.0f);
		gl2.glVertex2f((float) x2, (float) -y2);

		gl2.glTexCoord2f(1.0f, 1.0f);
		gl2.glVertex2f((float) x2, (float) -y);

		gl2.glTexCoord2f(0.0f, 1.0f);
		gl2.glVertex2f((float) x, (float) -y);

		gl2.glEnd();

		texture.disable(gl2);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(double r, double g, double b, double alpha) {
		gl.glClearColor((float) r, (float) g, (float) b, (float) alpha);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(String string, Vector pos, double rot, Vector scale,
			Camera cam, HandlerPanel panel, double gameScale, Font font,
			double r, double g, double b, double alpha, boolean blend) {
		stringDrawer.drawString(this, string, pos, rot, scale, cam, panel,
				gameScale, font, r, g, b, alpha, blend);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(String string, Vector pos, Font font, double r,
			double g, double b, double alpha, boolean blend) {
		stringDrawer.drawString(this, string, pos, font, r, g, b, alpha, blend);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDrawBuffer(AbstractFramebuffer fb) {

		if (fb instanceof GLFramebuffer) {
			GLFramebuffer glfb = (GLFramebuffer) fb;
			if (glfb.getFBObject().isInitialized()) {
				glfb.getFBObject().bind(gl);
				drawFramebuffer = glfb;
			}
		} else if (fb == null && drawFramebuffer != null) {
			drawFramebuffer.getFBObject().unbind(gl);
			drawFramebuffer = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLFramebuffer getDrawBuffer() {
		return drawFramebuffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLFramebuffer createFramebuffer(int width, int height) {
		return new GLFramebuffer(gl, width, height);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object destroyFramebuffer(AbstractFramebuffer fb, boolean destroyData) {
		if (fb instanceof GLFramebuffer) {
			GLFramebuffer glfb = (GLFramebuffer) fb;

			glfb.getFBObject().destroy(gl);

			if (destroyData) {
				Texture tex = (Texture) glfb.getData();
				if (tex != null) {
					tex.destroy(gl);
				}
				return null;
			}
			return glfb.getData();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyFramebuffer(AbstractFramebuffer fb) {
		destroyFramebuffer(fb, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawPolygon(Vector pos, double rot, double r, double g,
			double b, double alpha, Camera cam, HandlerPanel panel,
			double gameScale, boolean blend, Vector... p) {
		GL2 gl2 = gl.getGL2();

		Vector panelSize = null;
		Vector panelPos = null;

		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		if (panel != null) {
			panelSize = panel.size;
			panelPos = panel.position;
		} else {
			panelSize = fbSize;
			panelPos = new Vector();
		}

		Vector relativePosition = (Vector) Vector.subtract(pos,
				cam.getPosition()
		).multiply(gameScale);

		Vector cameraScale = new Vector(panelSize.x() * 1.0 / cam.getWidth(),
				panelSize.y() * 1.0 / cam.getHeight()
		);

		gl2.glScissor((int) panelPos.x(), (int) (fbSize.y() - panelPos.y() - panelSize.y()),
				(int) panelSize.x(), (int) panelSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}

		transform(relativePosition, rot, cam.getRotation(), cameraScale,
				panelPos, panelSize, fbSize);
		gl2.glScaled(gameScale, gameScale, 1);

		gl2.glColor4f((float) r, (float) g, (float) b, (float) alpha);

		gl2.glBegin(GL2.GL_POLYGON);

		for (int i = 0; i < p.length; i++) {
			gl2.glVertex2f((float) p[i].x(), (float) p[i].y());
		}

		gl2.glEnd();

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawPolygon(double r, double g, double b, double a, boolean blend, Vector... p) {
		GL2 gl2 = gl.getGL2();

		if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}

		transform(new Vector(), 0, 0, new Vector(1, 1), new Vector(), getDrawbufferSize(drawFramebuffer), getDrawbufferSize(drawFramebuffer));

		gl2.glColor4f((float) r, (float) g, (float) b, (float) a);

		gl2.glBegin(GL2.GL_POLYGON);

		for (int i = 0; i < p.length; i++) {
			gl2.glVertex2f((float) p[i].x(), (float) -p[i].y());
		}

		gl2.glEnd();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Rectangle2D getTextExtents(String text, Font font) {
		/*if (!(font instanceof GLGameFont)) {
			return null;
		}
		GLGameFont glfont = (GLGameFont) font;
		
		AABBox aabb = glfont.getFont().getMetricBounds(text, glfont.getSize());
		
		return new Rectangle2D.Double(aabb.getMinX(), aabb.getMinY(),
				aabb.getWidth(), aabb.getHeight());*/
		return stringDrawer.getTextExtents(this, text, font);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCharacterWidth(char c, Font font) {
		return stringDrawer.getCharacterWidth(this, c, font);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override	
	public double getDescent(Font f) {
		return stringDrawer.getDescent(this, f);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLineHeight(Font f) {
		return stringDrawer.getLineHeight(this, f);
	}
}
