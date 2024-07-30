package inventorymanagement;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MultiLanguageSupport {
    private static ResourceBundle resourceBundle;

    public static void initialize(Locale locale) {
        resourceBundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String getString(String key) {
        return resourceBundle.getString(key);
    }
}