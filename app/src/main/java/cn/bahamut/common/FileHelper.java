package cn.bahamut.common;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

/**
 * Created by alexchow on 16/4/25.
 */
public class FileHelper {
    public static void customBufferBufferedStreamCopy(File source, File target) {
        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(source));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            byte[] buf = new byte[2048];
            int i;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
            close(fos);
        }
    }

    private static void close(OutputStream fos) {
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(InputStream fis) {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveFile(byte[] data, File descFile) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(descFile);
            fos.write(data);
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File generateTempFile(Context context,String fileType){
        if (fileType.startsWith(".")){
            fileType = fileType.substring(1);
        }else if(fileType.startsWith("*.")){
            fileType = fileType.substring(2);
        }
        return new File(context.getCacheDir(),generateTempFileName() + "." + fileType);
    }

    public static String generateTempFileNameWithType(String fileType){
        if (fileType.startsWith(".")){
            fileType = fileType.substring(1);
        }else if(fileType.startsWith("*.")){
            fileType = fileType.substring(2);
        }
        return String.format("%s.%s",generateTempFileName(),fileType);
    }

    public static String generateTempFileName() {
        return String.format("%d_%d", new Date().getTime(), new Random(new Date().getTime()).nextInt(100));
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("error  ");
            e.printStackTrace();
        }
    }

    public static void copyFile(File oldfile, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            //File     oldfile     =     new     File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldfile);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("error  ");
            e.printStackTrace();
        }
    }
}
