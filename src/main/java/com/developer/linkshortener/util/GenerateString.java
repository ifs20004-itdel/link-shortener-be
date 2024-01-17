package com.developer.linkshortener.util;

import com.developer.linkshortener.Constant.Constant;

public class GenerateString {

    public static String generate(){
        String alphaNumericString = "abcdefghijklmnopqrstuvwxyz";

        StringBuilder sb = new StringBuilder();
        for(int i=0; i< Constant.FILENAME_STR_SIZE; i++){
            int index = (int)(alphaNumericString.length()
                    * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}
