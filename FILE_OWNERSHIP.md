# 파일 소유권 및 수정 금지 목록

## 원본 파일 (프론트 팀 소유 - 수정 금지)

```
app/src/main/java/com/example/mobile_survey_application/
  ├─ MainActivity.java
  └─ HomeActivity.java

app/src/main/java/viewmodel/
  └─ survey_viewmodel.java

app/src/main/res/layout/
  ├─ activity_main.xml
  └─ activity_home.xml

app/src/main/res/drawable/
app/src/main/res/mipmap-anydpi-v26/
app/src/main/res/values/
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
```

---

## 백엔드 개발자 생성/수정 파일

### 신규 생성

```
app/src/main/java/data/network/
  ├─ AuthApiService.java
  ├─ CategoryApiService.java
  └─ RetrofitClient.java

app/src/main/java/data/repository/
  ├─ AuthRepository.java
  └─ CategoryRepository.java

app/src/main/java/data/local/
  └─ TokenManager.java

app/src/main/java/model/
  ├─ ApiResponse.java
  ├─ GoogleLoginRequest.java
  ├─ GoogleLoginResponse.java
  ├─ RegisterRequest.java
  ├─ TokenResponse.java
  └─ CategoryResponse.java

app/src/main/java/viewmodel/
  └─ AuthViewModel.java

app/src/main/java/com/example/mobile_survey_application/
  ├─ LoginTestActivity.java          ← 테스트용 (배포 전 제거)
  └─ RegisterActivity.java           ← 테스트용 (배포 전 제거)

app/src/main/res/layout/
  ├─ activity_login.xml              ← 빈 파일이었으나 테스트용 내용 추가
  └─ activity_register.xml           ← 신규 생성 (테스트용)

app/src/main/res/xml/
  └─ network_security_config.xml
```

### 수정

```
app/AndroidManifest.xml              ← INTERNET 권한, LoginTestActivity 추가, networkSecurityConfig 추가
app/build.gradle.kts                 ← 의존성 추가 (Retrofit, Google Sign-In, ViewModel, LiveData), BuildConfig 설정
gradle/libs.versions.toml            ← 라이브러리 버전 추가
```

---

## 배포 전 정리 필요 항목

- [ ] `LoginTestActivity.java` 제거
- [ ] `activity_login.xml` 프론트 팀 디자인으로 교체
- [ ] `AndroidManifest.xml`에서 LoginTestActivity launcher → MainActivity launcher로 원복
