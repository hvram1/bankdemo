/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2007 All Rights Reserved.
 */
package org.nuxeo.common.xmap.annotation.spring;

import org.nuxeo.common.xmap.XAnnotatedObject;
import org.nuxeo.common.xmap.XMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.springframework.context.ApplicationContext;

/**
 * 支持Spring的XAnnotatedObject对象，持有Spring的BeanFactory对象
 * @author xi.hux@alipay.com
 *
 * @version $Id$
 */
public class XAnnotatedSpringObject extends XAnnotatedObject {

    /**
     * Spring 容器对象
     */
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    public XAnnotatedSpringObject(XMap xmap, Class klass, XObject xob,
                                  ApplicationContext applicationContext) {
        super(xmap, klass, xob);
        this.applicationContext = applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
