package CAC.invasiv_backend.utils;

public class Base64Utils {

    public static String cleanBase64String(String base64String) {
        if (base64String == null) {
            return "";
        }
        return base64String.replaceAll("^data:image/[^;]+;base64,", "");
    }
}
