package io.github.jpautils.jpaimporter.service.impl;

import io.github.jpautils.jpaimporter.service.EntityClassFinderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

    @Override
    public Object createEntityInstanceForClass(Class<?> entityClass) {
        try {
            Constructor<?> constructor = entityClass.getConstructor();
            Object instance = constructor.newInstance();

            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException("Could not create an instance of class: [" + entityClass + "].", exception);
        }
    }
}
