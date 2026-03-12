package com.wedcrm.repository;

import com.wedcrm.entity.MessageTemplate;
import com.wedcrm.enums.MessageChannel;
import com.wedcrm.enums.TemplateCategory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, UUID> {

    List<MessageTemplate> findByChannelAndIsActiveTrue(MessageChannel channel);

    List<MessageTemplate> findByCategoryAndChannelAndIsActiveTrue(
            TemplateCategory category,
            MessageChannel channel
    );

    List<MessageTemplate> findAllByOrderByUsageCountDesc(Pageable pageable);

}