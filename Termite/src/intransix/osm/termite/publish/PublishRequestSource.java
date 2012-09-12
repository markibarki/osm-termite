package intransix.osm.termite.publish;

import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.net.RequestSource;
import java.io.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class PublishRequestSource implements RequestSource {
	
	public final static String STRUCTURE_FILENAME = "indoormap";
	public final static String LEVEL_FILENAME = "lvlgeom";
	
	private String fileName;
	private long key;
	private int version;
	private JSONObject body;
	private String response;
	
	//====================
	// Public Methods
	//====================
	
	public PublishRequestSource(String fileName, long key, int version, JSONObject body) {
		this.fileName = fileName;
		this.key = key;
		this.version = version;
		this.body = body;
	}
	
	public String getResponse() {
		return response;
	}
	
	/** This method should return the url. */
	public String getUrl() {
		String url = OsmModel.PUBLISH_SERVICE + "/file/" + fileName + "/" + key + "/" + version;
		return url;
	}
	
	/** This method returns the HTTP request method. */
	public String getMethod() {
		return "PUT";
	}
	
	/** This method should return true if there is a payload. */
	public boolean getHasPayload() {
		return true;
	}
	
	/** This method should be implemented to write the request body, if there is a payload.
	 * The output stream is closed from the NetRequest code. */
	public void writeRequestBody(OutputStream outputStream) throws Exception {
		String bodyText = body.toString();
		byte[] bytes = bodyText.getBytes("UTF-8");
		outputStream.write(bytes);
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
				response = new String(data,"UTF-8");
			}
			else {
				response = null;
			}
		}
		else {
			response = null;
		}
	}
	
}
