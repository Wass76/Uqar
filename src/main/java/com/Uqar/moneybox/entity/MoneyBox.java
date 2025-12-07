package com.Uqar.moneybox.entity;

import com.Uqar.moneybox.enums.MoneyBoxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_box")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBox {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pharmacy_id", nullable = false)
    private Long pharmacyId;
    
    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance;
    
    @Column(name = "initial_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal initialBalance;
    
    @Column(name = "last_reconciled")
    private LocalDateTime lastReconciled;
    
    @Column(name = "reconciled_balance", precision = 15, scale = 2)
    private BigDecimal reconciledBalance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MoneyBoxStatus status;
    
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
