package org.nuxeo.ecm.notifier.rendering;

import java.io.StringWriter;
import java.io.Writer;

import org.nuxeo.ecm.platform.ec.notification.email.HtmlEscapeMethod;
import org.nuxeo.ecm.platform.rendering.RenderingContext;
import org.nuxeo.ecm.platform.rendering.RenderingResult;
import org.nuxeo.ecm.platform.rendering.impl.DefaultRenderingResult;
import org.nuxeo.ecm.platform.rendering.template.DocumentRenderingEngine;
import org.nuxeo.ecm.platform.rendering.template.FreemarkerRenderingJob;

import freemarker.template.Configuration;

public class UserNotificationRenderingEngine extends DocumentRenderingEngine {

	private final String template;

	public UserNotificationRenderingEngine(String template) {
		this.template = template;
	}

	@Override
	public Configuration createConfiguration() throws Exception {
		Configuration cfg = super.createConfiguration();
		cfg.setSharedVariable("htmlEscape", new HtmlEscapeMethod());
		return cfg;
	}

	@Override
	protected FreemarkerRenderingJob createJob(RenderingContext ctx) {
		return new UserNotifsRenderingJob("ftl");
	}

	public String getFormatName() {
		// TODO Auto-generated method stub
		return null;
	}

	class UserNotifsRenderingJob extends DefaultRenderingResult implements
			FreemarkerRenderingJob {

		private static final long serialVersionUID = -713306284174259967L;

		final Writer strWriter = new StringWriter();

		UserNotifsRenderingJob(String formatName) {
			super(formatName);
		}

		@Override
		public Object getOutcome() {
			return strWriter.toString();
		}

		public RenderingResult getResult() {
			return this;
		}

		public String getTemplate() {
			return template;
		}

		public Writer getWriter() {
			return strWriter;
		}
	}

}
