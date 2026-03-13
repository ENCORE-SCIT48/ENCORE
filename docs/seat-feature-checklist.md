# 좌석 관련 기능 체크포인트

좌석 기능이 동작하지 않을 때 **좌석이 없어서인지** 확인할 수 있는 체크 리스트입니다.

---

## 1. 좌석이 쓰이는 기능

| 기능 | 위치 | 필요한 데이터 |
|------|------|----------------|
| **좌석 리뷰 탭** (공연 상세) | `/performances/{id}` → 좌석리뷰 탭 | 해당 공연의 **공연장(venue)** 에 소속된 **seat** 레코드, **x_pos/y_pos** 있으면 배치도 표시 |
| **좌석 리뷰 작성/수정** | `/performances/{id}/reviews/seats/new` | 위와 동일 (좌석 목록 + 선택 가능해야 작성 가능) |
| **공연장 등록/수정 폼** | 호스트 → 공연장 등록·수정 | 해당 공연장의 **seat** (폼에서 좌석 배치 편집) |
| **공연장 상세** | `/venues/{id}` | venue 정보만 (totalSeats 등). 좌석 목록 API는 호출하지 않음 |

---

## 2. 백엔드 체크포인트

### 2-1. 공연 ↔ 공연장 연결

- **performance.venue_id** 가 NULL이면 해당 공연의 좌석 목록은 항상 빈 배열입니다.
- `GET /api/performances/{id}/seats` → `PerformanceService.getSeatsByPerformanceId()` 에서 **venue == null** 이면 `List.of()` 반환.

**확인 쿼리 예시:**

```sql
SELECT p.performance_id, p.title, p.venue_id, v.venue_name
FROM performance p
LEFT JOIN venue v ON p.venue_id = v.venue_id
WHERE p.is_deleted = 0;
```

- `venue_id` 가 NULL이거나, 해당 venue가 없으면 좌석 기능 사용 불가.

### 2-2. 공연장별 좌석 존재 여부

- 좌석 데이터는 **seat** 테이블에 **venue_id** 로 묶여 있습니다.
- **seat** 가 한 개도 없으면 좌석 리뷰 탭/작성 페이지에서 "좌석 없음" 상태가 됩니다.

**확인 쿼리 예시:**

```sql
SELECT v.venue_id, v.venue_name, v.total_seats, COUNT(s.seat_id) AS seat_count
FROM venue v
LEFT JOIN seat s ON s.venue_id = v.venue_id AND s.is_deleted = 0
WHERE v.is_deleted = 0
GROUP BY v.venue_id, v.venue_name, v.total_seats;
```

- `seat_count` 가 0인 공연장은 좌석 리뷰 작성/배치도 표시 불가.

### 2-3. 좌석 좌표 (x_pos, y_pos)

- **배치도(캔버스)** 는 `seat.x_pos`, `seat.y_pos` 가 모두 있을 때만 그려집니다.
- 둘 다 NULL이면 API는 좌석 목록을 주지만, 프론트에서 **드롭다운으로만** 선택 가능 (배치도는 숨김).

**확인 쿼리 예시:**

```sql
SELECT venue_id, COUNT(*) AS total,
       SUM(CASE WHEN x_pos IS NOT NULL AND y_pos IS NOT NULL THEN 1 ELSE 0 END) AS with_position
FROM seat
WHERE is_deleted = 0
GROUP BY venue_id;
```

- `with_position` 이 0이면 해당 공연장의 좌석 리뷰 화면에서 **배치도는 안 나오고** 드롭다운만 나옵니다.

---

## 3. 프론트 체크포인트

### 3-1. 공연 상세 – 좌석 리뷰 탭

- **API:** `GET /api/performances/{performanceId}/seats`
- **동작:**
  - 응답이 빈 배열 → 배치도 숨기고 `#seatReviewEmpty` 메시지 표시.
  - 좌석은 있는데 **x_pos/y_pos 있는 좌석이 0개** → 마찬가지로 빈 배치도로 처리, `#seatReviewEmpty` 표시.
- **체크:** 해당 공연의 공연장에 seat가 있고, 그 중 최소 1개라도 x_pos, y_pos가 있으면 배치도가 나와야 함.

### 3-2. 좌석 리뷰 작성 페이지

- **API:** `GET /api/performances/{performanceId}/seats`
- **동작:**
  - 응답이 빈 배열 → "이 공연의 공연장에 등록된 좌석이 없어 좌석 리뷰를 작성할 수 없습니다." 표시, 작성 버튼 비활성화.
  - 좌석이 있으면 드롭다운(또는 배치도)으로 선택 후 작성 가능.
