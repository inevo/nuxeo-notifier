package org.nuxeo.ecm.notifier.service;

import java.io.Serializable;
import java.util.List;

public interface NotificationTypeRegistry extends Serializable {

	void clear();

	void registerNotificationType(NotificationType notifType);

	void unregisterNotificationType(NotificationType notifType);

	List<NotificationType> getNotificationTypes();

	NotificationType getNotificationTypeByName(String name);

}
