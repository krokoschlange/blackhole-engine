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
package blackhole.client.game.input;

import blackhole.client.game.ClientManager;
import blackhole.client.network.ClientEndpoint;
import blackhole.networkData.ClientDataUpdate;
import blackhole.utils.BiMultimap;
import blackhole.utils.Debug;
import blackhole.utils.Settings;
import blackhole.utils.Vector;
import java.awt.AWTException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages Input and control mappings. A control is represented by a string and
 * can be mapped to multiple keys and/or mouse buttons (Also, a single key or
 * mouse button can be mapped to multiple controls). Instead of checking if a
 * speciic key or mouse button is pressed it is then possible to check if the
 * mapped control is active. This allows for dynamic input layouts.
 *
 * Anytime an integer is used in this class to describe a keyboard key it can be
 * offset by 256 to refer to a "right" version of the key (e.g. 272 for right
 * shift instead of 16 for left shift) or offset by 512 for numpad keys.
 *
 * @author fabian
 */
public class InputHandler {

	/**
	 * A class to hold the singleton instance of {@code ClientManager}
	 */
	private static class SingletonHelper {

		private static final InputHandler INSTANCE = new InputHandler();
	}

	/**
	 * The {@link MouseControl} instance used by this {@code InputHandler} to
	 * process mouse input
	 */
	private MouseControl mouseControl;

	/**
	 * The {@link KeyboardInput} instance used by this {@code InputHandler} to
	 * process keyboard input
	 */
	private KeyboardInput keyboardInput;

	/**
	 * The path to the file containing the key/mouse button to control mappings
	 */
	private String filename;

	/**
	 * The {@link Properties} object representing the mapping file specified by
	 * {@link #filename}
	 */
	private Properties symbols;

	/**
	 * The {@link BiMultimap} containing the control mappings
	 */
	private BiMultimap<String, String> symbolMapping;

	/**
	 * An {@link ArrayList} of controls. Events concerning these controls will
	 * sent to the server.
	 */
	private ArrayList<String> sendSymbols;

	/**
	 * If true, all events will be sent to the server. If not, only the ones
	 * specified by {@link #sendSymbols} will be sent.
	 */
	private boolean sendAllEvents;

	/**
	 * If true, no events will be sent to the server at all, not even the ones
	 * specified by {@link #sendSymbols}.
	 */
	private boolean sendNoEvents;

	/**
	 * A list of things that should be sent to the server in the next update.
	 */
	private CopyOnWriteArrayList<String> nextUpdate;

	/**
	 * A list of {@link InputEventListener}s that are subscribed to the
	 * {@code InputHandler}
	 */
	private volatile ArrayList<InputEventListener> listeners;

