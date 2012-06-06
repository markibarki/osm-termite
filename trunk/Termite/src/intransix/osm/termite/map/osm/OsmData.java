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
	private synchronized long getNextId() {
		return nextId++;
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
	
	OsmObject getOsmObject(long id, String type) {
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
		OsmNode node = nodeMap.get(id);
		if(node == null) {
			node = new OsmNode(id);
			nodeMap.put(id,node);
		}
		return node;
	}
	
	public OsmWay getOsmWay(long id) {
		OsmWay way = wayMap.get(id);
		if(way == null) {
			way = new OsmWay(id);
			wayMap.put(id,way);
		}
		return way;
	}
	
	public OsmRelation getOsmRelation(long id) {
		OsmRelation relation = relationMap.get(id);
		if(relation == null) {
			relation = new OsmRelation(id);
			relationMap.put(id,relation);
		}
		return relation;
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
			node.createCopy(dataCopy);
		}
		for(OsmWay way:getOsmWays()) {
			way.createCopy(dataCopy);
		}
		for(OsmRelation relation:getOsmRelations()) {
			relation.createCopy(dataCopy);
		}
		return dataCopy;
	}

}
