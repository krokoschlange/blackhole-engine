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

import blackhole.client.game.ClientObject;
import blackhole.common.GameObject;
import blackhole.common.GameObjectUpdateStrategy;
import blackhole.common.ObjectStrategy;
import blackhole.utils.Vector;
import java.util.ArrayList;

/**
 * Class representing a {@link GameObject} on the Server
 * @author fabian
 */
public abstract class ServerObject extends GameObject {

	/**
	 * The class that should be used to instanciate the object on the client
	 */
	private Class<?> clientObjectClass;

	/**
	 * Whether the object should always be loaded, no matter the distance to
	 * the camera of a client
	 */
	private boolean alwaysLoaded;
	
	/**
	 * Whether the object should not be sent to clients. Overrides
	 * {@link #alwaysLoaded}
	 */
	private boolean serverOnly;
	
	/**
	 * Whether to use a list of clients which should load the object, others
	 * should not
	 */
	private boolean includeClients;
	
	/**
	 * The list of clients that should load the object if
	 * {@link #includeClients} is {@code true}
	 */
	private ArrayList<Client> includedClients;
	
	/**
	 * Whether to use a list of clients which should not load the object.
	 */
	private boolean excludeClients;
	
	/**
	 * The list of clients that should not load the object if
	 * {@link #excludeClients} is {@code true}
	 */
	private ArrayList<Client> excludedClients;
	
	/**
	 * The {@link ObjectStrategy} that is used to handle the visual appearance
	 * of the game object
	 */
	private ObjectStrategy visualStrategy;

