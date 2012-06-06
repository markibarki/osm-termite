package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteWay;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	//=======================
	//
	//=======================
	private ArrayList<OsmNode> nodes = new ArrayList<OsmNode>();
	
	private TermiteWay termiteWay = null;

	//=======================
	//
	//=======================
	
	/** Constructor. */
	public OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	
	/** This method gets the nodes for this way. */
	public ArrayList<OsmNode> getNodes() {
		return nodes;
	}
	
	/** This method sets the TermiteWay for this OsmWay. */
	public void setTermiteWay(TermiteWay termiteWay) {
		this.termiteWay = termiteWay;
	}
	
	/** This method gets the TermiteWay for this OsmWay. */
	public TermiteWay getTermiteWay() {
		return termiteWay;
	}
	
	/** This method is used in parsing XML. */
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
			long ref = OsmParser.getLong(attr,"ref",INVALID_ID);
			OsmNode node = (OsmNode)osmData.getOsmObject(ref,"node");
			if(node != null) {
				addNode(node);
			}
		}
	}
	
	//==========================
	// package methods
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
	
	/** This method adds a node to the way. */
	private void addNode(OsmNode node) {
		this.nodes.add(node);
		node.addWay(this);
	}
	
	/** This method removes a node from the way. */
	private void removeNode(OsmNode node) {
		this.nodes.remove(node);
		node.removeWay(this);
	}
	
}
