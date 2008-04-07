/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.db.ERTICaptionInfo;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Provides data from a already executed and displayed QB query.
 */
public class QBLiveJRDataSource extends QBJRDataSourceBase
{
    protected final ResultSetTableModel data;
    protected int row = -1;
    
    /**
     * @param data
     * @param columnInfo
     */
    public QBLiveJRDataSource(final ResultSetTableModel data, final List<ERTICaptionInfo> columnInfo)
    {
        super(columnInfo);
        this.data = data;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    @Override
    public Object getFieldValue(JRField arg0) throws JRException
    {
        // TODO Auto-generated method stub
        int fldIdx = getFldIdx(arg0.getName());
        if (fldIdx < 0)
            return null;
        return processValue(fldIdx, data.getCacheValueAt(row, fldIdx));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceBase#next()
     */
    @Override
    public boolean next() throws JRException
    {
        return ++row < data.getRowCount();
    }    
}
