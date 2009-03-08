/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <code>XObject</code>对应的对象
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedObject.java,v 0.1 2008-4-21 上午08:01:37 xi.hux Exp $
 */
public class XAnnotatedObject {

    public final XMap            xmap;
    public final Class<?>        klass;
    public final Path            path;

    final List<XAnnotatedMember> members;

    Sorter                       sorter;

    Sorter                       deSorter;

    public XAnnotatedObject(XMap xmap, Class<?> klass, XObject xob) {
        this.xmap = xmap;
        this.klass = klass;
        path = new Path(xob.value());
        members = new ArrayList<XAnnotatedMember>();
        String[] order = xob.order();
        if (order.length > 0) {
            sorter = new Sorter(order);
        }
    }

    public void addMember(XAnnotatedMember member) {
        members.add(member);
    }

    public Path getPath() {
        return path;
    }

    public Object newInstance(Context ctx, Element element) throws Exception {
        Object ob = klass.newInstance();
        ctx.push(ob);

        if (sorter != null) {
            Collections.sort(members, sorter);
            deSorter = sorter;
            sorter = null; // sort only once
        }

        // set annotated members
        for (XAnnotatedMember member : members) {
            member.process(ctx, element);
        }

        return ctx.pop();
    }

    public Object newInstance(Context ctx, Map<String, Object> map, String keyPrefix) throws Exception {
        Object ob = klass.newInstance();
        ctx.push(ob);

        // set annotated members
        for (XAnnotatedMember member : members) {
            member.process(ctx, map, keyPrefix);
        }

        return ctx.pop();
    }

    public void decode(Object instance, Node base, Document document, List<String> filters)
                                                                                           throws Exception {

        Node node = base;
        String name = path.path;

        // 进行排序,防止xml->ojbect没有执行
        if (sorter != null) {
            deSorter = sorter;
        }

        if (deSorter != null) {
            Collections.sort(members, deSorter);
            deSorter = null; // sort only once
        }

        // 添加XObject.name对应的element
        if (name != null && name.length() > 0) {
            Element element = document.createElement(name);
            node = node.appendChild(element);
        }

        // 取得所有的member进行decode操作
        for (XAnnotatedMember annotatedMember : members) {
            annotatedMember.decode(instance, node, document, filters);
        }
    }
}

class Sorter implements Comparator<XAnnotatedMember> {

    private final Map<String, Integer> order = new HashMap<String, Integer>();

    Sorter(String[] order) {
        for (int i = 0; i < order.length; i++) {
            this.order.put(order[i], i);
        }
    }

    public int compare(XAnnotatedMember o1, XAnnotatedMember o2) {
        Integer order1 = order.get(o1.path.path);
        Integer order2 = order.get(o2.path.path);
        int n1 = order1 == null ? Integer.MAX_VALUE : order1;
        int n2 = order2 == null ? Integer.MAX_VALUE : order2;
        return n1 - n2;
    }

}
