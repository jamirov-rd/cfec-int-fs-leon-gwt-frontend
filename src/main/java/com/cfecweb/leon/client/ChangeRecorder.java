package com.cfecweb.leon.client;

import java.util.Date;
import java.util.List;

import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewChangesId;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.widget.form.TextField;

/*
 *	This class receives calls from various UI screens when a change and/or update occurs on particular fields. Those changes are recorded
 *	in a change table and used in reporting. 
 */

public class ChangeRecorder {
	ArenewChanges change = null;
	ArenewChangesId changeId = null;
	
	public void getAddressChanges(String field, String id, FieldEvent be, ArenewEntity entity, List<ArenewChanges> changeList) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Person");
		change.getId().setAttribute(field);
		if (!(be.getOldValue() == null)) {
			change.setOldvalue(be.getOldValue().toString());
		} else {
			change.setOldvalue("empty");
		}
		if (!(be.getValue() == null)) {
			change.setNewvalue(be.getValue().toString());
		} else {
			change.setNewvalue("empty");
		}			
		change.setObject(entity.getXname());
		changeList.add(change);			
		be.getBoxComponent().setStyleAttribute("color", "blue");
		//Log.info(entity.getId().getCfecid() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getBAddressChanges(String field, String id, String oldvalue, String newvalue, ArenewEntity entity, List<ArenewChanges> changeList) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Person");
		change.getId().setAttribute(field);
		if (!(oldvalue == null)) {
			change.setOldvalue(oldvalue);
		} else {
			change.setOldvalue("empty");
		}
		if (!(newvalue == null)) {
			change.setNewvalue(newvalue);
		} else {
			change.setNewvalue("empty");
		}			
		change.setObject(entity.getXname());
		changeList.add(change);			
		//Log.info(entity.getId().getCfecid() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getBillingChanges(String field, String id, FieldEvent be, ArenewEntity entity, List<ArenewChanges> changeList) {
		if (!(entity.getFirsttime().equalsIgnoreCase("true"))) {
			change = new ArenewChanges();
			changeId = new ArenewChangesId();
			change.setId(changeId);
			//change.setArenewEntity(entity);
			change.setType("Billing");
			change.getId().setAttribute(field);
			if (!(be.getOldValue() == null)) {
				change.setOldvalue(be.getOldValue().toString());
			} else {
				change.setOldvalue("empty");
			}
			if (!(be.getValue() == null)) {
				change.setNewvalue(be.getValue().toString());
			} else {
				change.setNewvalue("empty");
			}	
			change.setObject(entity.getXname());
			changeList.add(change);
			//Log.info(entity.getId().getCfecid() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
		}
	}
	
	public void getPermitChange1(String field, String id, List<ArenewChanges> changeList, TextField<String> padfg, 
			ArenewEntity entity, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Permits");
		change.getId().setAttribute(field);
		if (!(padfg.getOriginalValue() == null)) {
			change.setOldvalue(padfg.getOriginalValue().toString());
		} else {
			change.setOldvalue("empty");
		}
		if (!(padfg.getValue() == null)) {
			change.setNewvalue(padfg.getValue().toString());
		} else {
			change.setNewvalue("empty");
		}		
		padfg.setStyleAttribute("color", "blue");
		change.setObject(object);
		changeList.add(change);
		//Log.info(entity.getId().getCfecid() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getPermitChanges2(String field, String id, List<ArenewChanges> changeList, String oldv, String newv, 
			ArenewEntity entity, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Intent");
		change.getId().setAttribute(field);
		change.setOldvalue(oldv);
		change.setNewvalue(newv);
		change.setObject(object);
		changeList.add(change);		
		//Log.info(entity.getId().getCfecid() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getVesselChanges3(ArenewEntity entity, String field, String id, Date oldvalue, Date newvalue, List<ArenewChanges> changeList, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Vessels");
		change.getId().setAttribute(field);
		change.setOldvalue(oldvalue.toString());
		change.setNewvalue(newvalue.toString());
		change.setObject(object);
		changeList.add(change);
		//Log.info(id + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getVesselChanges5(ArenewEntity entity, String field, String id, String newreg, String oldreg, List<ArenewChanges> changeList, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Vessels");
		change.getId().setAttribute(field);
		change.setOldvalue(oldreg.toString());
		change.setNewvalue(newreg.toString());
		change.setObject(object);
		changeList.add(change);
		//Log.info(id + " has changed attribute " + field + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getVesselChanges1(ArenewEntity entity, String field, String id, FieldEvent be, List<ArenewChanges> changeList, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Vessels");
		change.getId().setAttribute(field);
		if (!(be.getOldValue() == null)) {
			change.setOldvalue(be.getOldValue().toString());
		} else {
			change.setOldvalue("empty");
		}
		if (!(be.getValue() == null)) {
			change.setNewvalue(be.getValue().toString());
		} else {
			change.setNewvalue("empty");
		}		
		change.setObject(object);
		changeList.add(change);
		//Log.info(id + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	public void getVesselChanges4(ArenewEntity entity, String field, String id, FieldEvent be, List<ArenewChanges> changeList, String object, String type) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Vessels");
		change.getId().setAttribute(field);
		//if (be.getBoxComponent().get)
		//if (!(be.getOldValue() == null)) {
		//	change.setOldvalue(be.getOldValue().toString());
		//} else {
		//	change.setOldvalue("empty");
		//}
		//if (!(be.getValue() == null)) {
		//	change.setNewvalue(be.getValue().toString());
		//} else {
		//	change.setNewvalue("empty");
		//}
		change.setOldvalue("empty");
		change.setNewvalue(type);
		change.setObject(object);
		changeList.add(change);
		//Log.info(id + " has changed attribute " + field + " ("+type+") from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}
	
	/*
	 * handles checkbox selections, which are basically just boolean
	 */
	public void getVesselChanges2(ArenewEntity entity, String field, String id, FieldEvent be, List<ArenewChanges> changeList, String object) {
		change = new ArenewChanges();
		changeId = new ArenewChangesId();
		change.setId(changeId);
		//change.setArenewEntity(entity);
		change.setType("Vessels");
		change.getId().setAttribute(field);
		change.setNewvalue(be.getField().getValue().toString());
		if (be.getField().getValue().toString().equalsIgnoreCase("false")) {
			change.setOldvalue("true");
		} else {
			change.setOldvalue("false");
		}
		change.setObject(object);
		changeList.add(change);
		//Log.info(id.toUpperCase() + " has changed attribute " + field + " from " + change.getOldvalue() + " to " + change.getNewvalue() + ", obect is " + change.getObject());
	}

}
