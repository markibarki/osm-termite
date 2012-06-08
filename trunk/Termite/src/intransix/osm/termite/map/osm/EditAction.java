package intransix.osm.termite.map.osm;

import java.util.*;

/**
 *
 * @author sutter
 */
public class EditAction {
	
	private String desc;
	private List<EditInstruction> instructions = new ArrayList<EditInstruction>();
	
	
	public EditAction(String desc) {
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
				instr.doInstruction();
			}
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index--; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction();
			}
			return false;
		}
		
		completeAction();
		return true;
	}
	
	public boolean undoAction() throws Exception {
		int index = 0;
		int cnt = instructions.size();
		EditInstruction instr;
		try {
			for(index = cnt - 1; index >= 0; index--) {
				instr = instructions.get(index);
				instr.undoInstruction();
			}
			
		}
		catch(UnchangedException ex) {
			//we can fix one unchanged exception
			//undo any actions to this point
			//starting with the previous
			for(index++; index < cnt; index++) {
				instr = instructions.get(index);
				instr.doInstruction();
			}
			return false;
		}
		
		completeAction();
		return true;
	}
	
	private void completeAction() {
		//do whatever has to be done
throw new RuntimeException("Implemenet this");
	}
}
