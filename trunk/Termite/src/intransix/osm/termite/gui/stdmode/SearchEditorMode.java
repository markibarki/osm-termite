/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class SearchEditorMode implements EditorMode {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Search Mode";

	private TermiteGui termiteGui;
	private JToolBar toolBar = null;
	
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
		
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		
	}	
	
	
	private void createToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JLabel("Enter a location: "));
		JTextField textField = new JTextField();
		textField.setColumns(25);
		textField.setMaximumSize(textField.getPreferredSize());
		toolBar.add(textField);
		toolBar.add(new JButton("Search"));
		toolBar.add(new JButton("Download Data"));
	}
}
