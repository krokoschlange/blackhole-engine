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
package blackhole.networkData;

/**
 * A {@link NetworkUpdate} describing that an object has been unloaded on a
 * client and that it needs to be sent again if it should be loaded again
 * (e.g. if the camera of that client gets closer to the object again)
 * @author fabian
 */
public class UnloadUpdate extends NetworkUpdate {
	/**
	 * the ID of the object that got unloaded
	 */
	public int objectID;
	
	/**
	 * Creates a new {@code UnloadUpdate} for the given object ID
	 * @param id the ID
	 */
	public UnloadUpdate(int id) {
		super(NetworkUpdate.UNLOAD_UPDATE);
		objectID = id;
	}
}
