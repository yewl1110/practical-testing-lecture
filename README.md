# Practical Testing: 실용적인 테스트 가이드

## 섹션 3. 단위 테스트

<details>
<summary><strong>강의 6. 수동테스트</strong></summary>

### ❌문제 코드

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

### ✅테스트 라이브러리를 사용한 코드

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

<details>
<summary><strong>강의 20. Persistence Layer 테스트</strong></summary>

- Persistence Layer
    - DataAccess의 역할
    - 비즈니스 로직이 포함되면 안됨

- JPA에서의 repository의 쿼리 메서드는 테스트를 통해 쿼리 생성이 의도대로 되는지 확인 필요
- `@DataJpaTest` 는 JPA에 필요한 빈만 올려서 테스트 (`@SpringBootTest` 에 비해 속도 빠름)

---

### ✅ Persistence Layer 테스트 코드

```java

@ActiveProfiles("test")
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @DisplayName("원하는 판매상태를 가진 상품들을 조회한다.")
    @Test
    void findAllBySellingStatusIn() {
        // given
        // product1, product2, product3 생성
        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        List<Product> products = productRepository.findAllBySellingStatusIn(List.of(SELLING, HOLD));

        // then
        assertThat(products).hasSize(2)
                .extracting("productNumber", "name", "sellingStatus")
                .containsExactlyInAnyOrder(
                        tuple("001", "아메리카노", SELLING),
                        tuple("002", "카페라떼", HOLD)
                );
    }
}
```

</details>

<details>
<summary><strong>강의 23. Business Layer 테스트</strong></summary>

- Business Layer
    - 비즈니스 로직을 구현하는 역할
    - Persistence Layer와의 상호작용을 통해 비즈니스 로직 전개
    - 트랜잭션 보장해야 함

---

### ✅ Business Layer 통합 테스트 코드

```java

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder() {
        // given
        // product1, product2, product3 생성
        productRepository.saveAll(List.of(product1, product2, product3));

        // when
        // 생성된 productNumbers로 product를 가져옴 

        // then
        // 데이터 검증
    }

    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있다.")
    @Test
    void createOrderWithDuplicateProductNumbers() {
        // given
        // product1, product2, product3 생성
        productRepository.saveAll(List.of(product1, product2, product3));

        OrderCreateRequest request = OrderCreateRequest.builder()
                .productNumbers(List.of("001", "001"))
                .build();

        // when
        // 생성된 productNumbers로 product를 가져옴 

        // then
        // 데이터 검증
    }
}
```

### @AfterEach

- `@SpringBootTest` 환경에서는 테스트 메서드 간 **데이터가 공유**되는 문제가 발생할 수 있음
- 단일 메서드 실행 시에는 통과하지만, **클래스 전체 실행 시에는 데이터 누적으로 실패**할 수 있음
- 해결 방법:
    - `@AfterEach`에서 매번 데이터를 초기화
    - `@Transactional`을 사용하여 **테스트 종료 시 자동 롤백**되도록 설정

---

## ⚠️ @Transactional 사용시 주의할 점

### 테스트 클래스에서만 @Transactional을 사용하는 경우

**문제 상황**

- `OrderService`에는 `@Transactional`이 선언되어 있지 않음에도,  
  테스트 클래스에 `@Transactional`이 붙어 있으면 테스트 실행 시 트랜잭션이 활성화된다.
- 이 때문에 실제 서비스 코드가 트랜잭션 없이도 정상 동작하는 것처럼 보일 수 있다.
- 즉, **실제 환경과 다른 조건에서 테스트가 통과**하면서 잘못된 확신을 줄 위험이 있다.

**왜 위험한가?**

- 서비스 로직에서 `dirty checking`(JPA 변경 감지) 같은 기능은 **트랜잭션이 반드시 있어야 작동**한다.
- 테스트에서는 `@Transactional` 덕분에 업데이트가 적용되지만,  
  운영 환경에서 트랜잭션이 없다면 동일한 코드가 동작하지 않는다.

**권장 방법**

