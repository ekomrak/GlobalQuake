package globalquake.station;

import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.utils.NamedThreadFactory;
import gqserver.api.packets.data.DataRequestPacket;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StationSubscriber {
    private final ScheduledExecutorService subscribeService;

    public StationSubscriber() {
        subscribeService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Subscribe on station data"));
        subscribeService.scheduleAtFixedRate(this::subscribe, 15000, 1000, TimeUnit.MILLISECONDS);
    }

    private void subscribe() {
        try {
            GlobalQuakeClient.instance.getClientSocket().sendPacket(new DataRequestPacket("KZ PDGK BHZ ", false));
            GlobalQuakeClient.instance.getClientSocket().sendPacket(new DataRequestPacket("AD ANAN HNZ ", false));
            GlobalQuakeClient.instance.getClientSocket().sendPacket(new DataRequestPacket("AD TMCH HNZ ", false));
            subscribeService.shutdown();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public void destroy() {
        GlobalQuake.instance.stopService(subscribeService);
    }
}
