package intransix.osm.termite.map.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class parses an OSM request data.
 * 
 * @author sutter
 */
public class CloseChangeSetRequest extends RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
			
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public CloseChangeSetRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		this.setMethod("PUT");
		this.setHasPayload(false);
		
		String path = String.format(OsmModel.CLOSE_CHANGESET_REQUEST_PATH,changeSet.getId());
		setUrl(OsmModel.OSM_SERVER + path);
	}
	
	/** This method gets the URL. */
	@Override
	public String getUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(OsmModel.OSM_SERVER);
		sb.append(OsmModel.CLOSE_CHANGESET_REQUEST_PATH);
		return sb.toString();
	}
	
	/** This is the SAX parser start element method. */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
//		if(activeObject != null) {
//			activeObject.startElement(name,attributes,osmData);
//		}
//		else if(name.equalsIgnoreCase("osm")) {
//			String version = attributes.getValue("version");
//			osmData.setVersion(version);
//			String generator = attributes.getValue("generator");
//			osmData.setGenerator(generator);
//		}
//		else {
//			long osmId = getLong(attributes,"id",OsmData.INVALID_ID);
//			if(osmId != OsmData.INVALID_ID) {
//				//lookup or create object
//				activeObject = osmData.createOsmSrcObject(osmId,name);
//				if(activeObject != null) {
//					//we are processing a new object
//					activeObjectName = name;
//					activeObject.startElement(name,attributes,osmData);
//				}
//			}
//		}
	}

	/** This is the SAX parser end element method. */
	@Override
	public void endElement(String uri, String localName,
			String name) throws SAXException {

//		if(activeObjectName != null) {
//			if(activeObjectName.equalsIgnoreCase(name)) {
//				if(activeObject != null) {
//					activeObject.endElement(osmData);
//				}
//				activeObject = null;
//				activeObjectName = null;
//			}
//		}
	}

	/** This is the SAX parser characters method. */
	public void characters(char ch[], int start, int length) throws SAXException {
	}

}
