package com.avilix.eco.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class EcoCommands {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Основная команда /noks
        LiteralArgumentBuilder<CommandSourceStack> noksCommand = Commands.literal("noks")
                .requires(source -> source.hasPermission(0))
                .executes(context -> showBalance(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.literal("balance")
                        .executes(context -> showBalance(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> showBalance(context.getSource(), EntityArgument.getPlayer(context, "player")))))

                // Команда перевода
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(context -> payPlayer(context)))))

                // Административные команды
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(context -> addMoney(context)))))

                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(context -> removeMoney(context)))))

                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(context -> setMoney(context)))));

        dispatcher.register(noksCommand);
    }

    private int showBalance(CommandSourceStack source, ServerPlayer player) {
        // Временная заглушка
        double balance = 100.0; // Здесь должно быть получение баланса из CurrencyManager
        Component message = Component.literal("Баланс ")
                .append(player.getDisplayName())
                .append(": ")
                .append(Component.literal(balance + " Нокс").withStyle(ChatFormatting.GOLD))
                .withStyle(ChatFormatting.GREEN);

        source.sendSuccess(() -> message, false);
        return 1;
    }

    private int payPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer from = context.getSource().getPlayerOrException();
        ServerPlayer to = EntityArgument.getPlayer(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        if (from == to) {
            context.getSource().sendFailure(Component.literal("Нельзя перевести средства самому себе!"));
            return 0;
        }

        // Временная заглушка
        context.getSource().sendSuccess(() -> Component.literal("Перевод " + amount + " Нокс игроку " + to.getName().getString()), false);

        return 1;
    }

    private int addMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        // Временная заглушка
        context.getSource().sendSuccess(() -> Component.literal("Игроку " + target.getName().getString() + " добавлено " + amount + " Нокс"), true);

        return 1;
    }

    private int removeMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        // Временная заглушка
        context.getSource().sendSuccess(() -> Component.literal("У игрока " + target.getName().getString() + " списано " + amount + " Нокс"), true);

        return 1;
    }

    private int setMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        // Временная заглушка
        context.getSource().sendSuccess(() -> Component.literal("Баланс игрока " + target.getName().getString() + " установлен на " + amount + " Нокс"), true);

        return 1;
    }
}