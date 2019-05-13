package handler.common

import com.guosen.webx.d.D
import com.guosen.webx.web.ChainHandler
import comp.common.RedisCacher
import service.SkipLoginMapping

// handler is GroovyShell binding variable
ChainHandler h = (ChainHandler) handler
Properties c = (Properties) config

/**
 * 给native端的统一的配置
 */
SkipLoginMapping.inst.skipLogin = true;
h.get('/a/m/tj/config') { req, resp, ctx ->
    RedisCacher cacher = ctx.redisCacher
    Map one = cacher.doAndCache('startupAd', 3600 * 1000) {
        D d = ctx.d
        d.queryMap('select ver,imgs from tj_startup_ad order by id desc limit 1')
    }

    int startupAdVer = one ? one.ver : 0
    String startupAdImgs = one ? one.imgs : ''

    resp.ok([
            host         : c.hostDomain,
            whiteList    : [c.hostDomain, "http://blog.guosen.com.cn", "https://3gimg.qq.com", "http://www.baidu.com/", "https://www.baidu.com/", "http://apis.map.qq.com/", "https://map.qq.com"],
            startupAdVer : startupAdVer,
            startupAdImgs: startupAdImgs
    ])
}

// 查看当前发布的版本号
h.get('/a/m/version') { req, resp, ctx ->
    def f = new File('config/version.txt')
    String version = f.exists() ? f.text : ''
    resp.end '' + version
}