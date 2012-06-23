package intransix.osm.termite.gui;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.app.TermiteApp;
import intransix.osm.termite.render.tile.TileLayer;
import intransix.osm.termite.util.LocalCoordinates;
import intransix.osm.termite.util.MercatorCoordinates;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.structure.StructureLayer;

import intransix.osm.termite.map.theme.Theme;

/**
 *
 * @author sutter
 */
public class TermiteGui extends javax.swing.JFrame {
	
	private final static String SUBMODE_TOOLBAR_NAME = "submode";
	
	//=====================
	// Private Properties
	//=====================
	
	private TermiteApp app;
	
	private EditorMode searchMode; //not an edit mode - has no button
	private EditorMode defaultEditMode;
	private java.util.List<EditorMode> editModes;
	private boolean dataPresent = false;
	private EditorMode activeMode = null;
	
	private TileLayer baseMapLayer;
	private StructureLayer renderLayer;
	private EditLayer editLayer;
	
	private TermiteData termiteData;
	
//	private FeatureInfoMap featureMap;
//	private List<DataTab> dataTabs;
	
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
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public TileLayer getBaseMapLayer() {
		return baseMapLayer;
	}
	
	public StructureLayer getRenderLayer() {
		return renderLayer;
	}
	
	public EditLayer getEditLayer() {
		return editLayer;
	}

	public void initialize() {
		//no data present at startup
		dataPresent = false;
		
		//MODES
		searchMode = app.getSearchMode();
		editModes = app.getEditModes();
		loadEditModes();
		
		
		//MAP
		initializeBaseMap();
		initialzeMapEditLayers();
		initializeView();
		
		//DATA PANEL
		//create the map layer panel
		//create the relation panel
		//create ano other panels
		
		//set state to "search"
		
		setToSearchState();
		
		//SEARCH MODE////////////////////////
		
		//disable all buttons on the toolbar
		//sets the active editor mode to search, loading the search submode toolbar
		
		//disables the meta data panels (content, feature layer, property)
		
		//add the base layer and search layer to the map (clear others)
		
		//add the map layer panel to the data panel
		
		//EDIT MODE//////////////////////////////
		
		//enable the buttons on the edit mode tool bar
		//set the mode to select
		
		//enable the meta data panels
		//enable source upload (where ever this is done)
		
		//add the base layer, render layer, edit layer to the map (clear others)
		
		//add the map layer and relation panel to the data panel (clear others)
		
		
	}
	
	public void setEditData(TermiteData termiteData) {
		this.termiteData = termiteData;
		
//we need to set the active level properly
		TermiteLevel level = termiteData.getOutdoorLevel();
		
		renderLayer.setLevel(level);
		editLayer.setLevel(level);
		
		setToEditState();
	}
	
	public void clearEditData() {
		this.termiteData = null;
		
		//clean up the places we left data
		renderLayer.setLevel(null);
		editLayer.setLevel(null);
		
		setToSearchState();
	}
	
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
		JToolBar toolBar;
		if(activeMode != null) {
			//turn off mode
			this.activeMode.turnOff();
			//remove submode toolbar
			toolBar = activeMode.getSubmodeToolbar();
			if(toolBar != null) {
				toolBarPanel.remove(toolBar);
			}
			this.activeMode = null;
		}
		
		activeMode = editorMode;	
		//prepare the new mode
		editorMode.turnOn();
		toolBar = editorMode.getSubmodeToolbar();
		if(toolBar != null) {
			toolBarPanel.add(toolBar);
		}
		
		//update the layout because of the toolbars
		pack();
		
		//repaint map
		mapPanel.repaint();
	}
	
	private void setEditModesEnable(boolean enabled) {
		for(Component c:modeToolBar.getComponents()) {
			c.setEnabled(enabled);
		}
	}
	
	private void initializeView() {
		Rectangle2D latLonBounds = app.getInitialLatLonBounds();
		double minLat = Math.toRadians(latLonBounds.getMinY());
		double minLon = Math.toRadians(latLonBounds.getMinX());
		double maxLat = Math.toRadians(latLonBounds.getMaxY());
		double maxLon = Math.toRadians(latLonBounds.getMaxX());
		
		double minLocX = LocalCoordinates.mercToLocalX(MercatorCoordinates.lonRadToMx(minLon)); 
		double minLocY = LocalCoordinates.mercToLocalY(MercatorCoordinates.latRadToMy(maxLat)); 
		double maxLocX = LocalCoordinates.mercToLocalX(MercatorCoordinates.lonRadToMx(maxLon)); 
		double maxLocY = LocalCoordinates.mercToLocalY(MercatorCoordinates.latRadToMy(minLat)); 
		Rectangle2D localBounds = new Rectangle2D.Double(minLocX,minLocY,maxLocX - minLocX,maxLocY - minLocY);
		mapPanel.setViewBounds(localBounds);
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
		renderLayer = new StructureLayer();
		Theme theme = app.getTheme();
		renderLayer.setTheme(theme);
		
		editLayer = new EditLayer();
	}
	

	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

		//create content panels
        jSplitPane1 = new javax.swing.JSplitPane();
		jSplitPane1.setAlignmentX(Component.LEFT_ALIGNMENT);
		
        jSplitPane2 = new javax.swing.JSplitPane();
		jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane4 = new javax.swing.JSplitPane();
    
		jScrollPane1 = new javax.swing.JScrollPane();
        contentTree = new intransix.osm.termite.gui.contenttree.ContentTree();
		jScrollPane1.setViewportView(contentTree);
		
        jScrollPane2 = new javax.swing.JScrollPane();
        featureTree = new intransix.osm.termite.gui.featuretree.FeatureTree();
		jScrollPane2.setViewportView(featureTree);
		
		propertyTabPane = new javax.swing.JTabbedPane();
        
        mapPanel = new intransix.osm.termite.render.MapPanel();
		mapPanel.setMinimumSize(new java.awt.Dimension(200, 200));
        mapPanel.setPreferredSize(new java.awt.Dimension(600, 600));
		
        DataTabPane = new javax.swing.JTabbedPane();

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
        jSplitPane3.setBottomComponent(DataTabPane);
		
//		 setJMenuBar(menuBar);
		this.add(menuBar);
		this.add(toolBarPanel);
		this.add(jSplitPane1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
		app.exit();
	}//GEN-LAST:event_quitItemActionPerformed

	

	
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
	private javax.swing.JTabbedPane propertyTabPane; 
    
    private intransix.osm.termite.render.MapPanel mapPanel;
	private javax.swing.JTabbedPane DataTabPane;
	
	/** This is a listener for the mode buttons. */
	private class ModeButtonListener implements ActionListener {
		
		private EditorMode mode;
		
		public ModeButtonListener(EditorMode mode) {
			this.mode = mode;
		}
		
		public void actionPerformed(ActionEvent ae) {
			setEditorMode(mode);
		}
	}

}
