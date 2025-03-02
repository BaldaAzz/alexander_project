package alexander.project.models.enums;

public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход"),
    TRANSFER("Перевод"),
    OTHER("Другое");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
} 