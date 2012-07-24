package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class EditOperation {
	
	private OsmData osmData;
	
	public EditOperation(OsmData osmData) {
		this.osmData = osmData;
	}
	
	public OsmData getOsmData() {
		return osmData;
	}
	
	//=======================
	//protected methods
	//=======================
	
	protected Long createOrUseExistingNode(EditAction action, EditDestPoint dest, OsmRelation currentLevel) {
		
		Long startNodeId = null;
		if(dest.snapNode != null) {
			startNodeId = dest.snapNode.getId();
		}
		else if(dest.point != null) {
			OsmNodeSrc nodeSrc = new OsmNodeSrc();
			nodeSrc.setPosition(dest.point.getX(),dest.point.getY());
			EditInstruction instr = new CreateInstruction(nodeSrc,osmData);
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
	
	protected void insertNodeIntoWay(EditAction action, OsmWay way, long nodeId, int index) {
		UpdateInsertNode uin = new UpdateInsertNode(nodeId,index);
		EditInstruction instr = new UpdateInstruction(way,uin);
		action.addInstruction(instr);
	}
	
	protected void reportError(String actionDesc) {
		JOptionPane.showMessageDialog(null,"There was an unknown error on the action: " + actionDesc);
	}
	
	protected void reportFatalError(String actionDesc, String exceptionMsg) {
		JOptionPane.showMessageDialog(null,"There was a fatal error on the action: " + actionDesc +
				"; " + exceptionMsg + "The application must exit");
		System.exit(-1);
	}
	
	
}
