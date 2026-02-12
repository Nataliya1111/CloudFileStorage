package com.nataliya.persistence;

import com.nataliya.model.ResourceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResourceTypeConverter implements AttributeConverter<ResourceType, Short> {

    @Override
    public Short convertToDatabaseColumn(ResourceType type) {
        return type.getCode();
    }

    @Override
    public ResourceType convertToEntityAttribute(Short code) {
        return ResourceType.fromCode(code);
    }
}