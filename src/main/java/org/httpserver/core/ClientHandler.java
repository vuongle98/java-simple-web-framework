package org.httpserver.core;

import org.simpleframework.utils.ResponseUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Router router;

    public ClientHandler(Socket socket, Router router) {
        this.clientSocket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try (InputStream is = clientSocket.getInputStream()) {
            OutputStream os = clientSocket.getOutputStream();

            // Parse request line
            String requestLine = readLine(is);
            String[] requestParts = requestLine.split(" ");

            // TODO: Postman will send Option method, will break this?
            String method = requestParts[0];
            String[] pathAndQuery = requestParts[1].split("\\?");
            String path = pathAndQuery[0];

            // Parse query parameters
            Map<String, String> queryParams = new HashMap<>();
            if (pathAndQuery.length > 1) {
                for (String param : pathAndQuery[1].split("&")) {
                    String[] kv = param.split("=");
                    queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
                }
            }

            // Parse headers
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while (!(headerLine = readLine(is)).isEmpty()) {
                String[] headerParts = headerLine.split(": ");
                headers.put(headerParts[0], headerParts[1]);
            }

            // create HttpExchange
            HttpExchange exchange = new HttpExchange(method, path, headers, queryParams, is, os);

            HttpContext context = router.findContext(path);

            if (context != null) {
                context.handler().accept(exchange);
            } else {
                ResponseUtils.sendResponse(exchange, 404, "Not found");
            }

            exchange.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readLine(InputStream inputStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            if (ch == '\n') break;
            if (ch != '\r') sb.append((char) ch);
        }
        return sb.toString();
    }
}
