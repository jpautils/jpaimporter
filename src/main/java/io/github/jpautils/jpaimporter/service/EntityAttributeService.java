package io.github.jpautils.jpaimporter.service;

import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValueDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityAttributeDto;

import java.util.List;
import java.util.Map;

public interface EntityAttributeService {
    List<EntityAttributeDto> getEntityAttributesFor(List<ParsedEntityAttributeDto> parsedEntityAttributes, Class<?> entityClass);

    //TODO: For setter and getter method name, currently is uses prefix get/set, but for booleans it could be generated with is instead of get/set
    //TODO: Currently is ignoring JPA annotations that mark the getter/setter
    EntityAttributeCharacteristicsDto getEntityAttributeSetterCharacteristics(Class<?> entityClass, String attributeName);

    void setEntityAttributeValue(Object instance, EntityAttributeDto entityAttribute, EntityAttributeValueDto entityAttributeValue);

    List<EntityAttributeValueDto> getEntityAttributeValuesFor(List<EntityAttributeDto> entityAttributes, List<ParsedAttributeValueDto> parsedAttributeValues, Map<String, Object> context);
}
