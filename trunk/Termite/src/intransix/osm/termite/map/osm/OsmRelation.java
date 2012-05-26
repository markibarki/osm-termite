package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmRelation extends OsmObject {
	
	private ArrayList<OsmObject> members = new ArrayList<OsmObject>();
	
	public class OsmMember {
		String role;
		String type;
		OsmObject member;
	}
	
	/** The argument is the combined type + osmId string. */
	public OsmRelation(String id) {
		super(id);
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("relation")) {
		}
		else if(name.equalsIgnoreCase("member")) {
			String type = attr.getValue("type");
			String ref = attr.getValue("ref");
			String role = attr.getValue("role");
			OsmObject object = root.getOsmObject(ref,type);
			if(object != null) {
				OsmMember member = new OsmMember();
				member.role = role;
				member.type = type;
				member.member = object;
			}
		}
	}
}
