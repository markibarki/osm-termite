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
	
	private boolean endWay = false;
	
	public WayNodeEdit(OsmData osmData) {
		super(osmData);
	}
	
	/** After a call to wayToolClicked, this method should be called to check 
	 * if the way should be ended. It will return yes if the way was closed by 
	 * the latest action. */
	public boolean getEndWay() {
		return endWay;
	}
	
	
	/** This method executes the edit associated with clicking the way tool. */
	public OsmWay wayToolClicked(OsmWay activeWay, boolean isEnd, 
			EditDestPoint destPoint, FeatureInfo featureInfo, OsmRelation currentLevel) {
		
System.out.println("Add a node to a way");

		if(activeWay == null) {
			return createWay(destPoint,featureInfo, currentLevel);
		}
		else {
			int insertIndex;
			if(isEnd) insertIndex = activeWay.getNodes().size();
			else insertIndex = 0;
			addNodeToWay(activeWay,destPoint,insertIndex,currentLevel);
			return activeWay;
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

				insertNodeIntoWay(action,way,nodeId,insertIndex);
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
	
	
	private OsmWay createWay(EditDestPoint dest, FeatureInfo featureInfo, OsmRelation currentLevel) {
		EditAction action = new EditAction(getOsmData(),"Create Way");
		
		Long startNodeId = createOrUseExistingNode(action,dest,currentLevel);
		
		OsmWaySrc waySrc = new OsmWaySrc();
		if(startNodeId != null) {
			List<Long> nodeIds = waySrc.getNodeIds();
			nodeIds.add(startNodeId);
		}
		
		//get the properties
		List<PropertyPair> properties = OsmModel.featureInfoMap.getFeatureProperties(featureInfo);
		for(PropertyPair pp:properties) {
			waySrc.addProperty(pp.key,pp.value);
		}
			
		EditInstruction instr = new CreateInstruction(waySrc,getOsmData());
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			if(success) {
				long id = waySrc.getId();
				return getOsmData().getOsmWay(id);
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
	
	private boolean addNodeToWay(OsmWay way, EditDestPoint dest, int insertIndex, OsmRelation currentLevel) {
		EditAction action = new EditAction(getOsmData(),"Add node to way");
		
		if(dest.snapNode != null) {
			//make sure this node can be added
			if(checkIfAddNodeIsValid(way,dest.snapNode,insertIndex) == false) {
				JOptionPane.showMessageDialog(null,"That node can no be added to the current way.");
				return false;
			}
		}
		
		Long addNodeId = createOrUseExistingNode(action,dest,currentLevel);
		insertNodeIntoWay(action,way,addNodeId,insertIndex);
		
		try {
			boolean success = action.doAction();
			if(!success) reportError(action.getDesc());
			return success;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
	
	private void insertNodeIntoWay(EditAction action, OsmWay way, long nodeId, int index) {
		UpdateInsertNode uin = new UpdateInsertNode(nodeId,index);
		EditInstruction instr = new UpdateInstruction(way,uin);
		action.addInstruction(instr);
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
					((insertIndex == 0)&&(existingIndex == nodes.size())));
			
			if(closedWay) endWay = true;
			return closedWay;
			
		}
		else {
			//legal if node is not already in the way
			return true;
		}
		
	}
	
}
