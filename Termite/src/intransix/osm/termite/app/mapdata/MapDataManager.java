package intransix.osm.termite.app.mapdata;

import intransix.osm.termite.map.data.*;
import java.util.ArrayList;
import java.util.List;
import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.util.JsonIO;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class MapDataManager {
	
	//download
	//commit
	//publish?

	private OsmData osmData;
	private List<MapDataListener> mapDataListeners = new ArrayList<MapDataListener>();
	
	private RenderLayer renderLayer;
	private DownloadLayer downloadLayer;
	private DownloadEditorMode downloadEditorMode;
	
	public void init(String confogFileName) throws Exception {
		
		JSONObject modelJson = JsonIO.readJsonFile(confogFileName);
		OsmModel.parse(modelJson);
		
		renderLayer = new RenderLayer();
		this.addMapDataListener(renderLayer);
		
		downloadLayer = new DownloadLayer();
		downloadEditorMode = new DownloadEditorMode(this,downloadLayer);
	}
	
	public DownloadLayer getDownloadLayer() {
		return downloadLayer;
	}
	
	public RenderLayer getRenderLayer() {
		return renderLayer;
	}
	
	public DownloadEditorMode getDownloadEditorMode() {
		return downloadEditorMode;
	}
	
	/** This method gets the map data. */
	public OsmData getOsmData() {
		return osmData;
	}
	
	public void setOsmData(OsmData osmData) {
		this.osmData = osmData;
		for(MapDataListener listener:mapDataListeners) {
			listener.onMapData(osmData);
		}
	}
	
	/** This adds a map data listener. */
	public void addMapDataListener(MapDataListener listener) {
		mapDataListeners.add(listener);
	}

	/** This removes a map data listener. */
	public void removeMapDataListener(MapDataListener listener) {
		mapDataListeners.remove(listener);
	}	
}
