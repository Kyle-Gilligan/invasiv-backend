package CAC.invasiv_backend.utils;

// You can delete the readBase64FromFile method, keeping only the cleanBase64String method.

public class Base64Utils {

    /**
     * Cleans a Base64 string by removing common data URI prefixes.
     *
     * @param base64String The raw Base64 string, which may contain a prefix.
     * @return The cleaned Base64 string.
     */
    public static String cleanBase64String(String base64String) {
        if (base64String == null) {
            return "";
        }
        return base64String.replaceAll("^data:image/[^;]+;base64,", "");
    }
}
