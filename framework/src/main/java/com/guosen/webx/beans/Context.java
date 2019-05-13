package com.guosen.webx.beans;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericGroovyApplicationContext;

import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;

public class Context implements GroovyObject {
    private Logger log = LoggerFactory.getLogger(Context.class);

    private MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(getClass());

    private static Object lock = new Object();

    private static Context inst;

    public static Context getInst() {
        if (inst != null)
            return inst;

        synchronized (lock) {
            if (inst != null)
                return inst;

            inst = new Context();
        }
        return inst;
    }

    private final String configFile = "classpath*:config/comp-*.groovy";
    private GenericGroovyApplicationContext context;

    private Context() {
        init();
    }

    private void init() {
        context = new GenericGroovyApplicationContext();
        context.setClassLoader(ClassLoaderHolder.gcl);

        try {
            context.load(configFile);
            context.refresh();

            log.info("load context ok, size - " + context.getBeanDefinitionCount());
        } catch (Exception e) {
            log.error("load context error", e);
        }
    }

    public void reload() {
        close();
        init();
    }

    public void close() {
        if (context != null) {
            context.close();
            log.info("context closed");
        }
//      ClassLoaderHolder.gcl.clearCache();
        log.info("class loader class cache is cleared");
    }

    // for short
    public DataSource getDs() {
        return (DataSource) context.getBean("defaultDs");
    }

    // Implementation of the GroovyObject interface
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public MetaClass getMetaClass() {
        return this.metaClass;
    }

    public Object invokeMethod(String name, Object args) {
        return this.metaClass.invokeMethod(this, name, args);
    }

    public void setProperty(String property, Object newValue) {
        throw new UnsupportedOperationException("set property not supported!");
    }

    public Object getProperty(String property) {
        if (context != null && context.containsBean(property)) {
            return context.getBean(property);
        }

        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object bean(Class clazz) {
        return context.getBean(clazz);
    }
}
