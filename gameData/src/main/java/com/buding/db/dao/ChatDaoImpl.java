package com.buding.db.dao;

import com.buding.common.db.cache.CachedServiceAdpter;
import com.buding.common.db.executor.DbService;
import com.buding.common.server.ServerConfig;
import com.buding.db.model.Chat;
import com.buding.db.model.ChatContent;
import com.buding.hall.module.chat.dao.ChatDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatDaoImpl extends CachedServiceAdpter implements ChatDao {

	@Autowired
	DbService dbService;

	@Override
	public long insertChat(Chat chat) {
		this.commonDao.save(chat);
		return chat.getId();
	}

	@Override
	public void deleteChat(long chatId) {
        this.commonDao.delete(chatId,Chat.class);
	}

	@Override
	public void updateChat(Chat chat) {
		this.put2EntityCache(chat);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(chat);
		} else {
			this.dbService.submitUpdate2Queue(chat);
		}
	}

    @Override
    public Chat selectChat(long chatId) {
        return this.commonDao.get(chatId,Chat.class);
    }

    @Override
    public List<Chat> selectAllPrivateChatList(int userId) {
	    String sql = "SELECT" +
				"            a.id," +
				"            a.chat_type," +
				"            a.club_id," +
				"            a.user1_id," +
				"            b.nickname as user1_name," +
				"            b.head_img as user1_img," +
				"            a.user2_id," +
				"            c.nickname as user2_name," +
				"            c.head_img as user2_img," +
				"            a.ctime" +
				"        FROM" +
				"          t_chat a," +
				"          user b," +
				"          user c" +
				"        WHERE" +
				"          a.user1_id = b.id" +
				"        AND" +
				"          a.user2_id = c.id" +
				"   AND" +
				"    (a.user1_id = ? or a.user2_id = ?)" +
				"    UNION" +
				"        SELECT" +
				"            d.id," +
				"            d.chat_type," +
				"            d.club_id," +
				"            d.user1_id," +
				"            d.user2_id," +
				"            '' as user1_name," +
				"            '' as user1_img," +
				"            '' as user2_img," +
				"            '' as user2_name," +
				"            d.ctime" +
				"         FROM" +
				"          t_chat d" +
				"         WHERE" +
				"          d.user1_id IS NULL" +
				"         AND" +
				"          d.user2_id IS NULL" +
				"         AND" +
				"          (d.user1_id = ? or d.user2_id = ?)";
	    List<Chat> list = this.commonDao.selectList(sql, Chat.class, userId,userId,userId,userId);
        List<Chat> result = new ArrayList<>();
        for (Chat chat : list){
        	if(chat == null) continue;
            List<ChatContent> contents = selectChatContent(chat.getId());
            if(contents != null && contents.size() > 0) result.add(chat);
            if(result.size() >= 50) break;
        }
        return result;
    }

    @Override
    public Chat selectChatList(int user1Id,int user2Id) {
	    String sql = "select * from t_chat where (user1_id = ? and user2_id = ?) or (user1_id = ? and user2_id = ?) ";
        return this.commonDao.selectOne(sql, Chat.class, user1Id,user2Id,user2Id,user1Id);
    }

    @Override
    public Chat selectClubChat(long clubId) {
        return this.commonDao.selectOne("select * from t_chat where club_id = ? ", Chat.class, clubId);
    }

	@Override
	public void insertChatContent(ChatContent content) {
		this.commonDao.save(content);
	}

	@Override
	public void deleteUserChatContent(long chatId, int playerId, long chatTime) {
        ChatContent content = this.commonDao.selectOne("select * from t_chat_content where chat_id = ? and player_id = ? and chat_time = ? ", ChatContent.class, chatId,playerId,new Date(chatTime));
        this.commonDao.delete(content.getId(),ChatContent.class);
	}

	@Override
	public void deleteChatContent(long chatId) {
        List<ChatContent> contentList = this.commonDao.selectList("select * from t_chat_content where chat_id = ? ", ChatContent.class, chatId);
        if(contentList == null || contentList.isEmpty()) return;
        for(ChatContent content : contentList){
            this.commonDao.delete(content.getId(),ChatContent.class);
        }
	}

	@Override
	public void updateChatContent(ChatContent content) {
		this.put2EntityCache(content);
		if(ServerConfig.immediateSave) {
			this.commonDao.update(content);
		} else {
			this.dbService.submitUpdate2Queue(content);
		}
	}

	@Override
	public List<ChatContent> selectChatContent(long chatId) {
		String sql = "select a.*,b.nickname as player_name,b.head_img as player_img from t_chat_content a,user b where a.player_id = b.id and chat_id = ? order by chat_time asc ";
		return this.commonDao.selectList(sql, ChatContent.class, chatId);
	}

}
