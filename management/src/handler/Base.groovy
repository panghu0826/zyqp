package handler

import com.guosen.webx.web.ChainHandler
import com.guosen.webx.web.ServerJetty
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import utils.BusinessException
import utils.Utils

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log
Properties c = (Properties) config

def staticFileUrlPat = /.*\.(js|css|html|properties|groovy|xml|json)/
def staticImageUrlPat = /.*\.(jpg|jpeg|png|gif|ico|eot|svg|ttf|woff|woff2|swf|mp3|mp4)/
def staticFontUrlPat = /.*\.(eot|svg|ttf|woff|woff2|swf|mp4|mp3)/

// 测试环境和本地环境没有nginx反向代理，jpg结尾的不能处理。正式环境由nginx反向代理去做
h.getRegEx(staticFileUrlPat) { req, resp, ctx ->
    resp.sendFile c.webroot + req.path
}.getRegEx(staticImageUrlPat) { req, resp, ctx ->
    String contentType = 'application/octet-stream'
    def mat = req.path =~ staticFontUrlPat
    if (!mat) {
        mat = req.path =~ staticImageUrlPat
        String ext = mat ? mat[0][1] : 'jpg'
        contentType = 'image/' + ext
    }
    resp.headers.set('Content-Type', contentType)

    def f = new File(c.webroot + req.path)
    if (f.exists())
        resp.end f.bytes
    else {
        resp.setStatusCode(404).end()
    }
}

h.get('/a/m/stopserver') { req, resp, ctx ->
    String key = req.params.key
    if (!c.serverKey || DigestUtils.md5Hex(c.serverKey) != key) {
        resp.end 'no auth'
        return
    }
    resp.end 'server stopped'
    Thread.start {
        Thread.currentThread().sleep(1000)
        ServerJetty.stop()
    }
}

h.setNoMatchHandler { req, resp, ctx ->
    resp.statusCode = 404

    resp.end 'file not found'
}.setErrorHandler { req, resp, ex ->
    l.error("", ex)
    if(ex instanceof BusinessException) {
        String msg = ex.getMessage();
        if(msg) {
            resp.fail(-1, msg)
            return ;
        }
    }

    String msg = req.getLocal('msg')

    // 业务处理异常，按约定格式返回
    if (msg != null) {
        resp.fail(-1, msg + '出现错误')
        return
    }

    resp.statusCode = 200
    if (req.isAjax()) {
        resp.fail(-1, "操作发生错误，请稍候重试")
    } else {
        resp.end 'exception caught ' + ex.message
    }
}