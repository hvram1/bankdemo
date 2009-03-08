/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.lang.reflect.Method;

/**
 * 用来获得<code>Contribution</code>中在<code>Field</code>上标注<code>Annotation</code>
 * 对应值得的<code>getter</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XMethodGetter.java,v 0.1 2008-4-17 上午10:02:06 xi.hux Exp $
 */

public class XMethodGetter implements XGetter {

    /** method */
    private Method method;
    
    /** class */
    private Class<?> clazz;
    
    /** bean name */
    private String name;

    /**
     * 构建一个<code>XMethodGetter</code>对象
     * 
     * @param method <code>Method</code>实例
     * @param clazz <code>Class</code>实例
     * @param name bean name
     */
    public XMethodGetter(Method method,Class<?> clazz ,String name) {
        this.method = method;
        this.clazz = clazz;
        this.name = name;
    }

    /**
     * @see org.nuxeo.common.xmap.XGetter#getType()
     */
    public Class<?> getType() {
        if(method == null){
            throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
        }
        
        return method.getReturnType();
    }

    /**
     * @see org.nuxeo.common.xmap.XGetter#getValue(java.lang.Object)
     */
    public Object getValue(Object instance) throws Exception {
        if(method == null){
            throw new IllegalArgumentException("no such getter method: " + clazz.getName() + '.' + name);
        }
        
        if (instance == null) {
            return null;
        }
        return method.invoke(instance, new Object[0]);
    }

}
