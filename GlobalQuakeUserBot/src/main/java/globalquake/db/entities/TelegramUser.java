package globalquake.db.entities;

import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDate;

public class TelegramUser {
    private Long id;
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private String languageCode;
    private Boolean premium;

    private Long orderId;

    private Boolean enabled;
    private LocalDate subscriptionDate;
    private LocalDate updatedDate;

    private Double homeLat;
    private Double homeLon;

    private Boolean enableTelegramEarthquakeAlert;
    private Boolean enableTelegramEarthquakeLocation;
    private Boolean enableTelegramEarthquakeImage;
    private Boolean enableTelegramEarthquakeMap;
    private Double tsEarthquakeMinMagnitudeArea1;
    private Integer tsEarthquakeMaxDistArea1;
    private Double tsEarthquakeMinMagnitudeArea2;
    private Integer tsEarthquakeMaxDistArea2;
    private Integer tsEarthquakeMinIntensity;

    private Boolean enableTelegramPossibleShakingAlert;
    private Boolean enableTelegramPossibleShakingLocation;
    private Boolean enableTelegramPossibleShakingImage;
    private Boolean enableTelegramPossibleShakingMap;
    private Integer tsPossibleShakingMinLevel;
    private Integer tsPossibleShakingMaxDist;

    private Boolean enableTelegramStationHighIntensityAlert;
    private Boolean enableTelegramStationHighIntensityLocation;
    private Boolean enableTelegramStationHighIntensityImage;
    private Boolean enableTelegramStationHighIntensityMap;
    private Integer tsStationMinIntensity1;
    private Integer tsStationMaxDist1;
    private Integer tsStationMinIntensity2;
    private Integer tsStationMaxDist2;

    private Boolean sendImageAsAPhoto;
    private Boolean sendMapAsAPhoto;

    private Boolean sendGraphsAsAPhoto;
    private Boolean sendPDGKStation;
    private Boolean sendANANStation;
    private Boolean sendTMCHStation;

    private Boolean showSmallCities;
    private Boolean showFaults;

    public TelegramUser() {

    }

    public TelegramUser(User user, Long chatId) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
        this.languageCode = user.getLanguageCode();
        this.premium = user.getIsPremium();
        if (this.premium == null) {
            this.premium = false;
        }
        this.chatId = chatId;
        this.subscriptionDate = LocalDate.now();

