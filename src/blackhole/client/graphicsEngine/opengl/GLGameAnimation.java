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
package blackhole.client.graphicsEngine.opengl;

import blackhole.client.game.ClientManager;
import blackhole.client.graphicsEngine.GameAnimation;
import blackhole.networkData.updateData.AnimationUpdate;
import blackhole.utils.Vector;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import java.util.ArrayList;

/**
 * Implementation of {@link GameAnimation} for use with {@link GLDrawer}
 *
 * @author fabian
 */
public class GLGameAnimation extends GameAnimation implements GLGameDrawable {

	/**
	 * A list of frames
	 */
	private ArrayList<Texture> data;

	/**
	 * Creates a new, empty {@code GLGameAnimation}
	 */
	public GLGameAnimation() {
		data = new ArrayList<>();
	}

	/**
	 * Creates a new {@code GLGameAnimation} containing the specified images
	 *
	 * @param textures the images
	 */
	public GLGameAnimation(ArrayList<Texture> textures) {
		this();
		data.addAll(textures);
	}

	/**
	 * Creates a new {@code GLGameAnimation} from the given
	 * {@link AnimationUpdate}
	 *
	 * @param backend the {@link GLGraphicsBackend}
	 * @param update the update
	 */
	public GLGameAnimation(GLGraphicsBackend backend, AnimationUpdate update) {
		this();
		for (int i = 0; i < update.paths.length; i++) {
			addFrame(backend.getImage(update.paths[i]));
		}
		setOffset(update.offset);
		setRotOffset(update.rotationOffset);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setData(ArrayList d) {
		try {
			data = d;
		} catch (ClassCastException e) {
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayList getData() {
		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFrameAmount() {
		return data.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFrame(Object frame) {
		addFrame(frame, data.size());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFrame(Object frame, int num) {
		if (frame instanceof Texture) {
			num = Math.max(0, Math.min(num, data.size()));
			data.add(num, (Texture) frame);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFrame(int num) {
		if (num >= 0 && num < data.size()) {
			data.remove(num);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return data.get(getFrameNumber()).getWidth();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return data.get(getFrameNumber()).getHeight();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Texture getDrawData() {
		return data.get(getFrameNumber());
	}
}
