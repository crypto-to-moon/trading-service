package org.example.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Trade {
    private String buyOrderId;
    private String sellOrderId;
    private BigDecimal price;
    private BigDecimal quantity;
    private Long timestamp;
    private String symbol;
}

