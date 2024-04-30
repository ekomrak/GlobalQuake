package globalquake.telegram.util;

import org.jxls.builder.JxlsOutput;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class JxlsByteArrayOutput implements JxlsOutput {
    ByteArrayOutputStream originOut = new ByteArrayOutputStream();

    @Override
    public OutputStream getOutputStream() {
        return originOut;
    }
}
