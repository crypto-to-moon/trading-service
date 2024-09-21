package org.example.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class OrderController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest orderRequest) throws ExecutionException, InterruptedException {
        OrderResponse orderResponse = clientService.sendOrder(orderRequest);

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
