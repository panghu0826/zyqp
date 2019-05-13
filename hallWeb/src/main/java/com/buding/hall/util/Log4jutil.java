package com.buding.hall.util;

/**
 * @author jaime(r67yfyt@126.com)
 * @date 2017/07/06
 * @since v1.0
 * @effect java生成Logger类,方便各个桌子生成自己的类
 */

public class Log4jutil {
//    public static Logger getLogger(String filePath, Class clazz){
//        Logger logSelf  = Logger.getLogger(Log4jutil.class);
//        Logger logger = Logger.getLogger(filePath);;
//        DailyRollingFileAppender appender;
//        try {
//            //生成新的Logger。
//            //如果已经有了一个Logger实例返回现有的。
//
//            //清空Appender。特别是不想使用现存实例时一定要初期化。
//            logger.removeAllAppenders();
//            //设定Logger级别。
//            logger.setLevel(Level.INFO);
//            //设定是否继承父Logger。
//            //默认为true。继承root输出。
//            //设定false后将不输出root。
//            logger.setAdditivity(true);
//            //生成新的Appender
//            appender = new DailyRollingFileAppender(new PatternLayout("%d %-5p %-5c{3}:%L -> %m%n"),filePath,"'.'yyyy-MM-dd");
//            appender.setEncoding("UTF-8");
//            appender.setAppend(true);
//            //将Appender添加到Logger
//            logger.addAppender(appender);
//            return logger;
//        } catch (IOException e) {
//            logSelf.info(e);
//            return Logger.getLogger(clazz);
//        }
//    }
}
