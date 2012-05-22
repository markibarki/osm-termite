package intransix.osm.termite.map;

import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public abstract class DataParser<TK, TV> {
	
	protected ArrayList<TV> valueParents = new ArrayList<TV>();
	protected ArrayList<TK> keyParents = new ArrayList<TK>();
	
	public void addValueParentData(TV data) {}
	
	public void removeValueParentData(TV data) {}
	
	public void addKeyParentData(TK data) {}
	
	public void removeKeyParentData(TK data) {}
	
	public abstract TV parseValueData(JSONObject json);
	
	public abstract TK parseKeyData(JSONObject json);
	
}
