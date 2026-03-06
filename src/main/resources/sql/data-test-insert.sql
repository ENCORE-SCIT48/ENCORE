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
-- [추가] 호스트 41~58, 공연장 18곳, 공연 15개 (좌석수 10~100 임의, 그만큼 좌석 생성·층/등급 골고루)
-- =============================================================================
-- ※ 이 섹션만 실행해도 됨 (기존 1~130 불필요). SET 한 줄 필수.
-- =============================================================================
SET @pwd_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- 호스트 계정 (user_id 41~58, 비밀번호 동일 password123)
INSERT INTO users (user_id, email, password_hash, nickname, role, status, agree_terms, agree_privacy, agree_marketing, agreed_at, created_at, updated_at, is_deleted) VALUES
(41,'host41@test.com',@pwd_hash,'YES24 LIVE HALL','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(42,'host42@test.com',@pwd_hash,'블루스퀘어 마스터카드홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(43,'host43@test.com',@pwd_hash,'예술의전당 콘서트홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(44,'host44@test.com',@pwd_hash,'세종문화회관 대극장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(45,'host45@test.com',@pwd_hash,'롯데콘서트홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(46,'host46@test.com',@pwd_hash,'홍대 롤링홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(47,'host47@test.com',@pwd_hash,'웨스트브릿지 라이브홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(48,'host48@test.com',@pwd_hash,'KT&G 상상마당 라이브홀','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(49,'host49@test.com',@pwd_hash,'노들섬 라이브하우스','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(50,'host50@test.com',@pwd_hash,'고양아람누리 아람극장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(51,'host51@test.com',@pwd_hash,'성남아트센터 오페라하우스','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(52,'host52@test.com',@pwd_hash,'수원 SK아트리움 대공연장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(53,'host53@test.com',@pwd_hash,'부산문화회관 대극장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(54,'host54@test.com',@pwd_hash,'부산 벡스코 오디토리움','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(55,'host55@test.com',@pwd_hash,'대구 오페라하우스','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(56,'host56@test.com',@pwd_hash,'광주 예술의전당 대극장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(57,'host57@test.com',@pwd_hash,'인천문화예술회관 대공연장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0),
(58,'host58@test.com',@pwd_hash,'춘천 KT&G 상상마당 공연장','USER','ACTIVE',1,1,0,NOW(),NOW(),NOW(),0);

-- 호스트용 performer_profile (프로필 선택 시 공연자 모드도 선택 가능하도록, 시연용)
INSERT INTO performer_profile (user_id, stage_name, profile_image_url, categories, description, activity_area, position, skill_level, initialized, created_at, updated_at, is_deleted) VALUES
(41, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(42, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(43, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(44, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(45, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(46, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(47, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(48, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(49, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '서울', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(50, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '경기', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(51, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '경기', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(52, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '경기', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(53, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '부산', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(54, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '부산', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(55, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '대구', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(56, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '광주', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(57, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '인천', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0),
(58, '공연장운영', NULL, 'PLAY', '공연장 운영 담당', '강원', 'VOCAL', 'BEGINNER', 1, NOW(), NOW(), 0);

-- 호스트용 user_profile (필수 1:1, 공백 없이 최소값 채움)
INSERT INTO user_profile (user_id, is_initialized, profile_image_url, birth_date, phone_number, location, preferred_genres, preferred_performance_types, introduction, created_at, updated_at, is_deleted) VALUES
(41, 1, NULL, '1990-01-01', '02-1544-6399', '서울', 'BAND', 'SEATING', 'YES24 LIVE HALL 운영 담당', NOW(), NOW(), 0),
(42, 1, NULL, '1990-01-01', '02-1544-1591', '서울', 'BAND', 'SEATING', '블루스퀘어 마스터카드홀 운영 담당', NOW(), NOW(), 0),
(43, 1, NULL, '1990-01-01', '02-580-1300', '서울', 'PLAY', 'SEATING', '예술의전당 콘서트홀 운영 담당', NOW(), NOW(), 0),
(44, 1, NULL, '1990-01-01', '02-399-1000', '서울', 'PLAY', 'SEATING', '세종문화회관 대극장 운영 담당', NOW(), NOW(), 0),
(45, 1, NULL, '1990-01-01', '1544-7744', '서울', 'BAND', 'SEATING', '롯데콘서트홀 운영 담당', NOW(), NOW(), 0),
(46, 1, NULL, '1990-01-01', '02-325-6071', '서울', 'BAND', 'STANDING', '홍대 롤링홀 운영 담당', NOW(), NOW(), 0),
(47, 1, NULL, '1990-01-01', '02-3143-7080', '서울', 'BAND', 'STANDING', '웨스트브릿지 라이브홀 운영 담당', NOW(), NOW(), 0),
(48, 1, NULL, '1990-01-01', '02-330-6200', '서울', 'BAND', 'STANDING', 'KT&G 상상마당 라이브홀 운영 담당', NOW(), NOW(), 0),
(49, 1, NULL, '1990-01-01', '02-749-4500', '서울', 'BAND', 'SEATING', '노들섬 라이브하우스 운영 담당', NOW(), NOW(), 0),
(50, 1, NULL, '1990-01-01', '1577-7766', '경기', 'PLAY', 'SEATING', '고양아람누리 아람극장 운영 담당', NOW(), NOW(), 0),
(51, 1, NULL, '1990-01-01', '031-783-8000', '경기', 'PLAY', 'SEATING', '성남아트센터 오페라하우스 운영 담당', NOW(), NOW(), 0),
(52, 1, NULL, '1990-01-01', '031-250-5300', '경기', 'PLAY', 'SEATING', '수원 SK아트리움 대공연장 운영 담당', NOW(), NOW(), 0),
(53, 1, NULL, '1990-01-01', '051-607-6000', '부산', 'PLAY', 'SEATING', '부산문화회관 대극장 운영 담당', NOW(), NOW(), 0),
(54, 1, NULL, '1990-01-01', '051-740-7300', '부산', 'BAND', 'SEATING', '부산 벡스코 오디토리움 운영 담당', NOW(), NOW(), 0),
(55, 1, NULL, '1990-01-01', '053-666-6000', '대구', 'PLAY', 'SEATING', '대구 오페라하우스 운영 담당', NOW(), NOW(), 0),
(56, 1, NULL, '1990-01-01', '062-613-8233', '광주', 'PLAY', 'SEATING', '광주 예술의전당 대극장 운영 담당', NOW(), NOW(), 0),
(57, 1, NULL, '1990-01-01', '032-420-2000', '인천', 'PLAY', 'SEATING', '인천문화예술회관 대공연장 운영 담당', NOW(), NOW(), 0),
(58, 1, NULL, '1990-01-01', '033-818-3200', '강원', 'BAND', 'STANDING', '춘천 KT&G 상상마당 공연장 운영 담당', NOW(), NOW(), 0);

-- 호스트 프로필 (host_id는 자동 생성, venue INSERT에서 서브쿼리로 참조, representative_name 비움 없음)
INSERT INTO host_profile (user_id, organization_name, representative_name, business_number, opening_date, contact_number, business_email, profile_image_url, is_verified, is_initialized, created_at, updated_at, is_deleted) VALUES
(41,'YES24 LIVE HALL','담당자','120-12-0041','20200101','02-1544-6399','livehall@yes24.com',NULL,1,1,NOW(),NOW(),0),
(42,'블루스퀘어 마스터카드홀','담당자','120-12-0042','20200101','02-1544-1591','info@bluesquare.kr',NULL,1,1,NOW(),NOW(),0),
(43,'예술의전당 콘서트홀','담당자','120-12-0043','20200101','02-580-1300','info@sac.or.kr',NULL,1,1,NOW(),NOW(),0),
(44,'세종문화회관 대극장','담당자','120-12-0044','20200101','02-399-1000','info@sejongpac.or.kr',NULL,1,1,NOW(),NOW(),0),
(45,'롯데콘서트홀','담당자','120-12-0045','20200101','1544-7744','info@lotteconcert.com',NULL,1,1,NOW(),NOW(),0),
(46,'홍대 롤링홀','담당자','120-12-0046','20200101','02-325-6071','info@rollinghall.co.kr',NULL,1,1,NOW(),NOW(),0),
(47,'웨스트브릿지 라이브홀','담당자','120-12-0047','20200101','02-3143-7080','info@westbridge.kr',NULL,1,1,NOW(),NOW(),0),
(48,'KT&G 상상마당 라이브홀','담당자','120-12-0048','20200101','02-330-6200','info@sangsangmadang.com',NULL,1,1,NOW(),NOW(),0),
(49,'노들섬 라이브하우스','담당자','120-12-0049','20200101','02-749-4500','info@nodeul.kr',NULL,1,1,NOW(),NOW(),0),
(50,'고양아람누리 아람극장','담당자','120-12-0050','20200101','1577-7766','info@aramtheater.or.kr',NULL,1,1,NOW(),NOW(),0),
(51,'성남아트센터 오페라하우스','담당자','120-12-0051','20200101','031-783-8000','info@snart.or.kr',NULL,1,1,NOW(),NOW(),0),
(52,'수원 SK아트리움 대공연장','담당자','120-12-0052','20200101','031-250-5300','info@suwonskartrium.or.kr',NULL,1,1,NOW(),NOW(),0),
(53,'부산문화회관 대극장','담당자','120-12-0053','20200101','051-607-6000','info@busancc.or.kr',NULL,1,1,NOW(),NOW(),0),
(54,'부산 벡스코 오디토리움','담당자','120-12-0054','20200101','051-740-7300','info@bexco.or.kr',NULL,1,1,NOW(),NOW(),0),
(55,'대구 오페라하우스','담당자','120-12-0055','20200101','053-666-6000','info@daeguoperahouse.org',NULL,1,1,NOW(),NOW(),0),
(56,'광주 예술의전당 대극장','담당자','120-12-0056','20200101','062-613-8233','info@gjart.or.kr',NULL,1,1,NOW(),NOW(),0),
(57,'인천문화예술회관 대공연장','담당자','120-12-0057','20200101','032-420-2000','info@iac.or.kr',NULL,1,1,NOW(),NOW(),0),
(58,'춘천 KT&G 상상마당 공연장','담당자','120-12-0058','20200101','033-818-3200','info@ktngchuncheon.or.kr',NULL,1,1,NOW(),NOW(),0);

-- 공연장 18곳 (좌석수 10~100 임의: 47,82,31,95,58,44,76,23,89,62,38,71,55,19,66,42,73,27)
INSERT INTO venue (host_id, venue_name, address, contact, description, venue_image, venue_type, total_seats, open_time, close_time, booking_unit, rental_fee, facilities, regular_closing, temporary_closing, created_at, updated_at, is_deleted) VALUES
((SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1),'YES24 LIVE HALL','서울특별시 광진구 구천면로 20','070-5001-4532','대중음악 공연 중심의 전문 라이브 공연장.','/uploads/yes24_live_hall.jpg','CONCERT_HALL',47,'09:00','22:00',60,1000000,'주차장,대기실,음향장비',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1),'블루스퀘어 마스터카드홀','서울특별시 용산구 이태원로 294','1544-1591','대형 뮤지컬과 공연이 열리는 공연장.','/uploads/bluesquare_mastercard_hall.jpg','THEATER',82,'09:00','22:00',60,2500000,'주차장,분장실,음향시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1),'예술의전당 콘서트홀','서울특별시 서초구 남부순환로 2406','02-580-1300','국내 대표 클래식 콘서트홀.','/uploads/sac_concert_hall.jpg','CONCERT_HALL',31,'09:00','22:00',60,3000000,'주차장,대기실,음향시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 44 LIMIT 1),'세종문화회관 대극장','서울특별시 종로구 세종대로 175','02-399-1000','서울 대표 공연예술 공간.','/uploads/sejong_grand_theater.png','THEATER',95,'09:00','22:00',60,3000000,'주차장,분장실,무대장비',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 45 LIMIT 1),'롯데콘서트홀','서울특별시 송파구 올림픽로 300','1544-7744','롯데월드타워 내 클래식 전용 콘서트홀.','/uploads/lotte_concert_hall.jpg','CONCERT_HALL',58,'09:00','22:00',60,2500000,'주차장,대기실,파이프오르간',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 46 LIMIT 1),'홍대 롤링홀','서울특별시 마포구 어울마당로 35','02-325-6071','홍대 대표 인디 공연장.','/uploads/rolling_hall.jpg','LIVE_HOUSE',44,'10:00','23:00',60,500000,'음향시설,조명시설,대기실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 47 LIMIT 1),'웨스트브릿지 라이브홀','서울특별시 마포구 와우산로25길 6','02-3143-7080','홍대 지역 라이브 공연장.','/uploads/westbridge_livehall.jpg','LIVE_HOUSE',76,'10:00','23:00',60,600000,'음향시설,조명시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 48 LIMIT 1),'KT&G 상상마당 라이브홀','서울특별시 마포구 어울마당로 65','02-330-6200','인디 공연 중심 라이브홀.','/uploads/sangsangmadang_livehall.jpg','LIVE_HOUSE',23,'10:00','23:00',60,600000,'음향시설,조명시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 49 LIMIT 1),'노들섬 라이브하우스','서울특별시 용산구 양녕로 445','02-749-4500','노들섬 복합문화공간 공연장.','/uploads/nodeul_livehouse.jpg','CONCERT_HALL',89,'10:00','22:00',60,700000,'주차장,음향시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 50 LIMIT 1),'고양아람누리 아람극장','경기도 고양시 일산동구 중앙로 1286','1577-7766','고양아람누리 대표 공연장.','/uploads/aram_theater.jpg','THEATER',62,'09:00','22:00',60,2000000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 51 LIMIT 1),'성남아트센터 오페라하우스','경기도 성남시 분당구 성남대로 808','031-783-8000','대형 공연 가능한 공연장.','/uploads/seongnam_opera_house.jpg','THEATER',38,'09:00','22:00',60,2000000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 52 LIMIT 1),'수원 SK아트리움 대공연장','경기도 수원시 장안구 경수대로 893','031-250-5300','수원 대표 공연장.','/uploads/suwon_sk_atrium.jpg','THEATER',71,'09:00','22:00',60,1500000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 53 LIMIT 1),'부산문화회관 대극장','부산광역시 남구 유엔평화로76번길 1','051-607-6000','부산 대표 종합 공연장.','/uploads/busan_culture_center.jpg','THEATER',55,'09:00','22:00',60,1500000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 54 LIMIT 1),'부산 벡스코 오디토리움','부산광역시 해운대구 APEC로 55','051-740-7300','대형 공연 가능한 오디토리움.','/uploads/bexco_auditorium.jpg','CONCERT_HALL',19,'09:00','22:00',60,3000000,'주차장,음향시설',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 55 LIMIT 1),'대구 오페라하우스','대구광역시 북구 호암로 15','053-666-6000','오페라 전문 공연장.','/uploads/daegu_opera_house.jpg','THEATER',66,'09:00','22:00',60,1500000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 56 LIMIT 1),'광주 예술의전당 대극장','광주광역시 북구 북문대로 60','062-613-8233','광주 대표 공연예술 공간.','/uploads/gwangju_art_center.jpg','THEATER',42,'09:00','22:00',60,1500000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 57 LIMIT 1),'인천문화예술회관 대공연장','인천광역시 남동구 예술로 149','032-420-2000','인천 대표 공연장.','/uploads/incheon_art_center.jpg','THEATER',73,'09:00','22:00',60,1500000,'주차장,분장실',NULL,NULL,NOW(),NOW(),0),
((SELECT host_id FROM host_profile WHERE user_id = 58 LIMIT 1),'춘천 KT&G 상상마당 공연장','강원특별자치도 춘천시 스포츠타운길 399','033-818-3200','춘천 문화예술 공연장.','/uploads/chuncheon_sangsangmadang.jpg','LIVE_HOUSE',27,'10:00','22:00',60,500000,'음향시설,조명시설',NULL,NULL,NOW(),NOW(),0);

-- 공연 15개 (capacity = 해당 공연장 좌석수: YES24 47, 블루스퀘어 82, 예술의전당 31)
INSERT INTO performance (venue_id, host_creator_id, performer_creator_id, title, description, performance_image_url, recruit_status, capacity, category, status, created_at, updated_at, is_deleted) VALUES
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1), NULL, '2025 EVNNE CONCERT "SET N GO" SEOUL', '라이브 콘서트 공연', '/uploads/evnne_setngo.jpg', 'CLOSED', 47, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1), NULL, 'THE SOLUTIONS Concert "Emergence"', '라이브 공연', '/uploads/solutions_emergence.jpg', 'CLOSED', 47, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1), NULL, 'CONCRETE SPARK', '라이브 콘서트', '/uploads/concrete_spark.jpg', 'CLOSED', 47, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1), NULL, 'TOGENASHI TOGEARI Live in SEOUL', '라이브 콘서트', '/uploads/togenashi.jpg', 'OPEN', 47, 'BAND', 'ONGOING', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 41 LIMIT 1), NULL, '2025 YESUNG CONCERT [It''s Complicated] IN SEOUL', '콘서트 공연', '/uploads/yesung_itscomplicated.jpg', 'CLOSED', 47, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1), NULL, '2025 BAEKHYUN WORLD TOUR "Reverie [dot]"', '월드 투어 콘서트', '/uploads/baekhyun_reverie.jpg', 'CLOSED', 82, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1), NULL, 'DAY6 Special Concert The Present', 'DAY6 서울 공연', '/uploads/day6_thepresent.jpg', 'CLOSED', 82, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1), NULL, 'HIGHLIGHT LIVE 2025 [RIDE OR DIE]', '하이라이트 콘서트', '/uploads/highlight_rideordie.jpg', 'CLOSED', 82, 'BAND', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1), NULL, '2025-2026 이찬원 〈찬가 : 찬란한 하루〉 앵콜 콘서트', '콘서트', '/uploads/leechanwon_changa.jpg', 'OPEN', 82, 'BAND', 'ONGOING', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 42 LIMIT 1), NULL, 'KPOP Festival Live', '대형 콘서트 공연', '/uploads/kpop_festival.jpg', 'OPEN', 82, 'BAND', 'ONGOING', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1), NULL, 'SAC 11AM CONCERT (Nov)', '클래식 공연', '/uploads/sac_11am_nov.jpg', 'CLOSED', 31, 'PLAY', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1), NULL, 'SAC 11AM CONCERT (Dec)', '클래식 공연', '/uploads/sac_11am_dec.jpg', 'CLOSED', 31, 'PLAY', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1), NULL, 'Daniel Harding ＆ Orchestra', '오케스트라 공연', '/uploads/daniel_harding_orchestra.jpg', 'CLOSED', 31, 'PLAY', 'ENDED', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1), NULL, '리처드 용재 오닐 ＆ 제레미 덴크 듀오 리사이틀', '클래식 듀오 리사이틀', '/uploads/richard_jangle_duo.jpg', 'OPEN', 31, 'PLAY', 'ONGOING', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), (SELECT host_id FROM host_profile WHERE user_id = 43 LIMIT 1), NULL, 'KBS교향악단 제826회 정기연주회', '클래식 연주회', '/uploads/kbs_orchestra.jpg', 'OPEN', 31, 'PLAY', 'ONGOING', NOW(), NOW(), 0);

