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
		
		NetRequest netRequest;
		int responseCode;
		
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
								(member.osmObject.getId() == structureId)) {
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
			
			//get the version
			int version;
			VersionRequestSource vrs = new VersionRequestSource(PublishRequestSource.STRUCTURE_FILENAME,structureId);
			
			netRequest = new NetRequest(vrs);
			responseCode = netRequest.doRequest();
			
			if(responseCode == 200) {
				version = vrs.getVersion();
			}
			else {
				errorMsg = "Error in request for version: " + responseCode;
				success = false;
				return null;
			}
			
			//create structure json
			ProductJson productJson = new ProductJson(osmData,structure,levels,version);
			productJson.createProducts();
			
			//make network requests
			//commit the data
			JSONObject resultJson = productJson.getStructureJson();
			success = submitProductJson(PublishRequestSource.STRUCTURE_FILENAME,structure.getId(),version,resultJson);
			//handle failure
			if(!success) {
				return null;
			}
			
			int cnt = levels.size();
			List<JSONObject> levelJsons = productJson.getLevelJsons();
			for(int i = 0; i < cnt; i++) {
				OsmRelation level = levels.get(i);
				resultJson = levelJsons.get(i);
				success = submitProductJson(PublishRequestSource.LEVEL_FILENAME,level.getId(),version,resultJson);
				//handle failure
				if(!success) {
					return null;
				}
			}
			
			//post the footprint to the feature service
			resultJson = productJson.getFootprintJson();
			success = submitFeatureJson(PublishFeatureRequestSource.STRUCTURE_LAYERNAME,structure.getId(),resultJson);
			//handle failure
			if(!success) {
				return null;
			}
			
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return "";
	}
	
	private boolean submitProductJson(String fileName, long key, int version, JSONObject json) throws Exception {
		PublishRequestSource prs = new PublishRequestSource(fileName,key,version,json);
		NetRequest netRequest = new NetRequest(prs);
		int responseCode = netRequest.doRequest();

		if(responseCode == 200) {
			//success
			return true;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}
	}
	
	private boolean submitFeatureJson(String layerName, long key, JSONObject json) throws Exception {
		PublishFeatureRequestSource prs = new PublishFeatureRequestSource(layerName,key,json);
		NetRequest netRequest = new NetRequest(prs);
		int responseCode = netRequest.doRequest();

		if(responseCode == 200) {
			//success
			return true;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}
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
