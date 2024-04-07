package main;


import com.sun.net.httpserver.HttpServer;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.fileutil.PathUtil;
import edu.fdu.se.net.*;

import java.net.InetSocketAddress;
import java.util.logging.Level;

/**
 * Created by huangkaifeng on 2018/8/23.
 *
 * html api for CLDIFF. Only works when CLDIFF-WEB and CommitCrawler is on.
 *
 */
public class CLDIFFServerOnline {

    public static void main(String[] arg) throws Exception {
        Global.runningMode = Constants.ONLINE;
        AServer.initCLDIFFGlobals();
        Global.outputDir = PathUtil.unifyPathSeparator(arg[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress(12007), 0);
        server.createContext("/Diff/genCache", new CacheGeneratorHandler());
        server.createContext("/Diff/fetchMeta", new FetchMetaCacheHandler());
        server.createContext("/Diff/fetchFile", new FetchFileContentHandler());
        server.createContext("/Diff/clearCommitRecord", new ClearCacheHandler());
        Global.logger.info("Started");
        server.start();
    }


}
