/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <code>XAnnotatedParent</code>对象，对应<code>XParent</code><code>Annotation</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedParent.java,v 0.1 2008-4-17 上午10:23:06 xi.hux Exp $
 */
public class XAnnotatedParent extends XAnnotatedMember {

    /**
     * 构建一个<code>XAnnotatedParent</code>对象
     * 
     * @param xmap <code>XMap</code>对象
     * @param setter <code>XSetter<code>对象
     * @param getters <code>XGetter<code>对象
     */
    protected XAnnotatedParent(XMap xmap, XSetter setter, XGetter getter) {
        super(xmap, setter, getter);
    }

    @Override
    protected Object getValue(Context ctx, Element base) throws Exception {
        return ctx.getParent();
    }

    @Override
    public void decode(Object instance, Node base, Document document, List<String> filters)
                                                                                           throws Exception {
        // do nothing
    }

}
