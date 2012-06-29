package intransix.osm.termite.gui;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmRelation;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import intransix.osm.termite.render.*;
import intransix.osm.termite.app.TermiteApp;
import intransix.osm.termite.render.tile.TileLayer;
import intransix.osm.termite.util.MercatorCoordinates;

import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.structure.RenderLayer;

import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.map.theme.Theme;

/**
 *
 * @author sutter
 */
public class TermiteGui extends javax.swing.JFrame {
	
	//=====================
	// Private Properties
	//=====================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	private TermiteApp app;

	//map data
	private OsmData osmData;
	
	//editor modes
	private EditorMode searchMode; //not an edit editor mode - has no button for mode toolbar
	private EditorMode defaultEditMode; //the default edit editor mode
	private java.util.List<EditorMode> editModes; //the list of possible edit editor modes
	private EditorMode activeMode = null; //the active editor mode
	
	//standard map layers
	private TileLayer baseMapLayer;
	private RenderLayer renderLayer;
	private EditLayer editLayer;
	
	//feature layer info
	private FeatureInfoMap featureMap;
	private FeatureInfo activeFeatureLayer;
	
	//selected level and feature
	private OsmObject selectedFeature;
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	//listeners
	private java.util.List<MapDataListener> mapDataListeners = new ArrayList<MapDataListener>();
	private java.util.List<FeatureSelectedListener> featureSelectedListeners = new ArrayList<FeatureSelectedListener>();
	private java.util.List<LevelSelectedListener> levelSelectedListeners = new ArrayList<LevelSelectedListener>();
	private java.util.List<FeatureLayerListener> featureTypeListeners = new ArrayList<FeatureLayerListener>();
	
	//UI components
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenuItem quitItem;
	
	private javax.swing.JPanel toolBarPanel;
	private javax.swing.JToolBar modeToolBar;
	
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    
	
    private javax.swing.JScrollPane jScrollPane1;
    private intransix.osm.termite.gui.contenttree.ContentTree contentTree;
	private javax.swing.JScrollPane jScrollPane2;
    private intransix.osm.termite.gui.featuretree.FeatureTree featureTree;
	private intransix.osm.termite.gui.property.PropertyTabPane propertyTabPane; 
    
    private intransix.osm.termite.render.MapPanel mapPanel;
	private javax.swing.JTabbedPane supplementalTabPane;
	
	private intransix.osm.termite.gui.maplayer.MapLayerManagerPane mapLayerTab;
	
	// </editor-fold>
	
	//=====================
	// Public Methods
	//=====================

	/**
	 * Creates new form TermiteGui
	 */
	public TermiteGui(TermiteApp app) {
		this.app = app;
		initComponents();
	}
	
	// <editor-fold defaultstate="collapsed" desc="Map Data Methods and Events">
	
	/** This method returns the map data object. */
	public OsmData getMapData() {
		return osmData;
	}
	
	/** This adds a map data listener. */
	public void addMapDataListener(MapDataListener listener) {
		mapDataListeners.add(listener);
	}
	
	/** This removes a map data listener. */
	public void removeMapDataListener(MapDataListener listener) {
		mapDataListeners.remove(listener);
	}
	
	/** This method will dispatch a map data event. It should be called
	 * when a map data is set to notify all interested objects. */
	public void setMapData(OsmData osmData) {
		this.osmData = osmData;
		
		for(MapDataListener listener:mapDataListeners) {
			listener.onMapData(osmData);
		}
		
		//control state based on presence of data
		if(osmData != null) {
			//put the app in the edit state
			setToEditState();
			//set the level to null (outdoor level)
			this.setSelectedLevel(null, null);
		}
		else {
			//put the app in the search state
			setToSearchState();
		}
	}
	
	/** This method returns the selected feature. */
	public OsmObject getSelectedFeature() {
		return null;
	}
	
