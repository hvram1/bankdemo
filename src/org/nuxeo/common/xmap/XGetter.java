/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

/**
 * 用来获得<code>Contribution</code>中对应值得的<code>getter</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XGetter.java,v 0.1 2008-4-17 上午09:40:53 xi.hux Exp $
 */

public interface XGetter {
    
    /**
     * 返回getter对应的<coede>Class</code>对象
     *
     * @return <code>Class</code>
     */
    Class<?> getType();

    /**
     * 返回getter中包含的值
     *
     * @param <code>Contribution</code>对象
     * @throws Exception 
     */
    Object getValue(Object instance) throws Exception;
}
