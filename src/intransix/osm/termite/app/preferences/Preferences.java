package intransix.osm.termite.app.preferences;

import org.json.*;
import intransix.osm.termite.util.JsonIO;

/**
 * This is a class to hold preferences, accessible statically
 * @author sutter
 */
public class Preferences {
	
	private static JSONObject preferences = new JSONObject();
	
	/** This method initializes the preferences. This will overwrite the currently loaded
	 * preferences with the contents of the preference file, which should be a JSON
	 * file. 
	 * 
	 * @param configFileName	The file name for the preference configuration file.
	 * @throws Exception 
	 */
	public static void init(String configFileName) throws Exception {
		preferences = JsonIO.readJsonFile(configFileName);
	}
	
	/** This method saves the preferences to a file. */
	public static void savePreferences(String fileName) throws Exception {
		JsonIO.writeJsonFile(fileName, preferences);
	}
	
	public static String getProperty(String property) {
		return preferences.optString(property,null);
	}
	
	public static void setProperty(String property, Object value) throws Exception {
		preferences.put(property,value);
	}
	
	public static void clearProperty(String property) {
		preferences.remove(property);
	}
	
	public static int getIntProperty(String property, int defaultValue) {
		return preferences.optInt(property, defaultValue);
	}
	
	public static boolean getBooleanProperty(String property, boolean defaultValue) {
		return preferences.optBoolean(property, defaultValue);
	}
	
	public static double getDoubleProperty(String property, double defaultValue) {
		return preferences.optDouble(property, defaultValue);
	}
	
	public static JSONObject getObjectProperty(String property) {
		return preferences.optJSONObject(property);
	}
	
	public static JSONArray getArrayProperty(String property) {
		return preferences.optJSONArray(property);
	}
	
}
