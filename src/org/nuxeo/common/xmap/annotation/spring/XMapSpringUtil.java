/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2007 All Rights Reserved.
 */
package org.nuxeo.common.xmap.annotation.spring;

import org.nuxeo.common.xmap.DOMHelper;
import org.nuxeo.common.xmap.XAnnotatedMember;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

/**
 * XMapSpring util类
 * @author xi.hux@alipay.com
 *
 * @version $Id$
 */
public class XMapSpringUtil {

    @SuppressWarnings("unchecked")
    public static Object getSpringObject(Class type, String beanName,
                                         ApplicationContext applicationContext) {
        //处理spring的资源对象
        if (type == Resource.class) {
            //返回一个spring类型的值，但是如果是带有属性的path返回的就是属性的值
            //如果是是个node path 返回的就是这个节点的内容
            return applicationContext.getResource(beanName);
        } else {
            return applicationContext.getBean(beanName, type);
        }
    }

    public static Object getSpringOjbect(XAnnotatedMember xam,
                                         ApplicationContext applicationContext, Element base) {
        String val = DOMHelper.getNodeValue(base, xam.path);
        if (val != null && val.length() > 0) {
            if (xam.trim) {
                val = val.trim();
            }
            return getSpringObject(xam.type, val, applicationContext);
        }
        return null;
    }
}
