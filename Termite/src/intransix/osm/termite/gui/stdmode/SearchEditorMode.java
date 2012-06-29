package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
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
public class SearchEditorMode implements EditorMode, ActionListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Search Mode";
	
	private final static String SEARCH_CMD = "saerch";
	private final static String DOWNLOAD_CMD = "download";

	private TermiteGui termiteGui;
	private JToolBar toolBar = null;	
	private JTextField searchField;
	
	private SearchLayer searchLayer;
	
	//====================
	// Public Methods
	//====================
	
	public SearchEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
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
		return null;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		MapPanel mapPanel = termiteGui.getMapPanel();
		searchLayer = new SearchLayer();
		mapPanel.addLayer(searchLayer);
		
		if(toolBar == null) {
			createToolBar();
		}
		termiteGui.addToolBar(toolBar);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		MapPanel mapPanel = termiteGui.getMapPanel();
		mapPanel.removeLayer(searchLayer);
		mapPanel.removeMouseListener(searchLayer);
		mapPanel.removeMouseMotionListener(searchLayer);
		
		termiteGui.removeToolBar(toolBar);
		
		searchLayer = null;	
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
		toolBar.add(new JLabel("Enter a location: "));
		JTextField textField = new JTextField();
		textField.setColumns(25);
		textField.setMaximumSize(textField.getPreferredSize());
		toolBar.add(textField);
		JButton searchButton = new JButton("Search");
		JButton downloadButton = new JButton("Download Data");
		toolBar.add(searchButton);
		toolBar.add(downloadButton);
		
		//add action listeners
		searchButton.setActionCommand(SEARCH_CMD);
		searchButton.addActionListener(this);
		downloadButton.setActionCommand(DOWNLOAD_CMD);
		downloadButton.addActionListener(this);
	}
}
