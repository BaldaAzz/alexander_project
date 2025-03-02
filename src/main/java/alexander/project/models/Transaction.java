package alexander.project.models;

import alexander.project.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Положительное значение amount - доход, отрицательное - расход
    public boolean isIncome() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isExpense() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    // Геттер для получения абсолютной величины суммы транзакции
    public BigDecimal getAbsoluteAmount() {
        return amount.abs();
    }
    
    // Метод для автоматического определения типа транзакции на основе суммы
    @PrePersist
    @PreUpdate
    private void setTypeBasedOnAmount() {
        if (this.type == null) {
            if (isIncome()) {
                this.type = TransactionType.INCOME;
            } else if (isExpense()) {
                this.type = TransactionType.EXPENSE;
            } else {
                this.type = TransactionType.OTHER;
            }
        }
    }
} 