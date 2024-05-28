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

import blackhole.client.game.ClientManager;
import blackhole.client.game.input.InputHandler;
import blackhole.client.game.input.KeyPosition;
import blackhole.client.game.input.KeyboardInput;
import blackhole.client.game.input.MouseControl;
import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Graphics2D;
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
import javax.swing.JFrame;

/**
 * Implements a game window using swing and Java2D
 * @author fabian.baer2
 */
public class J2DGameWindow implements AbstractGameWindow {

	/**
	 * the associated {@link GraphicsBackend}
	 */
	private J2DGraphicsBackend backend;

	/**
	 * the window
	 */
	private JFrame frame;
	
	/**
	 * the canvas that will be drawn to
	 */
	private Canvas canvas;

	/**
	 * the render thread
	 */
	private Thread renderLoop;

	/**
	 * the keyboard listener
	 */
	private J2DGameWindowKeyListener keyListener;
	
	/**
	 * the mouse listener
	 */
	private J2DGameWindowMouseListener mouseListener;

	/**
	 * an invisible cursor
	 */
	private Cursor hiddenCursor;
	
	/**
	 * the default (visible) cursor
	 */
	private Cursor defaultCursor;

	/**
	 * the maximum fps
	 */
	private double targetFPS;

	/**
	 * current fps
	 */
	private double fps;

	/**
	 * Creates a new {@code J2DGameWindow} associated with the given
	 * {@link J2DGraphicsBackend}
	 * @param j2dgb the graphics backend
	 */
	public J2DGameWindow(J2DGraphicsBackend j2dgb) {
		backend = j2dgb;
		frame = new JFrame();
		canvas = new Canvas();
		
		frame.add(canvas);
		frame.pack();
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				ClientManager.getInstance().killClient();
			}
		});
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int width = getWidth();
				int height = getHeight();
				
				ClientManager.getInstance().windowResized(width, height);
			}
		});

		canvas.createBufferStrategy(2);
		canvas.requestFocusInWindow();

		BufferedImage hiddenCursorImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(hiddenCursorImage, new Point(0, 0), "hiddenCursor");
		defaultCursor = Cursor.getDefaultCursor();

		if (Settings.getProperty("render_speed") != null) {
			targetFPS = Integer.parseInt(Settings.getProperty("render_speed"));
		} else {
			targetFPS = 60;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		renderLoop = new Thread() {
			@Override
			public void run() {
				try {
					setKeyboardInput(InputHandler.getInstance().getKeyboardInput());
					setMouseInput(InputHandler.getInstance().getMouseControl());
					frame.setVisible(true);
					long now = System.nanoTime();

					long targetDtime = (long) (1 / targetFPS * 1e9);

					long time = 0;
					long frames = 0;

					while (true) {

						long dtime = System.nanoTime() - now;
						now = System.nanoTime();

						J2DDrawer drawer = backend.getDrawer();

						drawer.setWindowGraphics((Graphics2D) canvas.getBufferStrategy().getDrawGraphics());

						drawer.setDrawBuffer(null);
						drawer.clear(0.3, 0.3, 0.3, 1);

						ClientManager.getInstance().draw(dtime / 1e9);
						canvas.getBufferStrategy().show();
						Toolkit.getDefaultToolkit().sync();
						
						long drawTime = System.nanoTime() - now;
						
						long extraTime = targetDtime - drawTime;
						if (extraTime > 0) {
							sleep((int) (extraTime / 1e6));
						}

						time += dtime;
						frames++;
						if (frames >= 10) {
							fps = frames / (time / 1e9);
							time = 0;
							frames = 0;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		renderLoop.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		renderLoop.interrupt();
		frame.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCurrentFPS() {
		return fps;
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
	public boolean isRunning() {
		return renderLoop.isAlive();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setWidth(int w) {
		canvas.setSize(w, canvas.getHeight());
		frame.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHeight(int h) {
		canvas.setSize(canvas.getWidth(), h);
		frame.pack();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTitle(String t) {
		frame.setTitle(t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return frame.getTitle();
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
	private class J2DGameWindowKeyListener implements KeyListener {

		private KeyboardInput keyboardInput;

		public J2DGameWindowKeyListener(KeyboardInput input) {
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
			keyListener = new J2DGameWindowKeyListener(input);
			canvas.addKeyListener(keyListener);
		} else {
			canvas.removeKeyListener(keyListener);
			keyListener = null;
		}
	}

	/**
	 * The mouse listener that is used by this window
	 */
	private class J2DGameWindowMouseListener extends MouseAdapter {

		private MouseControl mouseInput;

		public J2DGameWindowMouseListener(MouseControl input) {
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMouseInput(MouseControl input) {
		if (input != null) {
			mouseListener = new J2DGameWindowMouseListener(input);
			canvas.addMouseListener(mouseListener);
			canvas.addMouseMotionListener(mouseListener);
			canvas.addMouseWheelListener(mouseListener);
		} else {
			canvas.removeMouseListener(mouseListener);
			canvas.removeMouseMotionListener(mouseListener);
			canvas.removeMouseWheelListener(mouseListener);
			mouseListener = null;
		}
	}

	/**
	 * Returns the {@link GraphicsConfiguration} used by this window
	 * @return the {@link GraphicsConfiguration} used by this window
	 */
	protected GraphicsConfiguration getGraphicsConfiguration() {
		return canvas.getGraphicsConfiguration();
	}

}
