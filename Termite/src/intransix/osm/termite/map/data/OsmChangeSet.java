package intransix.osm.termite.map.data;

import intransix.osm.termite.util.MercatorCoordinates;
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
	private List<ChangeObject> created = new ArrayList<ChangeObject>();
	private List<ChangeObject> updated = new ArrayList<ChangeObject>();
	private List<ChangeObject> deleted = new ArrayList<ChangeObject>();
	
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
		created.add(new ChangeObject(null,osmObject));
	}
	
	public void addUpdated(OsmSrcData osmSrcData, OsmObject osmObject) {
		updated.add(new ChangeObject(osmSrcData,osmObject));
	}
	
	public void addDeleted(OsmSrcData osmSrcData) {
		deleted.add(new ChangeObject(osmSrcData,null));
	}
	
	public boolean isEmpty() {
		return ((created.isEmpty())&&(updated.isEmpty())&&(deleted.isEmpty()));
	}
	
	public void writeChangeSet(XMLStreamWriter xsw) throws Exception {
		//sort the created objects so we don't use an object that is not yet creted.
		Collections.sort(created);
		
		xsw.writeStartDocument();
		
		xsw.writeStartElement("osmChange");
		xsw.writeAttribute("version","0.3");
		xsw.writeAttribute("generator","Termite");
		
		if(!created.isEmpty()) {
			xsw.writeStartElement("create");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			for(ChangeObject co:created) {
				co.writeEntry(xsw, id);
			}
			xsw.writeEndElement();
		}
		
		if(!updated.isEmpty()) {
			xsw.writeStartElement("modify");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			for(ChangeObject co:updated) {
				co.writeEntry(xsw, id);
			}
			xsw.writeEndElement();
		}
		
		if(!deleted.isEmpty()) {
			xsw.writeStartElement("delete");
			xsw.writeAttribute("version","0.3");
			xsw.writeAttribute("generator","Termite");
			xsw.writeAttribute("if-unused","*");
			for(ChangeObject co:deleted) {
				co.writeEntry(xsw, id);
			}
			xsw.writeEndElement();
		}
		
		xsw.writeEndElement();
		xsw.writeEndDocument();
	}
	
	private class ChangeObject implements Comparable<ChangeObject> {
		private OsmSrcData osmSrcObject;
		private OsmObject osmObject;
		
		public ChangeObject(OsmSrcData osmSrcObject, OsmObject osmObject) {
			this.osmSrcObject = osmSrcObject;
			this.osmObject = osmObject;
		}
		
		/** This is used to order objects during creation, so it doesn't try to 
		 * use an object that has not been created yet. */
		public int compareTo(ChangeObject changeObject) {
			//only rearrange if there is an osm object (don't both with deletes)
			if((osmObject == null)||(changeObject.osmObject == null)) return 0;
			
			if(osmObject instanceof OsmNode) {
				if(changeObject.osmObject instanceof OsmNode) return 0;
				else return -1;
			}
			else if(osmObject instanceof OsmWay) {
				if(changeObject.osmObject instanceof OsmNode) return 1;
				else if(changeObject.osmObject instanceof OsmWay) return 0;
				else return -1;
			}
			else if(osmObject instanceof OsmRelation) {
				if(changeObject.osmObject instanceof OsmRelation) {
//I should check if the relation contains a reference to a relation that
//is also being created. If so update the order. If this can not be done
//create an update instruction for it?
					return 0;
				}
				else {
					return 1;
				}
			}
			else {
				//this shouldn't happen
				return 0;
			}
			
		}
		
		public void writeEntry(XMLStreamWriter xsw, long changeSetId) throws Exception {
			
			long id;
			String osmObjectVersion;
			
			List<OsmNode> nodes = null;
			List<OsmMember> members = null;
			Point2D point = null;
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
					point = ((OsmNode)osmObject).getPoint();
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
					point = ((OsmNodeSrc)osmSrcObject).getPoint();
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
			if(point != null) {
				double lat = Math.toDegrees(MercatorCoordinates.myToLatRad(point.getY()));
				double lon = Math.toDegrees(MercatorCoordinates.mxToLonRad(point.getX()));
				xsw.writeAttribute("lat",String.valueOf(lat));
				xsw.writeAttribute("lon",String.valueOf(lon));
			}
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
			
//			xsw.writeEmptyElement("tag");
//			xsw.writeAttribute("k","created_by");
//			xsw.writeAttribute("v","Termite");
			
			//finish element
			xsw.writeEndElement();
		}
	}
	
}
