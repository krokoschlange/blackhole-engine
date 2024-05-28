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
package blackhole.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A container class for a single {@link Properties} instance for access
 * throughout the engine
 * @author fabian
 */
public class Settings {

    private static Properties SETTINGS;

	/**
	 * Loads a properties file
	 * @param filename the path to the file to load
	 */
	public static void setup(String filename) {
        SETTINGS = new Properties();
        try {
            SETTINGS.load(new FileInputStream(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Debug.log("Settings loaded.");
    }

	/**
	 * Returns the value of a property
	 * @param name the name of the property
	 * @return the value of the specified property
	 */
	public static String getProperty(String name) {
        return SETTINGS.getProperty(name);
    }

	/**
	 * Sets the value of a property
	 * @param name the name of the property
	 * @param value the value to set the property to
	 */
    public static void setProperty(String name, String value) {
        SETTINGS.setProperty(name, value);
    }
}
