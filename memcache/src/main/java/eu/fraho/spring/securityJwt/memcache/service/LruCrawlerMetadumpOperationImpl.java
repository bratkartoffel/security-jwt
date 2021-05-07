package eu.fraho.spring.securityJwt.memcache.service;

import eu.fraho.spring.securityJwt.memcache.dto.LruMetadumpEntry;
import lombok.extern.slf4j.Slf4j;
import net.spy.memcached.ops.OperationErrorType;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.StatusCode;
import net.spy.memcached.protocol.BaseOperationImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class LruCrawlerMetadumpOperationImpl extends BaseOperationImpl {
    private static final OperationStatus END = new OperationStatus(true, "END", StatusCode.SUCCESS);
    private static final String CHARSET = "UTF-8";

    private final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    private final LruCrawlerMetadumpOperation.Callback cb;
    private final byte[] msg;
    private byte[] errorMsg;

    public LruCrawlerMetadumpOperationImpl(String arg, LruCrawlerMetadumpOperation.Callback cb) {
        super();
        super.setCallback(cb);
        this.cb = cb;
        this.msg = ("lru_crawler metadump " + arg + "\r\n").getBytes();
    }

    private OperationErrorType classifyError(String line) {
        OperationErrorType rv = null;
        if (line.startsWith("ERROR")) {
            rv = OperationErrorType.GENERAL;
        } else if (line.startsWith("CLIENT_ERROR")) {
            rv = OperationErrorType.CLIENT;
        } else if (line.startsWith("SERVER_ERROR")) {
            rv = OperationErrorType.SERVER;
        }
        return rv;
    }

    @Override
    public void readFromBuffer(ByteBuffer data) throws IOException {
        // Loop while there's data remaining to get it all drained.
        while (getState() != OperationState.COMPLETE && data.remaining() > 0) {
            int offset = -1;
            for (int i = 0; data.remaining() > 0; i++) {
                byte b = data.get();
                if (b == '\n') {
                    offset = i;
                    break;
                } else if (b != '\r') {
                    byteBuffer.write(b);
                }
            }
            if (offset >= 0) {
                String line = byteBuffer.toString(CHARSET);
                byteBuffer.reset();
                OperationErrorType eType = classifyError(line);
                if (eType != null) {
                    errorMsg = line.getBytes();
                    handleError(eType, line);
                } else {
                    handleLine(line);
                }
            }
        }
    }

    public void handleLine(String line) {
        if (line.equals("END")) {
            cb.receivedStatus(END);
            transitionState(OperationState.COMPLETE);
            return;
        }

        String[] parts = line.split(" ", 7);
        String key = null;
        Integer exp = null;
        Integer la = null;
        Integer cas = null;
        Boolean fetch = null;
        Integer cls = null;
        Integer size = null;
        for (String part : parts) {
            String[] kv = part.split("=", 2);

            switch (kv[0]) {
                case "key":
                    try {
                        key = URLDecoder.decode(kv[1], CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Unsupported encoding: {}", e.getMessage(), e);
                        //noinspection deprecation
                        key = URLDecoder.decode(kv[1]);
                    }
                    break;
                case "exp":
                    exp = Integer.parseInt(kv[1]);
                    break;
                case "la":
                    la = Integer.parseInt(kv[1]);
                    break;
                case "cas":
                    cas = Integer.parseInt(kv[1]);
                    break;
                case "fetch":
                    fetch = Objects.equals("yes", kv[1]);
                    break;
                case "cls":
                    cls = Integer.parseInt(kv[1]);
                    break;
                case "size":
                    size = Integer.parseInt(kv[1]);
                    break;
            }
        }

        cb.gotMetadump(LruMetadumpEntry.builder()
                .key(key).exp(exp).la(la).cas(cas)
                .fetch(fetch).cls(cls).size(size)
                .build());
    }

    @Override
    public byte[] getErrorMsg() {
        return errorMsg;
    }

    @Override
    public void initialize() {
        setBuffer(ByteBuffer.wrap(msg));
    }

    @Override
    protected void wasCancelled() {
        cb.receivedStatus(CANCELLED);
    }

    @Override
    public String toString() {
        return "Cmd: " + Arrays.toString(msg);
    }
}