- **체크:** 좌석이 하나도 없으면 위 안내 문구가 보이고, 작성이 막혀 있는지 확인.

### 3-3. 공연장 등록/수정 폼

- **API:** `GET /api/venues/{venueId}/form` (수정 시) → `VenueFormResponseDto` 에 seats 포함.
- 좌석이 없으면 폼의 좌석 영역이 비어 있음. 등록 시에는 좌석을 추가해 저장하면 됨.

---

## 4. 요약 체크 리스트

| 순서 | 확인 항목 | 확인 방법 |
|------|-----------|-----------|
| 1 | 문제되는 **공연**에 **venue_id** 가 연결되어 있는가? | DB: `performance.venue_id` NOT NULL |
| 2 | 해당 **공연장(venue)** 에 **seat** 레코드가 있는가? | DB: `SELECT COUNT(*) FROM seat WHERE venue_id = ? AND is_deleted = 0` |
| 3 | 좌석 **배치도**까지 필요하면 **x_pos, y_pos** 가 들어 있는가? | DB: `SELECT * FROM seat WHERE venue_id = ? AND x_pos IS NOT NULL AND y_pos IS NOT NULL` |
| 4 | 테스트 데이터만 쓰는 경우, **data-test-insert-seats-full.sql** 등으로 해당 공연장 좌석을 넣었는가? | 스크립트 실행 여부 및 venue 목록 일치 여부 |

위를 순서대로 확인하면 **좌석이 없어서인지**, **좌표가 없어서인지** 구분할 수 있습니다.

---

## 5. 좌석이 안 뜨는 경우 해결 절차

**원인:** 공연(performance)은 공연장(venue)에 연결돼 있고, 좌석(seat)은 공연장에 연결됩니다.  
**공연 → 공연장 → 좌석** 이어야 좌석 리뷰/배치도가 나옵니다.  
좌석이 안 뜨면 대부분 **해당 공연의 공연장(venue_id)에 seat 행이 없을 때**입니다.

**해결:**

1. **공연에 공연장이 붙어 있는지 확인**
   ```sql
   SELECT performance_id, title, venue_id FROM performance WHERE is_deleted = 0;
   ```
   `venue_id` 가 NULL이면 해당 공연은 **좌석 리뷰 페이지가 아예 동작하지 않습니다** (좌석 정보를 불러올 수 없음).

   **한 번에 고치기:** `venue_id` 가 NULL인 공연에, 좌석이 있는 공연장을 자동으로 넣는 스크립트 실행:
   ```text
   sql/data-fix-performance-venue.sql
   ```
   (실행 전에 `data-test-insert.sql` 등으로 venue·seat 데이터가 있어야 함. 실행 후 해당 공연으로 좌석 리뷰 페이지 접속 시 좌석이 뜸.)

2. **해당 공연장에 좌석이 있는지 확인**
   ```sql
   SELECT v.venue_id, v.venue_name, COUNT(s.seat_id) AS seat_count
   FROM venue v
   LEFT JOIN seat s ON s.venue_id = v.venue_id AND s.is_deleted = 0
   WHERE v.is_deleted = 0
   GROUP BY v.venue_id, v.venue_name;
   ```
   `seat_count` 가 0인 공연장에는 좌석을 넣어야 함.

3. **테스트 데이터로 18곳 공연장 + 좌석 넣기**
   - 18곳 venue가 아직 없으면: `data-test-insert-venues-18.sql` 실행 (data-test-insert.sql 실행 후).
   - 그 다음 **좌석** 넣기: `data-test-insert-seats-full.sql` 실행.  
     이 스크립트는 `venue_name` 이 YES24 LIVE HALL, 블루스퀘어 마스터카드홀 등 **18곳과 일치하는 venue**에만 좌석을 넣습니다.  
     (다른 이름으로 공연장을 넣었다면, 위 18곳 이름과 맞추거나, 스크립트의 venue_name을 실제 DB의 venue_name으로 바꿔서 실행.)

4. **실행 순서 정리**
   - `data-test-insert.sql` (회원·호스트 등)
   - `data-test-insert-venues-18.sql` (18곳 공연장, 없으면 생략)
   - `data-test-insert-seats-full.sql` (18곳에 대한 좌석 INSERT)

위 순서로 넣으면, 해당 18곳 공연장을 쓰는 공연에서는 좌석이 정상적으로 표시됩니다.

---

## 6. 전체 검증 (리포지토리 ~ CSS) — "아예 안 먹을 때"

아래를 순서대로 확인하면, 수정 사항이 반영되었는지·환경 문제인지 구분할 수 있습니다.