        setDefaultValues();
    }

    public void updateWith(User user, Long chatId) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.userName = user.getUserName();
        this.languageCode = user.getLanguageCode();
        this.premium = user.getIsPremium();
        if (this.premium == null) {
            this.premium = false;
        }
        this.chatId = chatId;
        this.updatedDate = LocalDate.now();
    }

    private void setDefaultValues() {
        homeLat = 43.238949;
        homeLon = 76.889709;

        enableTelegramEarthquakeAlert = true;
        enableTelegramEarthquakeLocation = false;
        enableTelegramEarthquakeImage = true;
        enableTelegramEarthquakeMap = false;
        tsEarthquakeMinMagnitudeArea1 = 0.0;
        tsEarthquakeMaxDistArea1 = 100;
        tsEarthquakeMinMagnitudeArea2 = 5.0;
        tsEarthquakeMaxDistArea2 = 300;

        enableTelegramPossibleShakingAlert = true;
        enableTelegramPossibleShakingLocation = false;
        enableTelegramPossibleShakingImage = true;
        enableTelegramPossibleShakingMap = false;
        tsPossibleShakingMinLevel = 2;
        tsPossibleShakingMaxDist = 300;

        enableTelegramStationHighIntensityAlert = true;
        enableTelegramStationHighIntensityLocation = false;
        enableTelegramStationHighIntensityImage = false;
        enableTelegramStationHighIntensityMap = true;
        tsStationMinIntensity1 = 4000;
        tsStationMaxDist1 = 100;
        tsStationMinIntensity2 = 10000;
        tsStationMaxDist2 = 400;

        tsEarthquakeMinIntensity = 2;

        sendImageAsAPhoto = true;
        sendMapAsAPhoto = true;
        sendGraphsAsAPhoto = true;

        sendPDGKStation = true;
        sendANANStation = true;
        sendTMCHStation = true;

        showSmallCities = true;
        showFaults = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public Double getHomeLat() {
        return homeLat;
    }

    public void setHomeLat(Double homeLat) {
        this.homeLat = homeLat;
    }

    public Double getHomeLon() {
        return homeLon;
    }

    public void setHomeLon(Double homeLon) {
        this.homeLon = homeLon;
    }

    public Boolean getEnableTelegramEarthquakeAlert() {
        return enableTelegramEarthquakeAlert;
    }

    public void setEnableTelegramEarthquakeAlert(Boolean enableTelegramEarthquakeAlert) {
        this.enableTelegramEarthquakeAlert = enableTelegramEarthquakeAlert;
    }

    public Double getTsEarthquakeMinMagnitudeArea1() {
        return tsEarthquakeMinMagnitudeArea1;
    }

    public void setTsEarthquakeMinMagnitudeArea1(Double tsEarthquakeMinMagnitudeArea1) {
        this.tsEarthquakeMinMagnitudeArea1 = tsEarthquakeMinMagnitudeArea1;
    }

    public Integer getTsEarthquakeMaxDistArea1() {
        return tsEarthquakeMaxDistArea1;
    }

    public void setTsEarthquakeMaxDistArea1(Integer tsEarthquakeMaxDistArea1) {
        this.tsEarthquakeMaxDistArea1 = tsEarthquakeMaxDistArea1;
    }

    public Double getTsEarthquakeMinMagnitudeArea2() {
        return tsEarthquakeMinMagnitudeArea2;
    }

    public void setTsEarthquakeMinMagnitudeArea2(Double tsEarthquakeMinMagnitudeArea2) {
        this.tsEarthquakeMinMagnitudeArea2 = tsEarthquakeMinMagnitudeArea2;
    }

    public Integer getTsEarthquakeMaxDistArea2() {
        return tsEarthquakeMaxDistArea2;
    }

    public void setTsEarthquakeMaxDistArea2(Integer tsEarthquakeMaxDistArea2) {
        this.tsEarthquakeMaxDistArea2 = tsEarthquakeMaxDistArea2;
    }

    public Boolean getEnableTelegramPossibleShakingAlert() {
        return enableTelegramPossibleShakingAlert;
    }

    public void setEnableTelegramPossibleShakingAlert(Boolean enableTelegramPossibleShakingAlert) {
        this.enableTelegramPossibleShakingAlert = enableTelegramPossibleShakingAlert;
    }

    public Integer getTsPossibleShakingMinLevel() {
        return tsPossibleShakingMinLevel;
    }

    public void setTsPossibleShakingMinLevel(Integer tsPossibleShakingMinLevel) {
        this.tsPossibleShakingMinLevel = tsPossibleShakingMinLevel;
    }

    public Integer getTsPossibleShakingMaxDist() {
        return tsPossibleShakingMaxDist;
    }

    public void setTsPossibleShakingMaxDist(Integer tsPossibleShakingMaxDist) {
        this.tsPossibleShakingMaxDist = tsPossibleShakingMaxDist;
    }

    public Boolean getEnableTelegramStationHighIntensityAlert() {
        return enableTelegramStationHighIntensityAlert;
    }

    public void setEnableTelegramStationHighIntensityAlert(Boolean enableTelegramStationHighIntensityAlert) {
        this.enableTelegramStationHighIntensityAlert = enableTelegramStationHighIntensityAlert;
    }

    public Integer getTsStationMinIntensity1() {
        return tsStationMinIntensity1;
    }

    public void setTsStationMinIntensity1(Integer tsStationMinIntensity1) {
        this.tsStationMinIntensity1 = tsStationMinIntensity1;
    }

    public Integer getTsStationMaxDist1() {
        return tsStationMaxDist1;
    }

    public void setTsStationMaxDist1(Integer tsStationMaxDist1) {
        this.tsStationMaxDist1 = tsStationMaxDist1;
    }

    public Integer getTsStationMinIntensity2() {
        return tsStationMinIntensity2;
    }

    public void setTsStationMinIntensity2(Integer tsStationMinIntensity2) {
        this.tsStationMinIntensity2 = tsStationMinIntensity2;
    }

    public Integer getTsStationMaxDist2() {
        return tsStationMaxDist2;
    }

    public void setTsStationMaxDist2(Integer tsStationMaxDist2) {
        this.tsStationMaxDist2 = tsStationMaxDist2;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDate getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(LocalDate subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Boolean getEnableTelegramEarthquakeLocation() {
        return enableTelegramEarthquakeLocation;
    }

    public void setEnableTelegramEarthquakeLocation(Boolean enableTelegramEarthquakeLocation) {
        this.enableTelegramEarthquakeLocation = enableTelegramEarthquakeLocation;
    }

    public Boolean getEnableTelegramEarthquakeImage() {
        return enableTelegramEarthquakeImage;
    }

    public void setEnableTelegramEarthquakeImage(Boolean enableTelegramEarthquakeImage) {
        this.enableTelegramEarthquakeImage = enableTelegramEarthquakeImage;
    }

    public Boolean getEnableTelegramEarthquakeMap() {
        return enableTelegramEarthquakeMap;
    }

    public void setEnableTelegramEarthquakeMap(Boolean enableTelegramEarthquakeMap) {
        this.enableTelegramEarthquakeMap = enableTelegramEarthquakeMap;
    }

    public Boolean getEnableTelegramPossibleShakingLocation() {
        return enableTelegramPossibleShakingLocation;
    }

    public void setEnableTelegramPossibleShakingLocation(Boolean enableTelegramPossibleShakingLocation) {
        this.enableTelegramPossibleShakingLocation = enableTelegramPossibleShakingLocation;
    }

    public Boolean getEnableTelegramPossibleShakingImage() {
        return enableTelegramPossibleShakingImage;
    }

    public void setEnableTelegramPossibleShakingImage(Boolean enableTelegramPossibleShakingImage) {
        this.enableTelegramPossibleShakingImage = enableTelegramPossibleShakingImage;
    }

    public Boolean getEnableTelegramPossibleShakingMap() {
        return enableTelegramPossibleShakingMap;
    }

    public void setEnableTelegramPossibleShakingMap(Boolean enableTelegramPossibleShakingMap) {
        this.enableTelegramPossibleShakingMap = enableTelegramPossibleShakingMap;
    }

    public Boolean getEnableTelegramStationHighIntensityLocation() {
        return enableTelegramStationHighIntensityLocation;
    }

    public void setEnableTelegramStationHighIntensityLocation(Boolean enableTelegramStationHighIntensityLocation) {
        this.enableTelegramStationHighIntensityLocation = enableTelegramStationHighIntensityLocation;
    }

    public Boolean getEnableTelegramStationHighIntensityImage() {
        return enableTelegramStationHighIntensityImage;
    }

    public void setEnableTelegramStationHighIntensityImage(Boolean enableTelegramStationHighIntensityImage) {
        this.enableTelegramStationHighIntensityImage = enableTelegramStationHighIntensityImage;
    }

    public Boolean getEnableTelegramStationHighIntensityMap() {
        return enableTelegramStationHighIntensityMap;
    }

    public void setEnableTelegramStationHighIntensityMap(Boolean enableTelegramStationHighIntensityMap) {
        this.enableTelegramStationHighIntensityMap = enableTelegramStationHighIntensityMap;
    }

    public Integer getTsEarthquakeMinIntensity() {
        return tsEarthquakeMinIntensity;
    }

    public void setTsEarthquakeMinIntensity(Integer tsEarthquakeMinIntensity) {
        this.tsEarthquakeMinIntensity = tsEarthquakeMinIntensity;
    }

    public Boolean getSendImageAsAPhoto() {
        return sendImageAsAPhoto;
    }

    public void setSendImageAsAPhoto(Boolean sendImageAsAPhoto) {
        this.sendImageAsAPhoto = sendImageAsAPhoto;
    }

    public Boolean getSendMapAsAPhoto() {
        return sendMapAsAPhoto;
    }

    public void setSendMapAsAPhoto(Boolean sendMapAsAPhoto) {
        this.sendMapAsAPhoto = sendMapAsAPhoto;
    }

    public Boolean getSendGraphsAsAPhoto() {
        return sendGraphsAsAPhoto;
    }

    public void setSendGraphsAsAPhoto(Boolean sendGraphsAsAPhoto) {
        this.sendGraphsAsAPhoto = sendGraphsAsAPhoto;
    }

    public Boolean getSendPDGKStation() {
        return sendPDGKStation;
    }

    public void setSendPDGKStation(Boolean sendPDGKStation) {
        this.sendPDGKStation = sendPDGKStation;
    }

    public Boolean getSendANANStation() {
        return sendANANStation;
    }

    public void setSendANANStation(Boolean sendANANStation) {
        this.sendANANStation = sendANANStation;
    }

    public Boolean getSendTMCHStation() {
        return sendTMCHStation;
    }

    public void setSendTMCHStation(Boolean sendTMCHStation) {
        this.sendTMCHStation = sendTMCHStation;
    }

    public Boolean getShowSmallCities() {
        return showSmallCities;
    }

    public void setShowSmallCities(Boolean showSmallCities) {
        this.showSmallCities = showSmallCities;
    }

    public Boolean getShowFaults() {
        return showFaults;
    }

    public void setShowFaults(Boolean showFaults) {
        this.showFaults = showFaults;
    }
}