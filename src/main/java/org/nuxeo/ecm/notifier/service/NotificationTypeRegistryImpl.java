package org.nuxeo.ecm.notifier.service;

import java.util.ArrayList;
import java.util.List;

public class NotificationTypeRegistryImpl implements NotificationTypeRegistry {
	private final List<NotificationType> notificationTypeList = new ArrayList<NotificationType>();

	@Override
	public void clear() {
		notificationTypeList.clear();
	}

	@Override
	public void registerNotificationType(NotificationType notifType) {
		NotificationTypeImpl notification = new NotificationTypeImpl(
				notifType.getName(), notifType.getLabel(),
				notifType.getTemplate(), notifType.getSummaryTemplate());
		int pos = notificationTypeList.indexOf(notification);
		if (pos >= 0) {
			unregisterNotificationType(notifType);
		}
		notificationTypeList.add(notification);

	}

	@Override
	public void unregisterNotificationType(NotificationType notifType) {
		NotificationTypeImpl notification = new NotificationTypeImpl(
				notifType.getName(), notifType.getLabel(),
				notifType.getTemplate(), notifType.getSummaryTemplate());
		notificationTypeList.remove(notification);

	}

	@Override
	public List<NotificationType> getNotificationTypes() {
		return notificationTypeList;
	}

	@Override
	public NotificationType getNotificationTypeByName(String name) {
		NotificationTypeImpl notification = new NotificationTypeImpl(name, "",
				"", "");
		return notificationTypeList.get(notificationTypeList
				.indexOf(notification));
	}

}