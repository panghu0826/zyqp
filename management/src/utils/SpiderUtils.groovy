package utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination


/**
 * Created by Kerry on 2016/5/5.
 * update panjintao 2016/07/11 增加敏感词替换
 */
class SpiderUtils {
    private static Map<String, Object> fields = new HashMap<String, Object>();

    public <T> T get(String key) {
        Object o = fields.get(key);
        if (o == null) {
            return null;
        }
        return (T) fields.get(key);
    }

    public static Map<String, Object> getAll() {
        return fields;
    }

    public <T> SpiderUtils put(String key, T value) {
        fields.put(key, value);
        return this;
    }

}