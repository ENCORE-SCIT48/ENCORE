# origin/main 수정 필수·없는 기능 체크리스트

> origin/main 브랜치 기준. 프론트 링크/API와 백엔드 라우트·로직 불일치 정리.

---

## 🔴 반드시 수정 (누르면 404·500)

### 1. 본 공연 목록 / 찜한 공연 목록 — **404**
- **위치**: 마이페이지(관람객) → "내가 본 공연", "내가 찜한 공연"
- **원인**: `mypage.html`이 `/performances/watched`, `/performances/wished`로 링크하는데, **PerformancePageController에 해당 GET 매핑 없음**. API(`/api/performances/watched`, `wished`)만 있음.
- **조치**: `PerformancePageController`에 추가
  - `@GetMapping("/watched")` → 로그인 체크 후 `performance/list` 반환, `listMode=watched`
  - `@GetMapping("/wished")` → 동일하게 `listMode=wished`
  - `list.html`에 `listMode` 전달, `list.js`에서 `data-list-mode` 읽어 `state.filter`(VIEWED/BOOKMARK) 설정 및 탭 활성화

---

### 2. 공연 수정 — **404**
- **위치**: 공연 상세(본인 공연) → "공연 수정" 버튼
- **원인**: `detail.js`가 `window.location.href = '/performances/${performanceId}/edit'` 로 이동하는데, **PerformancePageController에 `GET /performances/{id}/edit` 없음**.
- **조치**: `@GetMapping("/{performanceId}/edit")` 추가 → 공연자 체크 후 `performance/new` 반환, `performanceId` 모델에 담기 (동일 폼으로 수정)

---

### 3. 마이페이지 — **profileId null 시 500 또는 링크 깨짐**
- **위치**: 로그인 직후 프로필 선택 없이 `/mypage` 진입
- **원인**: `CustomUserDetails.activeProfileId`가 null일 수 있음. 이 상태에서 `relationService.countFollower(profileId, activeMode)` 호출 시 NPE 또는 쿼리 오류, 템플릿 `targetId=${profileId}` 도 null.
- **조치**: `MypageController`에서 `profileId == null`이면 `ProfileService.findProfileIdByMode(activeMode, user)` 로 조회 후 세션 반영. 해당 모드 프로필 없으면 `redirect:/profiles/select?error=no_profile`

---

### 4. 프로필 선택 페이지 — **공연자/호스트 없으면 500**
- **위치**: `/profiles/select` (공연자·호스트 프로필이 없는 계정)
- **원인**: `ProfileController.selectPage`에서 `getPerformerProfile()`, `getHostProfile()` 호출 → 프로필 없으면 **예외 발생**.
- **조치**: `getPerformerProfileOrNull`, `getHostProfileOrNull` 추가 후 select에서 사용. 템플릿은 `th:if="${performerProfile != null}"` 등으로 카드 조건부 노출. `switchMode`에서 `findProfileIdByMode` 예외 catch → `redirect:/profiles/select?error=no_profile`

---

### 5. 회원가입 이메일 인증 — **가입 시 "이메일 인증이 필요합니다"**
- **위치**: 이메일 인증 완료 후 "회원가입" 버튼 클릭
- **원인**: `EmailVerificationRepository.findValidVerification`가 JPQL에 `LIMIT 1` 사용. JPQL 표준에 LIMIT 없어 일부 환경에서 쿼리 실패 또는 미동작.
- **조치**: derived query로 교체  
  `findFirstByEmailAndVerifiedTrueAndIsDeletedFalseOrderByCreatedAtDesc(String email)`  
  `UserService.join`에서 위 메서드 사용.

---

### 6. 공연 등록 버튼 안 보임
- **위치**: 공연 목록 페이지 "공연 등록" 버튼
- **원인**: `list.html`이 `hasRole('PERFORMER')`로 노출. DB 역할은 모두 `USER`라 버튼이 안 나옴.
- **조치**: `PerformancePageController.listPage`에서 `activeMode` 모델에 담고, 템플릿은 `th:if="${activeMode == 'ROLE_PERFORMER'}"` 로 표시.

---

## 🟠 없는 기능 (라우트/연동만 없음)

| 기능 | 링크/동작 | origin/main 상태 |
|------|-----------|------------------|
| 본 공연 목록 **페이지** | `/performances/watched` | ❌ 페이지 매핑 없음 → 404 |
| 찜한 공연 목록 **페이지** | `/performances/wished` | ❌ 페이지 매핑 없음 → 404 |
| 공연 수정 **페이지** | `/performances/{id}/edit` | ❌ 매핑 없음 → 404 |
| 프로필 선택 시 공연자/호스트 없을 때 | 카드 노출·전환 | ❌ 예외 발생 또는 전환 실패 |
| 마이페이지 직진입 시 profileId | 팔로잉/팔로워 수·링크 | ❌ null이면 500 또는 링크 오류 |
| 이메일 인증 후 가입 | join 시 verified 조회 | ❌ JPQL LIMIT 이슈 가능 |
| 공연 등록 버튼 노출 | list 페이지 | ❌ hasRole로는 안 나옴 |

---

## ✅ 적용 시 수정할 파일 요약

| 파일 | 내용 |
|------|------|
| `PerformancePageController` | `GET /watched`, `GET /wished`, `GET /{id}/edit` 추가. listPage에 `activeMode` 전달 |
| `performance/list.html` | `activeMode` 사용해 공연 등록 버튼, `data-list-mode`(listMode) 추가 |
| `static/js/performance/list.js` | 초기 `data-list-mode` 읽어 `state.filter`·탭 설정 |
| `MypageController` | `profileId` null 시 `ProfileService.findProfileIdByMode`로 조회·세션 반영, 예외 시 리다이렉트 |
| `PerformerProfileService` | `getPerformerProfileOrNull(User)` 추가 |
| `HostProfileService` | `getHostProfileOrNull(User)` 추가 |
| `ProfileController` | select에서 OrNull 사용, switchMode에서 ApiException catch |
| `profile/select.html` | 관람객/공연자/호스트 카드 `th:if` 조건부, `error=no_profile` 메시지 |
| `static/js/profile/select.js` | `data-has-profile` 있을 때만 클릭 전송 |
| `EmailVerificationRepository` | `findValidVerification` 제거, `findFirstByEmailAndVerifiedTrueAndIsDeletedFalseOrderByCreatedAtDesc` 사용 |
| `UserService` | join에서 위 메서드 호출로 변경 |

---

## 참고

- 현재 **test/from-main** 등 로컬 브랜치에는 위 항목이 이미 반영된 상태일 수 있음.
- origin/main에 머지/푸시 전 위 체크리스트 기준으로 한 번 더 확인 권장.
