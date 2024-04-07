package edu.fdu.se.net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 获取文件内容 link diff
 */
public class FetchFileContentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        AServer.fetchFileContent(exchange);
    }
}
