package intransix.osm.termite.theme;

import intransix.osm.termite.map.geom.Feature;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class Theme {
	
	//I NEED TO RETHINK HOW THIS WORKS
	private Style defaultStyle;
	private ArrayList<Style> styles = new ArrayList<Style>();
	
	public void addStyle(Style style) {
		styles.add(style);
	}
	
	public void setDeafultStyle(Style style) {
		this.defaultStyle = style;
	}
	
	public void loadStyle(Feature feature) {
		for(Style style:styles) {
			if(style.matches(feature)) {
				feature.setStyle(style);
				return;
			}
		}
		feature.setStyle(defaultStyle);	
	}
	
}
