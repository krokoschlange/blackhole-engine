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
package blackhole.client.game;

import blackhole.common.GameObject;
import blackhole.common.GameObjectUpdateStrategy;
import blackhole.common.ObjectHandler;
import blackhole.utils.Debug;

/**
 * Class representing a {@link GameObject} on the Client
 * @author fabian
 */
public class ClientObject extends GameObject {

	/**
	 * The object's draw strategy (how it draws itself)
	 * @see DrawStrategy
	 */
	private DrawStrategy drawStrategy;
	
	
	/**
	 * The object's unloading strategy (only for server-sent objects, unloads
	 * them when certain conditions are met)
	 * @see UnloadStrategy
	 */
	private UnloadStrategy unloadStrategy;
	
	/**
	 * If the parent object cannot be found, its ID will be stored here to be
	 * able to find the object later.
	 */
	private Integer wantedParentID;
	
	/**
	 * Creates a new {@code ClientObject}
	 */
	public ClientObject() {
		
	}

	/**
	 * Set the {@link DrawStrategy} of the object. Removes the old
	 * {@link DrawStrategy} if present
	 * @param drawStrat the new {@link DrawStrategy}
	 */
	public void setDrawStrategy(DrawStrategy drawStrat) {
		if (drawStrategy != null) {
			drawStrategy.remove();
		}
		drawStrategy = drawStrat;
		if (drawStrategy != null) {
			drawStrategy.activate(this);
		}
	}

	/**
	 * Returns the objects {@link DrawStrategy}
	 * @return the objects {@link DrawStrategy}
	 */
	public DrawStrategy getDrawStrategy() {
		return drawStrategy;
	}
	
	/**
	 * Set the {@link UnloadStrategy} of the object. Removes the old
	 * {@link UnloadStrategy} if present
	 * @param unloadStrat the new {@link UnloadStrategy}
	 */
	public void setUnloadStrategy(UnloadStrategy unloadStrat) {
		if (unloadStrategy != null) {
			unloadStrategy.remove();
		}
		unloadStrategy = unloadStrat;
		if (unloadStrategy != null) {
			unloadStrategy.activate(this);
		}
	}

	/**
	 * Returns the objects {@link UnloadStrategy}
	 * @return the objects {@link UnloadStrategy}
	 */
	public UnloadStrategy getUnloadStrategy() {
		return unloadStrategy;
	}

	public void setWantedParentID(Integer id) {
		wantedParentID = id;
	}

	public Integer getWantedParentID() {
		return wantedParentID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setParent(GameObject obj) {
		super.setParent(obj);
		wantedParentID = null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This implementation also checks for {@link #wantedParentID}
	 */
	@Override
	public GameObject getParent() {
		GameObject parentObj = super.getParent();
		if (parentObj == null && wantedParentID != null && getHandler() != null) {
			parentObj = getHandler().getObjectByID(wantedParentID);
			if (parentObj != null) {
				setParent(parentObj);
			}
		}
		return parentObj;
	}

	/**
	 * {@inheritDoc}
	 * This implementation makes use of {@link #wantedParentID}
	 */
	@Override
	public GameObjectUpdateStrategy getDefaultUpdateStrategy() {
		for (int i = 0; i < getUpdateStrategies().size(); i++) {
			if (getUpdateStrategies().get(i) instanceof GameObjectUpdateStrategy) {
				return (GameObjectUpdateStrategy) getUpdateStrategies().get(i);
			}
		}
		GameObjectUpdateStrategy strat = new GameObjectUpdateStrategy();
		strat.addParameter("parent", (p) -> {
			GameObject parentObj = getHandler().getObjectByID((Integer) p);
			if (parentObj != null) {
				setParent(parentObj);
			} else {
				setParent(null);
				wantedParentID = (Integer) p;
				
			}
		}, () -> {
			return getParent() == null ? null : getParent().getID();
		});
		addUpdateStrategy(strat);
		return strat;
	}
	
	/**
	 * Set the object's handler and id. Used for server-sent objects where the
	 * id is provided by the server instead of the client's object handler
	 * @param handler the handler to be set
	 * @param id the id to be used
	 */
	public void setHandler(ObjectHandler handler, int id) {
		setHandler(handler);
		setID(id);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This is here to be able to instantiate objects of this class as it is
	 * the default class used for server-sent objects
	 * @see GameObject#step(double)
	 */
	@Override
	public void step(double dtime) {

	}
	
	/**
	 * Calls the draw method of the object's {@link DrawStrategy}.
	 * Should only be called from within the render thread
	 * to make sure that all rendering recources are available.
	 * @param drawPos the layer to be drawn
	 */
	public void draw(int drawPos) {
		if (getDrawStrategy() != null) {
			getDrawStrategy().draw(drawPos);
		}
	}
	
	/**
	 * Calls the unload method of the object's {@link UnloadStrategy}.
	 * @param dtime time since the last call
	 */
	public void unload(double dtime) {
		if (getUnloadStrategy() != null) {
			getUnloadStrategy().unload(dtime);
		}
	}

	/**
	 * This method is called just before everything is rendered. Can be
	 * overridden to do actions on each render frame.
	 * @param dtime time since the last frame
	 */
	public void onDraw(double dtime) {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {

	}

	/**
	 * Summarizes the object
	 * @return a String containing information about the object
	 */
	@Override
	public String toString() {
		String str = "\n";
		
		str = str + "OBJECT: " + super.toString() + "\n";
		str = str + "DrawStrat: " + getDrawStrategy()+ "\n";
		str = str + "Visibility: " + isVisible() + "\n";
		str = str + "Position: \n" + getPosition().toString() + "\n";
		str = str + "Scale: \n" + getScale().toString() + "\n";
		str = str + "Rotation: " + getRotation() + "\n";
		str = str + "DrawPos: ";
		for (int i = 0; i < getDrawPositions().size(); i++) {
			str = str + getDrawPositions().get(i) + ", ";
		}
		str = str + "\n";
		str = str + "ObjHandler: " + getHandler() + "\n";
		str = str + "ID: " + getID() + "\n";
		str = str + "Parent: " + getParent() + "\n";
		return str;
	}
}
