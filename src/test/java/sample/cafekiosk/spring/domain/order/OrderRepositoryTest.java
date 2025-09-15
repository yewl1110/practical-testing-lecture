package sample.cafekiosk.spring.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.product.ProductSellingStatus.SELLING;

@ActiveProfiles("test")
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    @DisplayName("입력한 두 날짜 사이의 특정한 상태값의 주문을 가져온다.")
    @Test
    void findOrdersBy() {
        // given
        Product product = Product.builder()
                .type(ProductType.HANDMADE)
                .name("아메리카노")
                .productNumber("001")
                .sellingStatus(SELLING)
                .price(3000)
                .build();
        Product savedProduct = productRepository.save(product);

        Order order = Order.create(List.of(savedProduct), LocalDateTime.of(2020, 1, 2, 0, 0));
        Order savedOrder = orderRepository.save(order);

        LocalDateTime startDateTime = LocalDateTime.of(2020, 1, 2, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2020, 1, 2, 1, 0);

        // when
        List<Order> ordersBy = orderRepository.findOrdersBy(startDateTime, endDateTime, OrderStatus.INIT);

        // then
        assertThat(ordersBy)
                .hasSize(1)
                .extracting("id", "orderStatus")
                .containsExactlyInAnyOrder(
                        tuple(savedOrder.getId(), savedOrder.getOrderStatus())
                )
        ;
    }

    @DisplayName("검색한 날짜 범위에 해당되지 않는 주문은 가져오지 않는다.")
    @Test
    void findOrdersByWithOutsideDateRange() {
        // given
        Product product = Product.builder()
                .type(ProductType.HANDMADE)
                .name("아메리카노")
                .productNumber("001")
                .sellingStatus(SELLING)
                .price(3000)
                .build();
        Product savedProduct = productRepository.save(product);

        Order order = Order.create(List.of(savedProduct), LocalDateTime.of(2020, 1, 2, 0, 0));
        Order savedOrder = orderRepository.save(order);

        LocalDateTime startDateTime = LocalDateTime.of(2020, 1, 2, 1, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2020, 1, 2, 2, 0);

        // when
        List<Order> ordersBy = orderRepository.findOrdersBy(startDateTime, endDateTime, OrderStatus.INIT);

        // then
        assertThat(ordersBy).isEmpty();
    }

    @DisplayName("입력한 두 날짜 사이의 특정한 상태값의 주문을 가져온다.")
    @Test
    void findOrdersByWithAnotherStatus() {
        // given
        Product product = Product.builder()
                .type(ProductType.HANDMADE)
                .name("아메리카노")
                .productNumber("001")
                .sellingStatus(SELLING)
                .price(3000)
                .build();
        Product savedProduct = productRepository.save(product);

        Order order = Order.create(List.of(savedProduct), LocalDateTime.of(2020, 1, 2, 0, 0));
        Order savedOrder = orderRepository.save(order);

        LocalDateTime startDateTime = LocalDateTime.of(2020, 1, 2, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2020, 1, 2, 1, 0);

        // when
        List<Order> ordersBy = orderRepository.findOrdersBy(startDateTime, endDateTime, OrderStatus.COMPLETED);

        // then
        assertThat(ordersBy).isEmpty();
    }
}