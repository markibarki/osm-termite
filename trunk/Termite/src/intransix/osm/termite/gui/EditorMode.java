package intransix.osm.termite.gui;

/**
 * This is the base class for an editor mode. The editor mode controls what the 
 * editor does.
 * 
 * @author sutter
 */
public interface EditorMode {
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	public String getName();
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	public String getIconImageName();
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn();
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff();
}
