package edu.fdu.se.net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClearCacheHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        AServer.clearCache(exchange);
    }
}
