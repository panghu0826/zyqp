package com.guosen.webx.beans;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class GroovyCompNsSupport extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("component-groovy", new ComponentGroovyScanParser());
    }
}