- 테스트 클래스에만 `@Transactional`을 추가하는 대신,  
  **실제 서비스 코드(`Service` 계층)** 에 트랜잭션을 선언하는 것이 안전하다.
- 테스트에서는 트랜잭션이 필요하면 `@Transactional`을 추가하되,  
  이는 **테스트 자체의 편의(롤백 등)** 을 위한 용도로만 사용해야 한다.

```java

@Service
public class OrderService {
    public OrderResponse createOrder(OrderCreateRequest request, LocalDateTime registeredDateTime) {
        List<Stock> stocks = stockRepository.findAllByProductNumberIn(stockProductNumbers);
        for (String stockProductNumber : new HashSet<>(stockProductNumbers)) {
            Stock stock = stockMap.get(stockProductNumber);
            stock.deductQuantity(quantity); // quantity update
        }
        return OrderResponse.of(savedOrder);
    }
}
```

```java

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @DisplayName("재고와 관련된 상품이 포함되어 있는 주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrderWithStock() {
        // given
        // when
        OrderResponse orderResponse = orderService.createOrder(request, registeredDateTime);

        // then
    }
}
```

</details>

<details>
<summary><strong>강의 25. Presentation Layer 테스트</strong></summary>

### MockMvc

- 스프링 MVC 동작을 `Mock` 객체로 재현할 수 있는 테스트 프레임워크
- 실제 서버를 띄우지 않고도 컨트롤러 계층을 테스트 가능
- `@MockMvcTest`
    - 컨트롤러 관련 Bean만 로드하여 테스트 수행
    - 서비스, 리포지토리 등은 Mocking 필요

### Mocking

- 메소드를 실제 호출하지 않고, 호출 시 리턴값을 미리 지정하는 것
- 보통 서비스 계층을 직접 실행하지 않고, **컨트롤러 단위 테스트**에서 사용
- 서비스 단의 실제 동작 검증은 **통합 테스트**에서 진행한다고 가정

```java
    // Mock Bean 생성    
@MockitoBean
private ProductService productService;

// productService의 getSellingProducts 메서드를 Mocking
List<ProductResponse> result = List.of();

when(productService.getSellingProducts()).

thenReturn(result);
```

- @Transactional(readOnly = true)
    - 읽기전용 트랜잭션이 열림
    - crud 에서 read만 작동함
    - JPA: CUD 스냅샷 저장, 변경감지 X (성능 향상)
    - CQRS - Command / Query 분리
        - 장애 발생 시 범위를 줄임
        - 서비스 분리
        - DB endpoint 분리 가능 (read DB, write DB)

### ✅ Presentation Layer 테스트 코드

- 주로 input의 validation이나, http status 값 등을 검증한다.

#### 1. 필수값 검증 테스트

- 신규 상품 등록 시 **상품 이름이 누락되었을 때** `400 BAD_REQUEST` 가 발생하는지 확인

```java

@DisplayName("신규 상품을 등록할 때 상품 이름은 필수값이다.")
@Test
void createProductWithoutName() throws Exception {
    // given
    ProductCreateRequest request = ProductCreateRequest.builder()
            .type(HANDMADE)
            .sellingStatus(SELLING)
            .price(4000)
            .build();

    // when
    // then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/products/new")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("400"))
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("상품 이름은 필수입니다."))
            .andExpect(jsonPath("$.data").isEmpty())
    ;
}
```

#### 2. API 응답 검증 테스트

- 판매 상품 조회 API 호출 시 200 OK 와 JSON 응답 구조를 검증

```java

@DisplayName("판매 상품을 조회한다.")
@Test
void getSellingProducts() throws Exception {
    // given
    List<ProductResponse> result = List.of();
    when(productService.getSellingProducts()).thenReturn(result);

    // when
    // then
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/products/selling"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("200"))
            .andExpect(jsonPath("$.status").value("OK"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data").isArray())
    ;
}
```

</details>

## 섹션 7. Mock

<details>
<summary><strong>🧪 Mockito Test Double 정리</strong></summary>

