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

import blackhole.client.game.ClientManager;
import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.networkData.updateData.AnimationUpdate;
import blackhole.networkData.updateData.TextureUpdate;
import blackhole.utils.Debug;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * The {@link GraphicsBackend} for a graphics engine based on Java2D
 *
 * @author fabian.baer2
 */
public class J2DGraphicsBackend implements GraphicsBackend {

	/**
	 * The game window
	 */
	private J2DGameWindow window;

	/**
	 * the drawer
	 */
	private J2DDrawer drawer;

	/**
	 * a map containing all loaded images. They are stored as weak references to
	 * allow the garbage collection to remove them if they are unused.
	 */
	private HashMap<String, WeakReference<BufferedImage>> images;

	/**
	 * Creates a new {@code J2DGraphicsBackend}
	 */
	public J2DGraphicsBackend() {
		images = new HashMap<>();
		window = new J2DGameWindow(this);
		drawer = new J2DDrawer(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DGameWindow getWindow() {
		return window;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DDrawer getDrawer() {
		return drawer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadImage(String path) {
		loadImage(path, Transparency.BITMASK);

	}

	/**
	 * Loads an image with the specified transparency mode
	 *
	 * @param path the path to the image
	 * @param transparency the transparency mode
	 * @see Transparency
	 */
	public void loadImage(String path, int transparency) {
		try {
			BufferedImage bimg = ImageIO.read(new File(ClientManager.getInstance().getBasePath() + path));

			images.put(path, new WeakReference<>(bimg));
		} catch (IOException e) {
			Debug.log(e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void unloadImage(String path) {
		if (images.containsKey(path)) {
			images.get(path).get().flush();
			images.remove(path);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getImage(String path) {
		if (!images.containsKey(path) || (images.get(path) != null && images.get(path).get() == null)) {
			loadImage(path);
		}
		return images.get(path).get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DGameTexture createGameTexture(String path) {
		return new J2DGameTexture(getImage(path), path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DGameTexture createGameTexture(TextureUpdate update) {
		return new J2DGameTexture(this, update);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DGameAnimation createGameAnimation(ArrayList<String> paths) {
		ArrayList<Image> textures = new ArrayList<>();
		for (int i = 0; i < paths.size(); i++) {
			textures.add(getImage(paths.get(i)));
		}
		return new J2DGameAnimation(textures);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public J2DGameAnimation createGameAnimation(AnimationUpdate update) {
		return new J2DGameAnimation(this, update);
	}

}
