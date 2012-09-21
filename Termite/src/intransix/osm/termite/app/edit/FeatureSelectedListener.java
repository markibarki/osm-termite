package intransix.osm.termite.app.edit;

import intransix.osm.termite.map.data.OsmObject;
import java.util.List;

/**
 * This interface is used to receive notification when a map feature is selected. 
 * 
 * @author sutter
 */
public interface FeatureSelectedListener {
	
	/** This enumeration indicates the selection type. */
	public enum SelectionType {
		NONE,	//no selection
		NODE,	//a single node selected
		VIRTUAL_NODE,	//a singel virtual node selected
		WAY,	//a single way selected
		COLLECTION	// a collection of nodes and ways (no virtual nodes)
	}
	
	/** This enumeration is valid if the selection is a way. This indicates
	 * if nodes within the way are a part of the selection. */
	public enum WayNodeType {
		NONE, //no way nodes selected
		SINGLE,	//a single way node selected
		MULTIPLE	//multiple way nodes selected
	}
	
	/** This method is called when a map feature is selected. The arguments selectionType
	 * and wayNodeType indicate the type of selection made. The list objects may be null
	 * if there is no selection for the list. The lists that are passed as arguments 
	 * should be read only. They should not be written to.
	 * 
	 * @param selection			A list of the selected objects
	 * @param selectionType		The type objects objects in the selection
	 * @param wayNodeSelection	If the selection is a single way, this is a possible list
	 *							of selected nodes within the way.
	 * @param wayNodeType		This is the type of way nodes selected for the way, if applicable.
	 */
	void onFeatureSelected(List<Object> selection, SelectionType selectionType,
			List<Integer> wayNodeSelection, WayNodeType wayNodeType);
}
