package service

import comp.user.User

/**
 * Created by panjintao on 2016/7/4.
 */
class UserTree {
    /*
    * 树结构，默认用户‘最终身份’至‘注册用户’只存在一条路径
    * */
    private static Map codeMap = [1:1] //存放每种身份实际的code值，如客户包括客户code以及注册用户code
    private static final Map m = [
            2: [//客户
                1024: null,//中小客户
                2048: null, //大客户
                8   : null,  //客户经理
                4   : null, //投顾
                16  : null//小信
            ]
    ] //用户层级关系
    private static List extLabel = [] //附加权限，独立于用户层级
    private static Map cacheMap = [:] //存放已匹配好的code，减少递归次数,
    private static UserTree instance = new UserTree()

    private UserTree() {
        int code = User.UserLabel.Normal.value

        setCodeMap(m, code)
        setExtLabel()
    }

    public static UserTree getInstance() {
        return instance
    }

    private void setExtLabel() {
        extLabel << User.UserLabel.XiaoxinManager.value //高级小信
    }

    /*
    * 初始化codeMap
    * */

    private void setCodeMap(Map map, int code) {
        map.each {
            codeMap.put(it.key, it.key + code)
            if (it.value) {
                setCodeMap(it.value, code + it.key)
            }
        }
    }

    /*
    * 获取叶节点
    * */

    private List getNodeList(Map map) {
        List list = []
        map.each {
            if (it.value) {

                list.addAll(getNodeList(it.value))
            } else {
                list.add(it.key)
            }
        }
        list
    }

    /*
    * 删除叶节点
    * */

    private Map removeNode(Map map) {
        Map tree = [:]

        map.each {
            if (it.value) {
                Map t = removeNode(it.value)
                tree.put(it.key, t)
            }
        }

        if (!tree) {
            return null
        }
        tree
    }

    /*
    * 获取身份code,即该用户最高级别的code
    * 展示的code
    * */

    public int getLabelCode(int code) {
        Map map = m
        List list = getNodeList(map)

        int c = 0

        while (list) {
            list.each {
                if ((code & it) == it) {
                    c = it
                }
            }

            if (c) {
                break
            } else {
                map = removeNode(map)
                list = getNodeList(map)
            }
        }

        c ? c : User.UserLabel.Normal.value
    }

    /*
    * 设置身份code
    * 调用时判断该函数返回值不为0再继续操作
    * */

    public int setLabelCode(int code) {
        int c

        code = code ? code : User.UserLabel.Normal.value

        if (cacheMap.containsKey(code)) {
            c = cacheMap[code]
        } else {
            c = getLabelCode(code)
            c = codeMap[c]

            //添加附加权限
            extLabel.each {

                if ((code & it) == it && (c & it) != it) {
                    c += it
                }
            }

            cacheMap[code] = c
        }
        c
    }

}
