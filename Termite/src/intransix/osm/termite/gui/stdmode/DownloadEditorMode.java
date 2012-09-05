package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.MapLayerManager;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.checkout.SearchLayer;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import intransix.osm.termite.util.*;
import intransix.osm.termite.gui.task.MapDataRequestTask;

/**
 *
 * @author sutter
 */
public class DownloadEditorMode extends EditorMode implements ActionListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Download Mode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/downloadMode.png";
	
	private final static String SEARCH_CMD = "saerch";
	private final static String DOWNLOAD_CMD = "download";
	
	private final static int SPACE_X = 50;
	private final static int SPACE_Y = 3;

	private TermiteGui termiteGui;
	private SearchLayer searchLayer;
	private JToolBar toolBar = null;	
	private JTextField searchField;
	
	//====================
	// Public Methods
	//====================
	
	public DownloadEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
		createToolBar();
		
		setDataEnabledStates(false,true);
	}
	
	/** This method will be called to set needed map layers. */
	@Override
	public void setLayers(MapLayerManager mapLayerManager) {
		searchLayer = mapLayerManager.getSearchLayer();
	}
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	public String getName() {
		return MODE_NAME;
	}
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	public String getIconImageName() {
		return ICON_NAME;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		searchLayer.setActiveState(true);
		termiteGui.addToolBar(toolBar);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		searchLayer.setActiveState(false);
		termiteGui.removeToolBar(toolBar);
	}	
	
	public void actionPerformed(ActionEvent ae) {
		if(DOWNLOAD_CMD.equals(ae.getActionCommand())) {
			doDownload();
		}
		else if(SEARCH_CMD.equals(ae.getActionCommand())) {
			doSearch();
		}
	}
	
	private void doDownload() {
		Rectangle2D selection = searchLayer.getSelectionMercator();
		if(selection == null) {
			JOptionPane.showMessageDialog(null, "You must select a bounding box to download.");
			return;
		}
		
		//get the bounding box
		double minLat = Math.toDegrees(MercatorCoordinates.myToLatRad(selection.getMaxY()));
		double minLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(selection.getMinX()));
		double maxLat = Math.toDegrees(MercatorCoordinates.myToLatRad(selection.getMinY()));
		double maxLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(selection.getMaxX()));
		
		//set local coordinates
		MapPanel mapPanel = termiteGui.getMapPanel();
		mapPanel.resetLocalCoordinates();
		
		//run the load data task
		MapDataRequestTask mdrt = new MapDataRequestTask(termiteGui,minLat,minLon,maxLat,maxLon);
		mdrt.execute();
		mdrt.blockUI();
	}
	
	private void doSearch() {
		JOptionPane.showMessageDialog(null, "Search is not implmented.");
	}
	
	
	private void createToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		JLabel label = new JLabel("Select area on map and press ");
		toolBar.add(label);
		JButton downloadButton = new JButton("Download");
		toolBar.add(downloadButton);
		
		Box.Filler space = new javax.swing.Box.Filler(new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y));
		toolBar.add(space);
		
		
//		toolBar.add(new JLabel("Enter a location: "));
		JTextField textField = new JTextField();
		textField.setColumns(25);
		textField.setMaximumSize(textField.getPreferredSize());
		toolBar.add(textField);
		JButton searchButton = new JButton("Search");
		
		toolBar.add(searchButton);
		
		
		//add action listeners
		searchButton.setActionCommand(SEARCH_CMD);
		searchButton.addActionListener(this);
		downloadButton.setActionCommand(DOWNLOAD_CMD);
		downloadButton.addActionListener(this);
	}
}
