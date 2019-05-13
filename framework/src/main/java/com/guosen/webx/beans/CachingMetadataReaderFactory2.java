package com.guosen.webx.beans;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import groovy.lang.GroovyCodeSource;
import org.springframework.asm.Opcodes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

public class CachingMetadataReaderFactory2 extends SimpleMetadataReaderFactory {

    /**
     * Default maximum number of entries for the MetadataReader cache: 256
     */
    public static final int DEFAULT_CACHE_LIMIT = 256;

    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

    @SuppressWarnings("serial")
    private final Map<Resource, MetadataReader> metadataReaderCache = new LinkedHashMap<Resource, MetadataReader>(
            DEFAULT_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Resource, MetadataReader> eldest) {
            return size() > getCacheLimit();
        }
    };

    /**
     * Create a new CachingMetadataReaderFactory for the default class loader.
     */
    public CachingMetadataReaderFactory2() {
        super();
    }

    /**
     * Create a new CachingMetadataReaderFactory for the given resource loader.
     *
     * @param resourceLoader the Spring ResourceLoader to use (also determines the
     *                       ClassLoader to use)
     */
    public CachingMetadataReaderFactory2(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    /**
     * Create a new CachingMetadataReaderFactory for the given class loader.
     *
     * @param classLoader the ClassLoader to use
     */
    public CachingMetadataReaderFactory2(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * Specify the maximum number of entries for the MetadataReader cache.
     * Default is 256.
     */
    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    /**
     * Return the maximum number of entries for the MetadataReader cache.
     */
    public int getCacheLimit() {
        return this.cacheLimit;
    }

    @Override
    public MetadataReader getMetadataReader(Resource resource) throws IOException {
        if (getCacheLimit() <= 0) {
            return super.getMetadataReader(resource);
        }
        synchronized (this.metadataReaderCache) {
            MetadataReader metadataReader = this.metadataReaderCache.get(resource);
            if (metadataReader == null) {
                metadataReader = genReader(resource);
                this.metadataReaderCache.put(resource, metadataReader);
            }
            return metadataReader;
        }
    }

    @SuppressWarnings("rawtypes")
    private MetadataReader genReader(final Resource resource) throws IOException {
        File f = resource.getFile();
        GroovyCodeSource source = new GroovyCodeSource(f);
        Class clz = ClassLoaderHolder.gcl.parseClass(source);

        final AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor(
                super.getResourceLoader().getClassLoader());

        Class[] ll = clz.getInterfaces();
        // must has GroovyObject
        String[] interfaces = new String[ll.length];
        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = ll[i].getName();
        }
        visitor.visit(0, Opcodes.ACC_PUBLIC, clz.getName(), null, clz.getSuperclass().getName(), interfaces);

        Annotation[] ll2 = clz.getAnnotations();
        if (ll2.length > 0) {
            for (Annotation ano : ll2) {
                visitor.getAnnotationTypes().add(ano.annotationType().getName());
            }
        }

        MetadataReader reader = new MetadataReader() {
            @Override
            public Resource getResource() {
                return resource;
            }

            @Override
            public ClassMetadata getClassMetadata() {
                return visitor;
            }

            @Override
            public AnnotationMetadata getAnnotationMetadata() {
                return visitor;
            }

        };
        return reader;
    }

    /**
     * Clear the entire MetadataReader cache, removing all cached class
     * metadata.
     */
    public void clearCache() {
        synchronized (this.metadataReaderCache) {
            this.metadataReaderCache.clear();
        }
    }

}
