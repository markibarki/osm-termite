package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteWay;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWay extends OsmObject implements EditData<OsmWay> {
	
	//=======================
	//
	//=======================
	
	private ArrayList<Long> nodeIds = new ArrayList<Long>();
	
	private TermiteWay termiteWay = null;

	//=======================
	//
	//=======================
	
	/** Constructor. */
	public OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	
	/** This method gets the nodes for this way. */
	public ArrayList<Long> getNodeIds() {
		return nodeIds;
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
			nodeIds.add(ref);
		}
	}
	
		//----------------------
	// Edit Methods
	//----------------------
	
	/** This method is not used on create/delete. */
	@Override
	public EditData<OsmWay> readInitialData(OsmWay objectToDelete) throws UnchangedException {
		return null;
	}
		
	/** This method is only called on create. */
	@Override
	public void writeData(OsmWay newObject) throws UnchangedException, Exception {
		this.copyInto(newObject);
	}
	
	//==========================
	// package methods
	//==========================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	void copyInto(OsmWay newWay) {
		for(Long nodeId:this.nodeIds) {
			newWay.nodeIds.add(nodeId);
		}
		super.copyInto(newWay);
	}
}
