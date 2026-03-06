# 머지/푸시 전 필수 점검 (최종 체크리스트)

명세·테스트케이스·코드 기준으로 **필수 개발사항**과 **이미 반영한 수정**을 정리했습니다.  
**푸시 직전**에는 아래 **📋 푸시 전 실행 체크리스트**를 순서대로 확인하세요.

- **develop 풀 후 기능 점검**(푸터/탑바/프로필/마이페이지 링크 등): → `docs/develop-pull-feature-check.md` 참고.

---

## ✅ CSS/JS 외부 파일 (프래그먼트)

| 파일 | 용도 |
|------|------|
| `static/css/fragments/profile.css` | 프로필 드롭다운 스타일 (profile, performer/profile 공통) |
| `static/js/fragments/profile.js` | 프로필 드롭다운 토글 (동일) |
| `static/css/fragments/footer.css` | fragments/footer (items 전달용) |
| `static/css/fragments/performer-footer.css` | fragments/performer/footer (하단 고정 네비) |

프래그먼트에서 인라인 `<style>`/`<script>` 제거 후 위 외부 파일 참조로 통일됨. 페이지는 프래그먼트 포함 시 자동으로 해당 CSS/JS 로드.

---

## ✅ 지금 할 일 체크리스트 (체크하면서 진행)

