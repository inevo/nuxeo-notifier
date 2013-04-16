package org.nexuo.ecm.notifier.service;

import java.util.List;

import org.nexuo.ecm.notifier.adapter.UserNotificationAdapter;

public interface NotifierService {
	
	List<UserNotificationAdapter> getUnreadNotifications(String username);

	void markAllRead(String username);
	
	Boolean addNotification(String user, String originEvent, String name, String target, String object);
	
	Boolean removeNotification(String id);

}
