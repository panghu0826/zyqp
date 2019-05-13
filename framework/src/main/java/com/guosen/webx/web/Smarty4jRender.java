package com.guosen.webx.web;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.Engine;
import org.lilystudio.smarty4j.Template;

public class Smarty4jRender {
    private Engine engine = new Engine();
    private static Smarty4jRender inst;

    public static Smarty4jRender getInst() {
        if (inst != null)
            return inst;

        synchronized (Smarty4jRender.class) {
            if (inst != null)
                return inst;

            inst = new Smarty4jRender();
        }

        return inst;
    }

    private Smarty4jRender() {
    }

    public void render(String tpl, Map<String, Object> model, OutputStream out) throws Exception {
        Template template = engine.getTemplate(tpl);
        Context context = new Context();
        context.putAll(model);
        template.merge(context, out);
        out.close();
    }

    public String render(String tpl, Map<String, Object> model) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        render(tpl, model, out);

        return new String(out.toByteArray(), engine.getEncoding());
    }

    public void config(Properties config) {
        engine.setDebug("true".equals(config.getProperty("isTemplateDebug")));
        String encoding = config.getProperty("templateEncoding");
        if (encoding != null)
            engine.setEncoding(encoding);
        else
            engine.setEncoding(HttpServerRequest.ENCODING);

        String templatePath = config.getProperty("templatePath");
        if (templatePath != null) {
            engine.setTemplatePath(templatePath);
        } else {
            String webrootPath = Smarty4jRender.class.getClassLoader().getResource(".").getPath() + config.getProperty("webroot");
            engine.setTemplatePath(webrootPath);
        }
    }
}
