-- =============================================================================
-- 시연용 데이터 삽입 스크립트 (처음부터 구성)
-- 실행 전: 스키마는 JPA ddl-auto 또는 schema.sql 로 생성된 상태를 가정합니다.
-- 용도: 한 계정으로 관람객·공연자·호스트 전 역할 시연
--
-- 로그인: demo@test.com / password123
-- =============================================================================

SET @pwd_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- -----------------------------------------------------------------------------
-- 1. 회원 1명
-- -----------------------------------------------------------------------------
INSERT INTO users (email, password_hash, nickname, role, status, agree_terms, agree_privacy, agree_marketing, agreed_at, created_at, updated_at, is_deleted) VALUES
('demo@test.com', @pwd_hash, '시연유저', 'USER', 'ACTIVE', 1, 1, 0, NOW(), NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 2. 관람객 프로필 (user_profile)
-- -----------------------------------------------------------------------------
INSERT INTO user_profile (user_id, is_initialized, profile_image_url, birth_date, phone_number, location, preferred_genres, preferred_performance_types, introduction, created_at, updated_at, is_deleted) VALUES
(1, 1, NULL, '1990-01-15', '010-0000-0000', '서울', 'MUSICAL,PLAY', 'SEATING', '시연용 관람객 프로필', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 3. 공연자 프로필 (performer_profile)
-- -----------------------------------------------------------------------------
INSERT INTO performer_profile (user_id, stage_name, profile_image_url, categories, description, activity_area, position, skill_level, initialized, created_at, updated_at, is_deleted) VALUES
(1, '시연공연자', NULL, 'MUSICAL,PLAY', '시연용 공연자 프로필', '서울', 'ACTOR,SINGER', 'INTERMEDIATE', 1, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 4. 호스트 프로필 (host_profile)
-- -----------------------------------------------------------------------------
INSERT INTO host_profile (user_id, organization_name, representative_name, business_number, opening_date, contact_number, business_email, profile_image_url, is_verified, is_initialized, created_at, updated_at, is_deleted) VALUES
(1, '(주)시연공연장', '시연대표', '1234567890', '20200101', '02-1234-5678', 'demo@venue.com', NULL, 1, 1, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 5. 공연장 1곳
-- -----------------------------------------------------------------------------
INSERT INTO venue (host_id, venue_name, address, contact, description, venue_image, venue_type, total_seats, open_time, close_time, booking_unit, rental_fee, facilities, regular_closing, temporary_closing, created_at, updated_at, is_deleted) VALUES
(1, '시연 소극장', '서울시 강남구 시연로 1', '02-1111-2222', '시연용 소규모 공연장.', NULL, 'THEATER', 80, '09:00', '22:00', 60, 50000, '주차,대기실', 'SUNDAY', NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 6. 좌석 (seat) — 좌석 리뷰 시연용
-- -----------------------------------------------------------------------------
INSERT INTO seat (venue_id, seat_floor, x_pos, y_pos, seat_number, seat_type, created_at, updated_at, is_deleted) VALUES
(1, 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
(1, 1, 30, 20, 'A-2', 'vip', NOW(), NOW(), 0),
(1, 1, 50, 20, 'B-1', 'r', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 7. 공연 2개 (본 공연 1 + 찜 1 시연용)
-- -----------------------------------------------------------------------------
INSERT INTO performance (venue_id, host_creator_id, performer_creator_id, title, description, performance_image_url, recruit_status, capacity, category, status, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, '시연 뮤지컬', '시연용 뮤지컬 공연입니다.', NULL, 'CLOSED', 80, 'MUSICAL', 'ENDED', NOW(), NOW(), 0),
(1, 1, 1, '시연 연극', '시연용 연극 공연입니다.', NULL, 'OPEN', 80, 'PLAY', 'UPCOMING', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 8. 공연 일정 (performance_schedule)
-- -----------------------------------------------------------------------------
INSERT INTO performance_schedule (performance_id, start_time, end_time, created_at, updated_at, is_deleted) VALUES
(1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
(2, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 9. 리뷰 — 공연 리뷰 1건, 좌석 리뷰 1건
-- -----------------------------------------------------------------------------
INSERT INTO review (user_id, performance_id, seat_id, rating, content, encore_pick, created_at, updated_at, is_deleted) VALUES
(1, 1, NULL, 5, '시연용 공연 리뷰입니다. 매우 만족스러웠어요.', '마지막 넘버가 인상 깊었어요', NOW(), NOW(), 0),
(1, 1, 1, 5, 'A-1 자리 시야·음향 좋아요.', NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 10. 대관 예약 (venue_reservation) 1건
-- -----------------------------------------------------------------------------
INSERT INTO venue_reservation (venue_id, host_id, performer_id, start_at, end_at, status, message, reject_reason, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 3 HOUR), 'APPROVED', '시연용 대관 신청', NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 11. 본 공연 / 찜 — "내가 본 공연", "내가 찜한 공연" 시연용
-- -----------------------------------------------------------------------------
INSERT INTO user_performance_relation (user_id, performance_id, status, watched_at, created_at, updated_at, is_deleted) VALUES
(1, 1, 'WATCHED', NOW(), NOW(), NOW(), 0),
(1, 2, 'WISHED', NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 12. 모집 게시글 1건 (선택)
-- -----------------------------------------------------------------------------
INSERT INTO post (performance_id, host_author_id, performer_author_id, capacity, post_type, title, content, view_count, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, 5, 'PERFORMANCE_RECRUIT', '시연용 배우 모집', '주연·조연 모집합니다.', 0, NOW(), NOW(), 0);

-- =============================================================================
-- 시연 계정: demo@test.com / password123
-- /profiles/select 에서 관람객·공연자·주최자 전환 후 각 기능 시연
-- =============================================================================
