package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sample.cafekiosk.spring.domain.product.ProductRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductNumberFactory {

    private final ProductRepository productRepository;

    public String createNextProductNumber() {
        String latestProductNumber = productRepository.findLatestProductNumber();
        int lastProductNumberInt = Optional.ofNullable(latestProductNumber).map(Integer::valueOf).orElse(0);
        int nextProductNumberInt = lastProductNumberInt + 1;
        return String.format("%03d", nextProductNumberInt);
    }
}
