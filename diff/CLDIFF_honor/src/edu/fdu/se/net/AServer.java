package edu.fdu.se.net;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import edu.fdu.se.fileutil.ResponseTextToFiles;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;

public class AServer {

    /**
     * Fetch file, meta,link,diff,.java file
     *
     * @param exchange
     */
    public static void fetchFileContent(HttpExchange exchange) {
        System.out.println("FetchFileContentHandler");
        OutputStream os = exchange.getResponseBody();
        try {
            InputStream is = exchange.getRequestBody();
            String contentType = exchange.getRequestHeaders().get("Content-type").get(0);
            Map<String, String> mMap = MyNetUtil.parsePostedKeys(is, contentType);
            String commit_hash = mMap.get(Constants.NET.COMMIT_NAME);
            String project_name = mMap.get(Constants.NET.PROJECT_NAME);
            String fileName = mMap.get(Constants.NET.FILE_NAME);
            //文件路径为 global_Path/project_name/commit_id/meta.json
            String outputPath = Global.outputDir + "/" + project_name + "/" + commit_hash + "/";
            String metaStr = ResponseTextToFiles.read(outputPath + Constants.META_JSON);
            Meta meta = new Gson().fromJson(metaStr, Meta.class);
            String result;
            if (Constants.NET.GRAPH.equals(fileName)) {
                result = returnDiffLink(exchange);

            } else {
                String[] fileNames = fileName.split(Constants.TOOLSPLITTER);
                int fileId = Integer.valueOf(fileNames[0]);
                result = initPackagedFileData(fileId, meta, outputPath);
            }
            byte[] bytes = result.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            MyNetUtil.writeResponseInBytes(os, bytes);
        } catch (Exception e) {
            onError(e, exchange);
        }

    }

    private static String returnDiffLink(HttpExchange exchange) {
        return "DIFFF";
    }

    private static String initPackagedFileData(int id, Meta meta, String outputPathCommit) {
        CommitFile file = meta.getFiles().get(id);
        String action = meta.getActions().get(id);
        String curr_file_path;
        String prev_file_path;
        String currFileContent = "";
        String prevFileContent = "";
        String diff = null;

        if (Constants.ChangeTypeString.MODIFY.equals(action)) {
            prev_file_path = file.getPrev_file_path();
            curr_file_path = file.getCurr_file_path();
            currFileContent = ResponseTextToFiles.read(outputPathCommit + curr_file_path);
            prevFileContent = ResponseTextToFiles.read(outputPathCommit + prev_file_path);
        } else if (Constants.ChangeTypeString.ADD.equals(action)) {
            curr_file_path = file.getCurr_file_path();
            currFileContent = ResponseTextToFiles.read(outputPathCommit + curr_file_path);
        } else if (Constants.ChangeTypeString.DELETE.equals(action)) {
            prev_file_path = file.getPrev_file_path();
            prevFileContent = ResponseTextToFiles.read(outputPathCommit + prev_file_path);
        }
        if (file.getDiffPath() != null) {
            diff = ResponseTextToFiles.read(meta.getOutputDir() + "/" + file.getDiffPath());
        }
        String link = ResponseTextToFiles.read(meta.getLinkPath());
        PackagedFileData content = new PackagedFileData(prevFileContent, currFileContent, diff, link);
        String contentResultStr = new Gson().toJson(content);
        return contentResultStr;
    }

    public static void clearCache(HttpExchange exchange) {
        System.out.println("clear cache");
        try {
            File f = new File(Global.outputDir);
            if (f.exists()) {
                FileUtils.forceDelete(new File(Global.outputDir));
            }
            OutputStream outs = exchange.getResponseBody();
            String success = "SUCCESS\n";
            exchange.sendResponseHeaders(200, success.length());
            outs.write(success.getBytes());
            outs.close();
        } catch (Exception e) {
            onError(e, exchange);
        }

    }


    public static void onError(Exception e, HttpExchange exchange) {
        e.printStackTrace();
        OutputStream os = exchange.getResponseBody();
        try {
            exchange.sendResponseHeaders(200, "error".length());
            os.write("error".getBytes());
            os.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static void initCLDIFFGlobals() {
        Global.granularity = Constants.GRANULARITY.STATEMENT;
        Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
        Global.isMethodRangeContainsJavaDoc = false;
        Global.isLink = true;

    }
}
