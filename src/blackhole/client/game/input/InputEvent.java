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

import blackhole.utils.Vector;

/**
 * This class represents an input event, i.e. a mouse or a keyboard event
 * @author fabian.baer2
 */
public class InputEvent {

	/**
	 * Events of this type indicate that a game control's state has changed.
	 * May be combined with {@link #KEY} or {@link #BUTTON} as mouse buttons and
	 * keyboard keys can be mapped to controls
	 * 
	 * @see InputHandler
	 */
	public static final int CONTROL = 1;
	
	/**
	 * Events of this type indicate that a keyboard key has changed its state
	 */
	public static final int KEY = 2;
	
	/**
	 * Events of this type indicate that a mouse button has changed its state
	 */
	public static final int BUTTON = 4;
	
	/**
	 * Events of this type indicate that the mouse has moved
	 */
	public static final int MOUSE = 8;
	
	/**
	 * Events of this type indicate that the mouse wheel has moved
	 */
	public static final int WHEEL = 16;
	
	/**
	 * Events of this type indicate that a character has been typed
	 */
	public static final int TYPED = 32;
	
	/**
	 * describes the type of the event. This should be a valid combination of
	 * {@link #CONTROL}, {@link #KEY}, {@link #BUTTON}, {@link #MOUSE} and
	 * {@link #WHEEL}
	 */
	public final int type;

	/**
	 * An array of controls that are mapped to the same key or button that
	 * changed its state. Only for events of {@link #CONTROL} type.
	 */
	public final String[] controls;
	
	/**
	 * The keycode of the key that changed its state. Only for events of the
	 * {@link #KEY} type.
	 */
	public final int key;
	
	/**
	 * The number of the mouse button that changed its state. Only for events of
	 * the {@link #BUTTON} type.
	 */
	public final int button;
	
	/**
	 * Whether the specified key/button/control is active (pressed) after the
	 * change of state or not
	 */
	public final boolean active;
	
	/**
	 * The position of the key that changed its state on the keyboard, i.e.
	 * {@link KeyPosition#STANDARD} for all keys and the left "version" of keys
	 * that appear twice on the keyboard (e.g. left shift),
	 * {@link KeyPosition#RIGHT} for right "versions" of keys (e.g. right shift)
	 * or {@link KeyPosition#NUMPAD} for numpad keys (e.g. numpad enter)
	 * Only for events of the {@link #KEY} type.
	 */
	public final KeyPosition keyPos;

	/**
	 * The new mouse position. Only for events of the {@link #MOUSE} and
	 * {@link #BUTTON} type
	 */
	public final Vector mousePos;
	
	/**
	 * The difference between the old and the new mouse positions. Only for
	 * events of the {@link #MOUSE} type
	 */
	public final Vector deltaPos;
	
	/**
	 * The new mouse wheel position. Only for events of the {@link #MOUSE} type
	 */
	public final double mouseRot;
	
	/**
	 * The difference between the old and the new mouse wheel positions. Only
	 * for events of the {@link #MOUSE} type
	 */
	public final double mouseDeltaRot;
	
	/**
	 * The character that has been typed. Only for events of the {@link #TYPED}
	 * type.
	 */
	public final char character;

	/**
	 * Creates a new {@code InputEvent} of type {@link #CONTROL} and
	 * {@link #KEY}.
	 * @param ctrls the controls that changed state
	 * @param k the key that changed state
	 * @param pos the key's position
	 * @param a the new state
	 */
	public InputEvent(String[] ctrls, int k, KeyPosition pos, boolean a) {
		type = CONTROL | KEY;

		key = k;
		active = a;
		keyPos = pos;
		controls = ctrls;

		button = -1;
		mousePos = null;
		deltaPos = null;
		mouseRot = 0;
		mouseDeltaRot = 0;
		character = 0;
	}

	/**
	 * Creates a new {@code InputEvent} of type {@link #CONTROL} and
	 * {@link #KEY}.
	 * @param ctrls the controls that changed state
	 * @param btn the button that changed state
	 * @param a the new state
	 * @param pos the mouse position
	 */
	public InputEvent(String[] ctrls, int btn, boolean a, Vector pos) {
		type = CONTROL | BUTTON;

		button = btn;
		active = a;
		controls = ctrls;
		mousePos = pos;

		key = -1;
		keyPos = null;
		deltaPos = null;
		mouseRot = 0;
		mouseDeltaRot = 0;
		character = 0;
	}

	/**
	 * Creates a new {@code InputEvent} of type {@link #MOUSE}.
	 * @param mp the new mouse position
	 * @param dp the difference between the old and new mouse positions
	 */
	public InputEvent(Vector mp, Vector dp) {
		type = MOUSE;

		mousePos = mp;
		deltaPos = dp;

		controls = null;
		key = -1;
		button = -1;
		active = false;
		keyPos = null;
		mouseRot = 0;
		mouseDeltaRot = 0;
		character = 0;
	}

	/**
	 * Creates a new {@code InputEvent} of type {@link #WHEEL}.
	 * @param wr the new mouse wheel position
	 * @param dr the difference between the old and new mouse wheel positions
	 * @param pos the mouse position
	 */
	public InputEvent(double wr, double dr, Vector pos) {
		type = WHEEL;

		mouseRot = wr;
		mouseDeltaRot = dr;
		mousePos = pos;

		controls = null;
		key = -1;
		button = -1;
		active = false;
		keyPos = null;
		deltaPos = null;
		character = 0;
	}
	
	/**
	 * Creates a new {@code InputEvent} of type {@link #TYPED}.
	 * @param c the character that was typed
	 */
	public InputEvent(char c) {
		type = TYPED;
		
		character = c;

		mouseRot = 0;
		mouseDeltaRot = 0;
		mousePos = null;
		controls = null;
		key = -1;
		button = -1;
		active = false;
		keyPos = null;
		deltaPos = null;
	}

	/**
	 * Returns true if the event is of the specified type. If an event has
	 * multiple types it returns true, even if not all of them are given.
	 * @param t the type to be checked for
	 * @return true if the event is of the specified type
	 */
	public boolean isType(int t) {
		return (t | type) == type;
	}
}
