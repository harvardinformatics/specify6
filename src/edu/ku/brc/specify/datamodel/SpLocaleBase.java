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
 */
package edu.ku.brc.specify.datamodel;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@MappedSuperclass
public abstract class SpLocaleBase extends DataModelObjBase
{
    private static final Logger  log      = Logger.getLogger(SpLocaleBase.class);
    
    protected String  name;
    protected String  type;

    /**
     * 
     */
    public SpLocaleBase()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        name = null;
        type = null;

    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        if (name != null && name.length() > 64)
        {
            log.error("String len: "+name.length()+ " is > 64 ["+name+"]");
            this.name = name.substring(0, 64);
            
        } else
        {
            this.name = name;
        }
    }

    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        if (type != null && type.length() > 32)
        {
            log.error("String len: "+type.length()+ " is > 32 ["+type+"]");
            this.type = type.substring(0, 32);
            
        } else
        {
            this.type = type;
        }
    }

}