## 🧪 Mockito Test Double 정리

### Stubbing

- **정의**: Mock 객체에 원하는 행위를 지정하는 것
- 예: `when(mock.method()).thenReturn(value)`

---

### Test Double 종류

- **Dummy**
    - 아무 동작도 하지 않는 깡통 객체
- **Fake**
    - 단순한 형태로 동일한 기능은 수행하지만, 실제 프로덕션에는 쓰기 어려운 객체
    - 예: `FakeRepository` (CRUD를 `Map` 으로 구현)
- **Stub**
    - 요청에 대해 **미리 준비된 결과**를 반환하는 객체
    - 주로 **상태 검증**에 사용
- **Spy**
    - Stub + 호출 기록 확인 가능
    - 일부는 실제 객체처럼 동작시키고, 일부만 Stubbing 가능
- **Mock**
    - 행위에 대한 **기대(Expectation)**를 명세하고, 그에 따라 동작하도록 만든 객체
    - 주로 **행위 검증**에 사용

---

### Stub vs Mock

- **Stub**: 상태 검증 (ex. "요청 후 객체가 이 상태로 바뀌었어?")
- **Mock**: 행위 검증 (ex. "이 메서드가 몇 번 호출됐고 어떤 값을 리턴했어?")

---

### Mockito 주요 어노테이션

- `@Mock`
    - Mock 객체 생성
    - 내부에서 호출하는 다른 메서드들도 모두 Stubbing 필요
- `@InjectMocks`
    - Mock/Spy 객체들을 주입받아 실제 객체 생성
- `@Spy`
    - 실제 객체를 기반으로 생성
    - 일부는 실제 기능 사용, 일부는 Stubbing

---

## 📌 코드 예제: Mock vs Spy 비교

```java

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Spy
    private MailSendClient mailSendClient;   // Spy 사용 시 실제 객체 기반
    @Mock
    private MailSendHistoryRepository mailSendHistoryRepository;

    @InjectMocks
    private MailService mailService;

    @DisplayName("메일 전송 테스트 (Mock 사용)")
    @Test
    void sendMail_withMock() {
        // given
        MailSendClient mailSendClient = mock(MailSendClient.class);
        MailService mailService = new MailService(mailSendClient, mailSendHistoryRepository);

        when(mailSendClient.sendEmail(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // when
        boolean result = mailService.sendMail("", "", "", "");

        // then
        assertThat(result).isTrue();
        verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
    }

    @DisplayName("메일 전송 테스트 (Spy 사용)")
    @Test
    void sendMail_withSpy() {
        // given
        doReturn(true)
                .when(mailSendClient)
                .sendEmail(anyString(), anyString(), anyString(), anyString());

        // when
        boolean result = mailService.sendMail("", "", "", "");

        // then
        assertThat(result).isTrue();
        verify(mailSendHistoryRepository, times(1)).save(any(MailSendHistory.class));
    }
}
```

### Mock만 사용했을 때

- mailSendClient는 완전한 가짜 객체
- 실제 로직은 전혀 실행되지 않고, 오직 Stubbing 한 동작만 수행
- 모든 동작을 직접 when(...).thenReturn(...) 으로 지정해야 함
- 장점: 독립적이고 부수효과 없는 테스트 가능
- 단점: 실제 객체 동작 기반 검증은 불가능

### Spy를 사용했을 때

- mailSendClient는 실제 객체 기반
- 지정하지 않은 부분은 실제 로직이 실행되고, 필요한 부분만 doReturn() 으로 Stubbing
- 장점: 실제 동작을 활용하면서도 부분적으로 제어 가능
- 단점: 실제 로직 실행으로 부수효과(side effect) 발생 가능

</details>

## 섹션 8. 더 나은 테스트를 작성하기 위한 구체적 조언

### 1. 한 문단에 한 주제

