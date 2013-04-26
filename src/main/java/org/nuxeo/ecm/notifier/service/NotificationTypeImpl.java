package org.nuxeo.ecm.notifier.service;

public class NotificationTypeImpl implements NotificationType {

	protected String label;
	protected String name;
	protected String template;
	protected String summaryTemplate;

	public NotificationTypeImpl(String name, String label, String template,
			String summaryTemplate) {
		this.label = label;
		this.name = name;
		this.template = template;
		this.summaryTemplate = summaryTemplate;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public String getSummaryTemplate() {
		return summaryTemplate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof NotificationTypeImpl) {
			NotificationTypeImpl other = (NotificationTypeImpl) obj;
			return name.equals(other.getName());
		}
		return false;
	}

}
