package intransix.osm.termite.app.mapdata.commit;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.dataset.*;

import intransix.osm.termite.net.NetRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import intransix.osm.termite.util.MercatorCoordinates;
import intransix.osm.termite.util.PropertyPair;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;

import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.util.*;
import intransix.osm.termite.util.SaxUtils;
import java.awt.geom.Point2D;

/**
 * This class manages on OSM commit request.
 * 
 * @author sutter
 */
public class CommitRequest extends DefaultHandler implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmChangeSet changeSet;
	private String url;
	private List<UpdateInfo> updateList = new ArrayList<UpdateInfo>();
	
	//==========================
	// Public Methods
	//==========================
	
	/** Constructor. */
	public CommitRequest(OsmChangeSet changeSet) {
		this.changeSet = changeSet;
		
		String path = String.format(Locale.US,OsmModel.COMMIT_REQUEST_PATH,changeSet.getId());
		url = OsmModel.OSM_SERVER + path;
	}
	
	/** This returns the update list for the commit. */
	public List<UpdateInfo> getUpdateList() {
		return updateList;
	}
	
	/** This method should return the url. */
	@Override
	public String getUrl() {
		return url;
	}
	
	/** This method returns the HTTP request method. */
	@Override
	public String getMethod() {
		return "POST";
	}
	
	/** This method should return true if there is a payload. */
	@Override
	public boolean getHasPayload() {
		return true;
	}
	
	/** This method should be implemented to write the XMl body, if there is a payload. */
	@Override
	public void writeRequestBody(OutputStream os) throws Exception {
		
		
		List<OsmChangeObject> created = changeSet.getCreated();
		List<OsmChangeObject> updated = changeSet.getUpdated();
		List<OsmChangeObject> deleted = changeSet.getDeleted();
		long changeSetId = changeSet.getId();
		
		//write the xml
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xsw = factory.createXMLStreamWriter(os);
		
		xsw.writeStartDocument();
		
		xsw.writeStartElement("osmChange");
		xsw.writeAttribute("version","0.3");
		xsw.writeAttribute("generator","Termite");
		
		if(!created.isEmpty()) {
			xsw.writeStartElement("create");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			for(OsmChangeObject co:created) {
				writeEntry(xsw, co, changeSetId);
			}
			xsw.writeEndElement();
		}
		
		if(!updated.isEmpty()) {
			xsw.writeStartElement("modify");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			for(OsmChangeObject co:updated) {
				writeEntry(xsw, co, changeSetId);
			}
			xsw.writeEndElement();
		}
		
		if(!deleted.isEmpty()) {
			xsw.writeStartElement("delete");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			xsw.writeAttribute("if-unused","*");
			for(OsmChangeObject co:deleted) {
				writeEntry(xsw, co, changeSetId);
			}
			xsw.writeEndElement();
		}
		
		xsw.writeEndElement();
		xsw.writeEndDocument();
		
		xsw.flush();
		xsw.close();
	}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(int responseCode, InputStream is) throws Exception {
		if(responseCode == 200) {
			//parse xml
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();	
			saxParser.parse(is,this);
		}
		else {
			String bodyText = NetRequest.readText(is);
			System.out.println(bodyText);
		}
	}
	
	/** This is the SAX parser start element method. */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if((name.equals("node"))||(name.equals("way"))||(name.equals("relation"))) {
			UpdateInfo info = new UpdateInfo();
			info.objectType = name;
			info.oldId = SaxUtils.getLong(attributes, "old_id", OsmData.INVALID_ID);
			info.newId = SaxUtils.getLong(attributes, "new_id", OsmData.INVALID_ID);
			info.newVersion = attributes.getValue("new_version");
			updateList.add(info);
		}
	}

	/** This is the SAX parser end element method. */
	@Override
	public void endElement(String uri, String localName,
			String name) throws SAXException {
	}

	/** This is the SAX parser characters method. */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
	}
	
	//=========================
	// Private Methods
	//=========================
	
	public void writeEntry(XMLStreamWriter xsw, OsmChangeObject changeObject, long changeSetId) throws Exception {

		OsmSrcData initialObject = changeObject.getInitialObject();
		OsmSrcData finalObject = changeObject.getFinalObject();
		Point2D point = null;
		List<Long> nodes = null;
		List<OsmRelationSrc.Member> members = null;

		//get an object on which to the object base
		OsmSrcData castObject = finalObject != null ? finalObject : initialObject;
		if(castObject == null) return;

		//write the start element, and read type-based data
		if(castObject instanceof OsmNodeSrc) {
			xsw.writeStartElement("node");
			
			//only write point if there is a final object
			if(finalObject != null) {
				point = ((OsmNodeSrc)finalObject).getPoint();
			}
		}
		else if(castObject instanceof OsmWaySrc) {
			xsw.writeStartElement("way");
			
			//only write way if there is a final object
			if(finalObject != null) {
				nodes = ((OsmWaySrc)finalObject).getNodeIds();
			}
		}
		else if(castObject instanceof OsmRelationSrc) {
			xsw.writeStartElement("relation");
			
			//only write members if there is a final object
			if(finalObject != null) {
				members = ((OsmRelationSrc)finalObject).getMembers();
			}
		}
		
		//add standard attributes
		Long id = castObject.getId();
		xsw.writeAttribute("id",String.valueOf(id));
		//point, in the case of nodes
		if(point != null) {
			double lat = Math.toDegrees(MercatorCoordinates.myToLatRad(point.getY()));
			double lon = Math.toDegrees(MercatorCoordinates.mxToLonRad(point.getX()));
			xsw.writeAttribute("lat",String.valueOf(lat));
			xsw.writeAttribute("lon",String.valueOf(lon));
		}
		//object version only relevent if there is an initial version
		if(initialObject != null) {
			xsw.writeAttribute("version",initialObject.getOsmObjectVersion());
		}
		//change set
		xsw.writeAttribute("changeset",String.valueOf(changeSetId));
		
		//content
		//nodes, if applicable
		if(nodes != null) {
			for(Long nodeId:nodes) {
				xsw.writeEmptyElement("nd");
				xsw.writeAttribute("ref",String.valueOf(nodeId.longValue()));
			}
		}

		//memebers, if applicable
		if(members != null) {
			for(OsmRelationSrc.Member member:members) {
				if(member.type != null) {
					xsw.writeEmptyElement("member");
					xsw.writeAttribute("ref",String.valueOf(member.memberId));
					xsw.writeAttribute("type",String.valueOf(member.type));
					if(member.role != null) {
						xsw.writeAttribute("role",member.role);
					}
				}
			}
		}

		//tags, only for create or update
		if(finalObject != null) {
			List<PropertyPair> propertyPairs = finalObject.getProperties();
			for(PropertyPair prop:propertyPairs) {
				if((prop.key != null)&&(prop.value != null)) {
					xsw.writeEmptyElement("tag");
					xsw.writeAttribute("k",prop.key);
					xsw.writeAttribute("v",prop.value);
				}
			}
		}

//			xsw.writeEmptyElement("tag");
//			xsw.writeAttribute("k","created_by");
//			xsw.writeAttribute("v","Termite");

		//finish element
		xsw.writeEndElement();
	}

}
