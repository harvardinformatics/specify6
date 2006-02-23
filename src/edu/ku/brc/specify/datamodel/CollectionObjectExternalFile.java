package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 * CollectionObjectExternalFile generated by hbm2java
 */
public class CollectionObjectExternalFile  implements ExternalFile,java.io.Serializable {

    // Fields    

     protected Integer collectioObjectExternalFileId;
     protected String mimeType;
     protected String fileName;
     protected Integer fileCreatedDate;
     protected String remarks;
     protected String externalLocation;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private Agent createdByAgent;
     private Set externalFiles;


    // Constructors

    /** default constructor */
    public CollectionObjectExternalFile() {
    }
    
    /** constructor with id */
    public CollectionObjectExternalFile(Integer collectioObjectExternalFileId) {
        this.collectioObjectExternalFileId = collectioObjectExternalFileId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getCollectioObjectExternalFileId() {
        return this.collectioObjectExternalFileId;
    }
    
    public void setCollectioObjectExternalFileId(Integer collectioObjectExternalFileId) {
        this.collectioObjectExternalFileId = collectioObjectExternalFileId;
    }

    /**
     *      *            @hibernate.property
     *             column="mimeType"
     *             length="32"
     *         
     */
    public String getMimeType() {
        return this.mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     *      *            @hibernate.property
     *             column="FileName"
     *             length="128"
     *         
     */
    public String getFileName() {
        return this.fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     *      *            @hibernate.property
     *             column="fileCreatedDate"
     *             length="10"
     *         
     */
    public Integer getFileCreatedDate() {
        return this.fileCreatedDate;
    }
    
    public void setFileCreatedDate(Integer fileCreatedDate) {
        this.fileCreatedDate = fileCreatedDate;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="ExternalLocation"
     *             length="1024"
     *         
     */
    public String getExternalLocation() {
        return this.externalLocation;
    }
    
    public void setExternalLocation(String externalLocation) {
        this.externalLocation = externalLocation;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="MadeByID"         
     *         
     */
    public Agent getCreatedByAgent() {
        return this.createdByAgent;
    }
    
    public void setCreatedByAgent(Agent createdByAgent) {
        this.createdByAgent = createdByAgent;
    }

    /**
     * 
     */
    public Set getExternalFiles() {
        return this.externalFiles;
    }
    
    public void setExternalFiles(Set externalFiles) {
        this.externalFiles = externalFiles;
    }




}