package alexander.project.models;

import alexander.project.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "categories")
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String icon; // путь к иконке или имя класса иконки

    @Column(nullable = false)
    private String color;

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "transaction_count")
    private int transactionCount = 0;

    @Column(name = "percentage")
    private double percentage = 0.0;

    @Column(name = "budget_limit")
    private BigDecimal budgetLimit;

    @Column(name = "notification_threshold")
    private Integer notificationThreshold = 80; // Default to 80%

    @Column(name = "is_archived")
    private boolean archived = false;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
} 