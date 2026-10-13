package com.sports.common.dto;

import com.sports.common.domain.Notification;
import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        Long recipientUserId,
        String title,
        String message,
        String type,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationDto fromEntity(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
