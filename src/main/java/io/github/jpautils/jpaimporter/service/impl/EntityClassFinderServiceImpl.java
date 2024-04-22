package io.github.jpautils.jpaimporter.service.impl;

import io.github.jpautils.jpaimporter.service.EntityClassFinderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Type;

public class EntityClassFinderServiceImpl implements EntityClassFinderService {

    private final EntityManager entityManager;

    public EntityClassFinderServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Class<?> getEntityClassForName(String name) {
        //TODO: Check if this operation is expensive and needs to be cached.
        //TODO: If search by name unsuccessful, search by full name (including package) as well before throwing error.
        Class<?> entityClass = entityManager.getMetamodel().getEntities()
                .stream()
                .filter(entityType -> entityType.getName().equalsIgnoreCase(name))
                .findFirst()
                .map(Type::getJavaType)
                .orElseThrow(() -> new RuntimeException("Could not find an entity with name: [" + name + "]."));

        return entityClass;
    }
}
