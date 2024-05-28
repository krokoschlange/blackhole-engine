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
import blackhole.networkData.ClientDataUpdate;
import blackhole.utils.Vector;
import java.util.ArrayList;

/**
 * Describes the perspective from which the game world is rendered
 * @author fabian
 */
public class Camera {

	/**
	 * the position of the camera
	 */
	private Vector position;
	
	/**
	 * the position of the camera in the last game step
	 */
	private Vector oldPosition;
	
	/**
	 * the velocity of the camera
	 */
	private Vector velocity;
	
	/**
	 * the rotation of the camera
	 */
	private double rotation;
	
	/**
	 * the width of the camera, in game coordinates scaled by the game world
	 * scale ({@link ClientObjectHandler#getScale()})
	 */
	private int width;

	/**
	 * the height of the camera, in game coordinates scaled by the game world
	 * scale ({@link ClientObjectHandler#getScale()})
	 */
	private int height;
	
	/**
	 * An {@link ArrayList} containing parameters the the server needs to be
	 * updated about
	 */
	private ArrayList<String> updates;

	/**
	 * Creates a new camera at the origin
	 */
	public Camera() {
		position = new Vector();
		oldPosition = new Vector();
		velocity = new Vector();
		updates = new ArrayList<>();
	}

	public Vector getPosition() {
		return position;
	}

	public void setPosition(int x, int y) {
		position.set(x, y);
		updates.add("position");
	}

	public void setPosition(Vector pos) {
		position.set(pos);
		updates.add("position");
	}

	public Vector getVelocity() {
		return velocity;
	}

	public void setRotation(double rot) {
		rotation = rot;
		updates.add("rotation");
	}

	public double getRotation() {
		return rotation;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Vector getSize() {
		return new Vector(width, height);
	}

	public void setWidth(int w) {
		width = w;
		updates.add("size");
	}

	public void setHeight(int h) {
		height = h;
		updates.add("size");
	}

	/**
	 * Called on every game step, used to calculate the velocity of the camera
	 * @param dtime the time since the last step
	 */
	public void step(double dtime) {
		velocity = Vector.subtract(position, oldPosition).divide(dtime);
		oldPosition = position;
	}

	/**
	 * Returns a {@link ClientDataUpdate} containing parameters that changed
	 * since the last call
	 * @return a {@link ClientDataUpdate}
	 */
	public ClientDataUpdate getUpdate() {
		ClientDataUpdate update = new ClientDataUpdate();
		boolean updated = false;
		if (updates.contains("position")) {
			update.camPos = getPosition();
			updated = true;
		}
		if (updates.contains("rotation")) {
			update.camRot = getRotation();
			updated = true;
		}
		if (updates.contains("size")) {
			update.camSize = new Vector(width, height);
			updated = true;
		}
		clearUpdate();
		if (updated) {
			return update;
		}
		return null;
	}

	/**
	 * Clears the list of things that the next update needs to contain
	 */
	private void clearUpdate() {
		updates.clear();
	}
}
