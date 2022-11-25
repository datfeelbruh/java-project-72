package hexlet.code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class AppUtil {
    private static final Logger UtilLogger = LoggerFactory.getLogger(AppUtil.class);
    public static String getNormalizedUrl(String url) {
        try {
            UtilLogger.info("Попытка нормализовать полученный URL {}", url);
            URL receivedUrl = new URL(url);

            String normalizedUrl = String.format("%s://%s", receivedUrl.getProtocol(), receivedUrl.getHost());

            if (receivedUrl.getPort() > 0) {
                normalizedUrl = normalizedUrl + ":" + receivedUrl.getPort();
            }

            UtilLogger.info("Received normalized URL {}", receivedUrl);

            return normalizedUrl;

        } catch (MalformedURLException e) {
            return null;
        }
    }
}
