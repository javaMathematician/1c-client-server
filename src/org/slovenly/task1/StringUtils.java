package org.slovenly.task1;

public class StringUtils {
    public static boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

}
