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

import blackhole.client.game.input.KeyboardInput;
import blackhole.client.game.input.MouseControl;
import blackhole.utils.Vector;

/**
 * Interface to describe a game window. The game window is responsible for
 * managing the render thread and receiving input events.
 * @author fabian
 */
public interface AbstractGameWindow {
	
	/**
	 * Starts the render thread
	 */
	public void start();
	
	/**
	 * stops the render thread and destroys the window
	 */
	public void destroy();
	
	/**
	 * Returns an estimate of the current FPS
	 * @return an estimate of the current FPS
	 */
	public double getCurrentFPS();
	
	/**
	 * Returns the width of the window (without decorations)
	 * @return 
	 */
	public int getWidth();
	
	/**
	 * Returns the height of the window (without decorations)
	 * @return 
	 */
	public int getHeight();
	
	/**
	 * Returns true if the render thread is running
	 * @return true if the render thread is running
	 */
	public boolean isRunning();
	
	/**
	 * Sets the window's width (without decorations)
	 * @param w the new width
	 */
	public void setWidth(int w);
	
	/**
	 * Sets the window's height (without decorations)
	 * @param h the new height
	 */
	public void setHeight(int h);
	
	/**
	 * Sets the window title
	 * @param t the new title
	 */
	public void setTitle(String t);
	
	/**
	 * Returns the window's title
	 * @return 
	 */
	public String getTitle();
	
	/**
	 * Sets the cursor's visibility
	 * @param state true if the curser should be visible
	 */
	public void setCursorVisible(boolean state);
	
	/**
	 * Returns true if the cursor is visible
	 * @return true if the cursor is visible
	 */
	public boolean isCursorVisible();
	
	/**
	 * Returns the current position of the window on the screen
	 * @return the current position of the window on the screen
	 */
	public Vector getLocationOnScreen();
	
	/**
	 * Sets the {@link KeyboardInput} that should be informed about keyboard
	 * events
	 * @param input the {@link KeyboardInput} to use
	 */
	public void setKeyboardInput(KeyboardInput input);
	
	/**
	 * Sets the {@link MouseControl} that should be informed about mouse
	 * events
	 * @param input the {@link MouseControl} to use
	 */
	public void setMouseInput(MouseControl input);
}
