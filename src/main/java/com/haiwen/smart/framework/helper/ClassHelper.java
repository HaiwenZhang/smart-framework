package com.haiwen.smart.framework.helper;

import com.haiwen.smart.framework.annotation.Controller;
import com.haiwen.smart.framework.annotation.Service;
import com.haiwen.smart.framework.util.ClassUtil;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class ClassHelper {

    private static final Set<Class<?>> CLASS_SET;

    static {
        String basePackage = ConfigHelper.getAppBasePackage();
        CLASS_SET = ClassUtil.getClassSet(basePackage);
    }

    public static Set<Class<?>> getClassSet() {
        return CLASS_SET;
    }

    public static Set<Class<?>> getServiceClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();

        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Service.class)) {
                classSet.add(cls);
            }
        }

        return classSet;
    }

    public static Set<Class<?>> getControllerClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();

        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Controller.class)) {
                classSet.add(cls);
            }
        }

        return classSet;
    }

    public static Set<Class<?>> getBeanClassSet() {
        Set<Class<?>> beanClassSet = new HashSet<Class<?>>();

        beanClassSet.addAll(getServiceClassSet());
        beanClassSet.addAll(getControllerClassSet());

        return beanClassSet;
    }

    /**
     * 获取应用包名下某父类(或者接口)的所有子类(或者实现类)
     * @param supperClass
     * @return
     */
    public static Set<Class<?>> getClassSetBySuper(Class<?> supperClass) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();

        for (Class<?> cls : CLASS_SET) {
            if (supperClass.isAssignableFrom(cls) && !supperClass.equals(cls)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    /**
     * 获取应用包名下带某注解的所有类
     * @param annotationClass
     * @return
     */
    public static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
        Set<Class<?>> classSet = new HashSet<>();

        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(annotationClass)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }
}
