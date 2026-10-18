package com.springboot_project.walkytalky.MESSAGE_DETAILS.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot_project.walkytalky.MESSAGE_DETAILS.Entity.MessageEntity;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    // Fetch messages for a channel ordered by creation time, eagerly fetching sender details
    @EntityGraph(attributePaths = {"sender"})
    Page<MessageEntity> findByChannel_ChannelIdOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    void deleteByChannelChannelId(Long channelId);
}
