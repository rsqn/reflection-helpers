package tech.rsqn.reflectionhelpers;

import com.google.common.base.CaseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReflectionHelper {
    static Logger logger = LoggerFactory.getLogger(ReflectionHelper.class);

    public static final String GET = "get";
    public static final String IS = "is";
    public static final String SET = "set";

    public static boolean reflectionEquals(Object a, Object b, boolean sysLog, String... ignore) {
        List<BeanAttribute> attrListA = ReflectionHelper.collectAttributeMetaData(a);
        List<BeanAttribute> attrListB = ReflectionHelper.collectAttributeMetaData(a);

        if (a.getClass() != b.getClass()) {
            logger.debug("Classes are not equal");
            return false;
        }
        if ((a == null && b != null) || (a != null && b == null)) {
            logger.debug("Cannot compare nulls");
            return false;
        }

        boolean ign = false;
        for (BeanAttribute attrA : attrListA) {
            ign = false;
            for (String s : ignore) {
                if (s.toLowerCase().equalsIgnoreCase(attrA.getSimplifiedName())) {
                    ign = true;
                }
            }
            if (ign) {
                logger.debug("Ignore " + attrA.getName());
                continue;
            }
            logger.debug("Comparing [" + attrA.getName() + "]");

            BeanAttribute attrB = null;
            for (BeanAttribute attrBIter : attrListB) {
                if (attrBIter.getName().equals(attrA.getName())) {
                    attrB = attrBIter;
                    break;
                }
            }
            if (attrB == null) {
                logger.debug("Could not find attrB");
                return false;
            }
            Object va = attrA.executeGetter(a);
            Object vb = attrB.executeGetter(b);
            if (va == null && vb != null) {
                logger.debug("InEqual " + attrA.getName() + ":[" + va + " != " + vb + "]");
                return false;
            }
            if (va != null && vb == null) {
                logger.debug("InEqual " + attrA.getName() + ":[" + va + " != " + vb + "]");
                return false;
            }
            if (va == null && vb == null) {
                continue;
            }
            if (TypeUtil.isSimpleType(va.getClass())) {
                if (!va.equals(vb)) {
                    logger.debug("InEqual " + attrA.getName() + ":[" + va + " != " + vb + "]");
                    return false;
                }
            } else {
                if (!reflectionEquals(va, vb, sysLog, ignore)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Class getAttributeType(Object o, String name) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith(GET)) {
                String s = method.getName();
                s = getSimplifiedName(s.substring(GET.length()));
                if (name.equalsIgnoreCase(s)) {
                    try {
                        return method.getReturnType();
                    } catch (java.lang.Exception e) {
                        throw new RuntimeException("exception invoking method " + method, e);
                    }
                }
            }
        }
        return null;
    }

    public static List<Method> getMethodsWithAnnotation(Object o, Class c) {
        Method[] methods;
        if (o instanceof Class) {
            methods = ((Class) o).getMethods();
        } else {
            methods = o.getClass().getMethods();
        }


        List<Method> ret = new ArrayList<Method>();

        for (Method method : methods) {
            if (method.getAnnotation(c) != null) {
                ret.add(method);
            }
        }

        return ret;
    }

    public static List<Method> getGetterMethods(Object o) {
        return getMethodsWithPrefix(o, GET);
    }

    public static List<Method> getSetterMethods(Object o) {
        return getMethodsWithPrefix(o, SET);
    }


    private static List<Method> getMethodsWithPrefix(Object o, String prefix) {
        Method[] methods;
        if (o instanceof Class) {
            methods = ((Class) o).getMethods();
        } else {
            methods = o.getClass().getMethods();
        }


        List<Method> ret = new ArrayList<Method>();

        for (Method method : methods) {
            if (method.getName().startsWith(prefix)) {
                String name = method.getName();
                if (!"getClass".equals(name)) {
                    ret.add(method);
                }

            }
        }

        return ret;
    }

    /**
     * @param o
     * @return
     * @deprecated
     */
    public static List<String> getSimplifiedAttributeNames(Object o) {
        Method[] methods;
        if (o instanceof Class) {
            methods = ((Class) o).getMethods();
        } else {
            methods = o.getClass().getMethods();
        }


        List<String> ret = new ArrayList<String>();

        for (Method method : methods) {
            if (method.getName().startsWith(GET)) {
                String s = method.getName();
                s = getSimplifiedName(s.substring(GET.length()));
                if (!"class".equals(s)) {
                    ret.add(s);
                }

            }
        }
        return ret;
    }

    private static String getSimplifiedName(String name) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    private static String getAttributeName(String name) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
    }

    /**
     * An attribute must have a setter and a getter
     *
     * @param o
     * @return
     */
    public static List<BeanAttribute> collectAttributeMetaData(Object o) {
        Method[] methods;
        if (o instanceof Class) {
            methods = ((Class) o).getMethods();
        } else {
            methods = o.getClass().getMethods();
        }

        List<BeanAttribute> ret = new ArrayList<BeanAttribute>();
        String name;
        String nameLower;

        Map<String, Method> methodMap = new HashMap<String, Method>();
        for (Method method : methods) {
            String key = method.getName();
            if (method.getName().startsWith(GET) || method.getName().startsWith(SET)) {
                methodMap.put(key, method);
            }

        }

        for (Method method : methods) {
            if (method.getName().startsWith(GET) || method.getName().startsWith(IS)) {
                if (method.getName().startsWith(IS)) {
                    name = method.getName().substring(IS.length());
                } else {
                    name = method.getName().substring(GET.length());
                }

                if ("class".equals(name.toLowerCase())) {
                    continue;
                }
                if (name.length() == 0) {
                    // probably stuck int a list or map
                    continue;
                }
//                nameLower = name.toLowerCase();
                nameLower = getSimplifiedName(name);
//                nameLower = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,name);
                BeanAttribute attr = new BeanAttribute();
                attr.setGetterMethod(method);
                attr.setName(name);
                attr.setSimplifiedName(nameLower);
                attr.setType(method.getReturnType());
                attr.setAnnotations(method.getAnnotations());
                attr.setSetterMethod(methodMap.get(SET + name)); //todo: support overridden methods
                ret.add(attr);
            }
        }

        return ret;
    }

    public static Class<?> getSimplifiedAttributeType(Object o, String name) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith(GET) || method.getName().startsWith(IS)) {
                String s = method.getName();
                if (method.getName().startsWith(IS)) {
                    s = getSimplifiedName(s.substring(IS.length()));
                } else {
                    s = getSimplifiedName(s.substring(GET.length()));
                }
                if (name.equalsIgnoreCase(s)) {
                    try {
                        return method.getReturnType();
                    } catch (Exception e) {
                        throw new RuntimeException("exception invoking method " + method, e);
                    }
                }
            }
        }
        return null;
    }


    public static <T> T getAttribute(Object o, String name) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith(GET) || method.getName().startsWith(IS)) {
                String s = method.getName();
                if (method.getName().startsWith(IS)) {
                    s = getSimplifiedName(s.substring(IS.length()));
                } else {
                    s = getSimplifiedName(s.substring(GET.length()));
                }
                if (name.equalsIgnoreCase(s)) {
                    try {
                        return (T) method.invoke(o, new Object[]{});
                    } catch (Exception e) {
                        throw new RuntimeException("exception invoking method " + method, e);
                    }
                }
            }
        }
        return null;
    }


    public static <T> T getAttributeAtPath(Object o, String[] path) {
        Object current = o;

        for (String s : path) {
            current = getAttribute(current, s);
            if (current == null) {
                return null;
            }
        }
        return (T) current;
    }

    public static Map<String, String> getAttributesAsStringMap(Object o, String... names) {
        Map<String, String> ret = new HashMap<String, String>();

        for (String name : names) {
            String v = getAttribute(o, name);
            ret.put(name, v);
        }
        return ret;

    }

    public static void putAttributes(Object o, Map<String, String> src) {
        for (String key : src.keySet()) {
            putAttribute(o, key, src.get(key));
        }
    }

    public static void putAttribute(Object o, String name, Object attribute) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().startsWith(SET)) {
                String s = method.getName();
                s = getSimplifiedName(s.substring(SET.length()));
                if (name.equalsIgnoreCase(s)) {
                    try {
                        Object converted = ConversionUtil.convertForSetter(method, attribute);
                        method.invoke(o, new Object[]{converted});
                        return;
                    } catch (Exception e) {
                        throw new RuntimeException("exception invoking method " + method, e);
                    }
                }
            }
        }
    }

    public static Object callGetterForProperty(Object o, String propertyName)
            throws InvocationTargetException, IllegalAccessException {
        return callGetterForPropertyInternal(o, propertyName, true);
    }

    /**
     * Calls a getter for a property name, expects case to be correct except for first character
     *
     * @param o
     * @param propertyName
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object callGetterForPropertyWithoutExceptionSuppression(
            Object o, String propertyName)
            throws InvocationTargetException, IllegalAccessException {
        return callGetterForPropertyInternal(o, propertyName, false);
    }


    private static Object callGetterForPropertyInternal(Object o,
                                                        String propertyName, boolean suppressMethodNotFound)
            throws InvocationTargetException, IllegalAccessException {
        String targetName = "" + propertyName.toUpperCase().charAt(0);

        if (propertyName.length() > 1) {
            targetName += propertyName.substring(1);
        }

        targetName = "get" + targetName;

//        Method m = ReflectionUtils.findMethod(o.getClass(), targetName);
        Method m = findFirstMethodWithName(o, targetName);

        if (m != null) {
            return m.invoke(o, (Object[]) null);
        } else {
            if (!suppressMethodNotFound) {
                throw new ReflectionException(
                        "No getter for property " + propertyName + " found on " +
                                o.getClass());
            }
        }

        return null;
    }

    /**
     * Returns the first method found with the given name, irrespective of parameters
     *
     * @param o
     * @param name
     * @return
     */
    public static Method findFirstMethodWithName(Object o, String name) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        return null;
    }

    public static Method findMethodWithParameters(Object o, String name,
                                                  Class<?>[] params) {
        Method[] methods = o.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().equals(name)) {
                Class<?>[] c = method.getParameterTypes();

                if (c.length == params.length) {
                    logger.debug("finding method " + name +
                            " params length match " + c.length);

                    for (int i = 0; i < c.length; i++) {
                        Class<?> methodParam = c[i];
                        Class<?> comparisonParam = params[i];
                        logger.debug("finding method " + name + " compare param " +
                                i + " methodParam = " + methodParam +
                                " comparisonParam = " + comparisonParam);

                        if (!methodParam.isAssignableFrom(comparisonParam)) {
                            logger.debug(" not assignable " + methodParam +
                                    " from " + comparisonParam);

                            break;
                        }

                        if (i == (c.length - 1)) {
                            return method;
                        }
                    }
                }
            }
        }

        return null;
    }

    private static Method getFirstMethodWithName(Object o, String name) {
        Method[] methods;
        if (o instanceof Class) {
            methods = ((Class) o).getMethods();
        } else {
            methods = o.getClass().getMethods();
        }


        List<Method> ret = new ArrayList<Method>();

        for (Method method : methods) {
            if (method.getName().equals(name)) {
                String n = method.getName();
                if (!"getClass".equals(n)) {
                    return method;
                }

            }
        }

        return null;
    }

    public static <T> T getFieldValueAtPath(Object o, String... path) {
        Object current = o;
        for (int i = 0; i < path.length; i++) {
            String n = path[i];
            current = getFieldValue(current, n);
            if (i == path.length - 1) {
                return (T) current;
            } else {
                if (current == null) {
                    return null;
                }
            }
        }
        return null;
    }

    public static <T> T getFieldValue(Object o, String name) {
        try {
            Field f = o.getClass().getDeclaredField(name);
            if (f == null) {
                throw new RuntimeException("Exception no field " + name + " on " + o.getClass());
            }
            f.setAccessible(true);
            T ret = (T) f.get(o);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("Exception fetching field value  " + name + " on " + o.getClass() + e, e);
        }
    }

    public static <T> T getFieldValueForClass(Class c, Object o, String name) {
        try {
            Field f = c.getDeclaredField(name);
            if (f == null) {
                throw new RuntimeException("Exception no field " + name + " on " + c + " impl " + o.getClass());
            }
            f.setAccessible(true);
            T ret = (T) f.get(o);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException("Exception fetching field value  " + name + " on " + o.getClass() + e, e);
        }
    }

    public static Object executeMethod(Object o, String method, Object... args) {
        Method m = null;

        try {
            if ( o == null ) {
                throw new Exception("Object is null");
            }

            m = getFirstMethodWithName(o, method);
            m.setAccessible(true);

            if ( m == null ) {
                throw new Exception("No method with name " + method + " in " + o.getClass());
            }

            return m.invoke(o, args);
        } catch (IllegalArgumentException iae) {
            logger.error("IllegalArgumentException " + iae, iae);
            logger.info("Method signature " + m.toString());

            List<String> argTypes = new ArrayList<>();
            for (Object arg : args) {
                if ( arg != null ) {
                    argTypes.add(arg.getClass().getName());
                } else {
                    argTypes.add("null");
                }
            }
            logger.info("Argument types " + argTypes);
            throw new RuntimeException("Exception executing method " + method + " on " + o.getClass() + iae, iae);
        } catch (Exception e) {
            throw new RuntimeException("Exception executing method " + method + " on " + o.getClass() + e, e);
        }
    }

    public static boolean isPrimitiveOrStringOrWrapper(Object o) {
        if (o == null) {
            return true;
        }

        return isPrimitiveOrStringOrWrapperClass(o.getClass());
    }

    public static boolean isPrimitiveOrStringOrWrapperClass(Class<?> clazz) {
        if (isWrapperType(clazz)) {
            return true;
        } else if (clazz.isPrimitive()) {
            return true;
        } else if (clazz.equals(String.class)) {
            return true;
        }

        return false;
    }

    public static boolean isWrapperType(Class<?> clazz) {
        return clazz.equals(Boolean.class) || clazz.equals(Integer.class) ||
                clazz.equals(Character.class) || clazz.equals(Byte.class) ||
                clazz.equals(Short.class) || clazz.equals(Double.class) ||
                clazz.equals(Long.class) || clazz.equals(Float.class);
    }
}
