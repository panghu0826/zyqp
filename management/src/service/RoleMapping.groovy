package service

import com.guosen.webx.web.ChainEventListener

import java.util.regex.Pattern

/**
 * Created by kerry on 2016/6/2.
 *
 * 用以保存角色和资源的对应关系
 */
class RoleMapping implements ChainEventListener {
    // singleton
    private RoleMapping() {
    }
    private static RoleMapping one = new RoleMapping()

    public static RoleMapping getInst() {
        return one
    }

    void setRole(String role) {
        this.role = role
    }

    public static final String ROLE_DEFAULT = 'default'
    private String role = ROLE_DEFAULT

    /*
    def rm = RoleMapping.inst
    handler.eventListener = rm
    rm.role = 'sys'

    handler.get(''){req, resp, ctx -> }
     */
    private Map<String, List<Item>> mapping = new HashMap()

    @Override
    void reset() {
        role = ROLE_DEFAULT
    }

    @Override
    void addRouter(String method, String urlPat, Pattern pat) {
        for (oneRole in role.split(','))
            addRoleUrl(oneRole, method, urlPat, pat)
    }

    private void addRoleUrl(String oneRole, String method, String urlPat, Pattern pat) {
        List<Item> ll = mapping.get(oneRole)
        if (ll == null) {
            ll = []
            mapping.put(oneRole, ll)
        }

        for (one in ll) {
            if (one.method == method && one.urlPat == urlPat)
                return
        }

        ll.add(new Item(method, urlPat, pat))
    }

    @Override
    void addFilter(String name, String type, String urlPat, Pattern pat) {
    }

    public boolean check(List<String> roles, String uri) {
        for (role in roles) {
            if (check(role, uri))
                return true
        }

        return false
    }

    public boolean check(String oneRole, String uri) {
        List<Item> ll = mapping.get(oneRole)
        if (ll == null) {
            return false
        }

        ll.each {
            print it.urlPat;
        }

        for (one in ll) {
            if (one.pat.matcher(uri).matches())
                return true
        }
        return false
    }

    String print() {
        StringBuilder sb = new StringBuilder()
        mapping.each { oneRole, ll ->
            sb << oneRole
            sb << ':'
            for (one in ll) {
                sb << one.urlPat
                sb << ','
            }
            sb << "\r\n"
        }
        sb.toString()
    }

    @Override
    String toString() {
        'role mapping number -' + mapping.size()
    }

    class Item {
        Item(String method, String urlPat, Pattern pat) {
            this.method = method
            this.urlPat = urlPat
            this.pat = pat
        }
        String method
        String urlPat
        Pattern pat
    }
}
