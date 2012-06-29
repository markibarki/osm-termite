package intransix.osm.termite.map.data;

import java.util.*;

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
	private GraduatedList<OsmObject> orderedMapObjects = new GraduatedList<OsmObject>();
	
	//the base data that was checked out
	private List<OsmNodeSrc> srcNodes = new ArrayList<OsmNodeSrc>();
	private List<OsmWaySrc> srcWays = new ArrayList<OsmWaySrc>();
	private List<OsmRelationSrc> srcRelations = new ArrayList<OsmRelationSrc>();
	
	//the list of edit actions
	private List<EditAction> actions = new ArrayList<EditAction>();
	
	//======================
	// Public Methods
	//======================
	
	/** This method returns a ordered list of osm objects, ordered according to
	 * the feature info for the given objects. 
	 * 
	 * @return		An ordered list of objects 
	 */
	public GraduatedList<OsmObject> getOrderedList() {
		return orderedMapObjects;
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
	
	//=============================
	// Package Methods
	//=============================
	
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

}
