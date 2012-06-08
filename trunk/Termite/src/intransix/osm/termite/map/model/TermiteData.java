package intransix.osm.termite.map.model;

import java.util.*;
import java.awt.geom.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.MercatorCoordinates;

/**
 * This class holds the active session data.
 * 
 * @author sutter
 */
public class TermiteData {
	
	//=====================
	// Properties
	//=====================
	
	private OsmData baseData;
	private OsmData workingData;
	private Rectangle2D bounds = null;
	
	private HashMap<Long,TermiteNode> nodeMap = new HashMap<Long,TermiteNode>();
	private HashMap<Long,TermiteWay> wayMap = new HashMap<Long,TermiteWay>();
	private HashMap<Long,TermiteMultiPoly> multiPolyMap = new HashMap<Long,TermiteMultiPoly>();
	private HashMap<Long,TermiteStructure> structureMap = new HashMap<Long,TermiteStructure>();
	
	//USED ONLY FOR METHOD 1 - level relation
	private HashMap<Long,TermiteLevel> levelMap = new HashMap<Long,TermiteLevel>();
	
	private TermiteStructure outdoorStructure;
	private TermiteLevel outdoorLevel;
	
	//=====================
	// Public Methods
	//=====================
	
	/**Constructor. */
	public TermiteData() {
	}
	
	public TermiteStructure getOutdoorStructure() {
		return outdoorStructure;
	}
	
	public TermiteLevel getOutdoorLevel() {
		return outdoorLevel;
	}
	
	public TermiteStructure getStructure(Long structureId) {
		return getStructure(structureId,false);
	}
	
	public TermiteLevel getLevel(Long structureId, int zlevel) {
		return getLevel(structureId, zlevel, false);
	}
	
	//----------------------
	//edit helper methods
	//----------------------
	
	public EditAction createAction(String desc) {
		return new EditAction(workingData,desc);
	}
	
	public <T extends OsmObject> EditInstruction<T> getCreateInstruction(T copyOfObjectToCreate) {
		EditInstruction<T> createInstruction = new EditInstruction<T>
				(copyOfObjectToCreate,EditInstruction.InstrType.CREATE);
		createInstruction.setData((EditData<T>)copyOfObjectToCreate);
		return createInstruction;
	}
	
	public <T extends OsmObject> EditInstruction<T> getUpdateInstruction(T objectToUpdate, 
			EditData<T> targetData) {
		EditInstruction<T> updateInstruction = new EditInstruction<T>
				(objectToUpdate,EditInstruction.InstrType.UPDATE);
		updateInstruction.setData(targetData);
		return updateInstruction;
	}
	
	public <T extends OsmObject> EditInstruction<T> getDeleteInstruction(T objectToDelete) {
		EditInstruction<T> deleteInstruction = new EditInstruction<T>
				(objectToDelete,EditInstruction.InstrType.DELETE);
		//no target data on delete
		return deleteInstruction;
	}
	
	public boolean doAction(EditAction action) throws Exception {
		boolean success = action.doAction();
		if(success) {
			this.updateLocalData();
			this.updateRemoteData();
		}
		
		return success;
	}
	
	public boolean undoAction(EditAction action) throws Exception {
		boolean success = action.undoAction();
		if(success) {
			this.updateLocalData();
			this.updateRemoteData();
		}
		
		return success;
	}
	
	//-----------------------
	// Load Data
	//-----------------------
	
	/** This method loads the osm format data into the data model. */
	public void loadData(OsmData baseData) {
		
long start;
double durationMsec;
		
		this.baseData = baseData;
start = System.nanoTime();
		this.workingData = baseData.createCopy();	
durationMsec = 1e-6*(System.nanoTime() - start);
System.out.println("data copy time: " + durationMsec);

		//create the outdoor objects
start = System.nanoTime();
		outdoorStructure = this.getStructure(OsmObject.INVALID_ID, true);
		outdoorLevel = outdoorStructure.initOutdoors(this);
long end = System.nanoTime();
durationMsec = 1e-6*(end - start);
System.out.println("create outdoor: " + durationMsec);
		
		//---------------------
		// Create raw termite objects based on OSM objects
		//---------------------
		
start = System.nanoTime();
		//create the termite nodes
		for(OsmNode osmNode:workingData.getOsmNodes()) {
			//create the node
			TermiteNode termiteNode = getNode(osmNode.getId(),true);
			termiteNode.setOsmNode(osmNode);
		}
		
		//create the termite ways
		//WE MUST DO THIS AFTER RELATIONS SINCE WE RELY ON CHECK THAT RELATION EXISTS
		for(OsmWay osmWay:workingData.getOsmWays()) {
			//create the node
			TermiteWay termiteWay = getWay(osmWay.getId(),true);
			termiteWay.setOsmWay(osmWay);
		}
		
		//create the objects based on relations
		for(OsmRelation osmRelation:workingData.getOsmRelations()) {
			String relationtype = osmRelation.getProperty(OsmModel.TAG_TYPE);
			Long memberId = osmRelation.getId();
			//create the node
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationtype)) {
				TermiteMultiPoly termiteMultiPoly = this.getMultiPoly(memberId, true);
				termiteMultiPoly.setOsmRelation(osmRelation);
			}
			else if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relationtype)) {
				//METHOD 1 only:
				TermiteLevel termiteLevel = this.getLevel(memberId, true);
				termiteLevel.setOsmRelation(osmRelation);
			}
			else if(OsmModel.TYPE_STRUCTURE.equalsIgnoreCase(relationtype)) {
				TermiteStructure termiteStructure = this.getStructure(memberId, true);
				termiteStructure.setOsmRelation(osmRelation);
			}
		}
