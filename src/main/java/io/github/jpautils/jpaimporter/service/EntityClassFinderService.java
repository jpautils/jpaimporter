package io.github.jpautils.jpaimporter.service;

public interface EntityClassFinderService {
    Class<?> getEntityClassForName(String name);

    Object createEntityInstanceForClass(Class<?> entityClass);
}
