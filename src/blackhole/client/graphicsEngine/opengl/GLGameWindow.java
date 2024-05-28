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

import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.client.game.ClientManager;
import blackhole.client.game.input.InputHandler;
import blackhole.client.game.input.KeyPosition;
import blackhole.client.game.input.KeyboardInput;
import blackhole.client.game.input.MouseControl;
import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Implementation of {@link AbstractGameWindow} using the JOGL NEWT framework.
 * It should be the fastest implementation, but it can not differentiate between
 * different physical positions of the same key on the keyboard (e.g. left shift
 * and right shift are the same)
 *
 * @author fabian
 */
public class GLGameWindow implements AbstractGameWindow, GLEventListener {

	/**
	 * the associated {@link GraphicsBackend}
	 */
	private GLGraphicsBackend backend;

	/**
	 * The window
	 */
	private GLWindow window;

	/**
	 * the canvas that will be drawn on
	 */
	private NewtCanvasAWT canvas;

	/**
	 * An animator that allows for active rendering
	 */
	private FPSAnimator animator;

	/**
	 * the time of the current frame
	 */
	private long now = 0;

	/**
	 * the keyboard listener
	 */
	private GLGameWindowKeyListener keyListener;

	/**
	 * the mouse listener
	 */
	private GLGameWindowMouseListener mouseListener;
	
	/**
	 * Whether the window is ready for use
	 */
	private boolean ready;

	/**
	 * Creates a new {@code GLGameWindow} associated with the given
	 * {@link GLGraphicsBackend}
	 *
	 * @param glgb the graphics backend
	 */
	public GLGameWindow(GLGraphicsBackend glgb) {
		backend = glgb;

		window = GLWindow.create(glgb.getGLCapabilities());
		canvas = new NewtCanvasAWT(window);

		window.setAutoSwapBufferMode(false);

		int targetFPS;
		if (Settings.getProperty("render_speed") != null) {
			targetFPS = Integer.parseInt(Settings.getProperty("render_speed"));
		} else {
			targetFPS = 60;
		}

		animator = new FPSAnimator(window, targetFPS, true);
		animator.setUpdateFPSFrames(10, null);
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyNotify(WindowEvent event) {
				ClientManager.getInstance().killClient();
			}

			@Override
			public void windowResized(WindowEvent event) {
				int width = getWidth();
				int height = getHeight();

				ClientManager.getInstance().windowResized(width, height);
			}

		});
		window.addGLEventListener(this);

		keyListener = null;
		ready = false;
	}

	/**
	 * Called when the window is initialized, sets up the listners and the
	 * {@link GLDrawer}
	 *
	 * @param drawable the {@code GLAutoDrawable} that got initialized
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		setKeyboardInput(InputHandler.getInstance().getKeyboardInput());
		setMouseInput(InputHandler.getInstance().getMouseControl());

		GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		GLDrawer drawer = backend.getDrawer();

		drawer.setGL(gl);
		drawer.init();
		
		ready = true;
	}

	/**
	 * Called when the window is destroyed
	 *
	 * @param drawable the {@code GLAutoDrawable} that got destroyed
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		GLDrawer drawer = backend.getDrawer();

		drawer.setGL(gl);
		drawer.dispose();
	}

	/**
	 * Called when a new frame has to be drawn. Clears the screen and draws
	 * everything. Also calls {@link GLGraphicsBackend#processEvents()}
	 *
	 * @param drawable the {@code GLAutoDrawable} that has to be redrawn
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		double dtime = System.nanoTime() - now;
		now = System.nanoTime();

		GL2 gl = drawable.getGL().getGL2();

		GLDrawer drawer = backend.getDrawer();

		drawer.setGL(gl);
		backend.processEvents();

		drawer.setDrawBuffer(null);
		drawer.clear(0.3, 0.3, 0.3, 1);

		ClientManager.getInstance().draw(dtime * 1e-9);

		window.swapBuffers();
	}

	/**
	 * Called when the window got resized
	 *
	 * @param drawable the {@code GLAutoDrawable} that got resized
	 * @param x the new x-coordinate
	 * @param y the new y-coordinate
	 * @param w the new width
	 * @param h the new height
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
	}

	public void resetGL(GL2 gl) {
		gl.glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
		gl.glLoadIdentity();
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.0f);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		window.setVisible(true);
		animator.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		animator.stop();
		window.destroy();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCurrentFPS() {
		return animator.getLastFPS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRunning() {
		return animator.isAnimating() && ready;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return window.getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return window.getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWidth(int w) {
		window.setSize(w, window.getHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHeight(int h) {
		window.setSize(window.getWidth(), h);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String t) {
		window.setTitle(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return window.getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCursorVisible(boolean state) {
		window.setPointerVisible(state);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCursorVisible() {
		return window.isPointerVisible();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getLocationOnScreen() {
		return new Vector(
				window.getX(),
				window.getY()
		);
	}

	/**
	 * The keyboard listener that is used by this window
	 */
	private class GLGameWindowKeyListener implements KeyListener {

		private KeyboardInput keyboardInput;

		public GLGameWindowKeyListener(KeyboardInput input) {
			keyboardInput = input;
		}

		@Override
		public void keyPressed(KeyEvent event) {
			if (!event.isAutoRepeat()) {
				keyboardInput.keyPressed(event.getKeyCode(), KeyPosition.STANDARD);
			}
			if (event.isPrintableKey()) {
				keyboardInput.keyTyped(event.getKeyChar());
			}
		}

		@Override
		public void keyReleased(KeyEvent event) {
			if (event.isAutoRepeat()) {
				return;
			}
			keyboardInput.keyReleased(event.getKeyCode(), KeyPosition.STANDARD);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setKeyboardInput(KeyboardInput input) {
		if (input != null) {
			keyListener = new GLGameWindowKeyListener(input);
			window.addKeyListener(keyListener);
		} else {
			window.removeKeyListener(keyListener);
			keyListener = null;
		}
	}

	/**
	 * The mouse listener that is used by this window
	 */
	private class GLGameWindowMouseListener extends MouseAdapter {

		private MouseControl mouseInput;

		public GLGameWindowMouseListener(MouseControl input) {
			mouseInput = input;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			mouseInput.mousePressed(event.getButton(), event.getX(), event.getY());
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			mouseInput.mouseReleased(event.getButton(), event.getX(), event.getY());
		}

		@Override
		public void mouseMoved(MouseEvent event) {
			mouseInput.mouseMoved(event.getX(), event.getY());
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			mouseInput.mouseMoved(event.getX(), event.getY());
		}

		@Override
		public void mouseWheelMoved(MouseEvent event) {
			mouseInput.mouseWheelMoved(-event.getRotation()[1]);
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			mouseInput.recapture();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMouseInput(MouseControl input) {
		if (input != null) {
			mouseListener = new GLGameWindowMouseListener(input);
			window.addMouseListener(mouseListener);
		} else {
			window.removeMouseListener(mouseListener);
			mouseListener = null;
		}
	}
}
