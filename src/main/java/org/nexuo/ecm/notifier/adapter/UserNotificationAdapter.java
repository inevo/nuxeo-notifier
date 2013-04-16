package org.nexuo.ecm.notifier.adapter;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UserNotificationAdapter {
	
	protected DocumentModel doc;

    public UserNotificationAdapter(DocumentModel doc) {
        this.doc=doc;
    }

    public DocumentModel getDocumentModel() {
        return doc;
    }

    public String getUsername() throws ClientException {
         try {
            return (String) doc.getPropertyValue("userNotification:username");
         }
         catch (ClientException e) {
             throw e;
         }
    }
    
   
}
