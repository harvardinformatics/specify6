/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;

import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace.CreationMode;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * A business rules class that handles various safety checking and housekeeping tasks
 * that must be performed when editing {@link Taxon} or
 * {@link TaxonTreeDefItem} objects.
 *
 * @author jstewart
 * 
 * @code_status Beta
 */
public class TaxonBusRules extends BaseTreeBusRules<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
    AttachmentOwnerBaseBusRules attachOwnerRules;
    
    /**
     * Constructor.
     */
    public TaxonBusRules()
    {
        super(Taxon.class,TaxonTreeDefItem.class);
        
        attachOwnerRules = new AttachmentOwnerBaseBusRules()
        {
            @Override
            public boolean okToEnableDelete(Object dataObj)
            {
                return false;
            }
        };
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        //System.out.println(formViewObj.hashCode()+"  "+viewableArg.hashCode());
        
        // TODO: the form system MUST require the hybridParent1 and hybridParent2 widgets to be present if the isHybrid checkbox is present
        final JCheckBox        hybridCheckBox = (JCheckBox)formViewObj.getControlByName("isHybrid");
        final GetSetValueIFace hybrid1Widget  = (GetSetValueIFace)formViewObj.getControlByName("hybridParent1");
        final GetSetValueIFace hybrid2Widget  = (GetSetValueIFace)formViewObj.getControlByName("hybridParent2");
        
        if (hybridCheckBox != null)
        {
            hybridCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (!hybridCheckBox.isSelected())
                    {
                        hybrid1Widget.setValue(null, null);
                        hybrid2Widget.setValue(null, null);
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#getDeleteMsg(java.lang.Object)
     */
    @Override
    public String getDeleteMsg(Object dataObj)
    {
        return getLocalizedMessage("TAXON_DELETE", ((Taxon)dataObj).getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        if (dataObj instanceof Taxon)
        {
            return super.okToDeleteNode((Taxon)dataObj);
        }
        else if (dataObj instanceof TaxonTreeDefItem)
        {
            return okToDeleteDefItem((TaxonTreeDefItem)dataObj);
        }
        
        return false;
    }
    
    /**
     * Handles the {@link #okToEnableDelete(Object)} method in the case that the passed in
     * {@link Object} is an instance of {@link TaxonTreeDefItem}.
     * 
     * @param defItem the {@link TaxonTreeDefItem} being inspected
     * @return true if the passed in item is deletable
     */
    public boolean okToDeleteDefItem(TaxonTreeDefItem defItem)
    {
        reasonList.clear();
        
       // never let the root level be deleted
        if (defItem.getRankId() == 0)
        {
            return false;
        }
        
        // don't let 'used' levels be deleted
        if (!okToDelete("taxon", "TaxonTreeDefItemID", defItem.getId()))
        {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String[] getRelatedTableAndColumnNames()
    {
        String[] relationships = 
        {
                "determination", "TaxonID",
                "taxoncitation", "TaxonID",
                "taxon",         "HybridParent1ID",
                "taxon",         "HybridParent2ID",
                "taxon",         "AcceptedID"
        };

        return relationships;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(Object dataObj, DataProviderSessionIFace session)
    {
        // make sure to handle all of the attachment stuff
        attachOwnerRules.beforeSave(dataObj, session);
        
        super.beforeSave(dataObj, session);
        
        if (dataObj instanceof Taxon)
        {
            Taxon taxon = (Taxon)dataObj;
            beforeSaveTaxon(taxon, session);

            // this might not do anything (if no names need to be changed)
            super.updateFullNamesIfNecessary(taxon, session);
            
            return;
        }
    }
    
    /**
     * Handles the {@link #beforeSave(Object)} method if the passed in {@link Object}
     * is an instance of {@link Taxon}.  The real work of this method is to
     * update the 'fullname' field of all {@link Taxon} objects effected by the changes
     * to the passed in {@link Taxon}.
     * 
     * @param taxon the {@link Taxon} being saved
     */
    protected void beforeSaveTaxon(Taxon taxon, @SuppressWarnings("unused") DataProviderSessionIFace session)
    {
        // if this node is "accepted" then make sure it doesn't point to an accepted parent
        if (taxon.getIsAccepted() == null || taxon.getIsAccepted().booleanValue() == true)
        {
            taxon.setAcceptedTaxon(null);
        }
        
        // if this node isn't a hybrid then make sure it doesn't point at hybrid "parents"
        if (taxon.getIsHybrid() == null || taxon.getIsHybrid().booleanValue() == false)
        {
            taxon.setHybridParent1(null);
            taxon.setHybridParent2(null);
        }
    }

    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeDeleteCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeDeleteCommit(dataObj, session);
        return retVal;
    }

    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        // make sure to handle all of the attachment stuff
        boolean retVal = attachOwnerRules.beforeSaveCommit(dataObj, session);
        if (retVal == false)
        {
            return retVal;
        }
        
        retVal = super.beforeSaveCommit(dataObj, session);
        return retVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#canAccessSynonymy(java.lang.Object)
     */
    @Override
    protected boolean canAccessSynonymy(Taxon dataObj)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseTreeBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(Object dataObj)
    {
        super.afterFillForm(dataObj);
        
        
        if (formViewObj.getAltView().getMode() != CreationMode.EDIT)
        {
            return;
        }
        
        // TODO: the form system MUST require the hybridParent1 and hybridParent2 widgets to be present if the isHybrid checkbox is present
        final JCheckBox        hybridCheckBox = (JCheckBox)formViewObj.getControlByName("isHybrid");
        final ValComboBoxFromQuery hybrid1Widget  = (ValComboBoxFromQuery)formViewObj.getControlByName("hybridParent1");
        final ValComboBoxFromQuery hybrid2Widget  = (ValComboBoxFromQuery)formViewObj.getControlByName("hybridParent2");
        
        
        if (hybridCheckBox != null)
        {
            //XXX TaxonSearchBuilder will still allow both hybrid parents to be the same.
            Taxon nodeInForm = (Taxon )formViewObj.getDataObj();
            if (nodeInForm != null)
            {
                hybrid1Widget.registerQueryBuilder(new TreeableSearchQueryBuilder(nodeInForm, null, false));
                hybrid2Widget.registerQueryBuilder(new TreeableSearchQueryBuilder(nodeInForm, null, false));
            }
        }
        
    }
    
    
}
