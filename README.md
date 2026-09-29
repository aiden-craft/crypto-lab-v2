# Crypto Lab V2 · 암호화 교육 실습

금융 ITO 공통 Skill 교육용 로컬 실습 서비스입니다. Spring Boot와 Thymeleaf로 화면을 제공하며, 브라우저에서 입력을 바꾸고 결과를 비교합니다.

## 실행

- Java 17
- Maven 3.9 이상

```bash
cd crypto-lab-java
mvn spring-boot:run
```

브라우저에서 **http://localhost:8080**으로 접속하세요. 이미 8080 포트를 사용 중이면 `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`로 실행할 수 있습니다.

## 학습 구성

| 주제 | 실습 | 화면에서 관찰하는 내용 |
| --- | --- | --- |
| 해시 이해 | Hash와 Salt | 같은 평문에 Salt를 추가했을 때의 결과 |
| 해시 이해 | Avalanche Effect | 다른 입력의 해시 비트 변화율 |
| 해시 이해 | Bulk Hash | 고유 결과 수와 처리 시간, 중복 해석의 한계 |
| 해시 이해 | 사전 공격 실험 | 미리 계산한 SHA-256 해시와 대조 |
| 암호화 방식 | AES | 임의 IV 재암호화, 복호화, 잘못된 키 |
| 암호화 방식 | RSA | 공개키/개인키, OAEP, 입력 크기 제한 |
| 검증과 비밀번호 | HMAC | 원본과 변조된 메시지의 검증 결과 |
| 검증과 비밀번호 | PBKDF2 | Salt가 다른 두 저장 값과 비밀번호 검증 |
| 종합 비교 | Benchmark | 알고리즘별 교육용 처리 시간 비교 |

실습은 원하는 순서로 열 수 있습니다. 결과 카드의 **실제 API 응답 보기**를 펼치면 서버 응답을 확인할 수 있습니다. 완료 표시와 마지막 위치, 테마만 브라우저에 저장하며 실습 입력값과 키는 저장하지 않습니다. 진행 기록은 사이드바에서 초기화할 수 있습니다.

각 실습 우측 상단의 **Java 예시 코드**를 누르면 해당 개념을 구현한 별도 Java 17 예제를 볼 수 있습니다. 창은 화면을 넓게 사용하며 코드 부분만 스크롤됩니다. 코드를 복사하거나 `Esc`로 창을 닫을 수 있습니다. 예제는 `src/main/resources/static/examples/`의 독립 파일이며, 서버의 Controller·Service 원본을 그대로 보여주지 않습니다. 이 파일들은 Java 소스 학습 자료로 배포되고 애플리케이션 클래스에는 컴파일되지 않습니다.

AES 예제는 인증 기능이 있는 **AES-GCM** 사용법을 보여줍니다. 실습 화면의 AES-CBC 방식과 모드·출력 형식이 다르므로 화면 안내를 확인하세요. 각 예제는 학습용 값만 사용하며 입력한 실습 값이나 키를 코드 창으로 전달하지 않습니다.

사전 공격 실험에서는 평문과 선택적 Salt를 직접 입력해 SHA-256 해시 생성부터 사전 조회까지 실행합니다. AES·RSA·HMAC·PBKDF2의 복호화 또는 검증 영역에는 이전 결과가 자동 입력되며, 사용자가 키·메시지·암호문·저장 값을 수정할 수 있습니다. 예제 변조 버튼은 입력값만 바꾸므로 내용을 확인한 뒤 실행할 수 있습니다. PBKDF2는 서로 다른 Salt로 두 저장 값을 만들면 완료로 표시되며 검증 실험은 계속할 수 있습니다.

Benchmark는 초(s) 단위의 평균 시간과 배수 비교를 함께 보여줍니다. AES 키는 두 비교에서 모두 편집할 수 있고 HMAC 키와 PBKDF2 반복 횟수는 전체 비교에 사용합니다. 요청 값은 POST 본문으로 전달합니다.

## 교육용 구현에서 알아둘 점

- AES는 AES-256-CBC를 사용하며 문자열 비밀키를 SHA-256으로 키 길이에 맞춥니다. 고정 파생 IV는 비교 학습용입니다. 이 예제로 데이터 무결성이나 운영 환경의 키 관리를 구현하지 마세요.
- 사전 공격 실험은 자주 쓰는 비밀번호의 해시를 미리 계산해 대조합니다. 해시 체인을 쓰는 엄밀한 Rainbow Table 구현은 아닙니다.
- Bulk Hash의 고유 결과 개수는 암호학적 충돌의 증거나 부재 증명이 아닙니다. 입력 중복 가능성을 함께 고려해야 합니다.
- Benchmark는 짧은 실행의 평균으로, JVM 예열 및 컴퓨터 상태에 영향을 받습니다. 절대 성능이나 보안 강도 순위를 뜻하지 않습니다.
- 앱은 개인 PC에서 실행하는 실습을 전제로 합니다. 사전 데이터는 실행 중인 서버 메모리에만 있으며 앱을 종료하면 사라집니다.
- 실제 개인정보, 운영 비밀번호, 비밀키는 입력하지 마세요.

## 코드 구성

- `src/main/resources/templates/index.html`: Thymeleaf 화면
- `src/main/resources/static/css/style.css`: 반응형 화면과 테마
- `src/main/resources/static/js/lab.js`: 화면 전환, API 호출, 결과 시각화
- `src/main/resources/static/js/example-viewer.js`: 예시 코드 창과 복사 기능
- `src/main/resources/static/examples/*.java`: 실습별 독립 Java 예제 9개
- `src/main/java/com/hyo/cryptolab/`: 기존 암호화 서비스 및 API

테스트: `mvn test`
