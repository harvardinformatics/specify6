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

package edu.ku.brc.stats;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static edu.ku.brc.ui.UIHelper.getInt;
import static edu.ku.brc.ui.UIHelper.getString;

import javax.swing.Icon;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsGetter;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.QueryResultsProcessable;
import edu.ku.brc.ui.IconManager;

/**
 * Creates a pane that can listener for Query Results and then create a Pie Chart
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PieChartPanel extends ChartPanel implements QueryResultsListener, QueryResultsProcessable
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(BarChartPanel.class);

    // Data Members
    @SuppressWarnings("unused")
    private QueryResultsGetter       getter;
    @SuppressWarnings("unused")
    private QueryResultsContainer    qrContainer;
    private QueryResultsHandlerIFace handler = null;


    /**
     * CReates a Pie Chart.
     */
    public PieChartPanel()
    {
        super(getResourceString("BuildingPieChart"));

        getter      = new QueryResultsGetter(this);
        qrContainer = new QueryResultsContainer("Pie Chart");
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getIcon()
     */
    public Icon getIcon()
    {
        return IconManager.getIcon("Pie_Chart", IconManager.IconSize.Std16);
    }


    //--------------------------------------
    // QueryResultsProcessable
    //--------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#setHandler()
     */
    public void setHandler(final QueryResultsHandlerIFace handler)
    {
        this.handler = handler;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsProcessable#getHandler()
     */
    public QueryResultsHandlerIFace getHandler()
    {
        return handler;
    }

    //--------------------------------------
    // QueryResultsListener
    //--------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#allResultsBack()
     */
    public synchronized void allResultsBack()
    {
        // create a dataset...
        DefaultPieDataset dataset = new DefaultPieDataset();

        java.util.List<Object> list = handler.getDataObjects();
        for (int i=0;i<list.size();i++)
        {
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.setValue(getString(descObj), getInt(valObj));
        }
        list.clear();

        // create a chart...
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                false, // legend?
                true, // tooltips?
                false // URLs?
            );

        // create and display a frame...
        chartPanel = new org.jfree.chart.ChartPanel(chart, true, true, true, true, true);
        //setBackground(Color.BLUE);

        removeAll(); // remove progress bar

        /*
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,p,p:g", "f:p:g"));
        CellConstraints cc         = new CellConstraints();
        builder.add(panel, cc.xy(3,1));
        add(builder.getPanel(), BorderLayout.CENTER);
        */
        //add(chartPanel, BorderLayout.CENTER);
        
        setLayout(new ChartLayoutManager(this));
        
        add(chartPanel);

        validate();
        doLayout();
        repaint();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsListener#resultsInError(edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void resultsInError(final QueryResultsContainer qrc)
    {

    }




}
