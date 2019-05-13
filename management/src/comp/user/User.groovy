package comp.user

import com.alibaba.fastjson.JSON
import com.guosen.webx.d.D
import com.guosen.webx.d.Pager
import comp.Constants
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import utils.Utils

/**
 * Created by kerry on 2016/5/24.
 */
@Component
class User {
    @Autowired
    @Qualifier("d")
    D d

    int add(Map one) {
        if (one.password)
            one.password = DigestUtils.md5Hex(one.password.toString())
        d.add(one, 'tj_user')
    }

    void update(int id, Map one, String fields = null) {
        Map user = fields == null ? one : Utils.getMapInKeys(one, fields)
        user.id = id
        d.update(user, 'tj_user')
    }

    void updatePwd(int id, String password) {
        password = DigestUtils.md5Hex(password)
        update(id, [password: password])
    }

    void updateMpUserId(int id, int mpUserId) {
        update(id, [mpUserId: mpUserId])
    }

    void updateRole(int id, UserRole userRole) {
        update(id, [role: userRole.value])
    }

    void updateRole(int id, int userRole) {
        update(id, [role: userRole])
    }

    void updateLabel(int id, UserLabel userLabel) {
        update(id, [label: userLabel.value])
    }

    void updateLabel(int id, int userLabel) {
        update(id, [label: userLabel])
    }

    void updateStatus(int id, int userStatus) {
        update(id, [status: userStatus])
    }

    Map one(int id, boolean withExt = false, boolean withMp = false) {
        Map one = d.queryMap('select * from tj_user where id = ?', [id])
        if (withExt) {
            Map ext = d.queryMap('select * from tj_user_ext where user_id = ?', [id])
            if (ext) {
                one << ext
            }
        }
        if (withMp && one.mpUserId) {
            Map ext = MpUserLocal.create().getUserById(one.mpUserId)
            if (ext) {
                one << ext
            }
        }
        one
    }

    // 查询用户列表
    Pager list(Map query, int pageNum, int pageSize = 10) {
        String sql = "select * from tj_user where 1 = 1"
        List args = []

        if (query.phone) {
            sql += ' and phone = ?'
            args << query.phone
        }
        if (query.status) {
            sql += ' and status = ?'
            args << query.status
        }
        if (query.role) {
            sql += ' and (role & ? = ?)'
            args << query.role
            args << query.role
        }
        if (query.name) {
            String name = Utils.transferSql(query.name)
            sql += " and name like '%${name}%'"
        }

        def d = D.gen()
        Pager pager = d.pagi(sql, args, pageNum, pageSize)

        // 默认关联合并对应微信用户信息
        if (pager.ll) {
            mergeMpUserInfo(pager.ll)
        }

        pager
    }

    void mergeMpUserInfo(List list) {
        List mpUserIds = list.collect { it.mpUserId }.grep { it }
        List ll = MpUserLocal.create().getUserList(mpUserIds)
        if (!ll) {
            return
        }

        for (one in list) {
            Map sub = ll.find { it.id == one.mpUserId }
            if (sub) {
                one << sub
            }
        }
    }

    void mergeExtUserInfo(List list) {
        List ids = list.collect { it.id }
        List ll = d.query("select * from tj_user_ext where user_id in (${ids})")
        if (!ll) {
            return
        }

        for (one in list) {
            Map sub = ll.find { it.userId == one.id }
            if (sub) {
                one << sub
            }
        }
    }

    // 验证，一般用phone
    int check(String phone, String password) {
        Map one = d.queryMap('select id, password, status from tj_user where phone = ?', [phone])
        if (!one)
            return -1

        if (one.password != DigestUtils.md5Hex(password) || one.status != Constants.STATUS_YES) {
            return -1
        }

        one.id
    }

    static boolean isRole(int role, UserRole userRole) {
        int i = userRole.value
        (role & i) == i
    }

    static boolean isLabel(int label, UserLabel userLabel) {
        int i = userLabel.value
        (label & i) == i
    }

    List getAllRoleList() {
        d.query('select * from tj_user_role')
    }

    Map getById(Long id) {
        List lstParam = [id]
        d.queryMap('select * from tj_user where id=?', lstParam)
    }

    List getAllLabelList() {
        d.query('select * from tj_label')
    }

    List getShowLabelList(){
        List list = [UserLabel.Xiaoxin.value,UserLabel.Advisor.value,UserLabel.Manager.value]
        String ids = list.join(',')
        String s = "($ids)"

        d.query("select * from tj_label where code in $s")
    }

    List getMenusByRole(int role) {
        List menus = []

        List roleList = d.query('select code, menus from tj_user_role where (code & ? = code)', [role])
        for (one in roleList) {
            def tmp = JSON.parse(one.menus);
            if (tmp) { //防止add null到list，报错
                menus.addAll(tmp)
            }
        }
        menus
    }

    Map getByPhone(String phone) {
        this.d.queryMap("select * from tj_user where phone = ?", [phone])
    }

    Map getByNickName(String nickname) {
        this.d.queryMap("select * from tj_user where nickname = ? ", [nickname])
    }

    Map getByCashAccount(String cashAccount) {
        this.d.queryMap("select * from tj_user where cash_account = ? ", [cashAccount])
    }

    // 用户标签层级，权限相关，用枚举方式
    enum UserLabel {
        Normal(1), //注册用户
        Customer(2), // 客户
        Advisor(4), // 投顾
        Manager(8), // 客户经理
        Xiaoxin(16), // 小信
        XiaoxinManager(32), // 高级小信
        MidCustomer(1024), // 中小客户
        HighCustomer(2048); // 大客户

        private UserLabel(int i) {
            this.value = i
        }
        int value = 0;

        public int getValue() {
            this.value
        }
    }

    // 用户角色，权限相关，用枚举方式
    enum UserRole {
        Sys(1),  // 系统管理员
        Manager(2),  // 管理员，可以登录后台管理系统
        Advisor(8), //
        Tj_Manager(512),//
        Consult(1024), //
        Taiji_Manager(4096), //
        ACTIVITY_TICKET_CHECK_MANAGER(2048), //
        ACTIVITY_PUBLISH_MANAGER(4), //
        ACTIVITY_MANAGER(16); //

        private UserRole(int i) {
            this.value = i
        }
        int value = 0;

        public int getValue() {
            this.value
        }
    }
}
