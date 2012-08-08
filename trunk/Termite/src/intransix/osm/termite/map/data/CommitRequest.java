package intransix.osm.termite.map.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class manages on OSM commit request.
 * 
 * @author sutter
 */
public class CommitRequest extends RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
	
	
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public CommitRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		this.setMethod("POST");
		this.setHasPayload(true);
		
		String path = String.format(OsmModel.COMMIT_REQUEST_PATH,changeSet.getId());
		setUrl(OsmModel.OSM_SERVER + path);
	}
	
	/** This method should be implemented to write the XMl body, if there is a payload. */
	@Override
	public void writeXml(XMLStreamWriter xmlWriter) {
	}
	
	/** This method is called after the response is parsed. */
	@Override
	public void parsingCompleted() {
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
