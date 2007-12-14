/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.expresssearch.ExpressSearchSQLAdjuster;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 14, 2007
 *
 */
public class SpecifyExpressSearchSQLAdjuster extends ExpressSearchSQLAdjuster
{

    public SpecifyExpressSearchSQLAdjuster()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchSQLAdjuster#adjustSQL(java.lang.String)
     */
    @Override
    public String adjustSQL(String sql)
    {
        // SpecifyUser should NEVER be null nor the Id !
        SpecifyUser user = SpecifyUser.getCurrentUser();
        if (user != null)
        {
            Integer id = user.getId();
            if (id != null)
            {
                String adjSQL = sql;
                if (StringUtils.contains(adjSQL, "SPECIFYUSERID"))
                {
                    adjSQL = StringUtils.replace(adjSQL, "SPECIFYUSERID", Integer.toString(id));
                }
                
                if (StringUtils.contains(adjSQL, "DIVISIONID"))
                {
                    Integer divId = user.getAgent().getDivision() != null ? user.getAgent().getDivision().getDivisionId() : null;
                    if (divId != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, "DIVSIONID", Integer.toString(divId));
                    }
                }
                
                System.out.println(adjSQL);
                if (StringUtils.contains(adjSQL, "COLMEMID"))
                {
                    Collection collection = Collection.getCurrentCollection();
                    if (collection != null)
                    {
                        adjSQL = StringUtils.replace(adjSQL, "COLMEMID", Integer.toString(collection.getCollectionId()));
                    }
                }
                return adjSQL;
            } else
            {
                throw new RuntimeException("The SpecifyUser cannot be null!");
            }
        }
        return super.adjustSQL(sql);
    }
    
    protected void appendId(final StringBuilder sb, final String text, final Integer id)
    {
        sb.append("(");
        sb.append(text);
        sb.append(" AND CL:");  
        sb.append(id);  
        sb.append(")");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ExpressSearchSQLAdjuster#adjustExpressSearchText(java.lang.String)
     */
    @Override
    public String adjustExpressSearchText(String text)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer id : Collection.getCurrentCollectionIds())
        {
            if (sb.length() > 0)
            {
                sb.append(" OR ");
            }
            appendId(sb, text, id);
        }
        
        if (sb.length() > 0)
        {
            sb.append(" OR ");
        }
        appendId(sb, text, 0);
        System.out.println("["+sb.toString()+"]");
        return sb.toString();
    }
    
}
