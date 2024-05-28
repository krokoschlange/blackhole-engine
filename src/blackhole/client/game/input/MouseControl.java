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
package blackhole.client.game.input;

import blackhole.client.game.ClientManager;
import blackhole.client.game.ClientObjectHandler;
import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.awt.AWTException;
import java.awt.Robot;
import java.util.ArrayList;

/**
 * Handles mouse input events and controls curser appearance and mouse position
 * @author fabian
 */
public class MouseControl extends Robot {

	/**
	 * Whether the mouse is trapped inside the game window
	 */
	private boolean cursorCaptured;

	/**
	 * reference to the game window
	 */
	private AbstractGameWindow window;

	/**
	 * the last known position while the {@link #cursorCaptured} was
	 * {@code false}
	 */
	private Vector uncapturedPos;
	
	/**
	 * the last known position while the {@link #cursorCaptured} was
	 * {@code true}. This is a virtual mouse position that allows for (almost)
	 * infinite movement of the mouse (the mouse pointer does not move)
	 */
	private Vector capturedPos;

	/**
	 * the {@link InputHandler} associated with the {@code MouseControl}
	 */
	private InputHandler inputHandler;

	/**
	 * An array containing the codes of all currently pressed mouse buttons
	 */
	private ArrayList<Integer> buttons;
	
	/**
	 * The mouse position as it was after the last mouse movement event
	 */
	private Vector oldMousePosition;
	
	/**
	 * the current mouse wheel rotation
	 */
	private double preciseMouseWheelRotation;

	/**
	 * whether the mouse is being moved by the {@code MouseControl}
	 */
	private volatile boolean robotActive;
	
	/**
	 * the position the {@code MouseControl} is moving the mouse to
	 */
	private volatile Vector robotPos;
	
	/**
	 * the amount the {@code MouseControl} is moving the mouse by
	 */
	private volatile Vector roboDelta;
	
	/**
	 * the current mouse position
	 */
	private volatile Vector currentMousePos;

	/**
	 * whether to send mouse press and release events
	 */
	private boolean sendPressEvents;
	
	/**
	 * whether to send mouse wheel events
	 */
	private boolean sendWheelEvents;
	
	/**
	 * whether to send mouse movement events
	 */
	private boolean sendMovedEvents;

	
	/**
	 * Creates a new {@code MouseControl} associated with the given
	 * {@link InputHandler}
	 * @param ih the {@link InputHandler}
	 * @throws AWTException
	 */
	protected MouseControl(InputHandler ih) throws AWTException {

		inputHandler = ih;
		uncapturedPos = new Vector();
		capturedPos = new Vector();

		window = ClientManager.getInstance().getGraphicsBackend().getWindow();

		buttons = new ArrayList<>();
		oldMousePosition = new Vector();
		preciseMouseWheelRotation = 0;
		robotActive = false;
		
		roboDelta = new Vector();
		robotPos = new Vector();
		currentMousePos = new Vector();

		sendPressEvents = true;
		if (Settings.getProperty("send_mouse_press_events") != null) {
			sendPressEvents = Boolean.parseBoolean(Settings.getProperty("send_mouse_press_events"));
		}
		sendWheelEvents = true;
		if (Settings.getProperty("send_mouse_wheel_events") != null) {
			sendWheelEvents = Boolean.parseBoolean(Settings.getProperty("send_mouse_wheel_events"));
		}
		sendMovedEvents = true;
		if (Settings.getProperty("send_mouse_moved_events") != null) {
			sendMovedEvents = Boolean.parseBoolean(Settings.getProperty("send_mouse_moved_events"));
		}
	}

