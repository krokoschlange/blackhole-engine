/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.client.graphicsEngine.opengl;

import blackhole.client.game.ClientObjectHandler;
import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.utils.Vector;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Draws Strings. This is separated from {@link GLDrawer} to allow for different
 * String drawing methods to be used.
 * @author fabian
 */
public interface GLStringDrawer {
	/**
	 * Draws text in the game world
	 * @param drawer the drawer to be used
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
	public void drawString(GLDrawer drawer, String string, Vector pos, double rot, Vector scale,
			Camera cam, HandlerPanel panel, double gameScale, Font font,
			double r, double g, double b, double alpha, boolean blend);
	
	/**
	 * Draws a text at the given position
	 * @param drawer the drawer to be used
	 * @param string the text to draw
	 * @param pos the position in window coordinates
	 * @param font the font
	 * @param r the red component of the text color
	 * @param g the green component of the text color
	 * @param b the blue component of the text color
	 * @param alpha the alpha component of the text color
	 * @param blend whether to use alpha blending
	 */
	public void drawString(GLDrawer drawer, String string, Vector pos, Font font, double r,
			double g, double b, double alpha, boolean blend);
	
	/**
	 * Returns the size of the given text in the given font.
	 * The x-coordinate of the returned {@link Rectangle2D} is always 0
	 * The y-coordinate of the returned {@link Rectangle2D} is the descend (positive)
	 * The width of the returned {@link Rectangle2D} is the width of the text
	 * The height of the returned {@link Rectangle2D} is whole height of the text
	 * @param drawer the drawer to be used
	 * @param text the text
	 * @param font the font of the text
	 * @return a {@link Rectangle2D} describing the text's dimensions
	 */
	public Rectangle2D getTextExtents(GLDrawer drawer, String text, Font font);
	
	/**
	 * Returns the on-screen width of the specified character in the given font.
	 * @param c the character
	 * @param f the font
	 * @return the width of the character
	 */
	public double getCharacterWidth(GLDrawer drawer, char c, Font f);
	
	/**
	 * Returns the maximum descend of the given font
	 * @param f the font
	 * @return the maximum descend
	 */
	public double getDescent(GLDrawer drawer, Font f);
	
	/**
	 * Returns the standard line height of text in the given font.
	 * @param f the font
	 * @return the standard line height of text in the given font
	 */
	public double getLineHeight(GLDrawer drawer, Font f);
}
