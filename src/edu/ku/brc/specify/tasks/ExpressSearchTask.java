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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.expresssearch.ERTIJoinColInfo;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigDlg;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsSQL;
import edu.ku.brc.af.core.expresssearch.SearchConfig;
import edu.ku.brc.af.core.expresssearch.SearchConfigService;
import edu.ku.brc.af.core.expresssearch.SearchTableConfig;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResultsFromQuery;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.SearchBox;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.JAutoCompTextField;
import edu.ku.brc.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
/**
 * This task will enable the user to index the database and preform express searches. This is where the Express Search starts.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ExpressSearchTask extends BaseTask implements CommandListener, SQLExecutionListener, CustomQueryListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchTask.class);

    public static final String EXPRESSSEARCH      = "Express_Search";
    public static final String CHECK_INDEXER_PATH = "CheckIndexerPath";
    
    // Static Data Members
    protected static ExpressSearchTask      instance    = null;
    protected static final String           LAST_SEARCH = "lastsearch"; 
    
    // Data Members
    protected SearchBox                     searchBox;
    protected JAutoCompTextField            searchText;
    protected JButton                       searchBtn;
    protected Color                         textBGColor      = null;
    protected Color                         badSearchColor   = new Color(255,235,235);
    
    protected Vector<ESResultsSubPane>      paneCache        = new Vector<ESResultsSubPane>();
    
    protected Vector<SQLExecutionProcessor> sqlProcessorList = new Vector<SQLExecutionProcessor>();
    protected boolean                       sqlHasResults    = false;
    protected int                           sqlResultsCount  = 0;
    
    protected ESResultsSubPane              queryResultsPane = null;


    /**
     * Default Constructor.
     */
    public ExpressSearchTask()
    {
        super(EXPRESSSEARCH, getResourceString(EXPRESSSEARCH));
        icon = IconManager.getIcon("Search", IconManager.IconSize.Std16);
        
        closeOnLastPane = true;

        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(EXPRESSSEARCH, this);
        
        instance = this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
        }
    }
    
    /**
     * Returns true if the talk has been started and false if it hasn't.
     * @return true if the talk has been started and false if it hasn't.
     */
    public static boolean isStarted()
    {
        return instance != null;
    }

    
    /**
     * Check to see of the index has been run and then enables the express search controls.
     *
     */
    public void appContextHasChanged()
    {
        searchText.setPickListAdapter(PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        searchText.setText(localPrefs.get(LAST_SEARCH, ""));
        
        if (searchBtn != null)
        {
            searchBtn.setEnabled(true);
            searchText.setEnabled(true);
        }
        
        SearchConfigService.getInstance().reset();
    }

    /**
     * Performs the express search and returns the results.
     */
    protected void doQuery()
    {
        sqlHasResults = false;
        
        searchText.setBackground(textBGColor);
        String searchTerm = searchText.getText();
        if (isNotEmpty(searchTerm))
        {
            if (QueryAdjusterForDomain.getInstance().isUerInputNotInjectable(searchTerm.toLowerCase()))
            {
                ESResultsSubPane expressSearchPane = new ESResultsSubPane(searchTerm, this, true);
                doQuery(searchText, null, badSearchColor, expressSearchPane);
                AppPreferences.getLocalPrefs().put(LAST_SEARCH, searchTerm);
                
            } else
            {
                setUserInputToNotFound("ES_SUSPICIOUS_SQL", true);
            }
        }
    }
    
    /**
     * @param esrPane the main display pane
     * @param searchTableConfig the configuration information
     * @param searchTerm the search term for the search
     * @return the JPAQuery
     */
    protected static JPAQuery startSearchJPA(final ExpressSearchResultsPaneIFace esrPane,
                                             final SearchTableConfig searchTableConfig,
                                             final String            searchTerm)
    {
        JPAQuery jpaQuery = null;
        String sqlStr = searchTableConfig.getSQL(searchTerm, true, true);
        if (sqlStr != null)
        {
            instance.sqlResultsCount++;

            jpaQuery = new JPAQuery(sqlStr, instance);
            jpaQuery.setData(new Object[] {searchTableConfig, esrPane, searchTerm});
            jpaQuery.start();
        }
        return jpaQuery;
    }

    /**
     * Performs the express search and returns the results to the ExpressSearchResultsPaneIFace.
     * If the control is null then it will use the string.
     *
     * @param searchText the Text Control that contains the search string (can be null)
     * @param searchTextStr the Text Control that contains the search string (can be null)
     * @param badSearchColor the color to set the control if no results (can be null if searchText is null)
     * @param tables ExpressResultsTableInfo hash
     * @param esrPane the pane that the results will be set into
     * @return true if results were found, false if not results
     */
    public static void doQuery(final JTextField searchText,
                               final String     searchTextStr,
                               final Color      badSearchColor,
                               final ExpressSearchResultsPaneIFace esrPane)
    {
        String searchTerm = (searchText != null ? searchTerm = searchText.getText() : searchTextStr).toLowerCase();

        //boolean hasResults = true;
        if (searchTerm != null && searchTerm.length() > 0)
        {
            SearchTableConfig context = SearchConfigService.getInstance().getSearchContext();
            if (context == null)
            {
                SearchConfig config = SearchConfigService.getInstance().getSearchConfig();

                if (config.getTables().size() > 0)
                {
                    instance.sqlHasResults    = false;
                    instance.sqlResultsCount  = 0;
                    
                    instance.searchText.setEnabled(false);
                    instance.searchBtn.setEnabled(false);
                    UIRegistry.getStatusBar().setIndeterminate(true);
                    
    
                    Vector<JPAQuery> queryList = new Vector<JPAQuery>();
                    
                    for (SearchTableConfig table : config.getTables())
                    {
                        //log.debug("**************> " +table.getTableName() );
                        queryList.add(startSearchJPA(esrPane, table, searchTerm));
                    }
                } else
                {
                    searchText.getToolkit().beep();
                }
                
            } else
            {
                startSearchJPA(esrPane, context, searchTerm);
            }
        }
    }
    
    /**
     * Traverses the individual result record ids and maps them into the result tables.
     * @param config
     * @param tableId
     * @param searchTerm
     * @param resultSet
     * @param id
     * @param joinIdToTableInfoHash
     * @param resultsForJoinsHash the related results table
     */
    protected static void collectResults(final SearchConfig           config,
                                         final String                 tableId,
                                         final String                 searchTerm,
                                         final ResultSet              resultSet,
                                         final Integer                id,
                                         final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash,
                                         final Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash)
    {
        try
        {
            Integer recId;
            if (resultSet != null)
            {
                recId = resultSet.getInt(1);
            } else
            {
                recId = id;
            }
            
            //System.out.println("id "+id+"  "+recId);
            
            //log.debug("Find any Joins for TableID ["+tblInfo.getTableId()+"]");
            
            // Start by getting the List of next round of 'view' queries 
            // Before getting here we have constructed list of the queries that will need to be run
            // when we get a hit from the current table against one of the joins in the view query
            // 
            List<ExpressResultsTableInfo> list = joinIdToTableInfoHash.get(tableId);
            if (list != null)
            {
                // Now loop through each of the view queries
                for (ExpressResultsTableInfo erti : list)
                {
                    //log.debug("Not Active: "+erti.getId()+"  "+erti.getTitle()+"  "+config.isActiveForRelatedQueryId(erti.getId()));
                    if (!config.isActiveForRelatedQueryId(erti.getId()))
                    {
                        continue;
                    }
                    
                    //if (!erti.getId().equals("5")) // ZZZ
                    //{
                    //    continue;
                    //}
                    //log.debug("Checking up["+tblInfo.getTableId()+"]");
                    
                    QueryForIdResultsSQL results = resultsForJoinsHash.get(erti.getId());
                    if (results == null)
                    {
                        Integer joinColTableId = null;
                        ERTIJoinColInfo joinCols[] = erti.getJoins();
                        if (joinCols != null)
                        {
                            for (ERTIJoinColInfo jci :  joinCols)
                            {
                                if (tableId.equals(jci.getJoinTableId()))
                                {
                                    joinColTableId = jci.getJoinTableIdAsInt();
                                    break;
                                }
                            }
                        }
                        if (joinColTableId == null)
                        {
                            throw new RuntimeException("Shouldn't have got here!");
                        }
                        Integer displayOrder = SearchConfigService.getInstance().getSearchConfig().getOrderForRelatedQueryId(erti.getId());
                        log.debug("ExpressSearchResults erti.getId()["+erti.getId()+"] joinColTableId["+joinColTableId+"] displayOrder["+displayOrder+"]");
                        results = new QueryForIdResultsSQL(erti.getId(), joinColTableId, erti, displayOrder, searchTerm);
                        resultsForJoinsHash.put(erti.getId(), results);
                    }
                    results.add(recId);
                }
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Traverses through the results and adds to the panel to be displayed.
     * @param resultsMap the primary result tables
     * @param resultsForJoinsMap the related results table
     */
    protected static void displayResults(final ExpressSearchResultsPaneIFace           esrPane,
                                         final QueryForIdResultsIFace                  queryResults,
                                         final Hashtable<String, QueryForIdResultsSQL> resultsForJoinsMap)
    {
        // For Debug Only
        if (false)
        {
            for (Enumeration<QueryForIdResultsSQL> e=resultsForJoinsMap.elements();e.hasMoreElements();)
            {
                QueryForIdResultsSQL qfirsql = e.nextElement();
                if (qfirsql.getRecIds().size() > 0)
                {
                    log.debug("\n\n------------------------------------");
                    log.debug("------------------------------------");
                    log.debug("Search Id "+qfirsql.getTableInfo().getId() + 
                                       " Table Id "+qfirsql.getTableInfo().getTableId() + 
                                       " Column Name "+qfirsql.getJoinColTableId());
                    log.debug("------------------------------------");
                    for (Integer l : qfirsql.getRecIds())
                    {
                        log.debug(l+" ");
                    }
                }
            }
        }
        
        if (queryResults.getRecIds().size() > 0)//|| tableInfo.getNumIndexes() > 0)
        {
            esrPane.addSearchResults(queryResults);
        }
        
        for (Enumeration<QueryForIdResultsSQL> e=resultsForJoinsMap.elements();e.hasMoreElements();)
        {
            QueryForIdResultsSQL rs = e.nextElement();
            if (rs.getRecIds().size() > 0)
            {
                esrPane.addSearchResults(rs);
            }
        }
        resultsForJoinsMap.clear();
    }
    
    /**
     * @param recordSet
     */
    public void displayRecordSet(final RecordSetIFace recordSet)
    {
        SearchConfig      config            = SearchConfigService.getInstance().getSearchConfig();
        SearchTableConfig searchTableConfig = config.getSearchTableConfigById(recordSet.getDbTableId());
        if (searchTableConfig != null)
        {
            ESResultsSubPane esrPane = new ESResultsSubPane("", this, true);
            
            Hashtable<String, QueryForIdResultsSQL> resultsForJoinsHash = new Hashtable<String, QueryForIdResultsSQL>();
            QueryForIdResultsHQL results = new QueryForIdResultsHQL(searchTableConfig, new Color(30, 144, 255), recordSet);
            results.setExpanded(true);
            //results.setShouldInstallServices(false);
            displayResults(esrPane, results, resultsForJoinsHash);
            addSubPaneToMgr(esrPane);
        }
    }

    /**
     * @param searchName
     */
    public void doBasicSearch(final String searchName)
    {
        Hashtable<String, ExpressResultsTableInfo> idToTableInfoMap = ExpressSearchConfigCache.getSearchIdToTableInfoHash();
        for (ExpressResultsTableInfo erti : idToTableInfoMap.values())
        {
            log.error("["+erti.getName()+"]["+searchName+"]");
            if (erti.getName().equals(searchName))
            {
                // This needs to be fixed in that it might not return any results
                // and we are always adding the pane.
                ESResultsSubPane     expressSearchPane = new ESResultsSubPane(erti.getTitle(), this, true);
                QueryForIdResultsSQL         esr               = new QueryForIdResultsSQL(erti.getTitle(), null, erti, 0, "");
                @SuppressWarnings("unused")
                ExpressTableResultsFromQuery esrfq             = new ExpressTableResultsFromQuery(expressSearchPane, esr, true);
                addSubPaneToMgr(expressSearchPane);
                return;
            }
        }
        log.error("Can't find a search definition for name ["+searchName+"]");
    }
    
    /**
     * Executes and displays an HQL Query.
     * 
     * @param hqlStr the HQL query string
     */
    protected void doHQLQuery(final QueryForIdResultsIFace  results, final Boolean reusePanel)
    {
        if (reusePanel == null || !reusePanel)
        {
            ESResultsSubPane expressSearchPane = new ESResultsSubPane(getResourceString("ES_QUERY_RESULTS"), this, true);
            addSubPaneToMgr(expressSearchPane);
            expressSearchPane.addSearchResults(results);
            
        } else
        {
            if (queryResultsPane == null)
            {
                queryResultsPane = new ESResultsSubPane(getResourceString("ES_QUERY_RESULTS"), this, true);
                queryResultsPane.setIcon(IconManager.getIcon("Query", IconManager.IconSize.Std16));
                //addSubPaneToMgr(queryResultsPane);
                
            } else
            {
                queryResultsPane.reset();
//                int index = SubPaneMgr.getInstance().indexOfComponent(queryResultsPane.getUIComponent());
//                if (index == -1)
//                {
//                    addSubPaneToMgr(queryResultsPane);
//                } else
//                {
//                    SubPaneMgr.getInstance().showPane(queryResultsPane);
//                }
            }
            queryResultsPane.addSearchResults(results);
        }
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new SimpleDescPane(name, this, "This is the Express Search Pane");
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getNavBoxes()
     */
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel     searchPanel = new JPanel(gridbag);
        JLabel     spacer      = new JLabel("  ");

        searchBtn = new JButton(getResourceString("Search"));
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));
        HelpMgr.setHelpID(searchBtn, "Express_Search");
        
        //searchText  = new JTextField("[19510707 TO 19510711]", 10);//"beanii"
        //searchText  = new JTextField("beanii", 15);
                
        searchText = new JAutoCompTextField(15, PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        searchText.setAskBeforeSave(false);
        HelpMgr.registerComponent(searchText, "Express_Search");
        searchBox = new SearchBox(searchText, new SearchBoxMenuCreator());
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        searchText.setText(localPrefs.get(LAST_SEARCH, ""));
        textBGColor = searchText.getBackground();

        //searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));

        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doQuery();
            }
        };

        searchBtn.addActionListener(doQuery);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                }
            }
        });
        
        searchText.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showContextMenu(e);

            }
        });

        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        searchPanel.add(spacer);

        c.weightx = 0.0;
        gridbag.setConstraints(searchBox, c);
        searchPanel.add(searchBox);

        searchPanel.add(spacer);
        
        if (!UIHelper.isMacOS())
        {
            gridbag.setConstraints(searchBtn, c);
            searchPanel.add(searchBtn);
        }

        list.add(new ToolBarItemDesc(searchPanel, ToolBarItemDesc.Position.AdjustRightLastComp));

        return list;
    }
    
    /**
     * Shows the Reset menu.
     * @param e the mouse event
     */
    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("ES_TEXT_RESET"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    searchText.setEnabled(true);
                    searchText.setBackground(textBGColor);
                    searchText.setText("");
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    if (statusBar != null)
                    {
                        statusBar.setIndeterminate(false);
                    }
                }
            });
            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());

        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //----------------------------------------------------------------
    //-- CommandListener Interface
    //----------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(APP_CMD_TYPE))
        {
            if (cmdAction.isAction(APP_RESTART_ACT))
            {
                appContextHasChanged();
            }
            
        } else if (cmdAction.isType(EXPRESSSEARCH))
        {
            if (cmdAction.isAction("HQL"))
            {
                doHQLQuery((QueryForIdResultsIFace)cmdAction.getData(), (Boolean)cmdAction.getProperty("reuse_panel"));
                
            } else if (cmdAction.isAction("ExpressSearch"))
            {
                String searchTerm = cmdAction.getData().toString();
                ESResultsSubPane expressSearchPane = new ESResultsSubPane(searchTerm, this, true);
                doQuery(null, searchTerm, badSearchColor, expressSearchPane);
                
            } else if (cmdAction.isAction("Search"))
            {
                doBasicSearch(cmdAction.getData().toString());
                
            } else if (cmdAction.isAction("SearchComplete"))
            {
                doSearchComplete(cmdAction);
                
            } else if (cmdAction.isAction("ViewRecordSet"))
            {
                RecordSetIFace recordSet = null;
                if (cmdAction.getData() instanceof RecordSet)
                {
                    recordSet = (RecordSetIFace)cmdAction.getData();
                    
                } else if (cmdAction.getData() instanceof RolloverCommand)
                {
                    RolloverCommand roc = (RolloverCommand)cmdAction.getData();
                    if (roc.getData() instanceof RecordSet)
                    {
                        recordSet = (RecordSetIFace)roc.getData();
                    }
                }
                if (recordSet != null)
                {
                    displayRecordSet(recordSet);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(SubPaneIFace subPane)
    {
        if (subPane == queryResultsPane)
        {
            queryResultsPane = null;
        }
        super.subPaneRemoved(subPane);
    }

    //-------------------------------------------------------------
    // SQLExecutionListener Interface
    //-------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    //@Override
    public synchronized void exectionDone(final SQLExecutionProcessor process, final ResultSet resultSet)
    {
        if (!sqlHasResults)
        {
            try
            {
                sqlHasResults = resultSet.first();
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        sqlProcessorList.remove(process);
        
        Object[] data = (Object[])process.getData();
        if (data != null)
        {
            if (data.length == 3)
            {
                SearchTableConfig             searchTableConfig = (SearchTableConfig)data[0];
                ExpressSearchResultsPaneIFace esrPane           = (ExpressSearchResultsPaneIFace)data[1];
                String                        searchTerm        = (String)data[2];
                
                Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = ExpressSearchConfigCache.getSearchIdToTableInfoHash();
                Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfohash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
                Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash    = new Hashtable<String, QueryForIdResultsSQL>();
                
                try
                {
                    if (resultSet.next())
                    {
                        String                  searchIdStr = Integer.toString(searchTableConfig.getTableInfo().getTableId());
                        ExpressResultsTableInfo tblInfo = idToTableInfoHash.get(searchIdStr);
                        if (tblInfo == null)
                        {
                            throw new RuntimeException("Bad id from search["+searchIdStr+"]");
                        }
                        
                        SearchConfig config = SearchConfigService.getInstance().getSearchConfig();

                        do
                        {
                            collectResults(config, tblInfo.getTableId(), searchTerm, resultSet, null, joinIdToTableInfohash, resultsForJoinsHash);
                            
                        } while(resultSet.next());
                        
                        if (resultsForJoinsHash.size() > 0)
                        {
                            QueryForIdResultsSQL queryResults = new QueryForIdResultsSQL(searchIdStr, null, tblInfo, searchTableConfig.getDisplayOrder(), searchTerm);
                            displayResults(esrPane, queryResults, resultsForJoinsHash);
                        }
                    }
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                }

            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    //@Override
    public synchronized void executionError(SQLExecutionProcessor process, Exception ex)
    {
        sqlProcessorList.remove(process);
        
    }
    
    /**
     * Changes the UI component to show that nothing was found.
     */
    protected void setUserInputToNotFound(final String msgKey, final boolean isInError)
    {
        if (badSearchColor != null)
        {
            searchText.setBackground(badSearchColor);
        }
        searchText.setSelectionStart(0);
        searchText.setSelectionEnd(searchText.getText().length());
        searchText.getToolkit().beep();
        searchText.repaint();
        searchText.requestFocus();
        
        if (isInError)
        {
            UIRegistry.getStatusBar().setErrorMessage(getResourceString(msgKey));
        } else
        {
            UIRegistry.displayLocalizedStatusBarText(StringUtils.isNotEmpty(msgKey) ? msgKey : "NoExpressSearchResults");
        }
    }
    
    //-------------------------------------------------------------------------
    //-- CustomQueryListener Interface
    //-------------------------------------------------------------------------
    
    protected synchronized void completionUIHelper(final boolean decrement)
    {
        if (decrement)
        {
            instance.sqlResultsCount--;
        }
        
        if (instance.sqlResultsCount < 1)
        {
            searchText.setEnabled(true);
            searchBtn.setEnabled(true);
            
            if (!sqlHasResults)
            {
                setUserInputToNotFound(null, false);
                
            } else
            {
                searchText.setSelectionStart(searchText.getText().length());
                searchText.setSelectionEnd(searchText.getText().length());
                searchText.repaint();
                UIRegistry.getStatusBar().setText("");
            }
            
            CommandDispatcher.dispatch(new CommandAction(EXPRESSSEARCH, "SearchComplete"));
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void exectionDone(final CustomQueryIFace customQuery)
    {
        boolean addPane = false;
        JPAQuery jpaQuery = (JPAQuery)customQuery;
        List<?> list      = jpaQuery.getDataObjects();
        if (!sqlHasResults)
        {
            sqlHasResults = !jpaQuery.isInError() && list != null && list.size() > 0;
            addPane = sqlHasResults;
        }
        
        completionUIHelper(false);
        
        if (list != null)
        {
            if (list.size() > 0)
            {
                Object[]                      data              = (Object[])jpaQuery.getData();
                SearchTableConfig             searchTableConfig = (SearchTableConfig)data[0];
                ExpressSearchResultsPaneIFace esrPane           = (ExpressSearchResultsPaneIFace)data[1];
                String                        searchTerm        = (String)data[2];
                
                if (addPane)
                {
                    addSubPaneToMgr((SubPaneIFace)esrPane);
                }
        
                Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
                Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash   = new Hashtable<String, QueryForIdResultsSQL>();
                
                //log.debug("TID["+searchTableConfig.getTableInfo().getTableId() + "] Table Order ["+searchTableConfig.getDisplayOrder()+"] "+list.size());
                SearchConfig config = SearchConfigService.getInstance().getSearchConfig();
                
                String tableIdStr = Integer.toString(searchTableConfig.getTableInfo().getTableId());
                for (Object idObj : list)
                {
                    collectResults(config, tableIdStr, searchTerm, null, (Integer)idObj, joinIdToTableInfoHash, resultsForJoinsHash);
                }
                
                QueryForIdResultsHQL results = new QueryForIdResultsHQL(searchTableConfig, new Color(30, 144, 255), searchTerm, list);
                results.setMultipleSelection(true);
                displayResults(esrPane, results, resultsForJoinsHash);
                
                //joinIdToTableInfoHash.clear();
            }
           
        } else
        {
            log.error("List was null and cant't be");
        }
        
        completionUIHelper(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public void executionError(final CustomQueryIFace customQuery)
    {
        completionUIHelper(true);
    }
    
    //-------------------------------------------------------------------------
    //-- CustomQueryListener Interface
    //-------------------------------------------------------------------------
    public class SearchBoxMenuCreator implements SearchBox.MenuCreator
    {
        protected List<JComponent>    menus       = null;
        protected SearchConfigService scService;
        protected JMenuItem           allMenuItem  = null;
        protected JMenuItem           configMenuItem  = null;
        protected ActionListener      action;
        
        /**
         * Constructor.
         */
        public SearchBoxMenuCreator()
        {
            scService = SearchConfigService.getInstance();
            
            action = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getSource() == allMenuItem)
                    {
                        scService.setSearchContext(null);
                        
                    } else if (e.getSource() == configMenuItem)
                    {
                        ExpressSearchConfigDlg dlg = new ExpressSearchConfigDlg();
                        dlg.setVisible(true); // modal
                        dlg.cleanUp();
                        
                    } else
                    {
                        String miTitle = ((JMenuItem)e.getSource()).getText();
                        for (SearchTableConfig stc : scService.getSearchConfig().getTables())
                        {
                            if (stc.getTitle().equals(miTitle))
                            {
                                scService.setSearchContext(stc);
                            }
                        }
                    }
                }
            };
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#createPopupMenus()
         */
        public List<JComponent> createPopupMenus()
        {
            if (menus == null)
            {
                menus = new Vector<JComponent>();
                allMenuItem = new JMenuItem(getResourceString("All"), SearchBox.getSearchIcon()); // I18N
                allMenuItem.addActionListener(action);
                menus.add(allMenuItem);
                
                for (SearchTableConfig stc : scService.getSearchConfig().getTables())
                {
                    JMenuItem menu = new JMenuItem(stc.getTitle(), IconManager.getIcon(stc.getIconName(), IconManager.IconSize.Std16));
                    menu.addActionListener(action);
                    menus.add(menu);
                }
                
                configMenuItem = new JMenuItem(getResourceString("ESConfig"), IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16)); // I18N
                configMenuItem.addActionListener(action);
                menus.add(configMenuItem);
            }
            
            return menus;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#reset()
         */
        public void reset()
        {
            this.menus = null;
        }
    }
    
    /**
     * @param cmdAction
     */
    protected void doSearchComplete(CommandAction cmdAction)
    {
        UIRegistry.getStatusBar().setIndeterminate(false);
        if (cmdAction.getData() instanceof JPAQuery)
        {
            //currently, this means an instance of ResultSetTableModel sent it's result.
            if (queryResultsPane != null)
            {
                int     rowCount = ((JPAQuery) cmdAction.getData()).getDataObjects().size();
                boolean isError  = ((JPAQuery) cmdAction.getData()).isInError();
                boolean showPane = !isError && rowCount > 0;
                //print status bar msg if error or rowCount == 0, 
                //else show the queryResults pane if rowCount > 0
                int index = SubPaneMgr.getInstance().indexOfComponent(queryResultsPane.getUIComponent());
                if (index == -1)
                {
                    if (showPane)
                    {
                        addSubPaneToMgr(queryResultsPane);
                    }
                }
                else
                {
                    if (showPane)
                    {
                        SubPaneMgr.getInstance().showPane(queryResultsPane);
                    }
                    else
                    {
                        SubPaneMgr.getInstance().removePane(queryResultsPane, false);
                    }
                }
                if (isError)
                {
                    UIRegistry.getStatusBar().setErrorMessage(getResourceString("QB_RUN_ERROR"));
                }
                else if (rowCount == 0)
                {
                    UIRegistry.displayLocalizedStatusBarText("QB_NO_RESULTS");
                }
                else
                {
                    UIRegistry.getStatusBar().setText(null);
                }
            }
        }
    }
}
