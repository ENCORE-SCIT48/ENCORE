# CSS 테마(theme) 적용 현황 조사

전체 CSS 파일을 대상으로 **하드코딩 색상(#hex, rgb/rgba)** 대신 **var(--encore-*)** 사용 여부를 조사한 결과입니다.

---

## ✅ 이미 테마 변수 사용 중인 파일

| 파일 | 비고 |
|------|------|
| `theme.css` | 변수 정의·전역 스타일 (의도적 hex/rgba 유지) |
| `common/forms.css` | var(--encore-*) 전부 사용 |
| `auth/login.css` | var(--encore-*) 사용 |
| `profile/select.css` | var(--encore-*) 사용 |
| `relation/follow.css` | var(--encore-primary), var(--encore-text) 사용 |
| `performance/new.css` | var(--encore-text) 등 사용 |
| `community/app-encore.css` | 테마 변수만 사용 |
| `venue/detail.css` | 로컬 변수가 --encore-* 참조 |
| `fragments/topbar.css` | 비어 있음 / theme.css 의존 |
| `fragments/footer.css` | theme.css 의존 |

---

## ⚠️ 일부만 적용 / 미적용 (수정 대상)

### 1. **index.css**
- `rgba(0,0,0,0.06)` → box-shadow (theme에 shadow 변수 없으면 유지 가능)

### 2. **venue/performerVenueList.css**
- `--primary-red`는 var(--encore-primary)로 이미 변경됨
- **미적용**: `--primary-red-dark`, `--primary-red-light`, `--dark-grey`, `--border-color`, `#fff`, `#f4f5f7`, `#5a6478`, `#222`, `#aaa`, `#edf0f5`, `rgba(230,57,70,...)`

### 3. **performance/detail.css**
- **미적용 다수**: `#fff`, `#dcdcdc`, `#f3f4ff`, `#e0e0e0`, `#efefef`, `#b3261e`, `#999`, `#555`, `#6f6f6f`, `#777`, `#444`, `#111`, `#888`, `#ffb300`, 여러 `rgba`

### 4. **profile/user-view.css**
- `rgba(255,255,255,0.95)`, `#fff`, `box-shadow` rgba

### 5. **performance/list.css**
- `rgba(0,0,0,0.75)`, `#fff`, `rgba(255,255,255,0.95)`, `rgba(0,0,0,0.5)`

### 6. **fragments/profile.css**
- `#e5e5e5`, `#bdbdbd`, `#fff`, `#dee2e6`, `rgba(0,0,0,0.1)`

### 7. **relation/blockList.css**
- `#fff`, `#888`, `#f2f2f2`, `#444`, `#bbb` (일부만 이전에 var로 변경됨)

### 8. **venue/reservationForm.css**
- 로컬 변수 + `#fff`, `#f4f5f7`, `#5a6478`, `#ddd`, `#aaa`, `#edf0f5`, `rgba(230,57,70,0.3)` 등

### 9. **community/mypage.css**
- `#f8f8f8`, `rgba(230,57,70,0.1)` (일부만 var 적용됨)

### 10. **profile/host-setup.css**
- `#fff`, `#fdfdfd`, `#555`, `#fff1f1`, `#6c757d`, `#dee2e6`, `rgba(255,77,77,0.3)` 등

### 11. **chat/chat-post-form.css**
- `#ffffff`, `#e63946`, `#d62839`, `#f1a1a8`, `#e9ecef`, `#6c757d`, `#ced4da`, `#f1f3f5` 등 (일부만 var 적용됨)

### 12. **venue/myVenues.css**
- `#fff`, `#f4f5f7`, `#5a6478`, `#edf0f5`, `rgba(26,26,46,0.92)` 등

### 13. **profile/performer-setup.css**
- `#fff`, `#fdfdfd`, `#444`, `#fff1f1`, `#6c757d`, `#dee2e6`, `rgba(255,77,77,...)` 등

### 14. **auth/join.css**
- `rgba(0,0,0,0.06)`, `#fff`

### 15. **venue/venueReservations.css**
- 로컬 변수 + 다수 hex/rgba (`#fff`, `#f4f5f7`, `#5a6478`, 상태색 등)

### 16. **venue/list.css**
- `rgba(255,255,255,0.95)`, `rgba(255,255,255,0.6)`, `rgba(0,0,0,0.06)` (카드 스타일)

### 17. **profile/user-setup.css**
- `#fff`, `rgba` box-shadow, `#fff` border

### 18. **venue/venueForm.css**
- `#fff`, `#eef0f4`, `#f8fafc`, `#edf0f5`, `#dee2e6`, `--r-color: #E63946` 등 (좌석 등급 색은 의미상 유지 가능)

### 19. **venue/myReservations.css**
- `#fff`, `#f4f5f7`, `#5a6478`, `#fdecea`, `#b91c1c`, 상태 배지 색 등

### 20. **community/reportForm.css**
- `#fff` (background)

---

## 정리

- **완전 적용**: theme, common/forms, auth/login, profile/select, relation/follow, performance/new, app-encore, venue/detail, fragments(topbar, footer)
- **2차 적용 완료**: index, auth/join, community/reportForm, community/mypage, profile/user-view, user-setup, host-setup, performer-setup, fragments/profile, relation/blockList, chat/chat-post-form, venue/list, venue/detail, performance/detail, performance/list — 배경·텍스트·테두리·강조색을 `var(--encore-*)`로 교체함.
- **일부 유지**: `performance/detail.css` 내 `#fff`(버튼/오버레이 텍스트), venue 로컬 변수(`--primary-red` 등은 이미 `var(--encore-primary)` 참조), venueForm 좌석 등급(VIP/R/S/A) 색, 상태 배지(APPROVED/REJECTED) 등 의미 색은 필요 시 별도 변수로만 통일.
- **추가 권장**: venue/performerVenueList, reservationForm, myVenues, venueReservations, venueForm, myReservations — 로컬 변수 정의부를 `var(--encore-*)` 참조로 바꾸면 전역 테마와 추가 통일 가능.

*마지막 조사·적용: 프로젝트 전체 CSS 테마 2차 적용*
