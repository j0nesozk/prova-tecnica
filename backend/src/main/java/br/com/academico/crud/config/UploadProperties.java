package br.com.academico.crud.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.uploads")
public record UploadProperties(
        String dir,
        String publicBase,
        List<String> allowedContentTypes
) {}
