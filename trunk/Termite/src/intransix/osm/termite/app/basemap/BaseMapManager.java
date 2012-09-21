package intransix.osm.termite.app.basemap;

import intransix.osm.termite.render.tile.*;
import intransix.osm.termite.util.JsonIO;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class BaseMapManager {
	
	private TileInfo activeBaseMapInfo;
	private List<TileInfo> baseMapList = new ArrayList<TileInfo>();
	private List<BaseMapListener> baseMapListeners = new ArrayList<BaseMapListener>();
	
	private TileLayer baseMapLayer;
	
	/** This method initializes the base map manager.
	 * 
	 * @param configFileName	The file name for the base map configuration file.
	 * @throws Exception 
	 */
	public void init(String configFileName) throws Exception {
		JSONObject mapInfoJson = JsonIO.readJsonFile(configFileName);
		baseMapList = TileInfo.parseInfoList(mapInfoJson);
		
		baseMapLayer = new TileLayer();
		baseMapLayer.setActiveState(true);
		
		//set the default map
		TileInfo defaultMap = TileInfo.getDefaultMap(mapInfoJson,baseMapList);
		if(defaultMap != null) {
			setBaseMap(defaultMap);
		}
	}
	
	/** This gets the map layer for the base map. */
	public TileLayer getBaseMapLayer() {
		return baseMapLayer;
	}
	
	/** This method sets the base map. */
	public void setBaseMap(TileInfo tileInfo) {
		activeBaseMapInfo = tileInfo;
		baseMapLayer.setTileInfo(tileInfo);
		baseMapLayer.setVisible( (tileInfo != null) ? true : false);
		for(BaseMapListener baseMapListener:baseMapListeners) {
			baseMapListener.baseMapChanged(tileInfo);
		}
	}
	
	/** This method gets the active base map type. */
	public TileInfo getBaseMapInfo() {
		return activeBaseMapInfo;
	}
	
	/** This method gets the list of base map types. */
	public List<TileInfo> getBaseMapList() {
		return baseMapList;
	}
	
	/** This method adds a base map listener. */
	public void addBaseMapListener(BaseMapListener baseMapListener) {
		if(!baseMapListeners.contains(baseMapListener)) {
			baseMapListeners.add(baseMapListener);
		}
	}
	
	/** This method removes a base map listener. */
	public void removeBaseMapListener(BaseMapListener baseMapListener) {
		baseMapListeners.remove(baseMapListener);
	}
}
