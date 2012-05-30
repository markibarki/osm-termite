package intransix.osm.termite.app.gui;

import intransix.osm.termite.map.geom.TermiteLevel;
import intransix.osm.termite.map.geom.TermiteFeature;
import intransix.osm.termite.map.geom.TermiteStructure;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

import intransix.osm.termite.theme.*;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel {
	
	private Theme theme;
	private TermiteStructure structure;
	private TermiteLevel currentLevel;
	
	public MapPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	public void setStructure(TermiteStructure structure) {
		this.structure = structure;
	}
	
	public void setLevel(String levelId) {
		if(structure == null) return;
		
		ArrayList<TermiteLevel> levels = structure.getLevels();
		for(TermiteLevel level:levels) {
			if(levelId.equalsIgnoreCase(level.getId())) {
				currentLevel = level;
				return;
			}
		}
//level not found!!!
		currentLevel = null;
	}
	
//	@Override
//	public Dimension getPreferredSize() {
//        return new Dimension(250,200);
//    }
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		//make sure antialiasing is on
		RenderingHints rh = g2.getRenderingHints();
		if((!rh.containsValue(RenderingHints.KEY_ANTIALIASING))||(rh.get(RenderingHints.KEY_ANTIALIASING) != RenderingHints.VALUE_ANTIALIAS_ON)) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		if((currentLevel == null)||(theme == null)) return;
		for(TermiteFeature feature:currentLevel.getFeatures()) {
			if(feature.getStyle() == null) {
				theme.loadStyle(feature);
			}	
			feature.render(g2);
		}
	}
	
}
