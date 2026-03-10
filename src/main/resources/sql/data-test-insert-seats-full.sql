-- =============================================================================
-- 전체 좌석 (18곳, venue.total_seats 수만큼, 좌표 규칙: 1층/2층, 행당 10석)
--
-- [중요] 좌석이 인식되려면 "18곳 venue"가 먼저 DB에 있어야 합니다.
--        data-test-insert.sql 에는 YES24/블루스퀘어 등 18곳이 없습니다.
--
-- 실행 순서:
--   1) data-test-insert.sql 실행 (회원·호스트·기본 데이터)
--   2) data-test-insert-venues-18.sql 실행 (18곳 공연장 추가)
--   3) 이 파일(data-test-insert-seats-full.sql) 실행
--
-- ※ 이미 해당 18곳 좌석이 있으면 아래 DELETE 후 INSERT 하면 됨
-- =============================================================================
SET SQL_SAFE_UPDATES = 0;
DELETE FROM seat WHERE venue_id IN (
  SELECT venue_id FROM venue WHERE venue_name IN (
    'YES24 LIVE HALL','블루스퀘어 마스터카드홀','예술의전당 콘서트홀','세종문화회관 대극장','롯데콘서트홀','홍대 롤링홀','웨스트브릿지 라이브홀','KT&G 상상마당 라이브홀','노들섬 라이브하우스','고양아람누리 아람극장','성남아트센터 오페라하우스','수원 SK아트리움 대공연장','부산문화회관 대극장','부산 벡스코 오디토리움','대구 오페라하우스','광주 예술의전당 대극장','인천문화예술회관 대공연장','춘천 KT&G 상상마당 공연장'
  )
);
SET SQL_SAFE_UPDATES = 1;

INSERT INTO seat (venue_id, seat_floor, x_pos, y_pos, seat_number, seat_type, created_at, updated_at, is_deleted) VALUES
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 10, 20, 'A-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 30, 20, 'A-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 50, 20, 'A-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 70, 20, 'A-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 90, 20, 'A-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 110, 20, 'A-6', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 130, 20, 'A-7', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 150, 20, 'A-8', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 170, 20, 'A-9', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 190, 20, 'A-10', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 10, 40, 'B-1', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 30, 40, 'B-2', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 50, 40, 'B-3', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 70, 40, 'B-4', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 90, 40, 'B-5', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 110, 40, 'B-6', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 130, 40, 'B-7', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 150, 40, 'B-8', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 170, 40, 'B-9', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 190, 40, 'B-10', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 10, 60, 'C-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 30, 60, 'C-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 50, 60, 'C-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 1, 70, 60, 'C-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 10, 100, 'S-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 30, 100, 'S-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 50, 100, 'S-3', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 70, 100, 'S-4', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 90, 100, 'S-5', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 110, 100, 'S-6', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 130, 100, 'S-7', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 150, 100, 'S-8', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 170, 100, 'S-9', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 190, 100, 'S-10', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 10, 120, 'T-1', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 30, 120, 'T-2', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 50, 120, 'T-3', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 70, 120, 'T-4', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 90, 120, 'T-5', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 110, 120, 'T-6', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 130, 120, 'T-7', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 150, 120, 'T-8', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 170, 120, 'T-9', 's', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 190, 120, 'T-10', 'a', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 10, 140, 'U-1', 'vip', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 30, 140, 'U-2', 'r', NOW(), NOW(), 0),
((SELECT venue_id FROM venue WHERE venue_name = 'YES24 LIVE HALL' LIMIT 1), 2, 50, 140, 'U-3', 's', NOW(), NOW(), 0);
-- 나머지 17곳 좌석: data-test-insert.sql [추가] 블록의 6-2. 좌석 INSERT 참고.
-- 한 번에 넣으려면 위 행의 ); 를 ), 로 바꾼 뒤, 블루스퀘어~춘천 공연장 VALUES 행을 붙이고 마지막만 0); 로 끝내면 됨.
