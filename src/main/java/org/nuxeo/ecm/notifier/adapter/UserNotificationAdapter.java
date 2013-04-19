package org.nuxeo.ecm.notifier.adapter;

import java.util.Calendar;
import java.util.Date;

import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;


public class UserNotificationAdapter {
	
    public static final String SEPARATOR = ":";

    public static final String ACTIVITY_PREFIX = "activity" + SEPARATOR;
    
	protected DocumentModel doc;

    public UserNotificationAdapter(DocumentModel doc) {
        this.doc=doc;
    }

    public DocumentModel getDocumentModel() {
        return doc;
    }

    public String getId() {
    	return doc.getId();
    }
    
    public String getUsername() throws ClientException {
        return ActivityHelper.getUsername((String) doc.getPropertyValue("userNotification:user"));
    }

    public Calendar getCreated() throws ClientException {
        return (Calendar) doc.getPropertyValue("userNotification:created");
    }
    
    public String getTarget() throws ClientException {
    	return (String) doc.getPropertyValue("userNotification:target");
    }
    
    public String getOriginEvent() throws ClientException {
    	return (String) doc.getPropertyValue("userNotification:originEvent");
    }
    
    public String getName() throws ClientException {
    	return (String) doc.getPropertyValue("userNotification:name");
    }
 
    public String getLabel() throws ClientException {
    	return (String) doc.getPropertyValue("userNotification:label");
    }

    public String getObject() throws ClientException {
    	return (String) doc.getPropertyValue("userNotification:object");
    }
    
    public Calendar getViewed() throws ClientException {
        return (Calendar) doc.getPropertyValue("userNotification:viewed");
    }

    public String getTargetActivityId() throws ClientException {
    	return ActivityHelper.getActivityId(getTarget());
    }
 
    public String getObjectDocumentId() throws ClientException {
    	return ActivityHelper.getDocumentId(getObject());
    }
    
    public boolean markRead() throws PropertyException, ClientException {
    	if(getViewed() == null){
    		doc.setPropertyValue("userNotification:viewed", Calendar.getInstance());
    		return true;
    	}
    	return false;
    }
    
    public boolean isUnread() throws ClientException {
    	return getViewed() == null;
    }
    
    //TODO: missing all other types of object and target. For now it is only needed activity target and document Objects
    
}
