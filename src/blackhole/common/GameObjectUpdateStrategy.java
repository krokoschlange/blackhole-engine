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
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author fabian
 */
public class GameObjectUpdateStrategy implements UpdateStrategy {

	private GameObject object;

	private HashMap<String, AccessPair> parameters;

	private static class AccessPair {

		public Consumer setter;
		public Supplier getter;
		public boolean includeInUpdateAll;

		public AccessPair(Consumer set, Supplier get, boolean updateAll) {
			setter = set;
			getter = get;
			includeInUpdateAll = updateAll;
		}
	}

	public GameObjectUpdateStrategy() {
		parameters = new HashMap<>();

		addParameter("position", (p) -> {
			object.setPosition((Vector) p);
		}, () -> {
			return object.getPosition();
		});
		addParameter("rotation", (p) -> {
			object.setRotation((Double) p);
		}, () -> {
			return object.getRotation();
		});
		addParameter("scale", (p) -> {
			object.setScale((Vector) p);
		}, () -> {
			return object.getScale();
		});
		addParameter("velocity", (p) -> {
			object.setVelocity((Vector) p);
		}, () -> {
			return object.getVelocity();
		});
		addParameter("angularVelocity", (p) -> {
			object.setAngularVelocity((Double) p);
		}, () -> {
			return object.getAngularVelocity();
		});
		addParameter("visibility", (p) -> {
			object.setVisible((Boolean) p);
		}, () -> {
			return object.isVisible();
		});
		addParameter("drawPosition", (p) -> {
			object.setDrawPositions((ArrayList<Integer>) p);
		}, () -> {
			return object.getDrawPositions();
		});
		addParameter("interpolate", (p) -> {
			object.setInterpolate((Boolean) p);
		}, () -> {
			return object.getInterpolate();
		});
		addParameter("parent", (p) -> {
			object.setParent(object.getHandler().getObjectByID((Integer) p));
		}, () -> {
			return object.getParent() == null ? null : object.getParent().getID();
		});
	}

	@Override
	public void setObject(GameObject obj) {
		object = obj;
	}

	@Override
	public GameObject getObject() {
		return object;
	}

	public void addParameter(String name, Consumer setter, Supplier getter) {
		addParameter(name, setter, getter, true);
	}

	public void addParameter(String name, Consumer setter, Supplier getter, boolean includeInUpdateAll) {
		parameters.put(name, new AccessPair(setter, getter, includeInUpdateAll));
	}

	public void removeParameter(String name) {
		parameters.remove(name);
	}

	public void clearAllParameters() {
		parameters.clear();
	}

	@Override
	public ObjectUpdate getUpdate(ObjectUpdate update) {
		ArrayList<String> updateData = object.getUpdateData();
		Iterator<Map.Entry<String, AccessPair>> it = parameters.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, AccessPair> entry = it.next();
			if (updateData.contains(entry.getKey())) {
				update.data.put(entry.getKey(), entry.getValue().getter.get());
			}
		}

		return update;
	}

	@Override
	public ObjectUpdate getUpdateAll(ObjectUpdate update) {
		Iterator<Map.Entry<String, AccessPair>> it = parameters.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, AccessPair> entry = it.next();
			if (entry.getValue().includeInUpdateAll) {
				update.data.put(entry.getKey(), entry.getValue().getter.get());
			}
		}

		return update;
	}

	@Override
	public void update(ObjectUpdate update) {
		HashMap<String, Object> data = update.data;
		if (data == null) {
			return;
		}

		Iterator<Map.Entry<String, AccessPair>> it = parameters.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, AccessPair> entry = it.next();
			if (data.containsKey(entry.getKey())) {
				entry.getValue().setter.accept(data.get(entry.getKey()));
			}
		}
	}
}
