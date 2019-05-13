package utils

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 卢丹文 on 2016/5/5.
 */
class HtmlRegexpUtils {
    private final static String regxpForHtml = "<([^>]*)>";

    private final static String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*>";

    private final static String regxpForImaTagSrcAttrib = "src=\"([^\"]+)\"";
    /**
     *
     * 替换特定的标签
     * <p>
     *
     * @param input
     * @return String
     */
    public String replaceTag(String input) {
        if (!hasSpecialChars(input)) {
            return input;
        }
        StringBuffer filtered = new StringBuffer(input.length());
        char c;
        for (int i = 0; i <= input.length() - 1; i++) {
            c = input.charAt(i);
            switch (c) {
                case '<':
                    filtered.append("&lt;");
                    break;
                case '>':
                    filtered.append("&gt;");
                    break;
                case '"':
                    filtered.append("&quot;");
                    break;
                case '&':
                    filtered.append("&amp;");
                    break;
                default:
                    filtered.append(c);
            }
        }
        return (filtered.toString());
    }

    /**
     *
     * 检验是否有特定的标签
     * <p>
     *
     * @param input
     * @return boolean
     */
    public boolean hasSpecialChars(String input) {
        boolean flag = false;
        if ((input != null) && (input.length() > 0)) {
            char c;
            for (int i = 0; i <= input.length() - 1; i++) {
                c = input.charAt(i);
                switch (c) {
                    case '>':
                        flag = true;
                        break;
                    case '<':
                        flag = true;
                        break;
                    case '"':
                        flag = true;
                        break;
                    case '&':
                        flag = true;
                        break;
                }
            }
        }
        return flag;
    }

    /**
     *
     * 过滤标签
     * <p>
     *
     * @param str
     * @return String
     */
    public static String filterHtml(String str) {
        Pattern pattern = Pattern.compile(regxpForHtml);
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1) {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**卢丹文
     * 提取标签中的属性
     * @param source
     * @param element
     * @param attr
     * @return
     */
    public static List<String> match(String source, String element, String attr) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + "[^<>]*?\\s.*?" + attr + "=['\"]?(.*?)['\"]?\\s.*?>";
        reg = "<"+element+"\\s+.*?"+attr+"\\s*=\\s*['\"](.+?)['\"].*?>" //add by vince
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            result.add(r);
        }
        return result;
    }


    public static String Html2Text(String inputString) {
        String htmlStr = inputString; // 含html标签的字符串
        String textStr = "";
        Pattern p_script;
        Matcher m_script;
        Pattern p_style;
        Matcher m_style;
        Pattern p_html;
        Matcher m_html;

        try {
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
            // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
            // }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
            // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
            // }
            String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

            String space_html = "&nbsp;"; // 空格的正则

            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); // 过滤script标签

            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); // 过滤style标签

            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); // 过滤html标签

            p_html = Pattern.compile(space_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); // 过滤空格标签

            textStr = htmlStr;
        } catch (Exception e) {
        }
        return textStr;// 返回文本字符串
    }

    /** 卢丹文
     * 将html内容按照标签转换为数组
     * @param source
     * @param element
     * @param attr
     * @return
     */
    public static List<String> tranformHtmlToArray(String source) {
        List<String> list = new ArrayList<String>();
        if (!source) {
            return null
        }

        //把换行符全部替换为空格
        source.replaceAll("\\n"," ")

        if (!source.contains("<") && !source.contains(">")) {
            //不是html标签,直接将内容返回
            list.add(source)
            return list
        }
        Matcher m = Pattern.compile("<p.*?>([\\s\\S]*?)</p>").matcher(source)

        while (m.find()) {
            String temp = m.group(1)
            if (temp.contains("img")) {
                //如果内容包含图片，则先把图片链接提取出来，再把文字内容放到数组中
                list.addAll(match(temp, "img", "src"))
                temp = Html2Text(temp)
            } else {
                temp = Html2Text(temp);
            }
            if (temp) {
                list << temp
            }
        }
        return list;
    }
}