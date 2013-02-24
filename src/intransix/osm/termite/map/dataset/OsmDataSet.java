package intransix.osm.termite.map.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sutter
 */
public class OsmDataSet {
	
	private String version;
	private String generator;
	
	//the base data that was checked out
	private HashMap<Long,OsmNodeSrc> srcNodes = new HashMap<Long,OsmNodeSrc>();
	private HashMap<Long,OsmWaySrc> srcWays = new HashMap<Long,OsmWaySrc>();
	private HashMap<Long,OsmRelationSrc> srcRelations = new HashMap<Long,OsmRelationSrc>();
	
	/** This sets the data version specified in the download. */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/** This set the generator specified in the download. */
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	
	//-----------------------------
	// Commit Actions
	//-----------------------------
	
	public Collection<OsmNodeSrc> getSrcNodes() {
		return srcNodes.values();
	}
	
	public Collection<OsmWaySrc> getSrcWays() {
		return srcWays.values();
	}
	
	public Collection<OsmRelationSrc> getSrcRelations() {
		return srcRelations.values();
	}
	
	public OsmNodeSrc getNodeSrc(Long id) {
		return srcNodes.get(id);
	}
	
	public OsmWaySrc getWaySrc(Long id) {
		return srcWays.get(id);
	}
	
	public OsmRelationSrc getRelationSrc(Long id) {
		return srcRelations.get(id);
	}
	
	public void putNodeSrc(OsmNodeSrc node) {
		srcNodes.put(node.getId(),node);
	}
	
	public void putWaySrc(OsmWaySrc way) {
		srcWays.put(way.getId(),way);
	}
	
	public void putRelationSrc(OsmRelationSrc relation) {
		srcRelations.put(relation.getId(),relation);
	}
	
	public void removeNodeSrc(Long id) {
		srcNodes.remove(id);
	}
	
	public void removeWaySrc(Long id) {
		srcWays.remove(id);
	}
	
	public void removeRelationSrc(Long id) {
		srcRelations.remove(id);
	}
}
