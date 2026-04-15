package com.sealforge.infrastructure.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.sealforge.application.service.SecretYamlRenderer;
import com.sealforge.domain.exception.TechnicalFailureException;
import com.sealforge.domain.model.SecretDraft;
import com.sealforge.domain.model.SecretEntry;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JacksonSecretYamlSerializer implements SecretYamlRenderer {

    private final ObjectMapper objectMapper;

    public JacksonSecretYamlSerializer() {
        YAMLFactory yamlFactory = YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .build();
        this.objectMapper = new ObjectMapper(yamlFactory)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String render(SecretDraft secretDraft) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("apiVersion", "v1");
        root.put("kind", "Secret");
        root.put("metadata", metadata(secretDraft));
        root.put("type", secretDraft.type());
        root.put("stringData", stringData(secretDraft));

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception exception) {
            throw new TechnicalFailureException(
                    "The Secret YAML could not be generated.",
                    exception.getMessage(),
                    exception);
        }
    }

    private Map<String, Object> metadata(SecretDraft secretDraft) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", secretDraft.name());
        metadata.put("namespace", secretDraft.namespace());
        return metadata;
    }

    private Map<String, String> stringData(SecretDraft secretDraft) {
        Map<String, String> stringData = new LinkedHashMap<>();
        for (SecretEntry entry : secretDraft.entries()) {
            stringData.put(entry.key(), entry.value());
        }
        return stringData;
    }
}

