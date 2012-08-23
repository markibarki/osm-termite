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
		//If the delete results with any way having 1 or 0 nodes, delete the way (verify with user?)
		//If the delete results with any relation having no members, delete the relation(verify with user?)

// There is something to be careful of - if you delete multiple objects from 
//a relation (or nodes from a way), the index of the second delete should be the
//index taht the nodes has AFTER the first is removed. It makes it tricky to
//ensure this is true.
		
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
		
			EditInstruction instr;
			
			//--------------------
			// rmove external references as needed
			//--------------------
			
			HashMap<OsmRelation,List<Integer>> relationRemovalMap = new HashMap<OsmRelation,List<Integer>>();
			HashMap<OsmWay,List<Integer>> wayRemovalMap = new HashMap<OsmWay,List<Integer>>(); 
			
			//process ways
			for(OsmWay way:ways) {
				//remove this object from any relations it is in
				getRelationIndices(way,relationRemovalMap);
			}
			
			//process nodes
			for(OsmNode node:nodes) {
				//remove this object from any relations it is in
				getRelationIndices(node,relationRemovalMap);
				
				//remove from any external ways
				for(OsmWay containerWay:node.getWays()) {
					if(!ways.contains(containerWay)) {
						getWayIndices(node,containerWay,wayRemovalMap);
					}
				}
			}
			
			//do the removal
			List<Integer> indices;
			for(OsmRelation relation:relationRemovalMap.keySet()) {
				indices = relationRemovalMap.get(relation);
				//sort - we must remove in reverse order to use this set of indices
				Collections.sort(indices);
				for(int i = indices.size() - 1; i >= 0; i--) {
					UpdateRemoveMember urm = new UpdateRemoveMember(indices.get(i));
					action.addInstruction(new UpdateInstruction(relation,urm));
				}
			}
			
			for(OsmWay way:wayRemovalMap.keySet()) {
				indices = wayRemovalMap.get(way);
				//sort - we must remove in reverse order to use this set of indices
				Collections.sort(indices);
				for(int i = indices.size() - 1; i >= 0; i--) {
					UpdateRemoveNode urn = new UpdateRemoveNode(indices.get(i));
					action.addInstruction(new UpdateInstruction(way,urn));
				}
			}
			
			//--------------------
			//delete the objects
			//--------------------
			
			//ways
			for(OsmWay way:ways) {
				//delete the object
				instr = new DeleteInstruction(way);
				action.addInstruction(instr);
			}
			
			//delete the nodes
			for(OsmNode node:nodes) {	
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
	
	private void getRelationIndices(OsmObject osmObject, 
			HashMap<OsmRelation,List<Integer>> relationRemovalMap) {
		
		for(OsmRelation relation:osmObject.getRelations()) {
			List<Integer> indices = relationRemovalMap.get(relation);
			if(indices == null) {
				indices = new ArrayList<Integer>();
				relationRemovalMap.put(relation,indices);
			}
			//load all copies of this object into the list
			int index = 0;
			for(OsmMember member:relation.getMembers()) {
				if(osmObject == member.osmObject) {
					indices.add(index);
				}
				index++;
			}
		}
	}
	
	private void getWayIndices(OsmNode node, OsmWay way,
			HashMap<OsmWay,List<Integer>> wayRemovalMap) {
		
		List<Integer> indices = wayRemovalMap.get(way);
		if(indices == null) {
			indices = new ArrayList<Integer>();
			wayRemovalMap.put(way,indices);
		}
		//load all copies of this object into the list
		int index = 0;
		for(OsmNode n:way.getNodes()) {
			if(n == node) {
				indices.add(index);
			}
			index++;
		}
	
	}
	
}
