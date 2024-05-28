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

import blackhole.client.game.ClientObjectHandler;
import blackhole.client.graphicsEngine.Camera;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author fabian
 */
public class SoundCore {

	private static class SingletonHelper {

		private static final SoundCore INSTANCE = new SoundCore();
	}

	public static final float samplerate = 48000;

	public static final double speedOfSound = 343;
	public static final double soundDecay = 0.001;

	private static final double dopplerFactor = 1;

	private double camHeight;

	private Thread loop;

	private SourceDataLine sourceLine;
	private AudioFormat outFormat;

	private CopyOnWriteArrayList<ClientObjectHandler> handlers;

	//private HashMap<PositionalSound, Double> playbackPos;
	private SoundCore() {
		outFormat = new AudioFormat(samplerate, 16, 2, true, false);
		try {
			sourceLine = AudioSystem.getSourceDataLine(outFormat);
			sourceLine.open();
			sourceLine.start();
		} catch (LineUnavailableException e) {
			Debug.logError("Audio module initialization failed: " + e.getMessage());
		}
		handlers = new CopyOnWriteArrayList<>();

		camHeight = 0.4;
		if (Settings.getProperty("cam_height") != null) {
			try {
				camHeight = Double.parseDouble(Settings.getProperty("cam_height"));
			} catch (NumberFormatException e) {

			}
		}
	}

	public static SoundCore getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public int[] getMonoAudioData(String filename) {
		try {
			File file = new File(filename);
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);

			AudioFormat format = new AudioFormat(samplerate, 16, 1, true, false);
			if (!AudioSystem.isConversionSupported(format, ais.getFormat())) {
				Debug.logError("Could not convert sound data: " + filename);
				return null;
			}
			AudioInputStream newFormatAIS = AudioSystem.getAudioInputStream(format, ais);
			long frames = newFormatAIS.getFrameLength();
			long size = frames * format.getFrameSize();
			byte[] buffer = new byte[(int) size];

			newFormatAIS.read(buffer, 0, buffer.length);

			int[] pcmData = new int[(int) frames];
			for (int i = 0; i < frames; i++) {
				pcmData[i] = ((buffer[i * 2]) & 0xff) | ((buffer[i * 2 + 1]) << 8);
			}
			return pcmData;
		} catch (IOException e) {
			Debug.logError("Error opening audio file:");
			Debug.logError(e.getMessage());
		} catch (UnsupportedAudioFileException e) {
			Debug.logError("Error reading audio file:");
			Debug.logError(e.getMessage());
		}
		return null;
	}

	public void addPositionalSound(PositionalSound snd, ClientObjectHandler handler) {
		if (!handlers.contains(handler)) {
			handlers.add(handler);
		}
		handler.addSound(snd);
		Debug.log("HANDLERS: " + handlers.size());
	}

	public void removePositionalSound(PositionalSound snd) {
		for (int i = 0; i < handlers.size(); i++) {
			handlers.get(i).removeSound(snd);
		}
	}

	public void removePositionalSound(PositionalSound snd, ClientObjectHandler handler) {
		handler.removeSound(snd);
	}

	public void updateSounds(double dtime) {
		for (int i = 0; i < handlers.size(); i++) {
			CopyOnWriteArrayList<PositionalSound> sounds = handlers.get(i).getSounds();
			for (int j = 0; j < sounds.size(); j++) {
				sounds.get(j).step(dtime);
			}
		}
	}

	public void soundLoop() {
		loop = new Thread("sound loop") {
			@Override
			public void run() {
				int frameBufferSize = 256;
				double looptime = 1e9 / (samplerate / frameBufferSize);
				double delta = 0;
				double time = System.nanoTime();
				double last = System.nanoTime();
				double extratime = 0;
				int[] buffer = new int[frameBufferSize * 2];
				while (!loop.isInterrupted()) {
					time = System.nanoTime();
					delta += time - last;
					last = time;

					try {
						if (extratime > 0) {
							loop.sleep((long) (extratime * 0.75 / 1e6));
							extratime = 0;
						}
					} catch (InterruptedException e) {
						break;
					}

					if (delta >= looptime) {
						delta -= looptime;

						Arrays.fill(buffer, 0);

						for (ClientObjectHandler handler : handlers) {
							Camera camera = handler.getCamera();
							CopyOnWriteArrayList<PositionalSound> sounds = handler.getSounds();
							for (int j = 0; j < sounds.size(); j++) {
								PositionalSound snd = sounds.get(j);
								Vector srcToCam = Vector.subtract(camera.getPosition(), snd.getPosition());
								double srcSpeed = Vector.dot(srcToCam, snd.getVelocity()) / srcToCam.magnitude();
								double camSpeed = Vector.dot(srcToCam, camera.getVelocity()) / srcToCam.magnitude();
								camSpeed = Math.min(camSpeed, speedOfSound);

								double dopplerEffect = (speedOfSound - dopplerFactor * camSpeed)
										/ (speedOfSound - dopplerFactor * srcSpeed);
								double deltaPos = dopplerEffect / snd.getData().length;
								double gain = 1;

								/*double panAngle = Math.atan2(-srcToCam.x(), -srcToCam.y()) - camera.getRotation();
                                if (panAngle > Math.PI / 2) {
                                    panAngle = Math.PI - panAngle;
                                } else if (panAngle < -Math.PI / 2) {
                                    panAngle = -Math.PI - panAngle;
                                }
                                panAngle /= 2;*/
								double panAngle = Math.asin(-srcToCam.x()
										/ Math.sqrt(srcToCam.x() * srcToCam.x()
												+ srcToCam.y() * srcToCam.y()
												+ camHeight * camHeight))
										/ 2;

								double panFactorL = (Math.sqrt(2) / 2.0) * (Math.cos(panAngle) - Math.sin(panAngle));
								double panFactorR = (Math.sqrt(2) / 2.0) * (Math.cos(panAngle) + Math.sin(panAngle));
								for (int k = 0; k < frameBufferSize; k++) {
									snd.setPlaybackPos(snd.getPlaybackPos() + deltaPos);
									double currentPos = snd.getPlaybackPos();
									if (currentPos > 1) {
										removePositionalSound(snd, handler);
										break;
									}

									int intPos = (int) (currentPos * snd.getData().length);
									int smpl1 = snd.getData()[intPos];
									int smpl2 = 0;
									smpl2 = intPos + 1 >= snd.getData().length ? snd.getData()[0] : snd.getData()[intPos + 1];

									int smpl = smpl1 + (int) ((smpl2 - smpl1) * (currentPos * snd.getData().length - intPos));

									buffer[k * 2] += smpl * gain * panFactorL;
									buffer[k * 2 + 1] += smpl * gain * panFactorR;
								}
							}
						}

						byte[] outBuffer = new byte[buffer.length * 2]; //bufferlenght * sample size in bytes
						for (int i = 0; i < buffer.length; i++) {
							int val = buffer[i];

							outBuffer[i * 2] = (byte) (val & 0xff);
							outBuffer[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
						}

						sourceLine.write(outBuffer, 0, outBuffer.length);
						if (!sourceLine.isActive()) {
							sourceLine.start();
						}
						extratime = looptime - (System.nanoTime() - time);
					}
				}
				sourceLine.stop();
				sourceLine.close();
			}
		};
		loop.start();
	}

	public void stopLoop() {
		loop.interrupt();
	}
}
