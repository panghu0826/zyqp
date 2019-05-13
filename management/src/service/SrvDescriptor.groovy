package service

import com.guosen.webx.web.ChainEventListener

import java.util.regex.Pattern

/**
 * Created by vince on 2016/8/26.
 *
 * 保存服务版本信息
 */
class SrvDescriptor implements ChainEventListener {
    // singleton
    private SrvDescriptor() {
    }
    private static SrvDescriptor one = new SrvDescriptor()

    public static SrvDescriptor getInst() {
        return one
    }

    void setDescriptor(Map descriptor) {
        this.descriptor = descriptor;
    }

    private Map descriptor = [:]

    private List list = new ArrayList()

    List getList(){
        return list;
    }

    @Override
    void reset() {
        descriptor = null;
    }

    @Override
    void addRouter(String method, String urlPat, Pattern pat) {
        if(descriptor){
            list.add(descriptor);
        }
        reset();
    }

    @Override
    void addFilter(String name, String type, String urlPat, Pattern pat) {

    }

    @Override
    String toString() {

    }
}
