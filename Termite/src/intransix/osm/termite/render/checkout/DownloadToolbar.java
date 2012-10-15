package intransix.osm.termite.render.checkout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import intransix.osm.termite.app.mapdata.DownloadEditorMode;

/**
 *
 * @author sutter
 */
public class DownloadToolbar extends JToolBar implements ActionListener {
	
	private final static String SEARCH_CMD = "search";
	private final static String CLEAR_CMD = "clear";
	private final static String DOWNLOAD_CMD = "download";
	
	private final static int SPACE_X = 50;
	private final static int SPACE_Y = 3;
		
	private JTextField searchField;
	private DownloadEditorMode downloadEditorMode;
	
	public DownloadToolbar(DownloadEditorMode downloadEditorMode) {
		this.downloadEditorMode = downloadEditorMode;
		this.initialize();
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(DOWNLOAD_CMD.equals(ae.getActionCommand())) {
			downloadEditorMode.doDownload();
		}
		if(CLEAR_CMD.equals(ae.getActionCommand())) {
			downloadEditorMode.clearSelection();
		}
		else if(SEARCH_CMD.equals(ae.getActionCommand())) {
			String searchText = this.searchField.getText();
			if(searchText != null) {
				JOptionPane.showMessageDialog(null,"You must enter a search string");
				return;
			}
			downloadEditorMode.doSearch(searchText);
		}
	}
	
		
	private void initialize() {
		this.setFloatable(false);
		
		JLabel label = new JLabel("Click map to start selection. Click again to complete. ");
		this.add(label);
		JButton downloadButton = new JButton("Download");
		this.add(downloadButton);
		JButton clearButton = new JButton("Clear [esc]");
		this.add(clearButton);
		
		Box.Filler space = new javax.swing.Box.Filler(new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y));
		this.add(space);
		
		JTextField textField = new JTextField();
		textField.setColumns(25);
		textField.setMaximumSize(textField.getPreferredSize());
		this.add(textField);
		JButton searchButton = new JButton("Search");
		
		this.add(searchButton);
		
		
		//add action listeners
		downloadButton.setActionCommand(DOWNLOAD_CMD);
		downloadButton.addActionListener(this);
		clearButton.setActionCommand(CLEAR_CMD);
		clearButton.addActionListener(this);
		searchButton.setActionCommand(SEARCH_CMD);
		searchButton.addActionListener(this);
	}
}
