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

import blackhole.client.game.ClientObjectHandler;
import blackhole.utils.Vector;
import java.awt.Color;

/**
 * Describes a region inside the {@link AbstractGameWindow} that the contents of
 * the associated {@link ClientObjectHandler}'s contents are rendered to.
 * The frame that the handler's camera produces is stretched onto the handler's
 * panel
 * @author fabian
 */
public class HandlerPanel {

	/**
	 * the size of the panel, in window coordinates
	 */
    public Vector size;
	
	/**
	 * the position of the panel, in window coordinates
	 */
    public Vector position;
	
	/**
	 * the background color of the panel. It may have an alpha channel
	 */
    public Color background;

	/**
	 * Creates a new {@code HandlerPanel}
	 */
    public HandlerPanel() {
    }

}