	/**
	 * Called when a mouse button is pressed. Saves the button in
	 * and informs the {@link InputHandler} about the event.
	 * @param button the button that was pressed
	 * @param x the x-coordinate of where it was pressed
	 * @param y the y-coordinate of where it was pressed
	 */
	public void mousePressed(int button, int x, int y) {
		if (!buttons.contains(button)) {
			buttons.add(button);
			String[] symbols = inputHandler.getSymbolsFromMouseButton(button);
			if (sendPressEvents) {
				if (symbols != null && symbols.length > 0) {
					for (int i = 0; i < symbols.length; i++) {
						inputHandler.addToUpdate(symbols[i]);
					}
				} else {
					inputHandler.addToUpdate("mouse_" + Integer.toString(button));
				}
			}
			if (symbols == null || symbols.length == 0) {
				symbols = new String[]{"mouse_" + button};
			}
			inputHandler.dispatchEvent(new InputEvent(symbols, button, true, new Vector(x, y)));
		}
	}

	/**
	 * Called when a mouse button is released. Removes the button from
	 * and informs the {@link InputHandler} about the event.
	 * @param button the button that was released
	 * @param x the x-coordinate of where it was released
	 * @param y the y-coordinate of where it was released
	 */
	public void mouseReleased(int button, int x, int y) {
		if (buttons.contains(button)) {
			buttons.remove(buttons.indexOf(button));
			String[] symbols = inputHandler.getSymbolsFromMouseButton(button);
			if (sendPressEvents) {
				if (symbols != null && symbols.length > 0) {
					for (int i = 0; i < symbols.length; i++) {
						inputHandler.addToUpdate(symbols[i]);
					}
				} else {
					inputHandler.addToUpdate("mouse_" + Integer.toString(button));
				}
			}
			if (symbols == null || symbols.length == 0) {
				symbols = new String[]{"mouse_" + button};
			}
			inputHandler.dispatchEvent(new InputEvent(symbols, button, false, new Vector(x, y)));
		}
	}

	/**
	 * Called when a mouse is moved.
	 * @param x the new x-coordinate
	 * @param y the new y-coordinate
	 */
	public void mouseMoved(int x, int y) {
		currentMousePos = new Vector(x, y);

		Vector delta = Vector.subtract(currentMousePos, oldMousePosition);
		//Vector winMid = new Vector(window.getWidth() / 2, window.getHeight() / 2);
		//Vector winMidToOldPos = Vector.subtract(oldMousePosition, winMid);
		//double dot = winMidToOldPos.dot(delta);
		
		//Debug.log(currentMousePos.equals(robotPos));
		//Debug.log("robot?: " + ((dot > 0 && !robotActive) && ! (currentMousePos.equals(robotPos))));

		if (/*dot < 0 &&*/ robotActive || currentMousePos.equals(robotPos)) {
			delta.subtract(roboDelta);
		} else {
			robotActive = false;
		}
		
		if (/*(dot > 0 && !robotActive) &&*/ ! (currentMousePos.equals(robotPos))) {

			if (cursorCaptured) {
				capturedPos.add(delta);
			} else {
				uncapturedPos = currentMousePos;
			}

			/*Vector winMid = new Vector(window.getWidth(), window.getHeight()).divide(2);
			Vector relMousePos = Vector.subtract(newPos, winMid);
			winMid.divide(2);
			if (Math.abs(relMousePos.x()) > winMid.x()
					|| Math.abs(relMousePos.y()) > winMid.y()) {
				recapture();
			}*/
			recapture();

			inputHandler.dispatchEvent(new InputEvent(uncapturedPos, delta));

			if (sendMovedEvents) {
				inputHandler.addToUpdate("mousePos");
			}
		} else {
			robotActive = false;
		}

		oldMousePosition = currentMousePos;
	}

	/**
	 * Called when a mouse wheel is moved.
	 * @param deltaRot the difference in the wheel position
	 */
	public void mouseWheelMoved(double deltaRot) {
		preciseMouseWheelRotation += deltaRot;
		inputHandler.dispatchEvent(new InputEvent(preciseMouseWheelRotation, deltaRot, uncapturedPos));
		if (sendWheelEvents) {
			inputHandler.addToUpdate("mouseWheel");
		}
	}

	/**
	 * Returns true if the given button is currently pressed
	 * @param button the button to be checked
	 * @return true if the given button is currently pressed
	 */
	public boolean getButtonPressed(int button) {
		return buttons.contains(button);
	}

