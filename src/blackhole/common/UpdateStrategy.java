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
package blackhole.common;

import blackhole.networkData.ObjectUpdate;

/**
 * An {@link ObjectStrategy} that handles {@link ObjectUpdate}s for an object
 * @author fabian
 */
public interface UpdateStrategy extends ObjectStrategy {

	/**
	 * Extends the given update by properties the strategy controls.
	 * The strategy decides whether to add a property to the update or not.
	 * This is usually done by calling
	 * {@code getObject().getUpdateData().contains(property)}
	 * @param update the update to extend
	 * @return the update after the extension
	 */
	public abstract ObjectUpdate getUpdate(ObjectUpdate update);

	/**
	 * Extends the given update by all of the properties the strategy controls.
	 * @param update the update to extend
	 * @return the update after the extension
	 */
	public abstract ObjectUpdate getUpdateAll(ObjectUpdate update);

	/**
	 * Updates the strategy's object according to the give update
	 * @param update the update to update the object with
	 */
	public abstract void update(ObjectUpdate update);
}
