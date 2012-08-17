package intransix.osm.termite.map.data;

import java.util.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.data.edit.EditOperation;
import javax.swing.JOptionPane;

/**
 * This object serves as the data manager for a working set of OSM data.
 *
 * @author sutter
 */
public class OsmData {
	
	//==========================
	// Private Fields
	//==========================
	
	public final static long INVALID_ID = 0;
	public final static int INVALID_DATA_VERSION = -1;
	
	private final static long FIRST_ID = -1;
	private final static int FIRST_EDIT_NUMBER = 1;
	public final static int INITIAL_DATA_VERSION = 0;
	
	private long nextId = FIRST_ID;
	private int nextEditNumber = FIRST_EDIT_NUMBER;
	
	
	private String version;
	private String generator;
	
	//The working data
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmRelation> relationMap = new HashMap<Long,OsmRelation>();
	
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private List<OsmObject> orderedFeatures = new ArrayList<OsmObject>();
	private FeatureLayerComparator flc = new FeatureLayerComparator();
	
	private HashMap<Object,OsmSegment> segments = new HashMap<Object,OsmSegment>();
	
	//the base data that was checked out
	private List<OsmNodeSrc> srcNodes = new ArrayList<OsmNodeSrc>();
	private List<OsmWaySrc> srcWays = new ArrayList<OsmWaySrc>();
	private List<OsmRelationSrc> srcRelations = new ArrayList<OsmRelationSrc>();
	
	//this is for filtering the features
	private FeatureFilter filter = null;
	
	//the list of edit actions
	private List<EditAction> actions = new ArrayList<EditAction>();
	private int nextAddIndex = 0;
	
	private List<OsmDataChangedListener> listeners = new ArrayList<OsmDataChangedListener>();
	
	//======================
	// Public Methods
	//======================
	
	public void setFilter(FeatureFilter filter) {
		this.filter = filter;
		
		//update the filtered value for all features. */
		filterAll();
	}
	
