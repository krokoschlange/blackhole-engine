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

import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.utils.Debug;
import java.util.ArrayList;

/**
 * A class for collecting all keyboard events and provide information about
 * which keys are pressed.
 * @author fabian
 */
public class KeyboardInput {

	/**
	 * Holds the keycodes for all currently pressed keys. Keycodes of keys that
	 * are "right" versions of a key (e.g. right shift) that appears twice on
	 * the keyboard are offset by 256, numpad keys are offset by 512. This is to
	 * distinguish them from the normal versions easily.
	 */
	private ArrayList<Integer> pressedKeys;

	/**
	 * A reference to the {@code InputHandler} singleton.
	 */
	private InputHandler inputHandler;

	/**
	 * Creates a new KeyboardHandler for an {@code InputHandler}.
	 * @param ih the {@code InputHandler}
	 */
	protected KeyboardInput(InputHandler ih) {
		pressedKeys = new ArrayList<>();
		
		inputHandler = ih;
	}

	/**
	 * This method is called by the {@link AbstractGameWindow} when a key is
	 * pressed. It informs the {@link InputHandler} about it and saves it in
	 * {@link #pressedKeys}
	 * 
	 * @param key the keycode for the key pressed
	 * @param keyPos the physical position on the keyboard
	 * 
	 * @see KeyPosition
	 */
	public void keyPressed(int key, KeyPosition keyPos) {
		int keyNum = key;
		if (keyPos == KeyPosition.RIGHT) {
			keyNum += 256;
		} else if (keyPos == KeyPosition.NUMPAD) {
			keyNum += 512;
		}
		
		if (pressedKeys.indexOf(keyNum) == -1) {
			pressedKeys.add(keyNum);
			String[] symbols = inputHandler.getSymbolsFromKey(keyNum);
			if (symbols != null && symbols.length > 0) {
				for (int i = 0; i < symbols.length; i++) {
					inputHandler.addToUpdate(symbols[i]);
				}
			} else {
				inputHandler.addToUpdate("key_" + keyNum);
				symbols = new String[] {"key_" + keyNum};
			}
			inputHandler.dispatchEvent(new InputEvent(symbols, key, keyPos, true));
		}
	}

	/**
	 * This method is called by the {@link AbstractGameWindow} when a key is
	 * released. It informs the {@link InputHandler} about it and removes it
	 * from {@link #pressedKeys}
	 * 
	 * @param key the keycode for the key released
	 * @param keyPos the physical position on the keyboard
	 * 
	 * @see KeyPosition
	 */
	public void keyReleased(int key, KeyPosition keyPos) {
		int keyNum = key;
		if (keyPos == KeyPosition.RIGHT) {
			keyNum += 256;
		} else if (keyPos == KeyPosition.NUMPAD) {
			keyNum += 512;
		}
		
		if (pressedKeys.indexOf(keyNum) != -1) {
			pressedKeys.remove(pressedKeys.indexOf(keyNum));
			String[] symbols = inputHandler.getSymbolsFromKey(keyNum);
			if (symbols != null && symbols.length > 0) {
				for (int i = 0; i < symbols.length; i++) {
					inputHandler.addToUpdate(symbols[i]);
				}
			} else {
				inputHandler.addToUpdate("key_" + keyNum);
				symbols = new String[] {"key_" + keyNum};
			}
			inputHandler.dispatchEvent(new InputEvent(symbols, key, keyPos, false));
		}
	}
	
	/**
	 * This method is called by the {@link AbstractGameWindow} when a character
	 * is typed. It dispatches an appropriate event.
	 * 
	 * @param c the character typed
	 */
	public void keyTyped(char c) {
		inputHandler.dispatchEvent(new InputEvent(c));
	}

	/**
	 * Returns true if the given key is pressed.
	 * @param key the key to be checked
	 * @param keyPos the position of the key on the keyboard
	 * @return true if the given key is pressed
	 */
	public boolean getKeyPressed(int key, KeyPosition keyPos) {
		int keyNum = key;
		if (keyPos == KeyPosition.RIGHT) {
			keyNum += 256;
		} else if (keyPos == KeyPosition.NUMPAD) {
			keyNum += 512;
		}
		
		return pressedKeys.contains(keyNum);
	}
	
	/**
	 * Returns true if the given key is pressed. The {@code key} argument needs
	 * to be offset by 256 to check for "right" versions fo keys
	 * (e.g. right shift) or 512 for numpad keys as this method only checks if 
	 * {@link #pressedKeys} contains the given keycode.
	 * @param key the keycode of the key to be checked
	 * @return true if the given key is pressed
	 */
	public boolean getKeyPressed(int key) {
		return pressedKeys.contains(key);
	}
}
