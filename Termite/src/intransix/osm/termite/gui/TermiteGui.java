package intransix.osm.termite.gui;

import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.app.TermiteApp;

/**
 *
 * @author sutter
 */
public class TermiteGui extends javax.swing.JFrame {
	
	//=====================
	// Private Properties
	//=====================
	
	private TermiteApp app;
	
//	private EditorMode searchMode; //not an edit mode - has no button
//	private List<EditorMode> editModes;
//	private TermiteData termiteData;
//	private Theme theme;
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
		initialize();
	}
	
	public MapPanel getMap() {
		return mapPanel;
	}

	private void initialize() {
		//MODES
		//load edit modes into the mode toolbar
		//load the searc editor mode
		
		//MAP
		//create the layers: base, render, edit, search
		
		//DATA PANEL
		//create the map layer panel
		//create the relation panel
		//create ano other panels
		
		//set state to "search"
		
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
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modeToolBar = new javax.swing.JToolBar();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane4 = new javax.swing.JSplitPane();
        propertyTabPane = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        featureTree = new intransix.osm.termite.gui.featuretree.FeatureTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        contentTree = new intransix.osm.termite.gui.contenttree.ContentTree();
        jSplitPane3 = new javax.swing.JSplitPane();
        mapPanel = new intransix.osm.termite.render.MapPanel();
        DataTabPane = new javax.swing.JTabbedPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        quitItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1000, 800));

        modeToolBar.setFloatable(false);
        modeToolBar.setRollover(true);

        jSplitPane1.setDividerLocation(150);

        jSplitPane2.setDividerLocation(200);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane4.setDividerLocation(200);
        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        propertyTabPane.setName("");
        jSplitPane4.setRightComponent(propertyTabPane);

        jScrollPane3.setViewportView(featureTree);

        jSplitPane4.setTopComponent(jScrollPane3);

        jSplitPane2.setBottomComponent(jSplitPane4);

        jScrollPane1.setViewportView(contentTree);

        jSplitPane2.setTopComponent(jScrollPane1);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jSplitPane3.setDividerLocation(500);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        mapPanel.setMinimumSize(new java.awt.Dimension(100, 100));
        mapPanel.setPreferredSize(new java.awt.Dimension(1200, 800));

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 840, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 98, Short.MAX_VALUE)
        );

        jSplitPane3.setTopComponent(mapPanel);
        jSplitPane3.setRightComponent(DataTabPane);

        jSplitPane1.setRightComponent(jSplitPane3);

        fileMenu.setText("File");

        quitItem.setText("Quit");
        quitItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(modeToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                .addGap(369, 369, 369))
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(modeToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void quitItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitItemActionPerformed
		app.exit();
	}//GEN-LAST:event_quitItemActionPerformed

	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane DataTabPane;
    private intransix.osm.termite.gui.contenttree.ContentTree contentTree;
    private intransix.osm.termite.gui.featuretree.FeatureTree featureTree;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private intransix.osm.termite.render.MapPanel mapPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JToolBar modeToolBar;
    private javax.swing.JTabbedPane propertyTabPane;
    private javax.swing.JMenuItem quitItem;
    // End of variables declaration//GEN-END:variables
}
