package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import java.util.*;

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
		
		//delete all ways in selection
		//delete all nodes in selection
		//delete nodes in the ways if:
		// - they are not in any ways not included in the selection
		// - they have no properties

		EditAction action = new EditAction(getOsmData(),"Delete objects");

		try {
			//create a local selection of the objects to be deleted
			HashSet<OsmWay> ways = new HashSet<OsmWay>();
			HashSet<OsmNode> nodes = new HashSet<OsmNode>();
			for(Object object:selection) {
				if(object instanceof OsmNode) {
					//copy object to working selection
					nodes.add((OsmNode)object);
				}
				else if(object instanceof OsmWay) {
					ways.add((OsmWay)object);
				}
			}
			
			//figure out what way nodes should be deleted - no properties and no other ways
			for(OsmWay way:ways) {
				for(OsmNode node:way.getNodes()) {
					//if the node has properties, don't delete it with the way
					if(node.hasProperties()) continue;
					
					//if the node is in a way not in the delete set, don't delete it
					boolean inExternalWay = false;
					for(OsmWay containerWay:node.getWays()) {
						if(!ways.contains(containerWay)) {
							inExternalWay = true;
						}
					}
					if(inExternalWay) continue;
					
					//add this node to the node set if it is not there
					nodes.add(node);
				}
			}
		
			//delete these ways
			EditInstruction instr;
			for(OsmWay way:ways) {
				//remove this object from any relations it is in
				removeFromRelations(way,action);

				//delete the object
				instr = new DeleteInstruction(way);
				action.addInstruction(instr);
			}
			
			//delete the nodes
			for(OsmNode node:nodes) {
				//remove this object from any relations it is in
				removeFromRelations(node,action);
				
				//remove from any external ways
				for(OsmWay containerWay:node.getWays()) {
					if(!ways.contains(containerWay)) {
						removeNodeFromWay(node,containerWay,action);
					}
				}
				
				//delete the object
				instr = new DeleteInstruction(node);
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
	private void removeNodeFromWay(OsmNode node, OsmWay way, EditAction action) {
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
