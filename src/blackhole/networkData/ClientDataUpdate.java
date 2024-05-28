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

import blackhole.utils.Vector;
import java.util.HashMap;

/**
 * A {@link NetworkUpdate} containing information about the client
 * @author fabian
 */
public class ClientDataUpdate extends NetworkUpdate {
	/**
	 * The game window size on the client
	 */
	public Vector windowSize;
	
	/**
	 * The camera position on the client
	 */
	public Vector camPos;
	
	/**
	 * The camera rotation on the client
	 */
	public Double camRot;
	
	/**
	 * The camera size on the client
	 */
	public Vector camSize;
	
	/**
	 * The mouse wheel position on the client
	 */
	public Double mouseScroll;
	
	/**
	 * The mouse position on the client
	 */
	public Vector mousePos;
	
	/**
	 * The object unload time, i.e. the time after which the client removes
	 * objects if they are out of range
	 */
	public Double unloadTime;
	
	/**
	 * The object unload distance. This is multiplied by the camera size, i.e.
	 * an unload distance of 4 means that objects that are further from the
	 * camera away than 4 times the camera diagonal are unloaded after
	 * {@link #unloadTime} seconds.
	 */
	public Double unloadDistance;
	
	/**
	 * A mapping of input controls and their state. This is not permanently
	 * updated, only when an input event happens.
	 */
	public HashMap<String, Boolean> inputs;
	
	/**
	 * Creates a new, empty {@code ClientDataUpdate}
	 */
	public ClientDataUpdate() {
		super(NetworkUpdate.CLIENT_DATA);
	}
}
