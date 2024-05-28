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

/**
 * A framebuffer is a fast drawing surface used for dynamic textures and other
 * effects.
 * @author fabian.baer2
 */
public interface AbstractFramebuffer {
    
	/**
	 * Returns the width of the framebuffer in pixels
	 * @return the width of the framebuffer in pixels
	 */
    public int getWidth();
	
	/**
	 * Returns the height of the framebuffer in pixels
	 * @return the height of the framebuffer in pixels
	 */
    public int getHeight();
    
	/**
	 * Returns the data stored in the framebuffer
	 * @return the data stored in the framebuffer
	 */
    public Object getData();
    
	/**
	 * Creates a texture from the buffer's contents
	 * @return a texture containing the buffers contents
	 */
    public GameTexture toTexture();
    
	/**
	 * Returns true if the buffer is ready for use
	 * @return true if the buffer is ready for use
	 */
    public boolean isReady();
}
