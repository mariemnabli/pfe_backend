package com.example.telecom.service;

import com.example.telecom.dto.NotificationDTO;
import com.example.telecom.entity.Notification;
import com.example.telecom.entity.Role;
import com.example.telecom.entity.User;
import com.example.telecom.repository.NotificationRepository;
import com.example.telecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final long SSE_TIMEOUT = 0L;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        User user = getUserByEmail(email);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(user.getId(), ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(user.getId(), emitter));
        emitter.onTimeout(() -> removeEmitter(user.getId(), emitter));
        emitter.onError(error -> removeEmitter(user.getId(), emitter));

        sendEvent(emitter, "connected", Map.of(
                "userId", user.getId(),
                "unreadCount", countUnread(user.getId())
        ));

        return emitter;
    }

    public List<NotificationDTO> getNotifications(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    public long countUnread(String email) {
        return countUnread(getUserByEmail(email).getId());
    }

    @Transactional
    public NotificationDTO markAsRead(String email, Long notificationId) {
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.getId())
                .orElseThrow(() -> new RuntimeException("Notification introuvable : " + notificationId));
        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        broadcastUnreadCount(user.getId());
        return toDTO(saved);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = getUserByEmail(email);
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        boolean updated = false;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                updated = true;
            }
        }
        if (updated) {
            notificationRepository.saveAll(notifications);
        }
        broadcastUnreadCount(user.getId());
    }

    @Transactional
    public void notifyRole(Role role, String type, String title, String message, String resourceType, Long resourceId) {
        userRepository.findAllByRole(role).forEach(user ->
                createAndDispatch(user, type, title, message, resourceType, resourceId));
    }

    @Transactional
    public void notifyUser(User user, String type, String title, String message, String resourceType, Long resourceId) {
        if (user == null) {
            return;
        }
        createAndDispatch(user, type, title, message, resourceType, resourceId);
    }

    private void createAndDispatch(User recipient, String type, String title, String message, String resourceType, Long resourceId) {
        Notification saved = notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .build());

        NotificationDTO dto = toDTO(saved);
        emitters.getOrDefault(recipient.getId(), new CopyOnWriteArrayList<>())
                .forEach(emitter -> sendEvent(emitter, "notification", dto, recipient.getId()));
        broadcastUnreadCount(recipient.getId());
    }

    private void broadcastUnreadCount(Long userId) {
        long unreadCount = countUnread(userId);
        emitters.getOrDefault(userId, new CopyOnWriteArrayList<>())
                .forEach(emitter -> sendEvent(emitter, "unread-count", Map.of("unreadCount", unreadCount), userId));
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        sendEvent(emitter, eventName, data, null);
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data, Long userId) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException exception) {
            if (userId != null) {
                removeEmitter(userId, emitter);
            }
            emitter.completeWithError(exception);
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }

    private long countUnread(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    private User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
