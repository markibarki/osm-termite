package intransix.osm.termite.svg;

import java.util.HashMap;

import org.apache.batik.dom.svg.*;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import java.awt.Shape;


public class SvgGeometry {

	//====================================
	// Fields
	//====================================
	public String id;
	public String fill;
	public String stroke;
	public double strokeWidth;
	public Shape shape;
	
	//====================================
	// Public Functions
	//====================================
	
	public SvgGeometry(SVGGraphicsElement svgObjectElement, Shape shape) {
		
		this.shape = shape;
		
		//get id
		id = svgObjectElement.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
		
		//get style properties
		HashMap<String,String> styleTable = null;
		String style = svgObjectElement.getAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE);
		if((style != null)&&(style.length() > 0)) {
			styleTable = getStyleTable(style);
		}
		
		String fillString = svgObjectElement.getAttribute(SVGConstants.SVG_FILL_ATTRIBUTE);
		if(((fillString == null)||(fillString.length() == 0))&&(styleTable != null)) {
			fillString = styleTable.get(CSSConstants.CSS_FILL_PROPERTY);
		}
		if((fillString == null)||(fillString.length() == 0)) fillString = null;
		
		if(fillString != null) {
			fill = formatColor(fillString);
		}
		else {
			fill = null;
		}
		
		String strokeString = svgObjectElement.getAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE);
		if(((strokeString == null)||(strokeString.length() == 0))&&(styleTable != null)) {
			strokeString = styleTable.get(CSSConstants.CSS_STROKE_PROPERTY);
		}
		if((strokeString == null)||(strokeString.length() == 0)) strokeString = null;
		
		if(strokeString != null) {
			stroke = formatColor(strokeString);
		}
		else {
			stroke = null;
		}
		
		String widthString = svgObjectElement.getAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE);
		if(((widthString == null)||(widthString.length() == 0))&&(styleTable != null)) {
			widthString = styleTable.get(CSSConstants.CSS_STROKE_WIDTH_PROPERTY);
		}
		if((widthString == null)||(widthString.length() == 0)) widthString = null;
		
		if(widthString != null) {
			widthString = widthString.replace("px", "");
			strokeWidth = Double.parseDouble(widthString);
		}
		else {
			strokeWidth = 0;
		}
	}
	


	//====================================
	// Private Functions
	//====================================
	
	private String formatColor(String colorString) {
		if(colorString.equals("none")) return null;
		return colorString;
	}
	
	private static HashMap<String,String> getStyleTable(String style) {
		HashMap<String,String> styleTable = new HashMap<String,String>();
		String[] pairs = style.split(";");
		String[] keyValue;
		for(String pair:pairs) {
			keyValue = pair.split(":");
			if(keyValue.length != 2) {
				throw new RuntimeException("Invalid style string format");
			}
			styleTable.put(keyValue[0].trim(), keyValue[1].trim());
		}
		return styleTable;
	}
}

