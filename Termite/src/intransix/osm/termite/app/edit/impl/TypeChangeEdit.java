package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.util.PropertyPair;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.mapdata.instruction.UpdateObjectProperty;
import intransix.osm.termite.app.mapdata.instruction.UpdateInstruction;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.List;

/**
 * This method updates the feature type associated with an object or set of objects.
 * @author sutter
 */
public class TypeChangeEdit extends EditOperation {
	
	/** Constructor. */
	public TypeChangeEdit(MapDataManager mapDataManager) {
		super(mapDataManager);
	}
	
	/* This method updates the feature type associated with an object or set of objects.
	 * The selection may include virtual nodes. These are ignored. */
	public boolean modifyType(List<Object> selection, FeatureInfo featureInfo) {
System.out.println("Update feature Type");

		EditAction action = new EditAction(getMapDataManager(),"Update feature Type");

		try {
			//get the new properties
			FeatureTypeManager featureTypeManager = getMapDataManager().getFeatureTypeManager();
			List<PropertyPair> newProperties = featureTypeManager.getFeatureProperties(featureInfo);
			
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
				FeatureInfo oldFeatureInfo = MapDataManager.getObjectFeatureInfo(osmObject);
				if(oldFeatureInfo != null) {
					oldProperties = featureTypeManager.getFeatureProperties(oldFeatureInfo);
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