- **한 문단에 한 주제**: 한 테스트는 한 가지 목적만 검증한다.
- **복잡한 제어문 지양**: `if`, `for` 같은 분기·반복문을 사용하면 가독성이 떨어진다.
- **가독성 유지**: 무엇을 테스트하고 어떤 환경을 구성하는지 명확해야 한다.
- **도구 활용**: `@ParameterizedTest`로 중복을 단순화한다.
- **표현 점검**: `@DisplayName`은 한 문장으로 목적을 설명할 수 있도록 작성한다.

<details>
<summary>💡 코드 예시 보기 (❌ Bad vs ✅ Good)</summary>

### ❌ 안 좋은 예시

- 하나의 테스트 메서드에서 for와 if문으로 여러 경우를 동시에 검증
- 어떤 경우가 실패했는지 직관적으로 알기 어려움

```java

@DisplayName("상품 타입이 재고 관련 타입인지 체크한다.")
@Test
void containsStockTypeEx() {
    // given
    ProductType[] productTypes = ProductType.values();

    for (ProductType productType : productTypes) {
        if (productType == ProductType.HANDMADE) {
            // when
            boolean result = ProductType.containsStockType(productType);

            // then
            assertThat(result).isFalse();
        }

        if (productType == ProductType.BAKERY || productType == ProductType.BOTTLE) {
            // when
            boolean result = ProductType.containsStockType(productType);

            // then
            assertThat(result).isTrue();
        }
    }
}
```

### ✅ 올바른 예시

- 각 경우를 별도의 테스트로 분리하여 명확한 목적 검증
- 실패 시 어떤 케이스가 잘못됐는지 바로 확인 가능

```java

@DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
@Test
void containsStockType() {
    // given
    ProductType givenType = ProductType.HANDMADE;

    // when
    boolean result = ProductType.containsStockType(givenType);

    // then
    assertThat(result).isFalse();
}

@DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
@Test
void containsStockType2() {
    // given
    ProductType givenType = ProductType.BOTTLE;

    // when
    boolean result = ProductType.containsStockType(givenType);

    // then
    assertThat(result).isTrue();
}
```

</details>

---

### 2. 완벽하게 제어하기

- **테스트는 항상 동일한 결과**를 내야 한다. (실행할 때마다 성공/실패가 바뀌면 안 됨)
- **테스트 환경을 완벽하게 구성하고 제어**할 수 있어야 한다.
- **제어 가능한 영역을 확실히 지정**해야 한다.
    - 외부 시스템과 연동하는 경우는 `Mock` 사용

<details>
<summary>💡 코드 예시 보기 (❌ Bad vs ✅ Good)</summary>

### ❌ 안 좋은 예시

- 테스트가 `LocalDateTime.now()` 같은 비제어 가능한 값을 의존
- 실행 시점에 따라 성공 여부가 달라질 수 있음

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

### ✅ 올바른 예시

- 현재 시간을 테스트가 제어 가능한 파라미터로 전달
- 외부 요인(LocalDateTime.now)에 영향을 받지 않음

```java
    public Order createOrder(LocalDateTime currentDateTime) {
    LocalTime currentTime = currentDateTime.toLocalTime();
    if (currentTime.isBefore(SHOP_OPEN_TIME) || currentTime.isAfter(SHOP_CLOSE_TIME)) {
        throw new IllegalArgumentException("주문 시간이 아닙니다.");
    }

    return new Order(LocalDateTime.now(), beverages);
}
```

</details>

---

### 3. 테스트 환경의 독립성을 보장하자

- **다른 API를 끌어다 쓰지 말자**: 테스트 환경에서 불필요하게 결합도를 높이지 않는다.
- **given 절 단순화**: 테스트 데이터는 **생성자 기반**으로 구성하는 것이 좋다.
- **팩토리 메서드 지양**: 테스트에서는 팩토리 메서드 사용을 피한다.
    - 팩토리 메서드는 목적이 들어가므로 테스트 맥락을 흐리게 할 수 있다.

<details>
<summary>💡 코드 예시 보기 (❌ Bad vs ✅ Good)</summary>

### ❌ 안 좋은 예시

