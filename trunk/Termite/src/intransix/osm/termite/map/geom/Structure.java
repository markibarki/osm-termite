package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class Structure extends MapObject {
	
	private Rectangle2D bounds;
	
	private Feature parent;
	private ArrayList<Level> levels = new ArrayList<Level>();
	
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}
	
	
	public Feature getParent() {
		return parent;
	}
	
	public void addLevel(Level level) {
		this.levels.add(level);
		level.setStructure(this);
	}
	
	public ArrayList<Level> getLevels() {
		return levels;
	}
}
