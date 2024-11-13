package org.httpserver.utils;

import org.httpserver.core.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseUtils {

    public static void sendResponse(HttpExchange exchange, int status, String response) {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(status, response.length());
            os.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
