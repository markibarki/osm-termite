package intransix.osm.termite.map.feature;

import intransix.osm.termite.map.proptree.PropertyNode;
import intransix.osm.termite.map.proptree.KeyNode;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.util.PropertyPair;
import java.util.List;
import java.util.ArrayList;
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
	
	/** This method returns the tree root. */
	public PropertyNode<Object,FeatureInfo> getRoot() {
		return treeRoot;
	} 
	
	/** This method sets the style for a feature. */
	public FeatureInfo getFeatureInfo(OsmObject mapObject) {
		return treeRoot.getPropertyData(mapObject);	
	}
	
//	/** This updates the properties of a feature to match those for the input color.\
//	 * It also returns the matching feature property object. */
//	public FeatureInfo updateFeatureProperties(OsmObject mapObject, String inputColorString) {
//		//find the original color
//		PropertyNode<Object,FeatureInfo> origClassifyingProp = treeRoot.getClassifyingProperty(mapObject);
//				
//		//lookup new color
//		PropertyNode<Object,FeatureInfo> newClassifyingProp = getColorProperty(treeRoot,inputColorString);
//		
//		if(newClassifyingProp == null) {
////we should store that there was an error!!!
//			newClassifyingProp = treeRoot;
//		}
//		
//		//update the properties
//		if(origClassifyingProp != newClassifyingProp) {
//			removeKeys(mapObject,origClassifyingProp);
//			addKeys(mapObject,newClassifyingProp);
//		}
//		
//		return newClassifyingProp.getData();
//	}
	
	public List<PropertyPair> getFeatureProperties(FeatureInfo featureInfo) {
		
		//get the property node for this feature info
		PropertyNode<Object,FeatureInfo> propNode = getFeatureInfoProperty(treeRoot,featureInfo);
		
		//load the properties for this node, tracing back to the tree root
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		if(propNode != null) {
			PropertyPair property;
			KeyNode<Object,FeatureInfo> keyNode = propNode.getParentKey();
			while(keyNode != null) {
				property = new PropertyPair(keyNode.getName(),propNode.getName());
				properties.add(property);
				propNode = keyNode.getParentValue();
				if(propNode != null) {
					keyNode = propNode.getParentKey();
				}
				else {
					keyNode = null;
				}
			}
		}
		
		return properties;
	}
	
	/** This method looks up a PropertyNode for a given input color. */
	private PropertyNode<Object,FeatureInfo> getFeatureInfoProperty(
			PropertyNode<Object,FeatureInfo> prop, FeatureInfo featureInfo) {
		
		//check if this property is the correct property
		if(featureInfo == null) {
			//if no feature info is specified, use tree root
			return treeRoot;
		}
		else if(prop.getData() == featureInfo) {
			return prop;
		}
		else {
			//recursively search children
			PropertyNode<Object,FeatureInfo> resultProp = null;
			for(KeyNode<Object,FeatureInfo> key:prop.getKeys()) {
				for(PropertyNode<Object,FeatureInfo> childProp:key.getValues()) {
					resultProp = getFeatureInfoProperty(childProp,featureInfo);
					if(resultProp != null) return resultProp;
				}
			}
		}
		
		//not found
		return null;
	}
	
//	/** This method looks up a PropertyNode for a given input color. */
//	private PropertyNode<Object,FeatureInfo> getColorProperty(
//			PropertyNode<Object,FeatureInfo> prop, String colorString) {
//		
//		//get the input color for this property
//		String propColor = null;
//		FeatureInfo fp = prop.getData();
//		if(fp != null) {
//			propColor = fp.getInputColor();
//		}
//		
//		//check this object for a match
//		if((propColor != null)&&(colorString.equalsIgnoreCase(propColor))) return prop;
//		
//		//check children for a match
//		PropertyNode<Object,FeatureInfo> resultProp = null;
//		for(KeyNode<Object,FeatureInfo> key:prop.getKeys()) {
//			for(PropertyNode<Object,FeatureInfo> childProp:key.getValues()) {
//				resultProp = getColorProperty(childProp,colorString);
//				if(resultProp != null) return resultProp;
//			}
//		}
//		//if we get here, it wasn't found
//		return null;
//	}
	
//	/** This method adds all the keys and values associated with the given 
//	 * classifying property. */
//	private void addKeys(OsmObject mapObject, PropertyNode prop) {
//		KeyNode parentKey = prop.getParentKey();
//		if(parentKey != null) {
//			//add the top level value
//			mapObject.setProperty(parentKey.getName(),prop.getName());
//			//add the parent value
//			PropertyNode parentProp = parentKey.getParentValue();
//			if(parentProp != null) {
//				addKeys(mapObject,parentProp);
//			}
//		}
//	}
//	
//	/** This method removes all the keys associated with the given 
//	 * classifying property. */
//	private void removeKeys(OsmObject mapObject, PropertyNode prop) {
//		KeyNode parentKey = prop.getParentKey();
//		if(parentKey != null) {
//			mapObject.removeProperty(parentKey.getName());
//			PropertyNode parentProp = parentKey.getParentValue();
//			if(parentProp != null) {
//				removeKeys(mapObject,parentProp);
//			}
//		}
//	}
	
	
}

