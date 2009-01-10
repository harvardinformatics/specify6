/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.ireportspecify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection;
import edu.ku.brc.specify.tasks.subpane.qb.QBJRDataSourceConnection.QBJRFieldDef;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class ReportRepeatPanel extends JPanel
{
    private static final Logger log = Logger.getLogger(ReportRepeatPanel.class);

    /**
     * List of fields that can be used to determine repeats.
     */
    protected final Vector<QBJRFieldDef> validFields; 
    protected JTextField constantTxt;
    protected JComboBox fldCombo;
    protected JComboBox typeCombo;
    protected JButton cancelBtn;
    
    
    //values for typeCombo selection indexes
    private static final int CONST = 1;
    private static final int DEFAULT = 0;
    private static final int FIELD = 2;
    /**
     * @param connection
     */
    public ReportRepeatPanel(final QBJRDataSourceConnection connection, final JButton cancelBtn)
    {
        super();
        validFields = bldValidFields(connection);
        this.cancelBtn = cancelBtn;
    }

    /**
     * @return list of fields that can be used to determine repeats. (ie: numeric fields)
     */
    protected Vector<QBJRFieldDef> bldValidFields(final QBJRDataSourceConnection connection)
    {
        Vector<QBJRFieldDef> result = new Vector<QBJRFieldDef>();
       //testing only...
//        if (connection == null)
//        {
//            QBJRDataSourceConnection junk = new QBJRDataSourceConnection("junk");
//            result.add(junk.new QBJRFieldDef("Ein", Integer.class));
//            result.add(junk.new QBJRFieldDef("Zwei", Integer.class));
//            return result;
//        }
        for (int f = 0; f < connection.getFields(); f++)
        {
            QBJRFieldDef fld = connection.getField(f);
            if (Number.class.isAssignableFrom(fld.getFldClass()))
            {
                result.add(fld);
            }
        }
        return result;
    }

    protected String[] getTypeChoices()
    {
        if (validFields.size() > 0)
        {
            String[] result = {UIRegistry.getResourceString("REP_DEFAULT_REPEAT"), 
                UIRegistry.getResourceString("REP_CONST_REPEAT"),
                UIRegistry.getResourceString("REP_FLD_REPEAT")};
            return result;
        }
        String[] result = {UIRegistry.getResourceString("REP_DEFAULT_REPEAT"), 
            UIRegistry.getResourceString("REP_CONST_REPEAT")};
        return result;
    }
    
    protected void createUI(final Object reps)
    {
        String repFldName = null; 
        Integer constRep = null;
        if (reps != null)
        {
            if (reps instanceof Integer)
            {
                constRep = (Integer )reps;
            }
            else if (reps instanceof String)
            {
                repFldName = (String )reps;
            }
            else
            {
                log.error("invalid repeats parameter: " + reps);
            }
        }
        PanelBuilder builder = new PanelBuilder(new FormLayout("left:p, fill:p:grow", "c:p"), this);
        CellConstraints cc = new CellConstraints();
        
        String[] chcs = getTypeChoices();
        typeCombo = UIHelper.createComboBox(chcs);
        typeCombo.addActionListener(new ActionListener()
        {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                //System.out.println("typeCombo selectedIndex = " + typeCombo.getSelectedIndex());
                constantTxt.setVisible(typeCombo.getSelectedIndex() == CONST);
                fldCombo.setVisible(typeCombo.getSelectedIndex() == FIELD);
                //ReportRepeatPanel.this.repaint();
            }
            
        });
        builder.add(typeCombo, cc.xy(1,1));
        
        constantTxt = UIHelper.createTextField("2");
        constantTxt.addFocusListener(new FocusListener()
        {

            /* (non-Javadoc)
             * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
             */
            //@Override
            public void focusGained(FocusEvent e)
            {
                //nothing
            }

            /* (non-Javadoc)
             * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
             */
            //@Override
            public void focusLost(FocusEvent e)
            {
                if (!e.isTemporary() && e.getOppositeComponent() != typeCombo && e.getOppositeComponent() != cancelBtn)
                {
                    boolean OK = true;
                    try
                    {
                        Integer entry = Integer.valueOf(constantTxt.getText().trim());
                        OK = entry > 1;
                    }
                    catch (NumberFormatException ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ReportRepeatPanel.class, ex);
                        //Damn, no status bar 
                        //UIRegistry.getStatusBar().setErrorMessage(UIRegistry.getResourceString("REP_INVALID_REPEAT"));
                        UIRegistry.showLocalizedMsg("REP_INVALID_COUNT_TITLE", "REP_INVALID_REPEAT_COUNT_MSG");
                        OK = false;
                    }
                    if (!OK)
                    {
                        constantTxt.grabFocus();
                    }
                }
                
            }
            
        });
        builder.add(constantTxt, cc.xy(2,1));
        
        fldCombo = UIHelper.createComboBox(validFields);
        builder.add(fldCombo, cc.xy(2, 1));
        
        typeCombo.setSelectedIndex(DEFAULT);
        if (repFldName != null)
        {
            for (int f = 0; f < validFields.size(); f++)
            {
                if (validFields.get(f).getFldName().equals(repFldName))
                {
                    fldCombo.setSelectedIndex(f);
                    typeCombo.setSelectedIndex(FIELD);
                    break;
                }
            }
        }
        else if (constRep != null)
        {
            constantTxt.setText(String.valueOf(constRep));
            typeCombo.setSelectedIndex(CONST);
        }
            
    }

    public Object getRepeats()
    {
        if (typeCombo.getSelectedIndex() == DEFAULT || typeCombo.getSelectedIndex() == -1)
        {
            return null;
        }
        if (typeCombo.getSelectedIndex() == CONST)
        {
            return Integer.valueOf(constantTxt.getText().trim());
        }
        if (typeCombo.getSelectedIndex() == FIELD)
        {
            return ((QBJRFieldDef )fldCombo.getSelectedItem()).getFldName();
        }
        log.error("Unrecognized repeat type: " + typeCombo.getSelectedItem());
        return null;
    }
    
    public boolean validInputs()
    {
        if (typeCombo.getSelectedIndex() == CONST)
        {
            try
            {
                Integer.valueOf(constantTxt.getText().trim());
                return true;
            }
            catch (NumberFormatException nfe)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ReportRepeatPanel.class, nfe);
                UIRegistry.showLocalizedMsg("REP_INVALID_COUNT_TITLE", "REP_INVALID_REPEAT_COUNT_MSG");
                return false;
            }
        }
        return true;
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ReportRepeatPanel rpp = new ReportRepeatPanel(null, null);
        rpp.createUI(6);
        CustomDialog cd = new CustomDialog(null, "Blah", true, rpp);
        UIHelper.centerAndShow(cd);
        System.exit(0);
    }

}