-- 7-2. 공연 일정 (15개 공연 각 1회차)
INSERT INTO performance_schedule (performance_id, start_time, end_time, created_at, updated_at, is_deleted) VALUES
((SELECT performance_id FROM performance WHERE title = '2025 EVNNE CONCERT "SET N GO" SEOUL' LIMIT 1), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'THE SOLUTIONS Concert "Emergence"' LIMIT 1), DATE_ADD(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'CONCRETE SPARK' LIMIT 1), DATE_ADD(NOW(), INTERVAL 9 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'TOGENASHI TOGEARI Live in SEOUL' LIMIT 1), DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = '2025 YESUNG CONCERT [It''s Complicated] IN SEOUL' LIMIT 1), DATE_ADD(NOW(), INTERVAL 11 DAY), DATE_ADD(NOW(), INTERVAL 11 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = '2025 BAEKHYUN WORLD TOUR "Reverie [dot]"' LIMIT 1), DATE_ADD(NOW(), INTERVAL 12 DAY), DATE_ADD(NOW(), INTERVAL 12 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'DAY6 Special Concert The Present' LIMIT 1), DATE_ADD(NOW(), INTERVAL 13 DAY), DATE_ADD(NOW(), INTERVAL 13 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'HIGHLIGHT LIVE 2025 [RIDE OR DIE]' LIMIT 1), DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = '2025-2026 이찬원 〈찬가 : 찬란한 하루〉 앵콜 콘서트' LIMIT 1), DATE_ADD(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'KPOP Festival Live' LIMIT 1), DATE_ADD(NOW(), INTERVAL 16 DAY), DATE_ADD(NOW(), INTERVAL 16 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'SAC 11AM CONCERT (Nov)' LIMIT 1), DATE_ADD(NOW(), INTERVAL 17 DAY), DATE_ADD(NOW(), INTERVAL 17 DAY) + INTERVAL 1 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'SAC 11AM CONCERT (Dec)' LIMIT 1), DATE_ADD(NOW(), INTERVAL 18 DAY), DATE_ADD(NOW(), INTERVAL 18 DAY) + INTERVAL 1 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'Daniel Harding ＆ Orchestra' LIMIT 1), DATE_ADD(NOW(), INTERVAL 19 DAY), DATE_ADD(NOW(), INTERVAL 19 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = '리처드 용재 오닐 ＆ 제레미 덴크 듀오 리사이틀' LIMIT 1), DATE_ADD(NOW(), INTERVAL 20 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY) + INTERVAL 1 HOUR, NOW(), NOW(), 0),
((SELECT performance_id FROM performance WHERE title = 'KBS교향악단 제826회 정기연주회' LIMIT 1), DATE_ADD(NOW(), INTERVAL 21 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY) + INTERVAL 2 HOUR, NOW(), NOW(), 0);

