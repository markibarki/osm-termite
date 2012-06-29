package intransix.osm.termite.map.data;

import java.util.*;
import org.xml.sax.Attributes;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWaySrc extends OsmSrcData<OsmWay> {
	
	//=======================
	// Properties
	//=======================
		
	private List<Long> nodeIds = new ArrayList<Long>();

	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmWaySrc() {
		super(OsmModel.TYPE_WAY,OsmData.INVALID_ID);
	}
	
	/** This method gets the nodes for this way. It can be used to populate the
	 * nodes in the way. */
	public List<Long> getNodeIds() {
		return nodeIds;
	}
	
	//-----------------------
	// Parse Methods
	//-----------------------
	
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
			long ref = OsmParser.getLong(attr,"ref",OsmData.INVALID_ID);
			nodeIds.add(ref);
		}
	}

	//====================
	// Package Methods
	//====================
	
	OsmWaySrc(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	
	/** Copies the source data to this instance. */
	@Override
	void copyInto(OsmWay target, OsmData osmData) {
		super.copyInto(target,osmData);
		
		Collection nodes = target.getNodes();
		nodes.clear();
		for(Long id:nodeIds) {
			OsmNode node = osmData.getOsmNode(id, true);
			if(node != null) {
				nodes.add(node);
				node.addWay(target);
			}
			else {
				//this shouldn't happen
				//if so, we will ignore the node
			}
		}
	}
	
	/** This method copies the src data to this object. */
	@Override
	void copyFrom(OsmWay src) {
		super.copyFrom(src);
		
		nodeIds.clear();
		for(OsmNode node:src.getNodes()) {
			nodeIds.add(node.getId());
		}
	}
}
