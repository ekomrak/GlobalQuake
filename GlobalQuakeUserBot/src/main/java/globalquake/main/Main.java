package globalquake.main;

import globalquake.client.ClientSocket;
import globalquake.client.GlobalQuakeClient;
import globalquake.core.GlobalQuake;
import globalquake.core.Settings;
import globalquake.core.exception.ApplicationErrorHandler;
import globalquake.core.exception.FatalIOException;
import globalquake.core.faults.Faults;
import globalquake.core.regions.Regions;
import globalquake.intensity.ShakeMap;
import globalquake.utils.Scale;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class Main {
    private static ApplicationErrorHandler errorHandler;
    public static final File MAIN_FOLDER = new File("./.GlobalQuakeUserBotData/");

    public static void main(String[] args) {
        initErrorHandler();
        initMainDirectory();
        GlobalQuake.prepare(MAIN_FOLDER, getErrorHandler());

        try {
            Regions.init();
            Faults.init();
            Scale.load();
            ShakeMap.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClientSocket client = new ClientSocket();
        try {
            new GlobalQuakeClient(client).init();
            client.connect(Settings.lastServerIP, Settings.lastServerPORT);
            client.runReconnectService();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initMainDirectory() {
        if (!MAIN_FOLDER.exists() && (!MAIN_FOLDER.mkdirs())) {
            getErrorHandler().handleException(new FatalIOException("Unable to create main directory!", null));
        }
        try {
            Path exportPath = Paths.get(new File(MAIN_FOLDER, "templates/").getAbsolutePath());
            if (!Files.exists(exportPath)) {
                Files.createDirectory(exportPath);
            }
            Path exportedFilePath = exportPath.resolve("earthquakes_template.xlsx");
            if (!Files.exists(exportedFilePath)) {
                InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("templates/earthquakes_template.xlsx");
                if (is != null) {
                    Files.copy(is, exportedFilePath, StandardCopyOption.REPLACE_EXISTING);
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ApplicationErrorHandler getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = new ApplicationErrorHandler(null, false);
        }
        return errorHandler;
    }

    public static void initErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler(getErrorHandler());
    }
}
