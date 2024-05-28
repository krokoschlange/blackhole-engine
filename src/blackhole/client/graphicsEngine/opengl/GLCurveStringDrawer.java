/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.client.graphicsEngine.opengl;

import blackhole.client.graphicsEngine.Camera;
import blackhole.client.graphicsEngine.HandlerPanel;
import blackhole.utils.Vector;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.math.geom.AABBox;
import com.jogamp.opengl.util.PMVMatrix;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * A {@link GLStringDrawer} that uses the JOGL curve text drawing method
 * @author fabian
 */
public class GLCurveStringDrawer implements GLStringDrawer {
	private RegionRenderer textRenderer;
	private RenderState textRenderState;
	private TextRegionUtil textUtil;
	private final int[] sampleCount = {16};
	
	/**
	 * Sets up the renderer
	 * @param gl the opengl object to use
	 */
	public void init(GL gl) {
		GL2 gl2 = gl.getGL2();
		gl2.glEnable(GL.GL_DEPTH_TEST);
		gl2.glEnable(GL.GL_BLEND);
		gl2.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);

		textRenderState = RenderState.createRenderState(SVertex.factory());
		textRenderState.setColorStatic(1f, 0f, 0f, 0.5f);
		textRenderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED | RenderState.BITHINT_BLENDING_ENABLED);

		textRenderer = RegionRenderer.create(textRenderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);

		textUtil = new TextRegionUtil(0);

		textRenderer.init(gl2, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(GLDrawer drawer, String string, Vector pos,
			double rot, Vector scale, Camera cam, HandlerPanel panel,
			double gameScale, Font font, double r, double g, double b,
			double alpha, boolean blend) {
		GLGameFont glfont = drawer.getBackend().getFont(font.getName(), font.getStyle(), font.getSize());

		GL2 gl2 = drawer.getGL().getGL2();

		Vector panelSize = null;
		Vector panelPos = null;

		Vector fbSize = drawer.getDrawbufferSize(drawer.getDrawBuffer());

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

		/*Font largeFont = new Font(font.getName(), font.getStyle(), font.getSize() * 10);
		TextRenderer textRenderer = getTextRenderer(largeFont);
		textRenderer.setSmoothing(false);*/
		gl2.glScissor((int) panelPos.x(), (int) (fbSize.y() - panelPos.y() - panelSize.y()),
				(int) panelSize.x(), (int) panelSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		/*if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}*/
		//textRenderer.beginRendering((int) fbSize.x(), (int) fbSize.y());
		/*gl2.glLoadIdentity();

		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl2.glTranslated(panelSize.x() / 2, panelSize.y() / 2, 0);
		gl2.glTranslated(panelPos.x(), -panelPos.y(), 0);
		gl2.glScaled(cameraScale.x(), cameraScale.y(), 1);
		gl2.glRotated(Math.toDegrees(-cam.getRotation()), 0, 0, 1);
		gl2.glTranslated(relativePosition.x(), relativePosition.y(), 0);
		gl2.glRotated(Math.toDegrees(rot), 0, 0, 1);

		gl2.glScaled(scale.x() / 10, scale.y() / 10, 1);*/
		//textRenderer.setColor((float) r, (float) g, (float) b, (float) alpha);
		//textRenderer.draw(string, 0, 0);
		//textRenderer.endRendering();
		//textRenderer.flush();
		PMVMatrix pmv = textRenderer.getMatrix();
		textRenderer.reshapeOrtho((int) fbSize.x(), (int) fbSize.y(), 0.1f, 70);
		pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		pmv.glLoadIdentity();
		pmv.glTranslatef((float) (panelSize.x() / 2 + panelPos.x()), (float) (panelSize.y() - panelPos.y()), -10);
		pmv.glScalef((float) cameraScale.x(), (float) cameraScale.y(), 1);
		pmv.glRotatef((float) Math.toDegrees(-cam.getRotation()), 0, 0, 1);
		pmv.glTranslatef((float) relativePosition.x(), (float) relativePosition.y(), 0);
		pmv.glRotatef((float) Math.toDegrees(rot), 0, 0, 1);
		
		textRenderState.setColorStatic((float) r, (float) g, (float) b, (float) alpha);

		if (1.0f != textRenderer.getRenderState().getWeight()) {
			textRenderer.getRenderState().setWeight(1.0f);
		}

		textRenderer.enable(gl2, true);
		if (!blend) {
			gl2.glDisable(GL.GL_BLEND);
		}
		textUtil.drawString3D(gl2, textRenderer, glfont.getFont(), glfont.getSize(), string, null, sampleCount);
		textRenderer.enable(gl2, false);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drawString(GLDrawer drawer, String string, Vector pos,
			Font font, double r, double g, double b, double alpha,
			boolean blend) {
		GLGameFont glfont = drawer.getBackend().getFont(font.getName(), font.getStyle(), font.getSize());

		GL2 gl2 = drawer.getGL().getGL2();

		Vector fbSize = drawer.getDrawbufferSize(drawer.getDrawBuffer());

		//TextRenderer textRenderer = getTextRenderer(font);
		//textRenderer.setSmoothing(false);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
		gl2.glViewport(0, 0, (int) fbSize.x(), (int) fbSize.y());

		/*if (blend) {
			gl2.glEnable(GL.GL_BLEND);
			gl2.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE);
		} else {
			gl2.glDisable(GL.GL_BLEND);
		}*/

		/*textRenderer.beginRendering((int) fbSize.x(), (int) fbSize.y());
		gl2.glLoadIdentity();

		gl2.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

		gl2.glTranslated(fbSize.x() / 2 + pos.x(), fbSize.y() / 2 - pos.y(), 0);

		textRenderer.setColor((float) r, (float) g, (float) b, (float) alpha);
		textRenderer.draw(string, 0, 0);
		textRenderer.endRendering();
		//textRenderer.flush();*/
		
		PMVMatrix pmv = textRenderer.getMatrix();
		textRenderer.reshapeOrtho((int) fbSize.x(), (int) fbSize.y(), 0.1f, 70);
		pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		pmv.glLoadIdentity();
		pmv.glTranslatef((float) (fbSize.x() / 2 + pos.x()), (float) (fbSize.y() / 2 -pos.y()), -10);
		
		textRenderState.setColorStatic((float) r, (float) g, (float) b, (float) alpha);

		if (1.0f != textRenderer.getRenderState().getWeight()) {
			textRenderer.getRenderState().setWeight(1.0f);
		}

		textRenderer.enable(gl2, true);
		if (!blend) {
			//gl2.glDisable(GL.GL_BLEND);
		}
		textUtil.drawString3D(gl2, textRenderer, glfont.getFont(), glfont.getSize(), string, null, sampleCount);
		textRenderer.enable(gl2, false);

		gl2.glScissor(0, 0, (int) fbSize.x(), (int) fbSize.y());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Rectangle2D getTextExtents(GLDrawer drawer, String text, Font font) {
		GLGameFont glfont = drawer.getBackend().getFont(font.getName(), font.getStyle(), font.getSize());
		AABBox aabb = glfont.getFont().getMetricBounds(text, glfont.getSize());
		
		return new Rectangle2D.Double(aabb.getMinX(), aabb.getMinY(),
				aabb.getWidth(), aabb.getHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCharacterWidth(GLDrawer drawer, char c, Font f) {
		GLGameFont glfont = drawer.getBackend().getFont(f.getName(), f.getStyle(), f.getSize());
		
		return glfont.getFont().getGlyph(c).getAdvance(glfont.getSize(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDescent(GLDrawer drawer, Font f) {
		GLGameFont glfont = drawer.getBackend().getFont(f.getName(), f.getStyle(), f.getSize());
		
		return glfont.getFont().getMetrics().getDescent(f.getSize());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getLineHeight(GLDrawer drawer, Font f) {
		GLGameFont glfont = drawer.getBackend().getFont(f.getName(), f.getStyle(), f.getSize());
		
		return glfont.getFont().getLineHeight(f.getSize());
	}
}
