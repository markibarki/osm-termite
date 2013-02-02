package intransix.osm.termite.app.mapdata.commit;

import intransix.osm.termite.map.dataset.OsmChangeSet;
import intransix.osm.termite.map.workingdata.OsmModel;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import intransix.osm.termite.net.NetRequest;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class parses an OSM request data.
 * 
 * @author sutter
 */
public class CloseChangeSetRequest implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
	private String url;
			
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public CloseChangeSetRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		
		String path = String.format(OsmModel.CLOSE_CHANGESET_REQUEST_PATH,changeSet.getId());
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
		return "PUT";
	}
	
	/** This method should return true if there is a payload. */
	@Override
	public boolean getHasPayload() {
		return false;
	}
	
		/** This method should be implemented to write the XMl body, if there is a payload. */
	@Override
	public void writeRequestBody(OutputStream os) throws Exception {}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(int responseCode, InputStream is) throws Exception {
		if(responseCode != 200) {
			String bodyText = NetRequest.readText(is);
			System.out.println(bodyText);
		}
	}
}
