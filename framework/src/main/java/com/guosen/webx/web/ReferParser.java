package com.guosen.webx.web;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kerry on 2016/6/8.
 * 分析handler中引入了comp中的关系，方便comp中有修改，重新reload context时候，对应引入该类的handler脚本也重新编译
 */
class ReferParser {
    private Logger log = LoggerFactory.getLogger(ReferParser.class);

    private Map<String, List<String>> mapping = new HashMap();

    public void addOne(String bean, String absPath) {
        List<String> list = mapping.get(bean);
        if (list == null) {
            list = new ArrayList<String>();
            mapping.put(bean, list);
        }
        if (!list.contains(absPath)) {
            list.add(absPath);
            log.info("add handler comp mapping - " + bean + " - " + absPath);
        }
    }

    public void parse(File handlerFile) throws IOException {
        List<String> lines = FileUtils.readLines(handlerFile);
        for (String it : lines) {
            it = it.trim();
            if (it.startsWith("import comp.")) {
                String[] arr = it.split("\\.");
                if (arr.length < 1) {
                    log.info("parse handler comp mapping fail " + it + " - " + handlerFile.getAbsolutePath());
                } else {
                    String bean = arr[arr.length - 1];
                    bean = bean.replace(";", "");
                    addOne(bean, handlerFile.getAbsolutePath());
                }
            }
        }
    }

    public boolean isHandlerReferComp(String bean, String absPath) {
        List<String> list = mapping.get(bean);
        return list != null && list.contains(absPath);
    }
}
