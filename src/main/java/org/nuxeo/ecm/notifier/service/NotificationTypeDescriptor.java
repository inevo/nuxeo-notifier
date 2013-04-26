package org.nuxeo.ecm.notifier.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("notificationType")
public class NotificationTypeDescriptor implements NotificationType {

	private static final long serialVersionUID = -5974825427883204458L;

	@XNode("@name")
	protected String name;

	@XNode("@label")
	protected String label; // used for i10n

	@XNode("@template")
	protected String template;

	@XNode("@summaryTemplate")
	protected String summaryTemplate;

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

}