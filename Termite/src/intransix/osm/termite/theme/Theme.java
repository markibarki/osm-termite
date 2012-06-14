package intransix.osm.termite.theme;

import intransix.osm.termite.map.proptree.PropertyNode;
import intransix.osm.termite.map.osm.OsmObject;
import org.json.*;

/**
 * This is a theme implemented as a property tree.
 * 
 * @author sutter
 */
public class Theme {
		
	//==============
	//Properties
	//==============
	
	private PropertyNode<Style,Style> treeRoot = null;
	
	//==============
	//Public Methods
	//==============
	
	/** This method parses a property tree for the theme. */
	public static Theme parse(JSONObject json) throws Exception {
		StyleParser styleParser = new StyleParser();
	
		Theme theme = new Theme();
		theme.treeRoot = new PropertyNode<Style,Style>();
		theme.treeRoot.parse(json,null,styleParser);
		return theme;
	}
	
	/** This method sets the style for a feature. */
	public Style getStyle(OsmObject mapObject) {
		return treeRoot.getPropertyData(mapObject);
	}
	
}
