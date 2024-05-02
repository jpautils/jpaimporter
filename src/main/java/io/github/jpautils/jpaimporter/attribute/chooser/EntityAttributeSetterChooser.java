package io.github.jpautils.jpaimporter.attribute.chooser;

import io.github.jpautils.jpaimporter.attribute.setter.EntityAttributeSetter;
import io.github.jpautils.jpaimporter.dto.entity.EntityAttributeCharacteristicsDto;

public interface EntityAttributeSetterChooser {
    EntityAttributeSetter getEntityAttributeSetterFor(EntityAttributeCharacteristicsDto entityAttributeSetterCharacteristics);
}
