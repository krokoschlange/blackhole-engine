/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.client.graphicsEngine.opengl;

import com.jogamp.graph.font.Font;

/**
 * A wrapper for {@code com.jogamp.graph.font.Font} that uses the same
 * parameters as {@code java.awt.Font}
 * @author fabian
 */
public class GLGameFont {

	/**
	 * The JOGL font
	 */
	private Font font;
	
	/**
	 * the font name
	 */
	private String fontName;
	
	/**
	 * the font style bits
	 */
	private int fontStyle;
	
	/**
	 * the font size
	 */
	private int fontSize;

	/**
	 * Creates a new {@code GLGameFont} with the given parameters. It gets the
	 * JOGL font from {@link FontManager#getFont(java.lang.String, int)}
	 * @param name the font name
	 * @param style the style bits
	 * @param size the font size
	 */
	public GLGameFont(String name, int style, int size) {
		font = FontManager.getInstance().getFont(name, style);
		fontName = name;
		fontStyle = style;
		fontSize = size;
	}
	
	/**
	 * Returns the JOGL font
	 * @return the JOGL font
	 */
	public Font getFont() {
		return font;
	}
	
	/**
	 * Returns the font name
	 * @return the font name
	 */
	public String getName() {
		return fontName;
	}

	/**
	 * Returns the font style bits
	 * @return the font style bits
	 */
	public int getStyle() {
		return fontStyle;
	}
	
	/**
	 * Returns the font size
	 * @return the font size
	 */
	public int getSize() {
		return fontSize;
	}
}