- 테스트의 관심사는 `create` 함수인데, **재고 차감 로직**을 직접 호출하고 있음
- given 절에서 맥락을 이해하기 위해 불필요하게 많은 생각이 필요함
- 해당 기능(재고 차감)에 문제가 생길 경우:
    - 본래 검증 주제인 **주문 생성**이 아닌 다른 이유로 테스트가 깨질 수 있음
    - 실패 원인을 파악하기 어려움

```java

@DisplayName("재고가 없는 상품으로 주문을 생서하려는 경우 예외가 발생한다.")
@Test
void createOrderWithNoStock() {
    // given
    LocalDateTime registeredDateTime = LocalDateTime.now();

    Product product1 = createProduct(BOTTLE, "001", 1000);
    Product product2 = createProduct(BAKERY, "002", 3000);
    Product product3 = createProduct(HANDMADE, "003", 5000);
    productRepository.saveAll(List.of(product1, product2, product3));

    Stock stock1 = Stock.create("001", 2);
    Stock stock2 = Stock.create("002", 2);
    stock1.deductQuantity(1); //TODO: 이렇게 작성하면 안됨
    stockRepository.saveAll(List.of(stock1, stock2));

    OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
            .productNumbers(List.of("001", "001", "002", "003"))
            .build();

    // when
    // then
    assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("재고가 부족한 상품이 있습니다.");
}
```

### ✅ 올바른 예시

- 테스트 주제를 흐리는 재고 차감 로직 제거
- 생성자 기반으로 필요한 상태를 직접 세팅
- 실패했을 때 원인이 명확히 드러남

```java

@DisplayName("재고가 없는 상품으로 주문을 생서하려는 경우 예외가 발생한다.")
@Test
void createOrderWithNoStock() {
    // given
    LocalDateTime registeredDateTime = LocalDateTime.now();

    Product product1 = createProduct(BOTTLE, "001", 1000);
    Product product2 = createProduct(BAKERY, "002", 3000);
    Product product3 = createProduct(HANDMADE, "003", 5000);
    productRepository.saveAll(List.of(product1, product2, product3));

    Stock stock1 = Stock.builder()
            .productNumber("001")
            .quantity(1)
            .build();
    Stock stock2 = Stock.builder()
            .productNumber("002")
            .quantity(2)
            .build();
    stockRepository.saveAll(List.of(stock1, stock2));

    OrderCreateServiceRequest request = OrderCreateServiceRequest.builder()
            .productNumbers(List.of("001", "001", "002", "003"))
            .build();

    // when
    // then
    assertThatThrownBy(() -> orderService.createOrder(request, registeredDateTime))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("재고가 부족한 상품이 있습니다.");
}
```

</details>

---

### 4. 테스트 간 독립성을 보장하자

- 테스트 사이에는 **순서가 영향을 주면 안 된다.**
- 각각의 테스트는 **독립적으로 성공과 실패가 보장**되어야 한다.
- 테스트 간 **공유 자원 사용을 피하라.**
- 하나의 객체가 변화하는 모습을 테스트하고 싶다면
    - `@DynamicTest`를 사용하자.

<details>
<summary>💡 코드 예시 보기 (❌ Bad vs ✅ Good)</summary>

### ❌ 안 좋은 예시

- 두 개 이상의 테스트가 하나의 자원(`static Stock`)을 공유하고 있음
- 객체 내부 상태를 변경하는 테스트라면 다른 테스트 결과에 영향을 줄 수 있음

```java
    private static final Stock stock = Stock.create("001", 1);

@DisplayName("재고의 수량이 제공된 수량보다 작은지 확인한다.")
@Test
void isQuantityLessThan() {
    // given
    int quantity = 2;

    // when
    boolean quantityLessThan = stock.isQuantityLessThan(quantity);

    // then
    assertThat(quantityLessThan).isTrue();
}

@DisplayName("재고를 주어진 개수만큼 차감할 수 있다.")
@Test
void deductQuantity() {
    // given
    int quantity = 1;

    // when
    stock.deductQuantity(quantity);

    // then
    assertThat(stock.getQuantity()).isZero();
}
```

