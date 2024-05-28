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
package blackhole.client.graphicsEngine;

import blackhole.networkData.updateData.AnimationUpdate;
import blackhole.networkData.updateData.TextureUpdate;
import java.util.ArrayList;

/**
 * An interface used for easy access to the graphics engine. It also has to
 * cache all the needed textures.
 * @author fabian
 */
public interface GraphicsBackend {

	/**
	 * Returns the game window used by the {@code GraphicsBackend}
	 * @return the game window used by the {@code GraphicsBackend}
	 */
	public AbstractGameWindow getWindow();
	
	/**
	 * Returns the drawer window used by the {@code GraphicsBackend}
	 * @return the drawer window used by the {@code GraphicsBackend}
	 */
	public AbstractDrawer getDrawer();
	
	/**
	 * Loads an image
	 * @param path the path to the image
	 */
	public void loadImage(String path);
	
	/**
	 * Destroys the resources used by the specified texture to free up memory
	 * @param path the path to the image
	 */
	public void unloadImage(String path);
	
	/**
	 * Returns the data of the specified image. It calls
	 * {@link #loadImage(java.lang.String)} if the specified image has not been
	 * loaded yet.
	 * @param path the path to the image
	 * @return the data of the image. The type depends on the backend that is
	 * used
	 */
	public Object getImage(String path);
	
	/**
	 * Creates a new {@link GameTexture} from the specified image. It loads the
	 * image first if it is not already loaded.
	 * @param path the path to the image
	 * @return a new {@link GameTexture} containing the image
	 */
	public GameTexture createGameTexture(String path);
	
	/**
	 * Creates a new {@link GameTexture} from a {@link TextureUpdate}. It loads
	 * the image first if it is not already loaded.
	 * @param update the update
	 * @return a new {@link GameTexture}, set up according to the update
	 */
	public GameTexture createGameTexture(TextureUpdate update);
	
	/**
	 * Creates a new {@link GameAnimation} from the specified images. It loads
	 * the images first if they are not already loaded.
	 * @param paths the paths to the images
	 * @return a new {@link GameAnimation} containing the images
	 */
	public GameAnimation createGameAnimation(ArrayList<String> paths);
	
	/**
	 * Creates a new {@link GameAnimation} from an {@link AnimationUpdate}.
	 * It loads the images first if they are not already loaded.
	 * @param update the update
	 * @return a new {@link GameAnimation}, set up according to the update
	 */
	public GameAnimation createGameAnimation(AnimationUpdate update);
	
}
