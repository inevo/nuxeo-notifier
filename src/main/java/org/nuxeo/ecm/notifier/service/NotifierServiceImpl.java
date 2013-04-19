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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.notifier.service.NotifierService;
import org.nuxeo.ecm.notifier.service.NotifierServiceImpl;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.notifier.adapter.UserNotificationAdapter;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

public class NotifierServiceImpl extends DefaultComponent implements
		NotifierService {

	private static final Log log = LogFactory.getLog(NotifierServiceImpl.class);
	protected static final String NOTIFIER_DIRECTORY_NAME = "userNotification";
			
	@Override
	public List<UserNotificationAdapter> getUnreadNotifications(String username) {
		String user_ref = ActivityHelper.createUserActivityObject(username);
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		filterMap.put(NOTIFICATION_FIELD_USER, user_ref);
		filterMap.put(NOTIFICATION_FIELD_VIEWED, null);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for(DocumentModel doc : docs){
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}
	
	
	
	@Override
	public List<UserNotificationAdapter> getNotifications(String username, int limit) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		String user_ref = ActivityHelper.createUserActivityObject(username);
		filterMap.put(NOTIFICATION_FIELD_USER, user_ref);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for(DocumentModel doc : docs){
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}
	@Override
	public long countUnreadNotifications(String username) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		String user_ref = ActivityHelper.createUserActivityObject(username);
		filterMap.put(NOTIFICATION_FIELD_USER, user_ref);
		filterMap.put(NOTIFICATION_FIELD_VIEWED, null);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		
		return docs.totalSize();
	}

	public List<UserNotificationAdapter> getUserNotifications(String username) {
		Map<String, Serializable> filterMap = new HashMap<String, Serializable>();
		String user_ref = ActivityHelper.createUserActivityObject(username);
		filterMap.put(NOTIFICATION_FIELD_USER, user_ref);
		DocumentModelList docs = queryNotifierDirectory(filterMap);
		List<UserNotificationAdapter> notifications = new ArrayList<UserNotificationAdapter>();
		for(DocumentModel doc : docs){
			notifications.add(doc.getAdapter(UserNotificationAdapter.class));
		}
		return notifications;
	}
	
	@Override
	public void markAllRead(String username) {
		List<UserNotificationAdapter> unreadNotifications = getUnreadNotifications(username);
		DirectoryService directoryService = Framework
				.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService
					.open(NOTIFIER_DIRECTORY_NAME);
			for (UserNotificationAdapter notification : unreadNotifications) {
					notification.markRead();
					notifierDirectory.updateEntry(notification.getDocumentModel());
				
	        }
		} catch (DirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to create a new notification",
					e);
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
		DirectoryService directoryService = Framework
				.getLocalService(DirectoryService.class);
		Session notifierDirectory = null;
		try {
			notifierDirectory = directoryService
					.open(NOTIFIER_DIRECTORY_NAME);
			// try to get an existing entry
			Map<String, Serializable> notification = new HashMap<String, Serializable>();
			notification.put(NOTIFICATION_FIELD_USER, user);
			notification.put(NOTIFICATION_FIELD_NAME, name);
			notification.put(NOTIFICATION_FIELD_TARGET, target);
			notification.put(NOTIFICATION_FIELD_LABEL, label);
			notification.put(NOTIFICATION_FIELD_OBJECT, object);
			notification.put(NOTIFICATION_FIELD_ORIGIN_EVENT, originEvent);
			notification.put(NOTIFICATION_FIELD_CREATED, Calendar.getInstance());

			DocumentModelList notifications = notifierDirectory
					.query(notification);
			if (notifications.isEmpty()) {
				notifierDirectory.createEntry(new HashMap<String, Object>(
						notification));
				return true;
			} else {
				return false;
			}
		} catch (ClientException e) {
			throw new ClientRuntimeException("Unable to create a new notification",
					e);
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
	public Boolean removeNotification(String id) {
	       DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
	        Session notifierDirectory = null;
	        try {
	            notifierDirectory = directoryService.open(NOTIFIER_DIRECTORY_NAME);

	            Map<String, Serializable> filter = new HashMap<String, Serializable>();
	            filter.put(NOTIFICATION_FIELD_ID, id);

	            DocumentModelList notifications = notifierDirectory.query(filter,
	                    filter.keySet());
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
	            throw new ClientRuntimeException("Unable to remove the notification",
	                    e);
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
        	if(limit > 0){
                return notifierDirectory.query(filter, null, getRelationshipsOrderBy(), false, limit, 0);
        	} else {
                return notifierDirectory.query(filter, null,
                        getRelationshipsOrderBy());        		
        	}

        } catch (ClientException e) {
            throw new ClientRuntimeException(
                    "Unable to query through notifier directory", e);
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


}
