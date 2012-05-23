package intransix.osm.termite.svg.inputcolor;

import intransix.osm.termite.map.geom.Feature;
import intransix.osm.termite.map.*;
import org.json.*;

/**
 * This is a input color mapper implemented as a property tree.
 * 
 * @author sutter
 */
public class InputColorMapper {
	
	PropertyNode<Object,String> treeRoot = null;
	
	/** This method parses a property tree for the theme. */
	public static InputColorMapper parse(JSONObject json) throws Exception {
		InputColorParser parser = new InputColorParser();
	
		InputColorMapper icm = new InputColorMapper();
		icm.treeRoot = new PropertyNode<Object,String>();
		icm.treeRoot.parse(json,null,parser);
		return icm;
		
	}
	
	/** This method sets the style for a feature. */
	public String getColorString(Feature feature) {
		return treeRoot.getPropertyData(feature);	
	}
	
	/** This updates the properties of a feature to match those for the input color. */
	public void updateFeature(Feature feature, String inputColorString) {
		//find the original color
		PropertyNode<Object,String> origClassifyingProp = treeRoot.getClassifyingProperty(feature);
				
		//lookup new color
		PropertyNode<Object,String> newClassifyingProp = getColorProperty(treeRoot,inputColorString);
		
		//update the properties
		if(origClassifyingProp != newClassifyingProp) {
			removeKeys(feature,origClassifyingProp);
			addKeys(feature,newClassifyingProp);
		}
	}
	
	
	/** This method looks up a PropertyNode for a given input color. */
	private PropertyNode<Object,String> getColorProperty(
			PropertyNode<Object,String> prop, String colorString) {
		
		//check this object for a match
		if(colorString.equalsIgnoreCase(prop.getName())) return prop;
		
		//check children for a match
		PropertyNode<Object,String> resultProp = null;
		for(KeyNode<Object,String> key:treeRoot.getKeys()) {
			for(PropertyNode<Object,String> childProp:key.getValues()) {
				resultProp = getColorProperty(childProp,colorString);
				if(resultProp != null) return resultProp;
			}
		}
		//if we get here, it wasn't found
		return null;
	}
	
	/** This method adds all the keys and values associated with the given 
	 * classifying property. */
	private void addKeys(Feature feature, PropertyNode prop) {
		KeyNode parentKey = prop.getParentKey();
		if(parentKey != null) {
			//add the top level value
			feature.setProperty(parentKey.getName(),prop.getName());
			//add the parent value
			PropertyNode parentProp = parentKey.getParentValue();
			if(parentProp != null) {
				addKeys(feature,parentProp);
			}
		}
	}
	
	/** This method removes all the keys associated with the given 
	 * classifying property. */
	private void removeKeys(Feature feature, PropertyNode prop) {
		KeyNode parentKey = prop.getParentKey();
		if(parentKey != null) {
			feature.removeProperty(parentKey.getName());
			PropertyNode parentProp = parentKey.getParentValue();
			if(parentProp != null) {
				removeKeys(feature,parentProp);
			}
		}
	}
	
	
}

