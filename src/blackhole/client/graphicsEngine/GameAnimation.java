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

import java.util.ArrayList;

/**
 * A {@link GameDrawable} that has multiple frames and can loop through them
 * @author fabian
 */
public abstract class GameAnimation extends GameDrawable {
    
	/**
	 * the index of the current frame
	 */
    private int frame;
    
	/**
	 * the frame rate of the animation
	 */
    private double frameRate;
    
	/**
	 * a ttimer used to calculate the current frame
	 */
    private double time;

	/**
	 * Sets the frames to use
	 * @param d an {@link ArrayList} of frames
	 */
    public abstract void setData(ArrayList d);

	/**
	 * Returns a list of all frames
	 * @return a list of all frames
	 */
    public abstract ArrayList getData();
    
	/**
	 * Returns the amount of frames this animation has
	 * @return the amount of frames
	 */
    public abstract int getFrameAmount();

	/**
	 * Adds a new frame
	 * @param frame the frame to add
	 */
    public abstract void addFrame(Object frame);
	
	/**
	 * Adds a new frame at the given position
	 * @param frame the frame to add
	 * @param num the position
	 */
    public abstract void addFrame(Object frame, int num);
    
	/**
	 * Removes the given frame
	 * @param num the index of the frame to remove
	 */
    public abstract void removeFrame(int num);
    
	/**
	 * Returns the current frame index
	 * @return the current frame index
	 */
    public int getFrameNumber() {
        return frame;
    }
    
	/**
	 * Sets the current frame index
	 * @param f the frame index
	 */
    public void setFrameNumber(int f) {
        int amount = getFrameAmount();
        frame = ((f % amount) + amount) % amount;
    }
    
	/**
	 * moves the animation to the next frame
	 */
    public void nextFrame() {
        setFrameNumber(frame + 1);
    }
    
	/**
	 * Sets the frame rate
	 * @param fps the new frame rate
	 */
    public void setFrameRate(double fps) {
        frameRate = fps;
    }
    
	/**
	 * Returns the frame rate
	 * @return the frame rate
	 */
    public double getFrameRate() {
        return frameRate;
    }
    
	/**
	 * Increases the timer and sets current frame accordingly. This method is
	 * not called automatically
	 * @param dtime the time since the last call
	 */
    public void update(double dtime) {
        time += dtime;
        
        setFrameNumber((int) (time * frameRate));
    }
}
