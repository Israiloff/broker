package uz.cbssolutions.broker.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import uz.cbssolutions.broker.config.SerializerConfig;

import java.util.function.BiFunction;

/**
 * Utility for serialization purposes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SerializationUtil {

    /**
     * A base instance of Jackson's serializer.
     */
    @Qualifier(SerializerConfig.SERIALIZER_NAME)
    private final ObjectMapper objectMapper;

    private <TResult, TData> Mono<TResult> reactiveCall(BiFunction<TData, Class<TResult>, TResult> serializer, TData data, Class<TResult> resultClass) {
        return Mono.<TResult>create(sink -> {
            TResult result;
            try {
                result = serializer.apply(data, resultClass);
            } catch (Throwable e) {
                sink.error(e);
                return;
            }
            sink.success(result);
        }).publishOn(Schedulers.boundedElastic());
    }

    /**
     * Byte array reactive deserialization operation with runtime exception.
     *
     * @param bytes  Data for deserialization.
     * @param tClass Result class.
     * @param <T>    Type of deserialization result.
     * @return Deserialized data.
     */
    public <T> Mono<T> reactiveDeserialization(byte[] bytes, Class<T> tClass) {
        return reactiveCall(this::deserialize, bytes, tClass);
    }

    /**
     * String overload of reactive deserialization operation with runtime exception.
     *
     * @param text   Text for deserialization.
     * @param tClass Result class.
     * @param <T>    Type of deserialization result.
     * @return Deserialized data.
     */
    public <T> Mono<T> reactiveDeserialization(String text, Class<T> tClass) {
        return reactiveCall(this::deserialize, text, tClass);
    }

    /**
     * Byte array deserialization operation with runtime exception.
     *
     * @param bytes  Data for deserialization.
     * @param tClass Result class.
     * @param <T>    Type of deserialization result.
     * @return Deserialized data.
     */
    @SneakyThrows
    public <T> T deserialize(byte[] bytes, Class<T> tClass) {
        var result = objectMapper.readValue(bytes, tClass);
        log.debug("deserialization result : {}", result);
        return result;
    }

    /**
     * String overload of deserialization operation with runtime exception.
     *
     * @param text   Text for deserialization.
     * @param tClass Result class.
     * @param <T>    Type of deserialization result.
     * @return Deserialized data.
     */
    @SneakyThrows
    public <T> T deserialize(String text, Class<T> tClass) {
        var result = objectMapper.readValue(text, tClass);
        log.debug("deserialization result : {}", result);
        return result;
    }
}
