package intransix.osm.termite.app.edit;

import java.util.List;

/**
 * This interface is used to receive notification when a map feature is selected. 
 * 
 * @author sutter
 */
public interface FeatureSelectedListener {
	
	/** This method is called when a map feature is selected. The arguments selectionType
	 * and wayNodeType indicate the type of selection made. The list objects may be null
	 * if there is no selection for the list. The lists that are passed as arguments 
	 * should be read only. They should not be written to.
	 * 
	 * @param selection			A list of the selected objects
	 * @param wayNodeSelection	If the selection is a single way, this is a possible list
	 *							of selected nodes within the way.
	 */
	void onFeatureSelected(List<Object> selection, List<Integer> wayNodeSelection);
}
