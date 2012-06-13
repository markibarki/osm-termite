package intransix.osm.termite.map.osm;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author sutter
 */
public class OsmData {
	
		private final static long FIRST_ID = -1;
		/** This method gets the next available termite id, to be used for generating
	 * temporary IDs. */
	public synchronized long getNextId() {
		return nextId--;
	}
	private long nextId = FIRST_ID;
	
	//==========================
	// Private Fields
	//==========================
	
	private String version;
	private String generator;
	
	private HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	private HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	private HashMap<Long,OsmRelation> relationMap = new HashMap<Long,OsmRelation>();
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	
	public OsmObject getOsmObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			return getOsmNode(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			return getOsmWay(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			return getOsmRelation(id);
		}
		else {
			//unknown object
			return null;
		}
	}

	public OsmNode getOsmNode(long id) {
		return nodeMap.get(id);
	}
	
	public OsmWay getOsmWay(long id) {
		return wayMap.get(id);
	}
	
	public OsmRelation getOsmRelation(long id) {
		return relationMap.get(id);
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
	
	/** This method makes a copy of the OsmData object, with new instances of
	 * each internal object. */
	public OsmData createCopy() {
		OsmData dataCopy = new OsmData();
		for(OsmNode node:getOsmNodes()) {
			OsmNode nodeCopy = dataCopy.createOsmNode(node.getId());
			node.copyInto(nodeCopy);
		}
		for(OsmWay way:getOsmWays()) {
			OsmWay wayCopy = dataCopy.createOsmWay(way.getId());
			way.copyInto(wayCopy);
		}
		for(OsmRelation relation:getOsmRelations()) {
			OsmRelation relationCopy = dataCopy.createOsmRelation(relation.getId());
			relation.copyInto(relationCopy);
		}
		return dataCopy;
	}
	
	/** This method creates an object of the given type with the given id. */
	public OsmObject createOsmObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			return createOsmNode(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			return createOsmWay(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			return createOsmRelation(id);
		}
		else {
			//unknown object
			return null;
		}
	}
	
	/** This method removes the object from the active data. */
	public void removeOsmObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			removeOsmNode(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			removeOsmWay(id);
		}
		else if(type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			removeOsmRelation(id);
		}
		else {
			//unknown object
		}
	}
	
	
	public OsmNode createOsmNode(long id) {
		OsmNode node = new OsmNode(id);
		nodeMap.put(id,node);
		return node;
	}
	
	public OsmWay createOsmWay(long id) {
		OsmWay way = new OsmWay(id);
		wayMap.put(id,way);
		return way;
	}
	
	public OsmRelation createOsmRelation(long id) {
		OsmRelation relation = new OsmRelation(id);
		relationMap.put(id,relation);
		return relation;
	}
	
	public void removeOsmNode(long id) {
		nodeMap.remove(id);
	}
	
	public void removeOsmWay(long id) {
		wayMap.remove(id);
	}
	
	public void removeOsmRelation(long id) {
		relationMap.remove(id);
	}

}
