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