	/** This adds a data changed listener. */
	public void addDataChangedListener(OsmDataChangedListener listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/** This removes a data changed listener. */
	public void removeDataChangedListener(OsmDataChangedListener listener) {
		listeners.remove(listener);
	}
	
	/** This method returns a ordered list of osm objects, ordered according to
	 * the feature info for the given objects. 
	 * 
	 * @return		An ordered list of objects 
	 */
	public List<OsmObject> getFeatureList() {
		return orderedFeatures;
	}
	
	/** This method gets the specified object of the given id. If there is no matching object,
	 * null is returned.
	 * 
	 * @param id	The id of the object
	 * @return		The object
	 */
	public OsmObject getOsmObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			return getOsmNode(id,false);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			return getOsmWay(id,false);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			return getOsmRelation(id,false);
		}
		else {
			//unknown object
			return null;
		}
	}
	
	public OsmSegment getOsmSegment(OsmNode nodeA, OsmNode nodeB) {
		Object key = OsmSegment.getKey(nodeA, nodeB);
		OsmSegment segment = segments.get(key);
		if(segment == null) {
			segment = new OsmSegment(nodeA,nodeB);
			nodeA.addSegment(segment);
			nodeB.addSegment(segment);
			segments.put(key, segment);
		}
		return segment;
	}
	
	public void discardSegment(OsmSegment segment) {
		segment.getNode1().removeSegment(segment);
		segment.getNode2().removeSegment(segment);
		segments.remove(segment.getKey());
	}

	/** This method gets the node of the given id. If there is no matching node,
	 * null is returned.
	 * 
	 * @param id	The id of the node
	 * @return		The node
	 */
	public OsmNode getOsmNode(long id) {
		return getOsmNode(id,false);
	}
	
	/** This method gets the node of the given id. If there is no matching node,
	 * null is returned.
	 * 
	 * @param id	The id of the way
	 * @return		The way
	 */
	public OsmWay getOsmWay(long id) {
		return getOsmWay(id,false);
	}
	
	/** This method gets the relation of the given id. If there is no matching relation,
	 * null is returned.
	 * 
	 * @param id	The id of the relation
	 * @return		The relation
	 */
	public OsmRelation getOsmRelation(long id) {
		return getOsmRelation(id,false);
	}
	
	/** This method returns a collection of all the nodes. */
	public Collection<OsmNode> getOsmNodes() {
		return nodeMap.values();
	}
	
	/** This method returns a collection of all the ways. */
	public Collection<OsmWay> getOsmWays() {
		return wayMap.values();
	}
	
	/** This method returns a collection of all the relations. */
	public Collection<OsmRelation> getOsmRelations() {
		return relationMap.values();
	}
	
	/** This is a test method to load the latest edit number used. */
	public int test_getLatestEditNumber() {
		return nextEditNumber - 1;
	}
	
	//-----------------------------
	// Undo/Redo Actions
	//-----------------------------
	
	/** This method returns a description of the undo command. If there is no
	 * command to undo, null will be returned. 
	 * 
	 * @return	A description of the action to be done for undo
	 */
	public String getUndoMessage() {
		if(nextAddIndex > 0) {
			EditAction undoAction = actions.get(nextAddIndex - 1);
			String desc = undoAction.getDesc();
			if(desc == null) {
				desc = "Unspecified action";
			}
			return desc;
		}
		else {
			return null;
		}
	}
	
	public String getRedoMessage() {
		if(nextAddIndex < actions.size()) {
			EditAction redoAction = actions.get(nextAddIndex);
			String desc = redoAction.getDesc();
			if(desc == null) {
				desc = "Unspecified action";
			}
			return desc;
		}
		else {
			return null;
		}
	}
	
	public boolean undo() {
		if(nextAddIndex > 0) {
			EditAction undoAction = actions.get(nextAddIndex - 1);
			try {
				nextAddIndex--;
				undoAction.undoAction();
				return true;
			}
			catch(Exception ex) {
				ex.printStackTrace();
				reportFatalError(undoAction.getDesc(),ex.getMessage());
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"There is no action to undo.");
			return false;
		}
	}
	
	public boolean redo() {
		if(nextAddIndex < actions.size()) {
			EditAction redoAction = actions.get(nextAddIndex);
			try {
				nextAddIndex++;
				redoAction.doAction();
				return true;
			}
			catch(Exception ex) {
				ex.printStackTrace();
				reportFatalError(redoAction.getDesc(),ex.getMessage());
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"There is no action to redo.");
			return false;
		}
	}
	
	/** This method clears the command queue. */
	public void clearCommandQueue() {
		actions.clear();
		nextAddIndex = 0;
	}
	
	//-----------------------------
	// Commit Actions
	//-----------------------------
	
	/** This method loads the edit diff data intot the change set. */
	public void loadChangeSet(OsmChangeSet changeSet) {
		
		//load the souce data maps
		HashMap<Long,OsmNodeSrc> srcNodeMap = createNodeSrcMap();
		HashMap<Long,OsmWaySrc> srcWayMap = createWaySrcMap();
		HashMap<Long,OsmRelationSrc> srcRelationMap = createRelationSrcMap();
		
		//look up existing data
		//nodes
		for(OsmNode node:nodeMap.values()) {
			OsmNodeSrc nodeSrc = srcNodeMap.get(node.getId());
			if(nodeSrc != null) {
				//compare and remove
				srcNodeMap.remove(nodeSrc.getId());
				if(nodeSrc.isDifferent(node)) {
					changeSet.addUpdated(nodeSrc,node);
				}
			}
			else {
				changeSet.addCreated(node);
			}
		}
		for(OsmNodeSrc nodeSrc:srcNodeMap.values()) {
			changeSet.addDeleted(nodeSrc);
		}
		//ways
		for(OsmWay way:wayMap.values()) {
			OsmWaySrc waySrc = srcWayMap.get(way.getId());
			if(waySrc != null) {
				//compare and remove
				srcWayMap.remove(waySrc.getId());
				if(waySrc.isDifferent(way)) {
					changeSet.addUpdated(waySrc,way);
				}
			}
			else {
				changeSet.addCreated(way);
			}
		}
		for(OsmWaySrc waySrc:srcWayMap.values()) {
			changeSet.addDeleted(waySrc);
		}
		//relations
		for(OsmRelation relation:relationMap.values()) {		
			OsmRelationSrc relationSrc = srcRelationMap.get(relation.getId());
			if(relationSrc != null) {
				//compare and remove
				srcRelationMap.remove(relationSrc.getId());
				if(relationSrc.isDifferent(relation)) {
					changeSet.addUpdated(relationSrc,relation);
				}
			}
			else {
				changeSet.addCreated(relation);
			}
		}
		for(OsmRelationSrc nodeSrc:srcRelationMap.values()) {
			changeSet.addDeleted(nodeSrc);
		}
	}
	
	public HashMap<Long,OsmNodeSrc> createNodeSrcMap() {
		//load the souce data maps
		HashMap<Long,OsmNodeSrc> srcNodeMap = new HashMap<Long,OsmNodeSrc>();
		for(OsmNodeSrc node:srcNodes) {
			srcNodeMap.put(node.getId(),node);
		}
		return srcNodeMap;
	}
	
	public HashMap<Long,OsmWaySrc> createWaySrcMap() {
		HashMap<Long,OsmWaySrc> srcWayMap = new HashMap<Long,OsmWaySrc>();
		for(OsmWaySrc way:srcWays) {
			srcWayMap.put(way.getId(),way);
		}
		return srcWayMap;
	}
	
	public HashMap<Long,OsmRelationSrc> createRelationSrcMap() {
		HashMap<Long,OsmRelationSrc> srcRelationMap = new HashMap<Long,OsmRelationSrc>();
		for(OsmRelationSrc relation:srcRelations) {
			srcRelationMap.put(relation.getId(),relation);
		}
		return srcRelationMap;
	}
	
	public OsmNodeSrc createNodeSrc(OsmNode node) {
		OsmNodeSrc osmNodeSrc = new OsmNodeSrc();
		osmNodeSrc.copyFrom(node);
		srcNodes.add(osmNodeSrc);
		return osmNodeSrc;
	}
	
	public OsmWaySrc createWaySrc(OsmWay way) {
		OsmWaySrc osmWaySrc = new OsmWaySrc();
		osmWaySrc.copyFrom(way);
		srcWays.add(osmWaySrc);
		return osmWaySrc;
	}
	
	public OsmRelationSrc createRelationSrc(OsmRelation relation) {
		OsmRelationSrc osmRelationSrc = new OsmRelationSrc();
		osmRelationSrc.copyFrom(relation);
		srcRelations.add(osmRelationSrc);
		return osmRelationSrc;
	}
	
	public void removeNodeSrc(OsmNodeSrc osmNodeSrc) {
		srcNodes.remove(osmNodeSrc);
	}
	
	public void removeWaySrc(OsmWaySrc osmWaySrc) {
		srcWays.remove(osmWaySrc);
	}
	
	public void removeRelationSrc(OsmRelationSrc osmRelationSrc) {
		srcRelations.remove(osmRelationSrc);
	}
	
	public void nodeIdChanged(long id1, long id2) {
		OsmNode node = nodeMap.remove(id1);
		nodeMap.put(id2, node);
	}
	
	public void wayIdChanged(long id1, long id2) {
		OsmWay way = wayMap.remove(id1);
		wayMap.put(id2, way);
	}
	
	public void relationIdChanged(long id1, long id2) {
		OsmRelation relation = relationMap.remove(id1);
		relationMap.put(id2, relation);
	}
	
	
	//=============================
	// Package Methods
	//=============================
	
	/** This method saves the actions to the queue. If there are any actions that
	 * can be redone, they will be removed before this is added. 
	 * 
	 * @param action	The action to add 
	 */
	void saveAction(EditAction action) {
		//get rid of any actions after this one
		while(nextAddIndex < actions.size()) {
			actions.remove(actions.size() - 1);
		}
		actions.add(action);
		nextAddIndex = actions.size();
	}
	
	/** This method gets the next available map object id, to be used for generating
	 * temporary IDs. */
	synchronized long getNextId() {
		return nextId--;
	}
	
	/** This method gets the next available edit number, to be used for data
	 * versioning within the editor. */
	synchronized int getNextEditNumber() {
		return nextEditNumber++;
	}
	
		/** This sets the data version specified in the download. */
	void setVersion(String version) {
		this.version = version;
	}
	
	/** This set the generator specified in the download. */
	void setGenerator(String generator) {
		this.generator = generator;
	}
	
	/** This method returns the object with the given id and type. If create reference
	 * is set to true and there is no object with this id, a new unloaded object is
	 * created and returned. If create reference is true null can still be returned if
	 * the type is unrecognized.
	 * 
	 * @param id				The id of the object
	 * @param type				The type of object
	 * @param createReference	If this is true, an new object will be created if one does not exist
	 * @return					The object.
	 */
	OsmObject getOsmObject(long id, String type, boolean createReference) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			return getOsmNode(id,createReference);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			return getOsmWay(id,createReference);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			return getOsmRelation(id,createReference);
		}
		else {
			//unknown object
			return null;
		}
	}
	
	/** This method creates on OsmSrcData object with the given id and type.
	 *  This method will return null if the type is not recognized. There is no
	 * protection against using the same id multiple times. 
	 * 
	 * @param id		The id
	 * @param type		The object type
	 * @return			The created object
	 */
	OsmSrcData createOsmSrcObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			OsmNodeSrc src = new OsmNodeSrc(id);
			this.srcNodes.add(src);
			return src;
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			OsmWaySrc src = new OsmWaySrc(id);
			this.srcWays.add(src);
			return src;
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			OsmRelationSrc src = new OsmRelationSrc(id);
			this.srcRelations.add(src);
			return src;
		}
		else {
			//unknown object
			return null;
		}
	}
	
	/** This method removes the object from the active data. */
	OsmObject removeOsmObject(long id, String type) {
		OsmObject osmObject = null;
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			osmObject = nodeMap.remove(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			osmObject = wayMap.remove(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			osmObject = relationMap.remove(id);
		}
		else {
			//unknown object
		}
		return osmObject;
	}
	

	/** This method returns the object with the given id. If create reference
	 * is set to true and there is no object with this id, a new unloaded object is
	 * created and returned.
	 * 
	 * @param id				The id of the object
	 * @param createReference	If this is true, an new object will be created if one does not exist
	 * @return					The object.
	 */
	OsmNode getOsmNode(long id, boolean createReference) {
		OsmNode node = nodeMap.get(id);
		if((node == null)&&(createReference)) {
			node = new OsmNode(id);
			nodeMap.put(id,node);
		}
		return node;
	}
	
	/** This method returns the object with the given id. If create reference
	 * is set to true and there is no object with this id, a new unloaded object is
	 * created and returned.
	 * 
	 * @param id				The id of the object
	 * @param createReference	If this is true, an new object will be created if one does not exist
	 * @return					The object.
	 */
	OsmWay getOsmWay(long id, boolean createReference) {
		OsmWay way = wayMap.get(id);
		if((way == null)&&(createReference)) {
			way = new OsmWay(id);
			wayMap.put(id,way);
		}
		return way;
	}
	
	/** This method returns the object with the given id. If create reference
	 * is set to true and there is no object with this id, a new unloaded object is
	 * created and returned.
	 * 
	 * @param id				The id of the object
	 * @param createReference	If this is true, an new object will be created if one does not exist
	 * @return					The object.
	 */
	OsmRelation getOsmRelation(long id, boolean createReference) {
		OsmRelation relation =  relationMap.get(id);
		if((relation == null)&&(createReference)) {
			relation = new OsmRelation(id);
			relationMap.put(id,relation);
		}
		return relation;
	}
	
	/** This method runs the given object through the filter. */
	void filterObject(OsmObject osmObject) {
		if(filter != null) {
			filter.filterFeature(osmObject);
		}
		else {
			osmObject.setFilterState(FilterRule.ALL_ENABLED);
		}
	}
	
	/** This method notifies any data changed listeners. It should be called 
	 * when the data changes.
	 * 
	 * @param editNumber	This is the data version for any data changed in this edit. 
	 */
	void dataChanged(int editNumber) {
		//update filter
		filterAll();

long startMsec = System.currentTimeMillis();
long start = System.nanoTime();
orderedFeatures.clear();
long t1 = System.nanoTime();
		for(OsmNode node:nodeMap.values()) {
			orderedFeatures.add(node);
		}
long t2 = System.nanoTime();
		for(OsmWay way:wayMap.values()) {
			orderedFeatures.add(way);
		}
long t3 = System.nanoTime();
		Collections.sort(orderedFeatures,flc);
long t4 = System.nanoTime();
long endMsec = System.currentTimeMillis();
System.out.println(getDelta(start,t1) + getDelta(t1,t2) + getDelta(t2,t3) + getDelta(t3,t4) + getDelta(start,t4));
System.out.println(endMsec - startMsec);
		//notify
		for(OsmDataChangedListener listener:listeners) {
			listener.osmDataChanged(editNumber);
		}
	}
	
private String getDelta(long t1, long t2) {
	return String.valueOf((t2 - t1)/1000000.0) + " ";
}
	//==========================
	// Private Methods
	//==========================
	
	/** this runs all object through the filter. */
	private void filterAll() {
		for(OsmNode node:nodeMap.values()) {
			filterObject(node);
		}
		for(OsmWay way:wayMap.values()) {
			filterObject(way);
		}
		//filter segments according to node state
		for(OsmSegment segment:segments.values()) {
			int state1 = segment.getNode1().getFilterState();
			int state2 = segment.getNode2().getFilterState();
			segment.setFilterState(state1 & state2);
		}
	}
	
	private void reportFatalError(String actionDesc, String exceptionMsg) {
		JOptionPane.showMessageDialog(null,"There was a fatal error on the action: " + actionDesc +
				"; " + exceptionMsg + "The application must exit");
		System.exit(-1);
	}
	
	//========================
	// Classes
	//========================
	
	private class FeatureLayerComparator implements Comparator<OsmObject> {
		public int compare(OsmObject o1, OsmObject o2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			FeatureInfo fi;
			fi= o1.getFeatureInfo();
			int ord1 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			fi = o2.getFeatureInfo();
			int ord2 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			return ord1 - ord2;
		}
	}

}
