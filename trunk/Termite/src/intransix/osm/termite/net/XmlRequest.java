package intransix.osm.termite.net;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import org.apache.commons.codec.binary.Base64;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;

/**
 *
 * @author sutter
 */
public class XmlRequest {
	
	private final static String AUTHENTICATION_HEADER = "Authorization";
	
	private String authenticationHeader = null;
	private RequestSource requestSource;
	
	public XmlRequest(RequestSource requestSource) {
		this.requestSource = requestSource;
	}
	
	/** This sets the credential for HTTP basic authentication. If no authentication
	 * is desired, do not set these values.
	 * 
	 * @param user		The user name
	 * @param password  The password
	 */
	public void setCredentials(String user, String password) {
		authenticationHeader = getEncodedAuthenticationField(user,password);
	}
	
	/** This method makes a request with an optional text request body. It is 
	 * assumed there will be a response. The text format for the request is UTF-8.
	 * If HTTP basic authentication is desired, the credentials can be set in the
	 * method setCredentials. Otherwise authentication will not be added.
	 * 
	 * @param stringUrl		The URL
	 * @param requestBody	The body, if applicable. If there is no body, pass null.
	 * @param method		The HTTP method
	 * @return				The integer response code for the request.
	 * @throws Exception 
	 */
	public int doRequest() throws Exception {
		
		URL url = new URL(requestSource.getUrl());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod(requestSource.getMethod());

		//add authnetication field if needed
		if(authenticationHeader != null) {
			conn.addRequestProperty(AUTHENTICATION_HEADER,authenticationHeader);
		}

		//set output flags
		conn.setDoInput(true);

		//write output
		if(requestSource.getHasPayload()) {
			conn.setDoOutput(true);
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			OutputStream os = conn.getOutputStream();
			XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os);

			requestSource.writeXml(xmlWriter);
			xmlWriter.flush();
			xmlWriter.close();
			os.flush();
			os.close();
		}
		else {
			conn.setDoOutput(false);
		}

		int responseCode = conn.getResponseCode();
		if(responseCode == HttpURLConnection.HTTP_OK) {
			//parse response
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
			
if(requestSource instanceof intransix.osm.termite.map.data.OpenChangeSetRequest) {
int c;
StringBuilder sb = new StringBuilder();
while((c = bis.read()) != -1) {
	sb.append((char)c);
}
System.out.println(sb.toString());
}
else {
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();	
			saxParser.parse(bis,requestSource);
}			
			//parsing complete
			requestSource.parsingCompleted();
		}
		
		return responseCode;
	}
	
	//=====================
	// Private Methods
	//=====================
	
	private static String getEncodedAuthenticationField(String userName, String password) {
		String credInfo = userName + ":" + password;
		byte[] bytes = Base64.encodeBase64(credInfo.getBytes());
		return "Basic " + new String(bytes);
	}
}

