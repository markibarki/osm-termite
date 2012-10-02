package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.map.dataset.*;
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
	
	//The working data
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmRelation> relationMap = new HashMap<Long,OsmRelation>();
	
	private HashMap<Object,OsmSegment> segments = new HashMap<Object,OsmSegment>();
	
	//======================
	// Public Methods
	//======================
	
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
	
	/** This method returns a collection of all the relations. */
	public Collection<OsmSegment> getOsmSegments() {
		return segments.values();
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
	public OsmObject getOsmObject(long id, String type, boolean createReference) {
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
	
	/** This method removes the object from the active data. */
	public OsmObject removeOsmObject(long id, String type) {
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
	public OsmNode getOsmNode(long id, boolean createReference) {
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
	public OsmWay getOsmWay(long id, boolean createReference) {
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
	public OsmRelation getOsmRelation(long id, boolean createReference) {
		OsmRelation relation =  relationMap.get(id);
		if((relation == null)&&(createReference)) {
			relation = new OsmRelation(id);
			relationMap.put(id,relation);
		}
		return relation;
	}
	
	public void loadFromDataSet(OsmDataSet dataSet, int initialVersionNumber) {
		for(OsmNodeSrc nodeSrc:dataSet.getSrcNodes()) {
			OsmNode node = this.getOsmNode(nodeSrc.getId(),true);
			node.copyFrom(nodeSrc, this);
			node.setDataVersion(initialVersionNumber);
		}
		for(OsmWaySrc waySrc:dataSet.getSrcWays()) {
			OsmWay way = this.getOsmWay(waySrc.getId(),true);
			way.copyFrom(waySrc, this);
			way.setDataVersion(initialVersionNumber);
		}
		for(OsmRelationSrc relationSrc:dataSet.getSrcRelations()) {
			OsmRelation relation = this.getOsmRelation(relationSrc.getId(),true);
			relation.copyFrom(relationSrc, this);
			relation.setDataVersion(initialVersionNumber);
		}
	}
	

	//==========================
	// Private Methods
	//==========================

	

}
