package intransix.osm.termite.net;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author sutter
 */
public class NetRequest {
	
	private final static String AUTHENTICATION_HEADER = "Authorization";
	
	private String authenticationHeader = null;
	private RequestSource requestSource;
	
	public NetRequest(RequestSource requestSource) {
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
	
	/** This method executes request based on the request source object.
	 * If HTTP basic authentication is desired, the credentials can be set in the
	 * method setCredentials. Otherwise authentication will not be added.
	 *
	 * @return				The integer response code for the request.
	 * @throws Exception 
	 */
	public int doRequest() throws Exception {
		
		URL url = new URL(requestSource.getUrl());
		HttpURLConnection conn = null;
		
		try {
			conn = (HttpURLConnection)url.openConnection();
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
				OutputStream os = conn.getOutputStream();
				try {
					requestSource.writeRequestBody(conn.getOutputStream());
					os.flush();
				}
				finally {
					os.close();
				}
			}
			else {
				conn.setDoOutput(false);
			}

			int responseCode = conn.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK) {
				//parse response
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				try {
					requestSource.readResponseBody(bis);
					bis.close();
				}
				finally {
					is.close();
				}
			}
		
			return responseCode;
		}
		finally {
			//close connection
			if(conn != null) {
				conn.disconnect();
			}
		}
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

