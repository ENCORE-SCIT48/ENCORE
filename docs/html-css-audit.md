# ENCORE HTML·CSS 전수 점검 보고서

전체 페이지의 HTML 구조·요소, 시맨틱, 접근성, CSS 일관성 및 개선사항을 정리했습니다.

---

## 적용 완료 (최근 반영)

- **xmlns:th** — `relation/blockList.html`, `user-settings/notificationSetting.html` 수정
- **lang="ko" / viewport** — index, login, join, user-view, performer-view, venueForm, account, chatJoinListFull 반영
- **Bootstrap/Bootswatch/Icons** — Bootstrap 5.3.3, Bootswatch 5.3.2, Icons 1.11.3로 통일
- **공통 폼 CSS** — `css/common/forms.css` 추가, performancePost(Write/Update)·performerPost(Write) 등 인라인 스타일 클래스로 이전
- **인라인 스타일 제거** — 로그아웃 폼 `form-inline-reset`, 로그인 링크 `link-encore-primary`, host-view `text-primary-red`, modals `modal-body-scroll`
- **display:none → d-none** — reservationForm, join authCodeSection, performer-setup, host-setup file input 등
- **푸터 CSS** — performer 푸터 클래스 `footer-fixed`로 분리 (footer.css와 충돌 방지)
- **theme 변수** — theme.css에 `--encore-footer-bg` 추가, topbar/footer/performer-footer에서 `var(--encore-primary)` 등 사용
- **시맨틱** — 로그인 페이지 제목 h3 → h1 (클래스 `.login-title`으로 시각 유지)

---

## 1. HTML 공통 이슈

### 1.1 `lang` 속성 누락
- **권장**: 모든 페이지 `<html>` 에 `lang="ko"` 지정 (한국어 서비스).
- **누락 페이지**: `index.html`, `auth/login.html`, `auth/join.html`, `profile/user-view.html`, `profile/performer-view.html`, `venue/venueForm.html`, `user-settings/account.html`, `chat/chatJoinListFull.html` 등.
- **조치**: 위 템플릿의 `<html>` 에 `lang="ko"` 추가.

### 1.2 Thymeleaf 네임스페이스 오기
- **문제**: `xmlns:th="thymeleaf.org"` (프로토콜 누락).
- **영향 파일**:
  - `relation/blockList.html`
  - `user-settings/notificationSetting.html`
- **조치**: `xmlns:th="http://www.thymeleaf.org"` 로 수정.

### 1.3 viewport 메타 누락
- **문제**: `index.html` 에 `<meta name="viewport" content="width=device-width, initial-scale=1">` 없음.
- **조치**: `index.html` 의 `<head>` 에 viewport 메타 추가.

### 1.4 프래그먼트 파일 구조
- **topbar.html**: `<html><body>` 로 감싼 뒤 그 안에 `<header>` 프래그먼트만 있음. `th:replace` 시에는 `<header>` 만 삽입되므로 동작에는 문제 없으나, 프래그먼트 전용 파일은 `<html>/<body>` 없이 fragment만 두는 편이 구조상 명확함.
- **footer.html (공통)**: `<link>` 가 `<footer>` 안에 있음. HTML5에서는 허용되나, 스타일은 보통 `<head>` 에 두는 것이 일반적. 가능하면 레이아웃 프래그먼트를 포함하는 페이지의 `<head>` 에서 footer CSS 로드하거나, 한 번만 로드되도록 정리 권장.

---

## 2. 시맨틱·랜드마크

### 2.1 잘 되어 있는 부분
- **feed/list, venue/list, performance/list, reservationForm, chat, mypage 등**: `<main>`, `<header>`, `<section>`, `<nav>`, `<footer>` 사용.
- **blockList.html**: `<main>`, `<nav aria-label="차단 목록 필터">`, `<section id="blockList" aria-live="polite">` 로 구조·접근성 고려.
- **reviewWrite.html**: `aria-label="별점 선택"` 등 사용.

### 2.2 개선 권장
- **페이지당 하나의 `<h1>`**: 로그인·회원가입·설정 등은 현재 `<h3>` 또는 `<h2>` 로 제목만 있는 경우가 있음. 페이지 주제를 나타내는 **한 개의 `<h1>`** 을 두고, 나머지는 `<h2>` 이하로 계층 유지 권장.
  - 예: `auth/login.html` — “로그인”을 `<h1>` 로, 시각 크기는 class로 조절.
