package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	private ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();

	/** The argument is the combined type + osmId string. */
	public OsmWay(long id) {
		super(TYPE_WAY,id);
	}
	
	public ArrayList<OsmNode> getNodes() {
		return nodes;
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("way")) {
			//mark loaded here - maybe we should wait though
			this.setIsLoaded(true);
		}
		else if(name.equalsIgnoreCase("nd")) {
			long ref = OsmXml.getLong(attr,"ref",INVALID_ID);
			OsmNode node = (OsmNode)root.getOsmObject(ref,"node");
			if(node != null) {
				this.nodes.add(node);
			}
		}
	}
	
}
