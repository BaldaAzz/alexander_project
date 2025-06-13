package alexander.project.dto;

import alexander.project.models.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private String accountName;
    private String accountCurrency;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
} 