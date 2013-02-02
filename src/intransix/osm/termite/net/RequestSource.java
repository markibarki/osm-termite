package intransix.osm.termite.net;

import java.io.*;

/**
 * This is the object provides the request info for an XmlRequest.
 * 
 * @author sutter
 */
public interface RequestSource {
	
	//====================
	// Public Methods
	//====================
	
	/** This method should return the url. */
	public String getUrl();
	
	/** This method returns the HTTP request method. */
	public String getMethod();
	
	/** This method should return true if there is a payload. */
	public boolean getHasPayload();
	
	/** This method should be implemented to write the request body, if there is a payload.
	 * The output stream is closed from the NetRequest code. */
	public void writeRequestBody(OutputStream outputStream) throws Exception;
	
	/** This method will be called to red the response body. The input stream is
	 * buffered input stream and it is closed from the NetRequest code. */
	public void readResponseBody(int responseCode, InputStream inputStream) throws Exception;

}
