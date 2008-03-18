package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;

public class DataObjFieldFormatDlg extends CustomDialog {

	protected DBTableInfo tableInfo;
	protected DataObjSwitchFormatter selectedFormat;
    protected Vector<DataObjSwitchFormatter> deletedFormats = new Vector<DataObjSwitchFormatter>(); 
	
	// UI controls
	protected JList formatList;
	protected DataObjFieldFormatPanelBuilder fmtSingleEditingPB;
	protected DataObjFieldFormatMultiplePanelBuilder fmtMultipleEditingPB;
	protected JComboBox valueFieldCbo;
	protected JRadioButton singleDisplayBtn;
	protected JRadioButton multipleDisplayBtn;
	
	protected ActionListener displayTypeRadioBtnL = null;
	protected ListSelectionListener formatListSelectionListener = null;
	
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public DataObjFieldFormatDlg(Frame                 frame, 
    					  		 DBTableInfo           tableInfo, 
    					  		 int                   initialFormatSelectionIndex) 
    	throws HeadlessException
    {
        super(frame, getResourceString("FFE_DLG_TITLE"), true, OKCANCELHELP, null); //I18N 
        this.tableInfo = tableInfo;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("10px,250px,10px,f:p:g,10px",  
        		"10px,"       + // empty space on top of panel 
        		"p,"          + // table name 
        		"10px,"       + // --- separator 
        		"p,"          + // help 
        		"10px,"       + // --- separator 
        		"p,"          + // available formats label,
        		"f:p:g,"      + // available formats list 
        		"10px,"       + // --- separator 
        		"p,"          + // delete button 
        		"15px"          // --- separator 
        		)/*, new FormDebugPanel()*/);
        
        // table info
        JLabel tableTitleLbl = new JLabel(getResourceString("FFE_TABLE") + ": " + 
        		tableInfo.getTitle(), SwingConstants.LEFT); 

        JLabel helpLbl = new JLabel("Define how records of this table are to be shown " +
        		"in a compact manner", SwingConstants.LEFT);

        // list of existing formats
        DefaultListModel listModel = new DefaultListModel();

        // add available data object formatters
        List<DataObjSwitchFormatter> fmtrs;
        fmtrs = DataObjFieldFormatMgr.getFormatterList(tableInfo.getClassObj());        
        for (DataObjSwitchFormatter format : fmtrs)
        {
        	listModel.addElement(format);
        }
        // add "New" string as last entry
        listModel.addElement("New");
        
        formatList = new JList(listModel);
        formatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hookFormatListSelectionListener();
        hookFormatListMouseListener();
        
        ActionListener deleteListener = new ActionListener()
        {
        	public void actionPerformed(ActionEvent e) 
        	{ 	
        		int index = formatList.getSelectedIndex();
        		DefaultListModel model = (DefaultListModel) formatList.getModel();
        		model.removeElement(selectedFormat);
        		index = (index >= model.getSize())? index - 1: index;
        		formatList.setSelectedIndex(index);
        		deletedFormats.add(selectedFormat);
        	}
        };
        
        // delete button
        PanelBuilder deletePB = new PanelBuilder(new FormLayout("l:p", "p"));  
        JButton deleteBtn = new JButton(getResourceString("FFE_DELETE")); 
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(deleteListener);
        deletePB.add(deleteBtn);
        
        // radio buttons (single/multiple/external object display formats
        singleDisplayBtn = new JRadioButton("Single display format");
        multipleDisplayBtn = new JRadioButton("Display depends on value of field:");
        
        ButtonGroup displayTypeGrp = new ButtonGroup();
        displayTypeGrp.add(singleDisplayBtn);
        displayTypeGrp.add(multipleDisplayBtn);
        hookupDisplayTypeRadioButtonListeners();
        
        // combo box that lists fields that can be selected when multiple display radio button is selected  
        DefaultComboBoxModel cboModel = new DefaultComboBoxModel();
        cboModel.addElement("First Name");
        cboModel.addElement("Last Name");
        cboModel.addElement("Separator");
        valueFieldCbo = new JComboBox(cboModel);
        
