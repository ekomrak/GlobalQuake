package globalquake.client;

import globalquake.core.seedlink.SeedlinkNetworksReader;
import globalquake.core.Settings;
import globalquake.events.GlobalQuakeLocalEventListener;
import globalquake.events.specific.SocketReconnectEvent;
import globalquake.events.specific.StationCreateEvent;
import globalquake.events.specific.StationMonitorCloseEvent;
import globalquake.events.specific.StationMonitorOpenEvent;
import globalquake.main.Main;
import globalquake.telegram.util.MapImageDrawer;
import globalquake.ui.StationMonitor;
import globalquake.ui.globalquake.GlobalQuakeFrame;
import gqserver.api.Packet;
import gqserver.api.packets.data.DataRequestPacket;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GlobalQuakeClient extends GlobalQuakeLocal {
    private final ClientSocket clientSocket;

    private final List<StationMonitor> openedStationMonitors = new ArrayList<>();

    public GlobalQuakeClient(ClientSocket clientSocket) {
        instance = this;

        super.globalStationManager = new GlobalStationManagerClient();
        super.earthquakeAnalysis = new EarthquakeAnalysisClient();
        this.seedlinkNetworksReader = new SeedlinkNetworksReader();
        super.clusterAnalysis = new ClusterAnalysisClient();
        super.archive = new EarthquakeArchiveClient();
        this.clientSocket = clientSocket;

        getLocalEventHandler().registerEventListener(new GlobalQuakeLocalEventListener() {
            @Override
            public void onStationMonitorOpened(StationMonitorOpenEvent event) {
                try {
                    if (openedStationMonitors.stream().noneMatch(stationMonitor -> stationMonitor.getStation().getIdentifier().equals(event.station().getIdentifier()))) {
                        clientSocket.sendPacket(new DataRequestPacket(event.station().getIdentifier(), false));
                    }

                    openedStationMonitors.add(event.stationMonitor());
                } catch (IOException e) {
                    Logger.trace(e);
                }
            }

            @Override
            public void onStationMonitorClosed(StationMonitorCloseEvent event) {
                try {
                    openedStationMonitors.remove(event.monitor());
                    if (openedStationMonitors.stream().noneMatch(stationMonitor -> stationMonitor.getStation().getIdentifier().equals(event.station().getIdentifier()))) {
                        event.station().getAnalysis().fullReset();
                        clientSocket.sendPacket(new DataRequestPacket(event.station().getIdentifier(), true));
                    }
                } catch (IOException e) {
                    Logger.trace(e);
                }
            }

            @Override
            public void onSocketReconnect(SocketReconnectEvent socketReconnectEvent) {
                try {
                    for (StationMonitor monitor : new HashSet<StationMonitor>(openedStationMonitors)) {
                        clientSocket.sendPacket(new DataRequestPacket(monitor.getStation().getIdentifier(), false));
                    }
                } catch (IOException e) {
                    Logger.trace(e);
                }
            }

            @Override
            public void onStationCreate(StationCreateEvent stationCreateEvent) {
                openedStationMonitors.stream().filter(stationMonitor -> stationMonitor.getStation().getIdentifier().equals(stationCreateEvent.station().getIdentifier())).forEach(stationMonitor -> stationMonitor.swapStation(stationCreateEvent.station()));
            }
        });

        new MapImageDrawer();
    }

    public void processPacket(ClientSocket socket, Packet packet) throws IOException {
        ((EarthquakeAnalysisClient) getEarthquakeAnalysis()).processPacket(socket, packet);
        ((EarthquakeArchiveClient) getArchive()).processPacket(socket, packet);
        ((GlobalStationManagerClient) getStationManager()).processPacket(socket, packet);
        ((ClusterAnalysisClient) getClusterAnalysis()).processPacket(socket, packet);
    }

    @Override
    public GlobalQuakeLocal createFrame() {
        if (!Settings.telegramBotUsername.isEmpty()) {
            try {
                getBotsApplication().registerBot(Settings.telegramBotToken, getTelegramService());
                getTelegramService().onRegister();
            } catch (TelegramApiException e) {
                Logger.error(e);
            }
        }

        try {
            globalQuakeFrame = new GlobalQuakeFrame();
            globalQuakeFrame.setVisible(true);

            Main.getErrorHandler().setParent(globalQuakeFrame);
        } catch (Exception e) {
            Logger.error(e);
            System.exit(0);
        }
        return this;

    }


    public void onIndexingReset(globalquake.client.ClientSocket socket) {
        ((GlobalStationManagerClient) getStationManager()).onIndexingReset(socket);
        ((EarthquakeAnalysisClient) getEarthquakeAnalysis()).onIndexingReset(socket);
        ((ClusterAnalysisClient) getClusterAnalysis()).onIndexingReset(socket);
        ((EarthquakeArchiveClient) getArchive()).onIndexingReset(socket);
    }

    @Override
    public boolean limitedSettings() {
        return true;
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }
}
