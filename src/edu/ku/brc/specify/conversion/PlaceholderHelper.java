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
/**
 * 
 */
package edu.ku.brc.specify.conversion;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 9, 2009
 *
 */
public class PlaceholderHelper
{
    protected static final Logger           log         = Logger.getLogger(PlaceholderHelper.class);
    protected static final String                PLACEHOLDER         = "Placeholder"; 
    
    public static final String                   SYN_PLACEHOLDER     = "Synonym Placeholder"; 
    
    protected TaxonTreeDef                       taxonTreeDef        = null;
    protected HashMap<Integer, Taxon>            placeHolderTreeHash = new HashMap<Integer, Taxon>();
    protected List<TaxonTreeDefItem>             treeDefItems        = null;
    protected HashMap<Integer, TaxonTreeDefItem> treeDefItemHash     = new HashMap<Integer, TaxonTreeDefItem>();
    protected HashMap<Integer, Integer>          rankParentHash      = new HashMap<Integer, Integer>();
    protected Integer                            taxonRootId         = null;
    protected Integer                            taxonTreeDefId      = null; 
    protected Connection                         conn;
    
    protected String                             taxonTitle          = PLACEHOLDER;
    protected boolean                            isSynonymBranch     = false;
    protected boolean							 doCleanup           = false;
    protected Taxon								 highestPlaceHolder  = null;
    
    /**
     * @param conn
     * @param taxonTreeDefId
     */
    public PlaceholderHelper(final boolean doCleanup, final TaxonTreeDef taxonTreeDef)
    {
        super();
        
        this.doCleanup = doCleanup;
        
        this.conn = DBConnection.getInstance().getConnection();
        
        if (taxonTreeDef != null)
        {
            this.taxonTreeDef   = taxonTreeDef;
            this.taxonTreeDefId = taxonTreeDef.getId();
        }
    }
    
    /**
     * @param isSynonymBranch the isSynonymBranch to set
     */
    public void setSynonymBranch(boolean isSynonymBranch)
    {
        this.isSynonymBranch = isSynonymBranch;
        this.taxonTitle      = isSynonymBranch ? SYN_PLACEHOLDER : PLACEHOLDER;
    }


    /**
     * @return the placeHolderTreeHash
     */
    public HashMap<Integer, Taxon> getPlaceHolderTreeHash()
    {
        return placeHolderTreeHash;
    }

    /**
     * @return the treeDefItems
     */
    public List<TaxonTreeDefItem> getTreeDefItems()
    {
        buildPlaceHolderInfo();
        
        return treeDefItems;
    }
    
