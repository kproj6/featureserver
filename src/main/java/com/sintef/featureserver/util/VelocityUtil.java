package com.sintef.featureserver.util;

import java.io.StringWriter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Utility functions relating to Velocity Template engine.
 */
public final class VelocityUtil {

    private static final String UTF_8 = "UTF-8";

    private VelocityUtil() {} // Should not be instantiated.

    public static Template loadTemplate(
            final VelocityEngine velocityEngine,
            final String templateName) {
        return velocityEngine.getTemplate(templateName, UTF_8);
    }

    public static String renderTemplate(final Template template, final VelocityContext context) {
        final StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

}
