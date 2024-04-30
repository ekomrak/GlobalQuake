package globalquake.telegram.util;

import globalquake.core.GlobalQuake;
import globalquake.db.DatabaseService;
import org.jxls.builder.JxlsOutput;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DataExcelGenerator {
    public static InputStream generateExcel(DatabaseService databaseService) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("earthquakes", databaseService.listAllEarthquakes());

        JxlsOutput output = new JxlsByteArrayOutput();
        JxlsPoiTemplateFillerBuilder.newInstance()
                .withTemplate(GlobalQuake.mainFolder + "/templates/earthquakes_template.xlsx")
                .build()
                .fill(data, output);

        return new ByteArrayInputStream(((ByteArrayOutputStream)output.getOutputStream()).toByteArray());
    }
}
