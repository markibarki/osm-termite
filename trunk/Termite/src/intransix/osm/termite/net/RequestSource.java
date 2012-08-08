package intransix.osm.termite.net;

import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is the object provides the request info for an XmlRequest.
 * 
 * @author sutter
 */
public abstract class RequestSource extends DefaultHandler {
	
	//====================
	// Private Properties
	//====================
	private String url;
	private String method = "GET";
	private boolean hasPayload = false;
	
	//====================
	// Public Methods
	//====================
	
	/** This method should return the url. */
	public String getUrl() {
		return url;
	}
	
	/** This method returns the HTTP request method. */
	public String getMethod() {
		return method;
	}
	
	/** This method should return true if there is a payload. */
	public boolean getHasPayload() {
		return hasPayload;
	}
	
	/** This method should be implemented to write the XMl body, if there is a payload. */
	public void writeXml(XMLStreamWriter xmlWriter) throws Exception {}
	
	/** This method is called after the response is parsed. */
	public void parsingCompleted() {}
	
	//=============================
	// Protected Methods
	//=============================
	
	/** This method sets the URL. */
	protected void setUrl(String url) {
		this.url = url;
	}
	
	/** This sets the request method. The default is GET. */
	protected void setMethod(String method) {
		this.method = method;
	}
	
	/** This sets the hasPayload. the default is false. */
	protected void setHasPayload(boolean hasPayload) {
		this.hasPayload = hasPayload;
	}
	
	
		
	//==========================
	// Protected Methods
	//==========================
	
	/** This method reads a boolean value from the attributes. */ 
	protected static boolean getBoolean(Attributes attr, String key, boolean defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Boolean.parseBoolean(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
	
	/** This method reads a long value from the attributes. */
	protected static long getLong(Attributes attr, String key, long defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Long.decode(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
	
	/** This method reads a double value from the attributes. */
	protected static double getDouble(Attributes attr, String key, double defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Double.parseDouble(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
}
