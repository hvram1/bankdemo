/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2007 All Rights Reserved.
 */
package org.nuxeo.common.xmap.annotation.spring;

import org.nuxeo.common.xmap.Context;
import org.nuxeo.common.xmap.Path;
import org.nuxeo.common.xmap.XAnnotatedMember;
import org.nuxeo.common.xmap.XGetter;
import org.nuxeo.common.xmap.XMap;
import org.nuxeo.common.xmap.XSetter;
import org.nuxeo.common.xmap.spring.XNodeSpring;
import org.w3c.dom.Element;

/**
 * 表示<code>XNodeSpring</code><code>Annotation</code>的对象
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedSpring.java,v 0.1 2008-4-17 上午10:37:17 xi.hux Exp $
 */
public class XAnnotatedSpring extends XAnnotatedMember {
    /**
     * 包含他的XAnnotatedSpringObject对象！
     * 主要是这个对象包含了Spring ioc 容器实例
     */
    public XAnnotatedSpringObject xaso;

    /**
     * 构建一个<code>XAnnotationSpring</code>对象 
     */
    protected XAnnotatedSpring(XMap xmap, XSetter setter, XGetter getter) {
        super(xmap, setter, getter);
    }

    /**
     * 构建一个<code>XAnnotatedSpring</code>对象
     * 
     * @param xmap <code>XMap</code>对象
     * @param setter <code>XSetter</code>对象
     * @param getter <code>XGetter</code>对象
     * @param anno <code>XNodeSrping</code>对象
     * @param xaso <code>XAnnotatedSpringObject</coed>对象
     */
    public XAnnotatedSpring(XMap xmap, XSetter setter, XGetter getter, XNodeSpring anno,
                            XAnnotatedSpringObject xaso) {
        super(xmap, setter, getter);
        path = new Path(anno.value());
        trim = anno.trim();
        type = setter.getType();
        this.xaso = xaso;
    }

    @Override
    protected Object getValue(Context ctx, Element base) throws Exception {
        // scalar field
        if (type == Element.class) {
            // allow DOM elements as values
            return base;
        }
        //从Spring容器中取得对应的bean或者Resource对象
        return XMapSpringUtil.getSpringOjbect(this, xaso.getApplicationContext(), base);
    }
}