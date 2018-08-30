package com.haiwen.smart.framework.helper;

import com.haiwen.smart.framework.annotation.Inject;
import com.haiwen.smart.framework.util.ArrayUtil;
import com.haiwen.smart.framework.util.CollectionUtil;
import com.haiwen.smart.framework.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class IocHelper {
    static {
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if (CollectionUtil.isNotEmpty((Collection<?>)beanMap)) {
            for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
                Class<?> beanClass = beanEntry.getKey();
                Object beanInstance = beanEntry.getValue();

                Field[] beanFields = beanClass.getDeclaredFields();

                if(ArrayUtil.isNotEmpty(beanFields)) {
                    for (Field beanField : beanFields) {
                        if (beanField.isAnnotationPresent(Inject.class)) {
                            Class<?> beanFieldClass = beanField.getType();
                            Object beanFieldInstance = beanMap.get(beanFieldClass);
                            if(beanFieldInstance != null) {
                                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                            }
                        }
                    }
                }
            }
        }
    }
}
