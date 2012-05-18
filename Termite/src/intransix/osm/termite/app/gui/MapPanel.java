package intransix.osm.termite.app.gui;

import intransix.osm.termite.map.geom.Level;
import intransix.osm.termite.map.geom.Feature;
import intransix.osm.termite.map.geom.Structure;
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
	private Structure structure;
	private Level currentLevel;
	
	public MapPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	public void setStructure(Structure structure) {
		this.structure = structure;
	}
	
	public void setLevel(long id) {
		if(structure == null) return;
		
		ArrayList<Level> levels = structure.getLevels();
		for(Level level:levels) {
			if(level.getId() == id) {
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
		for(Feature feature:currentLevel.getFeatures()) {
			if(feature.getStyle() == null) {
				theme.loadStyle(feature);
			}	
			feature.render(g2);
		}
	}
	
}
