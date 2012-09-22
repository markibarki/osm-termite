package intransix.osm.termite.gui;

import intransix.osm.termite.app.mode.EditorMode;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import intransix.osm.termite.app.TermiteApp;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.render.*;
import intransix.osm.termite.render.tile.TileInfo;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.gui.dialog.SourceLayerDialog;
import intransix.osm.termite.gui.task.CommitTask;
import intransix.osm.termite.publish.PublishTask;
import java.io.File;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.map.data.OsmDataChangedListener;
import intransix.osm.termite.app.mode.EditorModeManager;
import intransix.osm.termite.app.mode.EditorModeListener;
import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.basemap.BaseMapListener;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;

/**
 * This is the main UI class. It initializes the UI and it manages event flow.
 * 
 * @author sutter
 */
public class TermiteGui extends javax.swing.JFrame implements 
		MapDataListener, OsmDataChangedListener, EditorModeListener, BaseMapListener {
	
	//=====================
	// Private Properties
	//=====================
	
	private final static String HIDDEN_BASE_MAP_NAME = "Hidden";
	private final static String MAP_LAYER_PANE_NAME = "Map Layers";
	
	private final static String UNDO_ITEM_BASE = "Undo: ";
	private final static String UNDO_ITEM_TEXT = "Undo";
	private final static String REDO_ITEM_BASE = "Redo: ";
	private final static String REDO_ITEM_TEXT = "Redo";
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	private TermiteApp app;
	
	//map dasta
	private MapDataManager mapDataManager;
	private OsmData osmData;
	
	//editor modes
	private EditorModeManager modeManager;
	private HashMap<EditorMode,JToggleButton> modeButtons = new HashMap<EditorMode,JToggleButton>();
	private JToolBar submodeToolBar = null;
	private ButtonGroup editModeButtonGroup;
	
	//base maps
	private BaseMapManager baseMapManager;
	private HashMap<TileInfo,JRadioButtonMenuItem> tileInfoMap = new HashMap<TileInfo,JRadioButtonMenuItem>();
	private JRadioButtonMenuItem hiddenBaseMapMenuItem;
	private javax.swing.ButtonGroup baceMapButtonGroup;
	
	//level selection
	private intransix.osm.termite.gui.contenttree.ContentTree contentTree;
	
	//feature type
	private intransix.osm.termite.gui.featuretree.FeatureTree featureTree;
	
	//property editing
	private intransix.osm.termite.gui.property.PropertyTabPane propertyTabPane; 
    
	//map panel
	private MapLayerManager mapLayerManager; 
    private intransix.osm.termite.render.MapPanel mapPanel;
	
	//map layers control
	private intransix.osm.termite.gui.maplayer.LayerManagerPanel layerManagerPanel;
	
	//level
	private LevelManager levelManager;
	
	//this is used for keeping track of the workign directory
	private File workingDirectory;

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
	
	private javax.swing.JPanel toolBarPanel;
	private javax.swing.JToolBar modeToolBar;
	
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
	
    private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
    
	private javax.swing.JTabbedPane supplementalTabPane;
	

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
	
	// <editor-fold defaultstate="collapsed" desc="Initialization">
	
	public void setMapDataManager(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
		OsmData tempData = mapDataManager.getOsmData();
		
		this.onMapData(tempData);
		mapDataManager.addMapDataListener(this);
		
		//set data for property tab pane
		propertyTabPane.onMapData(tempData);
		mapDataManager.addMapDataListener(propertyTabPane);
	}
	
	public void setModeManager(EditorModeManager editorModeManager) {
		this.modeManager = editorModeManager;
		
		editModeButtonGroup = new ButtonGroup();
		modeButtons.clear();
		for(EditorMode mode:modeManager.getEditorModes()) {
			
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
			button.addActionListener(new ModeButtonListener(mode,modeManager));
			button.setMargin(new Insets(1,1,1,1));
			button.setToolTipText(name);
			button.setEnabled(mode.getModeEnabled());
			editModeButtonGroup.add(button);
			modeToolBar.add(button);
			modeButtons.put(mode, button);
		}
		
		modeManager.addModeListener(this);
	}
	
	public void setBaseMapManager(BaseMapManager baseMapManager) {
		this.baseMapManager = baseMapManager;
		baseMapManager.addBaseMapListener(this);
		java.util.List<TileInfo> tileInfoList = baseMapManager.getBaseMapList();
		
		//add base map choices
		addBaseMapMenuItem(null);
		if(tileInfoList != null) {
			for(TileInfo tileInfo:tileInfoList) {
				addBaseMapMenuItem(tileInfo);
			}
		}
	}
	
	public void setEditManager(EditManager editManager) {
		editManager.addFeatureSelectedListener(propertyTabPane);
	}
	
	public void setMapLayerManager(MapLayerManager mapLayerManager) {
		java.util.List<MapLayer> mapLayers = mapLayerManager.getMapLayers();
		
		layerManagerPanel.layerListChanged(mapLayers);
		mapLayerManager.addLayerListener(layerManagerPanel);
		
		mapPanel.layerListChanged(mapLayers);
		mapLayerManager.addLayerListener(mapPanel);	
	}
	
	public void setFeatureTypeManager(FeatureTypeManager featureTypeManager) {
		featureTree.setFeatureTypeManager(featureTypeManager);
	}
	
	public void setLevelManager(LevelManager levelManager) {
		this.levelManager = levelManager;
		contentTree.setLevelManager(levelManager);
		levelManager.addLevelSelectedListener(propertyTabPane);
	}
	
	public void setViewRegionManager(ViewRegionManager viewRegionManager) {
		mapPanel.setViewRegionManager(viewRegionManager);
	}
	
	// </editor-fold>
	
	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param mapData	The map data object
	 */
	@Override
	public void onMapData(OsmData osmData) {
		//clear listener from old data
		if(this.osmData != null) {
			this.osmData.removeDataChangedListener(this);
		}
		
		this.osmData = osmData;
		
		if(this.osmData != null) {
			this.osmData.addDataChangedListener(this);
		}
		
		updateUndoRedoItems();
	}
	
	/** This method is called when the data has changed. It updates the undo
	 * and redo actions.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		updateUndoRedoItems();
	}
	
	/** This method returns the type of user this listener is. The type of listener
	 * determines the order in which the listener is called when data has changed. 
	 * 
	 * @return 
	 */
	@Override
	public int getListenerType() {
		return OsmDataChangedListener.LISTENER_CONSUMER;
	}
	
	/** This method is called when the baseMap changes. */
	@Override
	public void baseMapChanged(TileInfo tileInfo) {
		JRadioButtonMenuItem selectedMenuItem = null;
		if(tileInfo != null) {
			selectedMenuItem = tileInfoMap.get(tileInfo);
		}
		if(selectedMenuItem == null) {
			selectedMenuItem = hiddenBaseMapMenuItem;
		}
		selectedMenuItem.setSelected(true);
	}
	
		/** This method is called when the mode changes. */
	@Override
	public void activeModeChanged(EditorMode activeMode) {
		JToggleButton button = modeButtons.get(activeMode);
		button.setSelected(true);
		
		//update the toolbar
		if(submodeToolBar != null) {
			removeToolBar(submodeToolBar);
		}
		submodeToolBar = activeMode.getToolBar();
		if(submodeToolBar != null) {
			addToolBar(submodeToolBar);
		}
	}
	
	/** This is called is a mode goes from disabled to enabled. */
	@Override
	public void modeEnableChanged(EditorMode mode) {
		JToggleButton button = modeButtons.get(mode);
		button.setEnabled(mode.getModeEnabled());
	}
	
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

	
	private void updateUndoRedoItems() {
		boolean undoSet = false;
		boolean redoSet = false;
		if(osmData != null) {
			String undoMessage = osmData.getUndoMessage();
			if(undoMessage != null) {
				undoItem.setText(UNDO_ITEM_BASE + undoMessage);
				undoItem.setEnabled(true);
				undoSet = true;
			}
			
			String redoMessage = osmData.getRedoMessage();
			if(redoMessage != null) {
				redoItem.setText(REDO_ITEM_BASE + redoMessage);
				redoItem.setEnabled(true);
				redoSet = true;
			}
			
		}
		
		if(!undoSet) {
			undoItem.setText(UNDO_ITEM_TEXT);
			undoItem.setEnabled(false);
		}
		
		if(!redoSet) {
			redoItem.setText(REDO_ITEM_TEXT);
			redoItem.setEnabled(false);
		}
	}

	
	//================================
	// Private Methods
	//================================

	
	// <editor-fold defaultstate="collapsed" desc="Initialization Methods">

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
		jScrollPane1.setViewportView(contentTree);
		
		//feature tree
        jScrollPane2 = new javax.swing.JScrollPane();
        featureTree = new intransix.osm.termite.gui.featuretree.FeatureTree();
		jScrollPane2.setViewportView(featureTree);
		
		//property tabbed pane
		propertyTabPane = new intransix.osm.termite.gui.property.PropertyTabPane();
        
		//map panel
        mapPanel = new intransix.osm.termite.render.MapPanel();
		mapPanel.setMinimumSize(new java.awt.Dimension(200, 200));
		mapPanel.setPreferredSize(new java.awt.Dimension(600,600));
		
		//supplemental tabbed pane
        supplementalTabPane = new javax.swing.JTabbedPane();

		//map layer panel
		layerManagerPanel = new intransix.osm.termite.gui.maplayer.LayerManagerPanel();
		supplementalTabPane.addTab(MAP_LAYER_PANE_NAME,layerManagerPanel);
		
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
		JRadioButtonMenuItem menuItem = new javax.swing.JRadioButtonMenuItem();
		
		if(tileInfo != null) {
			name = tileInfo.getName();
			isSelected = false;
			tileInfoMap.put(tileInfo,menuItem);
		}
		else {
			name = HIDDEN_BASE_MAP_NAME;
			isSelected = true;
			hiddenBaseMapMenuItem = menuItem;
		}
		
		baceMapButtonGroup.add(menuItem);
        menuItem.setSelected(isSelected);
        menuItem.setText(name);
		menuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseMapManager.setBaseMap(tileInfo);
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
			mapDataManager.setOsmData(null);
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
		if(osmData == null) {
			JOptionPane.showMessageDialog(this,"There is no data to publish.");
			return;
		}
		
		CommitTask commitTask = new CommitTask(osmData,app.getLoginManager());
		commitTask.execute();
	}
	
	private void publishMap() {
		if(osmData == null) {
			JOptionPane.showMessageDialog(this,"There is no data to publish.");
			return;
		}
		
		OsmWay activeStructure = levelManager.getSelectedStructure();
		OsmRelation activeLevel = levelManager.getSelectedLevel();
		if((activeStructure != null)&&(activeLevel == null)) {
			int result = JOptionPane.showConfirmDialog(null,"Publish Structure " + activeStructure.getId() + "?");
			if(result == JOptionPane.OK_OPTION) {
				PublishTask publishTask = new PublishTask(osmData,activeStructure.getId());
				publishTask.execute();
			}
		}
		else {
			JOptionPane.showMessageDialog(this,"You must select a level to publish.");
		}	
	}
	
	private void manageSourceLayers() {
		SourceLayerDialog sourceLayerDialog = new SourceLayerDialog(this,mapLayerManager);
		sourceLayerDialog.setVisible(true);
	}
	
	/** This is a listener for the mode buttons. */
	private class ModeButtonListener implements ActionListener {
		
		private EditorMode mode;
		private EditorModeManager modeManager;
		
		public ModeButtonListener(EditorMode mode, EditorModeManager modeManager) {
			this.mode = mode;
			this.modeManager = modeManager;
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			modeManager.setEditorMode(mode);
		}
	}
	// </editor-fold>
}
