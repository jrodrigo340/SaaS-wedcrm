package com.wedcrm.mapper;

import com.wedcrm.dto.request.NotificationRequestDTO;
import com.wedcrm.dto.response.NotificationResponseDTO;
import com.wedcrm.entity.Notification;
import com.wedcrm.entity.User;
import com.wedcrm.repository.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {java.time.LocalDateTime.class})
public abstract class NotificationMapper {

    @Autowired
    protected UserRepository userRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", expression = "java(getUser(request.userId()))")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", constant = "true")
    public abstract Notification toEntity(NotificationRequestDTO request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "icon", expression = "java(notification.getIcon())")
    @Mapping(target = "typeColor", expression = "java(notification.getTypeColor())")
    @Mapping(target = "timeAgo", expression = "java(notification.getTimeAgo())")
    public abstract NotificationResponseDTO toResponseDTO(Notification notification);

    protected User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));
    }
}