## ENCORE – 무대를 잇다

SCIT 48기 3조 팀 프로젝트, **공연·공연장·공연자를 잇는 매칭 & 피드 플랫폼**입니다.

관람객(유저), 공연자, 공연장 호스트가 한 곳에서:
- 공연을 **발견·찜·리뷰**하고
- 공연자와 호스트가 **공연/대관을 매칭**하고
- 채팅·DM·피드를 통해 **소통**할 수 있도록 하는 서비스입니다.

---

## 핵심 기능 한눈에 보기

- **역할 기반 프로필**
  - 한 계정으로 **관람객 / 공연자 / 호스트** 프로필을 모두 보유
  - `/profiles/select` 에서 활성 프로필 전환

- **공연 & 공연장**
  - 공연 목록 / 상세 / 찜(WISH) / 본공연(WATCHED)
  - 공연·좌석 리뷰 분리 (관람객 전용)
  - 공연장 목록 / 상세 / 좌석 구조 / 대관 신청·승인·거절

- **피드**
  - 내가 찜한 공연 **시작 임박 알림**
  - 팔로우한 사람이 찜한 공연 추천
  - 공연 포스터를 크게 보여주는 카드형 피드

- **소셜**
  - 유저/프로필 팔로우·언팔로우
  - 차단 목록 관리
  - 신고(Report) 기능

- **소통**
  - **공연 채팅** – 공연 본 뒤 이어지는 공간: 후기·감상(REVIEW), 택시 동승(TAXI_SHARE), 뒤풀이(AFTER_PARTY), 일반(GENERAL) 유형으로 모집글 생성·목록/상세에 표시
  - 공연별 채팅방 목록·참여·핫 채팅, 1:1 DM

- **마이페이지 & 계정**
  - 관람객/공연자/호스트별 활동 모아보기
  - **회원정보 수정** (닉네임·비밀번호) – `/user/account`
  - 알림 설정, 차단 목록, 팔로우/팔로워 리스트

---

## 기술 스택

- **Back-end**
  - Java 21, Spring Boot
  - Spring MVC, Spring Data JPA
  - Spring Security
  - HikariCP, MySQL (또는 호환 RDB)

- **Front-end**
  - Thymeleaf 템플릿
  - Vanilla JS, jQuery 일부
  - Bootstrap 5, Bootswatch Litera

- **기타**
  - Gradle 빌드
  - WebSocket (채팅)

---

## 로컬 실행 방법 (개요)

1. **환경 준비**
   - JDK 21
   - MySQL (또는 호환 DB) – 스키마는 JPA `ddl-auto` 또는 별도 `schema.sql` 기준

2. **DB 생성 & 테스트 데이터 삽입**
   - DB 생성 후 `src/main/resources/sql/data-test-insert.sql` 실행
   - 선택 사항: 테스트 이미지 경로 적용
     - `src/main/resources/static/image/test/` 에 샘플 이미지 파일 배치
     - 같은 SQL 내 `performance_image_url` UPDATE 구문을 함께 실행하면 피드·상세에서 포스터 확인 가능

3. **빌드 & 실행**
   ```bash
   # 윈도우 PowerShell 예시
   .\gradlew clean build
   .\gradlew bootRun
   ```

4. **접속**
   - 기본: `http://localhost:8080`
   - 주요 진입점:
     - `/auth/login` – 로그인
     - `/feed` – 피드
     - `/performances` – 공연 리스트
     - `/venues` – 공연장 리스트
     - `/mypage` – 마이페이지 (로그인 필요)

---

## 테스트용 계정 (data-test-insert.sql 기준)

> 비밀번호는 공통으로 `password123` 입니다.  
> 비밀번호가 맞지 않을 경우 `data-test-insert.sql` 하단의 안내에 따라 해시를 재적용할 수 있습니다.

- 관람객 유저: `user1@test.com`, `user2@test.com`
- 공연자 유저: `performer1@test.com` (+ 관람객/호스트 프로필도 보유)
- 호스트 유저: `host1@test.com`

로그인 후 `/profiles/select` 에서 **관람객 / 공연자 / 호스트 모드**를 전환할 수 있습니다.

---

## 디렉터리 개요

- `src/main/java/com/encore/encore/domain`
  - `performance` – 공연, 공연 스케줄, 좌석 리뷰
  - `venue` – 공연장, 좌석, 대관
  - `community` – 게시글, 추천, 신고
  - `chat` – 채팅, DM
  - `user` – 회원, 팔로우/차단, 설정
  - `feed` – 피드 API

- `src/main/resources/templates`
  - `auth` – 로그인/회원가입
  - `feed` – 피드 화면
  - `performance` – 공연 리스트/상세/리뷰 작성
  - `venue` – 공연장 리스트/상세/예약
  - `community/mypage` – 마이페이지
  - `fragments` – 헤더, 푸터, 프로필, 탑바 등 공통 UI

- `src/main/resources/static`
  - `css` – 화면별·프래그먼트별 스타일
  - `js` – 화면별 스크립트 (`feed/list.js`, `performance/detail.js` 등)
  - `image/test` – 샘플 이미지용 폴더

---

## 개발 메모

- **문서 유지보수**: 기능 추가·변경 시 필요하면 README와 `docs/*.md`(명세·체크리스트·플로우 등)를 함께 수정해 두면 협업·추적에 도움이 됩니다.

자세한 머지/테스트 플로우, 역할별 플로우, 명세 대비 구현 현황은 다음 문서를 참고하세요.

- `docs/pre-merge-checklist.md` – 머지/배포 전 체크리스트
- `docs/role-entry-and-user-flow.md` – 역할별 진입점·유저 플로우 정리
- `docs/spec-compliance.md` – 요구사항 명세 대비 구현 현황
- `docs/test-images-guide.md` – 테스트 이미지 적용 가이드

팀·발표 상황에 맞게 추가 설명이나 스크린샷 섹션을 README 하단에 자유롭게 덧붙여 사용하시면 됩니다.
