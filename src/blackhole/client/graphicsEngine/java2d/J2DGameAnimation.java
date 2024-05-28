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
package blackhole.client.graphicsEngine.java2d;

import blackhole.client.graphicsEngine.GameAnimation;
import blackhole.networkData.updateData.AnimationUpdate;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.util.ArrayList;

/**
 * Implementation of {@link GameAnimation} for use with {@link J2DDrawer}
 * @author fabian
 */
public class J2DGameAnimation extends GameAnimation implements J2DGameDrawable {

	/**
	 * A list of frames
	 */
	private ArrayList<Image> data;

	/**
	 * Creates a new, empty {@code J2DGameAnimation}
	 */
	public J2DGameAnimation() {
		data = new ArrayList<>();
	}

	/**
	 * Creates a new {@code J2DGameAnimation} containing the specified images
	 * @param textures the images
	 */
	public J2DGameAnimation(ArrayList<Image> textures) {
		this();
		data.addAll(textures);
	}

	/**
	 * Creates a new {@code J2DGameAnimation} from the given
	 * {@link AnimationUpdate}
	 * @param backend the {@link J2DGraphicsBackend}
	 * @param update the update
	 */
	public J2DGameAnimation(J2DGraphicsBackend backend, AnimationUpdate update) {
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
		if (frame instanceof Image) {
			data.add((Image) frame);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFrame(Object frame, int num) {
		if (frame instanceof Image) {
			data.add(num, (Image) frame);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFrame(int num) {
		data.remove(num);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth() {
		return data.get(getFrameNumber()).getWidth(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHeight() {
		return data.get(getFrameNumber()).getHeight(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Image getDrawData() {
		return data.get(getFrameNumber());
	}
}