	/** This adds a feature selected listener. */
	public void addFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.add(listener);
	}
	
	/** This removes a feature selected listener. */
	public void removeFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.remove(listener);
	}
	
	/** This method will dispatch a feature selected event. It should be called
	 * when a feature is selected to notify all interested objects. */
	public void setSelectedFeature(OsmObject feature) {
		this.selectedFeature = feature;
		
		for(FeatureSelectedListener listener:featureSelectedListeners) {
			listener.onFeatureSelected(feature);
		}
	}
	
	/** This method returns the active structure. */
	public OsmWay getActiveStructure() {
		return activeStructure;
	}
	
	/** This method returns the active level. */
	public OsmRelation getActiveLevel() {
		return activeLevel;
	}
	
	/** This adds a level selected listener. */
	public void addLevelSelectedListener(LevelSelectedListener listener) {
		levelSelectedListeners.add(listener);
	}
	
	/** This removes a level selected listener. */
	public void removeLevelSelectedListener(LevelSelectedListener listener) {
		levelSelectedListeners.remove(listener);
	}
	
	/** This method will dispatch a level selected event. It should be called
	 * when a level is selected to notify all interested objects. */
	public void setSelectedLevel(OsmWay structure, OsmRelation level) {
		this.activeStructure = structure;
		this.activeLevel = level;
		
		for(LevelSelectedListener listener:levelSelectedListeners) {
			listener.onLevelSelected(structure,level);
		}
	}
	
	/** This method returns the active feature type. */
	public FeatureInfo getActiveFeatureLayer() {
		return activeFeatureLayer;
	}
	
	/** This adds a feature type listener. */
	public void addFeatureLayerListener(FeatureLayerListener listener) {
		featureTypeListeners.add(listener);
	}
	
	public void removeFeatureLayerListener(FeatureLayerListener listener) {
		featureTypeListeners.remove(listener);
	}
	
	/** This method will dispatch a feature layer selected event. It should be called
	 * when a feature layer is selected to notify all interested objects. */
	public void setSelectedFeatureLayer(FeatureInfo featureInfo) {
		activeFeatureLayer = featureInfo;
		
		for(FeatureLayerListener listener:featureTypeListeners) {
			listener.onFeatureLayerSelected(featureInfo);
		}
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="UI Component Methods">
	
	public void addToolBar(JToolBar toolBar) {
		toolBarPanel.add(toolBar);
		pack();
	}
	
	public void removeToolBar(JToolBar toolBar) {
		toolBarPanel.remove(toolBar);
		pack();
	}
	
	public void addSupplementalTab(String title, Component component) {
		supplementalTabPane.addTab(title, component);
	}
	
	public void removeSupplementalTab(Component component) {
		int index = supplementalTabPane.indexOfTabComponent(component);
		if(index >= 0) {
			supplementalTabPane.remove(index);
		}
	}
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}

	public void addMapLayer(MapLayer mapLayer) {
		mapPanel.addLayer(mapLayer);
	}
	
	public void removeMapLayer(MapLayer mapLayer) {
		mapPanel.removeLayer(mapLayer);
	}
	
	public TileLayer getBaseMapLayer() {
		return baseMapLayer;
	}
	
	public RenderLayer getRenderLayer() {
		return renderLayer;
	}
	
	public EditLayer getEditLayer() {
		return editLayer;
	}
	
	// </editor-fold>

	public void initialize() {
		
		//MODES
		searchMode = app.getSearchMode();
		editModes = app.getEditModes();
		loadEditModes();
		
		//MAP
		initializeBaseMap();
		initialzeMapEditLayers();
		initializeView();
		
		//DATA PANEL
		setToSearchState();
	}
	
	//================================
	// Private Methods
	//================================
	
	// <editor-fold defaultstate="collapsed" desc="Edit Mode and State methods">
 	
	private void setToSearchState() {
		this.setEditModesEnable(false);
		this.setEditorMode(searchMode);
	}
	
	private void setToEditState() {
		this.setEditModesEnable(true);
		this.setEditorMode(defaultEditMode);
	}
	
	private void loadEditModes() {
		ButtonGroup buttonGroup = new ButtonGroup();
		for(EditorMode mode:editModes) {
			
			//initialize default edit mode
			if(defaultEditMode == null) {
				defaultEditMode = mode;
			}
			
			//create button
			String imageFile = mode.getIconImageName();
			String name = mode.getName();
			JToggleButton button;
			if(imageFile != null) {
				java.net.URL url = getClass().getResource(imageFile);
				ImageIcon icon = new ImageIcon(url);
				button = new JToggleButton(icon);
			}
			else {
				button = new JToggleButton(name);
			}			
			button.addActionListener(new ModeButtonListener(mode));
			button.setMargin(new Insets(1,1,1,1));
			button.setToolTipText(name);
			buttonGroup.add(button);
			modeToolBar.add(button);
			
		}
	}
	
	private void setEditorMode(EditorMode editorMode) {
		//get rid of old mode
		if(activeMode != null) {
			//turn off mode
			this.activeMode.turnOff();
			this.activeMode = null;
		}
		
		activeMode = editorMode;	
		//prepare the new mode
		editorMode.turnOn();
		
		//repaint map
		mapPanel.repaint();
	}
	
	private void setEditModesEnable(boolean enabled) {
		for(Component c:modeToolBar.getComponents()) {
			c.setEnabled(enabled);
		}
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Initialization Methods">
	
	private void initializeView() {
		Rectangle2D latLonBounds = app.getInitialLatLonBounds();
		double minLat = Math.toRadians(latLonBounds.getMinY());
		double minLon = Math.toRadians(latLonBounds.getMinX());
		double maxLat = Math.toRadians(latLonBounds.getMaxY());
		double maxLon = Math.toRadians(latLonBounds.getMaxX());
		
		double minMX = MercatorCoordinates.lonRadToMx(minLon); 
		double minMY = MercatorCoordinates.latRadToMy(maxLat); 
		double maxMX = MercatorCoordinates.lonRadToMx(maxLon); 
		double maxMY = MercatorCoordinates.latRadToMy(minLat); 
		Rectangle2D mercBounds = new Rectangle2D.Double(minMX,minMY,maxMX - minMX,maxMY - minMY);
		mapPanel.setViewBounds(mercBounds);
	}
	
	private void initializeBaseMap() {
		String mapQuestUrlTemplate = "http://otile1.mqcdn.com/tiles/1.0.0/osm/%3$d/%1$d/%2$d.jpg";
		int mapQuestMaxZoom = 18;
		int mapQuestMinZoom = 0;
		int mapQuestTileSize = 256;
		
		baseMapLayer = new TileLayer(mapQuestUrlTemplate,mapQuestMinZoom,mapQuestMaxZoom,mapQuestTileSize);
	
		mapPanel.addLayer(baseMapLayer);
		mapPanel.addMapListener(baseMapLayer);
	}
	
	private void initialzeMapEditLayers() {
		renderLayer = new RenderLayer();
		Theme theme = app.getTheme();
		renderLayer.setTheme(theme);
		renderLayer.setActiveState(false);
		this.addMapDataListener(renderLayer);
		mapPanel.addLocalCoordinateListener(renderLayer);
		
		editLayer = new EditLayer();
		editLayer.setActiveState(false);
		this.addMapDataListener(editLayer);
		
		mapPanel.addLayer(renderLayer);
		mapPanel.addLayer(editLayer);
	}
	

	@SuppressWarnings("unchecked")
    private void initComponents() {
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));		
		
		//create ment
		menuBar = new javax.swing.JMenuBar();
		menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		menuBar.setMinimumSize(new Dimension(0,0));
		menuBar.setMaximumSize(new Dimension(999999,100));
		
		fileMenu = new javax.swing.JMenu();
		fileMenu.setText("File");
        quitItem = new javax.swing.JMenuItem();
        quitItem.setText("Quit");
		quitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);
        menuBar.add(fileMenu);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		//create toolbar
		toolBarPanel = new javax.swing.JPanel();
		toolBarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBarPanel.setLayout(new BoxLayout(toolBarPanel,BoxLayout.X_AXIS));
		
		modeToolBar = new javax.swing.JToolBar();
		modeToolBar.setFloatable(false);
        modeToolBar.setRollover(true);
		modeToolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		toolBarPanel.add(modeToolBar);

		//create meta data panels
        jSplitPane1 = new javax.swing.JSplitPane();
		jSplitPane1.setAlignmentX(Component.LEFT_ALIGNMENT);
		
        jSplitPane2 = new javax.swing.JSplitPane();
		jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane4 = new javax.swing.JSplitPane();
    
		//content tree
		jScrollPane1 = new javax.swing.JScrollPane();
        contentTree = new intransix.osm.termite.gui.contenttree.ContentTree();
		this.addMapDataListener(contentTree);
		this.addLevelSelectedListener(contentTree);
		jScrollPane1.setViewportView(contentTree);
		
		//feature tree
        jScrollPane2 = new javax.swing.JScrollPane();
        featureTree = new intransix.osm.termite.gui.featuretree.FeatureTree();
		this.addFeatureLayerListener(featureTree);
		featureTree.setFeatureInfoMap(app.getFeatureInfoMap());
		jScrollPane2.setViewportView(featureTree);
		
		//property tabbed pane
		propertyTabPane = new intransix.osm.termite.gui.property.PropertyTabPane();
		this.addFeatureSelectedListener(propertyTabPane);
		this.addLevelSelectedListener(propertyTabPane);
        
		//map panel
        mapPanel = new intransix.osm.termite.render.MapPanel();
		mapPanel.setMinimumSize(new java.awt.Dimension(200, 200));
        mapPanel.setPreferredSize(new java.awt.Dimension(600, 600));
		
		//supplemental tabbed pane
        supplementalTabPane = new javax.swing.JTabbedPane();

		//layout the content panes
		jSplitPane1.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane1.setDividerLocation(150);
		
		jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane2.setDividerLocation(200);
        
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane3.setDividerLocation(500);
		
		jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane4.setDividerLocation(200);
		
		jSplitPane1.setLeftComponent(jSplitPane2);
		jSplitPane1.setRightComponent(jSplitPane3);
		
		jSplitPane2.setTopComponent(jScrollPane1);
		jSplitPane2.setBottomComponent(jSplitPane4);
		
		jSplitPane4.setTopComponent(jScrollPane2);
		jSplitPane4.setBottomComponent(propertyTabPane);
		
		jSplitPane3.setTopComponent(mapPanel);
        jSplitPane3.setBottomComponent(supplementalTabPane);
		
		//create standard supplemental tabs
		
		//map layer manager
		mapLayerTab = new intransix.osm.termite.gui.maplayer.MapLayerManagerPane();
		mapPanel.setMapLayerManager(mapLayerTab);
		mapLayerTab.setMapPanel(mapPanel);
		this.addSupplementalTab("Map Layers", mapLayerTab);
		
		this.add(menuBar);
		this.add(toolBarPanel);
		this.add(jSplitPane1);

        pack();
    }
	
	// </editor-fold>

	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
		app.exit();
	}
	
	/** This is a listener for the mode buttons. */
	private class ModeButtonListener implements ActionListener {
		
		private EditorMode mode;
		
		public ModeButtonListener(EditorMode mode) {
			this.mode = mode;
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			setEditorMode(mode);
		}
	}

}
