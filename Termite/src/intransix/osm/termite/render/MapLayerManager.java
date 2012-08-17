package intransix.osm.termite.render;

import intransix.osm.termite.render.LayerManagerPanel;
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
	
	//======================
	// Properties
	//======================
	
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
	
	private LayerManagerPanel layerManagerPanel;
	
	//======================
	// Public Methods
	//======================
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public LayerManagerPanel getLayerManagerPanel() {
		if(layerManagerPanel == null) {
			layerManagerPanel = new LayerManagerPanel();
			layerManagerPanel.layerStateChanged(layers);
		}
		return layerManagerPanel;
	}
	
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
		baseMapLayer = new TileLayer(this);
		layers.add(baseMapLayer);
		
		//geocode layer
		geocodeLayer = new GeocodeLayer(this);
		layers.add(geocodeLayer);
	
		//map render layer
		renderLayer = new RenderLayer(this);
		renderLayer.setTheme(theme);
		layers.add(renderLayer);

		gui.addMapDataListener(renderLayer);
		
		//edit layer
		editLayer = new EditLayer(this,gui);
		gui.addMapDataListener(editLayer);
		gui.addFeatureLayerListener(editLayer);
		gui.addLevelSelectedListener(editLayer);
		layers.add(editLayer);
		
		//checkout search layer
		searchLayer = new SearchLayer(this);
		layers.add(searchLayer);
		
		//initialize layers
		//basemap and source layer are always active, thorugh hidden if they have no map
		mapPanel.updateMapLayers(layers);
		baseMapLayer.setActiveState(true);
		
	}
	
	public List<SourceLayer> getSourceLayers() {
		return sourceLayers;
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
	
	//======================
	// Package Methods
	//======================
	
	/** MapLayers call this when their state changes. */
	void layerStateChanged(MapLayer layer) {
		updateLayers();
	}
	
	//======================
	// Private Methods
	//======================
	
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
		if(layerManagerPanel != null) {
			layerManagerPanel.layerStateChanged(layers);
		}
	}
	
	
}
