package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWay extends OsmObject<OsmWay> {
	
	//=======================
	// Properties
	//=======================
	
	private ArrayList<Long> nodeIds = new ArrayList<Long>();

	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	public OsmWay() {
		super(OsmModel.TYPE_WAY,OsmObject.INVALID_ID);
	}
	
	/** This method gets the nodes for this way. */
	public ArrayList<Long> getNodeIds() {
		return nodeIds;
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
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	public void copyInto(OsmWay newWay) {
		for(Long nodeId:this.nodeIds) {
			newWay.nodeIds.add(nodeId);
		}
		super.copyInto(newWay);
	}
}
