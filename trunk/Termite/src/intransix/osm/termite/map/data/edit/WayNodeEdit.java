package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class WayNodeEdit extends EditOperation {
	
	private OsmWay activeWay;
	private OsmNode activeNode;
	
	
	public WayNodeEdit(OsmData osmData) {
		super(osmData);
	}
	
	public OsmWay getActiveWay() {
		return activeWay;
	}
	
	public OsmNode getActiveNode() {
		return activeNode;
	}
	
	
	/** This method executes the edit associated with clicking the way tool. */
	public boolean wayToolClicked(EditDestPoint dest, OsmWay way, int insertIndex,  
			OsmNode node, FeatureInfo featureInfo, OsmRelation currentLevel) {
		
		this.activeWay = way;
		
System.out.println("Add a node to a way");		

		EditAction action = new EditAction(getOsmData(),"Add node to way");
		
		if(dest.snapNode != null) {
			//make sure this node can be added
			if(checkIfAddNodeIsValid(way,dest.snapNode,insertIndex) == false) {
				JOptionPane.showMessageDialog(null,"That node can no be added to the current way.");
				return false;
			}
		}
		
		//create a node
		Long addNodeId = createOrUseExistingNode(action,dest,currentLevel);
		Long addWayId = null;
		
		if((way == null)&&(node != null)) {
			//create a way extending the initial node
			
			OsmWaySrc waySrc = new OsmWaySrc();
			if(addNodeId != null) {
				List<Long> nodeIds = waySrc.getNodeIds();
				//add starter node
				nodeIds.add(node.getId());
				//add created node
				nodeIds.add(addNodeId);
			}

			//get the properties
			List<PropertyPair> properties = OsmModel.featureInfoMap.getFeatureProperties(featureInfo);
			for(PropertyPair pp:properties) {
				waySrc.addProperty(pp.key,pp.value);
			}

			EditInstruction instr = new CreateInstruction(waySrc,getOsmData());
			action.addInstruction(instr);
			addWayId = waySrc.getId();
		}
		else if(way != null) {
			//add new node to the active way
			
			UpdateInsertNode uin = new UpdateInsertNode(addNodeId,insertIndex);
			EditInstruction instr = new UpdateInstruction(way,uin);
			action.addInstruction(instr);
		}
		
		try {
			boolean success = action.doAction();
			if(success) {
				if((way == null)&&(addWayId != null)) {
					activeWay = getOsmData().getOsmWay(addWayId);
				}
				if(addNodeId != null) {
					activeNode = getOsmData().getOsmNode(addNodeId);
				}	
			}
			else {
				reportError(action.getDesc());
				activeWay = null;
				activeNode = null;
			}
			return success;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
	
	/** This method executes the edit associated with dragging a virtual node to add
	 * a node to a way. */
	public OsmNode nodeInserted(OsmSegment segment, EditDestPoint dest, OsmRelation currentLevel) {
		
System.out.println("Insert node into way");

		//create action
		EditAction action = new EditAction(getOsmData(),"Insert Node into Way");
			
		try {
			
			if(dest.snapNode != null) {
				//make sure this node can be added to all the ways containing this segment
				for(OsmWay way:segment.getOsmWays()) {
					
					int segmentIndex = way.getSegments().indexOf(segment);
					if(segmentIndex >= 0) {
						//check if this node can be added at the corresponding index
						if(checkIfAddNodeIsValid(way,dest.snapNode,segmentIndex + 1) == false) {
							JOptionPane.showMessageDialog(null,"That node can no be added to the current segment.");
							return null;
						}
					}
				}
			}
			
			
			//create node, if needed
			Long nodeId = createOrUseExistingNode(action,dest,currentLevel);

			//add node to each way containing this segment
			int insertIndex = 1;
			boolean found = false;
			for(OsmWay way:segment.getOsmWays()) {
				for(OsmSegmentWrapper osw:way.getSegments()) {
					if(segment == osw.segment) {
						found = true;
						break;
					}
					insertIndex++;
				}
				if(!found) continue;

				//add node to way
				UpdateInsertNode uin = new UpdateInsertNode(nodeId,insertIndex);
				EditInstruction instr = new UpdateInstruction(way,uin);
				action.addInstruction(instr);
			}

			//execute action
			boolean success = action.doAction();
			if(success) {
				OsmNode node = getOsmData().getOsmNode(nodeId);
				return node;
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
	
	private Long createOrUseExistingNode(EditAction action, EditDestPoint dest, OsmRelation currentLevel) {
		
		Long startNodeId = null;
		if(dest.snapNode != null) {
			startNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,getOsmData());
			action.addInstruction(instr);
			startNodeId = nodeSrc.getId();
			
			if(currentLevel != null) {
				//place node on the current level
				UpdateInsertMember uim = new UpdateInsertMember(startNodeId,OsmModel.TYPE_NODE,OsmModel.ROLE_FEATURE);
				instr = new UpdateInstruction(currentLevel,uim);
				action.addInstruction(instr);
			}
		}
		
		return startNodeId;
	}
	
	/** This method checks if the given node can be used to add to this way. */
	private boolean checkIfAddNodeIsValid(OsmWay way, OsmNode node, int insertIndex) {
		if(node.getWays().contains(way)) {
			int existingIndex = -1;
			List<OsmNode> nodes = way.getNodes();
			for(int i = 0; i < nodes.size(); i++) {
				if(nodes.get(i) == node) {
					existingIndex = i;
					break;
				}
			}
			//legal if the two copies of the node are at either end
			boolean closedWay = (((insertIndex == nodes.size())&&(existingIndex == 0)) ||
					((insertIndex == 0)&&(existingIndex == nodes.size()-1)));
			
			return closedWay;
			
		}
		else {
			//legal if node is not already in the way
			return true;
		}
		
	}
	
}
