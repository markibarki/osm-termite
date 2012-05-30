package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class TermiteStructure extends MapObject {
	
	private Rectangle2D bounds;
	
	private TermiteFeature parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}
	
	
	public TermiteFeature getParent() {
		return parent;
	}
	
	public void addLevel(TermiteLevel level) {
		this.levels.add(level);
		level.setStructure(this);
	}
	
	public ArrayList<TermiteLevel> getLevels() {
		return levels;
	}
}
