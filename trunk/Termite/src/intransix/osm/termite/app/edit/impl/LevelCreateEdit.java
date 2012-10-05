package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.dataset.OsmRelationSrc;
import intransix.osm.termite.app.mapdata.instruction.EditInstruction;
import intransix.osm.termite.app.mapdata.instruction.CreateInstruction;
import intransix.osm.termite.app.mapdata.instruction.EditAction;

/**
 *
 * @author sutter
 */
public class LevelCreateEdit extends EditOperation {
	
	public LevelCreateEdit(MapDataManager mapDataManager) {
		super(mapDataManager);
	}
	
		public OsmRelation nodeToolClicked(OsmObject parent, int zlevel, String displayName) {
System.out.println("Create a level");

		EditAction action = new EditAction(getMapDataManager(),"Create Level");

		try {
			OsmRelationSrc relationSrc = new OsmRelationSrc();
			relationSrc.addMember(parent.getId(),parent.getObjectType(),OsmModel.ROLE_PARENT);

			//get the properties
			relationSrc.putProperty(OsmModel.KEY_TYPE,OsmModel.TYPE_LEVEL);
			relationSrc.putProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel));
			relationSrc.putProperty(OsmModel.KEY_NAME,displayName);
			
			EditInstruction instr = new CreateInstruction(relationSrc,getMapDataManager());
			action.addInstruction(instr);
			long id = relationSrc.getId();
			
			boolean success = action.doAction();
			if(success) {

				OsmData osmData = getMapDataManager().getOsmData();
				OsmRelation relation = osmData.getOsmRelation(id);
				return relation;
			}
			else {
				reportError(action.getDesc());
				return null;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return null;
		}
	}
}
