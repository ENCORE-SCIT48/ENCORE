# DB에 실제 이미지 넣는 방법

이 프로젝트는 **이미지 파일을 DB에 바이너리로 저장하지 않습니다.**  
DB에는 **이미지 경로(문자열)**만 저장하고, 실제 파일은 서버의 **폴더** 또는 **static 리소스**에 둡니다.

---

## 방법 1: static 폴더에 이미지 넣기 (권장)

프로젝트 안에 테스트용 이미지를 두고, DB에는 **웹 경로**만 넣는 방식입니다.

### 1단계: 이미지 파일 위치

다음 폴더에 파일을 넣습니다.

```
src/main/resources/static/image/test/
```

### 2단계: 파일 이름 예시

| 용도 | 파일 이름 | 사용하는 테이블·컬럼 |
|------|-----------|----------------------|
| 관람객 프로필 | profile1.jpg, profile2.jpg, ... | user_profile.profile_image_url |
| 공연자 프로필 | performer1.jpg, ... | performer_profile.profile_image_url |
| 호스트 프로필 | host1.jpg, ... | host_profile.profile_image_url |
| 공연장 대표 이미지 | venue1.jpg, venue2.jpg | venue.venue_image |

이름은 자유롭게 지어도 됩니다. 아래 SQL에서 사용하는 경로와만 맞추면 됩니다.

### 3단계: DB에 경로 넣기

**옵션 A – INSERT 시점에 경로 지정**  
`data-test-insert.sql`에서 `profile_image_url`, `venue_image` 등에  
`'/image/test/profile1.jpg'` 처럼 **경로 문자열**을 넣습니다. (현재는 NULL로 두고 있을 수 있음.)

**옵션 B – INSERT 후 UPDATE**  
기본 INSERT는 그대로 두고, **테스트 이미지용 UPDATE**만 따로 실행합니다.  
`data-test-insert.sql` 맨 아래에 있는  
`-- [선택] 테스트 이미지 경로 적용` 섹션의 주석을 해제한 뒤 실행하면 됩니다.

### 웹에서 보이는 경로

- 저장한 경로: `'/image/test/profile1.jpg'`
- 브라우저 요청: `http://localhost:8080/image/test/profile1.jpg`
- Spring이 `static/image/test/` 아래 파일을 그대로 노출합니다.

---

## 방법 2: 업로드 폴더(uploads)에 이미지 넣기

실제 업로드와 같은 방식으로 쓰고 싶다면, **업로드 디렉터리**에 파일을 넣고 DB에는 `/uploads/파일명` 형태로 넣습니다.

### 1단계: 업로드 폴더 확인

`application.yaml` (또는 `application.properties`)에서 다음 설정을 봅니다.

```yaml
file:
  upload-dir: C:/encore/uploads/   # 예시 (OS마다 다를 수 있음)
```

이 경로(예: `C:/encore/uploads/`)에 폴더가 없으면 만들어 둡니다.

### 2단계: 이미지 파일 복사

테스트용 이미지(예: profile1.jpg)를 그 폴더에 복사합니다.  
파일명은 그대로 써도 되고, 나중에 DB에 넣을 경로와 맞추면 됩니다.

### 3단계: DB에 경로 넣기

- 웹 경로 형식: `'/uploads/파일명'` (예: `'/uploads/profile1.jpg'`)
- `user_profile`, `performer_profile`, `host_profile`의 `profile_image_url`,  
  `venue`의 `venue_image` 등에 위 문자열을 UPDATE 또는 INSERT 시 넣습니다.

이렇게 하면 앱이 제공하는 `/uploads/**` URL로 이미지가 노출됩니다.

---

## 요약

| 방법 | 이미지 위치 | DB에 넣는 값 예시 |
|------|-------------|-------------------|
| **1. static** | `src/main/resources/static/image/test/xxx.jpg` | `'/image/test/xxx.jpg'` |
| **2. uploads** | `file.upload-dir` 폴더(예: `C:/encore/uploads/xxx.jpg`) | `'/uploads/xxx.jpg'` |

둘 다 **DB에는 “실제 이미지”가 아니라 “경로 문자열”만** 넣으면 되고,  
실제 파일은 위 둘 중 한 곳에 두면 됩니다.