| 체크 | 할 일 |
|:---:|-------|
| ☐ | **빌드** — `.\gradlew clean build` |
| ☐ | **테스트** — `.\gradlew test` |
| ☐ | **프로필 드롭다운** — 오른쪽 위 아이콘 클릭 시 로그인/마이페이지/로그아웃 노출 확인 |
| ☐ | **프로필 CSS/JS** — 프로필 포함 페이지에서 드롭다운 스타일·토글 정상 동작 (외부 파일 로드 확인) |
| ☐ | **로그인 시 프로필 사진** — 프로필 이미지 있으면 헤더에 원형으로 표시되는지 확인 |
| ☐ | **스모크** — 로그인 → 프로필 선택 → 피드 → 공연 상세 → 찜/리뷰 → 채팅/DM |
| ☐ | **마이페이지** — 내가 본 공연, 찜한 공연, 알림 설정, 차단 목록 링크 동작 |
| ☐ | **푸터** — 공연리스트 / 홈 / DM / 채팅방 이동 (역할별 메뉴: 비로그인·USER·PERFORMER·HOST) |
| ☐ | **DB 테스트 데이터** — 필요 시 `data-test-insert.sql` [추가] 블록 실행, 전체 좌석은 `data-test-insert-seats-full.sql` 선택 실행 |
| ☐ | **테스트 로그인** — password123 안 되면 앱 실행 → `/dev/bcrypt-password123` 에서 해시 복사 → `UPDATE users SET password_hash = '...'` 실행 |
| ☐ | **문서** — 기능 추가·변경 시 필요하면 README·docs/*.md 반영 여부 확인 |
| ☐ | **푸시** — 위 다 확인 후 커밋 & 푸시 |

**"아직 피드가 없습니다" / "공연 리스트 목록 조회 실패" 나올 때**
- **공연 목록 실패**: 서버 터미널에 500 스택트레이스 나오면 → DB 스키마 확인(performance 테이블에 `category`, `status` 컬럼 있는지). JPA `ddl-auto=update` 쓰거나 스키마 맞춘 뒤 **data-test-insert.sql** 실행.
- **피드만 비어 있음**: 테스트 데이터 없으면 정상. 위 SQL 실행 후 다시 로드.

---

## 📋 푸시 전 실행 체크리스트 (잊지 말고 할 것)

| 순서 | 항목 | 명령/확인 |
|------|------|-----------|
| 1 | **빌드 클린** | `.\gradlew clean build` (또는 `./gradlew clean build`) |
| 2 | **테스트 통과** | `.\gradlew test` — 실패 시 푸시 보류 |
| 3 | **DB + 테스트 데이터** | 스키마 적용 후 `data-test-insert.sql` 실행 (로컬/배포 환경) |
| 4 | **스모크 테스트** | 로그인 → 프로필 선택 → 피드 → 공연 상세 → 찜/리뷰 → 채팅/DM 진입 |
| 5 | **마이페이지 링크** | "내가 본 공연"/"찜한 공연", "알림 설정", "차단 목록" 클릭 동작 |
| 6 | **푸터 링크** | 공연리스트 / 홈 / DM / 채팅방 이동 확인 |
| 7 | **(선택)** 호스트/공연자 | 공연장 등록·대관 관리 / 대관 신청·내 대관 |

- **1~2번 통과 후**에 푸시하는 것을 권장합니다.

---

## ✅ 전체 기능 점검 (주요 경로)

| 구분 | 경로/기능 | 비고 |
|------|-----------|------|
| **진입** | `/` → 홈, `/auth/login`, `/auth/join`, `/profiles/select`, `/profiles/switch` | 로그인·프로필 전환 |
| **피드** | `/feed`, `/api/feed` | 피드 목록 |
| **공연** | `/performances`, `/performances/{id}`, `/api/performances`, 리뷰/좌석리뷰, 찜/본공연 | 공연 목록·상세·리뷰·wish |
| **공연장** | `/venues`, `/venues/performer`, `/venues/{id}`, `/venues/my`, `/venues/{id}/reservation`, `/venues/reservations/my`, `/venues/reservations/manage` | 공연장 목록·상세·대관 |
| **모집** | `/posts/performance`, `/posts/performer`, 공연/공연자 모집글 CRUD | 모집글·신청 |
| **마이페이지** | `/mypage`, `/userprofile`, `/userprofile/setup`, `/performerprofile/*`, `/hostprofile/*`, `/user/notification`, `/user/block`, `/user/follow`, `/performances/watched`, `/performances/wished` | 관람객·공연자·호스트 |
| **공연자 전용** | `/mypage/performer/*` (posts, performances, applied-*, recruited-performers, venues, manage) | 공연자 마이페이지 |
| **채팅/DM** | `/chats`, `/performances/{id}/chats`, `/chats/join`, `/chat/{roomId}`, `/dm/list`, `/dm/{roomId}`, 관련 API | 공연 채팅(유형: 후기/택시/뒤풀이/일반), DM |
| **기타** | `/report`, `/api/community/reports`, `/dev/bcrypt-password123` | 신고·개발용 |

---

## ✅ 서비스·프론트 정합성 점검 요약 (참고)

- **빌드**: `clean build` (-x test) 및 `test` 실행 시 **성공** 확인됨.
- **API ↔ JS 경로**: 아래 경로들이 컨트롤러와 일치함.
  - `/api/performances`, `/api/performances/{id}/reviews`, `/api/performances/{id}/seat-reviews`, `/api/performances/{id}/wish`, `/api/performances/{id}/watched`
  - `/api/venues`, `/api/venues/my`, `/api/venues/{id}/reservations`, `/api/reservations`, `/api/reservations/my`, `/api/reservations/{id}/approve`, `/api/reservations/{id}/reject`
  - `/api/feed`, `/api/posts/performance`, `/api/posts/performer`, `/api/chat/join`, `/api/chat/hot`, `/api/users/chats`, `/api/chat/room/{id}/messages`, `/api/dms/**`
  - `/api/community/reports`, `/api/users/settings/notifications`, `/api/users/relations/blocks`, `/profiles/switch`
- **페이지 라우트**: `/performances/{id}/reviews/new`, `/performances/{id}/reviews/seats/new`, `/venues/{id}`, `/venues/{id}/reservation`, `/dm/list`, `/chats` 등 HTML/JS에서 사용하는 URL과 컨트롤러 매핑 일치.

---

## ✅ 이번 점검에서 수정한 것

| 항목 | 내용 |
|------|------|
| **피드 푸터 링크** | "DM" → `/dm/list`, "채팅방" → `/chats`, "좌석리뷰"(잘못된 `/seats`) 제거. 공연리스트·홈·DM·채팅방 4개로 정리. |
| **마이페이지 링크** | "알림 설정" `#` → `/user/notification`, "차단 목록 관리" `/relations/blocks` → `/user/block` |
| **userprofile 흐름** | 관람객 프로필 보기(user-view) → "관람객 프로필 설정"·"프로필 변경"·"마이페이지로" 링크 정리. userprofile 기능은 /userprofile, /userprofile/setup 쪽으로 유지. |
| **프로필 변경** | 마이페이지 공통 설정에 "프로필 변경" → `/profiles/select` 추가. 상단바 드롭다운에도 "프로필 변경" 추가. |

---

## ✅ 필수 개발사항 점검 결과 (잊은 것 없음)

### 인증·프로필
- 회원가입/로그인: ✅
- 비로그인 시 로그인 리다이렉트: `/profiles/select`, `/userprofile`, `/userprofile/setup`, `/performerprofile/setup`, `/mypage`, 공연자/공연 모집글 작성·수정, `/chat/{roomId}`, `/dm/{roomId}` → ✅ 컨트롤러에서 처리
- SecurityConfig: `/profiles/**`, `/setup/**`, `/user/**` 인증 필수. 나머지 permitAll(페이지는 열리나 API는 401 가능)

### 관람객(U)
- U-1 공연/좌석 리뷰 분리, U-2 북마크·마이페이지, U-3 채팅방 생성·참가, U-4 찜한 공연 30분 전 알림: ✅
- Encore pick: ✅

### 공연자(A) / 호스트(V)
- A-2 대관 신청·승인/거절, V-1 공연장 등록(좌석 호스트 직접 입력), V-2/V-3 공연·모집: ✅
- 공연자/호스트 전용 **페이지** 로그인 리다이렉트: `/venues/new`, `/venues/my`, `/venues/{id}/edit`, `/venues/reservations/my`, `/venues/reservations/manage` 는 **컨트롤러에서 비로그인 시 리다이렉트 없음**. Security는 permitAll이라 페이지는 열림 → API 호출 시 401. **선택**: 발표 시나리오에서만 사용한다면 그대로 두어도 됨. 필요 시 위 경로에 `userDetails == null → redirect:/auth/login` 추가.

### 데이터
- `data-test-insert.sql`: performance category/status, performance_schedule 포함. ✅
- `review.encore_pick`: JPA `ddl-auto=update` 시 컬럼 자동 추가. 기존 DB에 스키마 직접 관리 시 `ALTER TABLE review ADD COLUMN encore_pick VARCHAR(200);` 필요.

---

## ⚠️ 알려진 이슈 (머지 후/발표 시 결정)

| ID | 내용 | 권장 |
|----|------|------|
| **V-C-10-14** | 채팅방 나가기 후 이동 | 현재 `/chats` → 동작함. 테스트케이스는 `/chat/list` 기대. URL 통일 여부만 결정 |
| **V-U-4-2** | 알림 설정 저장 후 새로고침 시 유지 | API 있음. 저장 주체(계정 vs 프로필) 정한 뒤 GET/PUT 일치시키기. 발표에서 제외 가능 |
| **유저 계정 정보** | 닉네임·비밀번호 수정: `/user/account` (GET/POST) 구현됨. 이메일 변경은 미지원(동일 계정 유지). | 완료 |

---

## 📋 머지 직전 실행 체크리스트 (체크박스용)

- [ ] `.\gradlew clean build` 성공
- [ ] `.\gradlew test` 성공
- [ ] DB 스키마 적용 후 `data-test-insert.sql` 실행
- [ ] **프래그먼트** — 프로필·푸터 페이지에서 외부 CSS/JS 로드 시 스타일·드롭다운 동작 확인
- [ ] 로그인 → 프로필 선택 → 피드 → 공연 상세 → 찜/리뷰(Encore pick) → 채팅 목록/참가 → DM 목록/방 진입
- [ ] 마이페이지 → 관람객 시 "내가 본 공연"/"찜한 공연", "알림 설정", "차단 목록" 링크 동작 확인
- [ ] 피드 푸터에서 "공연리스트"/"홈"/"DM"/"채팅방" 클릭 시 해당 페이지 이동 확인 (역할별 메뉴)
- [ ] (선택) 호스트: 공연장 등록/내 공연장/대관 관리. 공연자: 대관 신청/내 대관, `/venues/performer`

---

## 정리

- **필수 개발사항**: 명세·테스트케이스 기준으로 누락된 필수 기능 없음.
- **수정 완료**: 피드 푸터 링크, 마이페이지 알림 설정/차단 목록 링크.
- **선택**: 호스트/공연자 전용 페이지 비로그인 리다이렉트, V-C-10-14 URL 통일, V-U-4-2 알림 저장 정책.

이 체크리스트까지 확인한 뒤 머지하면 됩니다.

---

## 📋 develop 머지 후 점검 (origin/develop 기준)

머지 후 아래만 한 번 확인하면 됨.

| 항목 | 확인 |
|------|------|
| **푸터** | develop에서 `footer(items=${null})` 쓰던 페이지(채팅·DM·신고·관계·회원프로필) → `performer/footer` 로 통일했는지 (빈 푸터/오류 방지) |
| **채팅** | `chatPerformanceList`, `chatJoinList`, `chatJoinListFull`, `chatPostForm`, `chatPostUpdateForm`, `chatPostDetail`, `dmList` 하단 네비 동작 |
| **기타** | `reportForm`, `memberProfile`, `following-follower`, `blockList` 푸터 |
| **fragments/footer** | `items`가 null이면 빈 링크로 렌더되도록 방어 처리됨. 기본은 역할별 푸터는 `performer/footer` 사용 권장 |

- develop 최근 머지: PR #109 (chat-test), `chatPerformanceList.html` 링크 수정 등 반영됨.

---

## 📌 머지 후 할 일 (기억용)

**진행 순서:**

1. **다른 분들 개발 끝나면** → 각자 머지
2. **내가 머지** → 컨플릭트 해결 (AI와 함께)
3. **공연 관련** — 공연 기능·페이지·추가로 필요한 것 점검
4. **시큐리티** — Spring Security 전체 정리
5. **CSS** — 전역·페이지별 스타일 수정

상세:
- **보안** — Spring Security 활용
- **로그인 후 프로필** — 프로필 요구사항 관련 기능
- **CSS 총정리** — 전역·페이지별 스타일 정리
