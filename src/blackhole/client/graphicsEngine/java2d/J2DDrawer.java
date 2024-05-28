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

import blackhole.client.graphicsEngine.AbstractDrawer;
import blackhole.client.graphicsEngine.AbstractFramebuffer;
import blackhole.client.graphicsEngine.GameDrawable;
import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.utils.Vector;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.VolatileImage;

/**
 * Implementation of {@link AbstractDrawer} that uses {@link Graphics2D} to
 * draw.
 * @author fabian.baer2
 */
public class J2DDrawer implements AbstractDrawer {

	/**
	 * the associated {@link J2DGraphicsBackend}
	 */
	private J2DGraphicsBackend backend;

	/**
	 * the {@link Graphics2D} object used to draw in the window
	 */
	private Graphics2D windowGraphics;

	/**
	 * the currently used {@link Graphics2D} object
	 */
	private Graphics2D graphics;

	/**
	 * the current {@link J2DFramebuffer} that will be drawn into, or null to
	 * draw into the game window
	 */
	private J2DFramebuffer drawFramebuffer;

	/**
	 * Creates a new {@code J2DDrawer} associated with the given
	 * {@link J2DGraphicsBackend}
	 * @param j2dgb the graphics backend
	 */
	public J2DDrawer(J2DGraphicsBackend j2dgb) {
		backend = j2dgb;
	}

	/**
	 * Sets the {@link Graphics2D} object that will be used to draw into the
	 * game window
	 * @param g2d the {@link Graphics2D} object to be used
	 */
	protected void setWindowGraphics(Graphics2D g2d) {
		windowGraphics = g2d;
	}

