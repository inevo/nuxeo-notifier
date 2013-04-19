package org.nuxeo.ecm.notifier.service;

import java.util.List;

import org.nuxeo.ecm.notifier.adapter.UserNotificationAdapter;

public interface NotifierService {
	
	List<UserNotificationAdapter> getUnreadNotifications(String username);
	
	List<UserNotificationAdapter> getNotifications(String username, int limit);
	
	long countUnreadNotifications(String username);

	void markAllRead(String username);
	
	Boolean addNotification(String user, String originEvent, String name, String target, String object, String label);
	
	Boolean removeNotification(String id);

}
