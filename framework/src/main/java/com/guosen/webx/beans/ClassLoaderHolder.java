package com.guosen.webx.beans;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClassLoaderHolder {
    public static GroovyClassLoader gcl;
    private final static String ENCODING = "utf-8";

    static {
        CompilerConfiguration compileConf = new CompilerConfiguration();
        compileConf.setSourceEncoding(ENCODING);
        gcl = new CachedLoader(ClassLoaderHolder.class.getClassLoader(), compileConf);
        gcl.addClasspath("src");
    }

    private static class CachedLoader extends GroovyClassLoader {

        private Logger log = LoggerFactory.getLogger(CachedLoader.class);

        public CachedLoader(ClassLoader classLoader, CompilerConfiguration compileConf) {
            super(classLoader, compileConf);
        }

        private Map<String, Long> modified = new HashMap();

        private boolean isModified(File f) {
            String absPath = f.getAbsolutePath();
            Long r = modified.get(absPath);

            boolean isModified = r != null && r.longValue() != f.lastModified();
            return isModified;
        }

        protected final Map<String, Class> clzMapping = new HashMap<String, Class>();

        private void logClassLoaded(String pre, Map<String, Class> x) {
            log.debug("begin log class loaded - " + pre);
            Iterator<Map.Entry<String, Class>> it = x.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Class> entry = it.next();
                log.debug("" + entry.getKey() + " - " + entry.getValue());
            }
        }

        private Class getFromLoaded(String name, File f) {
            log.debug("class loaded size - " + classCache.size());
            Class answer = null;
            synchronized (classCache) {
                Iterator<Map.Entry<String, Class>> it = classCache.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Class> entry = it.next();
                    String key = entry.getKey();
                    if (isMatch(name, key)) {
                        answer = entry.getValue();
                        log.debug("get class from loaded - " + key);
                        this.modified.put(f.getAbsolutePath(), f.lastModified());
                        break;
                    }
                }
            }
            return answer;
        }

        private boolean isMatch(String name, String key) {
//            log.debug("is match ? " + name + " - " + key);
            String str = name.replace(".groovy", "").replaceAll("/", ".");
            return str.endsWith(key);
        }

        public Class parseClass(GroovyCodeSource codeSource, boolean shouldCacheSource) throws CompilationFailedException {
//            if (log.isDebugEnabled()) {
//                logClassLoaded("class loaded", classCache);
//            }

            File f = codeSource.getFile();
            Class answer = null;
            boolean isModified = isModified(f);

            if (!isModified) {
                synchronized (clzMapping) {
                    answer = clzMapping.get(codeSource.getName());
                }
                if (answer == null) {
                    answer = getFromLoaded(codeSource.getName(), f);
                }
            }

            if (answer == null) {
                answer = super.parseClass(codeSource, false);
                clzMapping.put(codeSource.getName(), answer);
                this.modified.put(f.getAbsolutePath(), f.lastModified());

                log.debug("recompile as it is changed - " + codeSource.getName());
            } else {
                log.debug("class not changed or is already compiled by other bean - " + codeSource.getName());
            }

            return answer;
        }

    }
}
