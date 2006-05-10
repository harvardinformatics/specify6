package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.core.NavBox;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.WorkbenchPane;
import edu.ku.brc.specify.ui.CommandDispatcher;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;

public class WorkbenchTask extends BaseTask {
	private static Log log = LogFactory.getLog(WorkbenchTask.class);
	public static final DataFlavor WORKBENCH_FLAVOR = new DataFlavor(WorkbenchTask.class, "Workbench");
	public static final String WORKBENCH = "Workbench";
    
    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus  = new Vector<JComponent>();

	public WorkbenchTask() {
		super(WORKBENCH, getResourceString(WORKBENCH));
		CommandDispatcher.register(WORKBENCH, this);
		NavBox navBox = new NavBox(getResourceString("File"));
		navBox.add(NavBox.createBtn(getResourceString("New_Workbench"), name, IconManager.IconSize.Std16));
		navBox.add(NavBox.createBtn(getResourceString("New_Template"), name, IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);
		//      
		navBox = new NavBox(getResourceString("Templates"));
		navBox.add(NavBox.createBtn(getResourceString("Field_Book_Entry"),	name, IconManager.IconSize.Std16, new ImportFieldBookAction()));
		navBox.add(NavBox.createBtn(getResourceString("Label_Entry"), name,	IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);

		navBox = new NavBox(getResourceString("Workbenches"));
		navBox.add(NavBox.createBtn(getResourceString("Lawrence_River"), name,IconManager.IconSize.Std16));
		navBox.add(NavBox.createBtn(getResourceString("Smith_Collection"), name, IconManager.IconSize.Std16));
		navBoxes.addElement(navBox);
	}
		// super(getResourceString("Workbench"));
		// TODO Auto-generated constructor stub
		//NavBox navBox = new NavBox(name);

    public SubPaneIFace getStarterPane()
    {
        return new WorkbenchPane(name, this,null);
    }
     
    public List<ToolBarItemDesc> getToolBarItems()
    {
//        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
//        ToolBarDropDownBtn btn = createToolbarButton(name, "workbench.gif", "workbench_hint");      
//        list.add(new ToolBarItemDesc(btn));
//        return list;
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        ToolBarDropDownBtn btn = createToolbarButton(name, "workbench.gif", "workbench_hint");   
        
        list.add(new ToolBarItemDesc(btn));
        return list;    	
//    	return null;
    }
    
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        
        return list;
    }
    
    protected void importFieldNotebook()
    {
		JFileChooser jfc = new JFileChooser();
		FileFilter filter = new FileFilter()
		{
			public boolean accept(File f)
			{
				if( f.getName().length() < 4 )
				{
					return false;
				}
				String nameEnd = f.getName().substring(f.getName().length()-4);
				if( nameEnd.equalsIgnoreCase(".csv") || nameEnd.equalsIgnoreCase(".tab") )
					return true;
				return false;
			}
			public String getDescription()
			{
				return "csv files";
			}
		};
		jfc.setFileFilter(filter);
		int result = jfc.showOpenDialog(UICacheManager.get(UICacheManager.TOPFRAME));
		if( result == JFileChooser.APPROVE_OPTION )
		{
			File csvFile = jfc.getSelectedFile();
			log.info("Importing field notebook: " + csvFile.getAbsolutePath() );
			UICacheManager.getSubPaneMgr().addPane(new WorkbenchPane("Field Notebook Import", this, jfc.getSelectedFile()));
		}
    }
    
    public class ImportFieldBookAction implements ActionListener
    {
		public void actionPerformed(ActionEvent e)
		{
			importFieldNotebook();
		}
    }
}
