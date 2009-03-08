/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.lang.reflect.Field;

/**
 * 用来获得<code>Contribution</code>中在<code>Field</code>上标注<code>Annotation</code>
 * 对应值得的<code>getter</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XFiledGetter.java,v 0.1 2008-4-17 上午09:51:54 xi.hux Exp $
 */

public class XFieldGetter implements XGetter {

    /** field */
    private Field field;

    /**
     * 构建一个<code>XFieldGetter</code>对象
     * 
     * @param field <code>Field</code>实例
     */
    public XFieldGetter(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * @see org.nuxeo.common.xmap.XGetter#getType()
     */
    public Class<?> getType() {
        return field.getType();
    }

    /**
     * @see org.nuxeo.common.xmap.XGetter#getValue(java.lang.Object)
     */
    public Object getValue(Object instance) throws Exception {
        if (instance == null) {
            return null;
        }
        return field.get(instance);
    }

}