### ✅ 올바른 예시

- 각 테스트마다 새로운 객체를 생성하여 독립적으로 동작
- 테스트 간에 서로 영향을 주지 않음

```java

@DisplayName("재고의 수량이 제공된 수량보다 작은지 확인한다.")
@Test
void isQuantityLessThan() {
    // given
    Stock stock = Stock.create("001", 1);
    int quantity = 2;

    // when
    boolean quantityLessThan = stock.isQuantityLessThan(quantity);

    // then
    assertThat(quantityLessThan).isTrue();
}

@DisplayName("재고를 주어진 개수만큼 차감할 수 있다.")
@Test
void deductQuantity() {
    // given
    Stock stock = Stock.create("001", 1);
    int quantity = 1;

    // when
    stock.deductQuantity(quantity);

    // then
    assertThat(stock.getQuantity()).isZero();
}
```

</details>

---

### 5. 한 눈에 들어오는 Test Fixture 구성하기

#### ✅ 기본 개념

- **Fixture**: `given` 절에서 생성한 **테스트용 객체(들)**.
- 목적: 테스트를 **원하는 상태로 고정**해 재현성과 가독성 확보.

#### ✅ 원칙

- **테스트별로 필요한 것만** 생성한다. (불필요한 공통화 지양)
- **겹치는 `given` 데이터**가 많더라도, 테스트 의도를 흐리면 공통화하지 않는다.
- **객체 생성 함수(빌더/팩토리)**의 **파라미터는 “테스트에 필요한 필드만”** 명시한다.
- **data.sql** 등 **전역 초기 데이터 주입은 지양**한다. (숨겨진 의존성↑, 유지보수↓)

#### 🚫 피해야 할 것 (Anti-Patterns)

- **공통 Fixture를 `setup`(공용 필드)로 끌어올리기**
    - 테스트 간 **결합도↑**, 수정 시 **광역 영향** 발생, **긴 클래스에서 위치 탐색 어려움**.
- **테스트 전용 추상 클래스로 생성 로직 상속**
    - 계층 복잡도↑, 추론 비용↑, 테스트 의도 은닉.
- **환경/데이터 전역 상태에 의존** (예: `data.sql`, 싱글톤/캐시 공유)

#### 🧰 `@BeforeEach` / `@BeforeAll` 사용 가이드

- 아래 **두 질문**에 모두 “예”라면 사용 고려:
    1) **테스트를 이해하는 데 해당 셋업을 몰라도 문제가 없는가?**
    2) **셋업을 수정해도 모든 테스트에 영향을 주지 않는가?**
- 권장 사용 예
    - **공통 상수/불변값 초기화** (예: 고정된 시간대/통화 단위 등)
    - **가벼운 테스트 더블 초기화** (예: `Mock` 기본 스텁)
    - **반복적인 보일러플레이트 축소** (단, 의도 가시성 유지)
- 지양 사용 예
    - **케이스별로 달라지는 상태를 공용 셋업에 넣기**
    - **상태를 변경하는(Fixture 변형) 로직을 공용 셋업에 넣기**

#### 📝 실무 팁

- **테스트 메서드 내부 `given`의 가시성**을 최우선으로:
    - “이 테스트가 무엇을 보장하려는지”를 **코드만 보고** 이해 가능해야 함.
- **빌더/팩토리 설계**
    - 테스트 전용 빌더는 **필수 파라미터만 노출**, 나머지는 합리적 기본값 사용.
    - “목적이 드러나는 팩토리”는 의도를 왜곡할 수 있으니 **테스트에선 신중히**.
- **Fixture 드리프트 방지**
    - 공통화가 필요하면 **최소화**하고, **불변/재사용 가능한 것만** 올린다.
- **체크리스트**
    - 이 테스트는 **다른 테스트 없이도** 완전한가?
    - Fixture는 **이 테스트의 의도**를 바로 보여주는가?
    - 공용 셋업을 **수정했을 때 파급 범위**가 명확한가?

---

### 6. Test Fixture 클렌징

