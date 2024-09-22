package com.github.crypto.to.moon.trading.service.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.crypto.to.moon.trading.service.account.InsufficientBalanceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) throws InvalidOrderException, InsufficientBalanceException {
        Order order = orderService.createOrder(orderRequest);

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setClientOrderId(orderRequest.getClientOrderId());
        orderResponse.setOrderId(order.getOrderId());
        orderResponse.setStatus(order.getStatus().name());
        orderResponse.setMessage("");
        return ResponseEntity.ok(orderResponse);
    }


    @Data
    public static class OrderResponse {
        private String orderId;
        private String clientOrderId;
        private String status;
        private String message;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CommonResponse {
        private String code;
        private Object data;
        private String message;
    }
}
