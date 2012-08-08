package intransix.osm.termite.map.data;

import java.util.*;
import javax.xml.stream.XMLStreamWriter;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class OsmChangeSet {
	
	private long id;
	private String message;
	private List<OsmObject> created = new ArrayList<OsmObject>();
	private List<OsmObject> updated = new ArrayList<OsmObject>();
	private List<OsmSrcData> deleted = new ArrayList<OsmSrcData>();
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void addCreated(OsmObject osmObject) {
		created.add(osmObject);
	}
	
	public void addUpdated(OsmSrcData osmSrcData, OsmObject osmObject) {
		updated.add(osmObject);
	}
	
	public void addDeleted(OsmSrcData osmSrcData) {
		deleted.add(osmSrcData);
	}
	
	public boolean isEmpty() {
		return ((created.isEmpty())&&(updated.isEmpty())&&(deleted.isEmpty()));
	}
	
	private class ChangeObject {
		private OsmSrcData osmSrcObject;
		private OsmObject osmObject;
		
		public ChangeObject(OsmSrcData osmSrcObject, OsmObject osmObject) {
			this.osmSrcObject = osmSrcObject;
			this.osmObject = osmObject;
		}
		
		public void writeEntry(XMLStreamWriter xsw, long changeSetId) throws Exception {
			
			long id;
			String osmObjectVersion;
			
			List<OsmNode> nodes = null;
			List<OsmMember> members = null;
			boolean isDelete;
			boolean isCreate;
			
			//create start element
			if(osmObject != null) {
				
				id = osmObject.getId();
				
				isDelete = false;
				if(osmSrcObject != null) {
					isCreate = false;
					osmObjectVersion = osmSrcObject.getOsmObjectVersion();
				}
				else {
					isCreate = true;
					osmObjectVersion = null;
				}
				
				if(osmObject instanceof OsmNode) {
					xsw.writeStartElement("node");
					Point2D point = ((OsmNode)osmObject).getPoint();
					xsw.writeAttribute("lat",String.valueOf(point.getY()));
					xsw.writeAttribute("lon",String.valueOf(point.getX()));
				}
				else if(osmObject instanceof OsmWay) {
					xsw.writeStartElement("way");
					nodes = ((OsmWay)osmObject).getNodes();
				}
				else if(osmObject instanceof OsmRelation) {
					xsw.writeStartElement("relation");
					members = ((OsmRelation)osmObject).getMembers();
				}
			}
			else if(osmSrcObject != null) {
				
				id = osmSrcObject.getId();
				osmObjectVersion = osmSrcObject.getOsmObjectVersion();
				
				isDelete = true;
				isCreate = false;
				if(osmSrcObject instanceof OsmNodeSrc) {
					xsw.writeStartElement("node");
					Point2D point = ((OsmNodeSrc)osmSrcObject).getPoint();
					xsw.writeAttribute("lat",String.valueOf(point.getY()));
					xsw.writeAttribute("lon",String.valueOf(point.getX()));
				}
				else if(osmSrcObject instanceof OsmWaySrc) {
					xsw.writeStartElement("way");
				}
				else if(osmSrcObject instanceof OsmRelationSrc) {
					xsw.writeStartElement("relation");
				}
			}
			else {
				//no object to write
				return;
			}
			
			//add standard attributes
			xsw.writeAttribute("id",String.valueOf(id));
			if(osmObjectVersion != null) {
				xsw.writeAttribute("version",osmObjectVersion);
			}
			xsw.writeAttribute("changeset",String.valueOf(changeSetId));
			
			//nodes, if applicable
			if(nodes != null) {
				for(OsmNode node:nodes) {
					xsw.writeEmptyElement("nd");
					xsw.writeAttribute("ref",String.valueOf(node.getId()));
				}
			}
			
			//memebers, if applicable
			if(members != null) {
				for(OsmMember member:members) {
					if(member.osmObject != null) {
						xsw.writeEmptyElement("member");
						xsw.writeAttribute("ref",String.valueOf(member.osmObject.getId()));
						xsw.writeAttribute("type",String.valueOf(member.osmObject.getObjectType()));
						if(member.role != null) {
							xsw.writeAttribute("role",member.role);
						}
					}
				}
			}
			
			//tags, only for create or update
			if(osmObject != null) {
				for(String key:osmObject.getPropertyKeys()) {
					String value = osmObject.getProperty(key);
					if(value != null) {
						xsw.writeEmptyElement("tag");
						xsw.writeAttribute("k",key);
						xsw.writeAttribute("v",value);
					}
				}
			}
			
			//finish element
			xsw.writeEndElement();
		}
	}
	
}
