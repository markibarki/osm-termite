package intransix.osm.termite.map.data;

/**
 * This is a listener for data edits of the OsmData.
 * 
 * @author sutter
 */
public interface OsmDataChangedListener {
	
	public final static int LISTENER_PREPROCESSOR = 0;
	public final static int LISTENER_CONSUMER = 1;
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	void osmDataChanged(int editNumber);
	
	/** This method returns the type of user this listener is. The type of listener
	 * determines the order in which the listener is called when data has changed. 
	 * 
	 * @return 
	 */
	int getListenerType();
	
}
