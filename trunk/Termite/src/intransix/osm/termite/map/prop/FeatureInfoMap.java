package intransix.osm.termite.map.prop;

import intransix.osm.termite.map.*;
import org.json.*;

/**
 * This is a input color mapper implemented as a property tree.
 * 
 * @author sutter
 */
public class FeatureInfoMap {
	
	PropertyNode<Object,FeatureInfo> treeRoot = null;
	
	/** This method parses a property tree for the theme. */
	public static FeatureInfoMap parse(JSONObject json) throws Exception {
		FeatureInfoParser parser = new FeatureInfoParser();
	
		FeatureInfoMap icm = new FeatureInfoMap();
		icm.treeRoot = new PropertyNode<Object,FeatureInfo>();
		icm.treeRoot.parse(json,null,parser);
		return icm;
		
	}
	
	/** This method sets the style for a feature. */
	public FeatureInfo getFeatureInfo(MapObject mapObject) {
		return treeRoot.getPropertyData(mapObject);	
	}
	
	/** This updates the properties of a feature to match those for the input color.\
	 * It also returns the matching feature property object. */
	public FeatureInfo updateFeatureProperties(MapObject mapObject, String inputColorString) {
		//find the original color
		PropertyNode<Object,FeatureInfo> origClassifyingProp = treeRoot.getClassifyingProperty(mapObject);
				
		//lookup new color
		PropertyNode<Object,FeatureInfo> newClassifyingProp = getColorProperty(treeRoot,inputColorString);
		
		if(newClassifyingProp == null) {
//we should store that there was an error!!!
			newClassifyingProp = treeRoot;
		}
		
		//update the properties
		if(origClassifyingProp != newClassifyingProp) {
			removeKeys(mapObject,origClassifyingProp);
			addKeys(mapObject,newClassifyingProp);
		}
		
		return newClassifyingProp.getData();
	}
	
	
	/** This method looks up a PropertyNode for a given input color. */
	private PropertyNode<Object,FeatureInfo> getColorProperty(
			PropertyNode<Object,FeatureInfo> prop, String colorString) {
		
		//get the input color for this property
		String propColor = null;
		FeatureInfo fp = prop.getData();
		if(fp != null) {
			propColor = fp.getInputColor();
		}
		
		//check this object for a match
		if((propColor != null)&&(colorString.equalsIgnoreCase(propColor))) return prop;
		
		//check children for a match
		PropertyNode<Object,FeatureInfo> resultProp = null;
		for(KeyNode<Object,FeatureInfo> key:prop.getKeys()) {
			for(PropertyNode<Object,FeatureInfo> childProp:key.getValues()) {
				resultProp = getColorProperty(childProp,colorString);
				if(resultProp != null) return resultProp;
			}
		}
		//if we get here, it wasn't found
		return null;
	}
	
	/** This method adds all the keys and values associated with the given 
	 * classifying property. */
	private void addKeys(MapObject mapObject, PropertyNode prop) {
		KeyNode parentKey = prop.getParentKey();
		if(parentKey != null) {
			//add the top level value
			mapObject.setProperty(parentKey.getName(),prop.getName());
			//add the parent value
			PropertyNode parentProp = parentKey.getParentValue();
			if(parentProp != null) {
				addKeys(mapObject,parentProp);
			}
		}
	}
	
	/** This method removes all the keys associated with the given 
	 * classifying property. */
	private void removeKeys(MapObject mapObject, PropertyNode prop) {
		KeyNode parentKey = prop.getParentKey();
		if(parentKey != null) {
			mapObject.removeProperty(parentKey.getName());
			PropertyNode parentProp = parentKey.getParentValue();
			if(parentProp != null) {
				removeKeys(mapObject,parentProp);
			}
		}
	}
	
	
}

