# ENCORE 프로젝트 전체 기능 점검리스트

프로젝트의 **모든 기능**을 역할·화면·API 기준으로 정리한 점검리스트입니다.  
배포 전·발표 전·회귀 테스트 시 체크용으로 사용하세요.

---

## 1. 인증·회원

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 1.1 | 로그인 페이지 | GET `/auth/login` | ☐ |
| 1.2 | 로그인 처리 | POST `/auth/login` (Spring Security) | ☐ |
| 1.3 | 로그아웃 | POST `/logout` | ☐ |
| 1.4 | 회원가입 페이지 | GET `/auth/join` | ☐ |
| 1.5 | 회원가입 처리 | POST `/auth/join` | ☐ |
| 1.6 | 이메일 인증 | `/api/email/send`, `/api/email/verify` | ☐ |
| 1.7 | 비로그인 리다이렉트 | `/profiles/**`, `/setup/**`, `/mypage`, `/user/**` 등 | ☐ |

---

## 2. 프로필·역할

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 2.1 | 프로필 선택(전환) | GET `/profiles/select` | ☐ |
| 2.2 | 활성 프로필 전환 API | POST `/profiles/switch` | ☐ |
| 2.3 | 관람객 프로필 보기 | GET `/userprofile` | ☐ |
| 2.4 | 관람객 프로필 설정 | GET/POST `/userprofile/setup` | ☐ |
| 2.5 | 공연자 프로필 보기 | GET `/performerprofile/view` | ☐ |
| 2.6 | 공연자 프로필 설정 | GET/POST `/performerprofile/setup` | ☐ |
| 2.7 | 호스트 프로필 보기 | GET `/hostprofile/view` | ☐ |
| 2.8 | 호스트 프로필 설정 | GET/POST `/hostprofile/setup` | ☐ |
| 2.9 | 프로필 드롭다운(탑바) | 로그인/마이페이지/프로필변경/로그아웃 | ☐ |
| 2.10 | 로그인 시 프로필 이미지 | 헤더 원형 이미지 표시 | ☐ |

---

## 3. 홈·피드

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 3.1 | 홈(인덱스) | GET `/` | ☐ |
| 3.2 | 피드 목록 페이지 | GET `/feed` | ☐ |
| 3.3 | 피드 API | GET `/api/feed` (찜 임박 알림, 팔로우 추천 등) | ☐ |
| 3.4 | 피드 카드형 포스터 표시 | 포스터 이미지·공연 정보 | ☐ |

---

## 4. 공연

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 4.1 | 공연 목록 페이지 | GET `/performances` | ☐ |
| 4.2 | 공연 목록 API | GET `/api/performances` | ☐ |
| 4.3 | 공연 상세 페이지 | GET `/performances/{id}` | ☐ |
| 4.4 | 공연 상세 API | GET `/api/performances/{id}` | ☐ |
| 4.5 | 공연 등록 페이지 | GET `/performances/new` | ☐ |
| 4.6 | 공연 등록 API | POST `/api/performances` | ☐ |
| 4.7 | 공연 수정 | PUT `/api/performances/{id}` | ☐ |
| 4.8 | 공연 삭제 | DELETE `/api/performances/{id}` | ☐ |
| 4.9 | 찜(WISH) 토글 | POST `/api/performances/{id}/wish` | ☐ |
| 4.10 | 본공연(WATCHED) 토글 | POST `/api/performances/{id}/watched` | ☐ |
| 4.11 | 공연 리뷰 목록 | GET `/api/performances/{id}/reviews` | ☐ |
| 4.12 | 공연 리뷰 작성 페이지 | GET `/performances/{id}/reviews/new` | ☐ |
| 4.13 | 공연 리뷰 등록 | POST `/api/performances/{id}/reviews` | ☐ |
| 4.14 | 공연 리뷰 삭제 | DELETE `/api/performances/{id}/reviews/{reviewId}` | ☐ |
| 4.15 | Encore Pick | 리뷰 encore_pick 표시/저장 | ☐ |
| 4.16 | 좌석 리뷰 목록·요약 | GET seat-reviews API | ☐ |
| 4.17 | 좌석 리뷰 작성 페이지 | GET `/performances/{id}/reviews/seats/new` | ☐ |
| 4.18 | 좌석 리뷰 등록 | POST `/api/performances/{id}/seat-reviews` | ☐ |
| 4.19 | 좌석 리뷰 삭제 | DELETE seat-reviews API | ☐ |
| 4.20 | 인기 공연 | GET `/api/performances/hot` | ☐ |
| 4.21 | 내가 본 공연 목록 | GET `/performances/watched` | ☐ |
| 4.22 | 내가 찜한 공연 목록 | GET `/performances/wished` | ☐ |

