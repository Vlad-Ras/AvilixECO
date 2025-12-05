package com.avilix.eco;

import com.avilix.eco.currency.CurrencyManager;
import com.avilix.eco.data.DataManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AvilixECO.MODID)
public class AvilixECO {
    public static final String MODID = "avilixeco";
    public static final Logger LOGGER = LogManager.getLogger();

    public AvilixECO(IEventBus modBus) {
        LOGGER.info("Avilix ECO Mod инициализирован!");

        // Регистрируем обработчики событий
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new com.avilix.eco.commands.EcoCommands());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Инициализируем DataManager при запуске сервера
        DataManager.init();
        CurrencyManager.init();
    }
}