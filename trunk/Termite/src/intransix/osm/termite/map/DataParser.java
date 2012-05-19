package intransix.osm.termite.map;

import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public interface DataParser<T> {
	
	public T parseData(JSONObject json, ArrayList<T> parentData);
	
}
