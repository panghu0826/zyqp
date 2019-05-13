package com.guosen.webx.beans;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

public class ComponentGroovyScanParser extends ComponentScanBeanDefinitionParser {

    @Override
    protected ClassPathBeanDefinitionScanner configureScanner(ParserContext parserContext, Element element) {
        ClassPathBeanDefinitionScanner scanner = createScanner(parserContext.getReaderContext(), true);
        scanner.setEnvironment(parserContext.getReaderContext().getEnvironment());
        scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
        scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));

        scanner.setResourceLoader(new PathMatchingResourcePatternResolver(ClassLoaderHolder.gcl));
        scanner.setResourcePattern("**/*.groovy");
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(
                ClassLoaderHolder.gcl);
        scanner.setMetadataReaderFactory(new CachingMetadataReaderFactory2(resourcePatternResolver));

        return scanner;
    }
}
