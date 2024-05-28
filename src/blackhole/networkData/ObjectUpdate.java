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

import blackhole.common.GameObject;
import blackhole.common.UpdateStrategy;
import java.util.HashMap;

/**
 * A {@link NetworkUpdate} describing the state of a {@link GameObject}
 * @author fabian
 */
public class ObjectUpdate extends NetworkUpdate {
	/**
	 * the object's ID
	 */
	public int objectID;
	/**
	 * A mapping of parameter names and their values. This has to be evaluated
	 * by an {@link UpdateStrategy}
	 */
	public HashMap<String, Object> data;
	
	/**
	 * Creates a new {@code ObjectUpdate} for the given object ID. The data is
	 * meant to be filled by {@link UpdateStrategy}s
	 * @param id the ID
	 */
	public ObjectUpdate(int id) {
		super(NetworkUpdate.OBJECT_UPDATE);
		objectID = id;
	}
}
