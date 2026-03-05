-- =============================================================================
-- 테스트용 데이터 삽입 스크립트 (총정리)
-- 실행 전: 스키마는 JPA ddl-auto 또는 별도 schema.sql 로 생성된 상태 가정
-- 비밀번호: 모든 계정 공통 "password123" (BCrypt)
--
-- 규칙: user 1명당 user_profile, performer_profile, host_profile 각 1개씩 필수
-- 공연: category=장르(MUSICAL/PLAY/BAND), status=진행상태(UPCOMING/ENDED 등), recruit_status=모집상태
-- 이미지: INSERT 시에는 NULL. 실제 사진 넣을 때는 아래 "테스트 이미지 경로 적용" 참고.
-- =============================================================================

SET @pwd_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- -----------------------------------------------------------------------------
-- 1. 회원 (users)
-- -----------------------------------------------------------------------------
INSERT INTO users (email, password_hash, nickname, role, status, agree_terms, agree_privacy, agree_marketing, agreed_at, created_at, updated_at, is_deleted) VALUES
('user1@test.com',    @pwd_hash, '관람객유저',   'USER', 'ACTIVE', 1, 1, 0, NOW(), NOW(), NOW(), 0),
('performer1@test.com', @pwd_hash, '공연자일',   'USER', 'ACTIVE', 1, 1, 0, NOW(), NOW(), NOW(), 0),
('host1@test.com',    @pwd_hash, '호스트일',    'USER', 'ACTIVE', 1, 1, 0, NOW(), NOW(), NOW(), 0),
('user2@test.com',    @pwd_hash, '관람객둘',   'USER', 'ACTIVE', 1, 1, 0, NOW(), NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 2. 유저 프로필 (user_profile) - 관람객 모드용
-- -----------------------------------------------------------------------------
INSERT INTO user_profile (user_id, is_initialized, profile_image_url, birth_date, phone_number, location, preferred_genres, preferred_performance_types, introduction, created_at, updated_at, is_deleted) VALUES
(1, 1, NULL, '1990-01-15', '010-1111-1111', '서울', 'MUSICAL,PLAY', 'SEATING', '뮤지컬 좋아해요', NOW(), NOW(), 0),
(2, 1, NULL, '1988-03-10', '010-3333-3333', '서울', 'MUSICAL,PLAY', 'SEATING', '공연자이자 관람객', NOW(), NOW(), 0),
(3, 1, NULL, '1985-07-01', '010-4444-4444', '경기', 'PLAY', 'SEATING', '호스트이자 관람객', NOW(), NOW(), 0),
(4, 1, NULL, '1995-05-20', '010-2222-2222', '경기', 'BAND', 'STANDING', '밴드 공연 자주 봐요', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 3. 공연자 프로필 (performer_profile) - 공연자 모드용
-- -----------------------------------------------------------------------------
INSERT INTO performer_profile (user_id, stage_name, profile_image_url, categories, description, activity_area, position, skill_level, initialized, created_at, updated_at, is_deleted) VALUES
(2, '스테이지공연자', NULL, 'MUSICAL,PLAY', '뮤지컬·연극 배우', '서울', 'ACTOR,SINGER', 'INTERMEDIATE', 1, NOW(), NOW(), 0),
(1, '유저일공연자', NULL, 'BAND', '취미 밴드', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(3, '호스트공연자', NULL, 'PLAY', '연극 단원', '경기', 'ACTOR', 'ADVANCED', 1, NOW(), NOW(), 0),
(4, '관람객둘공연자', NULL, 'MUSICAL', '뮤지컬 지망', '경기', 'SINGER', 'INTERMEDIATE', 1, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 4. 호스트 프로필 (host_profile) - 호스트 모드용
-- -----------------------------------------------------------------------------
INSERT INTO host_profile (user_id, organization_name, representative_name, business_number, opening_date, contact_number, business_email, profile_image_url, is_verified, is_initialized, created_at, updated_at, is_deleted) VALUES
(3, '(주)테스트공연장', '홍길동', '1234567890', '20200101', '02-1234-5678', 'host@venue.com', NULL, 1, 1, NOW(), NOW(), 0),
(1, '(주)테스트호스트1', '김대표', '1234567891', '20210101', '02-1111-1111', 'host1@test.com', NULL, 1, 1, NOW(), NOW(), 0),
(2, '(주)테스트호스트2', '이대표', '1234567892', '20210201', '02-2222-2222', 'host2@test.com', NULL, 1, 1, NOW(), NOW(), 0),
(4, '(주)테스트호스트4', '박대표', '1234567893', '20210301', '02-3333-3333', 'host4@test.com', NULL, 1, 1, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 5. 공연장 (venue)
-- -----------------------------------------------------------------------------
INSERT INTO venue (host_id, venue_name, address, contact, description, venue_image, venue_type, total_seats, open_time, close_time, booking_unit, rental_fee, facilities, regular_closing, temporary_closing, created_at, updated_at, is_deleted) VALUES
(1, '테스트 소극장', '서울시 강남구 테스트로 1', '02-1111-2222', '소규모 공연 전문 공연장입니다.', NULL, 'THEATER', 80, '09:00', '22:00', 60, 50000, '주차,대기실', 'SUNDAY', NULL, NOW(), NOW(), 0),
(1, '테스트 콘서트홀', '서울시 마포구 공연로 2', '02-3333-4444', '중규모 콘서트홀.', NULL, 'CONCERT_HALL', 200, '10:00', '23:00', 120, 150000, '주차,록커룸', NULL, NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 6. 좌석 (seat)
-- -----------------------------------------------------------------------------
INSERT INTO seat (venue_id, seat_floor, x_pos, y_pos, seat_number, seat_type, created_at, updated_at, is_deleted) VALUES
(1, 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
(1, 1, 30, 20, 'A-2', 'vip', NOW(), NOW(), 0),
(1, 1, 50, 20, 'B-1', 'r', NOW(), NOW(), 0),
(1, 1, 70, 20, 'B-2', 'r', NOW(), NOW(), 0),
(1, 2, 10, 40, 'C-1', 's', NOW(), NOW(), 0),
(2, 1, 20, 30, '1-A', 'vip', NOW(), NOW(), 0),
(2, 1, 40, 30, '1-B', 'r', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 7. 공연 (performance)
--   category = 장르(MUSICAL/PLAY/BAND), status = 진행상태(UPCOMING/ENDED 등)
--   performance_image_url = 공연 포스터/대표 이미지 (NULL 가능, 이미지 넣을 때 UPDATE 사용)
-- -----------------------------------------------------------------------------
INSERT INTO performance (venue_id, host_creator_id, performer_creator_id, title, description, performance_image_url, recruit_status, capacity, category, status, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, '테스트 뮤지컬 - 첫공연', '테스트용 뮤지컬 공연입니다.', NULL, 'CLOSED', 80, 'MUSICAL', 'ENDED', NOW(), NOW(), 0),
(1, 1, NULL, '테스트 연극 - 두번째', '연극 공연 테스트.', NULL, 'OPEN', 80, 'PLAY', 'UPCOMING', NOW(), NOW(), 0),
(2, 1, 1, '테스트 밴드 공연', '밴드 공연 테스트.', NULL, 'OPEN', 200, 'BAND', 'UPCOMING', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 7-1. 공연 일정 (performance_schedule) - 북마크 공연 알림용
-- -----------------------------------------------------------------------------
INSERT INTO performance_schedule (performance_id, start_time, end_time, created_at, updated_at, is_deleted) VALUES
(1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
(2, DATE_ADD(NOW(), INTERVAL 25 MINUTE), DATE_ADD(NOW(), INTERVAL 25 MINUTE) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
(3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 8. 리뷰 (review) - 공연 리뷰(seat_id=NULL) / 좌석 리뷰(seat_id 있음)
--   encore_pick: 공연 리뷰만 사용, 좌석 리뷰는 NULL
-- -----------------------------------------------------------------------------
INSERT INTO review (user_id, performance_id, seat_id, rating, content, encore_pick, created_at, updated_at, is_deleted) VALUES
(1, 1, NULL, 5, '공연 너무 좋았어요! 감동이었습니다. 강력 추천합니다.', '마지막 넘버가 인상 깊었어요', NOW(), NOW(), 0),
(4, 1, NULL, 4, '전체적으로 만족스러운 공연이었어요. 조금만 더 길었으면.', NULL, NOW(), NOW(), 0),
(1, 1, 1, 5, 'A-1 자리 시야 완벽하고 음향도 좋아요. vip 강추.', NULL, NOW(), NOW(), 0),
(1, 1, 3, 4, 'B-1에서 봤는데 가성비 좋은 자리였어요.', NULL, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 9. 대관 예약 (venue_reservation)
-- -----------------------------------------------------------------------------
INSERT INTO venue_reservation (venue_id, host_id, performer_id, start_at, end_at, status, message, reject_reason, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 7 DAY), INTERVAL 3 HOUR), 'PENDING', '소규모 공연 대관 신청합니다.', NULL, NOW(), NOW(), 0),
(1, 1, 1, DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 14 DAY), INTERVAL 2 HOUR), 'APPROVED', '리허설 및 본 공연용.', NULL, NOW(), NOW(), 0),
(2, 1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 4 HOUR), 'REJECTED', '콘서트 대관 요청', '해당 일정 이미 예약됨', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 10. 게시글 (post)
-- -----------------------------------------------------------------------------
INSERT INTO post (performance_id, host_author_id, performer_author_id, capacity, post_type, title, content, view_count, created_at, updated_at, is_deleted) VALUES
(1, 1, 1, 5, 'PERFORMANCE_RECRUIT', '뮤지컬 첫공연 배우 모집', '주연·조연 모집합니다. 오디션 일정 추후 공지.', 10, NOW(), NOW(), 0),
(2, NULL, 1, 3, 'PERFORMER_RECRUIT', '연극 단역 공연자 구해요', '단역 3명 구합니다. 관심 있으신 분 연락 주세요.', 5, NOW(), NOW(), 0),
(3, 1, 1, 10, 'PERFORMANCE_RECRUIT', '밴드 공연 세션 멤버 모집', '드럼, 베이스 구합니다.', 0, NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 11. 게시글 신청 (post_interaction)
-- -----------------------------------------------------------------------------
INSERT INTO post_interaction (post_id, applicant_performer_id, target_performer_id, sender_performer_id, interaction_type, status, message, created_at, updated_at, is_deleted) VALUES
(1, 1, NULL, NULL, 'APPLY', 'PENDING', '지원합니다. 오디션 참가 희망해요.', NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 12. 유저-공연 관계 (user_performance_relation) - WATCHED(본공연) / WISHED(찜)
-- -----------------------------------------------------------------------------
INSERT INTO user_performance_relation (user_id, performance_id, status, watched_at, created_at, updated_at, is_deleted) VALUES
(1, 1, 'WATCHED', NOW(), NOW(), NOW(), 0),
(1, 2, 'WISHED', NULL, NOW(), NOW(), 0),
(4, 1, 'WATCHED', NOW(), NOW(), NOW(), 0);

-- -----------------------------------------------------------------------------
-- 13. 유저 관계 (user_relation) - 팔로우/차단
-- -----------------------------------------------------------------------------
INSERT INTO user_relation (actor_id, actor_profile_mode, relation_type, target_type, target_id, target_profile_mode, created_at, updated_at, is_deleted) VALUES
(1, 'ROLE_USER', 'FOLLOW', 'USER', 2, 'ROLE_PERFORMER', NOW(), NOW(), 0);

-- =============================================================================
-- 테스트 계정 (로그인: password123)
-- =============================================================================
-- user1@test.com      → 관람객   | performer1@test.com → 공연자 | host1@test.com → 호스트 | user2@test.com → 관람객2
-- 로그인 후 /profiles/select 에서 프로필 전환.
-- =============================================================================

-- =============================================================================
-- [선택] 테스트 이미지 적용 - 파일명만 맞추면 적용 가능
-- =============================================================================
-- 1. src/main/resources/static/image/test/ 폴더에 아래 파일명으로 이미지 넣기
-- 2. 아래 UPDATE 블록 주석 해제 후 실행
--
-- 사용 파일명:
--   user_profile      → profile1.jpg ~ profile4.jpg (user_id 1~4)
--   performer_profile → performer1.jpg(user_id=2), performer2.jpg(user_id=1)
--   host_profile      → host1.jpg (user_id=3)
--   venue             → venue1.jpg(venue_id=1), venue2.jpg(venue_id=2)
--   performance(공연)  → performance1.jpg(performance_id=1), performance2.jpg(2), performance3.jpg(3)
-- =============================================================================
/*
UPDATE user_profile SET profile_image_url = '/image/test/profile1.jpg' WHERE user_id = 1;
UPDATE user_profile SET profile_image_url = '/image/test/profile2.jpg' WHERE user_id = 2;
UPDATE user_profile SET profile_image_url = '/image/test/profile3.jpg' WHERE user_id = 3;
UPDATE user_profile SET profile_image_url = '/image/test/profile4.jpg' WHERE user_id = 4;
UPDATE performer_profile SET profile_image_url = '/image/test/performer1.jpg' WHERE user_id = 2;
UPDATE performer_profile SET profile_image_url = '/image/test/performer2.jpg' WHERE user_id = 1;
UPDATE host_profile SET profile_image_url = '/image/test/host1.jpg' WHERE user_id = 3;
UPDATE venue SET venue_image = '/image/test/venue1.jpg' WHERE venue_id = 1;
UPDATE venue SET venue_image = '/image/test/venue2.jpg' WHERE venue_id = 2;
UPDATE performance SET performance_image_url = '/image/test/performance1.jpg' WHERE performance_id = 1;
UPDATE performance SET performance_image_url = '/image/test/performance2.jpg' WHERE performance_id = 2;
UPDATE performance SET performance_image_url = '/image/test/performance3.jpg' WHERE performance_id = 3;
*/

-- -----------------------------------------------------------------------------
-- [비밀번호] password123 로그인 안 되면: 앱 실행 → /dev/bcrypt-password123 에서 해시 복사 후 아래 주석 해제 실행
-- -----------------------------------------------------------------------------
-- UPDATE users SET password_hash = '여기에_복사한_해시' WHERE email IN ('user1@test.com','performer1@test.com','host1@test.com','user2@test.com');

-- -----------------------------------------------------------------------------
-- [오류 시] PerformanceStatus.MUSICAL 등 enum 오류: category/status 뒤바뀜일 수 있음. 아래 실행 후 7·7-1 INSERT 재실행
-- -----------------------------------------------------------------------------
-- DELETE FROM performance_schedule;
-- DELETE FROM performance;
