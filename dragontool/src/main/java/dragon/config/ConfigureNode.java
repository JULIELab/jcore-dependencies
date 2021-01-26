package dragon.config;

/**
 * <p>Interface of Congituration Node</p>
 * <p>A configuration node can be the root node of the configuaration XML file or the XML node for an object.
 * An object node should have at least two attributes, type and id, which are used to search the object. Usually, the type is
 * the interface the object implements. The id is an integer value greater than zero. An object node contains mutilple parameters which can
 * be a simple datatype or an object.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ConfigureNode {
    /**
     * Gets the parent node of the current object node
     * @return the parent node of the current object node
     */
    public ConfigureNode getParentNode();

    /**
     * Gets the first object node within the current configuration node
     * @return the first object node within the current configuration node
     */
    public ConfigureNode getFirstChild();

    /**
     * Gets the next object node in parallel to the current onfiguration node
     * @return the next object node in parallel to the current onfiguration node
     */
    public ConfigureNode getNextSibling();

    /**
     * @return the name of the object node
     */
    public String getNodeName();

    /**
     * @return the id of the object node
     */
    public int getNodeID();

    /**
     * @return the type of the object node
     */
    public String getNodeType();

    /**
     * @return the class name the object corresponds to
     */
    public Class getNodeClass();

    public String getString(String key);

    /**
     * If the given parameter exists, return its values, otherwise return the default value.
     * @param key the name of the parameter
     * @param def the default value of the parameter
     * @return the string of the parameter
     */
    public String getString(String key, String def);
    public int getInt(String key);
    public int getInt(String key, int def);
    public boolean getBoolean(String key);
    public boolean getBoolean(String key, boolean def);
    public double getDouble(String key);
    public double getDouble(String key, double def);

    /**
     * If the parameter corresponds to an object, it returns the type (usually the interface) of the object.
     * Otherwise, it returns null.
     * @param key the name of the parameter
     * @return the type of the object
     */
    public String getParameterType(String key);

    /**
     * @param key the name of the parameter
     * @return true if the parameter exists
     */
    public boolean exist(String key);
}