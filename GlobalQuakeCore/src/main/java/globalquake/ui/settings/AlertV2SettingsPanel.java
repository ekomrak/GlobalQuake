package globalquake.ui.settings;

import globalquake.core.Settings;

import javax.swing.*;
import java.awt.*;


public class AlertV2SettingsPanel extends SettingsPanel {
    private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
    private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);

    private JCheckBox enableLimitedStations;
    private JCheckBox enableLimitedEarthquakes;
    private JCheckBox enableLimitedArchivedEarthquakes;
    private JTextField stationsLoadDist;

    private JTextField telegramBotToken;
    private JTextField telegramBotUsername;
    private JTextField telegramChatId;
    private JTextField telegramMessageThreadId;
    private JTextField telegramCreatorId;

    private JCheckBox enableTelegramEarthquakeAlert;
    private JCheckBox enableSpeechEarthquakeAlert;
    //private JCheckBox enableSoundEarthquakeAlert;
    private JCheckBox enableTelegramEarthquakeLocation;
    private JCheckBox enableTelegramEarthquakeImage;
    private JCheckBox enableTelegramEarthquakeMap;
    private JTextField tsEarthquakeMinMagnitudeArea1;
    private JTextField tsEarthquakeMaxDistArea1;
    private JTextField tsEarthquakeMinMagnitudeArea2;
    private JTextField tsEarthquakeMaxDistArea2;
    private IntensityScaleSelector tsEarthquakeMinIntensity;

    private JCheckBox enableTelegramPossibleShakingAlert;
    private JCheckBox enableSpeechPossibleShakingAlert;
    //private JCheckBox enableSoundPossibleShakingAlert;
    private JCheckBox enableTelegramPossibleShakingLocation;
    private JCheckBox enableTelegramPossibleShakingImage;
    private JCheckBox enableTelegramPossibleShakingMap;
    private JTextField tsPossibleShakingMinLevel;
    private JTextField tsPossibleShakingMaxDist;

    private JCheckBox enableTelegramStationHighIntensityAlert;
    private JCheckBox enableSpeechStationHighIntensityAlert;
    //private JCheckBox enableSoundStationHighIntensityAlert;
    private JCheckBox enableTelegramStationHighIntensityLocation;
    private JCheckBox enableTelegramStationHighIntensityImage;
    private JCheckBox enableTelegramStationHighIntensityMap;
    private JTextField tsStationMinIntensity1;
    private JTextField tsStationMaxDist1;
    private JTextField tsStationMinIntensity2;
    private JTextField tsStationMaxDist2;

    private JCheckBox useFreeTts;

    public AlertV2SettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createGeneralSettings();
        createTelegramBotSettings();
        createTSEarthquakeAlertSettings();
        createTSPossibleShakingAlertSettings();
        createTSStationHighIntensityAlertSettings();
        createSpeechServiceSettings();
    }

    private void createGeneralSettings() {
        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(BorderFactory.createTitledBorder("General Settings"));

        enableLimitedStations = new JCheckBox("Limit stations loading", Settings.enableLimitedStations);
        generalPanel.add(enableLimitedStations, createGbcOneLine(0));

        generalPanel.add(new JLabel("Stations load max distance: ", SwingConstants.LEFT), createGbc(0, 1));
        stationsLoadDist = new JTextField(String.format("%s", Settings.stationsLoadDist));
        generalPanel.add(stationsLoadDist, createGbc(1, 1));

        enableLimitedEarthquakes = new JCheckBox("Limit earthquake box display", Settings.enableLimitedEarthquakes);
        generalPanel.add(enableLimitedEarthquakes, createGbcOneLine(2));

        enableLimitedArchivedEarthquakes = new JCheckBox("Limit earthquake archive panel", Settings.enableLimitedArchivedEarthquakes);
        generalPanel.add(enableLimitedArchivedEarthquakes, createGbcOneLine(3));

        add(generalPanel);
    }

    private void createTelegramBotSettings() {
        JPanel telegramBotPanel = new JPanel(new GridBagLayout());
        telegramBotPanel.setBorder(BorderFactory.createTitledBorder("Telegram Bot Settings"));

        telegramBotPanel.add(new JLabel("Telegram Bot Token: ", SwingConstants.LEFT), createGbc(0, 0));
        telegramBotToken = new JTextField(String.format("%s", Settings.telegramBotToken));
        telegramBotPanel.add(telegramBotToken, createGbc(1, 0));

        telegramBotPanel.add(new JLabel("Telegram Bot Username: ", SwingConstants.LEFT), createGbc(0, 1));
        telegramBotUsername = new JTextField(String.format("%s", Settings.telegramBotUsername));
        telegramBotPanel.add(telegramBotUsername, createGbc(1, 1));

        JTextArea warning = new JTextArea("Warning: Restart of the application is required after the token or username update!");
        warning.setLineWrap(true);
        warning.setEditable(false);
        warning.setBackground(telegramBotPanel.getBackground());
        telegramBotPanel.add(warning, createGbcOneLine(2));

        telegramBotPanel.add(new JLabel("Telegram Chat Id: ", SwingConstants.LEFT), createGbc(0, 3));
        telegramChatId = new JTextField(String.format("%s", Settings.telegramChatId));
        telegramBotPanel.add(telegramChatId, createGbc(1, 3));

        telegramBotPanel.add(new JLabel("Telegram Message Thread Id: ", SwingConstants.LEFT), createGbc(0, 4));
        telegramMessageThreadId = new JTextField(String.format("%s", Settings.telegramMessageThreadId));
        telegramBotPanel.add(telegramMessageThreadId, createGbc(1, 4));

        telegramBotPanel.add(new JLabel("Telegram Super Admin Id: ", SwingConstants.LEFT), createGbc(0, 5));
        telegramCreatorId = new JTextField(String.format("%s", Settings.telegramCreatorId));
        telegramBotPanel.add(telegramCreatorId, createGbc(1, 5));

        add(telegramBotPanel);
    }

    private void createTSEarthquakeAlertSettings() {
        JPanel tsEarthquakeAlertPanel = new JPanel(new GridBagLayout());
        tsEarthquakeAlertPanel.setBorder(BorderFactory.createTitledBorder("Earthquake Alert Settings"));

        enableTelegramEarthquakeAlert = new JCheckBox("Enable earthquake alert by Telegram", Settings.enableTelegramEarthquakeAlert);
        tsEarthquakeAlertPanel.add(enableTelegramEarthquakeAlert, createGbcOneLine(0));

        enableSpeechEarthquakeAlert = new JCheckBox("Enable earthquake alert by Speech", Settings.enableSpeechEarthquakeAlert);
        tsEarthquakeAlertPanel.add(enableSpeechEarthquakeAlert, createGbcOneLine(1));

        /*enableSoundEarthquakeAlert = new JCheckBox("Enable earthquake alert by Sound", Settings.enableSoundEarthquakeAlert);
        tsEarthquakeAlertPanel.add(enableSoundEarthquakeAlert, createGbcOneLine(2));*/

        enableTelegramEarthquakeLocation = new JCheckBox("Send earthquake location", Settings.enableTelegramEarthquakeLocation);
        tsEarthquakeAlertPanel.add(enableTelegramEarthquakeLocation, createGbcOneLine(2));

        enableTelegramEarthquakeImage = new JCheckBox("Send earthquake image", Settings.enableTelegramEarthquakeImage);
        tsEarthquakeAlertPanel.add(enableTelegramEarthquakeImage, createGbcOneLine(3));

        enableTelegramEarthquakeMap = new JCheckBox("Send earthquake map", Settings.enableTelegramEarthquakeMap);
        tsEarthquakeAlertPanel.add(enableTelegramEarthquakeMap, createGbcOneLine(4));

        JPanel area1Panel = new JPanel(new GridBagLayout());
        area1Panel.setBorder(BorderFactory.createTitledBorder("Area 1"));

        area1Panel.add(new JLabel("Earthquake alert min magnitude: ", SwingConstants.LEFT), createGbc(0, 0));
        tsEarthquakeMinMagnitudeArea1 = new JTextField(String.format("%s", Settings.tsEarthquakeMinMagnitudeArea1));
        area1Panel.add(tsEarthquakeMinMagnitudeArea1, createGbc(1, 0));

        area1Panel.add(new JLabel("Earthquake alert max distance: ", SwingConstants.LEFT), createGbc(0, 1));
        tsEarthquakeMaxDistArea1 = new JTextField(String.format("%s", Settings.tsEarthquakeMaxDistArea1));
        area1Panel.add(tsEarthquakeMaxDistArea1, createGbc(1, 1));

        tsEarthquakeAlertPanel.add(area1Panel, createGbcOneLine(5));

        JPanel area2Panel = new JPanel(new GridBagLayout());
        area2Panel.setBorder(BorderFactory.createTitledBorder("Area 2"));

        area2Panel.add(new JLabel("Earthquake alert min magnitude: ", SwingConstants.LEFT), createGbc(0, 0));
        tsEarthquakeMinMagnitudeArea2 = new JTextField(String.format("%s", Settings.tsEarthquakeMinMagnitudeArea2));
        area2Panel.add(tsEarthquakeMinMagnitudeArea2, createGbc(1, 0));

        area2Panel.add(new JLabel("Earthquake alert max distance: ", SwingConstants.LEFT), createGbc(0, 1));
        tsEarthquakeMaxDistArea2 = new JTextField(String.format("%s", Settings.tsEarthquakeMaxDistArea2));
        area2Panel.add(tsEarthquakeMaxDistArea2, createGbc(1, 1));

        tsEarthquakeAlertPanel.add(area2Panel, createGbcOneLine(6));

        tsEarthquakeMinIntensity = new IntensityScaleSelector("Earthquake alert min intensity:", Settings.tsEarthquakeIntensityScale, Settings.tsEarthquakeMinIntensity);
        tsEarthquakeAlertPanel.add(tsEarthquakeMinIntensity, createGbc(1, 7));

        add(tsEarthquakeAlertPanel);
    }

    private void createTSPossibleShakingAlertSettings() {
        JPanel tsPossibleShakingAlertPanel = new JPanel(new GridBagLayout());
        tsPossibleShakingAlertPanel.setBorder(BorderFactory.createTitledBorder("Possible Shaking Alert Settings"));

        enableTelegramPossibleShakingAlert = new JCheckBox("Enable possible shaking alert by Telegram", Settings.enableTelegramPossibleShakingAlert);
        tsPossibleShakingAlertPanel.add(enableTelegramPossibleShakingAlert, createGbcOneLine(0));

        enableSpeechPossibleShakingAlert = new JCheckBox("Enable possible shaking alert by Speech", Settings.enableSpeechPossibleShakingAlert);
        tsPossibleShakingAlertPanel.add(enableSpeechPossibleShakingAlert, createGbcOneLine(1));

        /*enableSoundPossibleShakingAlert = new JCheckBox("Enable possible shaking alert by Sound", Settings.enableSoundPossibleShakingAlert);
        tsPossibleShakingAlertPanel.add(enableSoundPossibleShakingAlert, createGbcOneLine(2));*/

        enableTelegramPossibleShakingLocation = new JCheckBox("Send earthquake location", Settings.enableTelegramPossibleShakingLocation);
        tsPossibleShakingAlertPanel.add(enableTelegramPossibleShakingLocation, createGbcOneLine(2));

        enableTelegramPossibleShakingImage = new JCheckBox("Send earthquake image", Settings.enableTelegramPossibleShakingImage);
        tsPossibleShakingAlertPanel.add(enableTelegramPossibleShakingImage, createGbcOneLine(3));

        enableTelegramPossibleShakingMap = new JCheckBox("Send earthquake map", Settings.enableTelegramPossibleShakingMap);
        tsPossibleShakingAlertPanel.add(enableTelegramPossibleShakingMap, createGbcOneLine(4));

        tsPossibleShakingAlertPanel.add(new JLabel("Possible Shaking alert min level: ", SwingConstants.LEFT), createGbc(0, 5));
        tsPossibleShakingMinLevel = new JTextField(String.format("%s", Settings.tsPossibleShakingMinLevel));
        tsPossibleShakingAlertPanel.add(tsPossibleShakingMinLevel, createGbc(1, 5));

        tsPossibleShakingAlertPanel.add(new JLabel("Possible Shaking alert max distance: ", SwingConstants.LEFT), createGbc(0, 6));
        tsPossibleShakingMaxDist = new JTextField(String.format("%s", Settings.tsPossibleShakingMaxDist));
        tsPossibleShakingAlertPanel.add(tsPossibleShakingMaxDist, createGbc(1, 6));

        add(tsPossibleShakingAlertPanel);

    }

    private void createTSStationHighIntensityAlertSettings() {
        JPanel tsStationHighIntensityAlertPanel = new JPanel(new GridBagLayout());
        tsStationHighIntensityAlertPanel.setBorder(BorderFactory.createTitledBorder("Station High Intensity Alert Settings"));

        enableTelegramStationHighIntensityAlert = new JCheckBox("Enable station high intensity alert by Telegram", Settings.enableTelegramStationHighIntensityAlert);
        tsStationHighIntensityAlertPanel.add(enableTelegramStationHighIntensityAlert, createGbcOneLine(0));

        enableSpeechStationHighIntensityAlert = new JCheckBox("Enable station high intensity alert by Speech", Settings.enableSpeechStationHighIntensityAlert);
        tsStationHighIntensityAlertPanel.add(enableSpeechStationHighIntensityAlert, createGbcOneLine(1));

        /*enableSoundStationHighIntensityAlert = new JCheckBox("Enable station high intensity alert by Sound", Settings.enableSoundStationHighIntensityAlert);
        tsStationHighIntensityAlertPanel.add(enableSoundStationHighIntensityAlert, createGbcOneLine(2));*/

        enableTelegramStationHighIntensityLocation = new JCheckBox("Send earthquake location", Settings.enableTelegramStationHighIntensityLocation);
        tsStationHighIntensityAlertPanel.add(enableTelegramStationHighIntensityLocation, createGbcOneLine(2));

        enableTelegramStationHighIntensityImage = new JCheckBox("Send earthquake image", Settings.enableTelegramStationHighIntensityImage);
        tsStationHighIntensityAlertPanel.add(enableTelegramStationHighIntensityImage, createGbcOneLine(3));

        enableTelegramStationHighIntensityMap = new JCheckBox("Send earthquake map", Settings.enableTelegramStationHighIntensityMap);
        tsStationHighIntensityAlertPanel.add(enableTelegramStationHighIntensityMap, createGbcOneLine(4));

        JPanel area1Panel = new JPanel(new GridBagLayout());
        area1Panel.setBorder(BorderFactory.createTitledBorder("Area 1"));

        area1Panel.add(new JLabel("Station alert min intensity: ", SwingConstants.LEFT), createGbc(0, 0));
        tsStationMinIntensity1 = new JTextField(String.format("%s", Settings.tsStationMinIntensity1));
        area1Panel.add(tsStationMinIntensity1, createGbc(1, 0));

        area1Panel.add(new JLabel("Station alert max distance: ", SwingConstants.LEFT), createGbc(0, 1));
        tsStationMaxDist1 = new JTextField(String.format("%s", Settings.tsStationMaxDist1));
        area1Panel.add(tsStationMaxDist1, createGbc(1, 1));

        tsStationHighIntensityAlertPanel.add(area1Panel, createGbcOneLine(5));

        JPanel area2Panel = new JPanel(new GridBagLayout());
        area2Panel.setBorder(BorderFactory.createTitledBorder("Area 2"));

        area2Panel.add(new JLabel("Station alert min intensity: ", SwingConstants.LEFT), createGbc(0, 0));
        tsStationMinIntensity2 = new JTextField(String.format("%s", Settings.tsStationMinIntensity2));
        area2Panel.add(tsStationMinIntensity2, createGbc(1, 0));

        area2Panel.add(new JLabel("Station alert max distance: ", SwingConstants.LEFT), createGbc(0, 1));
        tsStationMaxDist2 = new JTextField(String.format("%s", Settings.tsStationMaxDist2));
        area2Panel.add(tsStationMaxDist2, createGbc(1, 1));

        tsStationHighIntensityAlertPanel.add(area2Panel, createGbcOneLine(6));

        add(tsStationHighIntensityAlertPanel);
    }

    public void createSpeechServiceSettings() {
        JPanel speechServiceAlertPanel = new JPanel(new GridBagLayout());
        speechServiceAlertPanel.setBorder(BorderFactory.createTitledBorder("Speech Service Settings"));

        useFreeTts = new JCheckBox("Use FreeTTS (free) instead of Google Text-to-Speech (paid)", Settings.useFreeTts);
        speechServiceAlertPanel.add(useFreeTts, createGbcOneLine(0));

        add(speechServiceAlertPanel);
    }

    @Override
    public void save() throws NumberFormatException {
        Settings.stationsLoadDist = parseDouble(stationsLoadDist.getText(), "Stations load max distance", 0, 30000);
        Settings.enableLimitedStations = enableLimitedStations.isSelected();
        Settings.enableLimitedEarthquakes = enableLimitedEarthquakes.isSelected();
        Settings.enableLimitedArchivedEarthquakes = enableLimitedArchivedEarthquakes.isSelected();

        Settings.telegramBotToken = telegramBotToken.getText();
        Settings.telegramBotUsername = telegramBotUsername.getText();
        Settings.telegramChatId = telegramChatId.getText();
        Settings.telegramMessageThreadId = parseInt(telegramMessageThreadId.getText(), "Telegram message thread id", 0, Integer.MAX_VALUE);
        Settings.telegramCreatorId = parseInt(telegramCreatorId.getText(), "Telegram super admin id", 0, Integer.MAX_VALUE);

        Settings.enableTelegramEarthquakeAlert = enableTelegramEarthquakeAlert.isSelected();
        Settings.enableSpeechEarthquakeAlert = enableSpeechEarthquakeAlert.isSelected();
        //Settings.enableSoundEarthquakeAlert = enableSoundEarthquakeAlert.isSelected();
        Settings.enableTelegramEarthquakeLocation = enableTelegramEarthquakeLocation.isSelected();
        Settings.enableTelegramEarthquakeImage = enableTelegramEarthquakeImage.isSelected();
        Settings.enableTelegramEarthquakeMap = enableTelegramEarthquakeMap.isSelected();
        Settings.tsEarthquakeMinMagnitudeArea1 = parseDouble(tsEarthquakeMinMagnitudeArea1.getText(), "Earthquake alert min magnitude", 0, 10);
        Settings.tsEarthquakeMaxDistArea1 = parseDouble(tsEarthquakeMaxDistArea1.getText(), "Earthquake alert max distance", 0, 30000);
        Settings.tsEarthquakeMinMagnitudeArea2 = parseDouble(tsEarthquakeMinMagnitudeArea2.getText(), "Earthquake alert min magnitude", 0, 10);
        Settings.tsEarthquakeMaxDistArea2 = parseDouble(tsEarthquakeMaxDistArea2.getText(), "Earthquake alert max distance", 0, 30000);
        Settings.tsEarthquakeIntensityScale = tsEarthquakeMinIntensity.getShakingScaleComboBox().getSelectedIndex();
        Settings.tsEarthquakeMinIntensity = tsEarthquakeMinIntensity.getLevelComboBox().getSelectedIndex();

        Settings.enableTelegramPossibleShakingAlert = enableTelegramPossibleShakingAlert.isSelected();
        Settings.enableSpeechPossibleShakingAlert = enableSpeechPossibleShakingAlert.isSelected();
        //Settings.enableSoundPossibleShakingAlert = enableSoundPossibleShakingAlert.isSelected();
        Settings.enableTelegramPossibleShakingLocation = enableTelegramPossibleShakingLocation.isSelected();
        Settings.enableTelegramPossibleShakingImage = enableTelegramPossibleShakingImage.isSelected();
        Settings.enableTelegramPossibleShakingMap = enableTelegramPossibleShakingMap.isSelected();
        Settings.tsPossibleShakingMinLevel = parseInt(tsPossibleShakingMinLevel.getText(), "Possible shaking alert min level", 0, 4);
        Settings.tsPossibleShakingMaxDist = parseDouble(tsPossibleShakingMaxDist.getText(), "Possible shaking alert max distance", 0, 30000);

        Settings.enableTelegramStationHighIntensityAlert = enableTelegramStationHighIntensityAlert.isSelected();
        Settings.enableSpeechStationHighIntensityAlert = enableSpeechStationHighIntensityAlert.isSelected();
        //Settings.enableSoundStationHighIntensityAlert = enableSoundStationHighIntensityAlert.isSelected();
        Settings.enableTelegramStationHighIntensityLocation = enableTelegramStationHighIntensityLocation.isSelected();
        Settings.enableTelegramStationHighIntensityImage = enableTelegramStationHighIntensityImage.isSelected();
        Settings.enableTelegramStationHighIntensityMap = enableTelegramStationHighIntensityMap.isSelected();
        Settings.tsStationMinIntensity1 = parseDouble(tsStationMinIntensity1.getText(), "Station alert min intensity", 0, Double.MAX_VALUE);
        Settings.tsStationMaxDist1 = parseDouble(tsStationMaxDist1.getText(), "Station alert max distance", 0, 30000);
        Settings.tsStationMinIntensity2 = parseDouble(tsStationMinIntensity2.getText(), "Station alert min intensity", 0, Double.MAX_VALUE);
        Settings.tsStationMaxDist2 = parseDouble(tsStationMaxDist2.getText(), "Station alert max distance", 0, 30000);

        Settings.useFreeTts = useFreeTts.isSelected();
    }

    @Override
    public String getTitle() {
        return "Alert V2";
    }

    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.fill = (x == 0) ? GridBagConstraints.BOTH
                : GridBagConstraints.HORIZONTAL;

        gbc.insets = (x == 0) ? WEST_INSETS : EAST_INSETS;
        gbc.weightx = (x == 0) ? 0.1 : 1.0;
        gbc.weighty = 1.0;
        return gbc;
    }

    private GridBagConstraints createGbcOneLine(int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.insets = WEST_INSETS;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        return gbc;
    }
}
