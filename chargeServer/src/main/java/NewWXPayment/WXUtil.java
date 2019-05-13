package NewWXPayment;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WXUtil 
{
	public static boolean CheckSign(Map<String , String> params , String paternerKey) throws UnsupportedEncodingException
	{
		Map<String, String> kToSignmap = new HashMap<String, String>();
		String requestSignValue = "";
		for (Map.Entry<String, String> entry : params.entrySet()) {  
		    if(entry.getKey() != "sign")
		    {
		    	kToSignmap.put(entry.getKey(), entry.getValue());
		    }
		    else
		    {
		    	requestSignValue = entry.getValue();
		    }
		}
		
		return requestSignValue == "" || getSign(kToSignmap, paternerKey).equals(requestSignValue);
	}
	
	public static String getSign(Map<String, String> params, String paternerKey) throws UnsupportedEncodingException{
		return MD5Utils.getMD5(createSign(params, false) + "&key=" + paternerKey).toUpperCase();
	}
	
	/**
	 * 鏋勯�绛惧悕
	 * 
	 * @param params
	 * @param encode
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String createSign(Map<String, String> params, boolean encode) throws UnsupportedEncodingException{
		Set<String> keysSet = params.keySet();
		Object[] keys = keysSet.toArray();
		Arrays.sort(keys);
		StringBuffer temp = new StringBuffer();
		boolean first = true;
		for (Object key : keys) {
			if (key == null || StringUtils.isEmpty(params.get(key)))
				continue;
			if (first) {
				first = false;
			} else {
				temp.append("&");
			}
			temp.append(key).append("=");
			Object value = params.get(key);
			String valueStr = "";
			if (null != value) {
				valueStr = value.toString();
			}
			if (encode) {
				temp.append(URLEncoder.encode(valueStr, "UTF-8"));
			} else {
				temp.append(valueStr);
			}
		}
		return temp.toString();
	}
	
	 // 转换为xml
    public static String map2xml(Map<String, String> map) {
        String info = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        info += "<xml>";
        Set<String> keySet = map.keySet();
        for (String string : keySet) {
            info += "<" + string + ">" + map.get(string) + "</" + string + ">";
        }
        info += "</xml>";
        return info;
    }
    
    public static Map<String, String> xml2Map(String xmlStr) {
        Map<String, String> map = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 将字符串转换成流
            ByteArrayInputStream bis = new ByteArrayInputStream(xmlStr
                    .getBytes());
            Document doc = builder.parse(bis);
            Node root = doc.getFirstChild();// 根节点
            NodeList nodeList = root.getChildNodes();
            map = new HashMap<String, String>();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child instanceof Element) {
                    Element e = (Element) child;
                    map.put(e.getNodeName(), e.getTextContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}
