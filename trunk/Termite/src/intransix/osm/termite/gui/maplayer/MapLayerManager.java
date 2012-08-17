package intransix.osm.termite.gui.maplayer;

import java.util.*;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.source.SourceLayer;
import intransix.osm.termite.gui.stdmode.GeocodeEditorMode;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.render.*;
import intransix.osm.termite.render.checkout.SearchLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.tile.TileLayer;

/**
 *
 * @author sutter
 */
public class MapLayerManager {
	
	//standard map layers
	private TileLayer baseMapLayer;
	private RenderLayer renderLayer;
	private EditLayer editLayer;
	private GeocodeLayer geocodeLayer;
	private SearchLayer searchLayer;
	private List<SourceLayer> sourceLayers = new ArrayList<SourceLayer>();
	
	private List<MapLayer> layers = new ArrayList<MapLayer>();
	
	private GeocodeEditorMode geocodeMode;
	private MapPanel mapPanel;
	
	public TileLayer getBaseMapLayer() {
		return baseMapLayer;
	}
	
	public EditLayer getEditLayer() {
		return editLayer;
	}
	
	public RenderLayer getRenderLayer() {
		return renderLayer;
	}
	
	public GeocodeLayer getGeocodeLayer() {
		return geocodeLayer;
	}
	
	public SearchLayer getSearchLayer() {
		return searchLayer;
	}
	
	public void setGeocodeMode(GeocodeEditorMode geocodeMode) {
		this.geocodeMode = geocodeMode;
	}
	
	public void init(TermiteGui gui, MapPanel mapPanel, Theme theme) {
		this.mapPanel = mapPanel;
		
		//base layer
		baseMapLayer = new TileLayer(mapPanel);
		layers.add(baseMapLayer);
		
		//geocode layer
		geocodeLayer = new GeocodeLayer(mapPanel);
		layers.add(geocodeLayer);
	
		//map render layer
		renderLayer = new RenderLayer(mapPanel);
		renderLayer.setTheme(theme);
		layers.add(renderLayer);

		gui.addMapDataListener(renderLayer);
		
		//edit layer
		editLayer = new EditLayer(gui,mapPanel);
		gui.addMapDataListener(editLayer);
		gui.addFeatureLayerListener(editLayer);
		gui.addLevelSelectedListener(editLayer);
		layers.add(editLayer);
		
		//checkout search layer
		searchLayer = new SearchLayer(mapPanel);
		layers.add(searchLayer);
		
		//initialize layers
		//basemap and source layer are always active, thorugh hidden if they have no map
		mapPanel.updateMapLayers(layers);
		baseMapLayer.setActiveState(true);
		
	}
	
	
	public void addSourceLayer(SourceLayer sourceLayer) {
		//add to source layers
		sourceLayers.add(sourceLayer);
		updateLayers();
	}
	
	public void removeSourceLayer(SourceLayer sourceLayer) {
		//add to source layers
		sourceLayers.remove(sourceLayer);
		updateLayers();
	}
	
	private void updateLayers() {
		//update map layers
		layers.clear();
		layers.add(baseMapLayer);
		for(SourceLayer layer:sourceLayers) {
			layers.add(layer);
		}
		layers.add(renderLayer);
		layers.add(editLayer);
		layers.add(geocodeLayer);
		layers.add(searchLayer);
		
		//update remote classes
		if(geocodeMode != null) {
			geocodeMode.updateSourceLayers(sourceLayers);
		}
		if(mapPanel != null) {
			mapPanel.updateSourceLayers(sourceLayers);
			mapPanel.updateMapLayers(layers);
		}
	}
	
	
}
