package intransix.osm.termite.map.data;

import intransix.osm.termite.net.NetRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
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

/**
 * This class manages on OSM commit request.
 * 
 * @author sutter
 */
public class CommitRequest extends DefaultHandler implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmData osmData;
	private OsmChangeSet changeSet;
	private String url;
	private List<UpdateInfo> updateList = new ArrayList<UpdateInfo>();
	
	//==========================
	// Public Methods
	//==========================
	
	/** Constructor. */
	public CommitRequest(OsmChangeSet changeSet, OsmData osmData) {
		this.changeSet = changeSet;
		this.osmData = osmData;
		
		String path = String.format(Locale.US,OsmModel.COMMIT_REQUEST_PATH,changeSet.getId());
		url = OsmModel.OSM_SERVER + path;
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
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(os);
		
		changeSet.writeChangeSet(xmlWriter);
		
		xmlWriter.flush();
		xmlWriter.close();
	}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(int responseCode, InputStream is) throws Exception {
		if(responseCode == 200) {
			//parse xml
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();	
			saxParser.parse(is,this);

			//post-parsing actions
			processUpdateList();
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
	
	/** This method updates the OsmData to reflect the new ID and version numbers
	 * after the commit. */
	private void processUpdateList() {
		//before we process the updates, we must flush the old command queue, since
		//it may contain old ids
		//alternatively, we could update the command queue ids
		osmData.clearCommandQueue();
		
		//create the maps to look up the src objects
		HashMap<Long,OsmNodeSrc> srcNodeMap = osmData.createNodeSrcMap();
		HashMap<Long,OsmWaySrc> srcWayMap = osmData.createWaySrcMap();
		HashMap<Long,OsmRelationSrc> srcRelationMap = osmData.createRelationSrcMap();
		
		OsmObject osmObject;
		OsmSrcData osmSrcObject;
		for(UpdateInfo info:updateList) {
	

			if(info.objectType.equals("node")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete - remove node src object, node already removed
					osmSrcObject = srcNodeMap.get(info.oldId);
					osmData.removeNodeSrc((OsmNodeSrc)osmSrcObject);
					srcNodeMap.remove(info.oldId);
					
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of node
					osmObject = osmData.getOsmNode(info.oldId);
					
					if(info.newId != info.oldId) {
						//create node src
						osmSrcObject = osmData.createNodeSrc((OsmNode)osmObject);
						//add to the map, at the new id
						srcNodeMap.put(info.newId,(OsmNodeSrc)osmSrcObject);
						//move the relation object
						osmData.nodeIdChanged(info.oldId,info.newId);
					}
					else {
						//get copy of node src
						osmSrcObject = srcNodeMap.get(info.oldId);
						//update source object
						osmSrcObject.copyFrom(osmObject);
					}	
				}
			}
			else if(info.objectType.equals("way")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete - remove way src, way already removed
					osmSrcObject = srcWayMap.get(info.oldId);
					osmData.removeWaySrc((OsmWaySrc)osmSrcObject);
					srcWayMap.remove(info.oldId);
					
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of way
					osmObject = osmData.getOsmWay(info.oldId);
					
					if(info.newId != info.oldId) {
						//create way src
						osmSrcObject = osmData.createWaySrc((OsmWay)osmObject);
						//add to the map, at the new id
						srcWayMap.put(info.newId,(OsmWaySrc)osmSrcObject);
						//move the way object
						osmData.wayIdChanged(info.oldId,info.newId);
					}
					else {
						//lookup copy of way
						osmSrcObject = srcWayMap.get(info.oldId);
						//update source object
						osmSrcObject.copyFrom(osmObject);
					}	
				}
			}
			else if(info.objectType.equals("relation")) {
				if(info.newId == OsmData.INVALID_ID) {
					//delete relation src, relation already deleted
					osmSrcObject = srcRelationMap.get(info.oldId);
					osmData.removeRelationSrc((OsmRelationSrc)osmSrcObject);
					srcRelationMap.remove(info.oldId);
					
					osmObject = null;
					osmSrcObject = null;
				}
				else {
					//get copy of relation
					osmObject = osmData.getOsmRelation(info.oldId);
					
					if(info.newId != info.oldId) {
						//create relation src
						osmSrcObject = osmData.createRelationSrc((OsmRelation)osmObject);
						//add to the map, at the new id
						srcRelationMap.put(info.newId,(OsmRelationSrc)osmSrcObject);
						//move the relation object
						osmData.relationIdChanged(info.oldId,info.newId);
					}
					else {
						//get copy of relation src
						osmSrcObject = srcRelationMap.get(info.oldId);
						//update source object
						osmSrcObject.copyFrom(osmObject);
					}	
				}	
			}
			else {
				continue;
			}

			
			if(osmObject != null) {
				//update version in source
				if(info.newVersion != null) {
					osmSrcObject.setOsmObjectVersion(info.newVersion);
				}
				
				if(info.newId != info.oldId) {
					osmObject.setId(info.newId);
					osmSrcObject.setId(info.newId);
				}
			}	
		}
		
		//notify of a change to the data
		//the data does change, but the main reason I do this is to notify the UI
		//that the undo/redo commands changed. That may be cludgey.
		int editNumber = osmData.getNextEditNumber();
		osmData.dataChanged(editNumber);
	}
	
	private class UpdateInfo {
		String objectType;
		long oldId;
		long newId;
		String newVersion;
	}

}
