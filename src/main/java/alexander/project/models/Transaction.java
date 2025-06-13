package alexander.project.models;

import alexander.project.models.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_icon")
    private String categoryIcon;

    @Column(name = "category_color")
    private String categoryColor;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull
    @JsonIgnore
    private Account account;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @JsonIgnore
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @Column(name = "is_recurring")
    private boolean isRecurring = false;

    @Column(name = "recurrence_type")
    private String recurrenceType; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "next_recurrence_date")
    private LocalDateTime nextRecurrenceDate;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reminder> reminders;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "transaction_tags",
        joinColumns = @JoinColumn(name = "transaction_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

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

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public LocalDateTime getDate() {
        return this.date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
    public TransactionType getType() {
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }

    public enum TransactionCategory {
        FOOD("Продукты"),
        TRANSPORT("Транспорт"),
        ENTERTAINMENT("Развлечения"),
        UTILITIES("Коммунальные услуги"),
        SALARY("Зарплата"),
        OTHER("Другое");

        private final String displayName;

        TransactionCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
} 