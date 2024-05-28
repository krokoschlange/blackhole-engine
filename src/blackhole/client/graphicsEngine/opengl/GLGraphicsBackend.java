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

import blackhole.client.graphicsEngine.GraphicsBackend;
import blackhole.client.game.ClientManager;
import blackhole.client.graphicsEngine.AbstractGameWindow;
import blackhole.networkData.updateData.AnimationUpdate;
import blackhole.networkData.updateData.TextureUpdate;
import blackhole.utils.Debug;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The implementation of a {@link GraphicsBackend} for an OpenGL-based graphics
 * engine.
 *
 * @author fabian
 */
public class GLGraphicsBackend implements GraphicsBackend {

	/**
	 * the GLProfile to be used
	 */
	private GLProfile glProfile;

	/**
	 * the GLCapabilities
	 */
	private GLCapabilities glCaps;

	/**
	 * The game window
	 */
	private AbstractGameWindow window;

	/**
	 * the drawer
	 */
	private GLDrawer drawer;

	/**
	 * a map containing all loaded images. They are stored as weak references to
	 * allow the garbage collection to remove them if they are unused.
	 */
	private HashMap<String, WeakReference<Texture>> images;

	/**
	 * Some operations need to happen on the render thread due to opengl
	 * limitations. The operations are stored in this list until the render
	 * thread processes them.
	 */
	private List<Event> events;

	/**
	 * Creates a new {@code GLGraphicsBackend}
	 *
	 * @param useAWTBackend whether to use an AWT-based game window
	 */
	public GLGraphicsBackend(boolean useAWTBackend) {
		glProfile = GLProfile.getDefault();
		glCaps = new GLCapabilities(glProfile);

		if (useAWTBackend) {
			window = new GLAWTGameWindow(this);
		} else {
			window = new GLGameWindow(this);
		}
		drawer = new GLDrawer(this);

		images = new HashMap<>();

		events = new CopyOnWriteArrayList<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractGameWindow getWindow() {
		return window;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLDrawer getDrawer() {
		return drawer;
	}

	protected GLProfile getGLProfile() {
		return glProfile;
	}

	protected GLCapabilities getGLCapabilities() {
		return glCaps;
	}

	/**
	 * An operation that needs to happen on the render thread
	 */
	private abstract class Event {

		public abstract void process();
	}

	/**
	 * called on each frame, processes all {@link Event}s
	 */
	protected void processEvents() {
		Iterator<Event> it = events.iterator();
		events.clear();
		while (it.hasNext()) {
			it.next().process();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadImage(String path) {
		loadImage(path, false);
	}

	/**
	 * Loads an image with or without mipmaps
	 *
	 * @param path the image path
	 * @param mipmap whether to generate mipmaps
	 */
	public void loadImage(String path, boolean mipmap) {
		Event event = new Event() {
			@Override
			public void process() {
				try {
					Texture tex = TextureIO.newTexture(new File(ClientManager.getInstance().getBasePath() + path), mipmap);
					images.replace(path, new WeakReference<>(tex));
				} catch (IOException e) {
					Debug.logError(path + ": image file not found");
				}
			}
		};
		images.put(path, null);
		events.add(event);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unloadImage(String path) {
		if (images.containsKey(path)) {
			Event event = new Event() {
				@Override
				public void process() {
					if (images.get(path) != null && images.get(path).get() != null) {
						images.get(path).get().destroy(drawer.getGL());
						images.remove(path);
					}
				}
			};
			events.add(event);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Texture getImage(String path) {
		if (!images.containsKey(path) || images.get(path) == null || images.get(path).get() == null) {
			loadImage(path);
		}
		while ((!images.containsKey(path) || images.get(path) == null)) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				break;
			}
		}
		return images.get(path).get();
	}

	/**
	 * Returns a {@link GLGameFont} with the specified characteristics. It is
	 * used by {@link GLCurveStringDrawer} to generate OpenGL curve fonts.
	 *
	 * @param name the font name
	 * @param style the font style
	 * @param size the font size
	 * @return a {@link GLGameFont} with the specified characteristics
	 *
	 * @see Font
	 */
	public GLGameFont getFont(String name, int style, int size) {
		return new GLGameFont(name, style, size);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLGameTexture createGameTexture(String path) {
		Texture tex = getImage(path);

		return new GLGameTexture(tex, path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLGameTexture createGameTexture(TextureUpdate update) {
		return new GLGameTexture(this, update);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLGameAnimation createGameAnimation(ArrayList<String> paths) {
		ArrayList<Texture> textures = new ArrayList<>();
		for (int i = 0; i < paths.size(); i++) {
			textures.add(getImage(paths.get(i)));
		}
		return new GLGameAnimation(textures);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GLGameAnimation createGameAnimation(AnimationUpdate update) {
		return new GLGameAnimation(this, update);
	}
}
