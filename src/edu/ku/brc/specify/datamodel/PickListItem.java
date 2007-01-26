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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.ui.db.PickListItemIFace;

/**
 * PickListItem generated by hbm2java
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
@Entity(name="picklistitem")
public class PickListItem implements PickListItemIFace, java.io.Serializable
{
    // Fields
    protected Long pickListItemId;
    private String title;
    private String value;
    private Date   timestampCreated;
    protected PickList pickList;
    
    // Non-Persisted Value as an Object
    private Object valueObject;

    // Constructors

    /** default constructor */
    public PickListItem()
    {
        // do nothing
    }

    public PickListItem(final String title, final String value, final Date timestampCreated)
    {
        super();
        this.title = title;
        this.value = value;
        this.timestampCreated = timestampCreated;
    }

    public PickListItem(final String title, final Object valueObject, final Date timestampCreated)
    {
        super();
        this.title       = title;
        this.value       = null;
        this.valueObject = valueObject;
        this.timestampCreated = timestampCreated;
    }

    @Id
    @GeneratedValue
    @Column(name = "PickListItemID", unique = false, nullable = false, insertable = true, updatable = true)
    protected Long getPickListItemId()
    {
        return pickListItemId;
    }

    protected void setPickListItemId(Long pickListItemId)
    {
        this.pickListItemId = pickListItemId;
    }

    /**
     * 
     */
    @Column(name = "title", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "PickListID", unique = false, nullable = false, insertable = true, updatable = true)
    public PickList getPickList()
    {
        return pickList;
    }

    public void setPickList(PickList pickList)
    {
        this.pickList = pickList;
    }

    /**
     * 
     */
    @Column(name = "value", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getValue()
    {
        return this.value == null ? title : value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
    
    @Transient
    public Object getValueObject()
    {
        return valueObject;
    }

    public void setValueObject(Object valueObject)
    {
        this.valueObject = valueObject;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return title;
    }

    /**
     * 
     */
    @Column(name = "TimestampCreated", unique = false, nullable = false, insertable = true, updatable = true)
    public Date getTimestampCreated()
    {
        return this.timestampCreated;
    }

    public void setTimestampCreated(Date createdDate)
    {
        this.timestampCreated = createdDate;
    }
    
    //-------------------------------------
    // Comparable
    //-------------------------------------
    public int compareTo(PickListItemIFace obj)
    {
        if (title.equals(obj.getTitle()))
        {
            return 0;
        }
        // else
        return title.compareTo(obj.getTitle());
    }


}
