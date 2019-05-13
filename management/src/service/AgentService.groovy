package service

import com.buding.hall.module.item.type.ItemChangeReason
import com.guosen.webx.beans.Context
import com.guosen.webx.d.D
import comp.battle.HallProxy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.buding.common.result.Result
import com.buding.db.model.User
/**
 * Created by jaime on 2016/6/13.
 * 代理商服务相关接口
 */
class AgentService {
    private static Logger log = LoggerFactory.getLogger(AgentService.class);

    private void sellFangka(int agentId, int userId, int fangka, req, resp) {
        if (fangka <= 0) {
            resp.fail("数量需要大于0");
            return;
        }
        D d = Context.inst.d;
        def agent = d.queryMap("select * from agent where id = ? ", agentId);
        if (!agent) {
            resp.fail("代理商不存在");
            return;
        }
        if (!agent.fangka || agent.fangka < fangka) {
            resp.fail("房卡不足");
            return;
        }
        HallProxy proxy = ctx.hallProxy
        def srv = proxy.getHallService(c.zkHost)
        User user = srv.getUser(userId)
        int from = user.getFanka();
        int to = from + fangka;
        Result ret = srv.changeFangka(userId, fangka, false, ItemChangeReason.AGENT_SELL)
        if (ret.isOk()) {
            agent.fangka -= fangka;
            d.update(agent, "agent");
            def str = "销售房卡${fangka}个给玩家${user.nickname}(${user.id})"
            AdminLogService.addAdminLog(req.getLocal("user").id, "AGENT_SELL_FANGKA", userId, from, to, fangka, new Date(), str)
            resp.ok()
        } else {
            resp.fail(ret.msg)
        }
        resp.ok()
    }
}