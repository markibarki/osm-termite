package intransix.osm.termite.app.mapdata.publish;

import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.net.RequestSource;
import java.io.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class PublishFeatureRequestSource implements RequestSource {
	
	public final static String STRUCTURE_LAYERNAME = "linkedmap";
	
	private String layerName;
	private long key;
	private JSONObject geoJson;
	private String response;
	
	//====================
	// Public Methods
	//====================
	
	public PublishFeatureRequestSource(String layerName, long key, JSONObject geoJson) {
		this.layerName = layerName;
		this.key = key;
		this.geoJson = geoJson;
	}
	
	public String getResponse() {
		return response;
	}
	
	/** This method should return the url. */
	public String getUrl() {
		String url = OsmModel.PUBLISH_SERVICE + "/feature/" + layerName + "/" + key;
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
		String bodyText = geoJson.toString();
		byte[] bytes = bodyText.getBytes("UTF-8");
		outputStream.write(bytes);
	}
	
	/** This method will be called to red the response body. The input stream is
	 * buffered input stream and it is closed from the NetRequest code. */
	public void readResponseBody(int responseCode, InputStream inputStream) throws Exception {
	}
	
}
