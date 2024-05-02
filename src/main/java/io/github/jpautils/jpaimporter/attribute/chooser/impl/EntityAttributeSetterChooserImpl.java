package io.github.jpautils.jpaimporter.attribute.chooser.impl;

import io.github.jpautils.jpaimporter.attribute.chooser.EntityAttributeSetterChooser;
import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityAttributeSetterChooserImpl implements EntityAttributeSetterChooser {

    final List<EntityAttributeSetter> entityAttributeSetterList;

    public EntityAttributeSetterChooserImpl(List<EntityAttributeSetter> entityAttributeSetterList) {
        this.entityAttributeSetterList = new ArrayList<>(entityAttributeSetterList);
        entityAttributeSetterList.sort(Comparator.comparing(EntityAttributeSetter::getPriority));
    }

    @Override
    public EntityAttributeSetter getEntityAttributeSetterFor(EntityAttributeCharacteristicsDto entityAttributeSetterCharacteristics) {

        return entityAttributeSetterList
                .stream()
                .filter(entityAttributeSetter -> entityAttributeSetter.matchesEntityAttribute(entityAttributeSetterCharacteristics))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find entity attribute setter for type: [" + entityAttributeSetterCharacteristics.getSetterType() + "] of entity: [" + entityAttributeSetterCharacteristics.getEntityClass() + "]"));
    }
}
