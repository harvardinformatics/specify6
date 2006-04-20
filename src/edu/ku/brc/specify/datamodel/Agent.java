package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Agent  implements java.io.Serializable {

    // Fields

    protected Integer                       agentId;
    protected Byte                          agentType;
    protected String                        firstName;
    protected String                        lastName;
    protected String                        middleInitial;
    protected String                        title;
    protected String                        interests;
    protected String                        abbreviation;
    protected String                        name;
    protected String                        remarks;
    protected Date                          timestampModified;
    protected Date                          timestampCreated;
    protected String                        lastEditedBy;
    protected Set<Author>                   authors;
    protected Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects;
    protected Set<BorrowReturnMaterial>     borrowReturnMaterials;
    protected Set<ExchangeIn>               exchangeInCatalogedBys;
    protected Set<Agent>                    members;
    protected Agent                         organization;
    protected Set<Project>                  projects;
    protected Set<Preparation>              preparations;
    protected Set<GroupPerson>              groupPersonsByGroup;
    protected Set<GroupPerson>              groupPersonsByMember;
    protected Set<Determination>            determinations;
    protected Set<Agent>                    agentsByOrganization;
    protected Set<Agent>                    agentsByAgent;
    protected Set<Shipment>                 shipments;
    protected Set<Collector>                collectors;
    protected Set<ExchangeOut>              exchangeOutCatalogedBys;
    protected Set<ExternalResource>         externalResources;
    protected Set<RepositoryAgreement>      repositoryAgreements;
     
    // From AgentAddress
    protected String                        jobTitle;
    protected String                        email;
    protected String                        url;
     
    protected Set<Address>                  addresses;
    protected Set<LoanAgent>                loanAgents;
    protected Set<Shipment>                 shipmentsByShipper;
    protected Set<Shipment>                 shipmentsByShippedTo;
    protected Set<DeaccessionAgent>         deaccessionAgents;
    protected Set<ExchangeIn>               exchangeInFromOrganizations;
    protected Set<Permit>                   permitsByIssuee;
    protected Set<Permit>                   permitsByIssuer;
    protected Set<BorrowAgent>              borrowAgents;
    protected Set<AccessionAgent>           accessionAgents;
    protected Set<ExchangeOut>              exchangeOutSentToOrganizations;

    // Constructors

    /** default constructor */
    public Agent() {
    }

    /** constructor with id */
    public Agent(Integer agentId) {
        this.agentId = agentId;
    }

    // Initializer
    public void initialize()
    {
        agentId = null;
        agentType = null;
        firstName = null;
        lastName = null;
        middleInitial = null;
        title = null;
        interests = null;
        abbreviation = null;
        name = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        authors = new HashSet<Author>();
        loanReturnPhysicalObjects = new HashSet<LoanReturnPhysicalObject>();
        borrowReturnMaterials = new HashSet<BorrowReturnMaterial>();
        exchangeInCatalogedBys = new HashSet<ExchangeIn>();
        members = new HashSet<Agent>();
        organization = null;
        projects = new HashSet<Project>();
        preparations = new HashSet<Preparation>();
        groupPersonsByGroup = new HashSet<GroupPerson>();
        groupPersonsByMember = new HashSet<GroupPerson>();
        determinations = new HashSet<Determination>();
        agentsByOrganization = new HashSet<Agent>();
        agentsByAgent = new HashSet<Agent>();
        shipments = new HashSet<Shipment>();
        collectors = new HashSet<Collector>();
        exchangeOutCatalogedBys = new HashSet<ExchangeOut>();
        externalResources = new HashSet<ExternalResource>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
        
        // Agent
        jobTitle = null;
        email = null;
        url = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        addresses = new HashSet<Address>();
        loanAgents = new HashSet<LoanAgent>();
        shipmentsByShipper = new HashSet<Shipment>();
        shipmentsByShippedTo = new HashSet<Shipment>();
        deaccessionAgents = new HashSet<DeaccessionAgent>();
        exchangeInFromOrganizations = new HashSet<ExchangeIn>();
        permitsByIssuee = new HashSet<Permit>();
        permitsByIssuer = new HashSet<Permit>();
        borrowAgents = new HashSet<BorrowAgent>();
        accessionAgents = new HashSet<AccessionAgent>();
        exchangeOutSentToOrganizations = new HashSet<ExchangeOut>();
        organization = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getAgentId() {
        return this.agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    /**
     *
     */
    public Byte getAgentType() {
        return this.agentType;
    }

    public void setAgentType(Byte agentType) {
        this.agentType = agentType;
    }

    /**
     *      * of Person
     */
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      * of Person
     */
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      * of Person
     */
    public String getMiddleInitial() {
        return this.middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     *      * of Person
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *      * of Person or Organization
     */
    public String getInterests() {
        return this.interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    /**
     *      * of organization
     */
    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     *      * of organization/group/Folks (and maybe persons)
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
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *
     */
    public Set<Author> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    /**
     *
     */
    public Set<LoanReturnPhysicalObject> getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }

    public void setLoanReturnPhysicalObjects(Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }

    /**
     *
     */
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }

    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *
     */
    public Set<ExchangeIn> getExchangeInCatalogedBys() {
        return this.exchangeInCatalogedBys;
    }

    public void setExchangeInCatalogedBys(Set<ExchangeIn> exchangeInCatalogedBys) {
        this.exchangeInCatalogedBys = exchangeInCatalogedBys;
    }

    /**
     *
     */
    public Set<Agent> getMembers() {
        return this.members;
    }

    public void setMembers(Set<Agent> members) {
        this.members = members;
    }

    /**
     *      * of organization
     */
    public Agent getOrganization() {
        return this.organization;
    }

    public void setOrganization(Agent organization) {
        this.organization = organization;
    }

    /**
     *
     */
    public Set<Project> getProjects() {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    /**
     *
     */
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    public Set getGroupPersonsByGroup() {
        return this.groupPersonsByGroup;
    }

    public void setGroupPersonsByGroup(Set<GroupPerson> groupPersonsByGroup) {
        this.groupPersonsByGroup = groupPersonsByGroup;
    }

    /**
     *
     */
    public Set<GroupPerson> getGroupPersonsByMember() {
        return this.groupPersonsByMember;
    }

    public void setGroupPersonsByMember(Set<GroupPerson> groupPersonsByMember) {
        this.groupPersonsByMember = groupPersonsByMember;
    }

    /**
     *
     */
    public Set<Determination> getDeterminations() {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) {
        this.determinations = determinations;
    }

    /**
     *
     */
    public Set<Agent> getAgentesByOrganization() {
        return this.agentsByOrganization;
    }

    public void setAgentsByOrganization(Set<Agent> agentsByOrganization) {
        this.agentsByOrganization = agentsByOrganization;
    }

    /**
     *
     */
    public Set<Agent> getAgentsByAgent() {
        return this.agentsByAgent;
    }

    public void setAgentsByAgent(Set<Agent> agentsByAgent) {
        this.agentsByAgent = agentsByAgent;
    }

    /**
     *
     */
    public Set<Shipment> getShipments() {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }

    /**
     *
     */
    public Set<Collector> getCollectors() {
        return this.collectors;
    }

    public void setCollectors(Set<Collector> collectors) {
        this.collectors = collectors;
    }

    /**
     *
     */
    public Set<ExchangeOut> getExchangeOutCatalogedBys() {
        return this.exchangeOutCatalogedBys;
    }

    public void setExchangeOutCatalogedBys(Set<ExchangeOut> exchangeOutCatalogedBys) {
        this.exchangeOutCatalogedBys = exchangeOutCatalogedBys;
    }

    /**
     *
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }

    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }

    /**
     *
     */
    public Set<RepositoryAgreement> getRepositoryAgreements() {
        return this.repositoryAgreements;
    }

    public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements) {
        this.repositoryAgreements = repositoryAgreements;
    }

    //----------------------------------------------------
    // Agent Address
    //----------------------------------------------------
    
    /**
     *      * Agent's (person) job title at specified address and organization
     */
    public String getJobTitle() {
        return this.jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     *
     */
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     */
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    /**
     *
     */
    public Set<LoanAgent> getLoanAgents() {
        return this.loanAgents;
    }

    public void setLoanAgents(Set<LoanAgent> loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     *
     */
    public Set getShipmentsByShipper() {
        return this.shipmentsByShipper;
    }

    public void setShipmentsByShipper(Set<Shipment> shipmentsByShipper) {
        this.shipmentsByShipper = shipmentsByShipper;
    }

    /**
     *
     */
    public Set<Shipment> getShipmentsByShippedTo() {
        return this.shipmentsByShippedTo;
    }

    public void setShipmentsByShippedTo(Set<Shipment> shipmentsByShippedTo) {
        this.shipmentsByShippedTo = shipmentsByShippedTo;
    }

    /**
     *
     */
    public Set<DeaccessionAgent> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }

    public void setDeaccessionAgents(Set<DeaccessionAgent> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     *
     */
    public Set<ExchangeIn> getExchangeInFromOrganizations() {
        return this.exchangeInFromOrganizations;
    }

    public void setExchangeInFromOrganizations(Set<ExchangeIn> exchangeInFromOrganizations) {
        this.exchangeInFromOrganizations = exchangeInFromOrganizations;
    }

    /**
     *
     */
    public Set<Permit> getPermitsByIssuee() {
        return this.permitsByIssuee;
    }

    public void setPermitsByIssuee(Set<Permit> permitsByIssuee) {
        this.permitsByIssuee = permitsByIssuee;
    }

    /**
     *
     */
    public Set<Permit> getPermitsByIssuer() {
        return this.permitsByIssuer;
    }

    public void setPermitsByIssuer(Set<Permit> permitsByIssuer) {
        this.permitsByIssuer = permitsByIssuer;
    }

    /**
     *
     */
    public Set<BorrowAgent> getBorrowAgents() {
        return this.borrowAgents;
    }

    public void setBorrowAgents(Set<BorrowAgent> borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     *
     */
    public Set<AccessionAgent> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }

    /**
     *
     */
    public Set<ExchangeOut> getExchangeOutSentToOrganizations() {
        return this.exchangeOutSentToOrganizations;
    }

    public void setExchangeOutSentToOrganizations(Set<ExchangeOut> exchangeOutSentToOrganizations) {
        this.exchangeOutSentToOrganizations = exchangeOutSentToOrganizations;
    }

    /**
     *      * Associated record in Address table
     */
    public Set<Address> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }
    


    // Add Methods

    public void addAuthor(final Author author)
    {
        this.authors.add(author);
        author.setAgent(this);
    }

    public void addLoanReturnPhysicalObject(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.add(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setAgent(this);
    }

    public void addBorrowReturnMaterial(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.add(borrowReturnMaterial);
        borrowReturnMaterial.setAgent(this);
    }

    public void addMember(final Agent member)
    {
        this.members.add(member);
        member.getMembers().add(this);
    }

    public void addProject(final Project project)
    {
        this.projects.add(project);
        project.setAgent(this);
    }
    
    public void addExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
        externalResource.getAgents().add(this);
    }

    public void addAddress(final Address address)
    {
        this.addresses.add(address);
        address.setAgent(this);
    }



    // Done Add Methods

    // Delete Methods

    public void removeMember(final Agent member)
    {
        this.members.remove(member);
        member.getMembers().remove(this);
    }

    public void removeProject(final Project project)
    {
        this.projects.remove(project);
        project.setAgent(null);
    }

    public void removeExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.remove(externalResource);
        externalResource.getAgents().remove(this);
    }
    
    public void removeAddress(final Address address)
    {
        this.addresses.remove(address);
        address.setAgent(null);
    }

     // Delete Add Methods
}
