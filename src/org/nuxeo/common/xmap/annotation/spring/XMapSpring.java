/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2007 All Rights Reserved.
 */
package org.nuxeo.common.xmap.annotation.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.nuxeo.common.xmap.XAnnotatedMember;
import org.nuxeo.common.xmap.XAnnotatedObject;
import org.nuxeo.common.xmap.XFieldGetter;
import org.nuxeo.common.xmap.XFieldSetter;
import org.nuxeo.common.xmap.XGetter;
import org.nuxeo.common.xmap.XMap;
import org.nuxeo.common.xmap.XMethodGetter;
import org.nuxeo.common.xmap.XMethodSetter;
import org.nuxeo.common.xmap.XSetter;
import org.nuxeo.common.xmap.annotation.XMemberAnnotation;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.common.xmap.spring.XNodeListSpring;
import org.nuxeo.common.xmap.spring.XNodeMapSpring;
import org.nuxeo.common.xmap.spring.XNodeSpring;
import org.springframework.context.ApplicationContext;

/**
 * 对原有的XMap对象进行与Spring结合方面的扩展对象
 * 
 * @author xi.hux@alipay.com
 *
 * @version $Id$
 */
public class XMapSpring extends XMap {

    @SuppressWarnings("unchecked")
    public XAnnotatedObject register(Class klass, ApplicationContext applicationContext) {
        XAnnotatedObject xao = objects.get(klass);
        if (xao == null) { // avoid scanning twice
            XObject xob = checkObjectAnnotation(klass);
            if (xob != null) {
                xao = new XAnnotatedSpringObject(this, klass, xob, applicationContext);
                objects.put(xao.klass, xao);
                scan(xao);
                String key = xob.value();
                if (key.length() > 0) {
                    roots.put(xao.path.path, xao);
                }
            }
        }
        return xao;
    }

    /**
     * 扫描所有Field
     * @param xob
     */
    @SuppressWarnings("unchecked")
    private void scan(XAnnotatedObject xob) {
        Field[] fields = xob.klass.getDeclaredFields();
        for (Field field : fields) {
            Annotation anno = checkMemberAnnotation(field);
            if (anno != null) {
                XAnnotatedMember member = createFieldMember(field, anno);
                //如果返回空，进行Spring的Annotation查找
                if (member == null) {
                    member = createExtendFieldMember(field, anno, xob);
                }
                xob.addMember(member);
            }
        }

        Method[] methods = xob.klass.getDeclaredMethods();
        for (Method method : methods) {
            // we accept only methods with one parameter
            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1) {
                continue;
            }
            Annotation anno = checkMemberAnnotation(method);
            if (anno != null) {
                XAnnotatedMember member = createMethodMember(method, xob.klass, anno);
                //如果返回空，进行Spring的Annotation查找
                if (member == null) {
                    member = createExtendMethodMember(method, anno, xob);
                }
                xob.addMember(member);
            }
        }
    }

    private XAnnotatedMember createExtendFieldMember(Field field, Annotation annotation,
                                                     XAnnotatedObject xob) {
        XSetter setter = new XFieldSetter(field);
        XGetter getter = new XFieldGetter(field);
        return createExtendMember(annotation, setter, getter, xob);
    }

    public final XAnnotatedMember createExtendMethodMember(Method method, Annotation annotation,
                                                           XAnnotatedObject xob) {
        XSetter setter = new XMethodSetter(method);
        // TODO XSpringMap 暂时不支持toXml
        XGetter getter = new XMethodGetter(null, null, null);
        return createExtendMember(annotation, setter, getter, xob);
    }

    /**
     * 创建扩张 annotation 
     * @param annotation
     * @param setter
     * @param xob
     * @return
     */
    private XAnnotatedMember createExtendMember(Annotation annotation, XSetter setter,
                                                XGetter getter, XAnnotatedObject xob) {
        XAnnotatedMember member = null;
        int type = annotation.annotationType().getAnnotation(XMemberAnnotation.class).value();
        if (type == XMemberAnnotation.NODE_SPRING) {
            member = new XAnnotatedSpring(this, setter, getter, (XNodeSpring) annotation,
                (XAnnotatedSpringObject) xob);
        } else if (type == XMemberAnnotation.NODE_LIST_SPRING) {
            member = new XAnnotatedListSpring(this, setter, getter, (XNodeListSpring) annotation,
                (XAnnotatedSpringObject) xob);
        } else if (type == XMemberAnnotation.NODE_MAP_SPRING) {
            member = new XAnnotatedMapSpring(this, setter, getter, (XNodeMapSpring) annotation,
                (XAnnotatedSpringObject) xob);
        }
        return member;
    }
}
