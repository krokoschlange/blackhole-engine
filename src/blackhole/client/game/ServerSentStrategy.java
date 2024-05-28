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

import blackhole.client.graphicsEngine.Camera;
import blackhole.client.network.ClientEndpoint;
import blackhole.common.GameObject;
import blackhole.common.UpdateStrategy;
import blackhole.networkData.ObjectUpdate;
import blackhole.networkData.UnloadUpdate;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class implements the default update and unload behavior for objects sent
 * by the server. It manages whether an object is never unloaded (the
 * "alwaysLoaded" parameter) and when it unloads the object. This is done based
 * on the distance of the object to the camera. If it gets too big, a timer
 * starts running and when that hits a certain time, the object gets removed. If
 * the camera gets close again, the timer is reset.
 *
 * @author fabian
 */
public class ServerSentStrategy implements UpdateStrategy, UnloadStrategy {

	/**
	 * The object that this strategy affects
	 */
	private GameObject object;

	/**
	 * The timer that starts running when the objcet is too far away
	 */
	private double unloadingTimer;

	/**
	 * When the timer hits this time, the object gets removed
	 */
	private double unloadTime;

	/**
	 * When the object is this distance away from the camera, the timer starts
	 * running. Measured in multiples of the camera's diagonal
	 */
	private double objectSendingRange;

	/**
	 * Whether the object should not be unloaded
	 */
	private boolean alwaysLoaded;

	public ServerSentStrategy() {
		unloadingTimer = 0;

		//load settings
		if (Settings.getProperty("object_sending_distance") != null) {
			objectSendingRange = Double.parseDouble(Settings.getProperty("object_sending_range"));
		} else {
			objectSendingRange = 4;
		}
		if (Settings.getProperty("object_unload_time") != null) {
			unloadTime = Double.parseDouble(Settings.getProperty("object_unload_time"));
		} else {
			unloadTime = 10;
		}
	}

	@Override
	public void setObject(GameObject obj) {
		object = obj;
	}

	@Override
	public GameObject getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation adds the "alwaysLoaded" parameter
	 */
	@Override
	public ObjectUpdate getUpdate(ObjectUpdate update) {
		ArrayList<String> updateData = object.getUpdateData();
		if (updateData.contains("alwaysLoaded")) {
			update.data.put("alwaysLoaded", getAlwaysLoaded());
		}
		return update;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation adds the "alwaysLoaded" parameter
	 */
	@Override
	public ObjectUpdate getUpdateAll(ObjectUpdate update) {
		update.data.put("alwaysLoaded", getAlwaysLoaded());
		return update;
	}

	/**
	 * {@inheritDoc}
	 *
	 * This implementation sets the "alwaysLoaded" parameter
	 */
	@Override
	public void update(ObjectUpdate update) {
		HashMap<String, Object> data = update.data;
		if (data == null) {
			return;
		}
		if (data.containsKey("alwaysLoaded")) {
			setAlwaysLoaded((boolean) data.get("alwaysLoaded"));
		}
	}

	public double getUnloadingTimer() {
		return unloadingTimer;
	}

	public void setUnloadingTimer(double t) {
		unloadingTimer = t;
	}

	public double getUnloadTime() {
		return unloadTime;
	}

	public double getObjectSendingRange() {
		return objectSendingRange;
	}

	public boolean getAlwaysLoaded() {
		return alwaysLoaded;
	}

	public void setAlwaysLoaded(boolean state) {
		alwaysLoaded = state;
	}

	/**
	 * Called every game step. Checks whether the object is too far away. If
	 * yes, starts the unloading timer and removes the object when the time runs
	 * out.
	 *
	 * @param dtime time since the last game step
	 */
	@Override
	public void unload(double dtime) {
		if (!alwaysLoaded) {
			ClientObjectHandler chandler = (ClientObjectHandler) object.getHandler();
			Camera cam = chandler.getCamera();
			Vector cameraPos = cam.getPosition();
			Vector cameraSize = new Vector(cam.getWidth(), cam.getHeight());
			Vector objPos = object.getRealPosition();
			Vector dist = Vector.subtract(cameraPos, objPos);
			double distance = Math.sqrt(dist.x() * dist.x() + dist.y() * dist.y());
			double cameraRadius = Math.sqrt(cameraSize.x() * cameraSize.x() + cameraSize.y() * cameraSize.y()) / chandler.getScale();

			double width = 1;
			double height = 1;

			ClientObject cobj = (ClientObject) object;
			DrawStrategy drawStrat = cobj.getDrawStrategy();
			if (drawStrat != null && (drawStrat instanceof TextureDrawStrategy)) {
				TextureDrawStrategy textureDrawStrategy = (TextureDrawStrategy) drawStrat;

				if (textureDrawStrategy.getTexture() != null) {
					width = textureDrawStrategy.getTexture().getWidth() * object.getRealScale().x() / chandler.getScale();
					height = textureDrawStrategy.getTexture().getHeight() * object.getRealScale().y() / chandler.getScale();
				}
			}

			double objectUnloadRadius = Math.sqrt(width * width + height * height);
			if (objectUnloadRadius + cameraRadius * getObjectSendingRange() < distance) {
				setUnloadingTimer(getUnloadingTimer() + dtime);
				if (getUnloadingTimer() > getUnloadTime()) {
					UnloadUpdate unldupdt = new UnloadUpdate(object.getID());
					ClientEndpoint.getInstance().sendData(unldupdt);
					object.remove();
					remove();
				}
			} else {
				setUnloadingTimer(0);
			}
		}
	}

}