#### ✅ 개념 요약

- **@Transactional**: 테스트 종료 시 자동 **rollback**으로 정리를 쉽게 할 수 있음
    - 단, 트랜잭션 경계·지연쓰기(Flush)·2차 캐시 등 **사이드 이펙트**를 이해하고 사용
- **deleteAllInBatch**: 대상 테이블을 **한 번의 배치 쿼리**로 전체 삭제
    - **외래키(FK) 제약**이 있으면 **삭제 순서**가 중요 (자식 → 부모)
- **deleteAll**: 엔티티를 **select 후 한 건씩 delete**
    - 연관관계 매핑이 있으면 관련 엔티티도 순차 삭제될 수 있으나,  
      매핑이 없으면 **FK 제약을 해소하지 못해 오류**가 날 수 있음 (느림)

#### 🔍 옵션별 동작 & 주의점

- **@Transactional (테스트 메서드/클래스에 적용)**
    - 장점: 테스트마다 **깨끗한 상태 보장**, 빠른 정리(rollback), 독립성↑
    - 주의: **Lazy 로딩/Flush 타이밍** 차이, **프로덕션과 다른 트랜잭션 경계**로 인한 오해,  
      필요 시 **명시적 `flush()/clear()`** 고려

- **deleteAllInBatch (Repository)**
    - 장점: **매우 빠름**(단일 배치 쿼리), 대량 초기화/정리에 적합
    - 주의: **삭제 순서 제어 필요**(자식 → 부모),  
      영속성 컨텍스트와 **상태 불일치** 방지를 위해 적절한 `clear` 고려

- **deleteAll (Repository)**
    - 장점: 엔티티 단위 삭제 → **연관관계 매핑**에 따라 안전하게 제거 가능
    - 주의: **성능 저하**(N번 select+delete), 매핑 부재 시 **FK 위반** 가능

---

### 7. @ParameterizedTest

- 같은 검증 로직을 **여러 입력값**으로 반복 실행할 때 사용한다.
- 테스트 케이스를 **데이터만 바꿔** 간결하게 표현하고 중복을 줄인다.
- **소량·단순 값** → `@CsvSource`, **복합 타입/계산 로직 포함** → `@MethodSource`.

<details>
<summary>💡 코드 예시 보기 </summary>

### @CsvSource

```java

@DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
@CsvSource({"HANDMADE,false", "BOTTLE,true", "BAKERY,true"})
@ParameterizedTest
void containsStockType4(ProductType productType, boolean expected) {
    // when
    boolean result = ProductType.containsStockType(productType);

    // then
    assertThat(result).isEqualTo(expected);
}
```

### @MethodSource

```java
private static Stream<Arguments> provideProductTypesForCheckingStockType() {
    return Stream.of(
            Arguments.of(ProductType.HANDMADE, false),
            Arguments.of(ProductType.BOTTLE, true),
            Arguments.of(ProductType.BAKERY, true)
    );
}

@DisplayName("상품 타입이 재고 관련 타입인지를 체크한다.")
@MethodSource("provideProductTypesForCheckingStockType")
@ParameterizedTest
void containsStockType5(ProductType productType, boolean expected) {
    // when
    boolean result = ProductType.containsStockType(productType);

    // then
    assertThat(result).isEqualTo(expected);
}

```

</details>

---

### 8. @DynamicTest

- **하나의 환경(Fixture)을 고정**해 두고 **사용자 시나리오를 순차적으로** 검증할 때 사용한다.
- 같은 객체의 **상태 변화를 연속적으로** 점검하는 흐름 테스트(인수/시나리오)에 적합하다.
- 시나리오 특성상 순서 의존이 생길 수 있다. 단위 테스트에서는 가급적 **독립 케이스(→ @ParameterizedTest)**를 우선하고, 상태 흐름 검증에 한해 @DynamicTest를 사용한다.

<details>
<summary>💡 코드 예시 보기</summary>

