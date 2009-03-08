/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.lang.reflect.Field;

/**
 * 用来设置<code>Contribution</code>中在<code>Field</code>上标注<code>Annotation</code>
 * 对应值得的<code>setter</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XFieldSetter.java,v 0.1 2008-4-17 下午01:31:32 xi.hux Exp $
 */
public class XFieldSetter implements XSetter {

    /** field */
    private final Field field;

    /**
     * 构建一个<code>XFieldSetter</code>对象
     * 
     * @param field <code>Field</code>对象
     */
    public XFieldSetter(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * @see org.nuxeo.common.xmap.XSetter#getType()
     */
    public Class<?> getType() {
        return field.getType();
    }

    /**
     * @see org.nuxeo.common.xmap.XSetter#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object instance, Object value) throws IllegalAccessException {
        field.set(instance, value);
    }
}
