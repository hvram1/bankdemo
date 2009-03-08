/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2007 All Rights Reserved.
 */
package org.nuxeo.common.xmap.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.nuxeo.common.xmap.annotation.XMemberAnnotation;

/**
 * 代表Spring map节点的XMap对象
 * @author xi.hux@alipay.com
 *
 * @version $Id$
 */
@XMemberAnnotation(XMemberAnnotation.NODE_MAP_SPRING)
@Target( { ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeMapSpring {

    /**
     * 节点的路径
     *
     * @return the node xpath
     */
    String value();

    /**
     * 是否需要trim内容
     *
     * @return
     */
    boolean trim() default true;

    /**
     * 当前节点路径(由 {@link XNodeMapSpring#value()}定位)相关的Map key 将被使用
     * 
     * @return
     */
    String key();

    /**
     * collection类型
     *
     * @return the type of items
     */
    @SuppressWarnings("unchecked")
    Class type();

    /**
     * 在collection中的对象类型，应该为Spring bean对应的类型
     *
     * @return the type of items
     */
    @SuppressWarnings("unchecked")
    Class componentType();
}
