//package com.buding.common.network.secret;
//
//import com.buding.common.network.model.Secretkey;
//import org.apache.commons.lang.StringUtils;
//
//import java.util.concurrent.ConcurrentHashMap;
//
//public class SecretKeyManager {
//
//    // 玩家id--DES密钥
//    public static ConcurrentHashMap<Integer,Secretkey> secretMap = new ConcurrentHashMap<>();
//
//    // 玩家key--DES密钥
//    public static ConcurrentHashMap<Long,Secretkey> secretKeyMap = new ConcurrentHashMap<>();
//
//    // 玩家wxunionid--DES密钥
//    public static ConcurrentHashMap<String,Secretkey> unionIdsecretMap = new ConcurrentHashMap<>();
//
//    private void init() {
//
//    }
//
//    public synchronized static void add(Integer playerId,String secretKey,long key,String wxunionid){
//
//
//        if(secretMap.get(playerId)!=null){
//            long oldKey = secretMap.get(playerId).getKey();
//            if(oldKey > 0) {
//                secretKeyMap.remove(oldKey);
//            }
//
//            String oldWxunionid = secretMap.get(playerId).getWxunionid();
//            if(StringUtils.isNotBlank(oldWxunionid)){
//                unionIdsecretMap.remove(oldWxunionid);
//            }
//        }
//        Secretkey model = new Secretkey(playerId,key,secretKey,wxunionid);
//        secretMap.put(playerId,model);
//        secretKeyMap.put(key,model);
//        unionIdsecretMap.put(wxunionid,model);
//    }
//}
