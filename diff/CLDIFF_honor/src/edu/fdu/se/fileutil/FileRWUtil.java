package edu.fdu.se.fileutil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.WorkingTreeOptions;

public class FileRWUtil {

    public static void writeInAll(String filePath, String content, Boolean append) {
        writeInAll(new File(filePath), content.getBytes(), append);
    }

    public static void writeInAll(String filePath, String content) {
        writeInAll(new File(filePath), content.getBytes());
    }

    public static void writeInAll(String filePath, byte[] content, Boolean append) {
        File file = new File(filePath);
        writeInAll(file, content, append);
    }

    public static void writeInAll(String filePath, byte[] content) {
        File file = new File(filePath);
        writeInAll(file, content);
    }

    public static void writeInAll(File file, byte[] content, Boolean append) {
        try {
            FileOutputStream fos = new FileOutputStream(file, append);
            fos.write(content);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeInAll(File file, byte[] content) {
        try {
            File fileP = file.getParentFile();
            if (!fileP.exists()) {
                fileP.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeInAll(String filePath, InputStream is) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            int ch;
            while ((ch = is.read()) != -1) {
                fos.write(ch);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeErrorLog(String filePath, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath, true);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<String> getLines(File f) {
        FileInputStream fos;
        List<String> result = new ArrayList<String>();
        String line;
        try {
            fos = new FileInputStream(f);
            InputStreamReader ir = new InputStreamReader(fos);
            BufferedReader br = new BufferedReader(ir);
            while ((line = br.readLine()) != null) {
                result.add(line.trim());
            }
            br.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<String> getLines(byte[] buffer) {
        InputStream sbs = new ByteArrayInputStream(buffer);
        List<String> result = new ArrayList<String>();
        String line;
        try {
            InputStreamReader ir = new InputStreamReader(sbs);
            BufferedReader br = new BufferedReader(ir);
            while ((line = br.readLine()) != null) {
                result.add(line.trim());
            }
            br.close();
            return result;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static boolean fileOutput(byte[] a, String output) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(output));
            fos.write(a);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static InputStream open(ObjectId blobId, Repository db) throws IOException, IncorrectObjectTypeException {
        if (blobId == null)
            return new ByteArrayInputStream(new byte[0]);

        try {
            WorkingTreeOptions workingTreeOptions = db.getConfig().get(WorkingTreeOptions.KEY);
            switch (workingTreeOptions.getAutoCRLF()) {
                case INPUT:
                    // When autocrlf == input the working tree could be either CRLF
                    // or LF, i.e. the comparison
                    // itself should ignore line endings.
                case FALSE:
                    return db.open(blobId, Constants.OBJ_BLOB).openStream();
                case TRUE:
                default:
                    return db.open(blobId, Constants.OBJ_BLOB).openStream();
//				return new AutoCRLFInputStream(db.open(blobId, Constants.OBJ_BLOB).openStream(), true);
            }
        } catch (MissingObjectException notFound) {
            return null;
        }
    }

    /**
     * 将obj每一行添加到 fileList 中，同时返回一个包含整个文件内容的字符串
     * @param obj
     * @param fileList
     * @return
     */
    public static String getLinesOfFile(Object obj, List<String> fileList) {
        if (obj instanceof String) {
            return getLinesOfFile((String) obj, fileList);
        } else if (obj instanceof byte[]) {
            return getLinesOfFile((byte[]) obj, fileList);
        }
        return null;
    }

    private static String getLinesOfFile(String filePath, List<String> fileList) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                fileList.add(line);
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将字节数组解析为文本文件的行，并将每一行添加到 fileList 中，同时返回一个包含整个文件内容的字符串。
     * @param fileContent
     * @param fileList
     * @return
     */
    private static String getLinesOfFile(byte[] fileContent, List<String> fileList) {
        try {
            InputStream fis = new ByteArrayInputStream(fileContent);
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                fileList.add(line);
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Integer> getLinesList(int line) {
        List<Integer> mList = new ArrayList<>();
        for (int i = 0; i < line; i++) {
            mList.add(i);
        }
        return mList;
    }

    public static String getDirPath(String fn) {
        String parentPath = getParentPath(new File(fn));
        int x = fn.indexOf(parentPath);
        return x >= 0 ? fn.substring(parentPath.length() + 1) : fn;
    }

    private static String getParentPath(File filePath) {
        File parentFile = filePath.getParentFile().getParentFile();
        if (parentFile != null && parentFile.isDirectory()) {
            File[] children = parentFile.listFiles();
            if (children != null && children.length != 0) {
                for (File child : children) {
                    if (child.getName().equals(".repo")) {
                        return child.getParentFile().getAbsolutePath();
                    }
                }
            }
            return getParentPath(filePath.getParentFile());
        }
        return filePath.getAbsolutePath();
    }

    public static void removeDir(String unZipDir) {
        delFolder(unZipDir); //删除所有内容
    }

    //folderPath 文件夹完整绝对路径
    private static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            File myFilePath = new File(folderPath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除指定文件夹下所有文件; path 文件夹完整绝对路径
    private static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }


}
