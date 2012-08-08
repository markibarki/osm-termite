package intransix.osm.termite.map.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;

import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class manages on OSM commit request.
 * 
 * @author sutter
 */
public class CommitRequest extends DefaultHandler implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
	private String url;
	
	
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public CommitRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		
		String path = String.format(OsmModel.COMMIT_REQUEST_PATH,changeSet.getId());
		url = OsmModel.OSM_SERVER + path;
	}
	
	/** This method should return the url. */
	@Override
	public String getUrl() {
		return url;
	}
	
	/** This method returns the HTTP request method. */
	@Override
	public String getMethod() {
		return "POST";
	}
	
	/** This method should return true if there is a payload. */
	@Override
	public boolean getHasPayload() {
		return true;
	}
	
	/** This method should be implemented to write the XMl body, if there is a payload. */
	@Override
	public void writeRequestBody(OutputStream os) throws Exception {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os);
		
//add content		
		
		xmlWriter.flush();
		xmlWriter.close();
	}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(InputStream is) throws Exception {
		//parse xml
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();	
		saxParser.parse(is,this);
		
		//post-parsing actions
//add these
		
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
