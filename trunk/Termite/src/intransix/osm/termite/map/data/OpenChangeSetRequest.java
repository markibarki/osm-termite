package intransix.osm.termite.map.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import javax.xml.stream.XMLOutputFactory;

/**
 * This class parses an OSM request data.
 * 
 * @author sutter
 */
public class OpenChangeSetRequest implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
	private String url;
	
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public OpenChangeSetRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		
		url = OsmModel.OSM_SERVER + OsmModel.OPEN_CHANGESET_REQUEST_PATH;
	}
	
	/** This method should return the url. */
	@Override
	public String getUrl() {
		return url;
	}
	
	/** This method returns the HTTP request method. */
	@Override
	public String getMethod() {
		return "PUT";
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
		
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("osm");
		xmlWriter.writeStartElement("changeset");
		//created by
		xmlWriter.writeEmptyElement("tag");
		xmlWriter.writeAttribute("k","created_by");
		xmlWriter.writeAttribute("v","Termite v0");
		//comment
		String message = changeSet.getMessage();
		if(message == null) message = "--no message provided--";
		xmlWriter.writeEmptyElement("tag");
		xmlWriter.writeAttribute("k","comment");
		xmlWriter.writeAttribute("v",message);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement();
		xmlWriter.writeEndDocument();
		
		xmlWriter.flush();
		xmlWriter.close();
	}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(InputStream is) throws Exception {
		int c;
		StringBuilder sb = new StringBuilder();
		while((c = is.read()) != -1) {
			sb.append((char)c);
		}
		
		//read the id
		long id = Long.decode(sb.toString());
		changeSet.setId(id);
	}

}
