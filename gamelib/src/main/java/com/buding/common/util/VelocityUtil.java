package com.buding.common.util;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityUtil {	
	public static String merge(String content, Map<String, String> params) {
		 VelocityEngine ve = new VelocityEngine();
		 VelocityContext context = new VelocityContext(params);
		 StringWriter writer = new StringWriter();
		 ve.evaluate(context, writer, "Merge", content);
		 return writer.toString();
	}
}
