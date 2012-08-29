package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class MoveEdit extends EditOperation {
	
	
	private boolean nodeDeleted = false;
	
	public MoveEdit(OsmData osmData) {
		super(osmData);
	}
	
	public boolean getNodeDeleted() {
		return nodeDeleted;
	}
	
	public boolean selectionMoved(List<Object> selection, EditDestPoint start,
			EditDestPoint dest) {
		
System.out.println("Move the selection");

System.out.println("start: " + start.point + "; end: " + dest.point);

		EditAction action = new EditAction(getOsmData(),"Move selection");
		try {

			boolean mergeNodes = false;
			boolean useStartNode = false;

			//check the case of dragging one node to another
			if((start.snapNode != null)&&(dest.snapNode != null)) {
				//make sure one node has no properties
				if((start.snapNode.hasProperties())&&(dest.snapNode.hasProperties())) {
					JOptionPane.showMessageDialog(null,"Current you can not move one node to another node if they both have properties.");
					return false;
				}

				//check we aren't 
				boolean nodeOk = checkNodeMoveOk(start.snapNode,dest.snapNode);
				if(!nodeOk) {
					JOptionPane.showMessageDialog(null,"This move is not allowed.");
					return false;	
				}

				//flag that we need to merge the nodes, save the one that has properties, if one does
				mergeNodes = true;
				useStartNode = start.snapNode.hasProperties();
			}

			//get the collection of nodes to move
			HashSet<OsmNode> nodeSet = new HashSet<OsmNode>();
			for(Object selectObject:selection) {
				if(selectObject instanceof OsmNode) {
					nodeSet.add((OsmNode)selectObject);
				}
				else if(selectObject instanceof OsmWay) {
					for(OsmNode node:((OsmWay)selectObject).getNodes()) {
						nodeSet.add(node);
					}
				}
			}

			//get move coordinates
			double dx = dest.point.getX() - start.point.getX();
			double dy = dest.point.getY() - start.point.getY();

			//create action
			boolean success;

			//do move
			success = moveNodes(nodeSet,dx,dy,action);
			if(!success) return false;
					
			if(mergeNodes) {
				success = doNodeMerge(start.snapNode,dest.snapNode,useStartNode,action);
				if(!success) return false;
			}
		
			success = action.doAction();
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
	
	private boolean moveNodes(Collection<OsmNode> nodes, double dx, double dy, EditAction action) {
		for(OsmNode node:nodes) {
			UpdatePosition up = new UpdatePosition(node.getPoint().getX() + dx,
					node.getPoint().getY() + dy);
			EditInstruction instr = new UpdateInstruction(node,up);
			action.addInstruction(instr);
		}

		return true;
	}
	
	private boolean doNodeMerge(OsmNode startNode, OsmNode destNode, 
			boolean saveStart, EditAction action) {
		
		OsmNode deleteNode = saveStart ? destNode : startNode;
		OsmNode saveNode = saveStart ? startNode : destNode;
		
		EditInstruction instr;
		
		//replace all copies of this node in each way
		for(OsmWay way:deleteNode.getWays()) {
			List<OsmNode> nodes = way.getNodes();
			OsmNode node;
			for(int i = 0; i < nodes.size(); i++) {
				node = nodes.get(i);
				if(node == deleteNode) {
					//remove the old node
					UpdateRemoveNode urn = new UpdateRemoveNode(i);
					instr = new UpdateInstruction(way,urn);
					action.addInstruction(instr);
					
					UpdateInsertNode uin = new UpdateInsertNode(saveNode.getId(),i);
					instr = new UpdateInstruction(way,uin);
					action.addInstruction(instr);
					
				}
			}
		}
		
		//replace all copies of this node in each relation
		for(OsmRelation relation:deleteNode.getRelations()) {
			List<OsmMember> members = relation.getMembers();

			OsmMember member;
			for(int i = 0; i < members.size(); i++) {
				member = members.get(i);
				if(member.osmObject == deleteNode) {
					//remove the old node
					UpdateRemoveMember urm = new UpdateRemoveMember(i);
					instr = new UpdateInstruction(relation,urm);
					action.addInstruction(instr);
					
					UpdateInsertMember uim = new UpdateInsertMember(saveNode.getId(),saveNode.getObjectType(),member.role,i);
					instr = new UpdateInstruction(relation,uim);
					action.addInstruction(instr);
				}
			}
		}
		
		//delete the proper node
		instr = new DeleteInstruction(deleteNode);
		action.addInstruction(instr);
		nodeDeleted = true;
		
		return true;
		
	}
	
	/** This method checks to make sure we aren't dragging from one node in a way onto another
	 * unless the two nodes are the start and end. */
	private boolean checkNodeMoveOk(OsmNode moveNode, OsmNode destNode) {
		for(OsmWay startWay:moveNode.getWays()) {
			for(OsmWay destWay:destNode.getWays()) {
				if(startWay == destWay) {
					//return false if these aren't the two end nodes in the way
					int startIndex = startWay.getNodes().indexOf(moveNode);
					int endIndex = destWay.getNodes().indexOf(destNode);
					if(!(((startIndex == 0)&&(endIndex == destWay.getNodes().size()-1)) ||
							((endIndex == 0)&&(startIndex == destWay.getNodes().size()-1)))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
