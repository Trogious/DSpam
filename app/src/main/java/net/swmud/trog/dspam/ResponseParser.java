package net.swmud.trog.dspam;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 24.04.16.
 */
public class ResponseParser {
    private static final String HEADER_SEPARATOR_STR = "\r\n";
    private static final int HEADER_SEPARATOR_LEN = HEADER_SEPARATOR_STR.length();

    private static final String CONTENT_LENGTH_STR = "Content-Length: ";
    private static final int CONTENT_LENGTH_LEN = CONTENT_LENGTH_STR.length();

    private static final String CONTENT_TYPE_STR = "Content-Type: ";
    private static final int CONTENT_TYPE_LEN = CONTENT_TYPE_STR.length();

    private static final Map<String, Integer> headerMap = new HashMap<>();

    static {
        headerMap.put(CONTENT_LENGTH_STR, CONTENT_LENGTH_LEN);
        headerMap.put(CONTENT_TYPE_STR, CONTENT_TYPE_LEN);
    }

    private StringBuilder sb = new StringBuilder();
    private int headerLength = 0;
    private int contentLength = 0;
    private int responseLength = 0;
    private int bodyStartIdx;
    private boolean contentLengthFound = false;
    private boolean headerEndFound;
    private boolean requestComplete;
    private String body;


    ResponseParser() {
    }

    public void parse(char[] requestFragment, int bytesRead) {
        if (!requestComplete && bytesRead > 0) {
            sb.append(requestFragment, 0, bytesRead);

            if (!headerEndFound) {
                for (Map.Entry<String, Integer> header : headerMap.entrySet()) {
                    int idx = sb.indexOf(header.getKey());
                    if (idx >= 0) {
                        int idxEnd = sb.indexOf(HEADER_SEPARATOR_STR, idx);
                        if (idxEnd >= 0) {
                            String headerValue = sb.substring(idx + header.getValue(), idxEnd);
                            responseLength += headerValue.length() + header.getValue() + HEADER_SEPARATOR_LEN;
                            Log.e(header.getKey(), headerValue);
                            if (CONTENT_LENGTH_STR.equals(header.getKey())) {
                                contentLength = Integer.parseInt(headerValue);
                                responseLength += contentLength;
                            }
                        }
                    }

                }

                int headerEndIdx = sb.indexOf(HEADER_SEPARATOR_STR + HEADER_SEPARATOR_STR);
                if (headerEndIdx >= 0) {
                    headerEndFound = true;
                    bodyStartIdx = headerEndIdx + HEADER_SEPARATOR_LEN + HEADER_SEPARATOR_LEN;
                    responseLength += HEADER_SEPARATOR_LEN;
                    Log.e("responseLength is: ", "" + responseLength);
                    headerLength = responseLength - contentLength;
                    Log.e("headersEnd", "found");
                }
            }

            if (sb.length() >= responseLength) {
                requestComplete = true;
                sb.delete(0, bodyStartIdx);
                body = sb.toString();
            }
        }

    }

    public int getHeaderLength() {
        return headerLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public int getResponseLength() {
        return responseLength;
    }

    public boolean isRequestComplete() {
        return requestComplete;
    }

    public String getBody() {
        return body;
    }
}
