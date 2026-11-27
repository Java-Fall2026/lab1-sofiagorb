package ua.restaurant.storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FileStorage<T> {

    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);

    private final Class<T> type;
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public FileStorage(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Target entity class must not be null");
        this.jsonMapper = configureMapper(new ObjectMapper());
        YAMLFactory yamlFactory = YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();
        this.yamlMapper = configureMapper(new ObjectMapper(yamlFactory));
    }

    private ObjectMapper configureMapper(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public List<T> load(Path file) throws IOException {
        DataFormat format = DataFormat.fromPath(file);
        if (!Files.exists(file)) {
            throw new IOException("Data file does not exist: " + file);
        }
        if (Files.size(file) == 0) {
            log.warn("File '{}' is empty, returning empty list", file);
            return Collections.emptyList();
        }
        ObjectMapper mapper = selectMapper(format);
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        List<T> items = mapper.readValue(file.toFile(), listType);
        log.info("Loaded {} raw record(s) from {} file '{}'", items == null ? 0 : items.size(), format, file);
        return items == null ? Collections.emptyList() : items;
    }

    public void save(Path file, List<T> items) throws IOException {
        Objects.requireNonNull(items, "Items list must not be null");
        DataFormat format = DataFormat.fromPath(file);
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ObjectMapper mapper = selectMapper(format);
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
        log.info("Saved {} record(s) to {} file '{}'", items.size(), format, file);
    }

    private ObjectMapper selectMapper(DataFormat format) {
        return switch (format) {
            case JSON -> jsonMapper;
            case YAML -> yamlMapper;
        };
    }
}