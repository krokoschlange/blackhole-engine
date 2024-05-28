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

import blackhole.client.game.ClientObjectHandler;
import blackhole.utils.Vector;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Interface to draw 2D graphics into the window or a framebuffer
 * @author fabian
 */
public interface AbstractDrawer {

	/**
	 * Draws a 2D image
	 * @param drawable the image
	 * @param alpha extra alpha
	 * @param pos the position in game coordinates
	 * @param rot the rotation
	 * @param scale the scale of the image
	 * @param cam the camera ({@link ClientObjectHandler#getCamera()})
	 * @param panel the panel into which to draw ({@link ClientObjectHandler#getPanel()})
	 * @param gameScale the scaling of the game world ({@link ClientObjectHandler#getScale()})
	 */
	public void drawTexture(GameDrawable drawable, double alpha, Vector pos, double rot,
			Vector scale, Camera cam, HandlerPanel panel,
			double gameScale);

	/**
	 * Draws an image in the specified rectangle
	 * @param drawable the image
	 * @param alpha extra alpha
	 * @param x the x coordinate of a left corner (window coordinates)
	 * @param y the y coordinate of an upper corner (window coordinates)
	 * @param x2 the x coordinate of a right corner (window coordinates)
	 * @param y2 the x coordinate of a lower corner (window coordinates)
	 */
	public void drawTexture(GameDrawable drawable, double alpha, double x, double y,
			double x2, double y2);

	/**
	 * Draws text in the game world
	 * @param string the text to draw
	 * @param pos the position in game coordinates
	 * @param rot the rotation
	 * @param scale the scale
	 * @param cam the camera ({@link ClientObjectHandler#getCamera()})
	 * @param panel the panel into which to draw ({@link ClientObjectHandler#getPanel()})
	 * @param gameScale the scaling of the game world ({@link ClientObjectHandler#getScale()})
	 * @param font the font
	 * @param r the red component of the text color
	 * @param g the green component of the text color
	 * @param b the blue component of the text color
	 * @param alpha the alpha component of the text color
	 * @param blend whether to use alpha blending
	 */
	public void drawString(String string, Vector pos, double rot, Vector scale,
			Camera cam, HandlerPanel panel, double gameScale, Font font,
			double r, double g, double b, double alpha, boolean blend);

	/**
	 * Draws a text at the given position
	 * @param string the text to draw
	 * @param pos the position in window coordinates
	 * @param font the font
	 * @param r the red component of the text color
	 * @param g the green component of the text color
	 * @param b the blue component of the text color
	 * @param alpha the alpha component of the text color
	 * @param blend whether to use alpha blending
	 */
	public void drawString(String string, Vector pos, Font font, double r,
			double g, double b, double alpha, boolean blend);

	/**
	 * Draws a polygon in the game world
	 * @param pos the position in game coordinates
	 * @param rot the rotation
	 * @param r the red component of the polygon color
	 * @param g the green component of the polygon color
	 * @param b the blue component of the polygon color
	 * @param alpha the alpha component of the polygon color
	 * @param cam cam the camera ({@link ClientObjectHandler#getCamera()})
	 * @param panel the panel into which to draw ({@link ClientObjectHandler#getPanel()})
	 * @param gameScale the scaling of the game world ({@link ClientObjectHandler#getScale()})
	 * @param blend whether to use alpha blending
	 * @param p an array of {@link Vector} describing the polygon
	 */
	public void drawPolygon(Vector pos, double rot, double r, double g,
			double b, double alpha, Camera cam, HandlerPanel panel,
			double gameScale, boolean blend, Vector... p);
	
	/**
	 * Draws a polygon
	 * @param r the red component of the polygon color
	 * @param g the green component of the polygon color
	 * @param b the blue component of the polygon color
	 * @param a the alpha component of the polygon color
	 * @param blend whether to use alpha blending
	 * @param p an array of {@link Vector} describing the polygon in window coordinates
	 */
	public void drawPolygon(double r, double g, double b, double a,
			boolean blend, Vector... p);

	/**
	 * Clears the selected buffer / the window
	 * @param r the red component of the clear color
	 * @param g the green component of the clear color
	 * @param b the blue component of the clear color
	 * @param alpha the alpha component of the clear color (only useful when clearing
	 * a framebuffer)
	 */
	public void clear(double r, double g, double b, double alpha);

	/**
	 * Creates a new Framebuffer with the specified dimensions
	 * @param width the width
	 * @param height the height
	 * @return the new framebuffer
	 */
	public AbstractFramebuffer createFramebuffer(int width, int height);

	/**
	 * Destroys the given framebuffer
	 * @param fb the framebuffer to destroy
	 * @param destroyData whether to destroy the data in the buffer
	 * @return the data if not destroyed, otherwise {@code null}
	 */
	public Object destroyFramebuffer(AbstractFramebuffer fb,
			boolean destroyData);

	/**
	 * Destroys the given framebuffer
	 * @param fb the framebuffer to destroy
	 */
	public void destroyFramebuffer(AbstractFramebuffer fb);

	/**
	 * Sets the drawbuffer to the specified framebuffer or the window if
	 * {@code fb == null}
	 * @param fb the framebuffer to use for future drawing operations
	 */
	public void setDrawBuffer(AbstractFramebuffer fb);

	/**
	 * Returns the current buffer used for drawing or null if the window is used
	 * @return the current buffer used for drawing
	 */
	public AbstractFramebuffer getDrawBuffer();

	/**
	 * Returns the current drawbuffer size
	 * @return the current drawbuffer size
	 */
	public Vector getDrawbufferSize();

	/**
	 * Returns the size of the given text in the given font.
	 * The x-coordinate of the returned {@link Rectangle2D} is always 0
	 * The y-coordinate of the returned {@link Rectangle2D} is the descend (positive)
	 * The width of the returned {@link Rectangle2D} is the width of the text
	 * The height of the returned {@link Rectangle2D} is whole height of the text
	 * @param text the text
	 * @param font the font of the text
	 * @return a {@link Rectangle2D} describing the text's dimensions
	 */
	public Rectangle2D getTextExtents(String text, Font font);
	
	/**
	 * Returns the on-screen width of the specified character in the given font.
	 * @param c the character
	 * @param font the font
	 * @return the width of the character
	 */
	public double getCharacterWidth(char c, Font font);
	
	/**
	 * Returns the maximum descend of the given font
	 * @param f the font
	 * @return the maximum descend
	 */
	public double getDescent(Font f);
	
	/**
	 * Returns the standard line height of text in the given font.
	 * @param f the font
	 * @return the standard line height of text in the given font
	 */
	public double getLineHeight(Font f);
}