        // little panel to hold multiple display radio button and its combo box
        PanelBuilder multipleDisplayPB = new PanelBuilder(new FormLayout("l:p,l:p", "p"));  
        multipleDisplayPB.add(multipleDisplayBtn, cc.xy(1,1));
        multipleDisplayPB.add(valueFieldCbo,      cc.xy(2,1));

        // format editing panels (dependent on the type for format: single/multiple
        fmtSingleEditingPB = new DataObjFieldFormatSinglePanelBuilder(tableInfo);
        fmtMultipleEditingPB = new DataObjFieldFormatMultiplePanelBuilder(tableInfo);
        
        // panel for radio buttons and display formatting editing panel
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("f:p:g", "p,p,f:p:g")/*, new FormDebugPanel()*/);
        rightPB.add(singleDisplayBtn,                cc.xy(1,1));
        rightPB.add(multipleDisplayPB.getPanel(),    cc.xy(1,2));
        // both panels occupy the same space
        rightPB.add(fmtSingleEditingPB.getPanel(),   cc.xy(1,3));
        rightPB.add(fmtMultipleEditingPB.getPanel(), cc.xy(1,3));

        // sample list box
        String[] listItems = {"Salutation", "First Name", "Separator ','", "Last Name"};
        JList sampleList = new JList(listItems);
        sampleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        // lay out components on main panel        
        int y = 2; // leave first row blank 
        pb.add(tableTitleLbl,  cc.xyw(2, y, 3)); y += 2;
        pb.add(helpLbl,        cc.xyw(2, y, 3)); y += 2;
        
        pb.add(new JLabel("Display Formats:", SwingConstants.LEFT), cc.xy(2, y)); y += 1; 
        int y2 = y; // align 3rd column with this row 
        pb.add(new JScrollPane(formatList), cc.xy(2,y)); y += 2;

        pb.add(deletePB.getPanel(), cc.xy(2,y)); y += 2;
        
        // 2nd column (4th if you count the spaces between them)
        pb.add(rightPB.getPanel(), cc.xywh(4, y2, 1, 4));

        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // pack after largest of the two panels is visible then set the other one visible  
        //multipleDisplayBtn.setSelected(true);
        //singleDisplayBtn.setSelected(true);
        
        // after all is created, set initial selection on format list 
        //formatList.setSelectedIndex(initialFormatSelectionIndex);
        DataObjSwitchFormatter fmt = fmtrs.get(0);
        fillWithObjFormatter(fmt);

        pack();
    }

    /*
     * Populates the dialog controls with data from a given formatter
     */
    protected void fillWithObjFormatter(DataObjSwitchFormatter fmt)
    {
    	boolean isSingle = (fmt == null)? singleDisplayBtn.isSelected() : fmt.isSingle();
    	if (isSingle)
    	{
    		singleDisplayBtn.setSelected(true);
    		setVisibleFormatPanel(singleDisplayBtn);
    		fmtSingleEditingPB.fillWithObjFormatter(fmt);
    	} else
    	{
    		multipleDisplayBtn.setSelected(true);
    		setVisibleFormatPanel(multipleDisplayBtn);
    		updateFieldValueCombo(fmt);
    		fmtMultipleEditingPB.fillWithObjFormatter(fmt);
    	}
    }
    
    /*
     * Populates the field value combo with fields and leaves the right one selected (for multiple formats) 
     */
    protected void updateFieldValueCombo(DataObjSwitchFormatter switchFormatter)
    {
    	// clear combo box list
    	DefaultComboBoxModel cboModel = (DefaultComboBoxModel) valueFieldCbo.getModel();
    	cboModel.removeAllElements();
    	
    	// add fields to combo box
    	List<DBFieldInfo> fields = tableInfo.getFields();
    	int selectedFieldIndex = -1;
    	for (int i = 0; i < fields.size(); ++i)
    	{
    		DBFieldInfo currentField = fields.get(i);
    		cboModel.addElement(currentField);
    		if (currentField.getName().equals(switchFormatter.getFieldName()))
    		{
    			// found the selected field
    			selectedFieldIndex = i;
    		}
    	}
    	
    	// set selected field
    	valueFieldCbo.setSelectedIndex(selectedFieldIndex);
    }
    
    private void hookFormatListMouseListener()
    {
    	MouseAdapter mAdp = new MouseAdapter()
    	{
    		public void mouseClicked(MouseEvent e)
    		{
    			if(e.getClickCount() == 2)
    			{
    				int index = formatList.locationToIndex(e.getPoint());
    				formatList.ensureIndexIsVisible(index);
    				okButtonPressed();
    			}
    		}
    	};
    	
    	formatList.addMouseListener(mAdp);
    }
    
	private void hookFormatListSelectionListener() 
	{
		if (formatListSelectionListener == null)
		{
	        formatListSelectionListener = new ListSelectionListener()
	        {
	        	public void valueChanged(ListSelectionEvent e)
	        	{
	        	    if (e.getValueIsAdjusting()) 
	        	    	return;
	
	        	    JList theList = (JList) e.getSource();
	        	    if (theList.isSelectionEmpty())
	        	    {
	        	    	return;
	        	    }
	
        	    	Object selValue = theList.getSelectedValue();
	        	    if (selValue instanceof DataObjSwitchFormatter)
	        	    {
	        	    	setSelectedFormat((DataObjSwitchFormatter) selValue);
	        	    }
	        	    else
	        	    {
	        	    	// set selected formatter to null
	        	    	setSelectedFormat(null);
	        	    }
	        	}
	        };
		}
		
        formatList.addListSelectionListener(formatListSelectionListener);
	}

	protected void setSelectedFormat(DataObjSwitchFormatter format)
	{
		fillWithObjFormatter(format);
	}
	
	protected void formatTextChanged()
	{
		// XXX may need to move this to single panel builder
		
		// if new value cannot be found among those listed, then it's a new one
		DefaultListModel listModel = (DefaultListModel) formatList.getModel();
		Enumeration elements = listModel.elements();
		int i = 0;
		int index = -1;
		while (elements.hasMoreElements())
		{
			Object obj = elements.nextElement();
			if (obj instanceof DataObjSwitchFormatter)
			{
				DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) obj;
				// XXX format text isn't here
				// Finish implementation
			}
		}
		
	
		if (index != -1) {
			// it's a new format
		}
		
		// detach selection listeners from formatList, change value to New (last on the list)
		// and attach listeners again
		
	}
	
	protected void hookupDisplayTypeRadioButtonListeners()
    {
    	if (displayTypeRadioBtnL == null)
    	{
    		displayTypeRadioBtnL = new ActionListener()
    		{
    			public void actionPerformed(ActionEvent e)
    			{
    				if (e.getSource() instanceof JRadioButton)
    				{
    					JRadioButton btn = (JRadioButton) e.getSource();
    					setVisibleFormatPanel(btn);
    				}
    			}
    		};
    	}
    	singleDisplayBtn.addActionListener(displayTypeRadioBtnL);
    	multipleDisplayBtn.addActionListener(displayTypeRadioBtnL);
    }

	protected void updateSample()
	{
		// no sample being used, just a placeholder
		return;
	}
	
    protected void setVisibleFormatPanel(JRadioButton btn)
    {
    	fmtSingleEditingPB.getPanel().setVisible  (btn == singleDisplayBtn); 
    	fmtMultipleEditingPB.getPanel().setVisible(btn == multipleDisplayBtn);
    }
    
    /**
     * 
     */
    protected void updateUIEnabled()
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
/*    	UIFieldFormatterMgr instance = UIFieldFormatterMgr.getInstance();
    	
    	// remove non-system formatters marked for deletion
    	Iterator<UIFieldFormatterIFace> it = deletedFormats.iterator();
    	while (it.hasNext()) 
    	{
    		instance.removeFormatter(it.next());
    	}
    	
    	// save formatter if new
    	if (formatIsNew) 
    	{
    		// if format is new, maybe the "by year" field hasn't been flipped correctly 
    		// (i.e. if formatter was created (ie. by a key press) after the checkbox was clicked
    		selectedFormat.setByYear(byYearCB.isSelected());
    		
    		// add formatter to list of existing ones and save it
    		instance.addFormatter(selectedFormat);
    		instance.save();
    	}
*/        
        super.okButtonPressed();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        // TODO Auto-generated method stub
        super.cleanUp();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#getOkBtn()
     */
    @Override
    public JButton getOkBtn()
    {
        // TODO Auto-generated method stub
        return super.getOkBtn();
    }
}
