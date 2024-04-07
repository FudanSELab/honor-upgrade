package edu.fdu.se.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.fdu.se.API.CLDiffLocal;
import edu.fdu.se.fileutil.ResponseTextToFiles;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 获取Meta缓存
 */
public class FetchMetaCacheHandler implements HttpHandler {
    /**
     * Post keys "commit_hash" "project_name"
     *
     * @param exchange
     */
    @Override
    public void handle(HttpExchange exchange) {
        Global.logger.info("FetchMetaCacheHandler");
        try {

            InputStream is = exchange.getRequestBody();
            OutputStream os = exchange.getResponseBody();
            String contentType = exchange.getRequestHeaders().get("Content-type").get(0);
            Map<String, String> mMap = MyNetUtil.parsePostedKeys(is, contentType);
            if (!(mMap.containsKey(Constants.NET.COMMIT_NAME) && mMap.containsKey(Constants.NET.PROJECT_NAME))) {
                throw new Exception();
            }
            String commitHash = mMap.get(Constants.NET.COMMIT_NAME);
            String projectName = mMap.get(Constants.NET.PROJECT_NAME);
            if (Constants.ONLINE == Global.runningMode) {
                Global.projectName = projectName;
            }
            if (Constants.EVALUATION == Global.runningMode) {
                Global.projectName = findResidingProjectNameOfCommit(commitHash);
            }
            //读取文件 文件路径为global_Path/project_name/commit_id/meta.txt
            File metaFile = new File(Global.outputDir + "/" + Global.projectName + "/" + commitHash + "/" + Constants.META_JSON);
            String metaStr = null;
            if (metaFile.exists()) {
                metaStr = ResponseTextToFiles.read(metaFile.getAbsolutePath());
            } else {
                if (Constants.OFFLINE == Global.runningMode) {
                    metaStr = generateCLDIFFResult(commitHash, metaFile, Global.outputDir);
                } else {
                    throw new Exception();
                }
            }
            exchange.sendResponseHeaders(200, metaStr.length());
            os.write(metaStr.getBytes());
            exchange.close();
        } catch (Exception e) {
            AServer.onError(e, exchange);
        }
    }


    private static String findResidingProjectNameOfCommit(String commit) throws Exception {
        File f = new File(Global.outputDir);
        File[] projects = f.listFiles();

        for (File proj : projects) {
            if (new File(proj.getAbsolutePath() + "/" + commit).exists()) {
                return proj.getName();
            }
        }
        throw new Exception();
    }




    private static String generateCLDIFFResult(String commitHash, File metaFile, String outputDir) {
        CLDiffLocal clDiffLocal = new CLDiffLocal(Global.prevCommitID);
        clDiffLocal.run(commitHash, Global.prevCommitID, outputDir);
        Meta meta = clDiffLocal.meta;
        //git 读取保存，生成meta
        //写入meta文件
        ResponseTextToFiles.createFile(Constants.META_JSON, new GsonBuilder().setPrettyPrinting().create().toJson(meta), new File(metaFile.getParent()));
        return new Gson().toJson(meta);
    }
}
