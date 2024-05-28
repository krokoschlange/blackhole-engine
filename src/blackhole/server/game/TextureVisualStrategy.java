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
import blackhole.common.UpdateStrategy;
import blackhole.networkData.ObjectUpdate;
import blackhole.networkData.updateData.TextureUpdate;
import blackhole.server.data.TextureData;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An {@link UpdateStrategy} used toconfigure the visual appearance of a
 * {@link ServerObject} using a {@link TextureData}.
 * @author fabian
 */
public class TextureVisualStrategy implements UpdateStrategy {
	
	/**
	 * the object that the strategy affects
	 */
	private GameObject object;
	
	/**
	 * the {@link TextureData} representing the visual appearance
	 */
	private TextureData texture;

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
	 * Sets the texture of the {@link ServerObject}
	 * @param tex the new {@link TextureData}
	 */
	public void setTexture(TextureData tex) {
		texture = tex;
		object.addToUpdate("texture");
	}
	
	/**
	 * Sets the texture of the {@link ServerObject}
	 * @param tex the name of/path to the texture
	 */
	public void setTexture(String tex) {
		texture = new TextureData(tex);
		object.addToUpdate("texture");
	}
	
	/**
	 * Returns a {@link TextureData} describing the currently used texture
	 * @return a {@link TextureData} describing the currently used texture
	 */
	public TextureData getTexture() {
		return texture;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectUpdate getUpdate(ObjectUpdate update) {
		ArrayList<String> updateData = object.getUpdateData();
		if (updateData.contains("texture")) {
			if (getTexture() != null) {
				update.data.put("texture", getTexture().getUpdateData());
			}
		}
		return update;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectUpdate getUpdateAll(ObjectUpdate update) {
		update.data.put("texture", getTexture().getUpdateData());
		return update;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(ObjectUpdate update) {
		HashMap<String, Object> data = update.data;
		if (data != null) {
			if (data.containsKey("texture")) {
				TextureUpdate texData = (TextureUpdate) data.get("texture");
				TextureData tex = new TextureData(texData.name);
				tex.setOffset(texData.offset);
				tex.setRotationOffset(texData.rotationOffset);
				/*for (int i = 0; i < texData.filters.length; i++) {
					tex.addFilter(texData.filters[i].name, texData.filters[i].args, i);
				}*/
				setTexture(tex);
			}
		}
	}
	
}
