package com.quan.diabetes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quan.diabetes.entity.AIMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AIMessageRepository extends JpaRepository<AIMessage, Long> {
    @Query("SELECT m FROM AIMessage m WHERE m.aiConversation.aiConversationId = :conversationId ORDER BY m.time ASC")
    List<AIMessage> findByConversationIdOrderByTimeAsc(@Param("conversationId") String conversationId);

    @Query("SELECT COUNT(m) FROM AIMessage m WHERE m.aiConversation.aiConversationId = :conversationId")
    long countByConversationId(@Param("conversationId") String conversationId);

    @Query("SELECT m FROM AIMessage m WHERE m.aiConversation.aiConversationId = :conversationId AND m.sender = :sender ORDER BY m.time ASC")
    List<AIMessage> findByConversationIdAndSender(@Param("conversationId") String conversationId, @Param("sender") String sender);

    @Query("SELECT m FROM AIMessage m WHERE m.content LIKE %:keyword%")
    List<AIMessage> searchByContent(@Param("keyword") String keyword);

    List<AIMessage> findTop20ByAiConversation_AiConversationIdOrderByTimeDesc(String conversationId);
}