-- 6-2. 추가 공연장 좌석 (18곳, 각 10석·층 1/2·등급 vip/r/s/a 골고루)
-- ※ venue.total_seats 수만큼 실제 좌석 넣으려면: data-test-insert-seats-full.sql 실행 (본 블록 좌석은 DELETE 후 994행 INSERT)
INSERT INTO seat (venue_id, seat_floor, x_pos, y_pos, seat_number, seat_type, created_at, updated_at, is_deleted) VALUES
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 2, 10, 100, 'R-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 2, 30, 100, 'R-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 2, 50, 100, 'R-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 2, 70, 100, 'R-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '블루스퀘어 마스터카드홀' LIMIT 1), 2, 90, 100, 'R-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '예술의전당 콘서트홀' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '세종문화회관 대극장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '롯데콘서트홀' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '홍대 롤링홀' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '웨스트브릿지 라이브홀' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'KT&G 상상마당 라이브홀' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '노들섬 라이브하우스' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '고양아람누리 아람극장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '성남아트센터 오페라하우스' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '수원 SK아트리움 대공연장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산문화회관 대극장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '부산 벡스코 오디토리움' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '대구 오페라하우스' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '광주 예술의전당 대극장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '인천문화예술회관 대공연장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 2, 10, 100, 'S-1', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 2, 30, 100, 'S-2', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 2, 50, 100, 'S-3', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 2, 70, 100, 'S-4', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = '춘천 KT&G 상상마당 공연장' LIMIT 1), 2, 90, 100, 'S-5', 'r', NOW(), NOW(), 0);

