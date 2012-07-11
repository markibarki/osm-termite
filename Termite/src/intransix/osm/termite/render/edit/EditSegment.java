package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmSegment;

/**
 *
 * @author sutter
 */
public class EditSegment {
	public EditNode en1;
	public EditNode en2;
	public OsmSegment osmSegment;
	
	public EditSegment(EditNode en1, EditNode en2, OsmSegment osmSegment) {
		this.en1 = en1;
		this.en2 = en2;
		this.osmSegment = osmSegment;
	}	
}
