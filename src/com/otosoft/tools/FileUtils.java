package com.android.otosoft.tools;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by libing on 2017/02/20.
 */
public class FileUtils {

    private static final int BUF_SIZE = 1024;

    /**
     * copy files or directory
     *
     * @param srcPath
     * @param destDir
     *
     * @return
     */
    public static boolean copyGeneralFile(String srcPath, String destDir) {
        File file = new File(srcPath);
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return copyFile(srcPath, destDir);
        } else if (file.isDirectory()) {
            return copyDirectory(srcPath, destDir);
        }
        return false;
    }

    /**
     * copy files
     *
     * @param srcPath
     *
     * @param destDir
     *
     * @return boolean
     */
    private static boolean copyFile(String srcPath, String destDir) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }
        String fileName = srcPath
                .substring(srcPath.lastIndexOf(File.separator));
        String destPath = destDir + fileName;
        if (destPath.equals(srcPath)) {
            return false;
        }
        File destFile = new File(destPath);
        if (destFile.exists() && destFile.isFile()) {
            return false;
        }

        File destFileDir = new File(destDir);
        destFileDir.mkdirs();
        try {
            FileInputStream fis = new FileInputStream(srcPath);
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buf = new byte[BUF_SIZE];
            int c;
            while ((c = fis.read(buf)) != -1) {
                fos.write(buf, 0, c);
            }
            fis.close();
            fos.close();

            return true;
        } catch (IOException e) {
            //
        }

        return false;
    }

   /**
     *  copy  directory
     * @param srcPath
     *
     * @param destPath
     *
     * @return
     */
    private static boolean copyDirectory(String srcPath, String destDir) {
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }
        String dirName = getDirName(srcPath);
        String destPath = destDir + File.separator + dirName;

        if (destPath.equals(srcPath)) {
            return false;
        }
        File destDirFile = new File(destPath);
        if (destDirFile.exists()) {
            return false;
        }
        destDirFile.mkdirs();

        File[] fileList = srcFile.listFiles();
        if (fileList.length == 0) {
            return true;
        } else {
            for (File temp : fileList) {
                if (temp.isFile()) {
                    if (!copyFile(temp.getAbsolutePath(), destPath)) {
                        return false;
                    }
                } else if (temp.isDirectory()) {
                    if (!copyDirectory(temp.getAbsolutePath(), destPath)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

   /**
     * get the direcotry name
     *
     * @param dir
     * @return String
     */
    private static String getDirName(String dir) {
        if (dir.endsWith(File.separator)) {
            dir = dir.substring(0, dir.lastIndexOf(File.separator));
        }
        return dir.substring(dir.lastIndexOf(File.separator) + 1);
    }

    /**
     * delete files or directory
     *
     * @param path
     *
     * @return boolean
     */
    public static boolean deleteGeneralFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            return deleteDirectory(file.getAbsolutePath());
        } else if (file.isFile()) {
            return deleteFile(file);
        }
        return false;
    }

    /**
     * delete files
     *
     * @param file
     * @return boolean
     */
    private static boolean deleteFile(File file) {
        return file.delete();
    }

     /**
     * delete all the files under the directory
     *
     * @param path
     *
     */
    private static boolean deleteDirectory(String path) {
        File dirFile = new File(path);
        if (!dirFile.isDirectory()) {
            return true;
        }
        File[] files = dirFile.listFiles();
        for (File file : files) {
            // Delete file.
            if (file.isFile()) {
                if (!deleteFile(file)) {
                   return dirFile.delete();
                }
            } else if (file.isDirectory()) {
                if(!deleteDirectory(file.getAbsolutePath())) {
                    return dirFile.delete();
                }
            }
        }
        return dirFile.delete();
    }

    /**
     * cut the files (just copy and delete)
     *
     * @param destDir
     *
     */
    public static boolean cutGeneralFile(String srcPath, String destDir) {
        return copyGeneralFile(srcPath, destDir) && deleteGeneralFile(srcPath);
    }
}
