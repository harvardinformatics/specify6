/* Filename:    $RCSfile: ExpressResultsTableInfo.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.core;

import static edu.ku.brc.specify.helpers.UIHelper.getBoolean;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.dom4j.Element;

/**
 * Hold information about the subset of returns results. Each Express Search can return results from several different 
 * DB tables of information. This information is constructed from an XML descrption
 * @author rods
 *
 */
public class ExpressResultsTableInfo
{
    public enum LOAD_TYPE {Building, Viewing, Both};
    
    protected LOAD_TYPE        loadType;
    
    protected String           tableId;
    protected String           title;
    
    // These are useed for viewing the results
    protected String           iconName  = null;
    protected String           viewSql;
    protected Vector<Integer>  recIds    = new Vector<Integer>();
    protected int[]            cols      = null;
    
    // These data members are use for indexing
    protected boolean                   useHitsCache = false;
    protected String                    buildSql;
    protected String[]                  colNames     = null;
    protected Hashtable<String, String> outOfDate    = new Hashtable<String, String>();
    protected Vector<Integer>           indexes      = new Vector<Integer>();
    
    /**
     * Constructs a table info object
     * @param tableElement the DOM4J element representing the information
     * @param loadType what type of info to load from the DOM
     */
    public ExpressResultsTableInfo(final Element tableElement, final LOAD_TYPE loadType)
    {
        this.loadType = loadType;
        
        fill(tableElement);
    }
    
    /**
     * Fill the current object with the info from the DOM depending on the LOAD_TYPE
     * @param tableElement the DOM4J element used to fill the object
     */
    public void fill(final Element tableElement)
    {
        tableId      = tableElement.attributeValue("id");
        title        = tableElement.attributeValue("title");
        
        String uhcStr = tableElement.attributeValue("usehitscache");
        useHitsCache  = uhcStr == null || uhcStr.length() == 0 ? false : getBoolean(uhcStr);
        
       // This info is used for indexing
        if (loadType == LOAD_TYPE.Building || loadType == LOAD_TYPE.Both)
        {
            List tables = tableElement.selectNodes("outofdate/table");
            for ( Iterator iter = tables.iterator(); iter.hasNext(); ) 
            {
                Element tblElement = (Element)iter.next();
                outOfDate.put(tblElement.attributeValue("name"), tblElement.attributeValue("title"));
            }  
            
            Element indexElement = (Element)tableElement.selectSingleNode("index");
            
            buildSql  = indexElement.selectSingleNode("sql").getText();
                            
            List colItems = indexElement.selectNodes("cols/col");
            cols = new int[colItems.size()];
            for (int i=0;i<colItems.size();i++)
            {
                Element colElement = (Element)colItems.get(i);
                cols[i] = Integer.parseInt(colElement.getTextTrim());
            }
        }
        
        if (loadType == LOAD_TYPE.Viewing || loadType == LOAD_TYPE.Both)
        {
            Element viewElement  = (Element)tableElement.selectSingleNode("detailView");
            
            viewSql  = viewElement.selectSingleNode("sql").getText();
            iconName = viewElement.attributeValue("icon");
           
            List captionItems = viewElement.selectNodes("captions/caption");
            if (captionItems.size() > 0)
            {
                colNames = new String[captionItems.size()];
                int i = 0;
                for ( Iterator capIter = captionItems.iterator(); capIter.hasNext(); ) 
                {
                    Element captionElement = (Element)capIter.next();
                    colNames[i++] = captionElement.attributeValue("text");
                }
            } else
            {
                //log.info("No Captions!");
            }
        }
        
    }
    
    public int getNumIndexes()
    {
        return indexes.size();
    }
    
    public void addIndex(int index)
    {
        indexes.add(index);
    }
    
    public int[] getIndexes()
    {
        int[] inxs = new int[indexes.size()];
        int inx = 0;
        for (Integer i : indexes)
        {
            inxs[inx++] = i;
        }
        indexes.clear();
        return inxs;
    }
    
    /**
     * Cleanup any memory
     */
    public void cleanUp()
    {
        if (recIds != null) recIds.clear();
        if (outOfDate != null) outOfDate.clear();
        colNames = null;
        cols     = null;
        viewSql  = null;
        buildSql = null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    public void finalize()
    {
        cleanUp();
    }
    
    public String[] getColNames()
    {
        return colNames;
    }


    public int[] getCols()
    {
        return cols;
    }


    public LOAD_TYPE getLoadType()
    {
        return loadType;
    }


    public Hashtable<String, String> getOutOfDate()
    {
        return outOfDate;
    }


    public Vector<Integer> getRecIds()
    {
        return recIds;
    }


    public String getTitle()
    {
        return title;
    }
    
    public String getViewSql()
    {
        
        return viewSql.replace("%s", getRecIdList());
    }
    
    public void setRecIds(Vector<Integer> recIds)
    {
        this.recIds = recIds;
    }

    public String getTableId()
    {
        return tableId;
    }
    
    public String getRecIdList()
    {
        StringBuffer idsStr = new StringBuffer();
        for (int i=0;i<recIds.size();i++)
        {
            if (i > 0) idsStr.append(',');
            idsStr.append(recIds.elementAt(i).toString());
        }
        return idsStr.toString();
    }

    public String getIconName()
    {
        return iconName;
    }
    
    public String getBuildSql()
    {
        return buildSql;
    }

    public boolean isUseHitsCache()
    {
        return useHitsCache;
    }
}

