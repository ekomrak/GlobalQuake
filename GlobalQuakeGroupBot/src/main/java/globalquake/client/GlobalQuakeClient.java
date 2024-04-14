package globalquake.client;

import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.events.GlobalQuakeEventHandler;
import globalquake.events.GlobalQuakeLocalEventHandler;
import globalquake.intensity.ShakemapService;
import globalquake.telegram.TelegramService;
import globalquake.telegram.util.MapImageDrawer;
import gqserver.api.Packet;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;


import java.io.IOException;


public class GlobalQuakeClient extends GlobalQuake {
    public static GlobalQuakeClient instance;
    private final ClientSocket clientSocket;

    private final GlobalQuakeLocalEventHandler localEventHandler;
    private final ShakemapService shakemapService;

    private final TelegramBotsLongPollingApplication botsApplication;
    private final TelegramService telegramService;

    public GlobalQuakeClient(ClientSocket clientSocket) {
        instance = this;
        this.clientSocket = clientSocket;

        super.globalStationManager = new GlobalStationManagerClient();
        super.earthquakeAnalysis = new EarthquakeAnalysisClient();
        super.clusterAnalysis = new ClusterAnalysisClient();
        super.archive = new EarthquakeArchiveClient();
        super.seedlinkNetworksReader = new SeedlinkNetworksReaderClient();
        super.eventHandler = new GlobalQuakeEventHandler().runHandler();

        this.localEventHandler = new GlobalQuakeLocalEventHandler().runHandler();
        this.shakemapService = new ShakemapService();
        this.botsApplication = new TelegramBotsLongPollingApplication();
        this.telegramService = new TelegramService(new OkHttpTelegramClient(Settings.telegramBotToken));
        new MapImageDrawer();
    }

    public void processPacket(ClientSocket socket, Packet packet) throws IOException {
        ((EarthquakeAnalysisClient)getEarthquakeAnalysis()).processPacket(socket, packet);
        ((EarthquakeArchiveClient)getArchive()).processPacket(socket, packet);
        ((GlobalStationManagerClient)getStationManager()).processPacket(socket, packet);
        ((ClusterAnalysisClient)getClusterAnalysis()).processPacket(socket, packet);
    }

    @Override
    public boolean limitedSettings() {
        return true;
    }

    @Override
    public boolean limitedWaveformBuffers() {
        return false;
    }

    public GlobalQuakeLocalEventHandler getLocalEventHandler() {
        return localEventHandler;
    }

    public ShakemapService getShakemapService() {
        return shakemapService;
    }

    public void init() {
        try {
            botsApplication.registerBot(Settings.telegramBotToken, telegramService);
            telegramService.onRegister();
        } catch (TelegramApiException e) {
            Logger.error(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        telegramService.destroy();
        try {
            botsApplication.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }
}
