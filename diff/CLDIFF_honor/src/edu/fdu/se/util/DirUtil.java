package edu.fdu.se.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DirUtil {

    public static List<File> getAllFilesOfADirectory(String path){
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        return result;
    }
    public static List<File> getAllSuffixFilesOfADirectory(String path,String suffix){
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        List<File> collect = result.stream()
                .filter(a -> a.getAbsolutePath().endsWith(suffix))
                .collect(Collectors.toList());

        return collect;
    }


    public static List<File> getAllJavaFilesOfADirectory(String path){
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        List<File> collect = result.stream()
                .filter(a -> a.getAbsolutePath().endsWith(".java"))
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * 获取所有以 .c .h .cpp 结尾的文件，把绝对路径封装在collect中
     */
    public static List<File> getAllCFileOfADirectory(String path) {
        List<File> result = new ArrayList<>();
        browse(new File(path),result);
        List<File> collect = result.stream()
                .filter(a -> a.getAbsolutePath().endsWith(".c") || a.getAbsolutePath().endsWith(".h")
                        || a.getAbsolutePath().endsWith(".cpp"))
                .collect(Collectors.toList());

        return collect;
    }

    private static void browse(File dir, List<File> mList){
        if(!dir.isDirectory()){
            mList.add(dir);
            return;
        }
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File f:files){
            if(f.isDirectory()){
                if(f.getName().equals(".git") || f.getName().equals("aidl")){
                    continue;
                }
                browse(f,mList);

            }else{
                mList.add(f);
            }
        }
    }

    /**
     * 去除 .. .符号
     * @return
     */
    public static String trimPathSub(String path) {
        path = path.replace("\\", "/");
        String[] data = path.split("/");
        Set<Integer> a = new HashSet<>();
        for (int i = 0; i < data.length; i++) {
            String temp = data[i];
            if (i != 0 && "..".equals(temp) && (!data[i - 1].equals(".."))) {
                a.add(i - 1);
                a.add(i);
            }
            if (i != 0 && ".".equals(temp)) {
                a.add(i);
            }
        }
        String res = "";
        for (int i = 0; i < data.length; i++) {
            if (data[i].equals("")) {
                continue;
            }
            if (a.contains(i)) {
                continue;
            } else {
                res += data[i];
                res += "/";
            }
        }
        if (path.endsWith("/")) {
            return res;
        }
        return res.substring(0, res.length()-1);
    }

    public static String trimPath(String path) {
        String path2 = path;
        String newPath = null;
        while(true) {
            newPath = trimPathSub(path2);
            if (!newPath.equals(path2)) {
                path2 = newPath;
            } else {
                break;
            }
        }
        return newPath;
    }

    /**
     * replacing "\\" with "/"
     *
     * /foo/bar/  C:\\foo\\bar  C:\\foot\\bar/foo/bar
     * @param path
     * @return
     */
    public static String unifyPathSeparator(String path){
        return path.replace('\\','/');
    }


    public static String getFileShortNameFromPackagePath(String packagePath){
        String packagePath2 = unifyPathSeparator(packagePath);
        if(packagePath2.endsWith("/")){
            packagePath2 = packagePath2.substring(0,packagePath2.length()-1);
        }
        int index = packagePath2.lastIndexOf("/");
        return packagePath2.substring(index+1);
    }

    public static String getFilePrefixPath(String filePath){
        String packagePath2 = unifyPathSeparator(filePath);
        if(packagePath2.endsWith("/")){
            packagePath2 = packagePath2.substring(0,packagePath2.length()-1);
        }
        int index = packagePath2.lastIndexOf("/");
        return packagePath2.substring(0,index);
    }
}
