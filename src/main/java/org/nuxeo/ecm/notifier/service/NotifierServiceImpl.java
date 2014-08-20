package org.nuxeo.ecm.notifier.service;

import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_CREATED;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_ID;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_NAME;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_OBJECT;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_ORIGIN_EVENT;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_TARGET;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_LABEL;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_USER;
import static org.nuxeo.ecm.notifier.NotifierConstants.NOTIFICATION_FIELD_VIEWED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamService;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.notifier.adapter.UserNotificationAdapter;
import org.nuxeo.ecm.notifier.rendering.UserNotificationRenderingEngine;
import org.nuxeo.ecm.platform.rendering.RenderingEngine;
import org.nuxeo.ecm.platform.rendering.RenderingResult;
import org.nuxeo.ecm.platform.rendering.RenderingService;
import org.nuxeo.ecm.platform.rendering.impl.DocumentRenderingContext;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.user.center.profile.UserProfileService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;

public class NotifierServiceImpl extends DefaultComponent implements NotifierService {

	private static final Log log = LogFactory.getLog(NotifierServiceImpl.class);
	protected static final String NOTIFIER_DIRECTORY_NAME = "userNotification";
	protected static final String NOTIFICATION_TYPES_EP = "notificationTypes";
	protected NotificationTypeRegistry notificationTypeRegistry;

	@Override
	public List<UserNotificationAdapter> getUnreadNotifications(String username) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		filterMap.put(NOTIFICATION_FIELD_USER, username);
		filterMap.put(NOTIFICATION_FIELD_VIEWED, null);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for (DocumentModel doc : docs) {
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}

	@Override
	public List<UserNotificationAdapter> getNotifications(String username, int limit) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		filterMap.put(NOTIFICATION_FIELD_USER, username);
		DocumentModelList docs = queryNotifierDirectory(filterMap, limit);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for (DocumentModel doc : docs) {
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}

