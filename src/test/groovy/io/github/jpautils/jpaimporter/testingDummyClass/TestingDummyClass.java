package io.github.jpautils.jpaimporter.testingDummyClass;

import java.util.List;

public class TestingDummyClass {
    public String attribute1;

    public List<String> parametrizedTypeWithNoGetSetMethods;

    public List<String> parametrizedTypeWithSetMethod;

    public List<String> parametrizedTypeWithGetMethod;

    public List<String> parametrizedTypeWithGetSetMethods;

    public String typeWithNoGetSetMethods;

    public String typeWithSetMethod;

    public String typeWithGetMethod;

    public String typeWithGetSetMethods;

    public List<String> getParametrizedTypeWithGetMethod() {
        return parametrizedTypeWithGetMethod;
    }

    public List<String> getParametrizedTypeWithGetSetMethods() {
        return parametrizedTypeWithGetSetMethods;
    }

    public String getTypeWithGetMethod() {
        return typeWithGetMethod;
    }

    public String getTypeWithGetSetMethods() {
        return typeWithGetSetMethods;
    }

    public void setParametrizedTypeWithSetMethod(List<String> parametrizedTypeWithSetMethod) {
        this.parametrizedTypeWithSetMethod = parametrizedTypeWithSetMethod;
    }

    public void setParametrizedTypeWithGetSetMethods(List<String> parametrizedTypeWithGetSetMethods) {
        this.parametrizedTypeWithGetSetMethods = parametrizedTypeWithGetSetMethods;
    }

    public void setTypeWithSetMethod(String typeWithSetMethod) {
        this.typeWithSetMethod = typeWithSetMethod;
    }

    public void setTypeWithGetSetMethods(String typeWithGetSetMethods) {
        this.typeWithGetSetMethods = typeWithGetSetMethods;
    }
}


