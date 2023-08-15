package uz.cbssolutions.broker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main serializer configurations.
 */
@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
public class SerializerConfig {

    public static final String SERIALIZER_NAME = "cmBrokerJacksonMapper";

    /**
     * Jackson mapper defined bean creation point.
     *
     * @return Configured Jackson mapper.
     */
    @Bean(SERIALIZER_NAME)
    public ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
