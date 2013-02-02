package intransix.osm.termite.map.dataset;

import intransix.osm.termite.util.PropertyPair;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.Point2D;
import java.util.List;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author sutter
 */
public class OsmChangeObject implements Comparable<OsmChangeObject> {


	private OsmSrcData initialObject;
	private OsmSrcData finalObject;

	public OsmChangeObject(OsmSrcData initialObject, OsmSrcData finalObject) {
		this.initialObject = initialObject;
		this.finalObject = finalObject;
	}
	
	public OsmSrcData getInitialObject() {
		return initialObject;
	}
	
	public OsmSrcData getFinalObject() {
		return finalObject;
	}

	/** This is used to order objects during creation, so it doesn't try to 
		* use an object that has not been created yet. */
	@Override
	public int compareTo(OsmChangeObject changeObject) {
		//only rearrange if there is an osm object (don't both with deletes)
		if((finalObject == null)||(changeObject.finalObject == null)) return 0;

		if(finalObject instanceof OsmNodeSrc) {
			if(changeObject.finalObject instanceof OsmNodeSrc) return 0;
			else return -1;
		}
		else if(finalObject instanceof OsmWaySrc) {
			if(changeObject.finalObject instanceof OsmNodeSrc) return 1;
			else if(changeObject.finalObject instanceof OsmWaySrc) return 0;
			else return -1;
		}
		else if(finalObject instanceof OsmRelationSrc) {
			if(changeObject.finalObject instanceof OsmRelationSrc) {
//I should check if the relation contains a reference to a relation that
//is also being created. If so update the order. If this can not be done
//create an update instruction for it?
				return 0;
			}
			else {
				return 1;
			}
		}
		else {
			//this shouldn't happen
			return 0;
		}

	}
}
