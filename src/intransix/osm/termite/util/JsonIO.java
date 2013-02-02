package intransix.osm.termite.util;

import java.io.*;
import org.json.*;

/**
 *
 * @author sutter
 */
public class JsonIO {
	
	public static JSONObject readJsonFile(String fileName) throws Exception {

		//load file
		BufferedReader reader = null;
		JSONObject json = null;
		try {
			File file = new File(fileName);
			FileInputStream is = new FileInputStream(file);
			InputStreamReader in = new InputStreamReader(is, "UTF-8");
			reader = new BufferedReader(in);
			StringBuilder sb = new StringBuilder();
			while(true) {
				String data = reader.readLine();
				if(data == null) break;
				sb.append(data);
			}
			json = new JSONObject(sb.toString());
		}
		finally {
			if(reader != null) try {reader.close();} catch(Exception ex) {};
		}

		return json;
	}

	public static void writeJsonFile(String fileName, JSONObject json) throws Exception {
		//save file
		FileOutputStream os = null;
		try {
			File file = new File(fileName);
			os = new FileOutputStream(file);
			OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
			String data = json.toString();
			out.write(data);
			out.flush();
		}
		finally {
			if(os != null) try {os.close();} catch(Exception ex) {};
		}
	}
}