	/**
	 * Creates a new {@code ServerObject}
	 */
	public ServerObject() {
		alwaysLoaded = false;
		serverOnly = false;

		includeClients = false;
		includedClients = new ArrayList<>();
		excludeClients = false;
		excludedClients = new ArrayList<>();
		
		addUpdateStrategy(new GameObjectUpdateStrategy());
		addUpdateStrategy(new ServerObjectUpdateStrategy());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPosition(double x, double y) {
		addToUpdate("position");
		super.setPosition(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPosition(Vector pos) {
		addToUpdate("position");
		super.setPosition(pos);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRotation(double rot) {
		addToUpdate("rotation");
		super.setRotation(rot);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScale(double x, double y) {
		addToUpdate("scale");
		super.setScale(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setScale(Vector sc) {
		addToUpdate("scale");
		super.setScale(sc);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVelocity(Vector vel) {
		addToUpdate("velocity");
		super.setVelocity(vel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAngularVelocity(double alpha) {
		addToUpdate("angularVelocity");
		super.setAngularVelocity(alpha);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDrawPosition(int p) {
		addToUpdate("drawPosition");
		super.setDrawPosition(p);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDrawPositions(ArrayList<Integer> pos) {
		addToUpdate("drawPosition");
		super.setDrawPositions(pos);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDrawPosition(int p) {
		if (!getDrawPositions().contains(p)) {
			addToUpdate("drawPosition");
			super.addDrawPosition(p);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeDrawPosition(int p) {
		if (getDrawPositions().contains(p)) {
			addToUpdate("drawPosition");
			super.removeDrawPosition(p);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVisible(boolean state) {
		addToUpdate("visibility");
		super.setVisible(state);
	}
	
	/**
	 * Sets the {@link ObjectStrategy} used to handle the visual appearance of#
	 * the object
	 * @param vstrat the visual strategy to use
	 */
	public void setVisualStrategy(ObjectStrategy vstrat) {
		if (visualStrategy != null) {
			visualStrategy.remove();
		}
		visualStrategy = vstrat;
		if (visualStrategy != null) {
			visualStrategy.activate(this);
		}
	}
	
	/**
	 * Returns the {@link ObjectStrategy} used to handle the visual appearance of#
	 * the object
	 * @return the visual strategy
	 */
	public ObjectStrategy getVisualStrategy() {
		return visualStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInterpolate(boolean interpolate) {
		addToUpdate("interpolate");
		super.setInterpolate(interpolate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParent(GameObject obj, boolean keepGlobalTransform) {
		addToUpdate("parent");
		super.setParent(obj, keepGlobalTransform);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParent(GameObject obj) {
		addToUpdate("parent");
		super.setParent(obj);
	}

	/**
	 * Sets whether the object should always be loaded, no matter the distance
	 * to the camera of a client
	 * @param state true if it should always be loaded
	 */
	public void setAlwaysLoaded(boolean state) {
		alwaysLoaded = state;
		addToUpdate("alwaysLoaded");
	}

	/**
	 * Returns true if the object is always loaded, no matter the distance to
	 * the camera of a client
	 * @return true if the object is always loaded
	 */
	public boolean getAlwaysLoaded() {
		return alwaysLoaded;
	}

	/**
	 * Returns true if the object is not sent to clients
	 * @return true if the object is not sent to clients
	 */
	public boolean getServerOnly() {
		return serverOnly;
	}

	/**
	 * Sets whether the object should not be sent to clients
	 * @param state the object should not be sent to clients
	 */
	public void setServerOnly(boolean state) {
		serverOnly = state;
		if (state) {
			includeClients = false;
			excludeClients = false;
		}
	}

	/**
	 * Sets whether the object should only be sent to a list of clients
	 * @param state the object should only be sent to a list of clients
	 * 
	 * @see #addClientToInclude(blackhole.server.game.Client)
	 * @see #removeClientFromInclude(blackhole.server.game.Client)
	 */
	public void useIncludeClients(boolean state) {
		includeClients = state;
		if (state) {
			excludeClients = false;
			serverOnly = false;
		}
	}

	/**
	 * Returns true if the object is only sent to a list of clients
	 * @return true if the object is only sent to a list of clients
	 */
	public boolean getIncludeClients() {
		return includeClients;
	}

	/**
	 * Sets whether the object should not be sent to a list of clients
	 * @param state the object should not be sent to a list of clients
	 * 
	 * @see #addClientToExlude(blackhole.server.game.Client)
	 * @see #removeClientFromExclude(blackhole.server.game.Client)
	 */
	public void useExcludeClients(boolean state) {
		excludeClients = state;
		if (state) {
			includeClients = false;
			serverOnly = false;
		}
	}

	/**
	 * Returns true if the object is not sent to a list of clients
	 * @return true if the object is not sent to a list of clients
	 */
	public boolean getExcludeClients() {
		return excludeClients;
	}

	/**
	 * Adds a client to the list of clients the object is sent to if
	 * {@link #includeClients} is {@code true}
	 * @param c the client to add
	 */
	public void addClientToInclude(Client c) {
		if (!includedClients.contains(c)) {
			includedClients.add(c);
		}
	}

	/**
	 * Removes a client from the list of clients the object is sent to if
	 * {@link #includeClients} is {@code true}
	 * @param c the client to remove
	 */
	public void removeClientFromInclude(Client c) {
		if (includedClients.contains(c)) {
			includedClients.remove(c);
		}
	}

	/**
	 * Returns a list of clients the object is sent to if
	 * {@link #includeClients} is {@code true}
	 * @return a list of clients the object is sent to if
	 * {@link #includeClients} is {@code true}
	 */
	public ArrayList<Client> getIncludedClients() {
		return includedClients;
	}

	/**
	 * Returns true if the given client is included in the list of clients the
	 * object is sent to if {@link #includeClients} is {@code true}
	 * @param c the client to check
	 * @return true if the object is sent to the client even if
	 * {@link #includeClients} is {@code true}
	 */
	public boolean getClientIncluded(Client c) {
		return includedClients.contains(c);
	}

	/**
	 * Adds a client to the list of clients the object is not sent to if
	 * {@link #excludeClients} is {@code true}
	 * @param c the client to add
	 */
	public void addClientToExlude(Client c) {
		if (!excludedClients.contains(c)) {
			excludedClients.add(c);
		}
	}

	/**
	 * Removes a client from the list of clients the object is not sent to if
	 * {@link #excludeClients} is {@code true}
	 * @param c the client to remove
	 */
	public void removeClientFromExclude(Client c) {
		if (excludedClients.contains(c)) {
			excludedClients.remove(c);
		}
	}

	/**
	 * Returns a list of clients the object is not sent to if
	 * {@link #excludeClients} is {@code true}
	 * @return a list of clients the object is not sent to if
	 * {@link #excludeClients} is {@code true}
	 */
	public ArrayList<Client> getExcludedClients() {
		return excludedClients;
	}

	/**
	 * Returns true if the given client is included in the list of clients the
	 * object is not sent to if {@link #excludeClients} is {@code true}
	 * @param c the client to check
	 * @return true if the object is not sent to the client when
	 * {@link #excludeClients} is {@code true}
	 */
	public boolean getClientExcluded(Client c) {
		return excludedClients.contains(c);
	}

	/**
	 * Sets the class that should be used to instanciate the object on the
	 * client
	 * @param cls the class to be used
	 */
	public void setClientObjectClass(Class<?> cls) {
		clientObjectClass = cls;
	}

	/**
	 * Returns the class that should be used to instanciate the object on the
	 * client
	 * @param c the {@link Client} that called the method during the update
	 * process. May be {@code null}. Can be used to instantiate objects of
	 * different classes on different clients by overriding the method and
	 * returning different classes depending on this parameter
	 * @return the class that should be used to instanciate the object on the
	 * client
	 */
	public Class<?> getClientObjectClass(Client c) {
		if (clientObjectClass == null) {
			return ClientObject.class;
		}
		return clientObjectClass;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void step(double dtime);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract void init();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String str = "\n";
		//str = str + "Texture: " + getTexture() + "\n";
		str = str + "Visibility: " + isVisible() + "\n";
		str = str + "Position: \n" + getPosition().toString() + "\n";
		str = str + "Scale: \n" + getScale().toString() + "\n";
		str = str + "Rotation: " + getRotation() + "\n";
		//str = str + "DrawPos: " + getDrawPositions() + "\n";
		str = str + "ObjHandler: " + getHandler() + "\n";
		str = str + "ID: " + getID() + "\n";
		str = str + "Parent: " + getParent();
		return str;
	}
}