durationMsec = 1e-6*(System.nanoTime() - start);
System.out.println("termite load time: " + durationMsec);
		
		//--------------------------------------------------
		//read the data from the osm object to the termite object
		//--------------------------------------------------
start = System.nanoTime();
		updateLocalData();
durationMsec = 1e-6*(System.nanoTime() - start);
System.out.println("Local update time: " + durationMsec);
		
		//-------------------------
		// update remote data now thatlocal data is updated
		//-------------------------
		
start = System.nanoTime();
		updateRemoteData();
durationMsec = 1e-6*(System.nanoTime() - start);
System.out.println("Remote update time: " + durationMsec);
	}
	
	//==========================
	// Package Methods
	//==========================
	

	/** This method looks up the TermiteNode associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	TermiteNode getNode(Long id, boolean createRef) {
		TermiteNode node = this.nodeMap.get(id);
		if((node == null)&&(createRef)) {
			node = new TermiteNode();
			nodeMap.put(id, node);
		}
		return node;
	}
	
	/** This method looks up the TermiteWay associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	TermiteWay getWay(Long id, boolean createRef) {
		TermiteWay way = this.wayMap.get(id);
		if((way == null)&&(createRef)) {
			way = new TermiteWay();
			wayMap.put(id, way);
		}
		return way;
	}
	
//////////////////////////////////////////////////////////////////////
// LEVEL LOOKUP
// In method 1 - level relation, lokup is by id
// In method 2 - node labeling, lookup is by structure + zlevel
// THE TWO LOOKUP METHODS CAN NOT BE MIXED!!!

	/** This method looks up the TermiteLevel associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	TermiteLevel getLevel(Long id, boolean createRef) {
		TermiteLevel level = this.levelMap.get(id);
		if((level == null)&&(createRef)) {
			level = new TermiteLevel();
			levelMap.put(id, level);
		}
		return level;
	}
	
	/** This method looks up a level for the given structure id and zlevel value. */
	TermiteLevel getLevel(Long structureId, int zlevel, boolean createRef) {
		TermiteStructure structure = getStructure(structureId,createRef);
		if(structure != null) {
			TermiteLevel level = structure.lookupLevel(zlevel);
			if((level == null)&&(createRef)) {
				level = new TermiteLevel();
				level.setZlevel(zlevel);
			}
			return level;
		}
		else {
			return null;
		}
	}
	
// end level lookup section
//////////////////////////////////////////////////////////////////////////////////////////////
	
	/** This method looks up the TermiteStructure associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	TermiteStructure getStructure(Long id, boolean createRef) {
		TermiteStructure structure = this.structureMap.get(id);
		if((structure == null)&&(createRef)) {
			structure = new TermiteStructure();
			structureMap.put(id, structure);
		}
		return structure;
	}
	
	/** This method looks up the TermiteMultiPoly associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	TermiteMultiPoly getMultiPoly(Long id, boolean createRef) {
		TermiteMultiPoly multiPoly = this.multiPolyMap.get(id);
		if((multiPoly == null)&&(createRef)) {
			multiPoly = new TermiteMultiPoly();
			multiPolyMap.put(id, multiPoly);
		}
		return multiPoly;
	}
	
	OsmData getWorkingData() {
		return workingData;
	}
	
	//==========================
	// Private Methods
	//==========================
	
	private void updateLocalData() {
		
		for(TermiteNode node:nodeMap.values()) {
			node.updateLocalData(this);
		}
		for(TermiteWay way:wayMap.values()) {
			way.updateLocalData(this);
		}
		for(TermiteMultiPoly multiPoly:multiPolyMap.values()) {
			multiPoly.updateLocalData(this);
		}
		for(TermiteStructure structure:structureMap.values()) {
			structure.updateLocalData(this);
			for(TermiteLevel level:structure.getLevels()) {
				level.updateLocalData(this);
			}
		}
	}
	
	private void updateRemoteData() {
		
		for(TermiteNode node:nodeMap.values()) {
			node.updateRemoteData(this);
		}
		for(TermiteWay way:wayMap.values()) {
			way.updateRemoteData(this);
		}
		for(TermiteMultiPoly multiPoly:multiPolyMap.values()) {
			multiPoly.updateRemoteData(this);
		}
		for(TermiteStructure structure:structureMap.values()) {
			structure.updateRemoteData(this);
			for(TermiteLevel level:structure.getLevels()) {
				level.updateRemoteData(this);
				level.orderFeatures();
			}
			structure.calculateBounds();
		}
	}
	
	
	// <editor-fold defaultstate="collapsed" desc="sample code fold">
	// </editor-fold>
}
