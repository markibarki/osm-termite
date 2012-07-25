package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class RemoveWayNodeEdit extends EditOperation {
	
	public RemoveWayNodeEdit(OsmData osmData) {
		super(osmData);
	}
	
	public boolean removeNodesFromWay(OsmWay way, List<Integer> removalIndices) {
		System.out.println("Remove nodes from way");
		
		//remove the nodes
		//delete the removed nodes if:
		// - they are not in any ways not included in the selection
		// - they have no properties
		
		EditAction action = new EditAction(getOsmData(),"Remove nodes from way");
		
		try {
		
			//--------------------
			//add instructions to remove nodes from ways
			//---------------------
			
			//sort the indices so we can remove last first, avoids problems with index values changing
			Collections.sort(removalIndices);
			//also retrieve the nodes
			HashSet<OsmNode> removedNodes = new HashSet<OsmNode>();
			List<OsmNode> wayNodes = way.getNodes();
			for(int i = removalIndices.size() - 1; i >= 0; i--) {
				//get nodes
				int index = removalIndices.get(i);
				removedNodes.add(wayNodes.get(index));

				//add remove instruction
				UpdateRemoveNode urn = new UpdateRemoveNode(index);
				action.addInstruction(new UpdateInstruction(way,urn));
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
}
