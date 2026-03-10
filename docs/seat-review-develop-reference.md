# develop 브랜치 좌석 리뷰 코드 참고

develop 브랜치 기준 좌석 리뷰 관련 **프론트/백엔드** 파일·API·동작 요약입니다.  
현재 브랜치와 비교하거나 동기화할 때 참고용으로 사용하세요.

---

## 1. 백엔드 (develop 기준)

### 1-1. 컨트롤러

**파일:** `src/main/java/com/encore/encore/domain/performance/controller/PerformanceController.java`

| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| getSeats | GET | `/{performanceId}/seats` | 해당 공연 공연장의 좌석 목록 (작성 폼·배치도용) |
| getSeatReviews | GET | `/{performanceId}/seat-reviews` | 좌석 리뷰 목록 페이징 |
| getSeatReviewsBySeat | GET | `/{performanceId}/seat-reviews/by-seat` | 좌석별 리뷰 (배치도 호버 툴팁용) |
| getSeatReviewSummary | GET | `/{performanceId}/seat-reviews/summary` | 좌석 리뷰 평균·개수 요약 |
| createSeatReview | POST | `/{performanceId}/seat-reviews` | 좌석 리뷰 작성 (관람객 전용) |
| getSeatReviewForEdit | GET | `/{performanceId}/seat-reviews/{reviewId}` | 수정 폼용 단건 조회 |
| updateSeatReview | PATCH | `/{performanceId}/seat-reviews/{reviewId}` | 좌석 리뷰 수정 |
| deleteSeatReview | DELETE | `/{performanceId}/seat-reviews/{reviewId}` | 좌석 리뷰 삭제 |

### 1-2. 서비스 (PerformanceService)

**파일:** `src/main/java/com/encore/encore/domain/performance/service/PerformanceService.java`

- **getSeatsByPerformanceId(performanceId)**  
  - 공연 조회 → `venue == null` 이면 `List.of()` 반환.  
  - 그 외에는 `seatRepository.findAllByVenueAndIsDeletedFalse(performance.getVenue())` → `SeatOptionDto` 리스트 반환.

- **getSeatReviews(performanceId, pageable)**  
  - `reviewRepository.findByPerformance_PerformanceIdAndSeatIsNotNullAndIsDeletedFalseOrderByCreatedAtDesc` 로 좌석 리뷰만 페이징 조회.

- **getSeatReviewsForMap(performanceId)**  
  - 위와 동일 조건으로 최대 500건 조회 → 배치도 호버 툴팁용.

- **getSeatReviewSummary(performanceId)**  
  - `reviewRepository.getSeatReviewSummary(performanceId)` → avgRating, reviewCount 반환.

- **createSeatReview(performanceId, userId, seatId, rating, content)**  
  - 별점 1~5, 내용 5자 이상, seatId 필수 검증.  
  - 공연의 venue와 좌석의 venue가 일치하는지 검증 후 Review 저장.

- **getSeatReviewForEdit / updateSeatReview / deleteSeatReview**  
  - 해당 공연·리뷰 소유자 검증 후 조회/수정/사제.

### 1-3. DTO·엔티티

| 파일 | 용도 |
|------|------|
| `performance/dto/SeatOptionDto.java` | 좌석 목록 (seatId, seatNumber, seatType, seatFloor, xPos, yPos, getXRatio/getYRatio) |
| `performance/dto/SeatReviewItemDto.java` | 좌석 리뷰 한 건 (리스트·배치도 툴팁) |
| `performance/dto/SeatReviewReqDto.java` | 작성/수정 요청 (seatId, rating, content) |
| `venue/entity/Seat.java` | seat 테이블 (venue, seatFloor, xPos, yPos, seatNumber, seatType) |
| `venue/repository/SeatRepository.java` | findByVenue, findAllByVenueAndIsDeletedFalse, deleteByVenue |

---

## 2. 프론트 (develop 기준)

### 2-1. 공연 상세 – 좌석 리뷰 탭

**파일:** `src/main/resources/static/js/performance/detail.js`

