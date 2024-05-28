/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.client.graphicsEngine.opengl;

import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.utils.Vector;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * A {@link GLStringDrawer} that uses the JOGL awt text drawing method. It uses
 * Java2D to render the text into a buffer and then uses the buffer as a texture
 * @author fabian
 */
public class GLAWTStringDrawer implements GLStringDrawer {

	/**
	 * cached text renderers as they cannot switch fonts
	 */
	private HashMap<Font, TextRenderer> textRenderers;

	/**
	 * Creates a new {@code GLAWTStringDrawer}
	 */
	public GLAWTStringDrawer() {
		textRenderers = new HashMap<>();
	}

	/**
	 * Returns the text renderer for the given font
	 * @param font the font
	 * @return the text renderer for the given font
	 */
	private TextRenderer getTextRenderer(Font font) {
		if (!textRenderers.containsKey(font)) {
			textRenderers.put(font, new TextRenderer(font, true, true, null, true));
		}
		return textRenderers.get(font);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(GLDrawer drawer, String string, Vector pos,
			double rot, Vector scale, Camera cam, HandlerPanel panel,
			double gameScale, Font font, double r, double g, double b,
			double alpha, boolean blend) {

		GL2 gl2 = drawer.getGL().getGL2();

		Vector panelSize = null;
		Vector panelPos = null;

		Vector fbSize = drawer.getDrawbufferSize( drawer.getDrawBuffer());

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

		Font largeFont = new Font(font.getName(), font.getStyle(), font.getSize() * 10);
		TextRenderer textRenderer = getTextRenderer(largeFont);
		textRenderer.setSmoothing(false);
		gl2.glScissor((int) panelPos.x(), (int) (fbSize.y() - panelPos.y() - panelSize.y()),
				(int) panelSize.x(), (int) panelSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}
		textRenderer.beginRendering((int) fbSize.x(), (int) fbSize.y());
		gl2.glLoadIdentity();

		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl2.glTranslated(panelSize.x() / 2, panelSize.y() / 2, 0);
		gl2.glTranslated(panelPos.x(), -panelPos.y(), 0);
		gl2.glScaled(cameraScale.x(), cameraScale.y(), 1);
		gl2.glRotated(Math.toDegrees(-cam.getRotation()), 0, 0, 1);
		gl2.glTranslated(relativePosition.x(), relativePosition.y(), 0);
		gl2.glRotated(Math.toDegrees(rot), 0, 0, 1);

		gl2.glScaled(scale.x() / 10, scale.y() / 10, 1);
		textRenderer.setColor((float) r, (float) g, (float) b, (float) alpha);
		textRenderer.draw(string, 0, 0);
		textRenderer.endRendering();
		//textRenderer.flush();

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(GLDrawer drawer, String string, Vector pos,
			Font font, double r, double g, double b, double alpha,
			boolean blend) {
		GL2 gl2 = drawer.getGL().getGL2();

		Vector fbSize = drawer.getDrawbufferSize(drawer.getDrawBuffer());

		TextRenderer textRenderer = getTextRenderer(font);
		textRenderer.setSmoothing(false);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}

		textRenderer.beginRendering((int) fbSize.x(), (int) fbSize.y());
		gl2.glLoadIdentity();

		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl2.glTranslated(fbSize.x() / 2 + pos.x(), fbSize.y() / 2 - pos.y(), 0);

		textRenderer.setColor((float) r, (float) g, (float) b, (float) alpha);
		textRenderer.draw(string, 0, 0);
		textRenderer.endRendering();
		//textRenderer.flush();;

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Rectangle2D getTextExtents(GLDrawer drawer, String text, Font font) {
		TextRenderer textRenderer = getTextRenderer(font);
		
		if (text.length() == 0) {
			text = " ";
		}
		
		TextLayout layout = new TextLayout(text, font, textRenderer.getFontRenderContext());
		return new Rectangle.Float(0, layout.getDescent(), layout.getAdvance(),
				layout.getLeading() + layout.getAscent() + layout.getDescent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCharacterWidth(GLDrawer drawer, char c, Font f) {
		TextRenderer textRenderer = getTextRenderer(f);
		
		return textRenderer.getCharWidth(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDescent(GLDrawer drawer, Font f) {
		TextRenderer textRenderer = getTextRenderer(f);
		
		return f.getLineMetrics(" ", textRenderer.getFontRenderContext()).getDescent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLineHeight(GLDrawer drawer, Font f) {
		TextRenderer textRenderer = getTextRenderer(f);
		return f.getLineMetrics(" ", textRenderer.getFontRenderContext()).getHeight();
	}
}
