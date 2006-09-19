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




/**
 * WorkbenchDataItem generated by hbm2java
 */
public class WorkbenchDataItem  implements java.io.Serializable {

    // Fields    

     private Long workbenchDataItemId;
     private String cellData;
     private String rowNumber;
     private String columnNumber;
     private Date timestampModified;
     private Date timestampCreated;
     protected Workbench owner;


    // Constructors

    /** default constructor */
    public WorkbenchDataItem() {
    }
    
    /** constructor with id */
    public WorkbenchDataItem(Long workbenchDataItemId) {
        this.workbenchDataItemId = workbenchDataItemId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Long getWorkbenchDataItemId() {
        return this.workbenchDataItemId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.workbenchDataItemId;
    }
    
    public void setWorkbenchDataItemId(Long workbenchDataItemId) {
        this.workbenchDataItemId = workbenchDataItemId;
    }

    /**
     * 
     */
    public String getCellData() {
        return this.cellData;
    }
    
    public void setCellData(String cellData) {
        this.cellData = cellData;
    }

    /**
     * 
     */
    public String getRowNumber() {
        return this.rowNumber;
    }
    
    public void setRowNumber(String rowNumber) {
        this.rowNumber = rowNumber;
    }

    /**
     * 
     */
    public String getColumnNumber() {
        return this.columnNumber;
    }
    
    public void setColumnNumber(String columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public Workbench getOwner() {
        return this.owner;
    }
    
    public void setOwner(Workbench owner) {
        this.owner = owner;
    }




}
