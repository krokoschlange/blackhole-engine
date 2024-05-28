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

import blackhole.client.network.ClientEndpoint;
import blackhole.server.network.ClientSocket;
import java.io.Serializable;

/**
 * A container for information to pass between the server ({@link ClientSocket})
 * and the client ({@link ClientEndpoint})
 * @author fabian
 */
public abstract class NetworkUpdate implements Serializable {
	
	/**
	 * update type constantW
	 */
	public static final int OBJECT_UPDATE = 1,
			OBJECT_SPAWN = 2,
			OBJECT_REMOVAL = 3,
			CLIENT_DATA = 4,
			SERVER_DATA = 5,
			UNLOAD_UPDATE = 6;
	
	/**
	 * the update type, must be one of the update type constants
	 */
	public final int updateType;
	
	/**
	 * Creates a new {@code NetworkUpdate} of the given type
	 * @param type the type of the update to be created
	 */
	public NetworkUpdate(int type) {
		updateType = type;
	}
}
