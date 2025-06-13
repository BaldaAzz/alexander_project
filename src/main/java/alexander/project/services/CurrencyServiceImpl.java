package alexander.project.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Кэш курсов валют (базовая валюта USD)
    private final Map<String, BigDecimal> usdRatesCache = new ConcurrentHashMap<>();
    private long lastFetchTime = 0;
    private static final long CACHE_VALIDITY = 1000 * 60 * 60; // Кэшировать на 1 час

    @Override
    public Map<String, BigDecimal> getExchangeRates(String baseCurrency, String[] targetCurrencies) {
        fetchRatesIfNeeded();
        Map<String, BigDecimal> rates = new HashMap<>();

        BigDecimal baseRateInUSD = usdRatesCache.get(baseCurrency);
        if (baseRateInUSD == null || baseRateInUSD.compareTo(BigDecimal.ZERO) == 0) {
             logger.error("Exchange rate for base currency {} not found or is zero.", baseCurrency);
             return Collections.emptyMap();
        }

        for (String targetCurrency : targetCurrencies) {
             if (targetCurrency.equals(baseCurrency)) {
                 rates.put(targetCurrency, BigDecimal.ONE);
                 continue;
             }
            BigDecimal targetRateInUSD = usdRatesCache.get(targetCurrency);
            if (targetRateInUSD != null && targetRateInUSD.compareTo(BigDecimal.ZERO) > 0) {
                // Конвертируем из baseCurrency в USD, затем из USD в targetCurrency
                // amount_in_usd = amount_in_base / baseRateInUSD
                // amount_in_target = amount_in_usd * targetRateInUSD
                // rate_base_to_target = targetRateInUSD / baseRateInUSD
                BigDecimal rate = targetRateInUSD.divide(baseRateInUSD, 4, RoundingMode.HALF_UP);
                rates.put(targetCurrency, rate);
            } else {
                logger.warn("Missing or invalid rate for target currency: {}", targetCurrency);
            }
        }

        logger.info("Successfully calculated exchange rates from {} to {}.", baseCurrency, Arrays.toString(targetCurrencies));

        return rates;
    }

    @Override
    public BigDecimal convertAmount(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        fetchRatesIfNeeded();

        BigDecimal fromRateInUSD = usdRatesCache.get(fromCurrency);
        BigDecimal toRateInUSD = usdRatesCache.get(toCurrency);

        if (fromRateInUSD == null || toRateInUSD == null || fromRateInUSD.compareTo(BigDecimal.ZERO) == 0) {
            logger.error("Could not get exchange rates for {} or {}.", fromCurrency, toCurrency);
            // Возвращаем исходную сумму или выбрасываем исключение, в зависимости от требуемой логики ошибки
            return BigDecimal.ZERO; // Или throw new RuntimeException("Could not convert currency");
        }

        // Конвертация: Amount (From) -> USD -> Amount (To)
        // Amount_in_USD = Amount_in_From / Rate_From_in_USD
        // Amount_in_To = Amount_in_USD * Rate_To_in_USD
        // Amount_in_To = (Amount_in_From / Rate_From_in_USD) * Rate_To_in_USD
        // Amount_in_To = Amount_in_From * (Rate_To_in_USD / Rate_From_in_USD)
        BigDecimal amountInUSD = amount.divide(fromRateInUSD, 4, RoundingMode.HALF_UP);
        BigDecimal convertedAmount = amountInUSD.multiply(toRateInUSD);

        logger.info("Converted {} {} to {} {}. Rate used: {} {} = {} {}",
                    amount, fromCurrency, convertedAmount, toCurrency,
                    BigDecimal.ONE, fromCurrency, toRateInUSD.divide(fromRateInUSD, 4, RoundingMode.HALF_UP), toCurrency);

        return convertedAmount.setScale(2, RoundingMode.HALF_UP); // Округляем до 2 знаков после запятой
    }

    private void fetchRatesIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFetchTime > CACHE_VALIDITY || usdRatesCache.isEmpty()) {
            logger.info("Fetching fresh exchange rates from API.");
            try {
                String url = API_URL;
                String jsonResponse = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode ratesNode = root.path("rates");

                if (ratesNode.isObject()) {
                    usdRatesCache.clear();
                    ratesNode.fields().forEachRemaining(entry -> {
                        if (entry.getValue().isNumber()) {
                            usdRatesCache.put(entry.getKey(), entry.getValue().decimalValue());
                        }
                    });
                    lastFetchTime = currentTime;
                    logger.info("Exchange rates fetched and cached successfully.");
                } else {
                    logger.error("Unexpected API response format: {}", jsonResponse);
                }
            } catch (Exception e) {
                logger.error("Error fetching or processing exchange rates", e);
                // В случае ошибки загрузки курсов, попробуем использовать старые из кэша, если они есть.
                if (usdRatesCache.isEmpty()) {
                    logger.error("Exchange rate cache is empty and fetching failed. Currency conversion may not work.");
                }
            }
        }
    }
} 