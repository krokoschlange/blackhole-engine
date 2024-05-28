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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Implementation of {@link AbstractGameWindow} using the AWT framework.
 * It fixes the issue that the {@link GLGameWindow} cannot distinuish between
 * left and right and numpad keys (e.g. left and right shift). It may be slighly
 * slower than {@link GLGameWindow} however.
 * @author fabian
 */
public class GLAWTGameWindow implements AbstractGameWindow, GLEventListener {

	/**
	 * the associated {@link GraphicsBackend}
	 */
	private GLGraphicsBackend backend;

	/**
	 * The window
	 */
	private Frame window;
	
	/**
	 * the canvas that will be drawn on
	 */
	private GLCanvas canvas;
	
	/**
	 * An animator that allows for active rendering
	 */
	private FPSAnimator animator;

	/**
	 * an invisible cursor
	 */
	private Cursor hiddenCursor;
	
	/**
	 * the default (visible) cursor
	 */
	private Cursor defaultCursor;

	/**
	 * the time of the current frame
	 */
	private long now = 0;

	/**
	 * the keyboard listener
	 */
	private GLAWTGameWindowKeyListener keyListener;
	
	/**
	 * the mouse listener
	 */
	private GLAWTGameWindowMouseListener mouseListener;
	
	/**
	 * Whether the window is ready for use
	 */
	private boolean ready;

