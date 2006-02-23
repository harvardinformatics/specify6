package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="geologictimeperiod"
 *     
 */
public class GeologicTimePeriod  implements java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodId;
     protected Integer parentId;
     protected Integer rankId;
     protected String name;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String standard;
     protected Float age;
     protected Float ageUncertainty;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private GeologicTimePeriodTreeDef definition;
     private Set children;
     private GeologicTimePeriod parent;


    // Constructors

    /** default constructor */
    public GeologicTimePeriod() {
    }
    
    /** constructor with id */
    public GeologicTimePeriod(Integer geologicTimePeriodId) {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GeologicTimePeriodID"
     *         
     */
    public Integer getGeologicTimePeriodId() {
        return this.geologicTimePeriodId;
    }
    
    public void setGeologicTimePeriodId(Integer geologicTimePeriodId) {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }

    /**
     *      *            @hibernate.property
     *             column="ParentTaxonID"
     *             length="10"
     *             index="IX_GTP_ParentID"
     *         
     */
    public Integer getParentId() {
        return this.parentId;
    }
    
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     *      *            @hibernate.property
     *             column="RankID"
     *             length="10"
     *             index="IX_GTP_RankId"
     *         
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="64"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="NodeNumber"
     *             length="10"
     *             index="IX_GTP_NodeNumber"
     *         
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="HighestChildNodeNumber"
     *             length="10"
     *             index="IX_GTP_NighestChildNodeNumber"
     *         
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Standard"
     *             length="64"
     *         
     */
    public String getStandard() {
        return this.standard;
    }
    
    public void setStandard(String standard) {
        this.standard = standard;
    }

    /**
     *      *            @hibernate.property
     *             column="Age"
     *             length="24"
     *         
     */
    public Float getAge() {
        return this.age;
    }
    
    public void setAge(Float age) {
        this.age = age;
    }

    /**
     *      *            @hibernate.property
     *             column="AgeUncertainty"
     *             length="24"
     *         
     */
    public Float getAgeUncertainty() {
        return this.ageUncertainty;
    }
    
    public void setAgeUncertainty(Float ageUncertainty) {
        this.ageUncertainty = ageUncertainty;
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
     *             column="TimestampVersion"
     *             length="16"
     *         
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="32"
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
     *            @hibernate.column name="GeologicTimePeriodID"         
     *         
     */
    public GeologicTimePeriodTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeologicTimePeriodTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="GeologicTimePeriodID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GeologicTimePeriod"
     *         
     */
    public Set getChildren() {
        return this.children;
    }
    
    public void setChildren(Set children) {
        this.children = children;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParentID"         
     *         
     */
    public GeologicTimePeriod getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriod parent) {
        this.parent = parent;
    }




}