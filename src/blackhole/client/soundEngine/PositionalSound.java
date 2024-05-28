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
package blackhole.client.soundEngine;

import blackhole.client.game.ClientObject;
import blackhole.utils.Vector;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author fabian.baer2
 */
public class PositionalSound {

    private ClientObject object;
    private Vector position;
    private Vector velocity;

    private boolean loop;

    private double playbackPos;

    private int[] sound;

    public PositionalSound(int[] data) {
        object = null;
        position = new Vector();
        velocity = new Vector();
        loop = false;

        sound = data;
        playbackPos = 0;
    }

    public PositionalSound(ClientObject obj, int[] data) {
        object = obj;
        sound = data;
        loop = false;

        position = object.getRealPosition();
        velocity = new Vector();
    }

    public PositionalSound(Vector pos, Vector vel, int[] data) {
        position = pos;
        velocity = vel;
        loop = false;

        sound = data;
        object = null;
    }

    public int[] getData() {
        return sound;
    }

    public void setObject(ClientObject obj) {
        object = obj;
    }

    public ClientObject getObject() {
        return object;
    }

    public void setPosition(Vector pos) {
        position = pos;
    }

    public Vector getPosition() {
        if (object != null) {
            return object.getPosition();
        } else {
            return position;
        }
    }

    public void setVelocity(Vector v) {
        velocity = v;
    }

    public Vector getVelocity() {
        if (object != null) {
            return object.getVelocity();
        } else {
            return velocity;
        }
    }

    public void setLoop(boolean state) {
        loop = state;
    }

    public boolean getLoop() {
        return loop;
    }

    public void setPlaybackPos(double p) {
        playbackPos = p;
        if (playbackPos > 1 && loop) {
            playbackPos = playbackPos - (int) playbackPos;
        }
    }

    public double getPlaybackPos() {
        return playbackPos;
    }

    public void step(double dtime) {
        position.add(Vector.multiply(velocity, dtime));
    }
}
