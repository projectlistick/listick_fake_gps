package CryptUtils;

import java.io.UnsupportedEncodingException;

public class Base64 {
    public static String decrypt(String cryptedData) throws UnsupportedEncodingException {
        return new String(android.util.Base64.decode(cryptedData, android.util.Base64.DEFAULT), "UTF-8");
    }

}
