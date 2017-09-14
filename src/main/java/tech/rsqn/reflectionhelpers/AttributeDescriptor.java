package tech.rsqn.reflectionhelpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AttributeDescriptor {

    private Method getterMethod;
    private Method setterMethod;
    private String name;
    private String simplifiedName;
    private Class<?> type;
    private List<Annotation> annotations;

    public boolean hasAnnotation(Class cmp) {
        if ( annotations == null) {
            return false;
        }
        for (Annotation annotation : annotations) {
            if ( annotation.annotationType().equals(cmp)) {
                return true;
            }
        }
        return false;
    }

    public Annotation getAnnotation(Class cmp) {
        if ( annotations == null) {
            return null;
        }
        for (Annotation annotation : annotations) {
            if ( annotation.annotationType().equals(cmp)) {
                return annotation;
            }
        }
        return null;
    }


    public Object executeGetter(Object o) {
        try {
            return getGetterMethod().invoke(o,new Object[]{});
        } catch (Exception e) {
            throw new ReflectionException("Exception fetching attribute " + name,e);
        }
    }

    public void executeSetter(Object o, Object value) {
        try {
            Object converted = ConversionUtil.convertForSetter(getSetterMethod(), value);
            getSetterMethod().invoke(o, new Object[]{converted});
        } catch (Exception e) {
            throw new ReflectionException("Exception fetching attribute " + name,e);
        }
    }
    
    public Method getGetterMethod() {
        return getterMethod;
    }

    public void setGetterMethod(Method getterMethod) {
        this.getterMethod = getterMethod;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public void setSetterMethod(Method setterMethod) {
        this.setterMethod = setterMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimplifiedName() {
        return simplifiedName;
    }

    public void setSimplifiedName(String simplifiedName) {
        this.simplifiedName = simplifiedName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] list) {
        this.annotations = new ArrayList();
        Collections.addAll(this.annotations,list);
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return "AttributeDescriptor{" +
                "getterMethod=" + getterMethod +
                ", setterMethod=" + setterMethod +
                ", name='" + name + '\'' +
                ", simplifiedName='" + simplifiedName + '\'' +
                ", type=" + type +
                ", annotations=" + annotations +
                '}';
    }
}
