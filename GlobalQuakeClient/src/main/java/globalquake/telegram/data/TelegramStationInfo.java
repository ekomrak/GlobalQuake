package globalquake.telegram.data;

import globalquake.client.data.ClientStation;

public class TelegramStationInfo extends TelegramAbstractInfo<ClientStation> {
    private double intensity;

    public TelegramStationInfo(ClientStation clientStation) {
        this.intensity = clientStation.getMaxRatio60S();
    }

    @Override
    public void updateWith(ClientStation clientStation) {
        this.intensity = clientStation.getMaxRatio60S();
    }

    @Override
    public boolean equalsTo(ClientStation clientStation) {
        return clientStation.getMaxRatio60S() == intensity;
    }
}
