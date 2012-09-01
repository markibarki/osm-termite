package intransix.osm.termite.publish;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.gui.dialog.CommitDialog;
import intransix.osm.termite.net.NetRequest;
import javax.swing.*;
import java.util.*;
import org.json.*;
import intransix.osm.termite.publish.ProductJson;
		

/**
 *
 * @author sutter
 */
public class PublishTask extends SwingWorker<Object,Object>{
	
	private String message;
	private OsmData osmData;
	private long structureId;
	
	private TermiteGui gui;
	private JDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	public PublishTask(TermiteGui gui, long structureId) {
		this.gui = gui;
		this.osmData = gui.getMapData();
		this.structureId = structureId;
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(gui,this,"Publishing map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		
		try {
			//check if there is any uncomitted data
			OsmChangeSet changeSet = new OsmChangeSet();
			osmData.loadChangeSet(changeSet);
			
			if(!changeSet.isEmpty()) {
				JOptionPane.showMessageDialog(null,"You cannot publish if there is data that has not yet been committed.");
				canceled = true;
				return null;
			}
			
			//create the products
			
			//get the way
			OsmWay structure = osmData.getOsmWay(structureId);
			if(structure == null) {
				JOptionPane.showMessageDialog(null,"The specified structure was not found.");
				canceled = true;
				return null;
			}
			
			//get all levels for this way
			List<OsmRelation> levels = new ArrayList<OsmRelation>();
			for(OsmRelation relation:osmData.getOsmRelations()) {
				if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relation.getRelationType())) {
					for(OsmMember member:relation.getMembers()) {
						if((OsmModel.ROLE_PARENT.equalsIgnoreCase(member.role))&&
								(member.osmObject instanceof OsmWay)) {
							levels.add(relation);
						}
					}
				}
			}
			if(levels.size() < 1) {
				JOptionPane.showMessageDialog(null,"No levels were found for the specified structure.");
				canceled = true;
				return null;
			}
			
			//create structure json
			ProductJson productJson = new ProductJson(osmData,structure,levels);
			productJson.createProducts();
			
			//make network requests
			NetRequest xmlRequest;
			int responseCode;
			
//get a version number for the structure data
//go back and add this to the files!!!!! (I ignored it. I should get the version first)
//submit the structure and level files.
//names: indoormap, lvlgeom, key: id of structure or level
			
//			//commit the data
////			CommitRequest commitRequest = new CommitRequest(changeSet,osmData);
////			xmlRequest = new NetRequest(commitRequest);
////			xmlRequest.setCredentials(username, password);
////			responseCode = xmlRequest.doRequest();
//			
//			if(responseCode == 200) {
//				//success
//				success = true;
//			}
////			else if(responseCode == 401) {
////				//unauthorized
////				errorMsg = "There username and password are not valid.";
////				loginManager.setCredentials(username,null);
////				success = false;
////				return null;
////			}
//			else {
//				errorMsg = "Server error: response code " + responseCode;
//				success = false;
//				return null;
//			}
			
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return "";
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(canceled) {
			return;
		}
		
		if(success) {
			//no action needed
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
