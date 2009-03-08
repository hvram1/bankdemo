/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <code>XAnnotatedMap</code>对象，对应<code>XNodeMap</code><code>Annotation</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedMap.java,v 0.1 2008-4-17 上午10:21:15 xi.hux Exp $
 */
public class XAnnotatedMap extends XAnnotatedList {

    protected static final ElementMapVisitor        elementMapVisitor = new ElementMapVisitor();
    protected static final ElementValueMapVisitor   elementVisitor    = new ElementValueMapVisitor();
    protected static final AttributeValueMapVisitor attributeVisitor  = new AttributeValueMapVisitor();

    protected Path                                  key;

    /**
     * 构建一个<code>XAnnotatedMap</code>对象
     * 
     * @param xmap <code>XMap</code>对象
     * @param setter <code>XSetter</code>对象
     * @param getter <code>XGetter</code>对象
     * @param anno <code>XNodeMap</code>对象
     */
    public XAnnotatedMap(XMap xmap, XSetter setter, XGetter getter, XNodeMap anno) {
        super(xmap, setter, getter);
        if (anno != null) {
            path = new Path(anno.value());
            trim = anno.trim();
            key = new Path(anno.key());
            type = anno.type();
            cdata = anno.cdata();
            componentType = anno.componentType();
            valueFactory = xmap.getValueFactory(componentType);
            xao = xmap.register(componentType);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object getValue(Context ctx, Element base) throws IllegalAccessException,
                                                        InstantiationException {
        Map<String, Object> values = (Map) type.newInstance();
        if (xao != null) {
            DOMHelper.visitMapNodes(ctx, this, base, path, elementMapVisitor, values);
        } else {
            if (path.attribute != null) {
                // attribute list
                DOMHelper.visitMapNodes(ctx, this, base, path, attributeVisitor, values);
            } else {
                // element list
                DOMHelper.visitMapNodes(ctx, this, base, path, elementVisitor, values);
            }
        }

        return values;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void decode(Object instance, Node base, Document document, List<String> filters)
                                                                                           throws Exception {
        if (!isFilter(filters)){
            return;
        }
        
        Map values = (Map) getter.getValue(instance);

        Node node = base;
        int len = path.segments.length - 1;
        for (int i = 0; i < len; i++) {
            // 先取得对应的Node对象
            Node n = DOMHelper.getElementNode(node, path.segments[i]);
            // 没有创建对应的Node对象
            if (n == null) {
                Element element = document.createElement(path.segments[i]);
                node = node.appendChild(element);
            } else {
                node = n;
            }
        }

        //需要循环创建的节点名称
        String name = path.segments[len];

        Node lastParentNode = node;

        Set<Map.Entry> entrys = values.entrySet();
        for (Map.Entry entry : entrys) {

            Element element = document.createElement(name);
            node = lastParentNode.appendChild(element);

            Object keyObj = entry.getKey();
            String keyValue = keyObj == null ? "" : keyObj.toString();

            Object object = entry.getValue();

            // 设置 key attribute
            Attr attrKey = document.createAttribute(key.attribute);
            attrKey.setNodeValue(keyValue);
            ((Element) node).setAttributeNode(attrKey);

            if (xao != null) {
                xao.decode(object, node, document, filters);
            } else {
                String value = object == null ? "" : object.toString();

                // 如果存在属性
                if (path.attribute != null && path.attribute.length() > 0) {
                    Attr attrValue = document.createAttribute(path.attribute);
                    attrValue.setNodeValue(value);

                    ((Element) node).setAttributeNode(attrValue);
                } else {
                    if (cdata) {
                        CDATASection cdataSection = document.createCDATASection(value);
                        node.appendChild(cdataSection);
                    } else {
                        node.setTextContent(value);
                    }
                }
            }
        }
    }

}

/**
 * <code>ElementMap</code><code>NodeVisitor</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedMap.java,v 0.1 2008-4-17 上午10:21:50 xi.hux Exp $
 */
class ElementMapVisitor extends DOMHelper.NodeMapVisitor {
    @Override
    public void visitNode(Context ctx, XAnnotatedMember xam, Node node, String key,
                          Map<String, Object> result) {
        try {
            result.put(key, xam.xao.newInstance(ctx, (Element) node));
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }
}

/**
 * <code>ElementValueMap</code><code>NodeVisitor</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedMap.java,v 0.1 2008-4-17 上午10:22:09 xi.hux Exp $
 */
class ElementValueMapVisitor extends DOMHelper.NodeMapVisitor {
    @Override
    public void visitNode(Context ctx, XAnnotatedMember xam, Node node, String key,
                          Map<String, Object> result) {
        String val = node.getTextContent();
        if (xam.trim) {
            val = val.trim();
        }
        if (xam.valueFactory != null) {
            result.put(key, xam.valueFactory.getValue(ctx, val));
        } else {
            // TODO: log warning?
            result.put(key, val);
        }
    }
}

/**
 * <code>AttributeValueMap</code><code>NodeVisitor</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XAnnotatedMap.java,v 0.1 2008-4-17 上午10:22:18 xi.hux Exp $
 */
class AttributeValueMapVisitor extends DOMHelper.NodeMapVisitor {
    @Override
    public void visitNode(Context ctx, XAnnotatedMember xam, Node node, String key,
                          Map<String, Object> result) {
        String val = node.getNodeValue();
        if (xam.valueFactory != null) {
            result.put(key, xam.valueFactory.getValue(ctx, val));
        } else {
            // TODO: log warning?
            result.put(key, val);
        }
    }
}
