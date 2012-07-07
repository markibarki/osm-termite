package intransix.osm.termite.map.data;

/**
 *
 * @author sutter
 */
public class OsmSegmentWrapper {
	public boolean isFwd;
	public OsmSegment segment;

	public OsmSegmentWrapper(OsmSegment segment, boolean isFwd) {
		this.segment = segment;
		this.isFwd = isFwd;
	}
	
	public OsmSegmentWrapper(OsmSegment segment, OsmNode nodeA, OsmNode nodeB) {
		this.segment = segment;
		this.isFwd = (segment.getNode1() == nodeA);
	}
}
