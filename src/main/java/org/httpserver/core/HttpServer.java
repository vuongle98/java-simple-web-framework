package org.httpserver.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HttpServer {
    private final int port;
    private final Router router;
    private final ExecutorService executorService;

    public HttpServer(int port, int threadPoolSize) {
        this.port = port;
        this.router = new Router();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public HttpContext createContext(String path, Consumer<HttpExchange> handler) {
        if (handler == null || path == null) {
            throw new NullPointerException("null handler, or path parameter");
        }
        HttpContext context = new HttpContext(path, handler);
        router.addContext(context);
        return context;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Server started on port: " + port);

            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();

                executorService.execute(new ClientHandler(socket, router));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdownExecutorService();
        }
    }

    private void shutdownExecutorService() {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();

                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.out.println("Execution did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
