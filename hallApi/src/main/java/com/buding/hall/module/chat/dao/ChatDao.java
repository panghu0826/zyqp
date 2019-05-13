package com.buding.hall.module.chat.dao;

import com.buding.db.model.Chat;
import com.buding.db.model.ChatContent;

import java.util.List;

public interface ChatDao {
    long insertChat(Chat chat);

    void deleteChat(long chatId);

    void updateChat(Chat chat);

    Chat selectChat(long chatId);

    List<Chat> selectAllPrivateChatList(int userId);

    Chat selectChatList(int user1Id, int user2Id);

    Chat selectClubChat(long clubId);

    void insertChatContent(ChatContent content);

    void deleteUserChatContent(long chatId, int playerId, long chatTime);

    void deleteChatContent(long chatId);

    void updateChatContent(ChatContent content);

    List<ChatContent> selectChatContent(long chatId);

}
