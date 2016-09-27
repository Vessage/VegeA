package cn.bahamut.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.bahamut.vessage.R;

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
            try {
                result = config?AESUtil.decrypt(xcode,result):result;
            } catch (Exception e) {
                result = null;
            }
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

    private static boolean config = true;
    private static String xcode = "5647382910";

    public static String readInputStreamText(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        config = (resId == R.raw.bahamut_config || resId == R.raw.bahamut_config_dev);
        String res = readInputStreamText(inputStream);
        config = false;
        return res;
    }

    public static String encodeText(String text,String seed) throws Exception {
        return AESUtil.encrypt(xcode,AESUtil.encrypt(seed,text));
    }
}
