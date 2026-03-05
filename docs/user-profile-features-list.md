# 유저(관람객) 프로필 관련 기능 정리

예영님이 개발하지 않은 것 중 **유저 프로필 관련** 기능만 정리한 목록입니다.  
(담당자 표기는 팀에서 구분해 두시면 됩니다.)

---

## 1. 관람객 프로필 (userprofile) — 보기/설정

| 기능 | URL | 컨트롤러 | 템플릿 | 비고 |
|------|-----|----------|--------|------|
| 관람객 프로필 보기 | GET `/userprofile` | UserProfileController | profile/user-view.html | 프로필 이미지, 전화번호, 활동지역, 생년월일, 선호 장르/공연 형태, 자기소개 |
| 관람객 프로필 설정 | GET `/userprofile/setup` | UserProfileController | profile/user-setup.html | 폼 저장 시 POST `/userprofile/setup` → redirect `/userprofile?success=true` |

**진입**: 마이페이지(관람객) → "관람객 프로필 보기" / "관람객 프로필 설정"  
**되돌아가기**: user-view에 "마이페이지로", "프로필 변경" 링크 있음. user-setup은 확인 필요.

---

## 2. 회원정보 수정 (계정 — 닉네임·비밀번호)

| 기능 | URL | 컨트롤러 | 템플릿 | 비고 |
|------|-----|----------|--------|------|
| 회원정보 수정 폼 | GET `/user/account` | UserPageController | user-settings/account.html | 이메일(표시만), 닉네임, 비밀번호 변경 |
| 회원정보 수정 제출 | POST `/user/account` | UserPageController | → redirect `/mypage` | UserService.updateAccount |

**진입**: 마이페이지 → "회원정보 수정"  
*(본인이 수정 중이라고 하신 부분)*

---

## 3. 알림 설정

| 기능 | URL | 컨트롤러 | 템플릿 | 비고 |
|------|-----|----------|--------|------|
| 알림 설정 페이지 | GET `user/notification` | UserPageController | user-settings/notificationSetting.html | **경로 주의**: `@GetMapping("user/notification")` → 슬래시 없음. 마이페이지 링크는 `/user/notification` |

**진입**: 마이페이지 → "알림 설정"

---

## 4. 차단 목록

| 기능 | URL | 컨트롤러 | 템플릿 | 비고 |
|------|-----|----------|--------|------|
| 차단 목록 관리 | GET `/user/block` | UserPageController | relation/blockList.html | API: RelationApiController (blocks, block/unblock) |

**진입**: 마이페이지 → "차단 목록 관리"

---

## 5. 팔로우·팔로워

| 기능 | URL | 컨트롤러 | 템플릿 | 비고 |
|------|-----|----------|--------|------|
| 팔로잉/팔로워 리스트 | GET `/user/follow?targetId=&profileMode=&tab=` | UserPageController | relation/following-follower.html | tab: following / follower |

**진입**: 마이페이지 상단 "팔로잉 N" / "팔로워 N" 클릭, 또는 타인 프로필에서 팔로우 수 클릭

---

## 6. 관람객 전용 활동 (마이페이지 메뉴)

| 메뉴 | URL | 비고 |
|------|-----|------|
| 내가 본 공연 | `/performances/watched` | Performance 쪽 |
| 내가 찜한 공연 | `/performances/wished` | Performance 쪽 |

---

## 7. 프로필 선택·전환 (공통)

| 기능 | URL | 비고 |
|------|-----|------|
| 프로필 선택 | GET `/profiles/select` | 관람객/공연자/호스트 선택 |
| 프로필 스위치 | (API) | ProfileController 등 |

---

## 8. 알림 설정 URL 확인

- 마이페이지 링크: `/user/notification`
- UserPageController: `@GetMapping("user/notification")` → Spring 기본 동작상 **앞에 `/` 없으면** 컨트롤러에 `@RequestMapping`이 없을 때 경로가 `/user/notification`이 아닐 수 있음.
- **권장**: `@GetMapping("/user/notification")` 처럼 앞에 `/` 붙이는 것이 안전합니다. 404 나오면 해당 컨트롤러 매핑 확인.

---

## 9. 정리 (예영님 미개발 분 중 유저 프로필 쪽)

- **관람객 프로필 보기/설정** (`/userprofile`, `/userprofile/setup`) — UserProfileController
- **회원정보 수정** (`/user/account`) — 본인 개발
- **알림 설정** (`/user/notification`)
- **차단 목록** (`/user/block`)
- **팔로우/팔로워** (`/user/follow`)
- **마이페이지에서 관람객 메뉴** (프로필 보기/설정, 본 공연, 찜한 공연 등)

위 목록 기준으로 담당/미개발 구분하시면 됩니다.
