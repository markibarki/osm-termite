package intransix.osm.termite.svg;

import java.io.IOException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

/**
 * This method converts between a map level and an SVG document
 * @author sutter
 */
public class SvgConverter {
	
	public void loadSvg(String fileName) {
		Document doc = loadXmlDoc(fileName);
		
		SvgDocument svgDocument = new SvgDocument();
		svgDocument.load(doc);
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
