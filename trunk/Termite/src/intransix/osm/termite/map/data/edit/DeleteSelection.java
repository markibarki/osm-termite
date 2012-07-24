package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sutter
 */
public class DeleteSelection extends EditOperation {
	
	public DeleteSelection(OsmData osmData) {
		super(osmData);
	}
	
	
	public boolean deleteSelection(List<Object> selection) {
		System.out.println("Delete selecton");

		EditAction action = new EditAction(getOsmData(),"Delete objects");

		try {
			//create a local selection of the objects to be deleted
			List<OsmObject> localSelection = new ArrayList<OsmObject>();
			for(Object object:selection) {
				if(object instanceof OsmObject) {
					//copy object to working selection
					if(!localSelection.contains(object)) {
						localSelection.add((OsmObject)object);
					}

					//for ways, also add any nodes if they are not features on their own
					if(object instanceof OsmWay) {
						for(OsmNode node:((OsmWay)object).getNodes()) {
							//don't delete nodes that are features in their own right
							if((!node.isFeature())&&(!localSelection.contains(node))) {
								localSelection.add(node);
							}
						}
					}
				}
			}
		
			//delete these objects
			EditInstruction instr;
			for(OsmObject osmObject:localSelection) {
		
				//remove this object from any relations it is in
				removeFromRelations(osmObject,action);

				//for a node remove it from any ways
				if(osmObject instanceof OsmNode) {
					removeNodeFromWays((OsmNode)osmObject,action);
				}

				//delete the object
				instr = new DeleteInstruction(osmObject);
				action.addInstruction(instr);

			}
			
			//execute the action
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
	
		/** This method removes all copies of an object from the relations it is in. */
	private void removeFromRelations(OsmObject osmObject, EditAction action) {
		for(OsmRelation relation:osmObject.getRelations()) {
			List<OsmMember> members = relation.getMembers();
			for(int index = members.size()-1; index >= 0; index--) {
				OsmMember member = members.get(index);
				if(member.osmObject == osmObject) {
					UpdateRemoveMember urm = new UpdateRemoveMember(index);
					action.addInstruction(new UpdateInstruction(relation,urm));
				}
			}
		}
	}
	
	/** This method removes the node (all copies) from the ways it is in. */
	private void removeNodeFromWays(OsmNode node, EditAction action) {
		for(OsmWay way:node.getWays()) {
			List<OsmNode> nodes = way.getNodes();
			for(int index = nodes.size()-1; index >= 0; index--) {
				OsmNode n = nodes.get(index);
				if(n == node) {
					UpdateRemoveNode urn = new UpdateRemoveNode(index);
					action.addInstruction(new UpdateInstruction(way,urn));
				}
			}
		}
	}
	
}
