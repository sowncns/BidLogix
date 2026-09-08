package com.qs.Backend.modules.notification.notification.service;

import com.qs.Backend.modules.notification.notification.dto.*;
import com.qs.Backend.modules.notification.notification.entity.Notification;
import com.qs.Backend.modules.notification.notification.repository.NotificationRepository;
import com.qs.Backend.shared.exception.AppException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse create(NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setUserId(required(request.getUserId(), "user_id is required"));
        notification.setType(required(request.getType(), "type is required"));
        notification.setParams(request.getParams() == null ? new LinkedHashMap<>() : request.getParams());
        notification.setLink(blankToNull(request.getLink()));
        notification.setData(request.getData());
        notification.setCreatedAt(Instant.now());
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public NotificationListResponse list(String userId, boolean unread, int limit, String cursor) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        var page = notificationRepository.findAll(spec(userId, unread, cursor), PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<NotificationResponse> items = page.getContent().stream().map(this::toResponse).toList();
        String nextCursor = items.size() == safeLimit ? items.get(items.size() - 1).getCreatedAt().toString() : null;
        return NotificationListResponse.builder().items(items).nextCursor(nextCursor).build();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(String userId) {
        return UnreadCountResponse.builder().count(notificationRepository.countByUserIdAndReadAtIsNull(userId)).build();
    }

    @Transactional
    public void markRead(String id, String userId) {
        Notification notification = findOwned(id, userId);
        if (notification.getReadAt() == null) notification.setReadAt(Instant.now());
    }

    @Transactional
    public void markAllRead(String userId) {
        Instant now = Instant.now();
        notificationRepository.findAll(spec(userId, true, null)).forEach(n -> n.setReadAt(now));
    }

    @Transactional
    public void delete(String id, String userId) {
        notificationRepository.delete(findOwned(id, userId));
    }

    @Transactional
    public void deleteAll(String userId) {
        notificationRepository.deleteByUserId(userId);
    }

    private Specification<Notification> spec(String userId, boolean unread, String cursor) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            if (unread) predicates.add(cb.isNull(root.get("readAt")));
            if (cursor != null && !cursor.isBlank()) predicates.add(cb.lessThan(root.get("createdAt"), Instant.parse(cursor.trim())));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Notification findOwned(String id, String userId) {
        return notificationRepository.findById(id).filter(n -> n.getUserId().equals(userId)).orElseThrow(() -> new AppException("Notification not found", HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder().id(notification.getId()).type(notification.getType()).params(notification.getParams()).link(notification.getLink()).data(notification.getData()).readAt(notification.getReadAt()).createdAt(notification.getCreatedAt()).build();
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) throw new AppException(message, HttpStatus.BAD_REQUEST, "NOTIFICATION_INVALID_REQUEST");
        return value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
