package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.util.GraduatedList;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.MapDataListener;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, MouseListener, MouseMotionListener {
	
	private final static double RADIUS_PIXELS = 5; 
	
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color HIGHLIGHT_COLOR = Color.MAGENTA;
	private final static Color BACKGROUND_HIGHLIGHT_COLOR = Color.PINK;
	
	private OsmData osmData;
	
	private OsmNode activeNode = null;
	
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
	}
	
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
				mapPanel.addMouseListener(this);
				mapPanel.addMouseMotionListener(this);
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
			}
		}
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();		
		
		OsmNode node = activeNode;
		Point2D pixXY = new Point2D.Double();
		if(node != null) {
			mercatorToPixels.transform(node.getPoint(),pixXY);
			g2.setColor(HIGHLIGHT_COLOR);
			Shape rect = new Rectangle2D.Double(pixXY.getX()-RADIUS_PIXELS,
					pixXY.getY()-RADIUS_PIXELS,2*RADIUS_PIXELS,2*RADIUS_PIXELS);
			g2.fill(rect);
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
		activeNode = null;
	}
	
	public void mouseMoved(MouseEvent e) {
		//read mouse location
		MapPanel mapPanel = getMapPanel();
		Point2D mousePix = new Point2D.Double(e.getX(),e.getY());
		Point2D nodePix = new Point2D.Double();
		AffineTransform mercatorToPixels = mapPanel.getMercatorToPixels();
		
		
		//loook for a point
		GraduatedList<OsmObject> objectList = osmData.getOrderedList();
		for(java.util.List<OsmObject> subList:objectList.getLists()) {
			for(OsmObject mapObject:subList) {
				if(mapObject instanceof OsmNode) {
					mercatorToPixels.transform(((OsmNode)mapObject).getPoint(),nodePix);
					double d = mousePix.distance(nodePix);
					if(d < RADIUS_PIXELS) {
						this.activeNode = (OsmNode)mapObject;
						mapPanel.repaint();
						return;
					}
				}
			}			
		}
		if(activeNode != null) {
			activeNode = null;
			mapPanel.repaint();
		}
	}
	
	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}
}
