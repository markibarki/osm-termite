package intransix.osm.termite.gui;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import intransix.osm.termite.app.TermiteApp;
import intransix.osm.termite.gui.filter.*;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.render.*;
import intransix.osm.termite.render.tile.TileLayer;
import intransix.osm.termite.render.tile.TileInfo;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.util.MercatorCoordinates;
import intransix.osm.termite.render.MapLayerManager;
import intransix.osm.termite.gui.dialog.SourceLayerDialog;
import intransix.osm.termite.gui.stdmode.*;
import intransix.osm.termite.gui.task.CommitTask;
import intransix.osm.termite.publish.PublishTask;
import java.io.File;

/**
 * This is the main UI class. It initializes the UI and it manages event flow.
 * 
 * @author sutter
 */
public class TermiteGui extends javax.swing.JFrame implements 
		OsmDataChangedListener, KeyListener {
	
	//=====================
	// Private Properties
	//=====================
	
	private final static String HIDDEN_BASE_MAP_NAME = "Hidden";
	
	private final static String UNDO_ITEM_BASE = "Undo: ";
	private final static String UNDO_ITEM_TEXT = "Undo";
	private final static String REDO_ITEM_BASE = "Redo: ";
	private final static String REDO_ITEM_TEXT = "Redo";
	
	private final static int[] EDIT_MODE_SHORTCUTS = {KeyEvent.VK_A,
		KeyEvent.VK_S,KeyEvent.VK_D,KeyEvent.VK_F,KeyEvent.VK_G,KeyEvent.VK_H
	};
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	private TermiteApp app;

	//map data
	private OsmData osmData;
	
	//editor modes
	private EditorMode searchMode; //not an edit editor mode - has no button for mode toolbar
	private EditorMode defaultEditMode; //the default edit editor mode
	private java.util.List<EditorMode> editModes; //the list of possible edit editor modes
	private EditorMode activeMode = null; //the active editor mode
	
	//edit mode ui elements we need to change programmatically
	private boolean editModesEnabled = false;
	private ButtonGroup editModeButtonGroup;
	private JToggleButton defaultEditModeButton;
	
	//feature layer info
	private FeatureInfoMap featureMap;
	private FeatureInfo activeFeatureLayer;
	
	//source manager
	private MapLayerManager mapLayerManager = new MapLayerManager(); 
	private EditLayer editLayer;
	private TileLayer baseMapLayer;
	
	//base map options
	private java.util.List<TileInfo> tileInfoList;
	
	//selected level and feature
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	//this is used for keeping track of the workign directory
	private File workingDirectory;
	
	//listeners
	private java.util.List<MapDataListener> mapDataListeners = new ArrayList<MapDataListener>();
	private java.util.List<LevelSelectedListener> levelSelectedListeners = new ArrayList<LevelSelectedListener>();
	private java.util.List<FeatureLayerListener> featureTypeListeners = new ArrayList<FeatureLayerListener>();
	
	//UI components
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenu fileMenu;
	private javax.swing.JMenuItem commitItem;
	private javax.swing.JMenuItem clearDataItem;
	private javax.swing.JMenuItem quitItem;
	private javax.swing.JMenu editMenu;
	private javax.swing.JMenuItem undoItem;
	private javax.swing.JMenuItem redoItem;
	private javax.swing.JMenu mapMenu;
	private javax.swing.JMenuItem openSourceMenuItem;
	private javax.swing.JMenu helpMenu;
	private javax.swing.JMenuItem aboutItem;
	private javax.swing.JMenu devMenu;
	private javax.swing.JMenuItem publishItem;
	
	private javax.swing.JMenu baseMapMenu;
    private javax.swing.ButtonGroup baceMapButtonGroup;
	
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
	
	private intransix.osm.termite.render.LayerManagerPanel layerManagerPanel;
	// </editor-fold>
	
	//=====================
	// Public Methods
	//=====================

	// <editor-fold defaultstate="collapsed" desc="Constructor">
	/**
	 * Creates new form TermiteGui
	 */
	public TermiteGui(TermiteApp app) {
		this.app = app;
		initComponents();
	}
	// </editor-fold>
	
	public TermiteApp getTermiteApp() {
		return app;
	}
	
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
	// <editor-fold defaultstate="collapsed" desc="Map Data Methods and Events">
	//These methods are used for event flow.
	
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
			
			//become a listener for data changed events
			osmData.addDataChangedListener(this);
		}
		else {
			//put the app in the search state
			setToSearchState();
			
			//update the menu
			clearUndoItem();
			clearRedoItem();
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
		
		//update filter
		if(osmData != null) {
			FilterRule filterRule = null;
			if(level != null) {
				filterRule = new LevelFilterRule(level);

			}
			else if(structure != null) {
				filterRule = new StructureFilterRule(structure);
			}
			else {
				filterRule = new OutdoorFilterRule();
			}

			FeatureFilter filter = new FeatureFilter(filterRule);
			osmData.setFilter(filter);
			mapPanel.repaint();
		}
		
		//notify listeners
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
		this.validate();
	}
	
	public void removeToolBar(JToolBar toolBar) {
		toolBarPanel.remove(toolBar);
		this.validate();
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
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Initialize">
	public void initialize() {
		
		//map layers
		Theme theme = app.getTheme();
		mapLayerManager.init(this, mapPanel, theme);
		//add the layer manager tab
		addSupplementalTab("Map Layers", mapLayerManager.getLayerManagerPanel());

		//get layers we need copies of
		editLayer = mapLayerManager.getEditLayer();
		baseMapLayer = mapLayerManager.getBaseMapLayer();
		
		//add the feature selected listeners to the edit layer
		editLayer.addFeatureSelectedListener(propertyTabPane);
		
		//editor modes
				
		//create the editor modes
		searchMode = new SearchEditorMode(this);
		
		SelectEditorMode selectMode = new SelectEditorMode(this);
		NodeEditorMode nodeMode = new NodeEditorMode(this);
		WayEditorMode wayMode = new WayEditorMode(this);
		GeocodeEditorMode geocodeMode = new GeocodeEditorMode(this);
		
		editModes = new ArrayList<EditorMode>();
		editModes.add(selectMode);
		editModes.add(nodeMode);
		editModes.add(wayMode);
		editModes.add(geocodeMode);
		searchMode.setLayers(mapLayerManager);
		
		for(EditorMode editMode:editModes) {
			editMode.setLayers(mapLayerManager);
		}
		
		loadEditModes();
		mapLayerManager.setGeocodeMode(geocodeMode);
		
		//map view
		initializeView();
		
		//set initial mode
		setToSearchState();
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Event Handlers">
	/** This method is called when the data has changed. It updates the undo
	 * and redo actions.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		if(osmData != null) {
			String undoMessage = osmData.getUndoMessage();
			if(undoMessage != null) {
				undoItem.setText(UNDO_ITEM_BASE + undoMessage);
				undoItem.setEnabled(true);
			}
			else {
				clearUndoItem();
			}
			
			String redoMessage = osmData.getRedoMessage();
			if(redoMessage != null) {
				redoItem.setText(REDO_ITEM_BASE + redoMessage);
				redoItem.setEnabled(true);
			}
			else {
				clearRedoItem();
			}
		}
		
		if(contentTree != null) {
			contentTree.mapDataUpdated();
		}
	}
	
	private void clearUndoItem() {
		undoItem.setText(UNDO_ITEM_TEXT);
		undoItem.setEnabled(false);
	}
	
	private void clearRedoItem() {
		redoItem.setText(REDO_ITEM_TEXT);
		redoItem.setEnabled(false);
	}
	// </editor-fold>
	
		
	// <editor-fold defaultstate="collapsed" desc="Key Listener">
	
	/** Handle the key typed event from the text field. */
    @Override
	public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
	@Override
    public void keyPressed(KeyEvent e) {
		if(editModesEnabled) {
			//select editor mode
			for(EditorMode editMode:editModes) {
				if(editMode.getUIShortcut() == e.getKeyCode()) {
					JToggleButton button = editMode.getUIButton();
					if(!button.isSelected()) {
						button.setSelected(true);
						this.setEditorMode(editMode);
					}
				}
			}
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
    }
	
	// </editor-fold>
	
	//================================
	// Private Methods
	//================================
	
	// <editor-fold defaultstate="collapsed" desc="Edit Mode and State methods">
 	
	private void setToSearchState() {
		this.setEditModesEnable(false);
		this.setEditorMode(searchMode);
		this.editModeButtonGroup.clearSelection();
		
		//disable some menu items
		this.commitItem.setEnabled(false);
	}
	
	private void setToEditState() {
		this.setEditModesEnable(true);
		this.setEditorMode(defaultEditMode);
		defaultEditModeButton.setSelected(true);
		
		//enable some menu items
		this.commitItem.setEnabled(true);
	}
	
	private void loadEditModes() {
		editModeButtonGroup = new ButtonGroup();
		int i = 0;
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
			editModeButtonGroup.add(button);
			modeToolBar.add(button);
			
			mode.setUIButton(button);
			if(i < EDIT_MODE_SHORTCUTS.length) {
				mode.setUIShortcut(EDIT_MODE_SHORTCUTS[i]);
			}
			i++;
			
			if(mode == defaultEditMode) {
				defaultEditModeButton = button;
			}
			
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
		editModesEnabled = enabled;
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

	@SuppressWarnings("unchecked")
    private void initComponents() {
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));	
		
		//create ment
		menuBar = new javax.swing.JMenuBar();
		menuBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		fileMenu = new javax.swing.JMenu();
		fileMenu.setText("File");
		
		commitItem = new javax.swing.JMenuItem();
        commitItem.setText("Commit");
		commitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitData();
            }
        });
        fileMenu.add(commitItem);
		
		clearDataItem = new javax.swing.JMenuItem();
        clearDataItem.setText("Clear Data");
		clearDataItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearData();
            }
        });
        fileMenu.add(clearDataItem);
		
        quitItem = new javax.swing.JMenuItem();
        quitItem.setText("Quit");
		quitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);
        menuBar.add(fileMenu);
		
		editMenu = new javax.swing.JMenu();
		editMenu.setText("Edit");
        undoItem = new javax.swing.JMenuItem();
        undoItem.setText(UNDO_ITEM_TEXT);
		undoItem.setEnabled(false);
		undoItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoItemActionPerformed(evt);
            }
        });
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        editMenu.add(undoItem);
		redoItem = new javax.swing.JMenuItem();
        redoItem.setText(REDO_ITEM_TEXT);
		redoItem.setEnabled(false);
		redoItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoItemActionPerformed(evt);
            }
        });
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        editMenu.add(redoItem);
        menuBar.add(editMenu);
		
		mapMenu = new javax.swing.JMenu();
		mapMenu.setText("Map");
		
		//base map
		baseMapMenu = new javax.swing.JMenu();
		baseMapMenu.setText("Base Map");
		baceMapButtonGroup = new javax.swing.ButtonGroup();
		
		tileInfoList = this.app.getBaseMapInfo();
		
		//add base map choices
		addBaseMapMenuItem(null);
		if(tileInfoList != null) {
			for(TileInfo tileInfo:tileInfoList) {
				addBaseMapMenuItem(tileInfo);
			}
		}

        mapMenu.add(baseMapMenu);
		
		//source map
		openSourceMenuItem = new javax.swing.JMenuItem();
		openSourceMenuItem.setText("Source Images...");
		openSourceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageSourceLayers();
            }
        });
		
        mapMenu.add(openSourceMenuItem);
		
		menuBar.add(mapMenu);
		
		//dev
		devMenu = new javax.swing.JMenu();
		devMenu.setText("Dev");
		publishItem = new javax.swing.JMenuItem();
		publishItem.setText("Publish...");
		publishItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				publishMap();
            }
        });
		devMenu.add(publishItem);
		menuBar.add(devMenu);
		
		//about button
		helpMenu = new javax.swing.JMenu();
		helpMenu.setText("Help");
		aboutItem = new javax.swing.JMenuItem();
		aboutItem.setText("About...");
		aboutItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				String versionInfo = "Version: " + app.getVersion();
                JOptionPane.showMessageDialog(null,versionInfo);
            }
        });
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
		
		//hold the height fixed, let the width vary...
		Dimension d = menuBar.getPreferredSize();
		Dimension dMin = new Dimension(d);
		menuBar.setMinimumSize(dMin);
		Dimension dMax = new Dimension(99999999,d.height);
		menuBar.setMaximumSize(dMax);
		
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
		
		Box.Filler space = new javax.swing.Box.Filler(new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y));
		toolBarPanel.add(space);
		
		JSeparator separator = new javax.swing.JSeparator();
		separator.setOrientation(javax.swing.SwingConstants.VERTICAL);
		int px = separator.getPreferredSize().width;
		int mxy = separator.getMaximumSize().height;
		separator.setMaximumSize(new Dimension(px,mxy));
		toolBarPanel.add(separator);
		
		space = new javax.swing.Box.Filler(new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y));
		toolBarPanel.add(space);

		//create meta data panels
        jSplitPane1 = new javax.swing.JSplitPane();
		jSplitPane1.setAlignmentX(Component.LEFT_ALIGNMENT);
		
        jSplitPane2 = new javax.swing.JSplitPane();
		jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane4 = new javax.swing.JSplitPane();
    
		//content tree
		jScrollPane1 = new javax.swing.JScrollPane();
        contentTree = new intransix.osm.termite.gui.contenttree.ContentTree(this);
		this.addMapDataListener(contentTree);
		this.addLevelSelectedListener(contentTree);
		jScrollPane1.setViewportView(contentTree);
		
		//feature tree
        jScrollPane2 = new javax.swing.JScrollPane();
        featureTree = new intransix.osm.termite.gui.featuretree.FeatureTree(this);
		this.addFeatureLayerListener(featureTree);
		featureTree.setFeatureInfoMap(app.getFeatureInfoMap());
		jScrollPane2.setViewportView(featureTree);
		
		//property tabbed pane
		propertyTabPane = new intransix.osm.termite.gui.property.PropertyTabPane(this);
		this.addLevelSelectedListener(propertyTabPane);
		this.addMapDataListener(propertyTabPane);
        
		//map panel
        mapPanel = new intransix.osm.termite.render.MapPanel();
		mapPanel.setMinimumSize(new java.awt.Dimension(200, 200));
		mapPanel.setPreferredSize(new java.awt.Dimension(600,600));
		this.addMapDataListener(mapPanel);
		mapPanel.addKeyListener(this);
		
		//supplemental tabbed pane
        supplementalTabPane = new javax.swing.JTabbedPane();

		//layout the content panes
		jSplitPane1.setOrientation(javax.swing.JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane1.setDividerLocation(150);
		jSplitPane1.setResizeWeight(.25);
		
		jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane2.setDividerLocation(200);
		jSplitPane3.setResizeWeight(.33);
        
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane3.setDividerLocation(500);
		jSplitPane3.setResizeWeight(.8);
		
		jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane4.setDividerLocation(200);
		jSplitPane3.setResizeWeight(.5);
		
		jSplitPane1.setLeftComponent(jSplitPane2);
		jSplitPane1.setRightComponent(jSplitPane3);
		
		jSplitPane2.setTopComponent(jScrollPane1);
		jSplitPane2.setBottomComponent(jSplitPane4);
		
		jSplitPane4.setTopComponent(jScrollPane2);
		jSplitPane4.setBottomComponent(propertyTabPane);
		
		jSplitPane3.setTopComponent(mapPanel);
        jSplitPane3.setBottomComponent(supplementalTabPane);
		
		//create standard supplemental tabs
		
		this.add(menuBar);
		this.add(toolBarPanel);
		this.add(jSplitPane1);

        pack();
    }
	
	private void addBaseMapMenuItem(final TileInfo tileInfo) {
		
		String name;
		boolean isSelected;
		if(tileInfo != null) {
			name = tileInfo.getName();
			isSelected = false;
		}
		else {
			name = HIDDEN_BASE_MAP_NAME;
			isSelected = true;
		}
		
		JRadioButtonMenuItem menuItem = new javax.swing.JRadioButtonMenuItem();
		baceMapButtonGroup.add(menuItem);
        menuItem.setSelected(isSelected);
        menuItem.setText(name);
		menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectBaseMap(tileInfo);
            }
        });
        baseMapMenu.add(menuItem);
	}
	
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Internal Event Functions ans Classes">
	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {
		app.exit();
	}
	
	private void clearData() {
		int result = JOptionPane.showConfirmDialog(this,"Are you sure you want to discard the current map data?");
		if(result == JOptionPane.OK_OPTION) {
			this.setMapData(null);
		}
	}
	
	private void undoItemActionPerformed(java.awt.event.ActionEvent evt) {
		if(osmData != null) {
			osmData.undo();
		}
	}
	
	private void redoItemActionPerformed(java.awt.event.ActionEvent evt) {
		if(osmData != null) {
			osmData.redo();
		}
	}
	
	private void commitData() {
		CommitTask commitTask = new CommitTask(this);
		commitTask.execute();
	}
	
	private void publishMap() {
		if((activeStructure != null)&&(activeLevel == null)) {
			int result = JOptionPane.showConfirmDialog(null,"Publish Structure " + activeStructure.getId() + "?");
			if(result == JOptionPane.OK_OPTION) {
				PublishTask publishTask = new PublishTask(this,activeStructure.getId());
				publishTask.execute();
			}
		}
		else {
			JOptionPane.showMessageDialog(this,"You must select a level to publish.");
		}
		
	}
	
	private void selectBaseMap(TileInfo tileInfo) {
		baseMapLayer.setTileInfo(tileInfo);
		baseMapLayer.setVisible( (tileInfo != null) ? true : false);
		mapPanel.repaint();
	}
	
	private void manageSourceLayers() {
		SourceLayerDialog sourceLayerDialog = new SourceLayerDialog(this,mapLayerManager);
		sourceLayerDialog.setVisible(true);
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
	// </editor-fold>
}
