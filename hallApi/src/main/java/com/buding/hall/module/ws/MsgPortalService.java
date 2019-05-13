package com.buding.hall.module.ws;

import com.buding.common.result.Result;
import com.buding.hall.module.msg.vo.BoxMsg;
import com.buding.hall.module.msg.vo.BoxMsgReq;
import com.buding.hall.module.msg.vo.GameChatMsg;
import com.buding.hall.module.msg.vo.MarqueeMsg;
import com.buding.hall.module.msg.vo.MarqueeMsgReq;
import com.buding.hall.module.msg.vo.TextMsg;

public interface MsgPortalService {
	public Result sendTplMarqueeMsg(MarqueeMsgReq req) throws Exception;

	public Result sendTplBoxMsg(BoxMsgReq req) throws Exception;

	public Result sendMarqueeMsg(MarqueeMsg req) throws Exception;

	public Result sendBoxMsg(BoxMsg req) throws Exception;
	
	public Result sendTextMsg(TextMsg req) throws Exception;
	
	public Result sendGameChatMsg(GameChatMsg req) throws Exception;
	
	public Result sendMarquee(long msgId, boolean check) throws Exception;
	
	public Result removeMarquee(long msgId) throws Exception;
	
	public Result removeMail(long mailId) throws Exception;
	
	public Result sendMail(long msgId) throws Exception;
	
	public Result repushActNotice() throws Exception;

	public void closeSocket(int playerId);

    void removeAllMarquee();
}
