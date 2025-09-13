# Practical Testing: 실용적인 테스트 가이드

## 섹션 3. 단위 테스트

<details>
<summary><strong>강의 6. 수동테스트</strong></summary>

```java
    @Test
    void add() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        cafeKiosk.add(new Americano());

        System.out.println(">>> 담긴 음료 수 : " + cafeKiosk.getBeverages().size());
        System.out.println(">>> 담긴 음료 : " + cafeKiosk.getBeverages().get(0).getName());
    }
```

### 문제점

- 무엇을 검증하는건지 알 수 없음
- 콘솔 출력은 검증이 아님 → 항상 성공처럼 보임

</details>

<details>
<summary><strong>강의 7. JUnit5로 테스트하기</strong></summary>

- 단위테스트
  - 작은 코드 단위를 독립적으로 검증
  - 클래스 or 메서드 단위
  - JUnit5 에 AssertJ 얹어서 씀

```java
    @Test
    void getName() {
        Americano americano = new Americano();

        assertEquals("아메리카노", americano.getName()); //JUnit5
        assertThat(americano.getName()).isEqualTo("아메리카노"); // AssertJ
    }
```

</details>

<details>
<summary><strong>강의 8. 테스트 케이스 세분화하기</strong></summary>

- 요구사항

  - 질문하기: 암묵적이거나 드러나지 않는 요구사항이 있는가?
- 테스트 케이스 세분화하기

  - 해피 케이스
  - 예외 케이스

  **→ 경계값 테스트가 중요 (범위, 구간, 날짜 등)**

  - ex) 3 이상의 값을 받는 API
    - 경계값 테스트:3
    - 예외 테스트:2

### ✅정상 케이스 테스트

```java
    @Test
    void addSeveralBeverages() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        Americano americano = new Americano();

        cafeKiosk.add(americano, 2);

        assertThat(cafeKiosk.getBeverages().get(0)).isEqualTo(americano);
        assertThat(cafeKiosk.getBeverages().get(1)).isEqualTo(americano);
    }
```

### ✅예외 케이스 테스트

```java
    @Test
    void addZeroBeverages() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        Americano americano = new Americano();

        assertThatThrownBy(() -> cafeKiosk.add(americano, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("음료는 1잔 이상 주문하실 수 있습니다.")
        ;
    }
```

</details>

<details>
<summary><strong>강의 9. 테스트하기 어려운 영역을 분리하기</strong></summary>

- 테스트가 어려운 부분을 외부로 분리할수록 테스트 가능한 코드는 많아진다
- 테스트하기 어려운 영역
  - 관측할 때마다 다른 값에 의존하는 코드
    - 현재 시간, 랜던 값, 사용자 입력
  - 외부 세계에 영향을 주는 코드
    - 출력, 메시지 전송, DB에 기록
  - 함수를 기준으로 input, output 에 의존
- 테스트 하기 좋은 코드
  - 순수함수
    - 같은 입력에는 항상 같은 결과
    - 외부 세상과 단절 된 형태

---

### ❌문제 코드 (시간에 직접 의존)

```java
public class CafeKiosk {

    private static final LocalTime SHOP_OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime SHOP_CLOSE_TIME = LocalTime.of(22, 0);
  
    public Order createOrder() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalTime currentTime = currentDateTime.toLocalTime();
        if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
            throw new IllegalArgumentException("주문 시간이 아닙니다.");
        }

        return new Order(LocalDateTime.now(), beverages);
    }
}
```

```java
    @Test
    void createOrder() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        Americano americano = new Americano();

        cafeKiosk.add(americano);

        Order order = cafeKiosk.createOrder();
        assertThat(order.getBeverages()).hasSize(1);
        assertThat(order.getBeverages().get(0).getName()).isEqualTo("아메리카노");
    }
```

### 문제점

- LocalDateTime.now() → 실행 시각에 따라 테스트가 깨질 수 있다

### 해결 방안

- createOrder() 의 currentDateTime을 현재 시각이 아닌 파라미터로 입력받도록 수정

