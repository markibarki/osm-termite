package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;

/**
 *
 * @author sutter
 */
public class LevelCreateEdit extends EditOperation {
	
	public LevelCreateEdit(OsmData osmData) {
		super(osmData);
	}
	
		public OsmRelation nodeToolClicked(OsmObject parent, int zlevel, String displayName) {
System.out.println("Create a level");

		EditAction action = new EditAction(getOsmData(),"Create Level");

		try {
			OsmRelationSrc relationSrc = new OsmRelationSrc();
			relationSrc.addMember(parent.getId(),parent.getObjectType(),OsmModel.ROLE_PARENT);

			//get the properties
			relationSrc.addProperty(OsmModel.KEY_TYPE,OsmModel.TYPE_LEVEL);
			relationSrc.addProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel));
			relationSrc.addProperty(OsmModel.KEY_NAME,displayName);
			
			EditInstruction instr = new CreateInstruction(relationSrc,getOsmData());
			action.addInstruction(instr);
			long id = relationSrc.getId();
			
			boolean success = action.doAction();
			if(success) {

				OsmRelation relation = getOsmData().getOsmRelation(id);
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