	/**
	 * Creates a new {@code GLAWTGameWindow} associated with the given
	 * {@link GLGraphicsBackend}
	 * @param glgb the graphics backend
	 */
	public GLAWTGameWindow(GLGraphicsBackend glgb) {
		backend = glgb;

		window = new Frame();
		canvas = new GLCanvas(glgb.getGLCapabilities()) {
			/* workaround for some weird bug where it gets confused when moved
			 * between monitors
			 */
			@Override
			public GraphicsConfiguration getGraphicsConfiguration() {
				return window.getGraphicsConfiguration();
			}

		};
		canvas.addGLEventListener(this);
		window.add(canvas);
		window.pack();

		canvas.setAutoSwapBufferMode(false);
		canvas.requestFocusInWindow();

		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				ClientManager.getInstance().killClient();
			}
		});
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				int width = getWidth();
				int height = getHeight();

				ClientManager.getInstance().windowResized(width, height);
			}
		});

		int targetFPS;
		if (Settings.getProperty("render_speed") != null) {
			targetFPS = Integer.parseInt(Settings.getProperty("render_speed"));
		} else {
			targetFPS = 60;
		}

		animator = new FPSAnimator(canvas, targetFPS, true);
		animator.setUpdateFPSFrames(10, null);
		BufferedImage hiddenCursorImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(hiddenCursorImage, new Point(0, 0), "hiddenCursor");
		defaultCursor = Cursor.getDefaultCursor();
		
		ready = false;

	}

	/**
	 * Called when the window is initialized, sets up the listners and the
	 * {@link GLDrawer}
	 * @param drawable the {@code GLAutoDrawable} that got initialized
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
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
	 * @param drawable the {@code GLAutoDrawable} that has to be redrawn
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		double dtime = System.nanoTime() - now;
		now = System.nanoTime();

		if (mouseListener != null) {
			mouseListener.recapture();
		}

		GL2 gl = drawable.getGL().getGL2();

		GLDrawer drawer = backend.getDrawer();

		drawer.setGL(gl);
		backend.processEvents();

		drawer.setDrawBuffer(null);
		drawer.clear(0.3, 0.3, 0.3, 1);

		ClientManager.getInstance().draw(dtime * 1e-9);

		gl.glFlush();
		canvas.swapBuffers();
	}

	/**
	 * Called when the window got resized
	 * @param drawable the {@code GLAutoDrawable} that got resized
	 * @param x the new x-coordinate
	 * @param y the new y-coordinate
	 * @param w the new width
	 * @param h the new height
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		setKeyboardInput(InputHandler.getInstance().getKeyboardInput());
		setMouseInput(InputHandler.getInstance().getMouseControl());
		window.setVisible(true);
		canvas.setVisible(true);
		animator.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		animator.stop();
		canvas.destroy();
		window.dispose();
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
		return canvas.getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return canvas.getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWidth(int w) {
		canvas.setSize(w, canvas.getHeight());
		window.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHeight(int h) {
		canvas.setSize(canvas.getWidth(), h);
		window.pack();
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
		if (state) {
			canvas.setCursor(defaultCursor);
		} else {
			canvas.setCursor(hiddenCursor);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCursorVisible() {
		return canvas.getCursor() != hiddenCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Vector getLocationOnScreen() {
		try {
			return new Vector(
					canvas.getLocationOnScreen().x,
					canvas.getLocationOnScreen().y
			);
		} catch (IllegalComponentStateException e) {
			return new Vector();
		}
	}

	/**
	 * The keyboard listener that is used by this window
	 */
	private static class GLAWTGameWindowKeyListener implements KeyListener {

		private KeyboardInput keyboardInput;

		public GLAWTGameWindowKeyListener(KeyboardInput input) {
			keyboardInput = input;
		}

		@Override
		public void keyTyped(KeyEvent event) {
			keyboardInput.keyTyped(event.getKeyChar());
		}

		@Override
		public void keyPressed(KeyEvent event) {
			KeyPosition keyPos = KeyPosition.STANDARD;
			if (event.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) {
				keyPos = KeyPosition.NUMPAD;
			} else if (event.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
				keyPos = KeyPosition.RIGHT;
			}
			keyboardInput.keyPressed(event.getKeyCode(), keyPos);
		}

		@Override
		public void keyReleased(KeyEvent event) {
			KeyPosition keyPos = KeyPosition.STANDARD;
			if (event.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) {
				keyPos = KeyPosition.NUMPAD;
			} else if (event.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT) {
				keyPos = KeyPosition.RIGHT;
			}
			keyboardInput.keyReleased(event.getKeyCode(), keyPos);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setKeyboardInput(KeyboardInput input) {
		if (input != null) {
			keyListener = new GLAWTGameWindowKeyListener(input);
			canvas.addKeyListener(keyListener);
		} else {
			canvas.removeKeyListener(keyListener);
			keyListener = null;
		}
	}

	/**
	 * The mouse listener that is used by this window
	 */
	private static class GLAWTGameWindowMouseListener extends MouseAdapter {

		private MouseControl mouseInput;

		public GLAWTGameWindowMouseListener(MouseControl input) {
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
		public void mouseWheelMoved(MouseWheelEvent event) {
			mouseInput.mouseWheelMoved(event.getPreciseWheelRotation());
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			mouseInput.recapture();
		}

		public void recapture() {
			mouseInput.recapture();
		}
	}

	/*private static class GLAWTGlobalListener implements AWTEventListener {

		private GLAWTGameWindowMouseListener listener;

		public GLAWTGlobalListener(MouseControl input) {
			listener = new GLAWTGameWindowMouseListener(input);
		}

		@Override
		public void eventDispatched(AWTEvent event) {
			switch (event.getID()) {
				case MouseEvent.MOUSE_PRESSED:
					listener.mousePressed((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_RELEASED:
					listener.mouseReleased((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_MOVED:
					listener.mouseMoved((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_DRAGGED:
					listener.mouseDragged((MouseEvent) event);
					break;
				case MouseEvent.MOUSE_WHEEL:
					listener.mouseWheelMoved((MouseWheelEvent) event);
					break;
				case MouseEvent.MOUSE_EXITED:
					listener.mouseExited((MouseEvent) event);
					break;
				default:
					break;
			}
		}

	}*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMouseInput(MouseControl input) {
		if (input != null) {
			mouseListener = new GLAWTGameWindowMouseListener(input);
			canvas.addMouseListener(mouseListener);
			canvas.addMouseMotionListener(mouseListener);
			canvas.addMouseWheelListener(mouseListener);
		} else {
			canvas.removeMouseListener(mouseListener);
			canvas.removeMouseMotionListener(mouseListener);
			canvas.removeMouseWheelListener(mouseListener);
			mouseListener = null;
		}
		/*Toolkit.getDefaultToolkit().addAWTEventListener(
				new GLAWTGlobalListener(input),
				AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_WHEEL_EVENT_MASK
				+ AWTEvent.MOUSE_EVENT_MASK
				+ AWTEvent.KEY_EVENT_MASK);*/
	}
}
