package intransix.osm.termite.map.model;

import intransix.osm.termite.map.model.UnchangedException;
import intransix.osm.termite.map.osm.OsmData;
import java.util.*;

/**
 *
 * @author sutter
 */
public class EditAction {
	
	private TermiteData termiteData;
	private OsmData osmData;
	private String desc;
	private List<EditInstruction> instructions = new ArrayList<EditInstruction>();
	
	public EditAction(TermiteData termiteData, String desc) {
		this.termiteData = termiteData;
		this.osmData = termiteData.getWorkingData();
		this.desc = desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void addInstruction(EditInstruction instr) {
		instructions.add(instr);
	}
	
	public boolean doAction() throws Exception {
		int index = 0;
		int cnt = instructions.size();
		EditInstruction instr;
		try {
			for(index = 0; index < cnt; index++) {
				instr = instructions.get(index);
				instr.doInstruction(termiteData);
			}
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index--; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction(termiteData);
			}
			return false;
		}
		
		return true;
	}
	
	public boolean undoAction() throws Exception {
		int index = 0;
		int cnt = instructions.size();
		EditInstruction instr;
		try {
			for(index = cnt - 1; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction(termiteData);
			}
			
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index++; index < cnt; index++) {
				instr = instructions.get(index);
				instr.doInstruction(termiteData);
			}
			return false;
		}

		return true;
	}
}
