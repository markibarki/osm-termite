package intransix.osm.termite.app.mapdata.publish;

import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.net.RequestSource;
import java.io.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class VersionRequestSource implements RequestSource {
	
	private final static int INVALID_VERSION = -1;
	
	private String fileName;
	private long key;
	private int version;
	
	//====================
	// Public Methods
	//====================
	
	public VersionRequestSource(String fileName, long key) {
		this.fileName = fileName;
		this.key = key;
	}
	
	public int getVersion() {
		return version;
	}
	
	/** This method should return the url. */
	public String getUrl() {
		String url = OsmModel.PUBLISH_SERVICE + "/version/" + fileName + "/" + key;
		return url;
	}
	
	/** This method returns the HTTP request method. */
	public String getMethod() {
		return "GET";
	}
	
	/** This method should return true if there is a payload. */
	public boolean getHasPayload() {
		return false;
	}
	
	/** This method should be implemented to write the request body, if there is a payload.
	 * The output stream is closed from the NetRequest code. */
	public void writeRequestBody(OutputStream outputStream) throws Exception {
	}
	
	/** This method will be called to red the response body. The input stream is
	 * buffered input stream and it is closed from the NetRequest code. */
	public void readResponseBody(int responseCode, InputStream inputStream) throws Exception {
		if(responseCode == 200) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			while((c = inputStream.read()) != -1) {
				baos.write((byte)c);
			}
			byte[] data = baos.toByteArray();
			if(data.length > 0) {
				String response = new String(data,"UTF-8");
				JSONObject json = new JSONObject(response);
				version = json.getInt("version");
			}
			else {
				version = INVALID_VERSION;
			}
		}
		else {
			version = INVALID_VERSION;
		}
	}
	
}
