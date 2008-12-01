/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang.StringUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 27, 2008
 *
 */
public class RegProcEntry implements TreeNode
{
    protected RegProcEntry         parent;
    protected String               type;
    protected String               name  = null;
    protected String               id;
    protected Properties           props = null;
    protected Vector<RegProcEntry> kids  = new Vector<RegProcEntry>();
    
    protected String               isaNumber = null;
    
    /**
     * 
     */
    public RegProcEntry(final String name)
    {
        this(name, new Properties());
    }
    
    public RegProcEntry(final Properties props)
    {
        this(null, props);
        this.type  = props.getProperty("reg_type");
    }
    
    public RegProcEntry(final String name, final Properties props)
    {
        super();
        this.name  = name;
        this.props = props;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        if (name == null)
        {
            name = props.getProperty(type+"_name");
            if (name == null)
            {
                name = "Anonymous";
            }
        }
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * @return the props
     */
    public Properties getProps()
    {
        return props;
    }
    /**
     * @return the kids
     */
    public Vector<RegProcEntry> getKids()
    {
        return kids;
    }
    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * @return whether this node (as a collection) has been registered.
     */
    public boolean isRegistered()
    {
        if (type != null && type.equals("Collection") && isaNumber == null)
        {
            isaNumber = props.getProperty("SA_Number", "");
        }
        return StringUtils.isNotEmpty(isaNumber);
    }
    
    /**
     * @return the isaNumber
     */
    public String getISANumber()
    {
        return isaNumber == null ? "&nbsp;" : isaNumber;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        if (parent != null)
        {
            return name + (isRegistered() ? ("  (Registered)") : "");
        }
        
        if (isaNumber == null)
        {
            isaNumber = props.getProperty("SA_Number", "");
            System.out.println(isaNumber);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append((name != null ? ("Name: "+ name) : "") + (type != null ? " Type: "+type : "")+ (id != null ? " id: "+id : ""));
        sb.append("\n");
        
        for (Object key : props.keySet())
        {
            if (key.equals("id")) continue;
            
            sb.append("  ");
            sb.append(key);
            sb.append("=");
            sb.append(props.get(key));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(RegProcEntry parent)
    {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enumeration children()
    {
        return kids.elements();
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    @Override
    public boolean getAllowsChildren()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    @Override
    public TreeNode getChildAt(int childIndex)
    {
        return kids.get(childIndex);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    @Override
    public int getChildCount()
    {
        return kids.size();
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    @Override
    public int getIndex(TreeNode node)
    {
        return kids.indexOf(node);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    @Override
    public TreeNode getParent()
    {
        return parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    @Override
    public boolean isLeaf()
    {
        return kids.size() == 0;
    }
}