---

## 5. 공연장·대관

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 5.1 | 공연장 목록 페이지 | GET `/venues` | ☐ |
| 5.2 | 공연장 목록 API | GET `/api/venues` | ☐ |
| 5.3 | 공연장 상세 페이지 | GET `/venues/{id}` | ☐ |
| 5.4 | 공연장 상세 API | GET `/api/venues/{id}` | ☐ |
| 5.5 | 공연장 등록 페이지 | GET `/venues/new` | ☐ |
| 5.6 | 공연장 등록 API | POST `/api/venues` (multipart) | ☐ |
| 5.7 | 공연장 수정 페이지 | GET `/venues/{id}/edit` | ☐ |
| 5.8 | 공연장 수정 API | PUT `/api/venues/{id}` | ☐ |
| 5.9 | 공연장 삭제 | DELETE `/api/venues/{id}` | ☐ |
| 5.10 | 내 공연장 목록 페이지 | GET `/venues/my` | ☐ |
| 5.11 | 내 공연장 API | GET `/api/venues/my` | ☐ |
| 5.12 | 대관 신청 폼 | GET `/venues/{id}/reservation` | ☐ |
| 5.13 | 대관 신청 API | POST `/api/reservations` | ☐ |
| 5.14 | 내 대관 신청 목록 | GET `/venues/reservations/my` | ☐ |
| 5.15 | 내 대관 API | GET `/api/reservations/my` | ☐ |
| 5.16 | 대관 요청 관리(호스트) | GET `/venues/reservations/manage` | ☐ |
| 5.17 | 공연장별 대관 목록 API | GET `/api/venues/{id}/reservations` | ☐ |
| 5.18 | 대관 승인 | POST `/api/reservations/{id}/approve` | ☐ |
| 5.19 | 대관 거절 | POST `/api/reservations/{id}/reject` | ☐ |
| 5.20 | 좌석 구조(호스트 직접 입력) | venueForm STEP 2, 층·좌석·등급 | ☐ |
| 5.21 | 공연자용 공연장 목록 | GET `/venues/performer` → `/venues` 리다이렉트 | ☐ |

---

## 6. 모집글(커뮤니티)

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 6.1 | 공연 모집글 목록 | GET `/posts/performance` | ☐ |
| 6.2 | 공연 모집글 상세 | GET `/posts/performance/{id}` | ☐ |
| 6.3 | 공연 모집글 작성 | GET/POST `/posts/performance/write` | ☐ |
| 6.4 | 공연 모집글 수정 | GET `/posts/performance/{id}/edit` | ☐ |
| 6.5 | 공연자 추천 | GET `/posts/performance/{id}/recommend` | ☐ |
| 6.6 | 공연 모집글 API | `/api/posts/performance` | ☐ |
| 6.7 | 공연자 모집글 목록 | GET `/posts/performer` | ☐ |
| 6.8 | 공연자 모집글 상세 | GET `/posts/performer/{id}` | ☐ |
| 6.9 | 공연자 모집글 작성 | GET/POST `/posts/performer/write` | ☐ |
| 6.10 | 공연자 모집글 수정 | GET `/posts/performer/{id}/edit` | ☐ |
| 6.11 | 공연자 모집글 API | `/api/posts/performer` | ☐ |
| 6.12 | 모집 신청 | POST `/api/posts/{postId}/apply` | ☐ |

---

