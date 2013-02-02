package intransix.osm.termite.map.workingdata;

/**
 * This object allows external objects to tie data to individual OsmObjects. To
 * do this, register for a piggy back data spot in OsmData.
 * @author sutter
 */
public class PiggybackData {
	
	private int dataVersion;
	
	public void markAsUpToDate(OsmObject osmObject) {
		this.dataVersion = osmObject.getDataVersion();
	}
	
	public boolean isUpToDate(OsmObject parent) {
		return (parent.getDataVersion() == dataVersion);
	}
	
}
