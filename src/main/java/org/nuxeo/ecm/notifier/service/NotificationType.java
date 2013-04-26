package org.nuxeo.ecm.notifier.service;

public interface NotificationType {

	String getLabel();

	String getName();

	String getTemplate();

	String getSummaryTemplate();
}
