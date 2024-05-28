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

import blackhole.client.graphicsEngine.AbstractDrawer;
import blackhole.client.graphicsEngine.GameDrawable;
import blackhole.common.GameObject;

/**
 * Implements a {@link DrawStrategy} that draws a {@link GameDrawable} at the
 * object's position
 *
 * @author fabian
 */
public class TextureDrawStrategy implements DrawStrategy {

	/**
	 * The object
	 */
	private GameObject object;

	/**
	 * The {@link GameDrawable} that is drawn
	 */
	private GameDrawable texture;

	@Override
	public void setObject(GameObject obj) {
		object = obj;
	}

	@Override
	public GameObject getObject() {
		return object;
	}

	public void setTexture(GameDrawable tex) {
		texture = tex;
	}

	public GameDrawable getTexture() {
		return texture;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This implementation draws {@link #texture} when the object's list of
	 * draw positions (layers) contains drawPos
	 * @param drawPos the layer to be drawn on
	 */
	@Override
	public void draw(int drawPos) {
		try {
			if (object.getDrawPositions().contains(drawPos) && object.isVisible() && getTexture() != null) {
				AbstractDrawer drawer = ClientManager.getInstance().getGraphicsBackend().getDrawer();
				ClientObjectHandler chandler = (ClientObjectHandler) object.getHandler();

				drawer.drawTexture(texture, 1, object.getRealPosition(),
						object.getRealRotation(),
						object.getRealScale(), chandler.getCamera(),
						chandler.getPanel(), chandler.getScale());

			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
}