### 6-1. 백엔드

| 구분 | 확인 내용 |
|------|-----------|
| **ReviewRepository** | 좌석 리뷰 조회 메서드에 `EntityGraph(attributePaths = {"user", "seat", "seat.venue"})` 로 seat.venue까지 fetch 되어 있는지 |
| **PerformanceRepository** | `findDetailById(performanceId)` 가 venue를 left join fetch 하는지 (좌석/공연 상세 조회 시 NPE 방지) |
| **PerformanceService** | `getSeatsByPerformanceId` 는 `findDetailById` 사용, `createSeatReview` 도 `findDetailById` 로 공연 로드하는지 |
| **SeatReviewItemDto** | `venueId` 필드가 있고, `toSeatReviewItemDto` 에서 `r.getSeat().getVenue().getVenueId()` 로 채우는지 |
| **CORS** | `SecurityConfig` 의 `CorsConfiguration` 에 **PATCH** 메서드가 포함되어 있는지 (좌석 리뷰 수정 시 PATCH 사용) |

### 6-2. API 경로·응답

- `GET /api/performances/{id}/seats` → `CommonResponse<List<SeatOptionDto>>` (data에 배열)
- `GET /api/performances/{id}/seat-reviews/by-seat` → `CommonResponse<List<SeatReviewItemDto>>` (data에 배열, 각 항목에 seatId·venueId 포함)
- `POST /api/performances/{id}/seat-reviews` → Body: `{ seatId, rating, content }`, 로그인·관람객(ROLE_USER)만 가능
- `PATCH /api/performances/{id}/seat-reviews/{reviewId}` → Body: `{ rating, content }` (seatId는 수정 시 변경 불가)

### 6-3. 프론트 (공연 상세 — 좌석 리뷰 탭)

- **detail.js**  
  - `loadSeatReviewMap()` 에서 `/seats`, `/seat-reviews/by-seat` 호출 시 **xhrFields: { withCredentials: true }** 사용하는지  
  - 응답은 `res?.data` 가 배열인지 확인 후 사용 (`Array.isArray(res?.data) ? res.data : []`)  
  - 좌석 로드 성공 시 `#seatReviewEmpty` 를 **hide** 하는지  
  - by-seat 실패 시에도 배치도만 그리도록 **initSeatReviewCanvas(); drawSeatReviewCanvas();** 호출하는지  

### 6-4. 프론트 (좌석 리뷰 작성/수정)

- **seatReviewWrite.js**  
  - `performanceId` 는 hidden `#performanceId` 우선, 없으면 URL `/performances/(\d+)/` 에서 추출  
  - 모든 API 호출에 **xhrFields: { withCredentials: true }**  
  - 제출 시 `JSON.stringify({ seatId: Number(seatId), rating, content })`  
- **seatReviewWrite.html**  
  - `th:value="${performanceId}"`, `th:value="${reviewId}"` 로 hidden 전달  
- **PerformancePageController**  
  - `GET /performances/{id}/reviews/seats/new` → `performance/seatReviewWrite`, model에 `performanceId`, `reviewId=0`  
  - `GET /performances/{id}/reviews/seats/{reviewId}/edit` → 동일 템플릿, `reviewId` 전달  

### 6-5. CSS

- **reviewWrite.css** (좌석 리뷰 작성 페이지)  
  - `.back-btn` 이 `z-index: 2`, 클릭 가능 영역 확보  
  - `.star` 버튼 클릭/호버 시 `.is-on`, 색상 변경  
  - `.seat-map-wrap`, `.seat-floor-tabs`, `.seat-canvas-wrapper` 로 배치도 영역 구성  
- **detail.css** (공연 상세 좌석 리뷰 탭)  
  - `.seat-review-map-wrap`, `.seat-review-tooltip` 등으로 배치도·툴팁 스타일 정의  

### 6-6. 브라우저에서 "안 먹을 때" 확인

1. **F12 → Network**  
   - `GET /api/performances/{id}/seats` → Status 200, 응답 body 의 `data` 가 배열인지  
   - `GET /api/performances/{id}/seat-reviews/by-seat` → Status 200, `data` 배열  
   - 작성/수정 시 `POST`/`PATCH` 가 **CORS preflight(OPTIONS)** 다음에 실제 요청이 가는지, 403/404가 아닌지  
2. **Console**  
   - `[SeatReviewWrite] loadSeats failed` 등 에러 로그 여부  
3. **DB**  
   - 위 2번·5번의 SQL로 해당 공연의 `venue_id`, 해당 venue의 seat 개수·x_pos/y_pos 확인  
