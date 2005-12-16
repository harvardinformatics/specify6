/* Filename:    $RCSfile: ExpressTableResultsBase.java,v $
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

package edu.ku.brc.specify.core.subpane;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.GradiantButton;
import edu.ku.brc.specify.ui.GradiantLabel;
import edu.ku.brc.specify.ui.TriangleButton;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.specify.core.*;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where 
 * supplied as an "in" clause.
 * 
 * @author rods
 *
 */
class ExpressTableResultsBase extends JPanel
{
    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    protected ExpressSearchResultsPane esrPane;
    protected JTable                table;
    protected JPanel                tablePane;
    protected TriangleButton        expandBtn;
    protected GradiantButton        showTopNumEntriesBtn;
    protected int                   rowCount = 0;
    protected boolean               showingAllRows = false;
   
    protected JPanel                morePanel     = null;       
    protected Color                 bannerColor   = new Color(30, 144, 255);   
    protected int                   topNumEntries = 7;
    protected String[]              colNames      = null;
    
    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent 
     * @param title the title of the resulys
     * @param sqlStr the SQL string used to populate the results
     * @param colNameMappings the mappings for the column names
     */
    public ExpressTableResultsBase(final ExpressSearchResultsPane esrPane, 
                                   final ExpressResultsTableInfo tableInfo)
    {
        super(new BorderLayout());
        
        this.esrPane  = esrPane;
        this.colNames = tableInfo.getColNames();
        
        table = new JTable();
        table.setShowVerticalLines(false);
        setBackground(table.getBackground());
        
        GradiantLabel vl = new GradiantLabel(tableInfo.getTitle(), JLabel.LEFT);
        vl.setForeground(bannerColor);
        vl.setTextColor(Color.WHITE);
        
        expandBtn = new TriangleButton();
        expandBtn.setForeground(bannerColor);
        expandBtn.setTextColor(Color.WHITE);
  
        showTopNumEntriesBtn = new GradiantButton(String.format(getResourceString("ShowTopEntries"), new Object[] {topNumEntries}));
        showTopNumEntriesBtn.setForeground(bannerColor);
        showTopNumEntriesBtn.setTextColor(Color.WHITE);
        showTopNumEntriesBtn.setVisible(false);
        showTopNumEntriesBtn.setCursor(handCursor);
        
        FormLayout      formLayout = new FormLayout("p,0px,p:g,0px,p,0px,p,0px,p", "center:p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();

        int col = 1;
        builder.add(expandBtn, cc.xy(col,1));
        col += 2;
        
        builder.add(vl, cc.xy(col,1));
        col += 2;
        
        builder.add(showTopNumEntriesBtn, cc.xy(col,1));
        col += 2;
        
        GradiantButton labelsBtn = new GradiantButton(IconManager.getImage("Labels", IconManager.IconSize.Std16));
        labelsBtn.setForeground(bannerColor);
        builder.add(labelsBtn, cc.xy(col,1));
        col += 2;
        
        CloseButton closeBtn = new CloseButton();
        closeBtn.setForeground(bannerColor);
        closeBtn.setCloseColor(new Color(255,255,255, 90));
        builder.add(closeBtn, cc.xy(col,1));
        col += 2;
        
        add(builder.getPanel(), BorderLayout.NORTH);
        
        tablePane = new JPanel(new BorderLayout());
        tablePane.setLayout(new BorderLayout());
        tablePane.add(table.getTableHeader(), BorderLayout.PAGE_START);
        tablePane.add(table, BorderLayout.CENTER);

        add(tablePane, BorderLayout.CENTER);
        
        expandBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                boolean isExpanded = !expandBtn.isDown();
                
                expandBtn.setDown(isExpanded);
                
                tablePane.setVisible(isExpanded);               
                
                if (!showingAllRows && morePanel != null)
                {
                    morePanel.setVisible(isExpanded);
                }
                invalidate();
                doLayout();
                esrPane.revalidateScroll();
            }
        });
        
        showTopNumEntriesBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                morePanel.setVisible(true);
                showTopNumEntriesBtn.setVisible(false);
                showingAllRows = false;
                setDisplayRows(rowCount, topNumEntries);
                
                // If it is collapsed then expand it
                if (!expandBtn.isDown())
                {
                    tablePane.setVisible(true);
                    expandBtn.setDown(true);
                }
                
                // Make sure the layout is updated
                invalidate();
                doLayout();
                esrPane.revalidateScroll();
            }
        });
        
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        removeMe();
                    }
                  });
              
            }
        });
        
    }
    
    /**
     * 
     *
     */
    protected void configColumnNames()
    {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

        TableColumnModel tableColModel = table.getColumnModel();
        for (int i=0;i<tableColModel.getColumnCount();i++) 
        {
            tableColModel.getColumn(i).setCellRenderer(renderer);
            if (colNames != null)
            {
                String label = (String)tableColModel.getColumn(i).getHeaderValue();
                if (label != null )
                {
                    tableColModel.getColumn(i).setHeaderValue(colNames[i]);
                }
            }
        }
    }
    
    /**
     * 
     *
     */
    protected void buildMorePanel()
    {
        FormLayout      formLayout = new FormLayout("15px,0px,p", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();
        
        JButton btn = new JButton(String.format(getResourceString("MoreEntries"), new Object[] {(rowCount - topNumEntries)}));//(rowCount - topNumEntries)+" more...");
        btn.setCursor(handCursor);

        btn.setBorderPainted(false);
        builder.add(new JLabel(" "), cc.xy(1,1));
        builder.add(btn, cc.xy(3,1));
        
        morePanel = builder.getPanel();
        Color bgColor = table.getBackground();
        bgColor = new Color(Math.max(bgColor.getRed()-10, 0), Math.max(bgColor.getGreen()-10, 0), Math.max(bgColor.getBlue()-10, 0));
        
        Color fgColor = new Color(Math.min(bannerColor.getRed()+10, 255), Math.min(bannerColor.getGreen()+10, 255), Math.min(bannerColor.getBlue()+10, 255));
        morePanel.setBackground(bgColor);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        add(builder.getPanel(), BorderLayout.SOUTH);
        
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                morePanel.setVisible(false);
                showTopNumEntriesBtn.setVisible(true);
                showingAllRows = true;
                setDisplayRows(rowCount, Integer.MAX_VALUE);
                esrPane.revalidateScroll();
            }
        });
        
    }
    
    /**
     * Aks parent to remove this table
     */
    protected void removeMe()
    {
        esrPane.removeTable(this);
    }
    
    /**
     * Creates an array of indexes
     * @param rows the number of rows to be displayed
     * @return an array of indexes
     */
    protected int[] createIndexesArray(final int rows)
    {
        int[] indexes = new int[rows];
        for (int i=0;i<rows;i++)
        {
            indexes[i] = i;
        }
        return indexes;            
    }
    
    /**
     * Display the 'n' number of rows up to topNumEntries
     * 
     * @param numRows the desired number of rows
     */
    protected void setDisplayRows(final int numRows, final int maxNum)
    {
        int rows = Math.min(numRows, maxNum);
        ResultSetTableModel rsm = (ResultSetTableModel)table.getModel();
        rsm.initializeDisplayIndexes();
        rsm.addDisplayIndexes(createIndexesArray(rows));
       
    }
}
