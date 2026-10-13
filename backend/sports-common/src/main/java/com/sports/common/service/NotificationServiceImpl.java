package com.sports.common.service;

import com.sports.common.domain.Notification;
import com.sports.common.dto.NotificationDto;
import com.sports.common.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public NotificationDto createNotification(Long recipientUserId, String title, String message, String type) {
        Notification notification = new Notification(recipientUserId, title, message, type);
        Notification saved = notificationRepository.save(notification);
        return NotificationDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long recipientUserId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientUserId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(recipientUserId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long recipientUserId) {
        notificationRepository.findById(notificationId).ifPresent(notif -> {
            if (notif.getRecipientUserId().equals(recipientUserId)) {
                notif.setRead(true);
                notificationRepository.save(notif);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long recipientUserId) {
        List<Notification> notifications = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
