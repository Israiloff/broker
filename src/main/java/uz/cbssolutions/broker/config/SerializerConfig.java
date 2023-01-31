package uz.cbssolutions.broker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main serializer configurations.
 */
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
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