	/**
	 * Returns the current mouse wheel rotation
	 * @return the current mouse wheel rotation
	 */
	public double getWheelRotation() {
		return preciseMouseWheelRotation;
	}

	/**
	 * Returns the current mouse position relative to the window if the
	 * mouse is not captured, otherwise it returns the captured mouse position
	 * ({@link #capturedPos})
	 * 
	 * @return the current mouse position
	 * 
	 * @see #capturedPos
	 */
	public Vector getMousePosition() {
		if (cursorCaptured) {
			return capturedPos;
		} else {
			return uncapturedPos;
		}
	}

	/**
	 * @return the {@link #capturedPos}
	 * @see #capturedPos
	 */
	public Vector getCapturedMousePosition() {
		return capturedPos;
	}

	/**
	 * Sets the {@link #capturedPos}
	 * @param pos the new position
	 * 
	 * @see #capturedPos
	 */
	public void setCapturedMousePosition(Vector pos) {
		capturedPos = pos;
	}

	/**
	 * Returns the current mouse position relative to the window if the
	 * mouse is not captured, otherwise the last position where the mouse was
	 * before it was captured
	 * @return the mouse position
	 */
	public Vector getUncapturedMousePosition() {
		return uncapturedPos;
	}

	/**
	 * Returns the position of the mouse transformed to game coordinates
	 * @param handler the {@link ClientObjectHandler} that is used to transform
	 * the mouse position into
	 * @return the position of the mouse transformed to game coordinates
	 */
	public Vector getMouseGamePosition(ClientObjectHandler handler) {
		double gameScale = handler.getScale();
		Vector panelSize = handler.getPanel().size;
		Vector panelPos = handler.getPanel().position;
		Vector camSize = handler.getCamera().getSize();
		double camRot = handler.getCamera().getRotation();
		Vector camPos = handler.getCamera().getPosition();

		Vector mousePos = Vector.subtract(getMousePosition(), Vector.divide(panelSize, 2)).subtract(panelPos).multiply(new Vector(1, -1));
		Vector windowCamRatio = Vector.divide(camSize, panelSize);
		mousePos.multiply(windowCamRatio).divide(gameScale);
		return mousePos.rotate(-camRot).add(camPos);
	}

	/**
	 * Sets the cursor visibility
	 * @param state whether the cursor should be visible
	 */
	public void setCurserVisible(boolean state) {
		window.setCursorVisible(state);
	}

	/**
	 * Returns true if the cursor is visible
	 * @return 
	 */
	public boolean getCursorVisible() {
		return window.isCursorVisible();
	}

	/**
	 * Sets whether the cursor should be captured. If true, the curser will be
	 * kept near the center of the game window, but the {@link #capturedPos}
	 * will be updated according to incoming movement events. This allows for
	 * simulated infinite mouse movement.
	 * @param state whether the cursor should be captured
	 */
	public void setCurserCaptured(boolean state) {
		if (cursorCaptured != state) {
			cursorCaptured = state;
			if (!state) {
				Vector pos = Vector.add(uncapturedPos, window.getLocationOnScreen());
				mouseMove((int) pos.x(), (int) pos.y());
			} else {
				oldMousePosition = uncapturedPos;
			}
		}
	}

	/**
	 * Returns true if the cursor is captured
	 * @return true if the cursor is captured
	 */
	public boolean getCursorCaptured() {
		return cursorCaptured;
	}

	/**
	 * Moves the cursor to the center of the game window if the curser is
	 * captured
	 */
	public void recapture() {
		if (cursorCaptured) {
			robotActive = true;
			Vector screenPos = new Vector(window.getWidth() / 2, window.getHeight() / 2);
			screenPos.add(window.getLocationOnScreen());
			robotPos = new Vector(window.getWidth() / 2, window.getHeight() / 2);
			roboDelta = Vector.subtract(robotPos, currentMousePos);
			mouseMove((int) screenPos.x(), (int) screenPos.y());

		}
	}
}
