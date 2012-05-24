package intransix.osm.termite.svg;

import intransix.osm.termite.map.geom.*;
import java.io.IOException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import intransix.osm.termite.map.prop.FeatureInfoMap;
import java.awt.geom.*;
import java.awt.*;

/**
 * This method converts between a map level and an SVG document
 * @author sutter
 */
public class SvgConverter {

public Structure structure;
	
	public void loadSvg(String fileName, FeatureInfoMap inputColorMapper) {
		Document doc = loadXmlDoc(fileName);
		
		SvgDocument svgDocument = new SvgDocument();
		svgDocument.load(doc);
		
////////////////////////////////////////////////////////////
//this is just for testing
		this.structure = new Structure();
		structure.setId(1);
		Level level = new Level();
		level.setId(1);
		structure.addLevel(level);
		
		for(SvgGeometry geom:svgDocument.getObjectList()) {
			String fillColor = geom.fill;
			String strokeColor = geom.stroke;
			String inputColor = null;
			if(fillColor != null) inputColor = fillColor;
			else if(strokeColor != null) inputColor = strokeColor;
			else {
				//do something here - exception?
				continue;
			}
			
			Shape shape = geom.shape;
			if(shape instanceof Path2D) {
				PathFeature.FeatureType featureType = (fillColor != null) ?
						PathFeature.FeatureType.AREA : PathFeature.FeatureType.LINE;
				PathFeature f = new PathFeature(featureType);
				f.setPath((Path2D)shape);
				
				inputColorMapper.updateFeature(f,inputColor);
				
				level.addFeature(f);
			}
		}
		
		//order the feature since we updated the list
		//we need to figure out a good way of doing this
		level.orderFeatures();
		
		
////////////////////////////////////////////////////////////
	}
	
	
	/** This method loads the SVG document. */
	private Document loadXmlDoc(String fileName) {
		try {
		    String parser = XMLResourceDescriptor.getXMLParserClassName();
		    SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		    return f.createDocument(fileName);
		    
		} catch (IOException ex) {
		    ex.printStackTrace();
		    return null;
		}
	}
	
}
