# ENCORE 요소 배치·구조 점검

전체 페이지의 레이아웃 구조(래퍼, 헤더/메인/푸터 위치, 하단 여백, 네이밍)를 정리했습니다.

---

## 1. 루트 구조 패턴

### 1.1 사용 중인 패턴

| 패턴 | 구조 | 사용 페이지 예 |
|------|------|----------------|
| **A** | `app-wrapper` > `topbar` > `main.app-main` > (내용) > `footer` | 피드, 공연 리스트, 공연 상세, 공연장 리스트 |
| **B** | `app-wrapper` > `topbar` > `app-container` > (top-bar, 프로필, 메뉴) > `footer` | 마이페이지, 커뮤니티(모집글/공연자글) |
| **C** | `app-container` > `header` > `main` > `footer` (topbar 없음) | 채팅 리스트(chatJoinList, chatPerformanceList 등) |
| **D** | `app-wrapper` > `topbar` > `header` > `main` | 공연장 상세, 대관/예약, 내 공연장 |
| **E** | 단일 컨테이너 (wrapper 없음) | 프로필 선택(profile-container), 로그인/회원가입 |

- **A**: 상단바 + 메인 본문만 있을 때.
- **B**: 상단바 아래에 “한 겹” 더 감싸는 구조(마이페이지, 포스트 작성/상세 등).
- **C**: 채팅 계열은 상단바 없이 `app-container`만 사용.
- **D**: 페이지별 `header` + `main` 조합.
- **E**: 독립 페이지(로그인, 프로필 선택 등).

### 1.2 정리 제안

- **topbar 유무**: 채팅·DM은 의도적으로 topbar 없이 가는지 확인 후, 필요하면 `app-wrapper` + topbar 추가 검토.
- **네이밍 통일**: 본문 영역을 가능한 한 `main` + `app-main` 또는 `app-content` 중 하나로 통일하면, 상단바용 `padding-top` 규칙(`.encore-topbar ~ .app-main` 등)과 푸터용 `padding-bottom` 규칙을 한 곳에서 관리하기 쉬움.

---

## 2. 본문 영역 클래스 혼용

본문(콘텐츠 영역)에 아래 클래스가 섞여 있습니다.

| 클래스 | 의미 | 사용처 |
|--------|------|--------|
| `app-main` | 메인 콘텐츠 | 피드, 공연 리스트/상세, 공연장 리스트, venue 대부분, 리뷰 작성 |
| `app-content` | 메인 콘텐츠 | 채팅(chatRoom, chatPostForm, dm 등) |
| `app-container` | 상단바 다음 “한 겹” 래퍼 (header+본문 포함) | 마이페이지, 커뮤니티 포스트 |
| `main-content` | 메인 콘텐츠 | reportForm |
| `profile-group` / `profile-section` | 메인 역할 | 프로필 선택, 회원 프로필 |
| `settings-content` | 메인 역할 | 알림 설정 |

- **topbar와의 관계**: `topbar.css`에서 `.encore-topbar ~ main`, `~ .app-main`, `~ .app-container`에 `padding-top: 56px`를 주고 있어, `main` 또는 `app-main`/`app-container`가 topbar 다음 형제로 오면 상단 여백이 적용됨.
- **제안**: “페이지 본문”은 `main` 시맨틱을 유지하고, 클래스는 `app-main`(목록/상세) vs `app-content`(채팅) 정도로만 나누고, 나머지는 점진적으로 맞추면 유지보수에 유리함.

---

## 3. header 위치

- **대부분**: `header`가 `main` 밖(형제)으로, 상단바 바로 아래에 페이지 헤더(뒤로가기, 제목 등).
- **예외**:
  - **피드, 공연장 리스트**: `main` > `header` (페이지 제목이 main 안 첫 블록).
  - **리뷰/좌석리뷰**: `main` > `header.review-header` (main 안에서만 사용).

시맨틱상으로는 “페이지 헤더”는 `main` 밖에 두는 쪽이 일반적이지만, 현재도 동작에는 문제 없음. 통일할 경우 피드/공연장 리스트만 `header`를 `main` 밖으로 빼도 됨.

---

## 4. 고정 푸터와 하단 여백

고정 하단 네비(`.footer-fixed`)를 쓰는 페이지는 **본문 영역에 `padding-bottom`**이 있어야 마지막 콘텐츠가 푸터에 가리지 않습니다.

### 4.1 이미 충분한 페이지

- **피드**: `.app-main` → `padding-bottom: 92px` (feed/list.css)
- **마이페이지**: `.app-container` → `padding: … 100px …` (mypage.css)
- **venue**: reservationForm, myReservations, venueReservations → 100px/200px 등 적용
- **채팅**: `.chat-list-page` → `padding-bottom: 80px` (chat.css)

### 4.2 보강이 필요한 페이지

- **공연 리스트** (performance/list): `.app-main`에 `padding: 16px`만 있어 하단 여백 없음 → **padding-bottom 추가 권장** (예: 92px).
- **공연 상세** (performance/detail): `.app-main`에 `min-height`만 있고 `padding-bottom` 없음 → **padding-bottom 추가 권장**.
- **공연장 리스트** (venue/list): 푸터 사용 시 list 전용 CSS에서 `.app-main`에 `padding-bottom` 확인 필요 (현재 venue list.css가 venue-form과 혼용된 부분 있음).

---

## 5. 상단바와의 간격

- **규칙**: `.encore-topbar ~ main`, `~ .app-main`, `~ .app-container`에 `padding-top: 56px` 적용 (topbar.css).
- **주의**: topbar **다음에 오는** 첫 번째 `main` 또는 `.app-main`/`.app-container`만 대상. 중간에 `div.top-slot` 등이 있어도, 그 뒤의 `main`은 형제이므로 `~`로 매칭됨 → 현재 구조에서는 문제 없음.

---

## 6. 기타 구조 포인트

- **performance/list**: `top-slot`, `bottom-slot`으로 상·하 여백용 div 사용. 시각적 여백만 필요하다면 CSS margin/padding으로 대체 가능.
- **community 쪽**: 일부 템플릿에서 `app-wrapper`, `app-container`를 인라인 `<style>`로 정의. 공통 theme/list/mypage CSS로 빼면 유지보수에 유리함.
- **반응형**: venueForm, reservationForm 등은 `@media`로 모바일/PC 분기 있음. 나머지 목록/상세도 동일한 breakpoint(예: 768px) 기준으로 통일하면 일관됨.

---

## 7. 적용 권장 사항 요약

| 우선순위 | 항목 | 조치 |
|----------|------|------|
| 높음 | 공연 리스트 하단 여백 | `.app-main`에 `padding-bottom: 92px` (또는 80px) 추가 |
| 높음 | 공연 상세 하단 여백 | `.app-main`에 `padding-bottom: 92px` 추가 |
| 중간 | 공연장 리스트 CSS | list 전용 스타일에서 `.app-main` + 푸터용 padding 확인 |
| 낮음 | header 위치 통일 | 피드/공연장 리스트에서 header를 main 밖으로 이동 (선택) |
| 낮음 | 본문 클래스 정리 | app-main / app-content 의미 정리 후 점진 통일 |

위 순서로 반영하면 푸터 가림이 사라지고, 이후 레이아웃 통일 작업이 수월해집니다.
