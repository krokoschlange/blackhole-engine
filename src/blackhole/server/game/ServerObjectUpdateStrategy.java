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

import blackhole.client.game.ServerSentStrategy;
import blackhole.common.GameObject;
import blackhole.common.UpdateStrategy;
import blackhole.networkData.ObjectUpdate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@link UpdateStrategy} for {@link ServerObject}s. It usually communicates
 * with a {@link ServerSentStrategy} on the client. It handles the property
 * whether an object is always loaded and should not be unloaded on the client.
 * @author fabian
 */
public class ServerObjectUpdateStrategy implements UpdateStrategy {
	
	/**
	 * the object that the strategy affects
	 */
	private GameObject object;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setObject(GameObject obj) {
		object = obj;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public GameObject getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectUpdate getUpdate(ObjectUpdate update) {
		ArrayList<String> updateData = object.getUpdateData();
		ServerObject sobj = (ServerObject) object;
		/*if (updateData.contains("texture")) {
			if (sobj.getTexture() != null) {
				update.data.put("texture", sobj.getTexture().getUpdateData());
			}
		}*/
		if (updateData.contains("alwaysLoaded")) {
			update.data.put("alwaysLoaded", sobj.getAlwaysLoaded());
		}
		return update;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectUpdate getUpdateAll(ObjectUpdate update) {
		ServerObject sobj = (ServerObject) object;
		update.data.put("alwaysLoaded", sobj.getAlwaysLoaded());
		return update;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ObjectUpdate update) {
		HashMap<String, Object> data = update.data;
		if (data != null) {
			/*if (data.containsKey("texture")) {
				TextureUpdate texData = (TextureUpdate) data.get("texture");
				TextureData tex = new TextureData(texData.name);
				tex.setOffset(texData.offset);
				tex.setRotationOffset(texData.rotationOffset);
				tex.useBuffer(texData.useBuffer);
				for (int i = 0; i < texData.filters.length; i++) {
					tex.addFilter(texData.filters[i].name, texData.filters[i].args, i);
				}
				((ServerObject) object).setTexture(tex);
			}*/
			if (data.containsKey("alwaysLoaded")) {
				((ServerObject) object).setAlwaysLoaded((boolean) data.get("alwaysLoaded"));
			}
		}
	}
	
}
