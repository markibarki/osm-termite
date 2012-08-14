package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.List;

/**
 * This method updates the feature type associated with an object or set of objects.
 * @author sutter
 */
public class TypeChangeEdit extends EditOperation {
	
	/** Constructor. */
	public TypeChangeEdit(OsmData osmData) {
		super(osmData);
	}
	
	/* This method updates the feature type associated with an object or set of objects.
	 * The selection may include virtual nodes. These are ignored. */
	public boolean modifyType(List<Object> selection, FeatureInfo featureInfo) {
System.out.println("Update feature Type");

		EditAction action = new EditAction(getOsmData(),"Update feature Type");

		try {
			//get the new properties
			List<PropertyPair> newProperties = OsmModel.featureInfoMap.getFeatureProperties(featureInfo);
			
			//traverse list of objects to update
			OsmObject osmObject;
			List<PropertyPair> oldProperties;
			UpdateObjectProperty uop;
			UpdateInstruction instr;
			for(Object object:selection) {
				if(object instanceof OsmObject) {
					osmObject = (OsmObject)object;
				}
				else {
					continue;
				}
				
				//remove old feature properties, if needed
				FeatureInfo oldFeatureInfo = osmObject.getFeatureInfo();
				if(oldFeatureInfo != null) {
					oldProperties = OsmModel.featureInfoMap.getFeatureProperties(oldFeatureInfo);
					for(PropertyPair pp:oldProperties) {
						uop = new UpdateObjectProperty(pp.key,null,null);
						instr = new UpdateInstruction(osmObject,uop);
						action.addInstruction(instr);
					}
				}
				
				for(PropertyPair pp:newProperties) {
					uop = new UpdateObjectProperty(null,pp.key,pp.value);
					instr = new UpdateInstruction(osmObject,uop);
					action.addInstruction(instr);
				}
			}
			
			boolean success = action.doAction();
			if(success) {
				return true;
			}
			else {
				reportError(action.getDesc());
				return false;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
}
