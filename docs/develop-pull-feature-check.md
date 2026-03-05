# Develop 풀 후 기능 점검 (팀 지시 반영)

> **진행 순서**: DB 스키마 한번 지우고 생성 → 노션 임시 데이터(필요 시) 참고 → develop 풀 → 아래 기능 점검.  
> 잘 안 되면 말씀해 주세요.

---

## 0. DB 준비

- 스키마 **한번 지웠다가** 다시 생성
- 노션에 올려둔 **임시 데이터** 필요하면 참고해서 사용

---

## 1. 푸터·프로필·탑바·마이페이지 링크 점검

(수정하신 분이 푸터/프로필/탑바/마이페이지 건드리셨으니, **본인 개발 링크**만 한번씩 체크해 주세요.)

### 1.1 푸터 (`fragments/performer/footer`)

| 역할 | 링크 라벨 | URL | 점검 |
|------|-----------|-----|:----:|
| 비로그인 | 공연리스트 | `/performances` | ☐ |
| 비로그인 | 공연장 | `/venues` | ☐ |
| 비로그인 | 피드 | `/feed` | ☐ |
| 비로그인 | 로그인 | `/auth/login` | ☐ |
| USER | 공연리스트 | `/performances` | ☐ |
| USER | 공연장 | `/venues` | ☐ |
| USER | 피드 | `/feed` | ☐ |
| USER | DM | `/dm/list` | ☐ |
| USER | 채팅 | `/chats` | ☐ |
| PERFORMER | 모집글 | `/posts/performance` | ☐ |
| PERFORMER | 공연장 | `/venues/performer` → `/venues` 리다이렉트 | ☐ |
| PERFORMER | 홈 | `/feed` | ☐ |
| PERFORMER | DM | `/dm/list` | ☐ |
| PERFORMER | 채팅 | `/chats` | ☐ |
| HOST | 내 공연장 | `/venues/my` | ☐ |
| HOST | 대관관리 | `/venues/reservations/manage` | ☐ |
| HOST | 홈 | `/feed` | ☐ |
| HOST | DM | `/dm/list` | ☐ |
| HOST | 채팅 | `/chats` | ☐ |

### 1.2 탑바 (`fragments/topbar`)

| 항목 | URL | 점검 |
|------|-----|:----:|
| 브랜드(일반) | `/feed` | ☐ |
| 브랜드(공연자용 topbarPerformer) | `/` | ☐ |
| 프로필 영역 | `fragments/profile` 또는 `performer/profile` 포함 | ☐ |

### 1.3 프로필 드롭다운 (`fragments/profile`, `performer/profile`)

| 항목 | URL | 점검 |
|------|-----|:----:|
| 로그인 | `/auth/login` | ☐ |
| 마이페이지 | `/mypage` | ☐ |
| 프로필 변경 | `/profiles/select` | ☐ |
| 로그아웃 | POST `/logout` | ☐ |

### 1.4 마이페이지 (`community/mypage/mypage.html`)

**공통**

| 메뉴 | URL | 점검 |
|------|-----|:----:|
| ← (뒤로) | `/feed` | ☐ |
| 프로필 변경 | `/profiles/select` | ☐ |
| 회원정보 수정 | `/user/account` | ☐ |
| 알림 설정 | `/user/notification` | ☐ |
| 차단 목록 관리 | `/user/block` | ☐ |
| 로그아웃 | POST `/logout` | ☐ |
| 팔로잉/팔로워 | `/user/follow(...)` | ☐ |

**관람객 전용**

| 메뉴 | URL | 점검 |
|------|-----|:----:|
| 관람객 프로필 보기 | `/userprofile` | ☐ |
| 관람객 프로필 설정 | `/userprofile/setup` | ☐ |
| 내가 본 공연 | `/performances/watched` | ☐ |
| 내가 찜한 공연 | `/performances/wished` | ☐ |

**공연자 전용**

| 메뉴 | URL | 점검 |
|------|-----|:----:|
| 공연자 프로필 설정 | `/performerprofile/setup` | ☐ |
| 내가 작성한 공연자 모집글 | `/mypage/performer/posts` | ☐ |
| 내가 작성한 공연 모집글 | `/mypage/performer/performances` | ☐ |
| 내가 신청한 공연자 모집글 | `/mypage/performer/applied-posts` | ☐ |
| 내가 신청한 공연 | `/mypage/performer/applied-performances` | ☐ |
| 내가 모집한 공연자 | `/mypage/performer/recruited-performers` | ☐ |
| 내 대관 신청 목록 | `/venues/reservations/my` | ☐ |
| 내가 대관한 공연장 | `/mypage/performer/venues` | ☐ |
| 신청자 관리 | `/mypage/performer/manage` | ☐ |

**호스트 전용**

| 메뉴 | URL | 점검 |
|------|-----|:----:|
| 호스트 프로필 보기 | `/hostprofile/view` | ☐ |
| 호스트 프로필 설정 | `/hostprofile/setup` | ☐ |
| 내 공연장 관리 | `/venues/my` | ☐ |
| 대관 요청 관리 | `/venues/reservations/manage` | ☐ |

---

## 2. CSS 수정 방향

- 수정하고 싶은 **방향성** 있으시면 **관련 정보**(페이지/영역, 원하는 톤, 참고 링크 등) 제공해 주시면 됩니다.

---

## 3. 이미지

- **시간 되시면**: 프로필용 이미지  
  - 관람객 / 공연자 / 호스트 **각 5개씩** 골라서 노션에 올려 주세요.

---

## 4. PPT·시연

- PPT는 토요일 완성 예정, 발표는 지시하신 분이 진행.
- **각자 개발하신 부분은 시연 직접** 준비해 주시면 됩니다.

---

## 5. 마무리 (지시하신 분)

- 유저 프로필 관련 기능 수정 중 → 이후 **기능 구멍** 체크 → **CSS** 손보기 → **시큐리티** 씌우고 마무리.
- **시간 나시면**: 공연장·공연 **실제 사진·정보** 각 20개 정도 구해 두시면 발표/데모 시 도움 됩니다.
