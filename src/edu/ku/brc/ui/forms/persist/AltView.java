/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */package edu.ku.brc.ui.forms.persist;

 import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Vector;

/**
 * Definition of an Alternate View which can be an "Edit", "View", "None"
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class AltView implements Comparable<AltView>, Cloneable
{
    public enum CreationMode {None, Edit, View, Search}
    
    protected View         view;
    protected String       name;
    protected String       label;
    protected CreationMode mode;
    protected boolean      validated;
    protected boolean      isDefault;
    
    protected ViewDef      viewDef = null;
    
    protected String        selectorName  = null;
    protected String        selectorValue = null;
    protected List<AltView> subViews      = null;

    /**
     * 
     */
    public AltView()
    {
        // do nothing
    }

    public AltView(final View view, 
                   final String name, 
                   final String label, 
                   final CreationMode mode, 
                   final boolean validated, 
                   final boolean isDefault, 
                   final ViewDef viewDef)
    {
        this.view = view;
        this.name = name;
        this.label = label;
        this.mode = mode;
        this.validated = validated;
        this.isDefault = isDefault;
        this.viewDef = viewDef;
    }


    public CreationMode getMode()
    {
        return mode;
    }

    public String getViewDefName()
    {
        return viewDef.getName();
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isValidated()
    {
        return validated;
    }

    public ViewDef getViewDef()
    {
        return viewDef;
    }

    public void setViewDef(ViewDef viewDef)
    {
        this.viewDef = viewDef;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }
    
    public View getView()
    {
        return view;
    }

    public void setMode(CreationMode mode)
    {
        this.mode = mode;
    }

    @Override
    public String toString()
    {
        return label;
    }
    
    public String getSelectorName()
    {
        return selectorName;
    }

    public void setSelectorName(String selectorName)
    {
        this.selectorName = selectorName;
    }

    public String getSelectorValue()
    {
        return selectorValue;
    }

    public void setSelectorValue(String selectorValue)
    {
        this.selectorValue = selectorValue;
    }

    public List<AltView> getSubViews()
    {
        return subViews;
    }

    public void setSubViews(List<AltView> subViews)
    {
        this.subViews = subViews;
    }

    public static AltView.CreationMode parseMode(final String modeStr, final AltView.CreationMode defaultMode)
    {
        if (isNotEmpty(modeStr))
        {
            if (modeStr.equalsIgnoreCase("edit"))
            {
                return AltView.CreationMode.Edit;
               
            } else if (modeStr.equalsIgnoreCase("view"))
            {
                return AltView.CreationMode.View;
                
            } if (modeStr.equalsIgnoreCase("search"))
            {
                return AltView.CreationMode.Search;
            }
        }
        return defaultMode;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        AltView altView = (AltView)super.clone();
        altView.view = view;
        altView.name = name;
        altView.label = label;
        altView.mode = mode;
        altView.validated = validated;
        altView.isDefault = isDefault;
        altView.viewDef = viewDef;
        altView.selectorName = selectorName;
        altView.selectorValue = selectorValue;
        altView.subViews = new Vector<AltView>(subViews);
        return altView;      
    }
    
    //-------------------------------------
    // Comparable
    //-------------------------------------
    public int compareTo(AltView obj)
    {
        if (name.equals(obj.name))
        {
            return 0;

        }
        // else
        return name.compareTo(obj.name);
    }

}
