/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.utils;

import java.util.Locale;

/**
 *
 * @author fabian
 */
public class OSChecker {
	public enum OSType {
		WINDOWS,
		LINUX,
		MAC_OS,
		OTHER
	}
	
	private static OSType os;
	
	public static OSType getOSType() {
		if (os == null) {
			String osString = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if (osString.contains("mac") || osString.contains("darwin")) {
				os = OSType.MAC_OS;
			} else if (osString.contains("win")) {
				os = OSType.WINDOWS;
			} else if (osString.contains("nux")) {
				os = OSType.LINUX;
			} else {
				os = OSType.OTHER;
			}
		}
		return os;
	}
}
