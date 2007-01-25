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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.util.HashSet;
import java.util.Set;

/**
 * Workbench generated by hbm2java
 */
@Entity
@Table(name = "workbench")
public class Workbench extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long workbenchId;
     protected String name;
     protected Integer dbTableId;
     protected String remarks;
     protected Integer formId;
     protected String exportInstitutionName;
     protected Integer ownerPermissionLevel;
     protected Integer groupPermissionLevel;
     protected Integer allPermissionLevel;
     protected WorkbenchTemplate workbenchTemplate;
     protected Set<WorkbenchDataItem> workbenchItems;
     protected SpecifyUser specifyUser;
     protected UserGroup group;

    // Constructors

    /** default constructor */
    public Workbench() {
        //
    }
    
    /** constructor with id */
    public Workbench(Long workbenchId) {
        this.workbenchId = workbenchId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        workbenchId = null;
        name = null;
        dbTableId = null;
        remarks = null;
        formId = null;
        exportInstitutionName = null;
        ownerPermissionLevel = null;
        groupPermissionLevel = null;
        allPermissionLevel = null;
        workbenchTemplate = null;
        workbenchItems = new HashSet<WorkbenchDataItem>();       
        specifyUser = null;
        group = null;
    }
    // End Initializer

    

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getWorkbenchId() {
        return this.workbenchId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.workbenchId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Workbench.class;
    }
    
    public void setWorkbenchId(Long workbenchId) {
        this.workbenchId = workbenchId;
    }

    /**
     *      * Name of workbench
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    @Column(name = "TableID", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getDbTableId() {
        return this.dbTableId;
    }
    
    public void setDbTableId(Integer tableId) {
        this.dbTableId = tableId;
    }

    /**
     * 
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @Column(name = "FormId", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getFormId() {
        return this.formId;
    }
    
    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    /**
     Name of Institution being exported from
     */
    @Column(name = "ExportInstitutionName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getExportInstitutionName() {
        return this.exportInstitutionName;
    }
    
    public void setExportInstitutionName(String exportInstitutionName) {
        this.exportInstitutionName = exportInstitutionName;
    }
    /**
     * 
     */
    @Column(name = "OwnerPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getOwnerPermissionLevel() {
        return this.ownerPermissionLevel;
    }
    
    public void setOwnerPermissionLevel(Integer ownerPermissionLevel) {
        this.ownerPermissionLevel = ownerPermissionLevel;
    }

    /**
     * 
     */
    @Column(name = "GroupPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getGroupPermissionLevel() {
        return this.groupPermissionLevel;
    }
    
    public void setGroupPermissionLevel(Integer groupPermissionLevel) {
        this.groupPermissionLevel = groupPermissionLevel;
    }

    /**
     * 
     */
    @Column(name = "AllPermissionLevel", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getAllPermissionLevel() {
        return this.allPermissionLevel;
    }
    
    public void setAllPermissionLevel(Integer allPermissionLevel) {
        this.allPermissionLevel = allPermissionLevel;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkbenchTemplateID", unique = false, nullable = false, insertable = true, updatable = true)
    public WorkbenchTemplate getWorkbenchTemplate() {
        return this.workbenchTemplate;
    }
    
    public void setWorkbenchTemplate(WorkbenchTemplate workbenchTemplates) {
        this.workbenchTemplate = workbenchTemplates;
    }

    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "workbench")
    public Set<WorkbenchDataItem> getWorkbenchItems() {
        return this.workbenchItems;
    }
    
    public void setWorkbenchItems(Set<WorkbenchDataItem> workbenchItems) {
        this.workbenchItems = workbenchItems;
    }
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser() {
        return this.specifyUser;
    }
    
    public void setSpecifyUser(SpecifyUser owner) {
        this.specifyUser = owner;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "UserGroupID", unique = false, nullable = true, insertable = true, updatable = true)
    public UserGroup getGroup() {
        return this.group;
    }
    
    public void setGroup(UserGroup group) {
        this.group = group;
    }
    
    public void addWorkbnechDataItem(WorkbenchDataItem item)
    {
        workbenchItems.add(item);
        item.setWorkbench(this);
        //item.set
    }
    
    /**
     * @param item - 
     * void
     */
    public void removeWorkbenchDataItem(final WorkbenchDataItem item)
    {
        this.workbenchItems.remove(item);
        item.setWorkbench(null);
    }  
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 79;
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    { 
        if(name!=null)return name;
        return super.getIdentityTitle();
    }
}