-- 8-2. 추가 공연 리뷰 (신규 공연 상세/피드 시연용, user_id 41·42 = 새 블록만 쓸 때 OK)
INSERT INTO review (user_id, performance_id, seat_id, rating, content, encore_pick, created_at, updated_at, is_deleted) VALUES
(41, (SELECT performance_id FROM performance WHERE title = '2025 EVNNE CONCERT "SET N GO" SEOUL' LIMIT 1), NULL, 5, '라이브 너무 좋았어요! 분위기 최고였습니다.', '마지막 무대가 인상 깊었어요', NOW(), NOW(), 0),
(42, (SELECT performance_id FROM performance WHERE title = '2025 BAEKHYUN WORLD TOUR "Reverie [dot]"' LIMIT 1), NULL, 5, '베키 콘서트 대박이었어요. 다음에도 꼭 가고 싶어요.', '오프닝 퍼포먼스 강추', NOW(), NOW(), 0),
(41, (SELECT performance_id FROM performance WHERE title = 'KBS교향악단 제826회 정기연주회' LIMIT 1), NULL, 4, '클래식 공연 분위기가 좋았습니다. 공연장 음향이 훌륭해요.', NULL, NOW(), NOW(), 0),
(41, (SELECT performance_id FROM performance WHERE title = '2025 EVNNE CONCERT "SET N GO" SEOUL' LIMIT 1), (SELECT s.seat_id FROM seat s JOIN venue v ON s.venue_id = v.venue_id WHERE v.venue_name = 'YES24 LIVE HALL' AND s.is_deleted = 0 LIMIT 1), 5, 'A-1 자리 시야 완벽하고 음향도 좋아요. vip 강추.', NULL, NOW(), NOW(), 0),
(42, (SELECT performance_id FROM performance WHERE title = '2025-2026 이찬원 〈찬가 : 찬란한 하루〉 앵콜 콘서트' LIMIT 1), (SELECT s.seat_id FROM seat s JOIN venue v ON s.venue_id = v.venue_id WHERE v.venue_name = '블루스퀘어 마스터카드홀' AND s.is_deleted = 0 LIMIT 1), 4, '1-A 자리에서 봤는데 가성비 좋은 자리였어요.', NULL, NOW(), NOW(), 0);

