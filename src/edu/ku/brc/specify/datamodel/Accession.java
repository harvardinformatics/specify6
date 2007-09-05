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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;




/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "accession", uniqueConstraints = { @UniqueConstraint(columnNames = { "RepositoryAgreementID" }) })
@org.hibernate.annotations.Table(appliesTo="accession", indexes =
    {   @Index (name="AccessionNumberIDX", columnNames={"Number"}),
        @Index (name="AccessionDateIDX", columnNames={"DateAccessioned"})
    })
public class Accession extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Integer accessionId;
     protected String type;
     protected String status;
     protected String number;
     protected String verbatimDate;
     protected Calendar dateAccessioned;
     protected Calendar dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<CollectionObject>       collectionObjects;
     protected Set<AccessionAuthorization> accessionAuthorizations;
     protected Set<AccessionAgent>         accessionAgents;
     protected RepositoryAgreement         repositoryAgreement;
     protected Set<Attachment>             attachments;
     protected Set<ConservDescription>     conservDescriptions;


    // Constructors

    /** default constructor */
    public Accession()
    {
        // do nothing
    }

    /** constructor with id */
    public Accession(Integer accessionId) 
    {
        this.accessionId = accessionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        accessionId = null;
        type = null;
        status = null;
        number = null;
        verbatimDate = null;
        dateAccessioned = null;
        dateReceived = null;
        text1 = null;
        text2 = null;
        text3 = null;
        number1 = null;
        number2 = null;
        remarks = null;
        yesNo1 = null;
        yesNo2 = null;
        collectionObjects       = new HashSet<CollectionObject>();
        accessionAuthorizations = new HashSet<AccessionAuthorization>();
        accessionAgents         = new HashSet<AccessionAgent>();
        repositoryAgreement     = null;
        conservDescriptions     = new HashSet<ConservDescription>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "AccessionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAccessionId() {
        return this.accessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.accessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Accession.class;
    }

    public void setAccessionId(Integer accessionId) {
        this.accessionId = accessionId;
    }

    /**
     *      * Source of Accession, e.g. 'Collecting', 'Gift',  'Bequest' ...
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Status of Accession, e.g. 'In process', 'Complete' ...
     */
    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * A user-visible identifier of the Accession. Typically an integer, but may include alphanumeric characters as prefix, suffix, and separators
     */
    @Column(name = "Number", unique = false, nullable = false, insertable = true, updatable = true, length = 60)
    public String getNumber() {
        return this.number;
    }

    public void setNumber(final String number) 
    {
        firePropertyChange("Number", this.number, number);
        this.number = number;
    }

    /**
     *      * accomodates historical accessions.
     */
    @Column(name = "VerbatimDate", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVerbatimDate() {
        return this.verbatimDate;
    }

    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * Date of Accession
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateAccessioned", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateAccessioned() {
        return this.dateAccessioned;
    }

    public void setDateAccessioned(Calendar dateAccessioned) {
        this.dateAccessioned = dateAccessioned;
    }

    /**
     *      * Date material was received
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateReceived", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateReceived() {
        return this.dateReceived;
    }

    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text3", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * Comments
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAuthorization> getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorization> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAgent> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }


    /**
     * RepositoryAgreement for this Accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryAgreementID", unique = false, nullable = true, insertable = true, updatable = true)
    public RepositoryAgreement getRepositoryAgreement() {
        return this.repositoryAgreement;
    }
    
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement) {
        this.repositoryAgreement = repositoryAgreement;
    }

    //@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    //@Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "accession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }
    

    /**
     *
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<ConservDescription> getConservDescriptions()
    {
        return this.conservDescriptions;
    }

    public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
    {
        this.conservDescriptions = conservDescriptions;
    }


    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return number != null ? number : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 7;
    }
}
