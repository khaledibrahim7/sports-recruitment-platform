package com.sports.common.service;

import com.sports.common.dto.NotificationDto;
import java.util.List;

public interface NotificationService {
    NotificationDto createNotification(Long recipientUserId, String title, String message, String type);
    List<NotificationDto> getUserNotifications(Long recipientUserId);
    long getUnreadCount(Long recipientUserId);
    void markAsRead(Long notificationId, Long recipientUserId);
    void markAllAsRead(Long recipientUserId);
}