### ✅개선 코드 (시간을 파라미터로 주입)

```java
    public Order createOrder(LocalDateTime currentDateTime) {
        LocalTime currentTime = currentDateTime.toLocalTime();
        if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
            throw new IllegalArgumentException("주문 시간이 아닙니다.");
        }

        return new Order(LocalDateTime.now(), beverages);
    }
```

```java
    @Test
    void createOrderWithCurrentTime() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        Americano americano = new Americano();

        cafeKiosk.add(americano);

        Order order = cafeKiosk.createOrder(LocalDateTime.of(2025, 1, 17, 22, 0));

        assertThat(order.getBeverages()).hasSize(1);
        assertThat(order.getBeverages().get(0).getName()).isEqualTo("아메리카노");
    }

    @Test
    void createOrderWithOutsideOpenTime() {
        CafeKiosk cafeKiosk = new CafeKiosk();
        Americano americano = new Americano();

        cafeKiosk.add(americano);

        assertThatThrownBy(() -> cafeKiosk.createOrder(LocalDateTime.of(2025, 1, 17, 9, 59)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 시간이 아닙니다.")
        ;
    }
```

</details>

## 섹션 4. TDD: Test Driven Development

<details>
<summary><strong>강의 11. TDD</strong></summary>

- 프로덕션 코드보다 테스트 코드를 먼저 작성
- RED -> GREEN -> REFACTOR
- RED (실패 테스트 작성) -> GREEN (테스트 통과하는 최소한의 코딩) -> REFACTOR (구현 코드 개선, 테스트 통과 유지)
- 선 기능 구현의 단점

  - 테스트 누락 가능성
  - 특정 테스트 케이스만 검증할 가능성
  - 잘못된 구현을 늦게 발견할 수 있음
- TDD 장점

  - 복잡도가 낮은 코드
  - 엣지 케이스를 쉽게 발견
  - 구현에 대한 빠른 피드백
  - 과감한 리팩토링 가능
- 키워드

  - 애자일 방법론
  - 익스트림 프로그래밍
  - 스크럼
  - 칸반

</details>

## 섹션 5. 테스트는 문서다

<details>
<summary><strong>강의 14. DisplayName을 섬세하게</strong></summary>

- JUnit5부터 DisplayName annotation 사용 가능
- DisplayName 잘 짓기
  - 문장으로 짓기
  - ~ 테스트 로 끝나는 문장은 지양
  - 테스트 행위에 대한 결과까지 기술하기
    - ❌ 음료 1개 주문 테스트
    - ✅ 음료를 1개 추가하면 주문 목록에 담긴다.
  - 도메인 용어를 사용해서 추상화 된 내용을 담기
    - 메서드 자체의 관점보다 도메인 정책 관점으로
  - 테스트의 현상을 중점으로 기술하지 말 것 (실패한다 등등)
    - ❌ 특정 시간 이전에 주문을 생성하면 실패한다.
    - ✅ 영업 시간 이전에는 주문을 생성할 수 없다.

</details>

<details>
<summary><strong>강의 15. BDD</strong></summary>

- Behavior Driven Development
- TDD에서 파생
- 시나리오에 기반한 테스트케이스 자체에 집중하여 테스트
- 개발자가 아닌 사람이 봐도 이해할 수 있을 정도의 추상화 수준
- Given: 시나리오 진행에 필요한 준비 과정
- When: 시나리오 행동 진행
- Then: 시나리오 진행에 대한 결과 검증

</details>

## 섹션 6. Spring & JPA 기반 테스트

<details>
<summary><strong>강의 17. 레이어드 아키텍처와 테스트</strong></summary>

- 각 레이어가 실제로 작동할 때 여러 모듈의 조합으로 작동된다.
- 통합 테스트
  - 여러 모듈이 협력하는 기능을 통합적으로 검증
  - 작은범위의 테스트만으로는 기능 전체의 신뢰성 보장 못함
  - 풍부한 단위 테스트
  - 큰 기능 단위를 검증하는 통합 테스트

</details>