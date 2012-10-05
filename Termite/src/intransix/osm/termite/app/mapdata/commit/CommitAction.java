package intransix.osm.termite.app.mapdata.commit;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.map.workingdata.*;
import intransix.osm.termite.net.NetRequest;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author sutter
 */
public class CommitAction {
	
	//======================
	// Properties
	//======================
	
	private final static String EMPTY_CHANGESET_MSG = "There is no data to commit.";
	
	private String errorMsg;
	
	private MapDataManager mapDataManager;
	private LoginManager loginManager;
	private OsmChangeSet changeSet;
	private OsmData osmData;
	private OsmDataSet dataSet;
	private List<UpdateInfo> updateList;
	
	//======================
	// CommitAction
	//======================
	
	/** Constructor */
	public CommitAction(MapDataManager mapDataManager, LoginManager loginManager) {
		this.mapDataManager = mapDataManager;
		this.loginManager = loginManager;
		osmData = mapDataManager.getOsmData();
		dataSet = mapDataManager.getDataSet();
	}
	
	/** This method returns an error message for the action. It is valid if one
	 * of the methods returns false. */
	public String getErrorMessage() {
		return errorMsg;
	}
	
	/** This method will check if a commit can be done. If the return value is false
	 * the associated message can be read from getErrorMessage(). */
	public boolean verifyChangeSet() {
		//get the change set
		loadChangeSet();
		
		if(changeSet.isEmpty()) {
			errorMsg = EMPTY_CHANGESET_MSG;
			return false;
		}
		else {
			return true;
		}
	}
	
	/** This method commits the data. If the return value is false
	 * the associated message can be read from getErrorMessage().
	 * @param commitMessage		The message to go with the commit
	 */
	public boolean commit(String commitMessage) throws Exception {
			changeSet.setMessage(commitMessage);
			
			//make network requests
			
			boolean success;
			
			success = openChangeSet();
			
			if(!success) return false;
			
			success = commitData();
			
			if(!success) return false;
			
			success = closeChangeSet();
				
			return success;
	}
	
	/** This method should be called from the UI thread after completion of the
	 * commit. It will update the working data and the base data.
	 * @return 
	 */
	public boolean postProcessInUiThread() {
		if(updateList != null) {
			processUpdateList();
			return true;
		}
		else {
			//we should not be here if there is no update list.
			//if for some other reason we return false afteer a successful commit,
			//we should delete the current data.
			return false;
		}
	}
	
	//======================
	// Private Methods
	//======================

	/** This method opens a change set on the remote server. */
	private boolean openChangeSet() throws Exception {
		NetRequest xmlRequest;
		int responseCode;
		String username = loginManager.getUsername();
		String password = loginManager.getPassword();

		//open a change set
		OpenChangeSetRequest openChangeSetRequest = new OpenChangeSetRequest(changeSet);
		xmlRequest = new NetRequest(openChangeSetRequest);
		xmlRequest.setCredentials(username, password);
		responseCode = xmlRequest.doRequest();

		if(responseCode == 200) {
			//success
			return true;
		}
		else if(responseCode == 401) {
			//unauthorized
			errorMsg = "There username and password are not valid.";
			loginManager.setCredentials(username,null);
			return false;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}
			
	}
	
	/** This method submits the data for commit. */
	private boolean commitData() throws Exception {
		NetRequest xmlRequest;
		int responseCode;
		String username = loginManager.getUsername();
		String password = loginManager.getPassword();
		updateList = null;

		//commit the data
		CommitRequest commitRequest = new CommitRequest(changeSet);
		xmlRequest = new NetRequest(commitRequest);
		xmlRequest.setCredentials(username, password);
		responseCode = xmlRequest.doRequest();

		if(responseCode == 200) {
			//success
			updateList = commitRequest.getUpdateList();
			return true;
		}
		else if(responseCode == 401) {
			//unauthorized
			errorMsg = "There username and password are not valid.";
			loginManager.setCredentials(username,null);
			return false;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}

	}
	
