package main;


import com.sun.net.httpserver.HttpServer;
import edu.fdu.se.net.*;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.fileutil.PathUtil;

import java.net.InetSocketAddress;

/**
 * Created by huangkaifeng on 2018/8/23.
 *
 * html api for CLDIFF.
 * Deploy CLDIFF-WEB and run CLDIFFServerOffline and the web visualization should be ready.
 *
 */
public class CLDIFFServerOffline {


    public static void main(String[] arg) throws Exception {
        Global.runningMode = Constants.OFFLINE;
        AServer.initCLDIFFGlobals();
        Global.outputDir = PathUtil.unifyPathSeparator(arg[0]);
        if (Constants.OFFLINE == Global.runningMode) {
            Global.prevCommitID = PathUtil.unifyPathSeparator(arg[1]); // XXX/.git
            String[] data = Global.prevCommitID.split("/");
            Global.projectName = data[data.length - 2];
        } else if (Constants.EVALUATION == Global.runningMode) {
            //none
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        //传meta文件，如果没有meta，则调用生成
        server.createContext("/fetchMeta", new FetchMetaCacheHandler());
        server.createContext("/fetchFile", new FetchFileContentHandler());
        server.createContext("/clearCommitRecord",new ClearCacheHandler());
        server.start();
    }





}
