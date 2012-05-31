package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmRelation extends OsmObject {
	
	public final static String TAG_TYPE = "type";
	public final static String TYPE_STRUCTURE = "structure";
	public final static String TYPE_LEVEL = "level";
	public final static String TYPE_MULTIPOLYGON = "multipolygon";
	
	private ArrayList<OsmMember> members = new ArrayList<OsmMember>();
	
	/** The argument is the combined type + osmId string. */
	public OsmRelation(long id) {
		super(TYPE_RELATION, id);
	}
	
	public ArrayList<OsmMember> getMembers() {
		return members;
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("relation")) {
			//mark loaded here - maybe we should wait though
			this.setIsLoaded(true);
		}
		else if(name.equalsIgnoreCase("member")) {
			String type = attr.getValue("type");
			long ref = OsmXml.getLong(attr,"ref",INVALID_ID);
			String role = attr.getValue("role");
			OsmObject object = root.getOsmObject(ref,type);
			if(object != null) {
				OsmMember member = new OsmMember();
				member.role = role;
				member.type = type;
				member.member = object;
				members.add(member);
			}
		}
	}
}
