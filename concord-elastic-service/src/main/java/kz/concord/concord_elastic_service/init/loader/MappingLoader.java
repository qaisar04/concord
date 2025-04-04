package kz.concord.concord_elastic_service.init.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

public class MappingLoader {

    public static String loadMapping(String resourcePath) {
        try (InputStream is = new ClassPathResource(resourcePath.replaceFirst("classpath:", "")).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load mapping from " + resourcePath, e);
        }
    }
}
