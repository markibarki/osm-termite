package intransix.osm.termite.render.tile;

import intransix.osm.termite.util.MercatorCoordinates;
import org.json.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class TileInfo {
	
	public final static String XYZ_KEY = "XYZ";
	public final static String LATLON_MINMAX_KEY = "LatLonMinMax"; 
	
	public enum RequestType {
		PARAMS_XYZ, 
		PARAMS_LATLON_MINMAX
	}
	
	private String name;
	private String urlTemplate;
	private RequestType requestType;
	private int maxZoom;
	private int minZoom;
	private int tileSize;
	
	public String getName() {
		return name;
	}
	
	public int getMaxZoom() {
		return maxZoom;
	}
	
	public int getMinZoom() {
		return minZoom;
	}
	
	public int getTileSize() {
		return tileSize;
	}
	
	public static List<TileInfo> parseInfoList(JSONObject json) {
		List<TileInfo> tileInfoList = new ArrayList<TileInfo>();
		
		try {
			JSONArray infoArray = json.getJSONArray("mapInfo");
			int cnt = infoArray.length();
			JSONObject jsonInfo;
			TileInfo tileInfo;
			for(int i = 0; i < cnt; i++) {
				jsonInfo = infoArray.getJSONObject(i);
				tileInfo = parseInfo(jsonInfo);
				if(tileInfo != null) {
					tileInfoList.add(tileInfo);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return tileInfoList;
	}
	
	public static TileInfo parseInfo(JSONObject json) {
		try {
			TileInfo tileInfo = new TileInfo();
			tileInfo.name = json.getString("name");
			tileInfo.maxZoom = json.getInt("maxZoom");
			tileInfo.minZoom = json.getInt("minZoom");
			tileInfo.tileSize = json.getInt("tileSize");
			tileInfo.urlTemplate = json.getString("urlTemplate");
			
			String type = json.getString("requestType");
			if(XYZ_KEY.equalsIgnoreCase(type)) {
				tileInfo.requestType = RequestType.PARAMS_XYZ;
			}
			else if(LATLON_MINMAX_KEY.equalsIgnoreCase(type)) {
				tileInfo.requestType = RequestType.PARAMS_LATLON_MINMAX;
			}
			else {
				//type not found
				return null;
			}
			return tileInfo;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static TileInfo getDefaultMap(JSONObject json,List<TileInfo> tileInfoList) {
		try {
			int defaultIndex = json.getInt("defaultMap");
			if((defaultIndex >= 0)&&(defaultIndex < tileInfoList.size())) {
				return tileInfoList.get(defaultIndex);
			}
			else {
				return null;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
		
	
	public String getUrl(int ix, int iy, int zoom) {
		switch(requestType) {
			case PARAMS_XYZ:
				return getXYZUrl(ix,iy,zoom);
				
			case PARAMS_LATLON_MINMAX:
				return getLatLonMinMaxUrl(ix,iy,zoom);
				
			default:
				return null;
		}
	}
	
	private String getXYZUrl(int ix, int iy, int zoom) {
		return String.format(urlTemplate,ix,iy,zoom);
	}
	
	private String getLatLonMinMaxUrl(int ix, int iy, int zoom) {
		double zoomScale = (1 << zoom);
		double minMercX = ((double)ix) / zoomScale;
		double minMercY = ((double)iy) / zoomScale;
		double maxMercX = ((double)ix + 1.0) / zoomScale;
		double maxMercY = ((double)iy + 1.0) / zoomScale;
		
		double minLonDeg = Math.toDegrees(MercatorCoordinates.mxToLonRad(minMercX));
		double maxLatDeg = Math.toDegrees(MercatorCoordinates.myToLatRad(minMercY));
		double maxLonDeg = Math.toDegrees(MercatorCoordinates.mxToLonRad(maxMercX));
		double minLatDeg = Math.toDegrees(MercatorCoordinates.myToLatRad(maxMercY));
		
		return String.format(urlTemplate,minLatDeg,minLonDeg,maxLatDeg,maxLonDeg);
	}
	
	
	
}
