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

import blackhole.server.game.Client;

/**
 * An interface which is meant to be used to write listeners for
 * {@link InputEvent}s. Can be used on the client and on the server.
 * @author fabian.baer2
 * 
 * @see InputHandler#addListener(blackhole.client.game.input.InputEventListener)
 * @see InputHandler#removeListener(blackhole.client.game.input.InputEventListener)
 * @see Client#addEventListener(blackhole.client.game.input.InputEventListener) 
 * @see Client#removeEventListener(blackhole.client.game.input.InputEventListener) 
 */
public interface InputEventListener {
	/**
	 * This method is called when any input event occurs.
	 * @param event the event that ocurred
	 */
    public void handleEvent(InputEvent event);
}
