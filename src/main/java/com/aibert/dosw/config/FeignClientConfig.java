package com.aibert.dosw.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de todos los Feign Clients.
 *
 * <ul>
 * <li>Nivel de log BASIC → registra método HTTP, URL, status y duración.</li>
 * <li>ErrorDecoder personalizado → loguea errores de servicios externos con
 * contexto suficiente para debugging y lanza la excepción apropiada.</li>
 * </ul>
 *
 * Los adapters (TaskFeignClientAdapter, ProfileFeignClientAdapter, etc.)
 * capturan
 * estas excepciones y retornan fallback data, garantizando resiliencia aislada.
 */
@Configuration
public class FeignClientConfig {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FeignClientConfig.class);

    /**
     * Activa logs de Feign al nivel BASIC (método, URL, HTTP status, duración).
     * Para ver los logs, el logger "feign" debe estar en DEBUG en
     * logback-spring.xml.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * ErrorDecoder personalizado: loguea el error con contexto completo
     * antes de delegar al decoder por defecto de Feign (que lanza FeignException).
     *
     * <p>
     * Los adapters (try/catch) interceptan la excepción y retornan datos mock
     * cuando el servicio externo no está disponible — este decoder sólo garantiza
     * que el error quede registrado incluso si el adapter lo silencia.
     * </p>
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            int status = response.status();
            String url = response.request().url();
            if (status == 404) {
                log.warn("FeignClient NOT FOUND: method={} url={}", methodKey, url);
            } else if (status >= 500) {
                log.error("FeignClient SERVER ERROR: method={} url={} httpStatus={}", methodKey, url, status);
            } else if (status >= 400) {
                log.warn("FeignClient CLIENT ERROR: method={} url={} httpStatus={}", methodKey, url, status);
            }
            return defaultDecoder.decode(methodKey, response);
        };
    }
}