```java

@DisplayName("재고 차감 시나리오")
@TestFactory
Collection<DynamicTest> stockDeductionDynamicTest() {
    // given
    Stock stock = Stock.create("001", 1);

    return List.of(
            DynamicTest.dynamicTest("재고를 주어진 개수만큼 차감할 수 있다.", () -> {
                // given
                int quantity = 1;

                // when
                stock.deductQuantity(quantity);

                // then
                assertThat(stock.getQuantity()).isZero();
            }),
            DynamicTest.dynamicTest("재고보다 많은 수의 수량으로 차감 시도하는 경우 예외가 발생한다.", () -> {
                // given
                int quantity = 1;

                // when // then
                assertThatThrownBy(() -> stock.deductQuantity(quantity))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("차감할 재고 수량이 없습니다.");
            })
    );
}
```

</details>

---

### 9. 테스트 환경 통합하기

- **실행 시간을 줄이려면** 테스트마다 **ApplicationContext 재시작**을 피해야 한다.
- `@SpringBootTest`는 **프로파일 / 설정 / MockBean 구성이 조금만 달라도** 컨텍스트를 새로 띄운다.
- `@MockBean`을 쓰면 **빈 교체**가 일어나므로 컨텍스트가 다시 올라간다.  
  → **공통 `@MockBean`을 상위 추상 클래스**로 모으거나, **Mock 유무로 테스트 환경을 분리**하자.
- `@DataJpaTest`는 JPA 슬라이스만 올려 **빠르지만**, 컨텍스트를 따로 띄운다.  
  → **통합이 더 이득**이면 `@SpringBootTest`로 통합하고 **`@Transactional`**을 추가하자.
- `@WebMvcTest`는 컨트롤러 계층만 테스트 → **별도 환경**을 구축(슬라이스)하되, 동일한 슬라이스는 **공통 베이스**로 묶자.

<details>
<summary>💡 코드 예시 보기</summary>

### ❌ 통합 테스트 - 환경 통합 전

```java

@ActiveProfiles("test")
@SpringBootTest
class OrderStatisticsServiceTest {

    @MockitoBean
    protected MailSendClient mailSendClient;
}

@ActiveProfiles("test")
@DataJpaTest
class ProductRepositoryTest {

}
```

### ✅ 통합 테스트 - 환경 통합 후

- MailSendClient가 다른 테스트에서 실제 빈으로 필요하다면, 이 베이스 클래스와 환경을 분리

```java

class OrderStatisticsServiceTest extends IntegrationTestSupport {

}

class ProductRepositoryTest extends IntegrationTestSupport {

}

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationTestSupport {

    @MockitoBean
    protected MailSendClient mailSendClient;
}
```

---

### ❌ WebMvcTest - 환경 통합 전

```java

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;
}

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;
}
```

### ✅ 통합 테스트 - 환경 통합 후

```java
class OrderControllerTest extends ControllerTestSupport {

}

class ProductControllerTest extends ControllerTestSupport {

}

@WebMvcTest(controllers = {OrderController.class, ProductController.class})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected OrderService orderService;

    @MockitoBean
    protected ProductService productService;
}
```

</details>

---

### 10. ❌ private 메서드 테스트

- **하지 않는다.**
- private 메서드를 테스트하고 싶다면 **객체(책임) 분리 시점**인지 고민한다.
- 클라이언트 입장에서는 private 메서드를 **알 필요가 없다**.
- **public 메서드를 검증**하면 그 내부에서 호출되는 private 메서드도 **자연스럽게 검증**된다.

#### 왜 피해야 하나?

- 구현 세부사항(캡슐화)을 테스트에 노출 → **리팩터링 저항** 증가
- 테스트가 내부 구조에 **강하게 결합** → 유지보수성 저하
- **무엇(행동)**이 아니라 **어떻게(구현)**를 검증하게 됨 → 테스트 가치 하락

---

### 11. 테스트에서만 필요한 메서드가 생겼다면?

- 생성자, getter, 기본생성자, size 등 객체에 필요한 행위이고, 미래에 필요할 수 있다고 생각되면 만들기
- 만들어도 되지만 보수적으로
