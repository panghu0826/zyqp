package com.buding.task.network.cmd;

import com.buding.db.model.UserRoomResult;
import com.buding.db.model.UserRoomResultDetail;
import com.buding.hall.module.user.service.UserService;
import com.buding.hall.module.vip.dao.UserRoomDao;
import com.buding.task.helper.TaskPushHelper;
import com.googlecode.protobuf.format.JsonFormat;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import packet.game.Hall;
import packet.game.Hall.PlayerScoreModel;
import packet.game.Hall.RoomResultModel;
import packet.game.Hall.RoomResultRequest;
import packet.game.Hall.RoomResultResponse;
import packet.msgbase.MsgBase.PacketBase;
import packet.msgbase.MsgBase.PacketType;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jaime qq_1094086610
 * @Description:
 *
 */
@Component
public class UserRoomResultCmd extends TaskBaseCmd {
	private Logger logger = LogManager.getLogger(getClass());

	@Autowired
	UserService userService;

	@Autowired
	TaskPushHelper pushHelper;

	@Autowired
	UserRoomDao userRoomDao;


	@Override
	public void execute(CmdData data) throws Exception {
		PacketBase packet = data.packet;
		RoomResultRequest ur = RoomResultRequest.parseFrom(packet.getData());
		long roomId = ur.getRoomId();
		long userId = data.session.userId;
		if(roomId == 0) { //查看总战绩
			RoomResultResponse.Builder rb = RoomResultResponse.newBuilder();
			rb.setClubId(ur.getClubId());
			rb.setPageNum(ur.getPageNum());
			List<RoomResultModel> roomResult = new ArrayList<>();
			if(ur.getClubId() <= 0) {//查询俱乐部普通场或外面的房间
				roomResult.addAll(getPlayingRoomResult(userId));
				roomResult.addAll(getPlayedRoomResult(userId));
			} else {//查询俱乐部积分场
				roomResult.addAll(getClubPlayedRoomResult(ur.getClubId()));
			}

			List<RoomResultModel> fenyeResult = fenye(roomResult,ur.getPageNum());
			rb.addAllList(fenyeResult);

			logger.info("战绩------------"+JsonFormat.printToString(rb.build()));
			pushHelper.pushRoomResultResponse(data.session, rb.build());
		} else {
			//查看某个房间的
			List<UserRoomResultDetail> list = userRoomDao.getUserRoomResultDetailList(roomId);
			RoomResultResponse.Builder rb = RoomResultResponse.newBuilder();
			rb.setClubId(ur.getClubId());
			rb.setPageNum(ur.getPageNum());
			for(UserRoomResultDetail model : list) {
				RoomResultModel.Builder bb  = RoomResultModel.newBuilder();
				bb.setPlayerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getEndTime()));
				bb.setRoomCode(model.getRoomName());
				bb.setRoomName(model.getRoomName());
				bb.setRoomId(model.getRoomId());
				bb.setVideoId(model.getVideoId());
				bb.setGameId(model.getGameId());
				JSONArray ja = JSONArray.fromObject(model.getDetail());
				for(int i = 0; i < ja.size(); i++) {
					JSONObject obj = ja.getJSONObject(i);
					PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
					score.setPlayerId(obj.getLong("playerId"));
					score.setPlayerName(obj.getString("playerName"));
					score.setScore(obj.getInt("score"));
					bb.addPlayerScore(score);
				}
				rb.addList(bb);
			}
			pushHelper.pushRoomResultResponse(data.session, rb.build());
		}
	}

	private List<RoomResultModel> getClubPlayedRoomResult(long clubId) {
		List<RoomResultModel> result = new ArrayList<>();
		List<UserRoomResult> list = userRoomDao.getClubRoomResultList(clubId);
		for(UserRoomResult model : list) {
			RoomResultModel.Builder bb  = RoomResultModel.newBuilder();
			bb.setPlayerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getEndTime()));
			bb.setRoomCode(model.getRoomName());
			bb.setRoomName(model.getRoomName());
			bb.setRoomId(model.getRoomId());
			bb.setGameId(model.getGameId());
			JSONArray ja = JSONArray.fromObject(model.getDetail());
			if(ja == null || ja.isEmpty()) continue;
			for(int i = 0; i < ja.size(); i++) {
				JSONObject obj = ja.getJSONObject(i);
//				logger.info("obj------"+obj);
				PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
				score.setPlayerId(obj.getLong("playerId"));
				score.setPlayerName(obj.getString("playerName"));
				score.setScore(obj.has("allScore")? obj.getInt("allScore") : obj.getInt("score"));
				bb.addPlayerScore(score);
			}
			result.add(bb.build());
		}

		return result;
	}

	private List<RoomResultModel> fenye(List<RoomResultModel> roomResult, int pageNum) {
		List<RoomResultModel> list = new ArrayList<>();
		int size = roomResult.size();
		int start = (pageNum - 1) * 20;
		int end = (start+20) < size ? (start+20) : size;
		for (int i = start; i < end; i++) {
			list.add(roomResult.get(i));
		}
		return list;
	}

	private List<RoomResultModel> getPlayedRoomResult(long userId) {
		List<RoomResultModel> result = new ArrayList<>();
		List<UserRoomResult> list = userRoomDao.getUserRoomResultList(userId);
		for(UserRoomResult model : list) {
            RoomResultModel.Builder bb  = RoomResultModel.newBuilder();
            bb.setPlayerTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getEndTime()));
            bb.setRoomCode(model.getRoomName());
            bb.setRoomName(model.getRoomName());
            bb.setRoomId(model.getRoomId());
            bb.setGameId(model.getGameId());
            JSONArray ja = JSONArray.fromObject(model.getDetail());
            for(int i = 0; i < ja.size(); i++) {
                JSONObject obj = ja.getJSONObject(i);
//                logger.info("obj------"+obj);
                PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
                score.setPlayerId(obj.getLong("playerId"));
                score.setPlayerName(obj.getString("playerName"));
                score.setScore(obj.has("allScore")? obj.getInt("allScore") : obj.getInt("score"));
                bb.addPlayerScore(score);
            }
			result.add(bb.build());
        }
        return result;
	}

	private List<RoomResultModel> getPlayingRoomResult(long userId) {
		List<RoomResultModel> result = new ArrayList<>();
		List<UserRoomResultDetail> detailList = userRoomDao.getUserPlayingRoomResultDetailList(userId);
		Map<Long,Integer> map = new HashMap<>();
		if(detailList!=null){
            for(UserRoomResultDetail u:detailList){
                JSONArray json = JSONArray.fromObject(u.getDetail());
                for(int i = 0; i < json.size(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    int score = obj.getInt("score");
                    long id = obj.getLong("playerId");
                    map.merge(id, score, (a1, b) -> a1 + b);

//						Integer a = map.get(id);
//						if(a==null){
//							map.put(id,score);
//						}else{
//							map.put(id,a+score);
//						}
                }
            }
            RoomResultModel.Builder bb  = RoomResultModel.newBuilder();
            bb.setPlayerTime("对局中");
            bb.setRoomCode(detailList.get(0).getRoomName());
            bb.setRoomName(detailList.get(0).getRoomName());
            bb.setRoomId(detailList.get(0).getRoomId());
            bb.setGameId(detailList.get(0).getGameId());
            JSONArray ja = JSONArray.fromObject(detailList.get(0).getDetail());
            for(int i = 0; i < ja.size(); i++) {
                JSONObject obj = ja.getJSONObject(i);
                PlayerScoreModel.Builder score = PlayerScoreModel.newBuilder();
                score.setPlayerId(obj.getLong("playerId"));
                score.setPlayerName(obj.getString("playerName"));
                score.setScore(map.get(obj.getLong("playerId")));
                bb.addPlayerScore(score);
            }
            result.add(bb.build());
        }
        return result;
	}

	@Override
	public PacketType getKey() {
		return PacketType.RoomResultRequest;
	}

}
