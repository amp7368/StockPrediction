package aaron.stock.utils;

public class UrlParameters {
    public static String buildURL(String baseURL, Pair<String, String>[] parameters) {
        StringBuilder url = new StringBuilder(baseURL);
        url.append('?');
        for (Pair<String, String> parameter : parameters) {
            url.append(parameter.getKey());
            url.append('=');
            url.append(parameter.getValue());
        }
        return url.toString();
    }
}
