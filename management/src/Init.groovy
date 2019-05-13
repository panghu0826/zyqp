import com.guosen.webx.web.ChainHandler
import org.slf4j.Logger
import service.SkipLoginMapping

ChainHandler h = (ChainHandler) handler
Logger l = (Logger) log

h.eventListener=SkipLoginMapping.inst
//启动的时候初始化最新的30条帖子到缓存中
l.info('set handler event listener')