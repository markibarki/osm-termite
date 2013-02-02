package intransix.osm.termite.app.filter;

import intransix.osm.termite.map.workingdata.PiggybackData;

/**
 *
 * @author sutter
 */
public class FilterData extends PiggybackData {
	
	private int filterState;
		
	public void setFilterState(int state) {
		this.filterState = state;
	}
	
	public boolean editEnabled() {
		return ((filterState & FilterRule.EDIT_ENABLED) != 0);
	}
	
	public boolean renderEnabled() {
		return ((filterState & FilterRule.RENDER_ENABLED) != 0);
	}
	
	public int getFilterState() {
		return filterState;
	}
	
}
