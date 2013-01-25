package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmMember;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.mapdata.instruction.UpdateRemoveNode;
import intransix.osm.termite.app.mapdata.instruction.UpdateInsertNode;
import intransix.osm.termite.app.mapdata.instruction.UpdateRemoveMember;
import intransix.osm.termite.app.mapdata.instruction.UpdateInstruction;
import intransix.osm.termite.app.mapdata.instruction.DeleteInstruction;
import java.util.*;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class RemoveWayNodeEdit extends EditOperation {
	
	public RemoveWayNodeEdit(MapDataManager mapDataManager) {
		super(mapDataManager);
	}
	
	/** This method removes the nodes represented by the list of node indices from the
	 * way. Note that if the node is the start and end node (for a closed way) removing
	 * it will remove that node whether of not both index values are included in the list. 
	 * 
	 * @param way				The way from which to remove nodes
	 * @param removalIndices	The nodes to remove, in terms of order within the way.
	 * @return					true or false for success or failure
	 */
	public boolean removeNodesFromWay(OsmWay way, List<Integer> removalIndices) {
		System.out.println("Remove nodes from way");
		
		//remove the nodes
		//delete the removed nodes if:
		// - they are not in any ways not included in the selection
		// - they have no properties
		
		EditAction action = new EditAction(getMapDataManager(),"Remove nodes from way");
		
		try {
			
			List<OsmNode> wayNodes = way.getNodes();
		
			//do not allow remove if this leaves too few nodes
			if(removalIndices.size() >= wayNodes.size()-1) {
				JOptionPane.showMessageDialog(null,"The remove can not be done because this leaves"
						+ " too few nodes in the way.");
				return false;
			}
			
			//--------------------
			//add instructions to remove nodes from ways
			//---------------------
			
			//we need to check fro repeats and remove the repeats (for now)
			boolean wayOpened = false;
			if(way.isClosed()) {
				int endIndex = way.getNodes().size()-1;
				boolean containsStart = removalIndices.contains(0);
				boolean containsEnd = removalIndices.contains(endIndex);
				if(containsStart && !containsEnd) removalIndices.add(endIndex);
				else if(!containsStart && containsEnd) removalIndices.add(0);
				wayOpened = true;
			}
			
			//sort the indices so we can remove last first, avoids problems with index values changing
			Collections.sort(removalIndices);
			//also retrieve the nodes
			HashSet<OsmNode> removedNodes = new HashSet<OsmNode>();
			for(int i = removalIndices.size() - 1; i >= 0; i--) {
				//get nodes
				int index = removalIndices.get(i);
				OsmNode node = wayNodes.get(index);
				removedNodes.add(node);

				//add remove instruction
				UpdateRemoveNode urn = new UpdateRemoveNode(index);
				action.addInstruction(new UpdateInstruction(way,urn));
			}
			
			//if the way was closed, we need to reclose it if 
			if(wayOpened) {
				//fined the largest remaining index
				int lri;
				for(lri = wayNodes.size()-1; lri >= 0; lri--) {
					if(!removalIndices.contains(lri)) {
						break;
					}
				}
				//add this node at the start to reclose the loop
				OsmNode node = wayNodes.get(lri);
				UpdateInsertNode uin = new UpdateInsertNode(node.getId(),0);
				action.addInstruction(new UpdateInstruction(way,uin));
			}
			
			//-------------------
			// Figure out which nodes to delete
			//-------------------
			
			ArrayList<OsmNode> nodesToDelete = new ArrayList<OsmNode>();
			//figure out if we need to delete any nodes
			for(OsmNode node:removedNodes) {

				//Delete node if the node has no properties and it is only in this one way 
				if((!node.hasProperties()) &&
						(node.getWays().size() <= 1)) {
					//delete node;
					nodesToDelete.add(node);
				}
			}
			
			//--------------------
			// rmove external references as needed
			//--------------------
			
			HashMap<OsmRelation,List<Integer>> relationRemovalMap = new HashMap<OsmRelation,List<Integer>>();
	
			//process nodes
			for(OsmNode node:nodesToDelete) {
				//remove this object from any relations it is in
				getRelationIndices(node,relationRemovalMap);
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

			
			//------------------
			// Delete the nodes
			//------------------

			//figure out if we need to delete any nodes
			for(OsmNode node:nodesToDelete) {
				action.addInstruction(new DeleteInstruction(node));
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
		
		List<OsmRelation> relations = osmObject.getRelations();
		for(OsmRelation relation:relations) {
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
}
