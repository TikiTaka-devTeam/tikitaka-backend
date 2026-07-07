package com.tikitaka.backend.notification.entity;

public enum NotificationType {
    DOCUMENT_UPLOADED, // 문서 업로드 알림
    DOCUMENT_UPDATED, // 문서 수정 알림
    ANSWER_POSTED, // 질문에 대한 답변 작성 알림
    SPACE_NOTIFIED, // 스페이스 공지 알림
    INVITATION_ACCEPTED // 초대 알림
}