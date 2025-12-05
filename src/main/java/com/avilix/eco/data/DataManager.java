package com.avilix.eco.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.avilix.eco.AvilixECO;
import com.avilix.eco.currency.PlayerBalance;

public class DataManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path CONFIG_DIR = Paths.get("config/aivlix_eco");
    private static final Path BALANCES_FILE = CONFIG_DIR.resolve("balances.json");
    private static final Path TRANSACTIONS_FILE = CONFIG_DIR.resolve("transactions.json");

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (!Files.exists(BALANCES_FILE)) {
                Files.writeString(BALANCES_FILE, "{}");
                AvilixECO.LOGGER.info("Создан файл балансов");
            }
            if (!Files.exists(TRANSACTIONS_FILE)) {
                Files.writeString(TRANSACTIONS_FILE, "[]");
                AvilixECO.LOGGER.info("Создан файл транзакций");
            }

        } catch (IOException e) {
            AvilixECO.LOGGER.error("Ошибка создания директорий конфигурации", e);
        }
    }

    public static Map<UUID, Double> loadBalances() {
        try {
            String json = Files.readString(BALANCES_FILE);
            Type type = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> stringMap = GSON.fromJson(json, type);

            Map<UUID, Double> result = new HashMap<>();
            if (stringMap != null) {
                stringMap.forEach((key, value) -> {
                    try {
                        result.put(UUID.fromString(key), value);
                    } catch (IllegalArgumentException e) {
                        AvilixECO.LOGGER.warn("Некорректный UUID в файле балансов: {}", key);
                    }
                });
            }

            AvilixECO.LOGGER.info("Загружено {} балансов из файла", result.size());
            return result;

        } catch (IOException e) {
            AvilixECO.LOGGER.error("Ошибка загрузки балансов", e);
            return new HashMap<>();
        }
    }

    public static void saveBalance(UUID playerId, double amount) {
        try {
            Map<UUID, Double> balances = loadBalances();
            balances.put(playerId, amount);

            // Преобразуем Map<UUID, Double> в Map<String, Double> для сохранения
            Map<String, Double> stringMap = new HashMap<>();
            balances.forEach((uuid, balance) -> {
                stringMap.put(uuid.toString(), balance);
            });

            String json = GSON.toJson(stringMap);
            Files.writeString(BALANCES_FILE, json);

        } catch (IOException e) {
            AvilixECO.LOGGER.error("Ошибка сохранения баланса для {}", playerId, e);
        }
    }

    public static void saveAllBalances(Map<UUID, PlayerBalance> balances) {
        try {
            Map<String, Double> toSave = new HashMap<>();
            balances.forEach((uuid, balance) -> {
                toSave.put(uuid.toString(), balance.getAmount());
            });

            String json = GSON.toJson(toSave);
            Files.writeString(BALANCES_FILE, json);
            AvilixECO.LOGGER.info("Сохранено {} балансов", balances.size());

        } catch (IOException e) {
            AvilixECO.LOGGER.error("Ошибка сохранения всех балансов", e);
        }
    }

    public static void logTransaction(UUID from, UUID to, double amount, String description) {
        try {
            String json = Files.readString(TRANSACTIONS_FILE);
            Type listType = new TypeToken<List<TransactionRecord>>(){}.getType();
            List<TransactionRecord> transactions = GSON.fromJson(json, listType);

            if (transactions == null) {
                transactions = new ArrayList<>();
            }

            transactions.add(new TransactionRecord(from, to, amount, description));

            // Ограничиваем размер лога последними 1000 транзакций
            if (transactions.size() > 1000) {
                transactions = transactions.subList(transactions.size() - 1000, transactions.size());
            }

            String newJson = GSON.toJson(transactions);
            Files.writeString(TRANSACTIONS_FILE, newJson);

        } catch (IOException e) {
            AvilixECO.LOGGER.error("Ошибка логирования транзакции", e);
        }
    }

    public static void saveAll() {
        AvilixECO.LOGGER.info("Все данные сохранены");
    }

    // Вспомогательный класс для записи транзакций
    private static class TransactionRecord {
        String from;
        String to;
        double amount;
        String description;
        long timestamp;

        TransactionRecord(UUID from, UUID to, double amount, String description) {
            this.from = from != null ? from.toString() : null;
            this.to = to != null ? to.toString() : null;
            this.amount = amount;
            this.description = description;
            this.timestamp = System.currentTimeMillis();
        }
    }
}