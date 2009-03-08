package org.nuxeo.common.xmap;

public interface XSetter {

    /**
     * Gets the type of the object to be set by this setter.
     *
     * @return the setter object type
     */
    @SuppressWarnings("unchecked")
    Class getType();

    /**
     * Sets the value of the underlying member.
     *
     * @param instance the instance of the object that owns this field
     * @param value the value to set
     * @throws Exception
     */
    void setValue(Object instance, Object value) throws Exception;

}
