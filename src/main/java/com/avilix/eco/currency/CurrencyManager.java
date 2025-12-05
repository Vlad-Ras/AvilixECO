package com.avilix.eco.currency;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.avilix.eco.avilix;
import com.avilix.eco.data.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CurrencyManager {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final Map<UUID, PlayerBalance> PLAYER_BALANCES = new ConcurrentHashMap<>();

    public static void init() {
        // Загружаем сохраненные балансы
        Map<UUID, Double> savedBalances = DataManager.loadBalances();
        savedBalances.forEach((uuid, balance) -> {
            PLAYER_BALANCES.put(uuid, new PlayerBalance(uuid, balance));
        });
        avilix.LOGGER.info("Загружено {} балансов", savedBalances.size());
    }

    public static double getBalance(UUID playerId) {
        PlayerBalance balance = PLAYER_BALANCES.get(playerId);
        return balance != null ? balance.getAmount() : 0.0;
    }

    public static double getBalance(Player player) {
        return getBalance(player.getUUID());
    }

    public static boolean hasEnough(UUID playerId, double amount) {
        return getBalance(playerId) >= amount;
    }

    public static TransactionResult addBalance(UUID playerId, double amount, String source) {
        if (amount <= 0) return TransactionResult.INVALID_AMOUNT;

        PlayerBalance balance = PLAYER_BALANCES.computeIfAbsent(
                playerId,
                id -> new PlayerBalance(id, 0.0)
        );

        double newBalance = balance.add(amount);
        DataManager.saveBalance(playerId, newBalance);

        avilix.LOGGER.debug("Игроку {} добавлено {} Нокс (источник: {})", playerId, amount, source);
        return TransactionResult.SUCCESS;
    }

    public static TransactionResult removeBalance(UUID playerId, double amount, String reason) {
        if (amount <= 0) return TransactionResult.INVALID_AMOUNT;

        PlayerBalance balance = PLAYER_BALANCES.get(playerId);
        if (balance == null) return TransactionResult.PLAYER_NOT_FOUND;
        if (!balance.has(amount)) return TransactionResult.INSUFFICIENT_FUNDS;

        double newBalance = balance.remove(amount);
        DataManager.saveBalance(playerId, newBalance);

        avilix.LOGGER.debug("С игрока {} списано {} Нокс (причина: {})", playerId, amount, reason);
        return TransactionResult.SUCCESS;
    }

    public static TransactionResult setBalance(UUID playerId, double amount) {
        if (amount < 0) return TransactionResult.INVALID_AMOUNT;

        PlayerBalance balance = PLAYER_BALANCES.computeIfAbsent(
                playerId,
                id -> new PlayerBalance(id, 0.0)
        );

        balance.set(amount);
        DataManager.saveBalance(playerId, amount);

        return TransactionResult.SUCCESS;
    }

    public static TransactionResult transfer(UUID fromId, UUID toId, double amount, String description) {
        if (amount <= 0) return TransactionResult.INVALID_AMOUNT;
        if (fromId.equals(toId)) return TransactionResult.SELF_TRANSFER;

        // Проверяем наличие средств
        if (!hasEnough(fromId, amount)) {
            return TransactionResult.INSUFFICIENT_FUNDS;
        }

        // Выполняем перевод
        removeBalance(fromId, amount, "Перевод игроку " + toId);
        addBalance(toId, amount, "Перевод от игрока " + fromId);

        // Логируем транзакцию
        DataManager.logTransaction(fromId, toId, amount, description);

        return TransactionResult.SUCCESS;
    }

    public static Component format(double amount) {
        return Component.literal(DECIMAL_FORMAT.format(amount) + " Нокс")
                .withStyle(ChatFormatting.GOLD);
    }

    public static void saveAll() {
        DataManager.saveAllBalances(PLAYER_BALANCES);
    }

    public enum TransactionResult {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        PLAYER_NOT_FOUND,
        INVALID_AMOUNT,
        SELF_TRANSFER,
        ERROR
    }
}