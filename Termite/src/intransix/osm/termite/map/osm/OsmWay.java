package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import intransix.osm.termite.map.MapObject;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	private ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
	
	private ArrayList<OsmRelation> parentRelation = new ArrayList<OsmRelation>();

	/** The argument is the combined type + osmId string. */
	public OsmWay(String id) {
		super(id);
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("way")) {
		}
		else if(name.equalsIgnoreCase("nd")) {
			String ref = attr.getValue("ref");
			OsmNode node = (OsmNode)root.getOsmObject(ref,"node");
			if(node != null) {
				this.nodes.add(node);
				node.addParentWay(this);
			}
		}
	}
	
}
