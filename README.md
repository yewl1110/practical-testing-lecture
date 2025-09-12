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

### 정상 케이스 테스트
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
### 예외 케이스 테스트
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
### ❌ 문제 코드 (시간에 직접 의존)
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
### ✅ 개선 코드 (시간을 파라미터로 주입)
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