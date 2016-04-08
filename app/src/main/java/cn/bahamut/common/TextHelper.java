package cn.bahamut.common;

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

            return strBuffer.toString();

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
}