- **본문 영역 랜드마크**: topbar + footer 구조인데, 일부 페이지는 `<main>` 없이 `.app-container` 만 있는 경우 있음. 가능한 페이지는 모두 `<main class="...">` 로 감싸서 랜드마크 일관화 권장.
- **venueForm.html**: `<h1 class="h6">` — 시맨틱은 1단계, 시각은 6단계. 의도된 것이라면 유지해도 되나, “공연장 등록”이 페이지 제목이면 `<h1>` 유지하고 스타일만 `.h6` 로 두는 현재 방식이 적절함.

---

## 3. 인라인 스타일 과다 사용

많은 페이지에서 `style=""` 로 레이아웃·색상·폰트를 지정하고 있음. 유지보수와 테마 일관성을 위해 **CSS 클래스로 이전**하는 것을 권장합니다.

### 3.1 폼·라벨 반복 스타일 (공통 클래스로 묶기)
- **performancePostWrite.html, performancePostUpdate.html, performerPostWrite.html, performerPostUpdate.html**  
  - `style="margin-bottom:15px;"`, `style="font-size:13px; color:#666;"` 등 반복.
- **권장**: `.form-group`, `.form-label-secondary` 같은 클래스를 도입해 `community/form-common.css` 등에서 정의 후 적용.

### 3.2 상태·정원 표시 색상
- **performancePost.html, performerPost.html, performancePostDetail.html, mypage/performer/posts.html, performances.html**  
  - `style="color:#E63946;"`, `style="color:#666;"` 등.
- **권장**: `.text-capacity-full`, `.text-capacity-available`, `.text-muted-secondary` 등으로 빼고, `theme.css` 의 `--encore-primary` 등 변수 사용.

### 3.3 레이아웃·숨김
- **reservationForm.html**: `style="display:none;"` 로 섹션/바 숨김.
- **venueForm.html**: `style="width:36px;"`, `style="height:400px;"`, `style="grid-column: span 4;"` 등.
- **권장**:  
  - 숨김: `.d-none` (Bootstrap) 또는 `.hidden` 사용.  
  - 크기/그리드: `.spacer-36`, `.canvas-height`, `.stat-full-width` 등 클래스로 정의.

### 3.4 기타
- **login.html**: 회원가입 링크 `style="color: #e63946"` → `.link-primary` 또는 theme 변수 사용.
- **profile (fragments)**: 로그아웃 폼 `style="margin:0;"` → `.form-inline-reset` 등으로 분리.
- **modals.html**: `style="max-height: 400px; overflow-y: auto;"` → `.modal-body-scroll` 등.

---

## 4. CSS·에셋 일관성

### 4.1 Bootstrap / Bootswatch 버전 혼재
- **Bootstrap**: 5.3.0 (index), 5.3.2 (login), 5.3.3 (feed, venue/list, performance/list, detail, new 등).
- **Bootswatch Litera**: 5.3.0 (reportForm, notificationSetting), 5.3.2 (대다수), 5.3.3 (account).
- **Bootstrap Icons**: 1.11.0, 1.11.1, 1.11.3 혼용.
- **권장**:  
  - 한 버전으로 통일 (예: Bootstrap 5.3.3 + Bootswatch 5.3.2 또는 5.3.3 + Icons 1.11.3).  
  - 가능하면 **공통 레이아웃 프래그먼트** 또는 **한 개의 base 템플릿**에서만 CDN 링크를 두고, 나머지 페이지는 그걸 상속하도록 하면 버전 관리가 쉬움.

### 4.2 theme.css 사용 여부
- **theme.css**: `:root` 에 `--encore-primary`, `--encore-bg`, `--encore-max-width` 등 정의.
- **적용 페이지**: index, feed/list 등 일부만 사용. login은 theme 없이 login.css에서 직접 변수명만 동일하게 사용.
- **권장**:  
  - 공통 레이아웃 또는 base에서 **theme.css를 공통으로 로드**.  
  - 페이지별 CSS는 theme 변수(`var(--encore-primary)` 등)를 사용해 색·간격을 맞추면, 테마 변경 시 한 곳만 수정하면 됨.

