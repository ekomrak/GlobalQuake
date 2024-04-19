package globalquake.client;

import globalquake.alert.AlertManager;
import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.events.GlobalQuakeEventHandler;
import globalquake.core.station.GlobalStationManager;
import globalquake.events.GlobalQuakeLocalEventHandler;
import globalquake.intensity.ShakemapService;
import globalquake.main.Main;
import globalquake.sounds.SoundsService;
import globalquake.speech.SpeechAndSoundService;
import globalquake.telegram.TelegramService;
import globalquake.ui.globalquake.GlobalQuakeFrame;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GlobalQuakeLocal extends GlobalQuake {

    @SuppressWarnings("unused")
    private final AlertManager alertManager;
    private final GlobalQuakeLocalEventHandler localEventHandler;

    public static GlobalQuakeLocal instance;
    private final ShakemapService shakemapService;

    private final TelegramBotsLongPollingApplication botsApplication;
    private final SoundsService soundsService;
    private TelegramService telegramService;
    private final SpeechAndSoundService speechService;

    protected GlobalQuakeFrame globalQuakeFrame;

    public GlobalQuakeLocal() {
        instance = this;
        this.localEventHandler = new GlobalQuakeLocalEventHandler().runHandler();
        super.eventHandler = new GlobalQuakeEventHandler().runHandler();

        this.alertManager = new AlertManager();
        this.shakemapService = new ShakemapService();
        this.soundsService = new SoundsService();
        this.botsApplication = new TelegramBotsLongPollingApplication();
        if (!Settings.telegramBotUsername.isEmpty()) {
            this.telegramService = new TelegramService(new OkHttpTelegramClient(Settings.telegramBotToken));
        }
        this.speechService = new SpeechAndSoundService();
    }

    public GlobalQuakeLocal(StationDatabaseManager stationDatabaseManager) {
        super(stationDatabaseManager);
        instance = this;

        this.localEventHandler = new GlobalQuakeLocalEventHandler().runHandler();

        this.alertManager = new AlertManager();
        this.shakemapService = new ShakemapService();
        this.soundsService = new SoundsService();
        this.botsApplication = new TelegramBotsLongPollingApplication();
        if (!Settings.telegramBotUsername.isEmpty()) {
            this.telegramService = new TelegramService(new OkHttpTelegramClient(Settings.telegramBotToken));
        }
        this.speechService = new SpeechAndSoundService();
    }

    public GlobalQuakeLocal(StationDatabaseManager stationDatabaseManager, GlobalStationManager globalStationManager) {
        super(stationDatabaseManager, globalStationManager);
        instance = this;

        this.localEventHandler = new GlobalQuakeLocalEventHandler().runHandler();

        this.alertManager = new AlertManager();
        this.shakemapService = new ShakemapService();
        this.soundsService = new SoundsService();
        this.botsApplication = new TelegramBotsLongPollingApplication();
        if (!Settings.telegramBotUsername.isEmpty()) {
            this.telegramService = new TelegramService(new OkHttpTelegramClient(Settings.telegramBotToken));
        }
        this.speechService = new SpeechAndSoundService();
    }

    public GlobalQuakeLocal createFrame() {
        if (!Settings.telegramBotUsername.isEmpty()) {
            try {
                botsApplication.registerBot(Settings.telegramBotToken, telegramService);
                telegramService.onRegister();
            } catch (TelegramApiException e) {
                Logger.error(e);
            }
        }

        EventQueue.invokeLater(() -> {
            try {
                globalQuakeFrame = new GlobalQuakeFrame();
                globalQuakeFrame.setVisible(true);

                Main.getErrorHandler().setParent(globalQuakeFrame);

                globalQuakeFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        for (Earthquake quake : getEarthquakeAnalysis().getEarthquakes()) {
                            getArchive().archiveQuake(quake);
                        }

                        getArchive().saveArchive();
                    }
                });
            }catch (Exception e){
                Logger.error(e);
                System.exit(0);
            }
        });
        return this;
    }

    protected TelegramBotsLongPollingApplication getBotsApplication() {
        return botsApplication;
    }

    protected TelegramService getTelegramService() {
        return telegramService;
    }

    @SuppressWarnings("unused")
    @Override
    public void destroy() {
        super.destroy();
        getLocalEventHandler().stopHandler();
        getShakemapService().stop();
        soundsService.destroy();
        telegramService.destroy();
        speechService.destroy();
        try {
            botsApplication.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public GlobalQuakeLocalEventHandler getLocalEventHandler() {
        return localEventHandler;
    }

    public GlobalQuakeFrame getGlobalQuakeFrame() {
        return globalQuakeFrame;
    }

    public ShakemapService getShakemapService() {
        return shakemapService;
    }

    @Override
    public void clear() {
        super.clear();
        shakemapService.clear();
        alertManager.clear();
        getGlobalQuakeFrame().clear();
    }

    @Override
    public boolean limitedSettings() {
        return false;
    }

    @Override
    public boolean limitedWaveformBuffers() {
        return false;
    }

}
