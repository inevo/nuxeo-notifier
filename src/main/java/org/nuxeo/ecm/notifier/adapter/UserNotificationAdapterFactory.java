package org.nuxeo.ecm.notifier.adapter;

import org.nuxeo.ecm.notifier.adapter.UserNotificationAdapter;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.adapter.DocumentAdapterFactory;

public class UserNotificationAdapterFactory  implements DocumentAdapterFactory {

    @Override
    public Object getAdapter(DocumentModel doc, Class itf) {
        return new UserNotificationAdapter(doc);
    }

}
