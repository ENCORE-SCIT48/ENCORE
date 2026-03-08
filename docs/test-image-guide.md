# 테스트 이미지 가이드 (54장 / 33장)

SQL에서 참조하는 이미지 수:
- **2차(호스트 41~58)**: 공연장 18개 + 공연 15개 = **33개** (`/uploads/xxx.jpg`)
- 1차 테스트: 프로필·공연장·공연 등 약 12개 (`/image/test/xxx.jpg`)

---

## 방법 1. 사진 없이 쓰기 (추천)

이미지 경로를 **NULL**로 두면, 앱에서 기본 placeholder만 보여도 동작합니다.

- **옵션 A**: `data-test-insert.sql`에서 2차 venue·performance INSERT 시  
  `'/uploads/yes24_live_hall.jpg'` → `NULL` 로 일괄 변경  
  → 실제 파일 0개로 테스트 가능.
- **옵션 B**: 프론트에서 `img src`가 없거나 null일 때  
  `placeholder.png` 또는 CSS 배경 한 장만 표시하도록 처리.

---

## 방법 2. 플레이스홀더 1장만 쓰기

33개(또는 54개) 모두 **같은 placeholder 이미지**를 가리키게 하기.

1. placeholder 이미지 1개 준비 (예: `placeholder.jpg`, 800x600 정도).
2. 서버 업로드 경로에 `placeholder.jpg` 저장.
3. SQL에서 `/uploads/yes24_live_hall.jpg` 등 **모든 경로를**  
   `'/uploads/placeholder.jpg'` 로 변경  
   → 파일은 1개만 있으면 됨.

---

## 방법 3. 무료 이미지 사이트에서 구하기

실제와 비슷한 분위기로 쓰고 싶을 때.

| 사이트 | 검색어 예시 | 비고 |
|--------|-------------|------|
| [Unsplash](https://unsplash.com) | concert hall, theater, music stage, orchestra | 무료, 고해상도 |
| [Pexels](https://pexels.com) | concert, theater interior, stage | 무료 |
| [Pixabay](https://pixabay.com) | 공연장, 콘서트, 극장 | 무료 |

- **공연장**: "concert hall", "theater interior", "stage", "auditorium" 등.
- **공연 포스터/무대**: "concert", "live music", "orchestra", "musical" 등.
- 다운로드 후 **파일명만** SQL에 나온 대로 변경  
  (예: `yes24_live_hall.jpg`, `evnne_setngo.jpg`)  
  → `src/main/resources/static/uploads/` 또는 서버의 업로드 디렉터리에 넣기.

---

## 방법 4. 플레이스홀더 URL 쓰기 (외부 URL 저장 시)

DB 컬럼이 **전체 URL**을 허용하면, 파일 없이 외부 이미지로 채울 수 있음.

- 예: [Lorem Picsum](https://picsum.photos)  
  `https://picsum.photos/800/600` (또는 `/id/1` 등)
- SQL에서  
  `'/uploads/yes24_live_hall.jpg'`  
  → `'https://picsum.photos/800/600?random=1'`  
  처럼 **경로 대신 URL** 저장.  
  (각 row마다 `?random=2`, `?random=3` … 으로 바꾸면 서로 다른 이미지처럼 보임.)

※ 현재 앱이 `/uploads/` 같은 **상대 경로**만 처리한다면, 이미지 로딩 로직에서 `http(s):` 로 시작하면 그대로 `img src`에 넣도록 수정이 필요할 수 있음.

---

## 방법 5. 스크립트로 placeholder 이미지 33개 생성

같은 디자인에 **파일명만** 다르게 33개 만들고 싶다면:

- **ImageMagick** 등으로  
  `convert -size 800x600 xc:#eee -gravity center -pointsize 24 -fill '#333' -annotate 0 "yes24_live_hall" yes24_live_hall.jpg`  
  같은 식으로 파일명 리스트 돌리면 됨.
- 또는 **Python (PIL/Pillow)** 로  
  - 800x600 캔버스 + 텍스트(파일명) 그린 뒤  
  - `venue_list`, `performance_list` 순서대로 저장하는 스크립트 작성 가능.

원하면 파일명 목록(33개) 기준으로 스크립트 예시도 적어줄 수 있음.

---

## SQL에 나오는 파일명만 빠른 참고 (2차 33개)

**공연장 (18)**  
`yes24_live_hall.jpg`, `bluesquare_mastercard_hall.jpg`, `sac_concert_hall.jpg`, `sejong_grand_theater.png`, `lotte_concert_hall.jpg`, `rolling_hall.jpg`, `westbridge_livehall.jpg`, `sangsangmadang_livehall.jpg`, `nodeul_livehouse.jpg`, `aram_theater.jpg`, `seongnam_opera_house.jpg`, `suwon_sk_atrium.jpg`, `busan_culture_center.jpg`, `bexco_auditorium.jpg`, `daegu_opera_house.jpg`, `gwangju_art_center.jpg`, `incheon_art_center.jpg`, `chuncheon_sangsangmadang.jpg`

**공연 (15)**  
`evnne_setngo.jpg`, `solutions_emergence.jpg`, `concrete_spark.jpg`, `togenashi.jpg`, `yesung_itscomplicated.jpg`, `baekhyun_reverie.jpg`, `day6_thepresent.jpg`, `highlight_rideordie.jpg`, `leechanwon_changa.jpg`, `kpop_festival.jpg`, `sac_11am_nov.jpg`, `sac_11am_dec.jpg`, `daniel_harding_orchestra.jpg`, `richard_jangle_duo.jpg`, `kbs_orchestra.jpg`

---

**정리**:  
- **빨리 테스트만 하려면** → 방법 1 (NULL) 또는 방법 2 (placeholder 1장).  
- **데모/발표용으로 예쁘게** → 방법 3 (무료 사이트) 또는 방법 4 (URL).  
- **파일 개수만 맞추고 싶으면** → 방법 5 (스크립트로 33개 생성).