	/**
	 * Creates a new {@link InputHandler} and initializes the mappings with
	 * mappings from the file specified in the global engine settings.
	 *
	 * @see Settings
	 */
	private InputHandler() {
		symbols = new Properties();
		symbolMapping = new BiMultimap<>();
		filename = "/res/config/input.conf";
		if (Settings.getProperty("intput_config") != null) {
			filename = Settings.getProperty("intput_config");
		}
		try {
			symbols.load(new FileInputStream(ClientManager.getInstance().getBasePath() + filename));

			Set<String> props = symbols.stringPropertyNames();
			Iterator<String> it = props.iterator();
			while (it.hasNext()) {
				String name = it.next();
				String property = symbols.getProperty(name);
				String[] split = property.split(",");
				for (int i = 0; i < split.length; i++) {
					symbolMapping.put(name, split[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		nextUpdate = new CopyOnWriteArrayList<>();

		try {
			mouseControl = new MouseControl(this);
		} catch (AWTException e) {
			mouseControl = null;
		}
		keyboardInput = new KeyboardInput(this);

		listeners = new ArrayList<>();
		sendAllEvents = true;
		sendSymbols = new ArrayList<>();
	}

	/**
	 * Returns the singleton instance
	 *
	 * @return Returns the singleton instance
	 */
	public static InputHandler getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public MouseControl getMouseControl() {
		return mouseControl;
	}

	public KeyboardInput getKeyboardInput() {
		return keyboardInput;
	}

	public BiMultimap<String, String> getSymbolMap() {
		return symbolMapping;
	}

	/**
	 * Returns a list of controls that are mapped to the specified key
	 *
	 * @param key the key of which the mappings should be returned
	 * @return a list of controls
	 */
	public String[] getSymbolsFromKey(int key) {
		String skey = Integer.toString(key);

		List<String> symbols = symbolMapping.getAllKeys(skey);
		if (symbols != null) {
			return symbols.toArray(new String[symbols.size()]);
		}
		return new String[0];
	}

	/**
	 * Returns a list of controls that are mapped to the specified mouse button
	 *
	 * @param button the button of which the mappings should be returned
	 * @return a list of controls
	 */
	public String[] getSymbolsFromMouseButton(int button) {
		String skey = "m" + Integer.toString(button);

		List<String> symbols = symbolMapping.getAllKeys(skey);
		if (symbols != null) {
			return symbols.toArray(new String[symbols.size()]);
		}
		return new String[0];
	}

	/**
	 * Returns a list of keys that are mapped to the specified control
	 *
	 * @param sym the control of which the mappings should be returned
	 * @return a list of keys
	 */
	public int[] getKeysFromSymbol(String sym) {
		List<String> symbols = symbolMapping.getAll(sym);
		int[] keys = new int[symbols.size()];
		for (int i = 0; i < keys.length; i++) {
			if (symbols.get(i).charAt(0) != 'm') {
				keys[i] = Integer.parseInt(symbols.get(i));
			}
		}
		return keys;
	}

	/**
	 * Returns a list of mouse buttons that are mapped to the specified control
	 *
	 * @param sym the control of which the mappings should be returned
	 * @return a list of mouse buttons
	 */
	public int[] getMouseButtonsFromSymbol(String sym) {
		List<String> symbols = symbolMapping.getAll(sym);
		int[] keys = new int[symbols.size()];
		for (int i = 0; i < keys.length; i++) {
			if (symbols.get(i).charAt(0) == 'm') {
				keys[i] = Integer.parseInt(symbols.get(i).substring(1));
			}
		}
		return keys;
	}

	/**
	 * Adds a new mapping for a key to a control
	 *
	 * @param sym the control
	 * @param key the key
	 */
	public void addKeySymbol(String sym, int key) {
		symbolMapping.put(sym, Integer.toString(key));
	}

	/**
	 * Adds a new mapping for a mouse button to a control
	 *
	 * @param sym the control
	 * @param button the mouse button
	 */
	public void addMouseSymbol(String sym, int button) {
		symbolMapping.put(sym, "m" + Integer.toString(button));
	}

	/**
	 * Saves all current control mappings to the file specified by
	 * {@link #filename} (which is set in {@link #InputHandler()})
	 */
	public void saveSymbols() {
		symbols.clear();

		Iterator<String> keyIt = symbolMapping.keySet().iterator();
		while (keyIt.hasNext()) {
			String propname = keyIt.next();
			String prop = "";
			Iterator<String> valIt = symbolMapping.getAll(propname).iterator();
			while (valIt.hasNext()) {
				prop += valIt.next() + ",";
			}
			symbols.setProperty(propname, prop);
		}
		try {
			symbols.store(new FileOutputStream(filename), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true if events concerning this control will be sent to the
	 * server.
	 *
	 * @param sym the control to be checked
	 * @return true if events concerning this control will be sent to the
	 * server.
	 */
	public boolean isSendingSymbol(String sym) {
		if (sendNoEvents) {
			return false;
		}
		if (sendAllEvents) {
			return true;
		}
		return sendSymbols.contains(sym);
	}

	/**
	 * Specifies if events concerning this control should be sent to the server.
	 *
	 * @param sym the control
	 * @param send true if the events should be sent
	 */
	public void setSendSymbol(String sym, boolean send) {
		if (send && !sendSymbols.contains(sym)) {
			sendSymbols.add(sym);
		} else if (!send && sendSymbols.contains(sym)) {
			sendSymbols.remove(sym);
		}
	}

	/**
	 * Sets whether all events should be sent to the server. Can be overridden
	 * by {@link #setSendNoEvents(boolean)}
	 * @param state whether all events should be sent to the server
	 */
	public void setSendAllEvents(boolean state) {
		sendAllEvents = state;
	}

	/**
	 * Returns true if all events are sent to the server. It may be overridden
	 * by {@link #setSendNoEvents(boolean)}.
	 * @return true if all events are sent to the server
	 */
	public boolean isSendAllEvents() {
		return sendAllEvents;
	}

	/**
	 * Sets whether no events should be sent to the server
	 *
	 * @param sendNoEvents whether no events should be sent to the server
	 */
	public void setSendNoEvents(boolean sendNoEvents) {
		this.sendNoEvents = sendNoEvents;
	}

	/**
	 * Returns true if no events are went to the server
	 *
	 * @return true if no events are went to the server
	 */
	public boolean isSendNoEvents() {
		return sendNoEvents;
	}

	/**
	 * Adds a new thing to the update queue
	 * @param name
	 */
	protected void addToUpdate(String name) {
		if (nextUpdate.indexOf(name) == -1) {
			nextUpdate.add(name);
		}/* else {
			if (name.startsWith("mouse_")) {
				nextUpdate.remove(name);
			}
		}*/
	}

	/**
	 * Packages and sends an update according to {@link #sendSymbols} and
	 * {@link #sendAllEvents}
	 */
	public void sendUpdate() {
		ClientDataUpdate update = new ClientDataUpdate();
		update.inputs = new HashMap<>();
		if (!nextUpdate.isEmpty()) {
			for (String name : nextUpdate) {
				if (!sendNoEvents) {
					if (name.equals("mousePos")) {
						update.mousePos = Vector.subtract(
								mouseControl.getMousePosition(),
								ClientManager.getInstance().getServerObjectHandler()
										.getPanel().position);
					} else if (name.equals("mouseWheel")) {
						update.mouseScroll = mouseControl.getWheelRotation();
					} else if (name.startsWith("mouse_")) {
						update.inputs.put(name, mouseControl.getButtonPressed(Integer.parseInt(name.replaceFirst("mouse_", ""))));
					} else if (name.startsWith("key_")) {
						update.inputs.put(name, keyboardInput.getKeyPressed(Integer.parseInt(name.replaceFirst("key_", ""))));
					} else {
						if (isSendingSymbol(name)) {
							update.inputs.put(name, isControlPressed(name));
						}
					}
				}
				nextUpdate.remove(name);
			}
			if (update.inputs.isEmpty()) {
				update.inputs = null;
			}
			ClientEndpoint.getInstance().sendData(update);
		}
	}

	/**
	 * Returns true if the specified control is active (at least one of the
	 * keys/buttons mapped to it are pressed)
	 *
	 * @param sym the control to be checked
	 * @return true if the specified control is active
	 */
	public boolean isControlPressed(String sym) {
		int[] keys = getKeysFromSymbol(sym);
		for (int i = 0; i < keys.length; i++) {
			if (keyboardInput.getKeyPressed(keys[i])) {
				return true;
			}
		}
		int[] buttons = getMouseButtonsFromSymbol(sym);
		for (int i = 0; i < keys.length; i++) {
			if (mouseControl.getButtonPressed(buttons[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Registers a new {@link InputEventListener}. The listener will receive all
	 * events happening.
	 *
	 * @param listener the listener to register
	 */
	public void addListener(InputEventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * Unregisters an {@link InputEventListener}
	 *
	 * @param listener the litener to unregister
	 */
	public void removeListener(InputEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Dispatches an event to all registered listeners
	 *
	 * @param event
	 */
	public void dispatchEvent(InputEvent event) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).handleEvent(event);
		}
	}
}