## 7. 채팅

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 7.1 | 채팅방 목록(전체) | GET `/chats` | ☐ |
| 7.2 | 공연별 채팅 목록 | GET `/performances/{id}/chats` | ☐ |
| 7.3 | 채팅 모집글 목록 | chatPerformanceList, chatJoinList 등 | ☐ |
| 7.4 | 채팅 모집글 작성 | GET/POST 채팅 모집 폼 | ☐ |
| 7.5 | 채팅 모집글 상세 | chatPostDetail | ☐ |
| 7.6 | 채팅 모집글 수정 | chatPostUpdateForm | ☐ |
| 7.7 | 채팅 유형 태그 | REVIEW, TAXI_SHARE, AFTER_PARTY, GENERAL | ☐ |
| 7.8 | 채팅방 참여 | POST `/api/chat/join` | ☐ |
| 7.9 | 핫 채팅 API | GET `/api/chat/hot` | ☐ |
| 7.10 | 내 채팅방 목록 API | GET `/api/users/chats` | ☐ |
| 7.11 | 채팅방 입장 페이지 | GET `/chat/{roomId}` | ☐ |
| 7.12 | 채팅 메시지 전송 API | POST `/api/chat/room/{roomId}/messages` | ☐ |
| 7.13 | 채팅 메시지 목록 API | GET `/api/chat/room/{roomId}/messages` | ☐ |
| 7.14 | 채팅방 나가기 | POST `/api/chat/room/{roomId}/exit` | ☐ |
| 7.15 | 참여자 목록 | GET `/api/chat/room/{roomId}/participants` | ☐ |
| 7.16 | WebSocket 실시간 채팅 | ChatWebSocketController | ☐ |

---

## 8. DM

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 8.1 | DM 목록 페이지 | GET `/dm/list` | ☐ |
| 8.2 | DM 목록 API | `/api/dms/**` | ☐ |
| 8.3 | DM 방 입장 | GET `/dm/{roomId}` | ☐ |
| 8.4 | DM 메시지 송수신 | API·WebSocket | ☐ |

---

## 9. 소셜(팔로우·차단·신고)

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 9.1 | 팔로우 | POST `/api/users/{id}/{mode}/follow` | ☐ |
| 9.2 | 언팔로우 | (relation API) | ☐ |
| 9.3 | 팔로잉 목록 | GET `/api/users/{id}/{mode}/following` | ☐ |
| 9.4 | 팔로워 목록 | GET `/api/users/{id}/{mode}/follower` | ☐ |
| 9.5 | 팔로우/팔로워 페이지 | GET `/user/follow` (following-follower) | ☐ |
| 9.6 | 차단 | POST `/api/users/relations/block` | ☐ |
| 9.7 | 차단 해제 | POST `/api/users/relations/unblock` | ☐ |
| 9.8 | 차단 목록 API | GET `/api/users/relations/blocks` | ☐ |
| 9.9 | 차단 목록 페이지 | GET `/user/block` (blockList) | ☐ |
| 9.10 | 신고 폼 | GET `/report` (reportForm) | ☐ |
| 9.11 | 신고 API | `/api/community/reports` | ☐ |
| 9.12 | 추천 친구 | GET `/api/users/me/recommended-friends` | ☐ |

---

## 10. 마이페이지·계정 설정

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 10.1 | 마이페이지 진입 | GET `/mypage` | ☐ |
| 10.2 | 회원정보 수정(닉네임·비밀번호) | GET/POST `/user/account` | ☐ |
| 10.3 | 알림 설정 페이지 | GET `/user/notification` | ☐ |
| 10.4 | 알림 설정 API | GET/PUT `/api/users/settings/notifications` | ☐ |
| 10.5 | 관람객: 내가 본 공연 | `/performances/watched` | ☐ |
| 10.6 | 관람객: 찜한 공연 | `/performances/wished` | ☐ |
| 10.7 | 공연자: 내 모집글 | `/mypage/performer/posts` | ☐ |
| 10.8 | 공연자: 내 공연 모집글 | `/mypage/performer/performances` | ☐ |
| 10.9 | 공연자: 신청한 공연자 모집글 | `/mypage/performer/applied-posts` | ☐ |
| 10.10 | 공연자: 신청한 공연 | `/mypage/performer/applied-performances` | ☐ |
| 10.11 | 공연자: 모집한 공연자 | `/mypage/performer/recruited-performers` | ☐ |
| 10.12 | 공연자: 내 대관 신청 목록 | `/venues/reservations/my` | ☐ |
| 10.13 | 공연자: 내가 대관한 공연장 | `/mypage/performer/venues` | ☐ |
| 10.14 | 공연자: 신청자 관리 | `/mypage/performer/manage` | ☐ |
| 10.15 | 호스트: 내 공연장 관리 | `/venues/my` | ☐ |
| 10.16 | 호스트: 대관 요청 관리 | `/venues/reservations/manage` | ☐ |
| 10.17 | 회원 프로필 보기(타인) | GET `/userprofile/{id}` 등 (memberProfile) | ☐ |