- **loadSeatReviewMap()**  
  - `GET /api/performances/{id}/seats` → `xPos/yPos` 또는 `xRatio/yRatio` 있는 좌석만 필터.  
  - 0개면 `#seatReviewMapWrap` 숨기고 `#seatReviewEmpty` 표시.  
  - 있으면 `buildSeatReviewFloors(list)` → `GET .../seat-reviews/by-seat` → `initSeatReviewCanvas()`, `drawSeatReviewCanvas()` (층 탭, 호버 툴팁 포함).

- **buildSeatReviewFloors(seatList)**  
  - seatFloor·xRatio·yRatio 기준으로 층별 그룹.

### 2-2. 좌석 리뷰 작성/수정 페이지

**파일:** `src/main/resources/static/js/performance/seatReviewWrite.js`

- **loadPerformanceInfo()**  
  - `GET /api/performances/{id}` → 제목·장소·장르 등 표시.

- **loadSeats()**  
  - `GET /api/performances/{id}/seats`  
  - 응답을 `#seatId` select 옵션으로 채움.  
  - `xPos/yPos` 또는 `xRatio/yRatio` 있는 좌석이 있으면 `buildFloors(list)` → `#seatMapWrap` 표시, 캔버스로 클릭 선택 가능.  
  - **develop에는 “좌석이 없을 때” 전용 안내 문구 없음** (빈 select만 있음).

- **캔버스**  
  - `initSeatCanvas()`: resize, 층 탭, 캔버스 클릭 시 `#seatId` 값 설정.  
  - `drawSeats()`: 등급별 색, 선택 좌석 테두리.

- **제출**  
  - POST 또는 PATCH로 `{ seatId, rating, content }` 전송 후 성공 시 `performances/{id}?tab=seatReview` 로 이동.

**템플릿:** `src/main/resources/templates/performance/seatReviewWrite.html`

- 구조: 뒤로가기, 제목, 공연 정보, 좌석 선택(배치도 영역 `#seatMapWrap` + select `#seatId`), 별점, 리뷰 내용, 작성 버튼.  
- develop과 동일한 마크업이면 현재 브랜치도 같은 요소 id를 사용하면 됨.

---

## 3. develop vs 현재 브랜치 (feature/demo-fixes) 차이 요약

| 항목 | develop | 현재 브랜치 (반영된 부분) |
|------|---------|---------------------------|
| 좌석 0개 시 seatReviewWrite | 별도 안내 없음, 빈 select | "이 공연의 공연장에 등록된 좌석이 없어…" 문구 + 작성 버튼 비활성화 |
| API·서비스 시그니처 | 위 표와 동일 | 동일 구조 유지 (참고 문서와 맞춤) |
| detail.js 좌석 탭 | withPos.length === 0 → 빈 배치도·Empty 표시 | 동일 로직 |

현재 브랜치에서 **develop과 완전히 동일하게** 맞추려면 `seatReviewWrite.js` 의 “좌석 없을 때” 안내 블록만 제거하면 됩니다.  
유지하려면 그대로 두고, 나머지 API·캔버스·폼 동작은 develop과 같은 흐름으로 맞추면 됩니다.

---

## 4. 참고 시 확인할 파일 경로 (develop)

```
src/main/java/.../performance/controller/PerformanceController.java  (좌석 API)
src/main/java/.../performance/service/PerformanceService.java        (getSeatsByPerformanceId, getSeatReviews, createSeatReview 등)
src/main/java/.../performance/dto/SeatOptionDto.java
src/main/java/.../performance/dto/SeatReviewItemDto.java
src/main/java/.../performance/dto/SeatReviewReqDto.java
src/main/java/.../venue/entity/Seat.java
src/main/java/.../venue/repository/SeatRepository.java
src/main/resources/static/js/performance/detail.js                  (좌석 리뷰 탭)
src/main/resources/static/js/performance/seatReviewWrite.js         (작성/수정)
src/main/resources/templates/performance/seatReviewWrite.html
```

위 파일들을 `git show develop:경로` 로 열어보면 develop 기준 최신 내용을 그대로 참고할 수 있습니다.