-- 12-2. 추가 유저-공연 관계 (찜 목록/피드 시연용, user_id 41·42 = 새 블록만 쓸 때 OK)
INSERT INTO user_performance_relation (user_id, performance_id, status, watched_at, created_at, updated_at, is_deleted) VALUES
(41, (SELECT performance_id FROM performance WHERE title = 'TOGENASHI TOGEARI Live in SEOUL' LIMIT 1), 'WISHED', NULL, NOW(), NOW(), 0),
(41, (SELECT performance_id FROM performance WHERE title = 'KPOP Festival Live' LIMIT 1), 'WISHED', NULL, NOW(), NOW(), 0),
(42, (SELECT performance_id FROM performance WHERE title = '2025-2026 이찬원 〈찬가 : 찬란한 하루〉 앵콜 콘서트' LIMIT 1), 'WISHED', NULL, NOW(), NOW(), 0),
(41, (SELECT performance_id FROM performance WHERE title = '리처드 용재 오닐 ＆ 제레미 덴크 듀오 리사이틀' LIMIT 1), 'WISHED', NULL, NOW(), NOW(), 0);

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
*/
-- 새 데이터(호스트 41~58, 신규 공연장·공연)만 쓸 경우 위 블록은 무시. 신규 공연/공연장 이미지는 INSERT 시 이미 /uploads/xxx 로 들어감.

-- -----------------------------------------------------------------------------
-- [비밀번호] password123 로그인 안 되면: 앱 실행 → /dev/bcrypt-password123 에서 해시 복사 후 아래 주석 해제 실행
-- -----------------------------------------------------------------------------
-- UPDATE users SET password_hash = '여기에_복사한_해시' WHERE email IN ('user1@test.com','performer1@test.com','host1@test.com','user2@test.com');

-- -----------------------------------------------------------------------------
-- [오류 시] PerformanceStatus.MUSICAL 등 enum 오류: category/status 뒤바뀜일 수 있음. 아래 실행 후 7·7-1 INSERT 재실행
-- -----------------------------------------------------------------------------
-- DELETE FROM performance_schedule;
-- DELETE FROM performance;
