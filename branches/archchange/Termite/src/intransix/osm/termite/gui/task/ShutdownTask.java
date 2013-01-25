package intransix.osm.termite.gui.task;

import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.app.TermiteApp;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class ShutdownTask extends SwingWorker<Object,Object> {
	
	private TermiteApp app;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public ShutdownTask(TermiteApp app) {
		this.app = app;
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(null,this,"Shutting down...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		try {
			app.preshutdown();
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return "";
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(!success) {
			JOptionPane.showMessageDialog(null,"There was an error shutting down: " + errorMsg);
		}	
	}
}