---

## 11. 푸터·공통 UI

| # | 기능 | 경로/동작 | 점검 |
|---|------|-----------|:----:|
| 11.1 | 푸터(비로그인) | 공연리스트, 공연장, 피드, 로그인 | ☐ |
| 11.2 | 푸터(USER) | 공연리스트, 공연장, 피드, DM, 채팅 | ☐ |
| 11.3 | 푸터(PERFORMER) | 모집글, 공연장, 홈, DM, 채팅 | ☐ |
| 11.4 | 푸터(HOST) | 내 공연장, 대관관리, 홈, DM, 채팅 | ☐ |
| 11.5 | 탑바 브랜드 링크 | 일반 `/feed`, 공연자용 `/` | ☐ |
| 11.6 | 프래그먼트 CSS/JS | profile.css, profile.js, footer.css 등 외부 로드 | ☐ |

---

## 12. 데이터·빌드·보안

| # | 기능 | 내용 | 점검 |
|---|------|------|:----:|
| 12.1 | 빌드 | `.\gradlew clean build` | ☐ |
| 12.2 | 테스트 | `.\gradlew test` | ☐ |
| 12.3 | 테스트 데이터 | `data-test-insert.sql` 실행 | ☐ |
| 12.4 | DB 스키마 | performance category/status, performance_schedule 등 | ☐ |
| 12.5 | 비밀번호 해시(개발용) | GET `/dev/bcrypt-password123` (필요 시) | ☐ |
| 12.6 | 인증 필수 경로 | SecurityConfig permitAll 제외 구간 | ☐ |

---

## 13. 명세 대비 미구현·부분 구현 (참고)

| 구분 | 항목 | 상태 |
|------|------|------|
| 공연자 | 파티 매칭(파트·1분 음원) | ⚠️ 모집글 수준, 파트/음원 필드 없음 |
| 공연자 | 세트리스트 필드 | ⚠️ 없음 |
| 공연자 | 긴급 매칭(노쇼 대체) | ❌ 미구현 |
| 공연장 | 화장실/변기 수 필드 | ⚠️ 없음 |
| 관리자 | 회원·공연·게시판·리뷰 관리 | ⏸️ 추후 개발 |
| 공연장 리뷰 | 공연자용 공연장 리뷰 | ⏸️ 미도입 예정 |

---

## 14. 스모크 시나리오 (한 번에 점검)

| 순서 | 시나리오 | 점검 |
|------|----------|:----:|
| 1 | 로그인 → 프로필 선택(관람객/공연자/호스트) | ☐ |
| 2 | 피드 → 공연 상세 → 찜/본공연 → 공연 리뷰·좌석 리뷰 | ☐ |
| 3 | 공연 상세 → 공연 채팅 목록 → 모집글 작성/참여 → 채팅방 입장 | ☐ |
| 4 | DM 목록 → DM 방 입장·메시지 | ☐ |
| 5 | 마이페이지 → 관람객: 본공연/찜한공연, 알림설정, 차단목록 | ☐ |
| 6 | 마이페이지 → 공연자: 모집글, 신청목록, 내 대관, 대관한 공연장 | ☐ |
| 7 | 마이페이지 → 호스트: 내 공연장, 대관 요청 관리 | ☐ |
| 8 | 공연장 목록 → 상세 → (공연자) 대관 신청 | ☐ |
| 9 | 푸터: 역할별 메뉴(공연리스트/홈/DM/채팅 등) 클릭 이동 | ☐ |

---

## 15. 문서 참고

- **명세 대비 구현**: `docs/spec-compliance.md`
- **머지 전 체크**: `docs/pre-merge-checklist.md`
- **역할별 플로우**: `docs/role-entry-and-user-flow.md`
- **develop 풀 후 점검**: `docs/develop-pull-feature-check.md`

---

*기능 추가·변경 시 이 점검리스트도 함께 갱신하면 협업·배포 시 유용합니다.*