    /**
     * @return
     */
    private boolean ensureTreeDef()
    {
        if (taxonTreeDef == null)
        {
            if (this.taxonTreeDefId == null)
            {
                throw new RuntimeException("The Taxon Tree Def Id was null and can't be!");
            }
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                taxonTreeDef = session.get(TaxonTreeDef.class, taxonTreeDefId);
                
                return taxonTreeDef != null;
                
            } catch(Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                if (session != null) session.close();
            }
            return false;
        }
        return true;
    }
    
    /**
     * 
     */
    public boolean buildPlaceHolderInfo()
    {
        if (!ensureTreeDef())
        {
            return false;
        }
        
        if (treeDefItems == null)
        {
            try
            {
                treeDefItems = new Vector<TaxonTreeDefItem>(taxonTreeDef.getTreeDefItems());
                Collections.sort(treeDefItems);
                
                int i = 0;
                for (TaxonTreeDefItem item : treeDefItems)
                {
                    if (i > 0)
                    {
                        rankParentHash.put(item.getRankId(), treeDefItems.get(i-1).getRankId());
                    }
                    i++;
                    treeDefItemHash.put(item.getRankId(), item);
                }
                
                if (!readPlaceHolders())
                {
                    buildPlaceHolders();
                }
                
                return true;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * @param item
     * @param parentTaxon
     * @param session
     * @return
     */
    private Taxon createTaxon(final TaxonTreeDefItem item, 
                              final Taxon parentTaxon,
                              final DataProviderSessionIFace session)
    {
        Taxon taxon = null;
    	if (isSynonymBranch)
    	{
    		taxon = getSynonymPlaceHolder(item, parentTaxon, session);
    	}
    	if (taxon == null)
    	{
    		taxon = new Taxon();
    		taxon.initialize();
    		taxon.setRankId(item.getRankId());
    		taxon.setName(taxonTitle);
    		taxon.setFullName(taxon.getName());
        
    		taxon.setDefinition(taxonTreeDef);
    		taxon.setDefinitionItem(item);
    		taxon.setParent(parentTaxon);
    	}
        return taxon;
    }
    
    private void recurse(final Taxon parentTaxon)
    {
        for (Taxon taxon : parentTaxon.getChildren())
        {
            if (taxon.getName().equals(taxonTitle))
            {
                placeHolderTreeHash.put(taxon.getRankId(), taxon);
                recurse(taxon);
            }
        }
    }
    
    /**
     * @return highest placeholder (with lowest RankID value).
     * 
     * Assumes placeholders have already been created.
     */
    public Taxon getHighestPlaceHolder()
    {
    	if (highestPlaceHolder == null)
    	{
    		int low = 25000;
    		for (Integer  e : getPlaceHolderTreeHash().keySet())
    		{
    			if (e < low)
    			{
    				low = e;
    			}
    		}
    		highestPlaceHolder = getPlaceHolderTreeHash().get(low);
    	}
    	return highestPlaceHolder;
    }
    
    /**
     * @return
     */
    private boolean readPlaceHolders()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            taxonTreeDef = session.get(TaxonTreeDef.class, taxonTreeDef.getId());
            
            String sql = "SELECT TaxonID FROM taxon WHERE RankID = 0 AND TaxonTreeDefID = " + taxonTreeDef.getId();
            log.debug(sql);
            Integer taxonId = BasicSQLUtils.getCount(sql);
            if (taxonId != null)
            {
                Taxon txRoot = (Taxon)session.getData("FROM Taxon WHERE id = " + taxonId);
                
                recurse(txRoot);

            } else
            {
                log.error("Couldn't find the Root Taxon Node");
            }
            
        } catch(Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return placeHolderTreeHash.size() > 0;
    }
    
    
    /**
     * @param node
     * @return  true if node 'is' a synonym placeholder node
     * 
     * Reliably determining whether a node is a placeholder is kind of impossible.
     *  
     */
    protected boolean isSynonymPlaceHolder(Taxon node)
    {
    	//but what if a user edited a placeholder node's name, or created a node with name of SYN_PLACEHOLDER?
    	return SYN_PLACEHOLDER.equals(node.getName());
    }
    
    /**
     * @param item
     * @param parent
     * @param session
     * @return
     */
    private Taxon getSynonymPlaceHolder(final TaxonTreeDefItem item, final Taxon parent, final DataProviderSessionIFace session)
    {
    	List<?> children = session.getDataList("from Taxon where parentId = " + parent.getParentId() + " and rankId = " + item.getRankId());
    	for (Object child : children)
    	{
    		if (isSynonymPlaceHolder((Taxon)child))
    		{
    			return (Taxon)child;
    		}
    	}
    	return null;
    }
    
    /**
     * @param item
     * @param parent
     * @param session
     * @return
     */
    private boolean needToCreateNode(final TaxonTreeDefItem item, final Taxon parent, final DataProviderSessionIFace session)
    {
    	return getSynonymPlaceHolder(item, parent, session) == null;
    }
    
    /**
     * 
     */
    private void buildPlaceHolders()
    {
        if (placeHolderTreeHash.size() == 0)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                taxonTreeDef = session.get(TaxonTreeDef.class, taxonTreeDef.getId());
                
                String  sql     = "SELECT TaxonID FROM taxon WHERE RankID = 0 AND TaxonTreeDefID = " + taxonTreeDef.getId();
                Integer taxonId = BasicSQLUtils.getCount(sql);
                if (taxonId != null)
                {
                    Taxon  txRoot = (Taxon)session.getData("FROM Taxon WHERE id = " + taxonId);
                    Taxon  parent = txRoot;
                    
                    for (TaxonTreeDefItem item : treeDefItems)
                    {
                        if (item.getRankId() > 0)
                        {
                            Taxon taxon = createTaxon(item, parent, session);
                            parent = taxon;
                            
                            if (doCleanup)
                            {
                            	try
                            	{
                            		session.beginTransaction();
                            		session.save(taxon);
                            		session.commit();
                                
                                
                            	} catch (Exception ex)
                            	{
                            		session.rollback();
                            	}
                            }
                    		placeHolderTreeHash.put(item.getRankId(), taxon);
                            
                        }
                    }
                } else
                {
                    log.error("Couldn't find the Root Taxon Node");
                }
                
            } catch(Exception ex)
            {
                //session.rollback();
                ex.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }

        }
    }
    
    
    /**
     * @return the treeDefItemHash
     */
    public HashMap<Integer, TaxonTreeDefItem> getTreeDefItemHash()
    {
        buildPlaceHolderInfo();
        return treeDefItemHash;
    }

    /**
     * @return the rankParentHash
     */
    public HashMap<Integer, Integer> getRankParentHash()
    {
        buildPlaceHolderInfo();
        return rankParentHash;
    }
    
    /**
     * @return the taxonTreeDef
     */
    public TaxonTreeDef getTaxonTreeDef()
    {
        return taxonTreeDef;
    }
    
}

