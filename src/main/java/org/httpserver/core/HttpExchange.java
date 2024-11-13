package org.httpserver.core;

import org.httpserver.utils.RequestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

public class HttpExchange {

    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final InputStream requestBody;
    private final OutputStream responseBody;

    private int statusCode = 200;
    private String contentType = "application/json; charset=UTF-8";


    public HttpExchange(String method, String path, Map<String, String> headers,
                        Map<String, String> queryParams, InputStream requestBody,
                        OutputStream responseBody) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams != null ?
                Map.copyOf(queryParams) : Collections.emptyMap();
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getQuery() {
        return RequestUtils.toQueryParams(queryParams);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }

    public InputStream getRequestBody() {
        return requestBody;
    }

    public OutputStream getResponseBody() {
        return responseBody;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void sendResponseHeaders(int statusCode, int contentLength) throws IOException {
        sendResponseHeaders(statusCode, contentLength, contentType);
    }

    public void sendResponseHeaders(int statusCode, int contentLength, String contentType) throws IOException {
        this.statusCode = statusCode;
        this.contentType = contentType;

        // Start building the response headers
        StringBuilder response = new StringBuilder();

        // HTTP Status Line (e.g., "HTTP/1.1 200 OK")
        response.append("HTTP/1.1 ").append(statusCode).append("\r\n");

        // Set default headers or add custom ones
        headers.putIfAbsent("Content-Type", contentType); // Default content type
        headers.putIfAbsent("Content-Length", String.valueOf(contentLength)); // Set Content-Length header
        headers.putIfAbsent("Connection", "close");  // Default to closing connection after response

        // Add custom headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            response.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // End of headers
        response.append("\r\n");

        // Write the headers to the output stream
        responseBody.write(response.toString().getBytes());

        responseBody.flush();
    }

    public void close() throws Exception {
        responseBody.close();
    }

}
