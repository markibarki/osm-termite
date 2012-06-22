package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.checkout.SearchLayer;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.model.*;
import intransix.osm.termite.util.*;

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
	
	/** This method returns the submode toolbar that will be active when this mode is
	 * active.
	 * 
	 * @return		The submode toolbar 
	 */
	public JToolBar getSubmodeToolbar() {
		if(toolBar == null) {
			createToolBar();
		}
		return toolBar;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		MapPanel mapPanel = termiteGui.getMap();
		searchLayer = new SearchLayer();
		mapPanel.addLayer(searchLayer);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		MapPanel mapPanel = termiteGui.getMap();
		mapPanel.removeLayer(searchLayer);
		mapPanel.removeMouseListener(searchLayer);
		mapPanel.removeMouseMotionListener(searchLayer);
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
		Rectangle2D selection = searchLayer.getSelection();
		if(selection == null) {
			JOptionPane.showMessageDialog(null, "You must select a bounding box to download.");
			return;
		}
		
		//set local coordinates
		double mx = LocalCoordinates.localToMercX(selection.getCenterX());
		double my = LocalCoordinates.localToMercY(selection.getCenterY());
		MapPanel mapPanel = termiteGui.getMap();
		mapPanel.resetLocalAnchor(mx, my);
		
		
		String mapDataFileName = "nodeTestBuilding.xml";
		OsmParser osmParser = new OsmParser();
		OsmData osmData = osmParser.parse(mapDataFileName);
		TermiteData termiteData = new TermiteData();
		termiteData.loadData(osmData);
		
		termiteGui.setEditData(termiteData);
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
