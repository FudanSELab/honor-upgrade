package edu.fdu.se.net;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.fdu.se.API.CLDiffAPI;
import edu.fdu.se.fileutil.ResponseTextToFiles;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import main.CLDIFFServerOnline;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CacheGeneratorHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        Global.logger.info("CacheHandler");
        OutputStream os = exchange.getResponseBody();
        try {
            InputStream is = exchange.getRequestBody();
            String postString = MyNetUtil.getPostString(is);
            //保存为文件
            String[] data = postString.split(Constants.ONLINE_DIVIDER);
            if (data.length <= 1) {
                return;
            }
            Meta meta = ResponseTextToFiles.filterMeta(data[data.length - 2]);
            //建立一个文件夹，文件夹命名为commit_hash,文件名以name字段的hash值
            File cacheFolder = ResponseTextToFiles.createFolder(Global.outputDir + "/" + meta.getProject_name() + "/" + meta.getCommit_hash());
            //代码文件
            convertAction(meta);
            meta.setOutputDir(Global.outputDir + '/' + meta.getProject_name() + '/' + meta.getCommit_hash());
            meta.setLinkPath(Constants.LINK_JSON);
            ResponseTextToFiles.convertCodeToFile(data, cacheFolder, meta);
            //写入meta文件
            //FileUtil.createFile("meta.json", new GsonBuilder().setPrettyPrinting().create().toJson(meta), folder);
            doDiff(meta);
            String response = new Gson().toJson(meta);
            Global.logger.info(response);
            exchange.sendResponseHeaders(200, response.length());
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            AServer.onError(e, exchange);
        }
    }

    private void doDiff(Meta meta) throws Exception {
        CLDiffAPI diff = new CLDiffAPI(Global.outputDir, meta);
        diff.generateDiffMinerOutput();
    }

    private void convertAction(Meta meta) {
        List<String> actions = meta.getActions();
        List<String> newActions = new ArrayList<>();
        for (String a : actions) {
            switch (a) {
                case Constants.NET.BWChangeTypeString.ADD:
                    newActions.add(Constants.ChangeTypeString.ADD);
                    break;
                case Constants.NET.BWChangeTypeString.MODIFY:
                    newActions.add(Constants.ChangeTypeString.MODIFY);
                    break;
                case Constants.NET.BWChangeTypeString.DELETE:
                    newActions.add(Constants.ChangeTypeString.DELETE);
                    break;
            }
        }
        meta.setActions(newActions);
    }

}
