package com.aivlix.eco;

// AivlixECO.java
@Mod(AivlixECO.MODID)
public class AivlixECO {
    public static final String MODID = "aivlixeco";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String CURRENCY_NAME = "Нокс";

    public AivlixECO(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onServerStarting);
        modBus.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Инициализация Aivlix ECO мода...");
            DataManager.init(); // Инициализация файлового хранилища
            CurrencyManager.init();
        });
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Сервер Aivlix ECO запущен");
    }

    private void onServerStopping(ServerStoppingEvent event) {
        DataManager.saveAll(); // Сохраняем все данные при остановке
        LOGGER.info("Данные Aivlix ECO сохранены");
    }
}
