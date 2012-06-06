package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteWay;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	private ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
	
	private TermiteWay termiteWay = null;

	/** The argument is the combined type + osmId string. */
	public OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	
	public ArrayList<OsmNode> getNodes() {
		return nodes;
	}
	
	public void setTermiteWay(TermiteWay termiteWay) {
		this.termiteWay = termiteWay;
	}
	
	public TermiteWay getTermiteWay() {
		return termiteWay;
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//let the parent parse
		super.startElement(name,attr,osmData);
		
		//parse this node
		if(name.equalsIgnoreCase("way")) {
			//parse common stuff
			parseElementBase(name, attr);
		}
		else if(name.equalsIgnoreCase("nd")) {
			long ref = OsmXml.getLong(attr,"ref",INVALID_ID);
			OsmNode node = (OsmNode)osmData.getOsmObject(ref,"node");
			if(node != null) {
				addNode(node);
			}
		}
	}
	
	//==========================
	// private methods
	//==========================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	void createCopy(OsmData destOsmData) {
		OsmWay newWay = destOsmData.getOsmWay(this.getId());
		for(OsmNode node:this.nodes) {
			OsmNode newNode = destOsmData.getOsmNode(node.getId());
			newWay.addNode(newNode);
		}
		copyFromBase(newWay);
	}
	
	private void addNode(OsmNode node) {
		this.nodes.add(node);
		node.addWay(this);
	}
	
	private void removeNode(OsmNode node) {
		this.nodes.remove(node);
		node.removeWay(this);
	}
	
}
