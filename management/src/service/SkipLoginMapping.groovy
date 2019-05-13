package service

import com.guosen.webx.web.ChainEventListener
import com.sun.org.apache.xpath.internal.operations.Bool

import java.util.regex.Pattern

/**
 * Created by kerry on 2016/6/2.
 *
 * 用以保存角色和资源的对应关系
 */
class SkipLoginMapping implements ChainEventListener {
    // singleton
    private SkipLoginMapping() {
    }
    private static SkipLoginMapping one = new SkipLoginMapping()

    public static SkipLoginMapping getInst() {
        return one
    }

    void setSkipLogin(boolean skipLogin) {
        this.skipLogin = skipLogin
    }

    private boolean skipLogin = false;

    private List<Item> skipLoginList = new ArrayList<Item>();

    @Override
    void reset() {
        skipLogin = false;
    }

    @Override
    void addRouter(String method, String urlPat, Pattern pat) {
        if(skipLogin) {
            skipLoginList.add(new Item(method, urlPat, pat))
        }
        skipLogin = false; //重置为需要登录状态
    }

    @Override
    void addFilter(String name, String type, String urlPat, Pattern pat) {
    }

    public boolean isSkipLogin(String uri) {
        for (one in skipLoginList) {
            if (one.pat.matcher(uri).matches())
                return true
        }
        return false
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