	@Override
	public long countUnreadNotifications(String username) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		filterMap.put(NOTIFICATION_FIELD_USER, username);
		filterMap.put(NOTIFICATION_FIELD_VIEWED, null);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		return docs.totalSize();
	}

	public List<UserNotificationAdapter> getUserNotifications(String username) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		filterMap.put(NOTIFICATION_FIELD_USER, username);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for (DocumentModel doc : docs) {
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}

	@Override
	public void markAllRead(String username) {
		List<UserNotificationAdapter> unreadNotifications = getUnreadNotifications(username);
		DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);
			for (UserNotificationAdapter notification : unreadNotifications) {
				notification.markRead();
				notifierDirectory.updateEntry(notification.getDocumentModel());
			}
		} catch (DirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to create a new notification", e);
		} finally {
			if (notifierDirectory != null) {
				try {
					notifierDirectory.close();
				} catch (DirectoryException e) {
					log.error("Error while trying to close notifier directory");
					log.debug("Exception occurred", e);
				}
			}
		}
	}

	@Override
	public Boolean addNotification(String user, String originEvent,
			String name, String target, String object, String label) {
		DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);
			// try to get an existing entry
			Map<String, Serializable> notification = new HashMap<String, Serializable>();
			notification.put(NOTIFICATION_FIELD_USER, user);
			notification.put(NOTIFICATION_FIELD_NAME, name);
			notification.put(NOTIFICATION_FIELD_TARGET, target);
			notification.put(NOTIFICATION_FIELD_LABEL, label);
			notification.put(NOTIFICATION_FIELD_OBJECT, object);
			notification.put(NOTIFICATION_FIELD_ORIGIN_EVENT, originEvent);
			notification.put(NOTIFICATION_FIELD_CREATED, Calendar.getInstance());

			DocumentModelList notifications = notifierDirectory.query(notification);
			if (notifications.isEmpty()) {
				notifierDirectory.createEntry(new HashMap<String, Object>(notification));
				return true;
			} else {
				return false;
			}
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to create a new notification", e);
		} finally {
			if (notifierDirectory != null) {
				try {
					notifierDirectory.close();
				} catch (DirectoryException e) {
					log.error("Error while trying to close notifier directory");
					log.debug("Exception occurred", e);
				}
			}
		}
	}

	@Override
	public Boolean removeNotification(long id) {
		DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);

			Map<String, Serializable> filter = new HashMap<String, Serializable>();
			filter.put(NOTIFICATION_FIELD_ID, id);

			DocumentModelList notifications = notifierDirectory.query(filter, filter.keySet());
			if (notifications.isEmpty()) {
				log.warn("Trying to delete a relationship that doesn't exists");
				return false;
			} else {
				for (DocumentModel notification : notifications) {
					notifierDirectory.deleteEntry(notification.getId());
				}
				return true;
			}
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to remove the notification", e);
		} finally {
			if (notifierDirectory != null) {
				try {
					notifierDirectory.close();
				} catch (DirectoryException e) {
					log.error("Error while trying to close notifier directory");
					log.debug("Exception occurred", e);
				}
			}
		}
	}

	@Override
	public UserNotificationAdapter getNotification(long id) {
		DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);

			Map<String, Serializable> filter = new HashMap<String, Serializable>();
			filter.put(NOTIFICATION_FIELD_ID, id);

			DocumentModelList notifications = queryNotifierDirectory(filter);
			if (notifications.isEmpty()) {
				log.warn("Trying to delete a relationship that doesn't exists");
				return null;
			} else {
				return notifications.get(0).getAdapter(UserNotificationAdapter.class);
			}
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to remove the notification", e);
		} finally {
			if (notifierDirectory != null) {
				try {
					notifierDirectory.close();
				} catch (DirectoryException e) {
					log.error("Error while trying to close notifier directory");
					log.debug("Exception occurred", e);
				}
			}
		}
	}

	protected DocumentModelList queryNotifierDirectory(Map<String, Serializable> filter) {
		return queryNotifierDirectory(filter, 0);
	}

	protected DocumentModelList queryNotifierDirectory(Map<String, Serializable> filter, int limit) {
		DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);
			if (limit > 0) {
				return notifierDirectory.query(filter, null, getRelationshipsOrderBy(), false, limit, 0);
			} else {
				return notifierDirectory.query(filter, null, getRelationshipsOrderBy());
			}

		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to query through notifier directory", e);
		} finally {
			if (notifierDirectory != null) {
				try {
					notifierDirectory.close();
				} catch (DirectoryException e) {
					log.error("Error while trying to close notifier directory");
					log.debug("Exception occurred", e);
				}
			}
		}
	}

	protected static Map<String, String> getRelationshipsOrderBy() {
		Map<String, String> order = new HashMap<String, String>();
		order.put(NOTIFICATION_FIELD_CREATED, "desc");
		return order;
	}

	@Override
	public void activate(ComponentContext context) throws Exception {
		notificationTypeRegistry = new NotificationTypeRegistryImpl();
	}

	@Override
	public void deactivate(ComponentContext context) throws Exception {
		notificationTypeRegistry.clear();
		notificationTypeRegistry = null;
	}

	@Override
	public void registerExtension(Extension extension) throws Exception {
		log.info("Registering notification extension");
		String xp = extension.getExtensionPoint();
		if (NOTIFICATION_TYPES_EP.equals(xp)) {
			Object[] contribs = extension.getContributions();
			for (Object contrib : contribs) {
				try {
					NotificationTypeDescriptor notifDesc = (NotificationTypeDescriptor) contrib;
					notificationTypeRegistry.registerNotificationType(notifDesc);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}

	@Override
	public void unregisterExtension(Extension extension) throws Exception {
		String xp = extension.getExtensionPoint();
		if (NOTIFICATION_TYPES_EP.equals(xp)) {
			Object[] contribs = extension.getContributions();
			for (Object contrib : contribs) {
				try {
					NotificationTypeDescriptor notifDesc = (NotificationTypeDescriptor) contrib;
					notificationTypeRegistry.unregisterNotificationType(notifDesc);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}

	public NotificationTypeRegistry getNotificationRegistry() {
		return notificationTypeRegistry;
	}

	@Override
	public String renderNotification(UserNotificationAdapter userNotification,
			CoreSession session) throws ClientException {
		NotificationType notificationType = notificationTypeRegistry
                .getNotificationTypeByName(userNotification.getName());
		return renderTemplate(notificationType.getTemplate(), userNotification,	session);
	}

	@Override
	public String renderNotificationSummary(
			UserNotificationAdapter userNotification, CoreSession session)
			throws ClientException {
		NotificationType notificationType = notificationTypeRegistry.getNotificationTypeByName(userNotification.getName());
		return renderTemplate(notificationType.getSummaryTemplate(), userNotification, session);
	}

	protected String renderTemplate(String template,
			UserNotificationAdapter userNotification, CoreSession session)
			throws ClientException {
		String notificationRender = "<P>User Notification without template.</P>";
		DocumentRenderingContext context = new DocumentRenderingContext();

		context.remove("doc");

		context.put("notification", userNotification.getDocumentModel());
		context.put("isUnread", userNotification.isUnread());
		// set user documents
		context.put("user",	getUserData(userNotification.getUsername(), session));
		// set Target documents
		context.putAll(setContextDocuments("target", userNotification.getTarget(), session));
		context.putAll(setContextDocuments("object", userNotification.getObject(), session));

		// render
		try {
			RenderingService rs = Framework.getService(RenderingService.class);
			rs.registerEngine((RenderingEngine) new UserNotificationRenderingEngine(template));
			Collection<RenderingResult> results = rs.process(context);

			for (RenderingResult result : results) {
				notificationRender = (String) result.getOutcome();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		context.put("Runtime", Framework.getRuntime());
		return notificationRender;
	}

	protected Map<String, Object> getUserData(String username,
			CoreSession coreSession) {
		Map<String, Object> user = new HashMap<String, Object>();
		UserProfileService userProfileService;
		UserManager userManager;
		try {
			userProfileService = Framework.getService(UserProfileService.class);
			DocumentModel userProfile = userProfileService.getUserProfileDocument(username, coreSession);
			user.put("userProfile", userProfile);

			userManager = Framework.getService(UserManager.class);
			user.put("userModel", userManager.getUserModel(username));

			String contextPath = Framework.getProperty("org.nuxeo.ecm.contextPath");
			Blob avatar;
			String avatarUri = contextPath + "/site/skin/nuxeo/icons/default_avatar.png";
			try {
				avatar = (Blob) userProfile.getPropertyValue("userprofile:avatar");
			} catch (ClientException e) {
				log.debug("No avatar found");
				avatar = null;
			}

			if (userProfile != null && avatar != null) {
				// TODO : Use a relative path (be careful of proxy stuff)
				String uriPattern = "%s/nxfile/%s/%s/userprofile:avatar/";
				String repositoryName = coreSession.getRepositoryName();
				avatarUri = String.format(uriPattern, contextPath, repositoryName, userProfile.getId());
			}
			user.put("avatarUri", avatarUri);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	protected Map<String, Object> setContextDocuments(String name,
			String objectId, CoreSession coreSession) throws ClientException {
		Map<String, Object> data = new HashMap<String, Object>();
		if (ActivityHelper.isActivity(objectId)) {
			Map<String, Object> activityData = new HashMap<String, Object>();
			ActivityStreamService activityStreamService = Framework.getLocalService(ActivityStreamService.class);
			Activity activity = activityStreamService.getActivity(Long.valueOf(ActivityHelper.getActivityId(objectId)));
			activity.getActor();
			activityData.put("activity_id",	ActivityHelper.getActivityId(objectId));
			activityData.put("actor", getUserData(ActivityHelper.getUsername(activity.getActor()), coreSession));
			// activity context
			activityData.putAll(setContextDocuments("context", activity.getContext(), coreSession));
			// activity object
			activityData.putAll(setContextDocuments("object", activity.getObject(), coreSession));
			// activity target
			activityData.putAll(setContextDocuments("target", activity.getTarget(), coreSession));

			data.put(name, activityData);
		} else if (ActivityHelper.isDocument(objectId)) {
			data.put(name, coreSession.getDocument((DocumentRef) new IdRef(ActivityHelper.getDocumentId(objectId))));
		} else if (ActivityHelper.isUser(objectId)) {
			data.put(name, getUserData(ActivityHelper.getUsername(objectId), coreSession));
		} else {
			data.put(name, objectId);
		}
		return data;
	}

}
