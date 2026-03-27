# JavaTest2 — TCP 소켓 Echo(에코) 예제

로컬 네트워크에서 **서버–클라이언트** 한 쌍이 `localhost`와 포트 **5000**으로 TCP 연결을 맺고, 콘솔에서 입력한 텍스트를 주고받는 Java 콘솔 프로그램입니다. IntelliJ IDEA용 모듈 구조(`.idea`, `JavaTest2.iml`)로 관리될 수 있습니다.

---

## 프로젝트 구조

| 경로 | 설명 |
|------|------|
| `src/EchoServer.java` | 포트 5000에서 대기 후 **한 번에 하나의** 클라이언트 연결을 받는 서버 |
| `src/EchoClient.java` | `localhost:5000`에 접속해 표준 입력으로 메시지를 보내는 클라이언트 |

---

## `EchoServer.java` 상세

### 역할

- **`ServerSocket`**으로 **포트 5000**을 열고, **`accept()`**로 첫 번째 클라이언트 연결만 처리합니다.
- 연결이 잡히면 **`Socket`**의 입력 스트림에서 **한 줄 단위(`readLine()`)**로 읽습니다.
- 읽은 내용은 **`System.out`에 `"client : "` 접두사와 함께** 로그로 출력합니다.

### 주요 API와 동작

| 요소 | 설명 |
|------|------|
| `try (ServerSocket …)` | try-with-resources로 서버 소켓을 자동으로 닫습니다. |
| `serverSocket.accept()` | 연결 요청이 올 때까지 블로킹한 뒤, 연결된 `Socket`을 반환합니다. |
| `BufferedReader` + `InputStreamReader` | 바이트 스트림을 문자로 디코딩하고, 줄 단위로 읽기 적합하게 버퍼링합니다. |
| `PrintWriter(..., true)` | 두 번째 인자 `true`는 **auto-flush**: `println` 시 즉시 상대로 전송됩니다. |
| `while ((line = in.readLine()) != null)` | 스트림이 끝나기 전까지 한 줄씩 반복 처리합니다. 연결이 끊기면 `null`이 될 수 있습니다. |
| `line.equals("exit")` | 소문자 `"exit"`와 **정확히 일치**할 때만 종료 분기로 들어갑니다(대소문자 구분). |
| 종료 시 | `out.println("Bye")`로 클라이언트에게 한 줄을 보낸 뒤 `break`로 루프를 빠져나옵니다. |

### 현재 구현에서 알아두면 좋은 점

- **`"exit"`가 아닌 일반 줄에 대해서는 서버가 `out.println(...)`으로 응답하지 않습니다.**  
  즉, “에코(받은 그대로 되돌려줌)” 동작은 구현되어 있지 않고, **종료 시에만 `"Bye"` 한 줄**을 보냅니다.

---

## `EchoClient.java` 상세

### 역할

- **`Socket("localhost", 5000)`**으로 서버에 접속합니다.
- **`Scanner(System.in)`**으로 사용자가 입력한 **한 줄**을 읽어 서버로 보냅니다.

### 주요 API와 동작

| 요소 | 설명 |
|------|------|
| `try (Socket …)` | 소켓을 try-with-resources로 닫아 연결·스트림 정리를 보장합니다. |
| `PrintWriter` / `BufferedReader` | 서버와 동일하게 **텍스트 한 줄 송수신**에 맞춘 래핑입니다. |
| `message = scanner.nextLine()` | EOF가 아니면 계속 한 줄씩 읽습니다(`null`이 될 때까지). |
| `"exit".equalsIgnoreCase(message)` | **대소문자 무시**로 종료 키워드를 판별합니다. **이 분기에서는 서버로 `"exit"`를 보내지 않고** 루프만 빠져나갑니다. |
| 그 외 메시지 | `out.println(message)`로 전송한 뒤 **`in.readLine()`**으로 서버가 보낸 **한 줄**을 읽어 콘솔에 출력합니다. |

### 서버/클라이언트 종료 조건의 차이

- **서버**는 클라이언트가 **정확히 `"exit"`**라는 줄을 보내면 `"Bye"`를 보내고 해당 연결 처리 루프를 끝냅니다.
- **클라이언트**는 사용자가 **`exit`(대소문자 무관)** 을 입력하면 **서버에 보내지 않고** 로컬에서만 종료합니다.

따라서 **“서버 쪽 종료 프로토콜”을 쓰려면** 클라이언트에서 소문자 `"exit"`를 그대로 서버로 보내야 하고, **클라이언트만 끊고 싶다면** 현재처럼 `equalsIgnoreCase` 분기에서 나가면 됩니다.

### 서버가 일반 메시지에 응답하지 않을 때

서버는 `"exit"`가 아닌 줄에 대해 `PrintWriter`로 아무것도 쓰지 않으므로, 클라이언트의 **`in.readLine()`**은 서버가 줄을 보내거나 스트림이 닫힐 때까지 **대기(블로킹)** 할 수 있습니다. 동작을 “진짜 에코”로 맞추려면 서버 루프 안에서 **`out.println(line);`** 같은 응답 한 줄을 추가하는 방식이 일반적입니다.

---

## 빌드 및 실행

프로젝트 루트(`JavaTest2`)에서:

```bash
# 컴파일 (클래스 파일은 보통 out/ 또는 현재 디렉터리에 두는 방식 중 택1)
javac -d out src/EchoServer.java src/EchoClient.java
```

**실행 순서:** 서버를 먼저 띄운 뒤, 다른 터미널에서 클라이언트를 실행합니다.

```bash
java -cp out EchoServer
java -cp out EchoClient
```

IDE에서는 `EchoServer`와 `EchoClient`의 `main` 각각을 **Run** 하면 됩니다(서버 먼저).

---

## 통신 요약

- **프로토콜:** TCP, 텍스트 **줄 단위**(`\n` 기준 `readLine`/`println`).
- **포트:** `5000`.
- **서버 종료 트리거(설계상):** 클라이언트가 줄 내용이 정확히 `exit`인 메시지를 보낸 경우 → 서버는 `Bye` 한 줄 응답 후 해당 세션 처리 종료.

---

## 의존성

표준 JDK만 사용합니다(`java.net`, `java.io`, `java.util`). 별도 빌드 도구나 외부 라이브러리는 없습니다.
