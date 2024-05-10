package io.github.jpautils.jpaimporter.service.impl;

import io.github.jpautils.jpaimporter.attribute.chooser.EntityAttributeSetterChooser;
import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dao.GenericJpaDao;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeValueDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedAttributeValueDto;
import io.github.jpautils.jpaimporter.dto.parsing.ParsedEntityAttributeDto;
import io.github.jpautils.jpaimporter.service.EntityAttributeService;
import io.github.jpautils.jpaimporter.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class EntityAttributeServiceImpl implements EntityAttributeService {
    private GenericJpaDao genericJpaDao;

    private EntityAttributeSetterChooser entityAttributeSetterChooser;

    public EntityAttributeServiceImpl(
            GenericJpaDao genericJpaDao,
            EntityAttributeSetterChooser entityAttributeSetterChooser
    ) {
        this.genericJpaDao = genericJpaDao;
        this.entityAttributeSetterChooser = entityAttributeSetterChooser;
    }

    @Override
    public List<EntityAttributeDto> getEntityAttributesFor(List<ParsedEntityAttributeDto> parsedEntityAttributes, Class<?> entityClass) {
        List<EntityAttributeDto> entityAttributes = new ArrayList<>();

        for (ParsedEntityAttributeDto parsedEntityAttribute : parsedEntityAttributes) {
            String attributeName = parsedEntityAttribute.getAttributeName();

            EntityAttributeCharacteristicsDto entityAttributeCharacteristics = getEntityAttributeSetterCharacteristics(entityClass, attributeName);

            EntityAttributeSetter entityAttributeSetter = entityAttributeSetterChooser.getEntityAttributeSetterFor(entityAttributeCharacteristics);

            final Class<?> referencedEntityClass;

            if (parsedEntityAttribute.getIsAttributeReferencingEntity()) {
                Type attributeSetterType = entityAttributeCharacteristics.getSetterType();
                if (attributeSetterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) attributeSetterType;

                    if (parameterizedType.getActualTypeArguments().length != 1) {
                        throw new RuntimeException("Class [" + entityClass + "] attribute name [" + attributeName + "] has parametrized type. Parametrized type are supported with 1 type arguments, but it has: [" + parameterizedType.getActualTypeArguments().length + "]");
                    }

                    if (!(parameterizedType.getActualTypeArguments()[0] instanceof Class<?>)) {
                        throw new RuntimeException("Class [" + entityClass + "] attribute name [" + attributeName + "] has parametrized type which is not a class.");
                    }

                    referencedEntityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                } else if (attributeSetterType instanceof Class<?>) {
                    referencedEntityClass = (Class<?>)attributeSetterType;
                } else {
                    throw new RuntimeException("attributes referencing entity have to not be primitive");
                }
            } else {
                referencedEntityClass = null;
            }

            final List<EntityAttributeSetter> referencedEntityUniqueAttributesSetters;
            final List<EntityAttributeCharacteristicsDto> referencedEntityUniqueAttributesCharacteristics;

            if (parsedEntityAttribute.getIsAttributeReferencingEntity()) {
                referencedEntityUniqueAttributesSetters = new ArrayList<>();
                referencedEntityUniqueAttributesCharacteristics = new ArrayList<>();

                for (String referencedEntityUniqueField : parsedEntityAttribute.getReferencedEntityUniqueAttributes()) {
                    EntityAttributeCharacteristicsDto referencedEntityAttributeCharacteristics =
                            getEntityAttributeSetterCharacteristics(referencedEntityClass, referencedEntityUniqueField);

                    EntityAttributeSetter referencedEntityAttributeSetter = entityAttributeSetterChooser
                            .getEntityAttributeSetterFor(referencedEntityAttributeCharacteristics);

                    referencedEntityUniqueAttributesSetters.add(referencedEntityAttributeSetter);
                    referencedEntityUniqueAttributesCharacteristics.add(referencedEntityAttributeCharacteristics);
                }
            } else {
                referencedEntityUniqueAttributesSetters = null;
                referencedEntityUniqueAttributesCharacteristics = null;
            }

            EntityAttributeDto entityAttribute = new EntityAttributeDto(
                    parsedEntityAttribute.getAttributeName(),
                    parsedEntityAttribute.getIsUnique(),
                    parsedEntityAttribute.getAttributeIsAMapValueForMapKeyName(),
                    entityAttributeSetter,
                    entityAttributeCharacteristics,
                    parsedEntityAttribute.getIsAttributeReferencingEntity(),
                    referencedEntityClass,
                    parsedEntityAttribute.getReferencedEntityUniqueAttributes(),
                    referencedEntityUniqueAttributesSetters,
                    referencedEntityUniqueAttributesCharacteristics
            );

            entityAttributes.add(entityAttribute);
        }

        return entityAttributes;
    }

    //TODO: For setter and getter method name, currently is uses prefix get/set, but for booleans it could be generated with is instead of get/set
    //TODO: Currently is ignoring JPA annotations that mark the getter/setter
    @Override
    public EntityAttributeCharacteristicsDto getEntityAttributeSetterCharacteristics(Class<?> entityClass, String attributeName) {
        final Method attributeSetterMethod;
        final Method attributeGetterMethod;
        final Field attributeField;

        attributeSetterMethod = Arrays.stream(entityClass.getMethods())
                .filter(method -> method.getName().equalsIgnoreCase("set" + attributeName) && method.getParameters().length == 1)
                .findFirst()
                .orElse(null);

        attributeGetterMethod = Arrays.stream(entityClass.getMethods())
                .filter(method -> method.getName().equalsIgnoreCase("get" + attributeName) && method.getParameters().length == 0)
                .findFirst()
                .orElse(null);

        attributeField = Arrays.stream(entityClass.getFields())
                .filter(field -> field.getName().equalsIgnoreCase(attributeName))
                .findFirst()
                .orElse(null);

        if (attributeSetterMethod == null && attributeField == null) {
            throw new RuntimeException("Could not find setter method or field for Class: [" + entityClass + "] and attribute name: [" + attributeName + "]");
        }

        if (attributeGetterMethod == null && attributeField == null) {
            throw new RuntimeException("Could not find getter method or field for Class: [" + entityClass + "] and attribute name: [" + attributeName + "]");
        }

        final Type attributeSetterType;
        if (attributeSetterMethod != null) {
            attributeSetterType = attributeSetterMethod.getParameters()[0].getAnnotatedType().getType();
        } else {
            attributeSetterType = attributeField.getAnnotatedType().getType();
        }

        final Type attributeGetterType;
        if (attributeGetterMethod != null) {
            attributeGetterType = attributeGetterMethod.getAnnotatedReturnType().getType();
        } else {
            attributeGetterType = attributeField.getAnnotatedType().getType();
        }

        return new EntityAttributeCharacteristicsDto(
                attributeField,
                attributeSetterMethod,
                attributeGetterMethod,
                attributeSetterType,
                attributeGetterType,
                entityClass
        );
    }

    @Override
    public void setEntityAttributeValue(Object instance, EntityAttributeDto entityAttribute, EntityAttributeValueDto entityAttributeValue) {
        entityAttribute.getEntityAttributeSetter().setEntityAttributeValue(entityAttribute.getEntityAttributeCharacteristics(), entityAttributeValue, instance);
    }

    @Override
    public List<EntityAttributeValueDto> getEntityAttributeValuesFor(List<EntityAttributeDto> entityAttributes, List<ParsedAttributeValueDto> parsedAttributeValues, Map<String, Object> context) {
        List<EntityAttributeValueDto> entityAttributeValues = new ArrayList<>();

        for (int i = 0; i < parsedAttributeValues.size(); i++) {
            ParsedAttributeValueDto parsedValue = parsedAttributeValues.get(i);
            EntityAttributeDto entityAttribute = entityAttributes.get(i);

            EntityAttributeValueDto entityAttributeValue = getEntityAttributeValueFor(entityAttribute, parsedValue, context);
            entityAttributeValues.add(entityAttributeValue);
        }

        return entityAttributeValues;
    }

    private EntityAttributeValueDto getEntityAttributeValueFor(EntityAttributeDto entityAttribute, ParsedAttributeValueDto parsedValue, Map<String, Object> context) {
        //Attribute was not set
        if (parsedValue.getAttributeValue() != null && parsedValue.getAttributeValue().isBlank()) {
            return new EntityAttributeValueDto(
                    false,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        //Attribute is referencing entity
        if (entityAttribute.getIsAttributeReferencingEntity()) {
            if (parsedValue.getAttributeValue() != null && parsedValue.getAttributeValue().trim().equalsIgnoreCase("null")) {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }

            if (parsedValue.getAttributeValue() != null) {
                Object referencedEntity = getReferencedEntity(entityAttribute, parsedValue.getAttributeValue(), context);

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        referencedEntity,
                        null,
                        null
                );
            }

            if (parsedValue.getAttributeValuesList() != null) {
                List<Object> referencedEntities = new ArrayList<>();

                for (String referencedEntityFieldsValue : parsedValue.getAttributeValuesList()) {
                    Object referencedEntity = getReferencedEntity(entityAttribute, referencedEntityFieldsValue, context);
                    referencedEntities.add(referencedEntity);
                }

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        null,
                        referencedEntities,
                        null
                );
            }
        }

        //Attribute is a map entry with key in attribute definition
        if (entityAttribute.getAttributeIsAMapValueForMapKeyName() != null && parsedValue.getAttributeValue() != null) {
            if (parsedValue.getAttributeValue().trim().equalsIgnoreCase("null")) {
                return new EntityAttributeValueDto(
                        true,
                        true,
                        null,
                        null,
                        Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), null),
                        null,
                        null,
                        Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), null)
                );
            } else {
                Object attributeValue = getActualValueFor(parsedValue.getAttributeValue(), context);

                if (attributeValue == null) {
                    return new EntityAttributeValueDto(
                            true,
                            true,
                            null,
                            null,
                            Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), null),
                            null,
                            null,
                            Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), null)
                    );
                }

                if (attributeValue instanceof String) {
                    return new EntityAttributeValueDto(
                            true,
                            true,
                            null,
                            null,
                            Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), (String)attributeValue),
                            null,
                            null,
                            null
                    );
                } else {
                    return new EntityAttributeValueDto(
                            true,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            Collections.singletonMap(entityAttribute.getAttributeIsAMapValueForMapKeyName(), attributeValue)
                    );
                }
            }
        }

        //Value is a string
        if (parsedValue.getAttributeValue() != null) {
            Object attributeValue = getActualValueFor(parsedValue.getAttributeValue(), context);

            if (attributeValue == null) {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            }

            if (attributeValue instanceof String) {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        (String) attributeValue,
                        null,
                        null,
                        null,
                        null,
                        null
                );
            } else {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        attributeValue,
                        null,
                        null
                );
            }
        }

        //Value is a list
        if (parsedValue.getAttributeValuesList() != null) {
            if (parsedValue.getAttributeValuesList().isEmpty()) {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        List.of(),
                        null,
                        null,
                        List.of(),
                        null
                );
            }

            boolean areAllActualValuesStrings = areAllActualValuesStrings(parsedValue.getAttributeValuesList());

            if (areAllActualValuesStrings) {
                List<String> actualValuesList = parsedValue.getAttributeValuesList().stream().map(string -> (String)getActualValueFor(string, context)).collect(Collectors.toList());

                if (actualValuesList.stream().allMatch(Objects::isNull)) {
                    return new EntityAttributeValueDto(
                            true,
                            false,
                            null,
                            actualValuesList,
                            null,
                            null,
                            actualValuesList,
                            null
                    );
                }

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        actualValuesList,
                        null,
                        null,
                        null,
                        null
                );
            } else {
                List<Object> attributeValues = parsedValue.getAttributeValuesList().stream().map(string -> getActualValueFor(string, context)).collect(Collectors.toList());

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        null,
                        attributeValues,
                        null
                );
            }
        }

        //Value is a map
        if (parsedValue.getAttributeValuesMap() != null) {
            if (parsedValue.getAttributeValuesMap().isEmpty()) {
                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        Map.of(),
                        null,
                        null,
                        Map.of()
                );
            }

            boolean areAllKeysStrings = areAllActualValuesStrings(parsedValue.getAttributeValuesMap().keySet());
            boolean areAllValuesStrings = areAllActualValuesStrings(parsedValue.getAttributeValuesMap().values());

            if (areAllKeysStrings && areAllValuesStrings) {
                Map<String, String> attributeValuesMap = new HashMap<>();
                for (Map.Entry<String, String> entry : parsedValue.getAttributeValuesMap().entrySet()) {
                    String actualKey = (String)getActualValueFor(entry.getKey(), context);
                    String actualValue = (String)getActualValueFor(entry.getValue(), context);

                    attributeValuesMap.put(actualKey, actualValue);
                }

                boolean allMapValuesAreNull =  attributeValuesMap.values().stream().allMatch(Objects::isNull);

                if (allMapValuesAreNull) {
                    return new EntityAttributeValueDto(
                            true,
                            false,
                            null,
                            null,
                            attributeValuesMap,
                            null,
                            null,
                            attributeValuesMap
                    );
                }

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        attributeValuesMap,
                        null,
                        null,
                        null
                );
            } else {
                Map<Object, Object> attributeValuesMap = new HashMap<>();

                for (Map.Entry<String, String> entry : parsedValue.getAttributeValuesMap().entrySet()) {
                    Object actualKey = getActualValueFor(entry.getKey(), context);
                    Object actualValue = getActualValueFor(entry.getValue(), context);

                    attributeValuesMap.put(actualKey, actualValue);
                }

                return new EntityAttributeValueDto(
                        true,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        attributeValuesMap
                );
            }
        }

        throw new RuntimeException("Could not translate the parsed attribute value: [" + parsedValue + "]");
    }

    private boolean areAllActualValuesStrings(Collection<String> values) {
        return values.stream().noneMatch(string -> string.trim().startsWith("@"));
    }

    private Object getActualValueFor(String value, Map<String, Object> context) {
        String trimmedValue = value.trim();

        if (trimmedValue.startsWith("@")) {
            if (!context.containsKey(trimmedValue.toLowerCase())) {
                throw new RuntimeException("Could not find reference: [" + trimmedValue + "] in context.");
            }

            return context.get(trimmedValue.toLowerCase());
        }

        if (trimmedValue.equalsIgnoreCase("null")) {
            return null;
        }

        if (trimmedValue.length() > 1 && trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
            return trimmedValue.substring(1, trimmedValue.length() - 1);
        }

        return trimmedValue;
    }

    private Object getReferencedEntity(EntityAttributeDto entityAttribute, String parsedValue, Map<String, Object> context) {
        String trimmedValue = parsedValue.trim();

        if (trimmedValue.equalsIgnoreCase("null")) {
            return null;
        }

        final String valueWithoutDoubleQuotes;

        if (trimmedValue.length() > 1 && trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
            valueWithoutDoubleQuotes = trimmedValue.substring(1, trimmedValue.length() - 1);
        } else {
            valueWithoutDoubleQuotes = trimmedValue;
        }

        List<Object> referencedEntityValues = Util.splitByCharacter(valueWithoutDoubleQuotes, ' ')
                .stream()
                .filter(Util::stringIsNotBlank)
                .map(string -> {
                    if (string.startsWith("@")) {
                        if (!context.containsKey(string.toLowerCase())) {
                            throw new RuntimeException("Could not find reference: [" + string + "] in context.");
                        }

                        return context.get(string);
                    }

                    return string;
                })
                .collect(Collectors.toList());

        Class<?> referencedEntityClass = entityAttribute.getReferencedEntityClass();
        List<String> referencedEntityUniqueAttributes = entityAttribute.getReferencedEntityUniqueAttributes();
        List<EntityAttributeSetter> referencedEntityUniqueAttributesSetters = entityAttribute.getReferencedEntityUniqueAttributesSetters();
        List<EntityAttributeCharacteristicsDto> referencedEntityUniqueAttributesCharacteristics = entityAttribute.getReferencedEntityUniqueAttributesCharacteristics();

        Optional<?> referencedEntityOptional = genericJpaDao.findReferencedEntityBy(
                referencedEntityClass,
                referencedEntityUniqueAttributes,
                referencedEntityUniqueAttributesSetters,
                referencedEntityUniqueAttributesCharacteristics,
                referencedEntityValues
        );

        if (referencedEntityOptional.isEmpty()) {
            throw new RuntimeException("Could not find referenced entity for Class: [" + referencedEntityClass + "], attributes: " + referencedEntityUniqueAttributes + ", values: [" + referencedEntityValues + "].");
        }

        return referencedEntityOptional.get();
    }
}
