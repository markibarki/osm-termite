package intransix.osm.termite.app.mapdata.publish;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.dataset.OsmChangeSet;
import intransix.osm.termite.map.workingdata.*;
import intransix.osm.termite.net.NetRequest;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class PublishAction {
	
	private String errorMsg;
	private MapDataManager mapDataManager;
	private long structureId;
	
	public PublishAction(MapDataManager mapDataManager, long structureId) {
		this.mapDataManager = mapDataManager;
		this.structureId = structureId;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

	public boolean verifyPublish() {
		//make sure these is no uncommited data
//this is not a perfect check, SHOULD BE IMPROVED
		String undoMessage = mapDataManager.getUndoMessage();

		if(undoMessage == null) {
			return true;
		}
		else {
			errorMsg = "You cannot publish if there is data that has not yet been committed.";
			return false;
		}
	}
	
	public boolean publish() throws Exception {
		
		NetRequest netRequest;
		int responseCode;

		//create the products
		OsmData osmData = mapDataManager.getOsmData();

		//get the way
		OsmWay structure = osmData.getOsmWay(structureId);
		if(structure == null) {
			errorMsg = "The specified structure was not found.";
			return false;
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
			errorMsg = "No levels were found for the specified structure.";
			return false;
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
			return false;
		}

		//create structure json
		ProductJson productJson = new ProductJson(mapDataManager,structure,levels,version);
		productJson.createProducts();

		//make network requests
		//commit the data
		JSONObject resultJson = productJson.getStructureJson();
		boolean success = submitProductJson(PublishRequestSource.STRUCTURE_FILENAME,structure.getId(),version,resultJson);
		//handle failure
		if(!success) {
			return false;
		}

		int cnt = levels.size();
		List<JSONObject> levelJsons = productJson.getLevelJsons();
		for(int i = 0; i < cnt; i++) {
			OsmRelation level = levels.get(i);
			resultJson = levelJsons.get(i);
			success = submitProductJson(PublishRequestSource.LEVEL_FILENAME,level.getId(),version,resultJson);
			//handle failure
			if(!success) {
				return false;
			}
		}

		//post the footprint to the feature service
		resultJson = productJson.getFootprintJson();
		success = submitFeatureJson(PublishFeatureRequestSource.STRUCTURE_LAYERNAME,structure.getId(),resultJson);

		return success;
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
	
}