### 4.3 푸터 CSS 중복
- **fragments/footer.css**: `.footer-wrapper` (상단 border, flex center), `.footer-link`.
- **fragments/performer-footer.css**: `.footer-wrapper` (fixed bottom, max-width 420px), `.footer-link`.
- 두 파일이 같은 클래스명을 다르게 정의하므로, **같은 페이지에 둘 다 포함되면 충돌 가능성** 있음.  
- **권장**:  
  - 클래스명 분리 (예: `.footer-common`, `.footer-fixed` 또는 BEM 식 `.footer--common`, `.footer--fixed`).  
  - 또는 푸터용 스타일을 하나로 합치고, 레이아웃만 modifier 클래스로 구분.

### 4.4 색상 하드코딩
- **topbar.css, footer, performer-footer, feed/list.css 등**: `#E63946`, `#333`, `#eee`, `#666` 등 직접 사용.
- **권장**: theme.css 의 `--encore-primary`, `--encore-text`, `--encore-bg` 등으로 교체하면 다크 모드나 리브랜딩 시 유리.

---

## 5. 페이지별 요약

| 구분 | 내용 |
|------|------|
| **index** | lang, viewport 없음. 인라인 스타일 있음. |
| **auth/login, join** | login은 lang 없음. join은 viewport 있음. 인라인 색상 1곳. |
| **feed/list** | 구조·시맨틱 양호. theme + list.css 사용. |
| **performance (list, detail, new 등)** | list/detail/new는 main·section 사용. detail은 JS 토글용 display:none 다수 → 클래스로 정리 권장. |
| **venue (list, detail, form, reservations, myVenues 등)** | venueForm 인라인 스타일 많음. reservationForm display:none 다수. |
| **community (mypage, performer, performance post 등)** | mypage는 Bootswatch+topbar+footer 구조 양호. performer/performance 쪽은 인라인 스타일·반복 라벨 스타일 많음. |
| **chat (chatRoom, chatPost*, dm/*)** | main·header·footer 사용. chatPostUpdateForm 등 일부 인라인 spacer. |
| **profile (user/performer/host view, setup)** | user-view, performer-view에 lang 없음. host-setup 등은 전반적으로 양호. |
| **relation (blockList, following-follower)** | blockList는 xmlns:th 오기 + 시맨틱·aria 좋음. |
| **user-settings (account, notificationSetting)** | notificationSetting은 Bootswatch 5.3.0 + Icons 1.11.0, xmlns 오기. |
| **member/memberProfile** | Bootswatch 사용, 구조 있음. |
| **fragments (topbar, profile, footer, performer/footer)** | topbar는 link/script가 header 안에 있음. profile 로그아웃 폼 인라인 margin. footer CSS 이중 정의 정리 권장. |

---

## 6. 권장 조치 우선순위

### 높음
1. **relation/blockList.html, user-settings/notificationSetting.html** — `xmlns:th` 를 `http://www.thymeleaf.org` 로 수정.
2. **모든 페이지** — `<html lang="ko">` 및 viewport 메타 보강 (누락 페이지만).
3. **Bootstrap/Bootswatch/Icons** — 버전 하나로 통일하고, 가능하면 공통 head 프래그먼트에서만 로드.

### 중간
4. **인라인 스타일** — 폼 라벨·마진·색상(정원 등)을 공통 클래스와 theme 변수로 이전 (community, venue, performer 쪽부터).
5. **display:none** — `.d-none` / `.hidden` + JS로 토글하도록 정리.
6. **푸터 CSS** — 클래스명 분리 또는 단일 파일로 통합해 충돌 제거.

### 낮음
7. **시맨틱** — 페이지당 `<h1>` 1개, 나머지 제목은 h2 이하로 통일.
8. **theme.css** — 공통 로드 후, topbar/footer/feed 등에서 색상·간격을 변수로 사용.
9. **프래그먼트** — topbar/footer 의 link 위치·구조 정리 (선택).

---

## 7. 공통 스타일 가이드 제안

- **색상**: `theme.css` 의 `--encore-*` 만 사용. 필요 시 `--encore-danger`, `--encore-muted` 등 추가.
- **간격**: `margin-bottom:15px` 등 반복 값은 `.mb-form-group` 같은 유틸 클래스로.
- **폼 라벨**: `.form-label-secondary` (font-size 13px, color #666) 한 번 정의 후 재사용.
- **숨김/표시**: Bootstrap `.d-none` / `.d-block` 또는 `.hidden` / `.visible` + JS.
- **버튼/링크 강조색**: `var(--encore-primary)` 또는 `.text-primary` (theme 적용 후).

이 순서대로 적용하면 HTML 구조·접근성·CSS 일관성이 개선되고, 이후 테마 변경과 반응형 조정이 수월해집니다.