	/** This method closes the change set on the remote server. */
	private boolean closeChangeSet() throws Exception {
		NetRequest xmlRequest;
		int responseCode;
		String username = loginManager.getUsername();
		String password = loginManager.getPassword();

		//close the change set
		CloseChangeSetRequest closeChangeSetRequest = new CloseChangeSetRequest(changeSet);
		xmlRequest = new NetRequest(closeChangeSetRequest);
		xmlRequest.setCredentials(username, password);
		responseCode = xmlRequest.doRequest();


		if(responseCode == 200) {
			//success
			return true;
		}
		else if(responseCode == 401) {
			//unauthorized
			errorMsg = "There username and password are not valid.";
			loginManager.setCredentials(username,null);
			return false;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}
	}
	
	
	/** This method creates the change set by comparing the base and working data. */
	private void loadChangeSet() {
		if(osmData == null) return;
		
		changeSet = new OsmChangeSet();
		
		//-----------------
		//nodes
		//-----------------
		
		//store a list of all the original nodes
		HashSet<OsmNodeSrc> srcNodes = new HashSet<OsmNodeSrc>();
		srcNodes.addAll(dataSet.getSrcNodes());
		
		OsmChangeObject changeObject;
		for(OsmNode node:osmData.getOsmNodes()) {
			//make sure the node was loaded from initial data or created with a create
			//and not a unloaded refernce created to populate a way or relation
			if(!node.getIsLoaded()) continue;
			
			//remove nodes in working data from list
			OsmNodeSrc originalNodeSrc = dataSet.getNodeSrc(node.getId());
			if(originalNodeSrc != null) {
				//compare and remove
				srcNodes.remove(originalNodeSrc);
				if(originalNodeSrc.isDifferent(node)) {
					OsmNodeSrc finalNodeSrc = new OsmNodeSrc();
					node.copyInto(finalNodeSrc);
					changeObject = new OsmChangeObject(originalNodeSrc,finalNodeSrc);
					changeSet.addUpdated(changeObject);
				}
			}
			else {
if(node.getId() > 0) {
	System.out.println("ERROR in Commit: trying to create a node with a positive ID.");
	continue;
}
				OsmNodeSrc finalNodeSrc = new OsmNodeSrc();
				node.copyInto(finalNodeSrc);
				changeObject = new OsmChangeObject(null,finalNodeSrc);
				changeSet.addCreated(changeObject);
			}
		}
		//remaining nodes have been deleted
		for(OsmNodeSrc deletedNodeSrc:srcNodes) {
			changeObject = new OsmChangeObject(deletedNodeSrc,null);
			changeSet.addDeleted(changeObject);
		}
		
		//-----------------
		//ways
		//-----------------
		HashSet<OsmWaySrc> srcWays = new HashSet<OsmWaySrc>();
		srcWays.addAll(dataSet.getSrcWays());
		
		for(OsmWay way:osmData.getOsmWays()) {
			//make sure the way was loaded from initial data or created with a create
			//and not a unloaded refernce created to populate a way or relation
			if(!way.getIsLoaded()) continue;
			
			OsmWaySrc originalWaySrc = dataSet.getWaySrc(way.getId());
			if(originalWaySrc != null) {
				//compare and remove
				srcWays.remove(originalWaySrc);
				if(originalWaySrc.isDifferent(way)) {
					OsmWaySrc finalWaySrc = new OsmWaySrc();
					way.copyInto(finalWaySrc);
					changeObject = new OsmChangeObject(originalWaySrc,finalWaySrc);
					changeSet.addUpdated(changeObject);
				}
			}
			else {
if(way.getId() > 0) {
	System.out.println("ERROR in Commit: trying to create a way with a positive ID.");
	continue;
}

				OsmWaySrc finalWaySrc = new OsmWaySrc();
				way.copyInto(finalWaySrc);
				changeObject = new OsmChangeObject(null,finalWaySrc);
				changeSet.addCreated(changeObject);
			}
		}
		//any objects left have been deleted
		for(OsmWaySrc deletedWaySrc:srcWays) {
			changeObject = new OsmChangeObject(deletedWaySrc,null);
			changeSet.addDeleted(changeObject);
		}
		
		//----------------
		//relations
		//----------------
		HashSet<OsmRelationSrc> srcRelations = new HashSet<OsmRelationSrc>();
		srcRelations.addAll(dataSet.getSrcRelations());
		
		for(OsmRelation relation:osmData.getOsmRelations()) {	
			//make sure the relation was loaded from initial data or created with a create
			//and not a unloaded refernce created to populate a way or relation
			if(!relation.getIsLoaded()) continue;
			
			OsmRelationSrc originalRelationSrc = dataSet.getRelationSrc(relation.getId());
			if(originalRelationSrc != null) {
				//compare and remove
				srcRelations.remove(originalRelationSrc);
				if(originalRelationSrc.isDifferent(relation)) {
					OsmRelationSrc finalRelationSrc = new OsmRelationSrc();
					relation.copyInto(finalRelationSrc);
					changeObject = new OsmChangeObject(originalRelationSrc,finalRelationSrc);
					changeSet.addUpdated(changeObject);
				}
			}
			else {
if(relation.getId() > 0) {
	System.out.println("ERROR in Commit: trying to create a relation with a positive ID.");
	continue;
}
				OsmRelationSrc finalRelationSrc = new OsmRelationSrc();
				relation.copyInto(finalRelationSrc);
				changeObject = new OsmChangeObject(null,finalRelationSrc);
				changeSet.addCreated(changeObject);
			}
		}
		//delete ones missing from working data
		for(OsmRelationSrc deletedRelationSrc:srcRelations) {
			changeObject = new OsmChangeObject(deletedRelationSrc,null);
			changeSet.addDeleted(changeObject);
		}
	}
	
	
	/** This method updates the OsmData to reflect the new ID and version numbers
	 * after the commit. */
	private void processUpdateList() {
		//before we process the updates, we must flush the old command queue, since
		//it may contain old ids
		//alternatively, we could update the command queue ids
		mapDataManager.clearCommandQueue();
		int editNumber = mapDataManager.getNextEditNumber();
		
		//create the maps to look up the src objects
		OsmObject osmObject;
		OsmSrcData osmSrcObject;
		for(UpdateInfo info:updateList) {
	
			if(info.objectType.equals("node")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete - remove node src object, node already removed
					dataSet.removeNodeSrc(info.oldId);
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of node
					osmObject = osmData.getOsmNode(info.oldId);
					
					if(info.newId != info.oldId) {
						//move the node object
						osmData.nodeIdChanged(info.oldId,info.newId);
						osmObject.setDataVersion(editNumber);
						//create node src
						OsmNodeSrc osmNodeSrc = new OsmNodeSrc();
						osmObject.copyInto(osmNodeSrc);
						dataSet.putNodeSrc(osmNodeSrc);
						osmSrcObject = osmNodeSrc;
					}
					else {
						//get copy of node src
						osmSrcObject = dataSet.getNodeSrc(info.oldId);
						//update source object
						osmObject.copyInto(osmSrcObject);
					}	
				}
			}
			else if(info.objectType.equals("way")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete - remove way src, way already removed
					dataSet.removeWaySrc(info.oldId);
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of way
					osmObject = osmData.getOsmWay(info.oldId);
					
					if(info.newId != info.oldId) {
						//move the way object
						osmData.wayIdChanged(info.oldId,info.newId);
						osmObject.setDataVersion(editNumber);
						//create way src
						OsmWaySrc osmWaySrc = new OsmWaySrc();
						osmObject.copyInto(osmWaySrc);
						dataSet.putWaySrc(osmWaySrc);
						osmSrcObject = osmWaySrc;
					}
					else {
						//lookup copy of way
						osmSrcObject = dataSet.getWaySrc(info.oldId);
						//update source object
						osmObject.copyInto(osmSrcObject);
					}	
				}
			}
			else if(info.objectType.equals("relation")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete relation src, relation already deleted
					dataSet.removeRelationSrc(info.oldId);
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of relation
					osmObject = osmData.getOsmRelation(info.oldId);
					
					if(info.newId != info.oldId) {
						//move the relation object
						osmData.relationIdChanged(info.oldId,info.newId);
						osmObject.setDataVersion(editNumber);
						//create relation src
						OsmRelationSrc osmRelationSrc = new OsmRelationSrc();
						osmObject.copyInto(osmRelationSrc);
						dataSet.putRelationSrc(osmRelationSrc);
						osmSrcObject = osmRelationSrc;
					}
					else {
						//get copy of relation src
						osmSrcObject = dataSet.getRelationSrc(info.oldId);
						//update source object
						osmObject.copyInto(osmSrcObject);
					}	
				}	
			}
			else {
				continue;
			}

			
			if(osmObject != null) {
				//update version in source
				if(info.newVersion != null) {
					osmSrcObject.setOsmObjectVersion(info.newVersion);
				}
			}	
		}
		
		//notify of a change to the data
		//the data does change, but the main reason I do this is to notify the UI
		//that the undo/redo commands changed. That may be cludgey.
		mapDataManager.dataChanged(editNumber);
	}
	
	
	
}
