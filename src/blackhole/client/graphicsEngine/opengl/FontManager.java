/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackhole.client.graphicsEngine.opengl;

import blackhole.utils.Debug;
import blackhole.utils.OSChecker;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.font.FontSet;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * When using {@link GLCurveStringDrawer} we need JOGL font objects instead of
 * AWT font objects.
 * Since JOGL Fonts cannot simply be loaded by calling the constructor, we need
 * to load the corresponding ttf file and create the font object from that.
 * The {@code FontManager} is responsible for detecting and loading all fonts it
 * can find on the system. 
 * @author fabian
 */
public class FontManager {

	/**
	 * A class to hold the singleton instance of {@code FontManager}
	 */
	private static class SingletonHolder {

		private static FontManager INSTANCE = new FontManager();
	}

	/**
	 * A class set of fonts of the same family with different style bits
	 */
	private static class LazyFontSet implements FontSet {

		/**
		 * A map containing mappings between stylebits and the associated fonts
		 */
		private HashMap<Integer, Font> fonts;

		/**
		 * Creates a new, empty font set
		 */
		public LazyFontSet() {
			fonts = new HashMap<>();
		}

		/**
		 * Returns the default font of the font family
		 * @return the default font of the font family
		 * @throws IOException 
		 */
		@Override
		public Font getDefault() throws IOException {
			return get(0, 0);
		}

		/**
		 * Returns the font with the specified style bits or the default font,
		 * if no font with the specified style bits is known
		 * @param family unused
		 * @param stylebits the stylebits if the requested font
		 * @return the font with the specified style bits
		 * @throws IOException 
		 */
		@Override
		public Font get(int family, int stylebits) throws IOException {
			Font f = fonts.get(stylebits);
			if (f == null) {
				f = fonts.get(0);
			}
			return f;
		}

		/**
		 * Adds a new font to the family
		 * @param f the font
		 * @param style the stylebits of the font
		 */
		public void addFont(Font f, int style) {
			if (!fonts.containsKey(style)) {
				fonts.put(style, f);
			}
		}
	}

	/**
	 * A mapping of font names and the font sets
	 */
	private HashMap<String, LazyFontSet> fontMap;

	/**
	 * Creates a new {@code FontManager}
	 */
	private FontManager() {
		fontMap = null;
	}

	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static FontManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Returns the font with the given name and stylebits or the UBUNTU standard
	 * font if there is no font known with the given name or null if even that
	 * fails.
	 * If {@link #loadFonts()} has not yet been called, it will be called
	 * automatically
	 * 
	 * @param name the name of the requested font
	 * @param style the stylebits of the requested font
	 * @return the font with the given name and stylebits
	 */
	public Font getFont(String name, int style) {
		if (fontMap == null) {
			loadFonts();
		}
		try {
			Debug.log("GET FONT " + name);
			return fontMap.get(name).get(0, style);
		} catch (Exception e) {
			Debug.log("font not found: " + name);
			try {
				return FontFactory.get(FontFactory.UBUNTU).get(0, 0);
			} catch (IOException e2) {
				e.printStackTrace();
				return null;
			}
			
		}
	}

	/**
	 * loads all fonts that it can find on the system and saves name-fontset and
	 * style-font mappings.
	 */
	public void loadFonts() {
		fontMap = new HashMap<>();
		
		ArrayList<File> files = getFontFiles();

		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			java.awt.Font f = null;
			try {
				f = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, file);
				String name = f.getFamily();
				int style = f.getStyle();
				
				LazyFontSet fontSet;
				if (!fontMap.containsKey(name)) {
					fontSet = new LazyFontSet();
					fontMap.put(name, fontSet);
				} else {
					fontSet = fontMap.get(name);
				}
				
				fontSet.addFont(FontFactory.get(file), style);
				
			} catch (FontFormatException | IOException e) {
			}
		}
		Debug.log("fonts loaded");
	}

	/**
	 * Returns a list of {@link File} in the given directory that pass the
	 * filter, including subdirectories.
	 * @param dir the directory to search
	 * @param filter the filter the files have to pass
	 * @return a list of {@link File} in the given directory
	 */
	private ArrayList<File> recursiveListFiles(File dir, FileFilter filter) {
		File[] found = dir.listFiles(filter);

		ArrayList<File> result = new ArrayList<>();

		if (found != null) {
			for (int i = 0; i < found.length; i++) {
				if (found[i].isDirectory()) {
					result.addAll(recursiveListFiles(found[i], filter));
				} else {
					result.add(found[i]);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a list of ttf font files that it can find
	 * @return a list of ttf font files
	 */
	public ArrayList<File> getFontFiles() {
		String[] paths = getFontPaths();

		ArrayList<File> files = new ArrayList<>();

		for (int i = 0; i < paths.length; i++) {
			File fontDirectory = new File(paths[i]);
			if (fontDirectory.exists()) {
				files.addAll(recursiveListFiles(fontDirectory, new FileFilter() {
					@Override
					public boolean accept(File f) {
						return f.getName().endsWith("ttf") || f.getName().endsWith("TTF") || f.isDirectory();
					}
				}));
			}
		}
		return files;
	}

	/**
	 * Returns an array of paths where fonts usually are on the current
	 * operating system
	 * @return an array of paths where fonts usually are
	 */
	public String[] getFontPaths() {
		String[] result = null;
		OSChecker.OSType os = OSChecker.getOSType();
		if (null != os) {
			switch (os) {
				case WINDOWS: {
					result = new String[1];
					String path = System.getenv("WINDIR");
					result[0] = path + "\\" + "Fonts";
					break;
				}
				case MAC_OS: {
					result = new String[3];
					result[0] = System.getProperty("user.home") + File.separator + "Library/Fonts";
					result[1] = "/Library/Fonts";
					result[2] = "/System/Library/Fonts";
					break;
				}
				case LINUX: {
					String[] pathsToCheck = {
						System.getProperty("user.home") + File.separator + ".fonts",
						"/usr/share/fonts/truetype",
						"/usr/share/fonts/TTF"
					};
					ArrayList<String> resultList = new ArrayList<>();
					for (int i = pathsToCheck.length - 1; i >= 0; i--) {
						String path = pathsToCheck[i];
						File tmp = new File(path);
						if (tmp.exists() && tmp.isDirectory() && tmp.canRead()) {
							resultList.add(path);
						}
					}
					if (resultList.isEmpty()) {
						result = new String[0];
					} else {
						result = new String[resultList.size()];
						result = resultList.toArray(result);
					}
					break;
				}
				default:
					break;
			}
		}
		return result;
	}
}
