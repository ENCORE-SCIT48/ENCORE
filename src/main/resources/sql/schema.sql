-- =============================================================================
-- ENCORE 테이블 생성 스크립트 (MySQL 8.x)
-- JPA ddl-auto 대신 수동 스키마 관리 시 사용. 실행 전 스키마 생성 권장.
-- 실행 순서: 1) DB/스키마 생성 2) 본 파일 실행 3) data-test-insert.sql (선택)
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. 회원 (users)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id         BIGINT       NOT NULL AUTO_INCREMENT,
    email           VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    nickname        VARCHAR(50)  NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    agree_terms     TINYINT(1)   NOT NULL DEFAULT 1,
    agree_privacy   TINYINT(1)   NOT NULL DEFAULT 1,
    agree_marketing TINYINT(1)   NOT NULL DEFAULT 0,
    agreed_at       DATETIME(6)  NULL,
    created_at      DATETIME(6)  NULL,
    updated_at      DATETIME(6)  NULL,
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. 관람객 프로필 (user_profile)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
    profile_id                  BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                     BIGINT       NOT NULL,
    is_initialized              TINYINT(1)   NOT NULL DEFAULT 0,
    profile_image_url           VARCHAR(512) NULL,
    birth_date                  DATE         NULL,
    phone_number                VARCHAR(50)  NULL,
    location                    VARCHAR(100) NULL,
    preferred_genres            VARCHAR(255) NULL,
    preferred_performance_types  VARCHAR(255) NULL,
    introduction                TEXT         NULL,
    created_at                  DATETIME(6)  NULL,
    updated_at                  DATETIME(6)  NULL,
    is_deleted                  TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (profile_id),
    UNIQUE KEY uk_user_profile_user_id (user_id),
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. 공연자 프로필 (performer_profile)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS performer_profile;
CREATE TABLE performer_profile (
    performer_id      BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    stage_name        VARCHAR(100) NULL,
    profile_image_url VARCHAR(512) NULL,
    categories        VARCHAR(255) NULL,
    description       TEXT         NULL,
    activity_area     VARCHAR(100) NULL,
    position          VARCHAR(50)  NULL,
    skill_level       VARCHAR(20)  NULL,
    initialized       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at        DATETIME(6)  NULL,
    updated_at        DATETIME(6)  NULL,
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (performer_id),
    UNIQUE KEY uk_performer_profile_user_id (user_id),
    CONSTRAINT fk_performer_profile_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. 호스트 프로필 (host_profile)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS host_profile;
CREATE TABLE host_profile (
    host_id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id             BIGINT       NOT NULL,
    organization_name   VARCHAR(100) NULL,
    representative_name VARCHAR(50)  NULL,
    business_number     VARCHAR(20)  NULL,
    opening_date        VARCHAR(10)  NULL,
    contact_number      VARCHAR(20)  NULL,
    business_email      VARCHAR(100) NULL,
    profile_image_url   VARCHAR(512) NULL,
    is_verified         TINYINT(1)   NOT NULL DEFAULT 0,
    is_initialized       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at           DATETIME(6)  NULL,
    updated_at           DATETIME(6)  NULL,
    is_deleted           TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (host_id),
    UNIQUE KEY uk_host_profile_user_id (user_id),
    UNIQUE KEY uk_host_profile_business_number (business_number),
    CONSTRAINT fk_host_profile_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. 공연장 (venue)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS venue;
CREATE TABLE venue (
    venue_id          BIGINT       NOT NULL AUTO_INCREMENT,
    host_id           BIGINT       NOT NULL,
    venue_name        VARCHAR(255) NOT NULL,
    address           VARCHAR(255) NOT NULL,
    contact           VARCHAR(100) NULL,
    description       VARCHAR(500) NULL,
    venue_image       VARCHAR(512) NULL,
    venue_type        VARCHAR(50)  NULL,
    total_seats       INT          NULL,
    open_time         VARCHAR(10)  NULL,
    close_time        VARCHAR(10)  NULL,
    booking_unit      INT          NULL,
    rental_fee        INT          NULL,
    facilities        VARCHAR(255) NULL,
    regular_closing   VARCHAR(100) NULL,
    temporary_closing TEXT         NULL,
    created_at        DATETIME(6)  NULL,
    updated_at        DATETIME(6)  NULL,
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (venue_id),
    CONSTRAINT fk_venue_host FOREIGN KEY (host_id) REFERENCES host_profile (host_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. 좌석 (seat)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS seat;
CREATE TABLE seat (
    seat_id     BIGINT       NOT NULL AUTO_INCREMENT,
    venue_id    BIGINT       NOT NULL,
    seat_floor  INT          NULL,
    x_pos       INT          NULL,
    y_pos       INT          NULL,
    seat_number VARCHAR(50)  NULL,
    seat_type   VARCHAR(20)  NULL,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (seat_id),
    CONSTRAINT fk_seat_venue FOREIGN KEY (venue_id) REFERENCES venue (venue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. 공연 (performance)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS performance;
CREATE TABLE performance (
    performance_id        BIGINT       NOT NULL AUTO_INCREMENT,
    venue_id              BIGINT       NULL,
    host_creator_id       BIGINT       NULL,
    performer_creator_id  BIGINT       NULL,
    title                 VARCHAR(255) NULL,
    description           TEXT         NULL,
    performance_image_url VARCHAR(512) NULL,
    recruit_status        VARCHAR(20)  NULL,
    capacity              INT          NULL,
    category              VARCHAR(20)  NULL,
    status                VARCHAR(20)  NULL,
    created_at             DATETIME(6)  NULL,
    updated_at            DATETIME(6)  NULL,
    is_deleted            TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (performance_id),
    CONSTRAINT fk_performance_venue    FOREIGN KEY (venue_id)             REFERENCES venue (venue_id),
    CONSTRAINT fk_performance_host     FOREIGN KEY (host_creator_id)      REFERENCES host_profile (host_id),
    CONSTRAINT fk_performance_performer FOREIGN KEY (performer_creator_id) REFERENCES performer_profile (performer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. 공연 일정 (performance_schedule)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS performance_schedule;
CREATE TABLE performance_schedule (
    schedule_id     BIGINT      NOT NULL AUTO_INCREMENT,
    performance_id  BIGINT      NOT NULL,
    start_time      DATETIME(6) NULL,
    end_time        DATETIME(6) NULL,
    created_at      DATETIME(6) NULL,
    updated_at      DATETIME(6) NULL,
    is_deleted      TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (schedule_id),
    CONSTRAINT fk_performance_schedule_performance FOREIGN KEY (performance_id) REFERENCES performance (performance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. 리뷰 (review) — 공연 리뷰(seat_id NULL) / 좌석 리뷰(seat_id 있음)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS review;
CREATE TABLE review (
    review_id      BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    performance_id BIGINT       NOT NULL,
    seat_id        BIGINT       NULL,
    rating         INT          NULL,
    content        TEXT         NULL,
    encore_pick    VARCHAR(200) NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    is_deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (review_id),
    CONSTRAINT fk_review_user        FOREIGN KEY (user_id)        REFERENCES users (user_id),
    CONSTRAINT fk_review_performance  FOREIGN KEY (performance_id) REFERENCES performance (performance_id),
    CONSTRAINT fk_review_seat         FOREIGN KEY (seat_id)        REFERENCES seat (seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. 대관 예약 (venue_reservation)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS venue_reservation;
CREATE TABLE venue_reservation (
    reservation_id BIGINT       NOT NULL AUTO_INCREMENT,
    venue_id       BIGINT       NOT NULL,
    host_id        BIGINT       NOT NULL,
    performer_id   BIGINT       NOT NULL,
    start_at       DATETIME(6)  NOT NULL,
    end_at         DATETIME(6)  NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    message        TEXT         NULL,
    reject_reason  TEXT         NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    is_deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (reservation_id),
    CONSTRAINT fk_venue_reservation_venue    FOREIGN KEY (venue_id)    REFERENCES venue (venue_id),
    CONSTRAINT fk_venue_reservation_host     FOREIGN KEY (host_id)     REFERENCES host_profile (host_id),
    CONSTRAINT fk_venue_reservation_performer FOREIGN KEY (performer_id) REFERENCES performer_profile (performer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 11. 게시글 (post) — 공연/공연자 모집글
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS post;
CREATE TABLE post (
    post_id               BIGINT       NOT NULL AUTO_INCREMENT,
    performance_id        BIGINT       NULL,
    venue_id              BIGINT       NULL,
    recruit_category      VARCHAR(255) NULL,
    recruit_part          VARCHAR(255) NULL,
    recruit_area          VARCHAR(100) NULL,
    host_author_id        BIGINT       NULL,
    performer_author_id   BIGINT       NULL,
    capacity              INT          NOT NULL,
    post_type             VARCHAR(50)  NULL,
    title                 VARCHAR(255) NULL,
    content               TEXT         NULL,
    view_count            INT          NULL DEFAULT 0,
    created_at            DATETIME(6)  NULL,
    updated_at            DATETIME(6)  NULL,
    is_deleted            TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (post_id),
    CONSTRAINT fk_post_performance  FOREIGN KEY (performance_id)      REFERENCES performance (performance_id),
    CONSTRAINT fk_post_venue        FOREIGN KEY (venue_id)          REFERENCES venue (venue_id),
    CONSTRAINT fk_post_host         FOREIGN KEY (host_author_id)      REFERENCES host_profile (host_id),
    CONSTRAINT fk_post_performer    FOREIGN KEY (performer_author_id) REFERENCES performer_profile (performer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 12. 게시글 신청 (post_interaction)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS post_interaction;
CREATE TABLE post_interaction (
    interaction_id           BIGINT       NOT NULL AUTO_INCREMENT,
    post_id                  BIGINT       NOT NULL,
    applicant_performer_id   BIGINT       NOT NULL,
    target_performer_id      BIGINT       NULL,
    sender_performer_id      BIGINT       NULL,
    interaction_type         VARCHAR(50)  NULL,
    status                   VARCHAR(50)  NULL,
    message                  TEXT         NULL,
    created_at               DATETIME(6)  NULL,
    updated_at               DATETIME(6)  NULL,
    is_deleted               TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (interaction_id),
    CONSTRAINT fk_post_interaction_post     FOREIGN KEY (post_id)                REFERENCES post (post_id),
    CONSTRAINT fk_post_interaction_performer FOREIGN KEY (applicant_performer_id) REFERENCES performer_profile (performer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 13. 유저-공연 관계 (user_performance_relation) — WATCHED / WISHED
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS user_performance_relation;
CREATE TABLE user_performance_relation (
    relation_id    BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    performance_id BIGINT       NULL,
    status         VARCHAR(20)  NULL,
    watched_at     DATETIME(6)  NULL,
    created_at     DATETIME(6)  NULL,
    updated_at     DATETIME(6)  NULL,
    is_deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (relation_id),
    CONSTRAINT fk_upr_user        FOREIGN KEY (user_id)        REFERENCES users (user_id),
    CONSTRAINT fk_upr_performance FOREIGN KEY (performance_id)  REFERENCES performance (performance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 14. 유저 관계 (user_relation) — 팔로우/차단
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS user_relation;
CREATE TABLE user_relation (
    relation_id         BIGINT      NOT NULL AUTO_INCREMENT,
    actor_id            BIGINT      NOT NULL,
    actor_profile_mode  VARCHAR(20) NOT NULL,
    relation_type       VARCHAR(20) NOT NULL,
    target_type         VARCHAR(20) NOT NULL,
    target_id           BIGINT      NOT NULL,
    target_profile_mode VARCHAR(20) NULL,
    created_at          DATETIME(6) NULL,
    updated_at          DATETIME(6) NULL,
    is_deleted          TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (relation_id),
    CONSTRAINT fk_user_relation_actor FOREIGN KEY (actor_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 15. 채팅 게시글 (chat_post)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS chat_post;
CREATE TABLE chat_post (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    performance_id  BIGINT       NULL,
    profile_id      BIGINT       NOT NULL,
    profile_mode    VARCHAR(20)  NOT NULL,
    post_type       VARCHAR(30)  NULL,
    title           VARCHAR(255) NULL,
    content         TEXT         NULL,
    max_member      INT          NULL,
    current_member  INT          NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at      DATETIME(6)  NULL,
    updated_at      DATETIME(6)  NULL,
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_post_performance FOREIGN KEY (performance_id) REFERENCES performance (performance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 16. 채팅방 (chat_room)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS chat_room;
CREATE TABLE chat_room (
    room_id    BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NULL,
    room_type  VARCHAR(20) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    is_deleted TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (room_id),
    CONSTRAINT fk_chat_room_post FOREIGN KEY (post_id) REFERENCES chat_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 17. 채팅 참여자 (chat_participant)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS chat_participant;
CREATE TABLE chat_participant (
    participant_id     BIGINT      NOT NULL AUTO_INCREMENT,
    room_id            BIGINT      NOT NULL,
    profile_id         BIGINT      NOT NULL,
    profile_mode       VARCHAR(20) NOT NULL,
    participant_status VARCHAR(20) NOT NULL,
    created_at         DATETIME(6) NULL,
    updated_at         DATETIME(6) NULL,
    is_deleted         TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (participant_id),
    CONSTRAINT fk_chat_participant_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 18. 채팅 메시지 (chat_message)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS chat_message;
CREATE TABLE chat_message (
    message_id  BIGINT       NOT NULL AUTO_INCREMENT,
    room_id     BIGINT       NOT NULL,
    profile_id  BIGINT       NOT NULL,
    profile_mode VARCHAR(20) NOT NULL,
    content     TEXT         NOT NULL,
    sent_at     DATETIME(6)  NULL,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    is_deleted  TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (message_id),
    CONSTRAINT fk_chat_message_room FOREIGN KEY (room_id) REFERENCES chat_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 19. 신고 (report)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS report;
CREATE TABLE report (
    report_id            BIGINT       NOT NULL AUTO_INCREMENT,
    reporter_id          BIGINT       NOT NULL,
    reporter_profile_mode VARCHAR(20) NOT NULL,
    target_id            BIGINT       NOT NULL,
    target_type          VARCHAR(30)  NOT NULL,
    reason_detail        TEXT         NULL,
    reason               VARCHAR(30)  NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at           DATETIME(6)  NULL,
    updated_at           DATETIME(6)  NULL,
    is_deleted           TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (report_id),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 20. 추천 (recommendation)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS recommendation;
CREATE TABLE recommendation (
    recommend_id   BIGINT      NOT NULL AUTO_INCREMENT,
    post_id        BIGINT      NOT NULL,
    recommender_id BIGINT      NOT NULL,
    rank_no        INT         NULL,
    created_at     DATETIME(6) NULL,
    updated_at     DATETIME(6) NULL,
    is_deleted     TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (recommend_id),
    CONSTRAINT fk_recommendation_post      FOREIGN KEY (post_id)        REFERENCES post (post_id),
    CONSTRAINT fk_recommendation_recommender FOREIGN KEY (recommender_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 21. 이메일 인증 (email_verifications)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS email_verifications;
CREATE TABLE email_verifications (
    verification_id BIGINT       NOT NULL AUTO_INCREMENT,
    email            VARCHAR(255) NOT NULL,
    code             VARCHAR(6)   NOT NULL,
    expired_at       DATETIME(6)  NOT NULL,
    verified         TINYINT(1)   NULL DEFAULT 0,
    created_at       DATETIME(6)  NULL,
    updated_at       DATETIME(6)  NULL,
    is_deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (verification_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 22. 알림 설정 (user_notification)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS user_notification;
CREATE TABLE user_notification (
    setting_id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id                BIGINT      NOT NULL,
    profile_mode           VARCHAR(20) NULL,
    performance_start_alert TINYINT(1) NULL,
    dm_alert               TINYINT(1)  NULL,
    created_at             DATETIME(6) NULL,
    updated_at             DATETIME(6) NULL,
    is_deleted             TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (setting_id),
    UNIQUE KEY uk_user_notification_user_id (user_id),
    CONSTRAINT fk_user_notification_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 23. 인증 로그 (auth_log)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS auth_log;
CREATE TABLE auth_log (
    auth_id       BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    login_type    VARCHAR(50)  NULL,
    login_ip      VARCHAR(50)  NULL,
    last_login_at DATETIME(6)  NULL,
    created_at    DATETIME(6)  NULL,
    updated_at    DATETIME(6)  NULL,
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (auth_id),
    CONSTRAINT fk_auth_log_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 24. 공연-공연자 (performance_performer)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS performance_performer;
CREATE TABLE performance_performer (
    performance_performer_id BIGINT       NOT NULL AUTO_INCREMENT,
    performance_id          BIGINT       NOT NULL,
    performer_id            BIGINT       NULL,
    artist_profile_id       BIGINT       NULL,
    role                    VARCHAR(100) NULL,
    confirmed_at            DATETIME(6)  NULL,
    applicant_performer_id  BIGINT       NULL,
    status                  VARCHAR(50)  NULL,
    rank_no                 INT          NULL,
    created_at              DATETIME(6)  NULL,
    updated_at              DATETIME(6)  NULL,
    is_deleted              TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (performance_performer_id),
    CONSTRAINT fk_performance_performer_performance FOREIGN KEY (performance_id) REFERENCES performance (performance_id),
    CONSTRAINT fk_performance_performer_performer    FOREIGN KEY (performer_id)    REFERENCES performer_profile (performer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 끝. 데이터 삽입은 data-test-insert.sql 사용.
-- =============================================================================
