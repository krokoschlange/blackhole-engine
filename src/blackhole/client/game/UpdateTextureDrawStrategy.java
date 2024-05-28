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

import blackhole.client.graphicsEngine.GameDrawable;
import blackhole.common.UpdateStrategy;
import blackhole.networkData.ObjectUpdate;
import blackhole.networkData.updateData.AnimationUpdate;
import blackhole.networkData.updateData.DrawableUpdate;
import blackhole.networkData.updateData.TextureUpdate;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements a {@link DrawStrategy} that draws a {@link GameDrawable} at the
 * object's position. This implementation is capable of being updated by the
 * server.
 *
 * @author fabian
 */
public class UpdateTextureDrawStrategy extends TextureDrawStrategy implements UpdateStrategy {

	/**
	 * {@inheritDoc} This implementation adds the "texture" parameter (which
	 * should contain the path to the texture that this strategy should draw)
	 */
	@Override
	public ObjectUpdate getUpdate(ObjectUpdate update) {
		ArrayList<String> updateData = getObject().getUpdateData();
		if (updateData.contains("texture")) {
			if (getTexture() != null) {
				GameDrawable tex = getTexture();
				TextureUpdate tupdt = new TextureUpdate();
				tupdt.name = tex.getName();
				tupdt.offset = tex.getOffset();
				tupdt.rotationOffset = tex.getRotOffset();
				update.data.put("texture", tupdt);
			}
		}
		return update;
	}

	/**
	 * {@inheritDoc} This implementation adds the "texture" parameter (which
	 * should contain the path to the texture that this strategy should draw)
	 */
	@Override
	public ObjectUpdate getUpdateAll(ObjectUpdate update) {
		if (getTexture() != null) {
			GameDrawable tex = getTexture();
			TextureUpdate tupdt = new TextureUpdate();
			tupdt.name = tex.getName();
			tupdt.offset = tex.getOffset();
			tupdt.rotationOffset = tex.getRotOffset();
			update.data.put("texture", tupdt);
		}
		return update;
	}

	/**
	 * {@inheritDoc} This implementation receives the "texture" parameter (which
	 * should contain the path to the texture that this strategy should draw)
	 */
	@Override
	public void update(ObjectUpdate update) {
		HashMap<String, Object> data = update.data;
		if (data == null) {
			return;
		}
		if (data.containsKey("texture")) {
			DrawableUpdate texData = (DrawableUpdate) data.get("texture");

			if (texData instanceof AnimationUpdate) {
				setTexture(ClientManager.getInstance().getGraphicsBackend().createGameAnimation((AnimationUpdate) texData));
			} else if (texData instanceof TextureUpdate) {
				setTexture(ClientManager.getInstance().getGraphicsBackend().createGameTexture((TextureUpdate) texData));
			}
		}
	}

	@Override
	public String toString() {
		if (getTexture() != null) {
			return "UpdateTextureDrawStrategy: " + getTexture().getName();
		}
		return "UpdateTextureDrawStrategy: null";
	}

}
