package intransix.osm.termite.map.osm;

import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmData {
	
	//==========================
	// Private Fields
	//==========================
	
	public final static long INVALID_ID = 0;
	
	private final static long FIRST_ID = -1;
	private final static int FIRST_EDIT_NUMBER = 1;
	
	private long nextId = FIRST_ID;
	private int nextEditNumber = FIRST_EDIT_NUMBER;
	
	private String version;
	private String generator;
	
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmRelation> relationMap = new HashMap<Long,OsmRelation>();
	
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private GraduatedList<OsmObject> orderedMapObjects = new GraduatedList<OsmObject>();
	
	private List<OsmNodeSrc> srcNodes = new ArrayList<OsmNodeSrc>();
	private List<OsmWaySrc> srcWays = new ArrayList<OsmWaySrc>();
	private List<OsmRelationSrc> srcRelations = new ArrayList<OsmRelationSrc>();
	
	private List<EditAction> actions = new ArrayList<EditAction>();
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	
	public GraduatedList<OsmObject> getOrderedList() {
		return orderedMapObjects;
	}
	
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

	public OsmNode getOsmNode(long id) {
		return getOsmNode(id,false);
	}
	
	public OsmWay getOsmWay(long id) {
		return getOsmWay(id,false);
	}
	
	public OsmRelation getOsmRelation(long id) {
		return getOsmRelation(id,false);
	}
	
	public Collection<OsmNode> getOsmNodes() {
		return nodeMap.values();
	}
	
	public Collection<OsmWay> getOsmWays() {
		return wayMap.values();
	}
	
	public Collection<OsmRelation> getOsmRelations() {
		return relationMap.values();
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
	void removeOsmObject(long id, String type) {
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
	}
	

	OsmNode getOsmNode(long id, boolean createReference) {
		OsmNode node = nodeMap.get(id);
		if((node == null)&&(createReference)) {
			node = new OsmNode(id);
			nodeMap.put(id,node);
		}
		return node;
	}
	
	OsmWay getOsmWay(long id, boolean createReference) {
		OsmWay way = wayMap.get(id);
		if((way == null)&&(createReference)) {
			way = new OsmWay(id);
			wayMap.put(id,way);
		}
		return way;
	}
	
	OsmRelation getOsmRelation(long id, boolean createReference) {
		OsmRelation relation =  relationMap.get(id);
		if((relation == null)&&(createReference)) {
			relation = new OsmRelation(id);
			relationMap.put(id,relation);
		}
		return relation;
	}

}
