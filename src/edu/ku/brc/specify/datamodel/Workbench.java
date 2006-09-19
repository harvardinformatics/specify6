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
import java.util.Set;




/**
 * Workbench generated by hbm2java
 */
public class Workbench  implements java.io.Serializable {

    // Fields    

     private Long workbenchId;
     private String name;
     private Integer tableId;
     protected String remarks;
     protected Integer formid;
     protected String exportinstitutionname;
     private Date timestampModified;
     private Date timestampCreated;
     protected WorkbenchTemplate workbenchTemplates;
     protected Set workbenchItems;


    // Constructors

    /** default constructor */
    public Workbench() {
    }
    
    /** constructor with id */
    public Workbench(Long workbenchId) {
        this.workbenchId = workbenchId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Long getWorkbenchId() {
        return this.workbenchId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.workbenchId;
    }
    
    public void setWorkbenchId(Long workbenchId) {
        this.workbenchId = workbenchId;
    }

    /**
     *      * Name of workbench
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    public Integer getTableId() {
        return this.tableId;
    }
    
    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Integer getFormid() {
        return this.formid;
    }
    
    public void setFormid(Integer formid) {
        this.formid = formid;
    }

    /**
     *      * Name of Institution being exported from
     */
    public String getExportinstitutionname() {
        return this.exportinstitutionname;
    }
    
    public void setExportinstitutionname(String exportinstitutionname) {
        this.exportinstitutionname = exportinstitutionname;
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
    public WorkbenchTemplate getWorkbenchTemplates() {
        return this.workbenchTemplates;
    }
    
    public void setWorkbenchTemplates(WorkbenchTemplate workbenchTemplates) {
        this.workbenchTemplates = workbenchTemplates;
    }

    /**
     * 
     */
    public Set getWorkbenchItems() {
        return this.workbenchItems;
    }
    
    public void setWorkbenchItems(Set workbenchItems) {
        this.workbenchItems = workbenchItems;
    }




}
