package com.Uqar.moneybox.entity;

import com.Uqar.user.Enum.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency", length = 3, nullable = false)
    private Currency fromCurrency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency", length = 3, nullable = false)
    private Currency toCurrency;
    
    @Column(name = "rate", precision = 15, scale = 6, nullable = false)
    private BigDecimal rate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    @Column(name = "source", length = 100)
    private String source;
    
    @Column(name = "notes", length = 500)
    private String notes;
}
