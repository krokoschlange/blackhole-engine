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
package blackhole.server.game;

import blackhole.common.GameObject;
import blackhole.common.ObjectHandler;
import blackhole.networkData.NetworkUpdate;

/**
 * A {@link ObjectHandler} for use on a server
 * @author fabian
 */
public abstract class ServerObjectHandler extends ObjectHandler {

	/**
	 * The ID that will be given to the next object that is added to the handler
	*/
    private int nextObjectID;

	/**
	 * Creates a new, empty {@code ServerObjectHandler}
	 */
    public ServerObjectHandler() {
        nextObjectID = 0;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
    public int getNewObjectID() {
        nextObjectID++;
        return nextObjectID;
    }

	/**
	 * {@inheritDoc}
	 * This implementation does nothing though.
	 */
	@Override
	public void handleUpdate(NetworkUpdate update) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public abstract void init();

	/**
	 * {@inheritDoc}
	 */
	@Override
    public abstract void step(double dtime);

	/**
	 * Called when a client connects to the server
	 * @param client the client that connected
	 */
    public abstract void clientConnect(Client client);

	/**
	 * Called when a client disconnects from the server
	 * @param client the client that disconnected
	 */
    public abstract void clientDisconnect(Client client);

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void gameStep(double dtime) {
        super.gameStep(dtime);
        for (GameObject object : getObjects()) {
            if (object.getPhysicsStrategy() == null) {
                object.integratePosition(dtime);
            }
            object.step(dtime);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		super.clear();
		//immediately update all clients to make sure the objects are getting removed
		ClientHandler.getInstance().update();
	}
	
	
}
