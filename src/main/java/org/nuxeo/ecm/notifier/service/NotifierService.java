package org.nuxeo.ecm.notifier.service;

import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.notifier.adapter.UserNotificationAdapter;

public interface NotifierService {

	List<UserNotificationAdapter> getUnreadNotifications(String username);

	List<UserNotificationAdapter> getNotifications(String username, int limit);

	long countUnreadNotifications(String username);

	void markAllRead(String username);

	Boolean addNotification(String user, String originEvent, String name,
			String target, String object, String label);

	UserNotificationAdapter getNotification(long id);

	Boolean removeNotification(long id);

	String renderNotification(UserNotificationAdapter userNotification,
			CoreSession session, Map<String, Object> parameters) throws ClientException;

	String renderNotificationSummary(UserNotificationAdapter userNotification,
			CoreSession session, Map<String, Object> parameters) throws ClientException;
}
