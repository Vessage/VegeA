package cn.bahamut.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alexchow on 16/4/8.
 */
public class TextHelper {

    public static String readInputStreamText(InputStream inputStream){

        InputStreamReader inputReader = null;
        BufferedReader bufferReader = null;
        try
        {
            inputReader = new InputStreamReader(inputStream);
            bufferReader = new BufferedReader(inputReader);

            // 读取一行
            String line = null;
            StringBuffer strBuffer = new StringBuffer();

            while ((line = bufferReader.readLine()) != null)
            {
                strBuffer.append(line);
            }
            String result = strBuffer.toString();
            return result;
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            try {
                inputReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readInputStreamText(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        String res = readInputStreamText(inputStream);
        //region
        res = trim(res,resId);
        //endregion
        return res;
    }

    //region
    public static String trim(String res, int resId){
        try {
            res = resId == xconfigId ? AESUtil.decrypt(xcode,res) : res;
        } catch (Exception e) {
            res = null;
        }
        return res;
    }
    private static String xcode = "5647382910";
    public static int xconfigId = 0;
    public static String encodeText(String text,String seed) throws Exception {
        return AESUtil.encrypt(xcode,AESUtil.encrypt(seed,text));
    }
    //endregion
}
