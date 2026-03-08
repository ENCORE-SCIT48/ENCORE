# ENCORE 스타일 가이드 — 공연·무대에 맞는 "섹시한" UI

프로젝트 주제(**무대를 잇다**, 공연·공연장·공연자)에 맞춘 CSS/디자인 조언을 정리했습니다.  
현재 **딥 레드 + 웜 배경** 기반을 유지하면서, 더 드라마틱하고 프리미엄하게 가져갈 수 있는 방향입니다.

---

## 1. 컨셉 정리

| 키워드 | 설명 |
|--------|------|
| **무대·공연** | 레드 커튼, 스포트라이트, 골드 포인트 — 클래식한 극장 감성 |
| **프리미엄** | 글래스모피즘, 은은한 그라데이션, 여백·타이포에 신경 |
| **에너지** | 라이브 공연 느낌 — 다크 테마 옵션, 강한 포인트 컬러 |
| **현대적** | 플랫만 쓰지 말고, blur·shadow·호버로 깊이감 |

---

## 2. 색상 팔레트 제안

### 2.1 현재 (theme.css) + 보강

지금 **딥 레드(#B91C2E)** + **골드(#B8860B)** 조합은 무대 느낌에 잘 맞습니다. 아래 변수들을 추가해 단계를 나누면 더 “섹시”해집니다.

```css
:root {
  /* 무대 레드 — 메인 포인트 */
  --encore-primary: #B91C2E;
  --encore-primary-hover: #9B2335;
  --encore-primary-light: rgba(185, 28, 46, 0.12);
  --encore-primary-glow: rgba(185, 28, 46, 0.4);

  /* 극장 골드 — 강조(배지, 아이콘, 테두리) */
  --encore-gold: #B8860B;
  --encore-gold-light: #D4A84B;
  --encore-gold-muted: rgba(184, 134, 11, 0.25);

  /* 배경·텍스트 */
  --encore-bg: #FAF9F7;
  --encore-text: #1a1a1a;
  --encore-text-muted: #5c5c5c;
  --encore-footer-bg: #1a1a1a;   /* 다크 푸터로 무게감 */

  /* 다크 모드용 (선택) */
  --encore-dark-bg: #0d0d0d;
  --encore-dark-surface: #1a1a1a;
  --encore-dark-border: rgba(255,255,255,0.08);
}
```

- **레드**: 버튼, 링크, 중요한 뱃지  
- **골드**: “ENCORE” 로고, VIP/프리미엄 요소, 구분선  
- **다크 푸터**: `#333` 대신 `#1a1a1a` 정도로 더 묵직하게 가져가기 좋습니다.

### 2.2 다크 테마 옵션 (공연 앱 트렌드)

공연/콘서트 앱은 **다크 배경 + 포인트 컬러**가 많이 쓰입니다.  
선택적으로 “다크 모드” 또는 “공연장 모드”로 적용할 수 있습니다.

```css
[data-theme="dark"],
.theme-stage {
  --encore-bg: #0d0d0d;
  --encore-text: #e5e5e5;
  --encore-primary-glow: rgba(185, 28, 46, 0.35);
  /* 카드/서페이스는 반투명 + blur → 아래 글래스 참고 */
}
```

---

## 3. 타이포그래피

공연/예술 사이트는 **세리프(제목) + 산세리프(본문)** 조합이 잘 어울립니다.

### 추천 조합 (Google Fonts)

| 용도 | 폰트 | 비고 |
|------|------|------|
| **메인 로고·큰 제목** | **Playfair Display** 또는 **Cormorant Garamond** | 클래식, 무대 포스터 느낌 |
| **헤딩** | **Montserrat** (세미볼드/볼드) | 깔끔하고 강한 인상 |
| **본문** | **Lato**, **Noto Sans KR** | 가독성 좋은 산세리프 |

### 적용 예시

```html
<link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Montserrat:wght@500;600;700&family=Noto+Sans+KR:wght@400;500;700&display=swap" rel="stylesheet">
```

```css
:root {
  --font-display: 'Cormorant Garamond', serif;   /* 로고, 히어로 제목 */
  --font-heading: 'Montserrat', sans-serif;
  --font-body: 'Noto Sans KR', 'Lato', sans-serif;
}

.encore-topbar-brand,
.hero-title {
  font-family: var(--font-display);
  letter-spacing: 0.02em;
}

h1, h2, .card-title {
  font-family: var(--font-heading);
}

body {
  font-family: var(--font-body);
}
```

---

## 4. 글래스모피즘 (프리미엄 카드·상단바)

다크/라이트 모두 **반투명 + blur**로 깊이감을 줄 수 있습니다.

### 카드·모달

```css
.card-glass,
.modal-content {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

/* 다크 배경 위에서는 */
.theme-stage .card-glass {
  background: rgba(26, 26, 26, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}
```

### 상단바

```css
.encore-topbar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
```

- `backdrop-filter` 미지원 브라우저는 `@supports (backdrop-filter: blur(10px))` 로 분기하고, fallback으로 단색 배경을 주면 됩니다.

---

## 5. 스포트라이트·호버 (드라마틱 카드)

피드/공연 카드에 **마우스 따라가는 빛**을 주면 “무대 스포트라이트” 느낌이 납니다.

### 단순 버전 (호버 시 빛만)

```css
.feed-card,
.performance-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  transition: box-shadow 0.3s ease, transform 0.25s ease;
}

.feed-card::before {
  content: '';
  position: absolute;
  top: var(--mouse-y, 50%);
  left: var(--mouse-x, 50%);
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, var(--encore-primary-glow) 0%, transparent 70%);
  transform: translate(-50%, -50%);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.25s ease;
}

.feed-card:hover {
  box-shadow: 0 12px 40px rgba(185, 28, 46, 0.15);
  transform: translateY(-2px);
}

.feed-card:hover::before {
  opacity: 1;
}
```

- JS로 카드 내부 `mousemove` 시 `--mouse-x`, `--mouse-y` 설정하면 커서 위치에 빛이 따라옵니다 (선택).

### 더 단순한 대안 (그라데이션 글로우만)

```css
.feed-card:hover {
  box-shadow: 0 12px 32px rgba(185, 28, 46, 0.2),
              0 0 0 1px rgba(185, 28, 46, 0.1);
  transform: translateY(-3px);
}
```

---

## 6. 그라데이션 포인트

배경을 평면으로 두지 말고, **은은한 그라데이션**을 넣으면 고급스러워집니다.

```css
body {
  background: linear-gradient(180deg, #FAF9F7 0%, #f0ede8 50%, #FAF9F7 100%);
}

/* 푸터·다크 영역 */
.footer-fixed,
.footer-wrapper {
  background: linear-gradient(180deg, #1a1a1a 0%, #0d0d0d 100%);
  border-top: 2px solid var(--encore-gold);
}
```

- 상단바 하단에 **레드→골드** 아주 얇은 라인을 넣는 것도 한 방법입니다.  
  `border-bottom: 2px solid transparent; border-image: linear-gradient(90deg, var(--encore-primary), var(--encore-gold)) 1;`

---

## 7. 적용 우선순위 제안

| 순서 | 항목 | 효과 |
|------|------|------|
| 1 | **색상 변수 보강** (골드, primary-light, primary-glow, 다크 푸터) | 전체 톤이 한 번에 정리됨 |
| 2 | **카드 호버** (shadow + translateY + 선택적으로 스포트라이트) | 피드·공연 목록이 살아남 |
| 3 | **상단바/카드 글래스** (blur + 반투명) | 프리미엄 느낌 |
| 4 | **타이포** (로고·제목에 Cormorant Garamond / Montserrat) | 브랜드 인상 강화 |
| 5 | **다크 테마** (토글이나 “공연장 모드”) | 차별화 + 트렌드 반영 |

---

## 8. 참고 출처 요약

- 공연/콘서트 앱: **다크 UI + 강한 포인트 컬러**, 감성·이미지 강조.
- 극장 브랜딩: **레드 + 골드** 팔레트, 벨벳·커튼 연상.
- 글래스모피즘: **backdrop-filter + 반투명 배경 + 은은한 테두리/그림자**.
- 타이포: **세리프(제목) + 산세리프(본문)** 조합으로 품격 유지.
- 스포트라이트: **radial-gradient + 마우스 좌표** 또는 **호버 시 shadow/glow**로 구현.

원하면 `theme.css`와 `feed/list.css`, 상단바/푸터 CSS에 위 변수·클래스만 골라서 단계별 패치 예시도 만들어 줄 수 있습니다.
