package alexander.project.services;

import java.math.BigDecimal;
import java.util.Map;
 
public interface CurrencyService {
    Map<String, BigDecimal> getExchangeRates(String baseCurrency, String[] targetCurrencies);
    BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency);
} 