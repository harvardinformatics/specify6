package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 * WorkbenchDataItem generated by hbm2java
 */
public class WorkbenchDataItem  implements java.io.Serializable {

    // Fields    

     private Integer workbenchDataItemID;
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
    public WorkbenchDataItem(Integer workbenchDataItemID) {
        this.workbenchDataItemID = workbenchDataItemID;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getWorkbenchDataItemID() {
        return this.workbenchDataItemID;
    }
    
    public void setWorkbenchDataItemID(Integer workbenchDataItemID) {
        this.workbenchDataItemID = workbenchDataItemID;
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