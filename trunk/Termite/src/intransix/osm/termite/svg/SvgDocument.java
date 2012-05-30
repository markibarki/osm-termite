package intransix.osm.termite.svg;

import intransix.osm.termite.map.geom.TermiteLevel;
import intransix.osm.termite.map.prop.FeatureInfoMap;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.batik.dom.svg.*;
import org.w3c.dom.*;

import java.awt.*;

/**
 *
 * @author sutter
 */
public class SvgDocument extends TransformManager {
	
	//======================================
	// Private Fields
	//======================================
	private Rectangle2D docSize;
	private ArrayList<SvgGeometry> geomList = new ArrayList<SvgGeometry>();

	private final static String DISPLAY_TAG = "display";
	private final static String DISPLAY_NONE = "none";
	
	//======================================
	// Public Methods
	//======================================
	
	public Rectangle2D getDocSize() { return docSize;}
	
	public void setDocSize(Rectangle2D docSize) {
		this.docSize = docSize;
	}
	
	public ArrayList<SvgGeometry> getObjectList() { return geomList;}
	
	public void addGeometry(SvgGeometry geom) {
		geomList.add(geom);
	}
	
	/** This method loads the SVG document. */
	public void load(Document svgDocument) {
		
		Element element = svgDocument.getDocumentElement();
		if(!(element instanceof SVGOMSVGElement)) {
			throw new RuntimeException("Invalid format for SVG document.");
		}
		SVGOMSVGElement svgElement = (SVGOMSVGElement)element;
		
		//load the document size and view
		float docWidth = svgElement.getWidth().getBaseVal().getValue();
		float docHeight = svgElement.getHeight().getBaseVal().getValue();
		docSize = new Rectangle2D.Double(0,0,docWidth,docHeight);
		
		//process the elements in the document
		TransformManager transformManager = new TransformManager();
		NodeList children = element.getChildNodes();
		processChildren(children,transformManager);
	}
	
	public Document create() {
		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		Document doc =  impl.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
		
		int width = (int)Math.ceil(docSize.getWidth());
		int height = (int)Math.ceil(docSize.getHeight());

		Element svgElement = doc.getDocumentElement();
		svgElement.setAttributeNS(null,"id", "svg0");
		svgElement.setAttributeNS(null, "width", String.valueOf(width));
		svgElement.setAttributeNS(null, "height", String.valueOf(height));
		
		//children geometry
		for(SvgGeometry geom:geomList) {
			Element element = SvgCoordinateLoader.getElement(doc, geom);
			svgElement.appendChild(element);
		}
		
		return doc;
	}
	
	//======================================
	// Private Methods
	//======================================
	
	
	/** This method creates the child node objects. */
	private void processChildren(NodeList nodes, TransformManager transformManager) {
		
		//process the children
		int count = nodes.getLength();
		Node node;
		for(int i = 0; i < count; i++) {
			node = nodes.item(i);

			//check if we want to use this geometry
			if(node instanceof SVGGraphicsElement) {
				//go to next if this is not displayed
				String displayValue = ((SVGGraphicsElement)node).getAttribute(DISPLAY_TAG);
				if((displayValue != null)&&(displayValue.equalsIgnoreCase(DISPLAY_NONE))) continue;
				
				if(node instanceof SVGOMGElement) {
					//flatten g, taking any transform from the group tag
					//handle the transforms
					int mark = transformManager.getMark();
					transformManager.loadTransforms((SVGOMGElement)node);	
					//process the nodees from this group element
					NodeList grandChildren = node.getChildNodes();
					processChildren(grandChildren,transformManager);
					transformManager.restoreToMark(mark);
				}
				else if(node instanceof SVGGraphicsElement) {
					Shape shape = SvgCoordinateLoader.loadGeometry((SVGGraphicsElement)node,transformManager);
					SvgGeometry geom = new SvgGeometry((SVGGraphicsElement)node,shape);
					geomList.add(geom);
				}
			}
		}
	}
}

