/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.util.List;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 28, 2007
 *
 */
public interface LocalizableItemIFace
{
    /**
     * @return
     */
    public abstract String getName();
    
    /**
     * @param name
     */
    public abstract void setName(String name);
    
    /**
     * @return
     */
    public abstract String getType();
    
    /**
     * @param type
     */
    public abstract void setType(String type);
    
    /**
     * @return
     */
    public abstract Boolean getIsHidden();
    
    /**
     * @param isHidden the isHidden to set
     */
    public abstract void setIsHidden(Boolean isHidden);

    /**
     * @param str
     */
    public abstract void addDesc(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public abstract void removeDesc(LocalizableStrIFace str);
    
    /**
     * @param descs
     */
    public abstract void fillDescs(List<LocalizableStrIFace> descs);

    /**
     * @param str
     */
    public abstract void addName(LocalizableStrIFace str);
    
    /**
     * @param str
     */
    public abstract void removeName(LocalizableStrIFace str);
    
    /**
     * @param names
     */
    public abstract void fillNames(List<LocalizableStrIFace> names);
    
}
