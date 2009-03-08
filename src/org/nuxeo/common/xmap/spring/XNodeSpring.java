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
 * 代表Spring节点的XMap对象
 * @author xi.hux@alipay.com
 *
 * @version $Id$
 */
@XMemberAnnotation(XMemberAnnotation.NODE_SPRING)
@Target( { ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface XNodeSpring {

    /**
     * 与xml节点绑定的path
     * @return
     */
    String value();

    /**
     * 是否需要trim.
     *
     * @return
     */
    boolean trim() default true;

    /**
     * 需要从Spring容器中取得的类型
     * 暂时不用，保留
     *
     * @return the type of items
     */
    @SuppressWarnings("unchecked")
    Class type() default Object.class;

}
