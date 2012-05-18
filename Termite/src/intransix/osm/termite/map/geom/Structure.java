package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class Structure extends MapObject {
	
	private Feature parent;
	private ArrayList<Level> levels = new ArrayList<Level>();
	
	public Feature getParent() {
		return parent;
	}
	
	public void addLevel(Level level) {
		this.levels.add(level);
	}
	
	public ArrayList<Level> getLevels() {
		return levels;
	}
}
