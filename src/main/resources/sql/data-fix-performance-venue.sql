-- =============================================================================
-- 좌석 리뷰가 동작하려면 "공연(performance)"에 "공연장(venue_id)"이 반드시 있어야 합니다.
-- venue_id가 NULL인 공연은 좌석 목록을 불러올 수 없어 좌석 리뷰 페이지에서 좌석이 안 뜹니다.
--
-- 이 스크립트: venue_id가 NULL인 공연에, 좌석이 있는 공연장 중 하나(가장 작은 venue_id)를 넣습니다.
-- 실행 전: venue, seat 데이터가 있어야 합니다. (data-test-insert.sql 실행 후 권장)
-- =============================================================================

-- venue_id가 NULL인 공연이 있는지 확인 (실행 전 참고)
-- SELECT performance_id, title, venue_id FROM performance WHERE is_deleted = 0 AND venue_id IS NULL;

-- 좌석이 1개 이상 있는 공연장 중 가장 작은 venue_id (예: data-test-insert.sql 기준이면 1)
SET @default_venue_id = (
    SELECT v.venue_id
    FROM venue v
    INNER JOIN seat s ON s.venue_id = v.venue_id AND s.is_deleted = 0
    WHERE v.is_deleted = 0
    GROUP BY v.venue_id
    ORDER BY v.venue_id
    LIMIT 1
);

UPDATE performance
SET venue_id = @default_venue_id
WHERE is_deleted = 0
  AND venue_id IS NULL
  AND @default_venue_id IS NOT NULL;

-- 적용된 행 수는 실행 결과에서 확인 (예: Query OK, 2 rows affected)