	/**
	 * transforms the currently used {@link Graphics2D} object according to the
	 * given parameters
	 * @param relPos the position relative to the camera
	 * @param rot the rotation
	 * @param camRot the camera's rotation
	 * @param camScale the scaling of the camera relative to the
	 * {@link HandlerPanel} that will be drawn into
	 * @param panelOffset the position of the panel
	 * @param panelSize the size of the panel
	 */
	private void transform(Vector relPos, double rot, double camRot, Vector camScale,
			Vector panelOffset, Vector panelSize) {

		graphics.translate(panelSize.x() / 2, panelSize.y() / 2);
		graphics.translate(panelOffset.x(), panelOffset.y());
		graphics.scale(camScale.x(), camScale.y());
		graphics.rotate(camRot);
		graphics.translate(relPos.x(), -relPos.y());
		graphics.rotate(-rot);
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
			J2DGameWindow win = backend.getWindow();
			return new Vector(win.getWidth(), win.getHeight());
		} else {
			return new Vector(drawFramebuffer.getWidth(),
					drawFramebuffer.getHeight());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawTexture(GameDrawable drawable, double alpha, Vector pos, double rot,
			Vector scale, Camera cam, HandlerPanel panel,
			double gameScale) {
		if (drawable == null || !(drawable instanceof J2DGameDrawable)) {
			return;
		}

		J2DGameDrawable j2dGameDrawable = (J2DGameDrawable) drawable;

		Vector panelSize;
		Vector panelPos;

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

		double radius = Math.sqrt(scaledWidth * scaledWidth + scaledHeight * scaledHeight) / 2;

		if (relativePosition.magnitude() - radius
				> cam.getSize().magnitude() / 2 * gameScale) {
			return;
		}

		Image img = j2dGameDrawable.getDrawData();

		graphics.setClip((int) panelPos.x(), (int) panelPos.y(), (int) panelSize.x(), (int) panelSize.y());

		AffineTransform origTrans = graphics.getTransform();
		transform(relativePosition, rot + drawable.getRotOffset(), cam.getRotation(), cameraScale,
				panelPos, panelSize);

		Composite comp = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
		
		graphics.drawImage(img, (int) (-scaledWidth / 2), (int) (-scaledHeight / 2),
				(int) scaledWidth, (int) scaledHeight, null);
		
		graphics.setComposite(comp);
		graphics.setTransform(origTrans);

		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawTexture(GameDrawable drawable, double alpha, double x, double y,
			double x2, double y2) {
		if (drawable == null || !(drawable instanceof J2DGameDrawable)) {
			return;
		}

		J2DGameDrawable j2dGameDrawable = (J2DGameDrawable) drawable;

		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		Image img = j2dGameDrawable.getDrawData();

		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());

		AffineTransform origTrans = graphics.getTransform();

		graphics.translate(fbSize.x() / 2, fbSize.y() / 2);

		Composite comp = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
		
		graphics.drawImage(img, (int) x, (int) y, (int) (x2 - x),
				(int) (y2 - y), null);
		
		graphics.setComposite(comp);
		graphics.setTransform(origTrans);

		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(String string, Vector pos, double rot, Vector scale,
			Camera cam, HandlerPanel panel, double gameScale, Font font,
			double r, double g, double b, double alpha, boolean blend) {
		
		Vector panelSize;
		Vector panelPos;

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

		graphics.setFont(font);
		graphics.setColor(new Color((float) r, (float) g, (float) b, (float) alpha));

		Composite comp = graphics.getComposite();
		if (!blend) {
			graphics.setComposite(AlphaComposite.Src);
		}

		graphics.setClip((int) panelPos.x(), (int) panelPos.y(), (int) panelSize.x(), (int) panelSize.y());
		AffineTransform origTrans = graphics.getTransform();
		transform(relativePosition, rot, cam.getRotation(), cameraScale,
				panelPos, panelSize);
		graphics.scale(scale.x(), scale.y());

		graphics.drawString(string, 0, 0);

		graphics.setTransform(origTrans);
		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());
		if (!blend) {
			graphics.setComposite(comp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(String string, Vector pos, Font font, double r,
			double g, double b, double alpha, boolean blend) {
		
		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		graphics.setFont(font);
		graphics.setColor(new Color((float) r, (float) g, (float) b, (float) alpha));

		Composite comp = graphics.getComposite();
		if (!blend) {
			graphics.setComposite(AlphaComposite.Src);
		}

		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());

		graphics.drawString(string, (int) fbSize.x() / 2 + (int) pos.x(),
				(int) fbSize.y() / 2 + (int) pos.y());

		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());
		if (!blend) {
			graphics.setComposite(comp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(double r, double g, double b, double alpha) {
		graphics.setBackground(new Color((float) r, (float) g, (float) b, (float) alpha));
		Vector size = getDrawbufferSize(drawFramebuffer);
		graphics.clearRect(0, 0, (int) size.x(), (int) size.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractFramebuffer createFramebuffer(int width, int height) {
		return new J2DFramebuffer(width, height);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object destroyFramebuffer(AbstractFramebuffer fb,
			boolean destroyData) {
		if (fb instanceof J2DFramebuffer) {
			J2DFramebuffer j2dfb = (J2DFramebuffer) fb;

			j2dfb.disable();
			if (destroyData) {
				VolatileImage img = (VolatileImage) j2dfb.getData();
				if (img != null) {
					img.flush();
				}
			}
			return j2dfb.getData();
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
	public void setDrawBuffer(AbstractFramebuffer fb) {
		if (fb instanceof J2DFramebuffer) {
			if (drawFramebuffer != null) {
				graphics.dispose();
				drawFramebuffer = null;
			}
			J2DFramebuffer j2dfb = (J2DFramebuffer) fb;
			drawFramebuffer = j2dfb;
			VolatileImage data = (VolatileImage) j2dfb.getData();
			graphics = data.createGraphics();
		} else if (fb == null) {
			if (drawFramebuffer != null) {
				graphics.dispose();
				drawFramebuffer = null;
			}
			graphics = windowGraphics;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DFramebuffer getDrawBuffer() {
		return drawFramebuffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawPolygon(Vector pos, double rot, double r, double g,
			double b, double alpha, Camera cam, HandlerPanel panel,
			double gameScale, boolean blend, Vector... p) {
		Vector panelSize;
		Vector panelPos;

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

		Path2D.Double pol = new Path2D.Double();
		pol.moveTo(p[0].x(), -p[0].y());
		for (int i = 1; i < p.length; i++) {
			pol.lineTo(p[i].x(), -p[i].y());
		}
		pol.closePath();

		graphics.setColor(new Color((float) r, (float) g, (float) b, (float) alpha));

		Composite comp = graphics.getComposite();
		if (!blend) {
			graphics.setComposite(AlphaComposite.Src);
		}

		graphics.setClip((int) panelPos.x(), (int) panelPos.y(), (int) panelSize.x(), (int) panelSize.y());
		AffineTransform origTrans = graphics.getTransform();
		transform(relativePosition, rot, cam.getRotation(), cameraScale,
				panelPos, panelSize);
		graphics.scale(gameScale, gameScale);

		graphics.fill(pol);
		
		graphics.setTransform(origTrans);
		graphics.setClip(0, 0, (int) fbSize.x(), (int) fbSize.y());
		if (!blend) {
			graphics.setComposite(comp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawPolygon(double r, double g, double b, double a, boolean blend, Vector... p) {
		int[] xPos = new int[p.length];
		int[] yPos = new int[p.length];

		Vector fbSize = getDrawbufferSize(drawFramebuffer);

		for (int i = 0; i < p.length; i++) {
			xPos[i] = (int) (p[i].x() + fbSize.x() / 2);
			yPos[i] = (int) (p[i].y() + fbSize.y() / 2);
		}

		Polygon pol = new Polygon(xPos, yPos, p.length);

		graphics.setColor(new Color((float) r, (float) g, (float) b, (float) a));

		Composite comp = graphics.getComposite();
		if (!blend) {
			graphics.setComposite(AlphaComposite.Src);
		}

		graphics.fill(pol);

		if (!blend) {
			graphics.setComposite(comp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Rectangle2D getTextExtents(String text, Font font) {
		FontMetrics metrics = graphics.getFontMetrics();
		return new Rectangle.Float(0, metrics.getDescent(), metrics.stringWidth(text),
				metrics.getLeading() + metrics.getAscent() + metrics.getDescent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCharacterWidth(char c, Font font) {
		graphics.setFont(font);
		return graphics.getFontMetrics().charWidth(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDescent(Font f) {
		graphics.setFont(f);
		return graphics.getFontMetrics().getDescent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLineHeight(Font f) {
		graphics.setFont(f);
		return graphics.getFontMetrics().getHeight();
	}
	
	

}
