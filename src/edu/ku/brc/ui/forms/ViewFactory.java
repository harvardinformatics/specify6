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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.forms.validation.UIValidator.parseValidationType;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.BrowserLauncher;
import edu.ku.brc.ui.BrowseBtnPanel;
import edu.ku.brc.ui.ColorChooser;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.ui.db.TextFieldWithInfo;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormCellCommand;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormCellPanel;
import edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubView;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.ui.forms.validation.UIValidatable;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValCheckBox;
import edu.ku.brc.ui.forms.validation.ValComboBox;
import edu.ku.brc.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.ui.forms.validation.ValListBox;
import edu.ku.brc.ui.forms.validation.ValPasswordField;
import edu.ku.brc.ui.forms.validation.ValSpinner;
import edu.ku.brc.ui.forms.validation.ValTextArea;
import edu.ku.brc.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.forms.validation.ValidatedJPanel;

/**
 * Creates FormViewObj object that implment the Viewable interface.
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class ViewFactory
{
    // Statics
    private static final Logger log = Logger.getLogger(ViewFactory.class);
    private static final ViewFactory  instance = new ViewFactory();

    // Data Members
    protected static ColorWrapper     viewFieldColor = null;

    protected MultiView               rootMultiView  = null; // transient - is valid only during a build process

    /**
     * Constructor.
     */
    protected ViewFactory()
    {
        // do nothing
    }

    /**
     * Returns the singleton for the ViewSetMgr.
     * @return the singleton for the ViewSetMgr
     */
    public static ViewFactory getInstance()
    {
        return instance;
    }

    /**
     * Creates a panel with the "..." icon.
     * @param comp the component to put into the panel
     */
    public JPanel createIconPanel(final JComponent comp)
    {
        JPanel  panel = new JPanel(new BorderLayout());
        JButton btn   = new JButton("...");
        panel.add(btn, BorderLayout.WEST);
        panel.add(comp, BorderLayout.EAST);
        return panel;
    }

    /**
     * Returns a ViewDef Obj with the form UI built.
     * @param view the view definition
     * @param altView which AltViewIFace to build
     * @param parentView the MultiViw that this view/form will be parented to
     * @param options the options needed for creating the form
     * @return a Viewable Obj with the form UI built
     */
    public Viewable buildViewable(final ViewIFace    view, 
                                  final AltViewIFace altView, 
                                  final MultiView    parentView,
                                  final int          options,
                                  final Color        bgColor)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        ViewDefIFace viewDef = altView.getViewDef();

        if (viewDef == null) return null;

        this.rootMultiView =  parentView;

        if (viewDef.getType() == ViewDefIFace.ViewType.form)
        {
            Viewable viewable = buildFormViewable(view, altView, parentView, options, bgColor);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == ViewDefIFace.ViewType.table ||
                   viewDef.getType() == ViewDefIFace.ViewType.formtable)
        {
            Viewable viewable = buildTableViewable(view, altView, parentView, options, bgColor);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == ViewDefIFace.ViewType.field)
        {
            this.rootMultiView =  null;
            return null;

        }
        else if (viewDef.getType() == ViewDefIFace.ViewType.iconview)
        {
            return new IconViewObj(view, altView, parentView, options);
        }
        else if (viewDef.getType() == ViewDefIFace.ViewType.rstable)
        {
            return buildRecordSetTableViewable(view, altView, parentView, options, bgColor);
                
        } else
        {
            this.rootMultiView =  null;
            throw new RuntimeException("Form Type not covered by builder ["+viewDef.getType()+"]");
        }

    }

    /**
     * Creates a ValTextField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return a ValTextField
     */
    protected JTextField createTextField(final FormValidator validator,
                                         final FormCellField cellField,
                                         final PickListDBAdapterIFace adapter)
    {
        String validationRule = cellField.getValidationRule();

        JTextField txtField;
        if (validator != null && (cellField.isRequired() || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValTextField textField = new ValTextField(cellField.getTxtCols(), adapter);
            textField.setRequired(cellField.isRequired());

            validator.hookupTextField(textField,
                                      cellField.getIdent(),
                                      cellField.isRequired(),
                                      parseValidationType(cellField.getValidationType()),
                                      cellField.getValidationRule(),
                                      cellField.isChangeListenerOnly());

            txtField = textField;
            textField.setEditable(!cellField.isReadOnly());

        } else if (adapter != null)
        {
            ValTextField textField = new ValTextField(cellField.getTxtCols(), adapter);

            txtField = textField;
            textField.setEditable(false);

        } else
        {
            txtField = new JTextField(cellField.getTxtCols());
        }
        
        return txtField;
    }

    /**
     * Creates a ValPasswordField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return a ValPasswordField
     */
    public JTextField createPasswordField(final FormValidator validator,
                                          final FormCellField cellField)
    {
        String validationRule = cellField.getValidationRule();
        JTextField txt;

        if (validator != null && (cellField.isRequired() || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValPasswordField textField = new ValPasswordField(cellField.getTxtCols());
            textField.setRequired(cellField.isRequired());
            textField.setEncrypted(cellField.isEncrypted());

            validator.hookupTextField(textField,
                                      cellField.getIdent(),
                                      cellField.isRequired(),
                                      parseValidationType(cellField.getValidationType()),
                                      validationRule,
                                      cellField.isChangeListenerOnly());

           txt = textField;

        } else
        {
            txt = new ValPasswordField(cellField.getTxtCols());
        }

        return txt;
    }


    /**
     * Creates a ValFormattedTextField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @param allEditOK indicates that all the fields should be editable (event the auto-numbered field)
     * @return ValFormattedTextField
     */
    protected JComponent createFormattedTextField(final FormValidator validator,
                                                  final FormCellField cellField,
                                                  final boolean       isViewOnly,
                                                  final boolean       allEditOK)
    {
        log.debug(cellField.getName()+"  "+cellField.getUIFieldFormatter());

        // Because it is formatted we ALWAYS validate it when there is a validator
        if (validator != null)
        {
            // deliberately ignore "cellField.isChangeListenerOnly()"
            // pass in false instead

            UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getFormatter(cellField.getUIFieldFormatter());
            if (formatter == null)
            {
                throw new RuntimeException("Missing formatter by name ["+cellField.getUIFieldFormatter()+"]");
            }
            
            if (formatter.isDate())
            {
                ValFormattedTextFieldSingle textField = new ValFormattedTextFieldSingle(cellField.getUIFieldFormatter(), isViewOnly);
                textField.setRequired(cellField.isRequired());
                
                validator.hookupTextField(textField,
                                          cellField.getIdent(),
                                          cellField.isRequired(),
                                          UIValidator.Type.Changed,  cellField.getValidationRule(), false);
                
                if (isViewOnly)
                {
                    changeTextFieldUIForDisplay(textField, cellField.getPropertyAsBoolean("transparent", false));
                } else
                {
                    textField.setEditable(!cellField.isReadOnly());
                }
                return textField;
                
            }
            
            ValFormattedTextField textField = new ValFormattedTextField(formatter, isViewOnly, allEditOK);
            textField.setRequired(cellField.isRequired());
            
            DataChangeNotifier dcn = validator.hookupComponent(textField,
                                                               cellField.getIdent(),
                                                               UIValidator.Type.Changed,  cellField.getValidationRule(), false);

            textField.setChangeListener(dcn);
            return textField;
 
        }
        
        if (isViewOnly)
        {
            ValFormattedTextFieldSingle vtfs = new ValFormattedTextFieldSingle(cellField.getUIFieldFormatter(), isViewOnly);
            changeTextFieldUIForDisplay(vtfs, cellField.getPropertyAsBoolean("transparent", false));
            return vtfs;
        }
        // else
        ValFormattedTextField vtf = new ValFormattedTextField(cellField.getUIFieldFormatter(), isViewOnly, allEditOK);
        vtf.setEnabled(!cellField.isReadOnly());
        return vtf;
    }

    /**
     * Creates a ValTextArea.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValTextArea
     */
    protected JTextArea createTextArea(final FormValidator validator,
                                       final FormCellField cellField)
    {
        ValTextArea textArea = new ValTextArea("", cellField.getTxtRows(), cellField.getTxtCols());
        if (validator != null)
        {
            UIValidator.Type type = parseValidationType(cellField.getValidationType());
            DataChangeNotifier dcn = validator.hookupComponent(textArea, cellField.getIdent(), type, null, true);
            if (type == UIValidator.Type.Changed)
            {
                textArea.getDocument().addDocumentListener(dcn);

            } else if (type == UIValidator.Type.Focus)
            {
                textArea.addFocusListener(dcn);

            } else
            {
               // Do nothing for UIValidator.Type.OK
            }
        }
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);


        textArea.setEditable(!cellField.isReadOnly());

        return textArea;
    }


    /**
     * Creates a ValListBox.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValListBox
     */
    protected JList createList(final FormValidator validator,
                               final FormCellField cellField)
    {
        String[] initArray = null;
        String dataStr = cellField.getProperty("data");
        if (isNotEmpty(dataStr))
        {
            initArray = split(dataStr, ",");
            for (int i=0;i<initArray.length;i++)
            {
                initArray[i] = initArray[i].trim();
            }
        }

        ValListBox valList = initArray == null ? new ValListBox() : new ValListBox(initArray);
        if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(valList, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            valList.getModel().addListDataListener(dcn);
            valList.addFocusListener(dcn);
        }
        valList.setRequired(cellField.isRequired());
        
        valList.setVisibleRowCount(cellField.getPropertyAsInt("rows", 15));
        
        return valList;
    }


    /**
     * Creates a ValComboBoxFromQuery.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValComboBoxFromQuery
     */
    public ValComboBoxFromQuery createQueryComboBox(final FormValidator validator,
                                                    final FormCellField cellField)
    {
        //String cbxName = cellField.getInitialize();
        String cbxName = cellField.getProperty("name");
        if (isNotEmpty(cbxName))
        {
            int btnOpts = 0;
            btnOpts |= cellField.getPropertyAsBoolean("editbtn", true) ? ValComboBoxFromQuery.CREATE_EDIT_BTN : 0;
            btnOpts |= cellField.getPropertyAsBoolean("newbtn", true) ? ValComboBoxFromQuery.CREATE_NEW_BTN : 0;
            btnOpts |= cellField.getPropertyAsBoolean("searchbtn", true) ? ValComboBoxFromQuery.CREATE_SEARCH_BTN : 0;

            ValComboBoxFromQuery cbx = TypeSearchForQueryFactory.createValComboBoxFromQuery(cbxName, btnOpts);
            cbx.setRequired(cellField.isRequired());
            if (validator != null)// && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
            {
                DataChangeNotifier dcn = validator.hookupComponent(cbx, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
                cbx.getComboBox().getModel().addListDataListener(dcn);

                if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
                {
                    cbx.addFocusListener(dcn);
                }
            }
            cbx.setCellName(cellField.getName());
            return cbx;

        }
        // else
        throw new RuntimeException("CBX Name for ValComboBoxFromQuery ["+cbxName+"] is empty!");
    }

    /**
     * Creates a ValComboBox.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValComboBox
     */
    protected ValComboBox createValComboBox(final FormValidator validator,
                                            final FormCellField cellField,
                                            final PickListDBAdapterIFace adapter)
    {
        boolean                makeEditable = cellField.getPropertyAsBoolean("editable", false);
        ValComboBox            cbx          = null;
        if (adapter != null)
        {
            cbx = new ValComboBox(adapter); // false means don't auto-create picklist
                
        } else
        {
            String[] initArray = null;
            String data = cellField.getProperty("data");
            if (isNotEmpty(data))
            {
                initArray = split(data, ",");
                for (int i=0;i<initArray.length;i++)
                {
                    initArray[i] = initArray[i].trim();
                }
            }

            cbx = initArray == null || initArray.length == 0 ? new ValComboBox(makeEditable) : new ValComboBox(initArray, makeEditable);
        }
        cbx.setRequired(cellField.isRequired());
        
        if (validator != null && (cellField.isRequired() || cellField.isChangeListenerOnly() || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(cbx, 
                                                               cellField.getIdent(), 
                                                               parseValidationType(cellField.getValidationType()), 
                                                               cellField.getValidationRule(), 
                                                               cellField.isChangeListenerOnly());
            //cbx.getModel().addListDataListener(dcn);
            cbx.getComboBox().addActionListener(dcn);

            if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
            {
                cbx.addFocusListener(dcn);
            }
        }

        return cbx;
    }
    
    /**
     * Makes adjusts to the border and the colors to make it "flat" for diaply mode.
     * @param textField the text field to be flattened
     * @param isTransparent make the background transparent instead of using the viewFieldColor
     */
    public static void changeTextFieldUIForDisplay(final JTextField textField, final boolean isTransparent)
    {
        Insets insets = textField.getBorder().getBorderInsets(textField);
        textField.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);
        textField.setFocusable(false);
        
        if (isTransparent)
        {
            textField.setOpaque(false);
            
        } else if (viewFieldColor != null)
        {
            textField.setBackground(viewFieldColor.getColor());
        }
    }
    
    /**
     * Creates a JTextArea for display purposes only.
     * @param cellField FormCellField info
     * @return the control
     */
    protected JScrollPane createDisplayTextArea(final FormCellFieldIFace cellField)
    {
        JTextArea ta = new JTextArea(cellField.getTxtRows(), cellField.getTxtCols());
        // ta.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        Insets insets = ta.getBorder().getBorderInsets(ta);
        ta.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        ta.setForeground(Color.BLACK);
        ta.setEditable(false);
        ta.setBackground(viewFieldColor.getColor());
        
        JScrollPane scrollPane = new JScrollPane(ta);
        insets = scrollPane.getBorder().getBorderInsets(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        
        return scrollPane;
    }
    
    /**
     * Creates a TextFieldWithInfo.
     * @param cellField FormCellField info
     * @param parent the parent mulitview needed because of pop Dialogs (from the info btn)
     * @return the control
     */
    protected TextFieldWithInfo createTextFieldWithInfo(final FormCellField cellField,
                                                        final MultiView     parent)
    {
        TextFieldWithInfo textFieldInfo = null;
        String            txtName = cellField.getProperty("name");
        if (isNotEmpty(txtName))
        {
            textFieldInfo = TypeSearchForQueryFactory.getTextFieldWithInfo(txtName);
            if (textFieldInfo != null)
            {
                textFieldInfo.setMultiView(parent);
                textFieldInfo.setFrameTitle(cellField.getProperty("title"));
                JTextField textField = textFieldInfo.getTextField();
                textField.setColumns(cellField.getTxtCols());
               
                changeTextFieldUIForDisplay(textField, false);
                
            } else
            {
                log.error("Could TypeSearchForQueryFactory.getTextFieldWithInfo("+txtName+")");
            }
        } else
        {
            throw new RuntimeException("textfieldinfo Name for textFieldWithInfo ["+txtName+"] is empty!");
        }
        return textFieldInfo;
    }
    
    /**
     * Creates an ImageDisplay control.
     * @param cellField FormCellField info
     * @param mode indicates whether in Edit or View mode
     * @param validator the validator
     * @return the control
     */
    protected ImageDisplay createImageDisplay(final FormCellField cellField,
                                              final AltViewIFace.CreationMode mode,
                                              final FormValidator validator)
    {
        int w = 150;
        int h = 150;
        String sizeDefStr = cellField.getProperty("size");
        if (isNotEmpty(sizeDefStr))
        {
            String[] wh = StringUtils.split(sizeDefStr, ",");
            if (wh.length == 2)
            {
                try
                {
                    w = Integer.parseInt(wh[0]);
                    h = Integer.parseInt(wh[1]);

                } catch (Exception ex)
                {
                    log.error("size prop for Image is incorrect ["+sizeDefStr+"]");
                }
            }

        }
        
        boolean imageInEdit = mode == AltViewIFace.CreationMode.EDIT;
        String editModeStr = cellField.getProperty("edit");
        if (isNotEmpty(editModeStr))
        {
            imageInEdit = editModeStr.toLowerCase().equals("true");
        }

        // create a new ImageDisplay
        // override the selectNewImage method to notify the parent MultiView when a change occurs
        ImageDisplay imgDisp = new ImageDisplay(w, h, imageInEdit, cellField.getPropertyAsBoolean("border", true));
        
        if (validator != null)
        {
            DataChangeNotifier dcn = validator.hookupComponent(imgDisp, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            imgDisp.addPropertyChangeListener("imageURL", dcn);

            if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
            {
                imgDisp.addFocusListener(dcn);
            }
        }
        
        String urlStr = cellField.getProperty("url");
        if (isNotEmpty(urlStr))
        {
            imgDisp.setValue(urlStr, "");
        } else
        {
            String name = cellField.getProperty("icon");
            if (isNotEmpty(name))
            {
                boolean loadIt   = true;
                String  iconSize = cellField.getProperty("iconsize");
                if (isNotEmpty(iconSize))
                {
                    IconManager.IconSize sz = IconManager.getIconSize(Integer.parseInt(iconSize), false, false);
                    if (sz != null)
                    {
                        imgDisp.setImage(IconManager.getImage(name, sz));
                        loadIt = false;
                    }
                    
                } 
                
                if (loadIt)
                {
                    imgDisp.setImage(IconManager.getIcon(name));
                }
            }
        }
        return imgDisp;
    }
    
    /**
     * Creates an ImageDisplay control,
     * @param cellField FormCellField info
     * @param mode indicates whether in Edit or View mode
     * @return the control
     */
    protected JComponent createPlugin(final FormValidator validator, 
                                      final FormCellField cellField,
                                      final boolean       isViewMode)
    {
        String classNameStr = cellField.getProperty("class");
        if (StringUtils.isEmpty(classNameStr))
        {
            throw new RuntimeException("Creating plugin and the class property was missing.");
        }
        
        try
        {
            JComponent uiObj = Class.forName(classNameStr).asSubclass(JComponent.class).newInstance();
            
            if (!(uiObj instanceof GetSetValueIFace))
            {
                throw new RuntimeException("Plugin of class["+classNameStr+"] doesn't implement the GetSetValueIFace!");
            }
            
            // This needs to be done before the initialize.
            if (uiObj instanceof UIValidatable)
            {
                ((UIValidatable)uiObj).setRequired(cellField.isRequired());
            }
            
            if (uiObj instanceof UIPluginable)
            {
                UIPluginable uip = (UIPluginable)uiObj;
                uip.initialize(cellField.getProperties(), isViewMode);
                
                if (validator != null && (cellField.isChangeListenerOnly() || 
                                          cellField.isRequired() || 
                                          isNotEmpty(cellField.getValidationRule())))
                {
                    DataChangeNotifier dcn = validator.hookupComponent(uiObj, 
                                                                       cellField.getIdent(),
                                                                       parseValidationType(cellField.getValidationType()), 
                                                                       cellField.getValidationRule(), 
                                                                       cellField.isChangeListenerOnly());
                    uip.setChangeListener(dcn);
                }
            }

            return uiObj;
            
        } catch (Exception ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }
    }
    


    /**
     * Processes the rows in a definition.
     * @param parent MultiView parent
     * @param formViewDef the FormViewDef (Viewdef)
     * @param validator optional validator
     * @param viewBldObj the FormViewObj this row belongs to
     * @param mode the creation mode
     * @param builder the current JGoodies builder
     * @param labelsForHash the has table for label
     * @param cc CellConstraints
     * @param currDataObj the current data object
     * @param formRows the list of rows to be processed
     */
    protected void processRows(final MultiView                 parent,
                               final FormViewDefIFace          formViewDef,
                               final FormValidator             validator,
                               final ViewBuilderIFace          viewBldObj,
                               final AltViewIFace.CreationMode mode,
                               final Hashtable<String, JLabel> labelsForHash,
                               final Object                    currDataObj,
                               final List<FormRowIFace>        formRows)
    {
        int rowInx    = 1;
        int curMaxRow = 1;
        
        boolean isNewObj = MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT);
        
        Hashtable<CollapsableSeparator, String> collapseSepHash = null;

        for (FormRowIFace row : formRows)
        {
            int colInx = 1;

            for (FormCellIFace cell : row.getCells())
            {
                JComponent compToAdd = null;
                JComponent compToReg = null;

                int        colspan   = cell.getColspan();
                int        rowspan   = cell.getRowspan();

                boolean    addToValidator = true;
                boolean    addControl     = true;

                if (cell.getType() == FormCellIFace.CellType.label)
                {
                    FormCellLabel cellLabel = (FormCellLabel)cell;

                    String lblStr = cellLabel.getLabel();
                    if (cellLabel.isRecordObj())
                    {
                        JComponent riComp = viewBldObj.createRecordIndentifier(lblStr, cellLabel.getIcon());
                        compToAdd = riComp;
                        
                    } else
                    {
                        boolean useColon = StringUtils.isNotEmpty(cellLabel.getLabelFor());
                        int align;
                        String alignProp = cellLabel.getProperty("align");
                        System.out.println(lblStr);
                        if (StringUtils.isNotEmpty(alignProp))
                        {
                            if (alignProp.equals("left"))
                            {
                                align = SwingConstants.LEFT;
                                
                            } else if (alignProp.equals("center"))
                            {
                                align = SwingConstants.CENTER;
                                
                            } else
                            {
                                align = SwingConstants.RIGHT;
                            }
                        } else if (useColon)
                        {
                            align = SwingConstants.RIGHT;
                        } else
                        {
                            align = SwingConstants.LEFT;
                        }
                        
                        String lStr;
                        if (isNotEmpty(lblStr))
                        {
                            if (useColon)
                            {
                                lStr = lblStr + ":";
                            } else
                            {
                                lStr = lblStr;
                            }
                        } else
                        {
                            lStr = "  ";
                        }
                        JLabel lbl = new JLabel(lStr, align);
                        labelsForHash.put(cellLabel.getLabelFor(), lbl);
                        compToAdd      =  lbl;
                        viewBldObj.addLabel(cellLabel, lbl);
                    }

                    addToValidator = false;
                    addControl     = false;


                } else if (cell.getType() == FormCellIFace.CellType.field)
                {
                    FormCellField cellField = (FormCellField)cell;
                    
                    boolean isEditOnCreateOnly = cellField.getPropertyAsBoolean("editoncreate", false);
                    
                    FormCellField.FieldType uiType = cellField.getUiType();
                    
                    // Check to see if there is a picklist and get it if there is
                    String                 pickListName = cellField.getPickListName();
                    PickListDBAdapterIFace adapter      = null;
                    if (isNotEmpty(pickListName))
                    {
                        adapter = PickListDBAdapterFactory.getInstance().create(pickListName, false);
                        
                        if (adapter == null || adapter.getPickList() == null)
                        {
                            throw new RuntimeException("PickList Adapter ["+pickListName+"] cannot be null!");
                        }
                    }

                    // The Default Display for combox is dsptextfield, except when there is a TableBased PickList
                    // At the time we set the display we don't want to go get the picklist to find out. So we do it
                    // here after we have the picklist and actually set the change into the cellField
                    // because it uses the value to determine whether to convert the value into a text string 
                    // before setting it.
                    if (mode == AltViewIFace.CreationMode.VIEW || (isEditOnCreateOnly && !isNewObj))
                    {
                        if (uiType == FormCellFieldIFace.FieldType.combobox && cellField.getDspUIType() != FormCellFieldIFace.FieldType.textpl)
                        {
                            if (adapter != null && adapter.isTabledBased())
                            {
                                uiType = FormCellFieldIFace.FieldType.textpl;
                                cellField.setDspUIType(uiType);
                                
                            } else
                            {
                                uiType = cellField.getDspUIType();    
                            }
                        } else
                        {
                            uiType = cellField.getDspUIType();
                        }
                    }
                    
                    // Create the UI Component
                    
                    switch (uiType)
                    {
                        case text:
                            compToAdd = createTextField(validator, cellField, adapter);
                            addToValidator = validator == null; // might already added to validator
                            break;
                        
                        case formattedtext:
                            compToAdd = createFormattedTextField(validator, cellField, mode == AltViewIFace.CreationMode.VIEW, cellField.getPropertyAsBoolean("alledit", false));
                            addToValidator = validator == null; // might already added to validator
                            break;
                            
                        case label:
                            JLabel label = new JLabel("", SwingConstants.LEFT);
                            compToAdd = label;
                            break;
                            
                        case dsptextfield:
                            if (StringUtils.isEmpty(cellField.getPickListName()))
                            {
                                JTextField text = new JTextField(cellField.getTxtCols());
                                changeTextFieldUIForDisplay(text, cellField.getPropertyAsBoolean("transparent", false));
                                compToAdd = text;
                            } else
                            {
                                compToAdd = createTextField(validator, cellField, adapter);
                                addToValidator = validator == null; // might already added to validator
                            }
                            break;

                        case textfieldinfo:
                            compToAdd = createTextFieldWithInfo(cellField, parent);
                            break;

                            
                        case image:
                            compToAdd = createImageDisplay(cellField, mode, validator);
                            addToValidator = (validator != null);
                            break;

                        
                        case url:
                            compToAdd = new BrowserLauncherBtn(cellField.getProperty("title"));
                            addToValidator = false;
    
                            break;
                        
                        case combobox:
                            compToAdd = createValComboBox(validator, cellField, adapter);
                            addToValidator = validator != null; // might already added to validator
                            break;
                            
                        case checkbox:
                        {
                            ValCheckBox checkbox = new ValCheckBox(cellField.getLabel(), 
                                                                   cellField.isRequired(), 
                                                                   cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getIdent(), checkbox, null);
                                checkbox.addActionListener(dcn);
                            }
                            compToAdd = checkbox;
                            break;
                        }
                        
                        case spinner:
                        {
                            String minStr = cellField.getProperty("min");
                            int    min    = StringUtils.isNotEmpty(minStr) ? Integer.parseInt(minStr) : 0;
                            
                            String maxStr = cellField.getProperty("max");
                            int    max    = StringUtils.isNotEmpty(maxStr) ? Integer.parseInt(maxStr) : 0; 
                            
                            ValSpinner spinner = new ValSpinner(min, max, cellField.isRequired(), 
                                                                   cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getIdent(), spinner, null);
                                spinner.addChangeListener(dcn);
                            }
                            compToAdd = spinner;
                            break;
                        }                            
                         
                        case password:
                            compToAdd      = createPasswordField(validator, cellField);
                            addToValidator = validator == null; // might already added to validator
                            break;
                        
                        case dsptextarea:
                            compToAdd = createDisplayTextArea(cellField);
                            break;
                        
                        case textarea:
                        {
                            JTextArea ta = createTextArea(validator, cellField);
                            JScrollPane scrollPane = new JScrollPane(ta);
                            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                            
                            addToValidator = validator == null; // might already added to validator
                            compToReg = ta;
                            compToAdd = scrollPane;
                            break;
                        }
                        
                        case browse:
                            BrowseBtnPanel bbp = new BrowseBtnPanel(createTextField(validator, cellField, null), 
                                                                    cellField.getPropertyAsBoolean("dirsonly", false), 
                                                                    cellField.getPropertyAsBoolean("forinput", true));
                            compToAdd = bbp;
                            break;
                            
                        case querycbx:
                        {
                            ValComboBoxFromQuery cbx = createQueryComboBox(validator, cellField);
                            cbx.setMultiView(parent);
                            cbx.setFrameTitle(cellField.getProperty("title"));
                            
                            compToAdd = cbx;
                            addToValidator = validator == null; // might already added to validator
                            break;
                        }
                        
                        case list:
                        {
                            JList list = createList(validator, cellField);
                            
                            JScrollPane scrollPane = new JScrollPane(list);
                            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
                            addToValidator = validator == null;
                            compToReg = list;
                            compToAdd = scrollPane;
                            break;
                        }
                        
                        case colorchooser:
                        {
                            ColorChooser colorChooser = new ColorChooser(Color.BLACK);
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), colorChooser, null);
                                colorChooser.addPropertyChangeListener("setValue", dcn);
                            }
                            compToAdd = colorChooser;
    
                            break;
                        }
                        
                        case button:
                            JButton btn = new JButton(cellField.getProperty("title"));
                            
                            compToAdd = btn;
                            break;
                            
                        case progress:
                            compToAdd = new JProgressBar(0, 100);
                            break;
                        
                        case plugin:
                            compToAdd = createPlugin(validator, cellField, mode == AltViewIFace.CreationMode.VIEW);
                            break;

                        case textpl:
                            JTextField txt = new TextFieldFromPickListTable(adapter);
                            changeTextFieldUIForDisplay(txt, cellField.getPropertyAsBoolean("transparent", false));
                            compToAdd = txt;
                            break;
                        
                        default:
                            throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
                        
                    } // switch

                } else if (cell.getType() == FormCellIFace.CellType.separator)
                {
                    
                    // still have compToAdd = null;
                    FormCellSeparatorIFace fcs             = (FormCellSeparatorIFace)cell;
                    String            collapsableName = fcs.getCollapseCompName();
                    Component         sep             = viewBldObj.createSeparator(fcs.getLabel());
                    if (isNotEmpty(collapsableName))
                    {
                        CollapsableSeparator collapseSep = new CollapsableSeparator(sep);
                        if (collapseSepHash == null)
                        {
                            collapseSepHash = new Hashtable<CollapsableSeparator, String>();
                        }
                        collapseSepHash.put(collapseSep, collapsableName);
                        sep = collapseSep;
                        
                    }
                    addControl     = cell.getName().length() > 0;
                    compToAdd      = (JComponent)sep;
                    addControl     = false;
                    addToValidator = false;
                    
                    //curMaxRow = rowInx;
                    //colInx += 2;

                } else if (cell.getType() == FormCellIFace.CellType.command)
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
                    addToValidator = false;
                    compToAdd = btn;

                } 
                else if (cell.getType() == FormCellIFace.CellType.iconview)
                {
                    FormCellSubView cellSubView = (FormCellSubView) cell;

                    String subViewName = cellSubView.getViewName();

                    ViewIFace subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        if (parent != null)
                        {
                            int options = MultiView.VIEW_SWITCHER
                                    | (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT
                                            : MultiView.NO_OPTIONS);

                            MultiView multiView = new MultiView(parent, 
                                                                cellSubView.getName(), 
                                                                subView, 
                                                                parent.getCreateWithMode(), 
                                                                options, null);
                            parent.addChild(multiView);

                            viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);
                            viewBldObj.closeSubView(cellSubView);
                            curMaxRow = rowInx;

                        }
                        else
                        {
                            log.error("buildFormView - parent is NULL for subview [" + subViewName + "]");
                        }

                    }
                    else
                    {
                        log.error("buildFormView - Could find subview's with ViewSet[" + cellSubView.getViewSetName()
                                + "] ViewName[" + subViewName + "]");
                    }
                    // still have compToAdd = null;
                    colInx += 2;
                    
                } else if (cell.getType() == FormCellIFace.CellType.subview)
                {
                    FormCellSubView cellSubView = (FormCellSubView)cell;

                    String subViewName = cellSubView.getViewName();

                    ViewIFace subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        // Check to see this view should be "flatten" meaning we are creating a grid from a form
                        if (!viewBldObj.shouldFlatten())
                        {
                            if (parent != null)
                            {
                                ViewIFace parentView = parent.getView();
                                
                                boolean isSingle      = cellSubView.isSingleValueFromSet();
                                boolean isACollection = false;
                                try
                                {
                                    Class<?> cls = Class.forName(parentView.getClassName());
                                    Field fld = cls.getDeclaredField(cellSubView.getName());
                                    if (fld != null)
                                    {
                                        isACollection = Collection.class.isAssignableFrom(fld.getType());
                                    } else
                                    {
                                        log.error("Couldn't find field ["+cellSubView.getName()+"] in class ["+parentView.getClassName()+"]");
                                    }
                                    
                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }

                                int options = (isACollection && !isSingle ? MultiView.RESULTSET_CONTROLLER : MultiView.IS_SINGLE_OBJ) | MultiView.VIEW_SWITCHER |
                                              (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT : MultiView.NO_OPTIONS);
                                
                                
                                Color bgColor = getBackgroundColor(cellSubView.getProperties(), parent.getBackground());
                                
                                //MultiView.printCreateOptions("SUBVIEW", options);
                                MultiView multiView = new MultiView(parent, 
                                                                    cellSubView.getName(),
                                                                    subView,
                                                                    parent.getCreateWithMode(), 
                                                                    cellSubView.getDefaultAltViewType(),
                                                                    options, 
                                                                    bgColor);
                                setBorder(multiView, cellSubView.getProperties());
                                
                                parent.addChild(multiView);
                                
                                viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);                               
                                viewBldObj.closeSubView(cellSubView);
                                
                                Viewable viewable = multiView.getCurrentView();
                                if (viewable instanceof TableViewObj)
                                {
                                    ((TableViewObj)viewable).setVisibleRowCount(cellSubView.getTableRows());
                                }
                                curMaxRow = rowInx;
                                
                                //if (hasColor)
                                //{
                                //    setMVBackground(multiView, multiView.getBackground());
                                //}
                                
                            } else
                            {
                                log.error("buildFormView - parent is NULL for subview ["+subViewName+"]");
                            }
                        } else
                        {
                            viewBldObj.addSubView(cellSubView, parent, colInx, rowInx, cellSubView.getColspan(), 1); 
                            
                            AltViewIFace     altView        = subView.getDefaultAltView();
                            FormViewDefIFace subFormViewDef = (FormViewDefIFace)altView.getViewDef();
                            processRows(parent, formViewDef, validator, viewBldObj, altView.getMode(), labelsForHash, currDataObj, subFormViewDef.getRows());
                            viewBldObj.closeSubView(cellSubView);
                        }

                    } else
                    {
                        log.error("buildFormView - Could find subview's with ViewSet["+cellSubView.getViewSetName()+"] ViewName["+subViewName+"]");
                    }
                    // still have compToAdd = null;
                    colInx += 2;

                } else if (cell.getType() == FormCellIFace.CellType.statusbar)
                {
                    compToAdd      = new JStatusBar();
                    addControl     = true;
                    addToValidator = false;
                    
                } else if (cell.getType() == FormCellIFace.CellType.panel)
                {
                    FormCellPanel           cellPanel = (FormCellPanel)cell;
                    PanelViewable.PanelType panelType = PanelViewable.getType(cellPanel.getPanelType());
                    
                    if (panelType == PanelViewable.PanelType.Panel)
                    {
                        PanelViewable panelViewable = new PanelViewable(viewBldObj, cellPanel);

                        processRows(parent, formViewDef, validator, panelViewable, mode, labelsForHash, currDataObj, cellPanel.getRows());

                        compToAdd = panelViewable;

                    } else if (panelType == PanelViewable.PanelType.ButtonBar)
                    {
                        compToAdd = PanelViewable.buildButtonBar(processRows(viewBldObj, cellPanel.getRows()));
                        
                    } else
                    {
                        throw new RuntimeException("Panel Type is not implemented.");
                    }

                    addControl     = false;
                    addToValidator = false;

                }

                if (compToAdd != null)
                {
                    //System.out.println(colInx+"  "+rowInx+"  "+colspan+"  "+rowspan+"  "+compToAdd.getClass().toString());
                    viewBldObj.addControlToUI(compToAdd, colInx, rowInx, colspan, rowspan);

                    if (addControl)
                    {
                        viewBldObj.registerControl(cell, compToReg == null ? compToAdd : compToReg);
                    }
                    
                    curMaxRow = Math.max(curMaxRow, rowspan+rowInx);


                    if (validator != null && addToValidator)
                    {

                        String id = cell.getIdent();
                        if (StringUtils.isEmpty(id))
                        {
                            System.out.println(cell.getName());
                        }
                        validator.addUIComp(cell.getIdent(), compToReg == null ? compToAdd : compToReg);
                    }
                    colInx += colspan + 1;
                 }

            }
            rowInx += 2;
        }

        if (collapseSepHash != null)
        {
            for (Enumeration<CollapsableSeparator> e=collapseSepHash.keys();e.hasMoreElements();)
            {
                CollapsableSeparator collapseSep = e.nextElement();
                Component            comp        = viewBldObj.getControlByName(collapseSepHash.get(collapseSep));
                if (comp != null)
                {
                    collapseSep.setInnerComp(comp);
                }
            }
        }
    }
    
    /**
     * Sets a border on the component as defined in the properties.
     * @param comp the component
     * @param props the list of properties
     */
    protected void setBorder(final JComponent comp, final Properties props)
    {
        if (props != null)
        {
            String borderType = props.getProperty("border");
            if (StringUtils.isNotEmpty(borderType))
            {
                if (borderType.equals("etched"))
                {
                    comp.setBorder(BorderFactory.createEtchedBorder());
                    
                } else if (borderType.equals("lowered"))
                {
                    comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    
                } else if (borderType.equals("raised"))
                {
                    comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    
                } else if (borderType.equals("empty"))
                {
                    comp.setBorder(BorderFactory.createEmptyBorder());
                    
                } else if (borderType.equals("line"))
                {
                    comp.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                }
            }
        }
    }

    protected Color getBackgroundColor(final Properties props, final Color bgColor)
    {
        if (props != null)
        {
            String colorStr = props.getProperty("bgcolor");
            if (StringUtils.isNotEmpty(colorStr))
            {
                if (colorStr.endsWith("%"))
                {
                    try
                    {
                        int percent = Integer.parseInt(colorStr.substring(0, colorStr.length()-1));
                        double per = (percent / 100.0);
                        int r = Math.min((int)(bgColor.getRed() * per), 255);
                        int g = Math.min((int)(bgColor.getGreen() * per), 255);
                        int b = Math.min((int)(bgColor.getBlue() * per), 255);
                        return new Color(r,g,b);

                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                } else
                {
                    try
                    {
                        return UIHelper.parseRGB(colorStr);
                        
                    } catch(ConfigurationException ex)
                    {
                        log.error(ex);
                    }
                }
            }
        }
        return bgColor;
    }
    
    public void setMVBackground(final MultiView parent, final Color bgColor)
    {
        for (MultiView mv : parent.getKids())
        {
            setMVBackground(mv, bgColor);
        }
        for (Viewable viewable : parent.getViewables())
        {
            viewable.getUIComponent().setBackground(bgColor);
        }
    }

    /**
     * Processes the rows for a button bar.
     * @param viewBldObj formViewObj
     * @param formRows formRows
     * @return the array of buttons
     */
    protected JButton[] processRows(final ViewBuilderIFace   viewBldObj,
                                    final List<FormRowIFace> formRows)
    {
        List<JButton> btns = new ArrayList<JButton>();

        for (FormRowIFace row : formRows)
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell.getType() == FormCellIFace.CellType.command)
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
                    viewBldObj.registerControl(cell, btn);
                    btns.add(btn);
                }
            }
        }

        JButton[] btnsArray = new JButton[btns.size()];
        int i = 0;
        for (JButton b : btns)
        {
            btnsArray[i++] = b;
        }
        btns.clear();
        return btnsArray;

    }



    /**
     * Creates a Form.
     * @param view view the view definition
     * @param altView the altView to use (if null, then it uses the default ViewDef)
     * @param parentView the MultiView parent (this may be null)
     * @param options the options needed for creating the form
     * @return the form
     */
    public FormViewObj buildFormViewable(final ViewIFace        view,
                                         final AltViewIFace     altView,
                                         final MultiView   parentView,
                                         final int         options,
                                         final Color       bgColor)
    {
        try
        {
            FormViewDef formViewDef = (FormViewDef)altView.getViewDef();

            Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
            
            ValidatedJPanel validatedPanel = null;
            FormValidator   validator      = null;
            if (altView.isValidated())
            {
                validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
                validator.setDataChangeNotification(true);
            }

            FormViewObj formViewObj = new FormViewObj(view, altView, parentView, validator, options, bgColor);

            Object currDataObj = formViewObj.getCurrentDataObj();

            processRows(parentView, formViewDef, validator, formViewObj, altView.getMode(), labelsForHash, currDataObj, formViewDef.getRows());


            if (validatedPanel != null)
            {
                validatedPanel.addPanel(formViewObj.getPanel());

                // Here we add all the components whether they are used or not
                // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
                Map<String, Component> mapping = formViewObj.getControlMapping();
                for (String id : mapping.keySet())
                {
                    validatedPanel.addValidationComp(id, mapping.get(id));
                }
                Map<String, String> enableRules = formViewDef.getEnableRules();

                // Load up validation Rules
                FormValidator fv = validatedPanel.getFormValidator();
                formViewObj.setValidator(fv);
                
                fv.setName(formViewDef.getName()); // For Debugging

                if (enableRules != null)
                {
                    for (String id : enableRules.keySet())
                    {
                        fv.addEnableRule(id, enableRules.get(id));
                    }
                }

                // Load up labels and associate them with their component
                for (String idFor : labelsForHash.keySet())
                {
                    fv.addUILabel(idFor, labelsForHash.get(idFor));
                }

                formViewObj.setFormComp(validatedPanel);
                
            } else
            {
                formViewObj.setFormComp(formViewObj.getPanel());
            }

            return formViewObj;

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Creates a Table View.
     * @param view view the view definition
     * @param altView the altView to use (if null, then it uses the default ViewDef)
     * @param parentView the MultiView parent (this may be null)
     * @param options the options needed for creating the form
     * @return the form
     */
    public TableViewObj buildTableViewable(final ViewIFace        view,
                                           final AltViewIFace     altView,
                                           final MultiView   parentView,
                                           final int         options,
                                           final Color       bgColor)
    {
        try
        {
            ViewDefIFace viewDef = altView.getViewDef();
            
            FormValidator validator = null;
            if (altView.isValidated())
            {
                ValidatedJPanel validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
                validator.setDataChangeNotification(true);
            }
            // Special situation where we create a table from a Form Definition
            if (viewDef instanceof FormViewDef)
            {
                FormViewDefIFace               formViewDef   = (FormViewDefIFace)viewDef;  
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                TableViewObj              tableViewObj  = new TableViewObj(view, altView, parentView, validator, options, bgColor);

                processRows(parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, validator, formViewDef.getRows());
                return tableViewObj;
                
            }
            // else
            FormViewDefIFace formViewDef = (FormViewDefIFace)altView.getViewDef();
            
            Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
            
            /*
            ValidatedJPanel validatedPanel = null;
            FormValidator   validator      = null;
            if (altView.isValidated())
            {
                validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
                validator.setDataChangeNotification(true);
            }
            */
            
            TableViewObj tableViewObj = new TableViewObj(view, altView, parentView, null, options, bgColor);

            //Object currDataObj = tableViewObj.getCurrentDataObj();

            processRows(parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, validator, formViewDef.getRows());

        /*
        if (validatedPanel != null)
        {
            validatedPanel.addPanel(formViewObj.getPanel());

            // Here we add all the components whether they are used or not
            // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
            Map<String, Component> mapping = formViewObj.getControlMapping();
            for (String id : mapping.keySet())
            {
                validatedPanel.addValidationComp(id, mapping.get(id));
            }
            Map<String, String> enableRules = formViewDef.getEnableRules();

            // Load up validation Rules
            FormValidator fv = validatedPanel.getFormValidator();
            formViewObj.setValidator(fv);
            
            fv.setName(formViewDef.getName()); // For Debugging

            for (String id : enableRules.keySet())
            {
                fv.addEnableRule(id, enableRules.get(id));
            }

            // Load up labels and associate them with there component
            for (String idFor : labelsForHash.keySet())
            {
                fv.addUILabel(idFor, labelsForHash.get(idFor));
            }

            formViewObj.setFormComp(validatedPanel);
            
        } else
        {
            formViewObj.setFormComp(formViewObj.getPanel());
        }
*/
          return tableViewObj;

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a Table View.
     * @param view view the view definition
     * @param altView the altView to use (if null, then it uses the default ViewDef)
     * @param parentView the MultiView parent (this may be null)
     * @param options the options needed for creating the form
     * @return the form
     */
    public TableViewObj buildRecordSetTableViewable(final ViewIFace        view,
                                                    final AltViewIFace     altView,
                                                    final MultiView   parentView,
                                                    @SuppressWarnings("unused")
                                                    final int         options,
                                                    final Color       bgColor)
    {
        RecordSetTableViewObj rsTableViewObj = null;
        try
        {
            ViewDefIFace viewDef = altView.getViewDef();
            
            // Special situation where we create a table from a Form Definition
            if (viewDef instanceof FormViewDef)
            {
                FormViewDefIFace               formViewDef   = (FormViewDefIFace)viewDef;  
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                
                rsTableViewObj  = new RecordSetTableViewObj(view, altView, parentView, null, 0, bgColor);

                processRows(parentView, formViewDef, null, rsTableViewObj, altView.getMode(), labelsForHash, null, formViewDef.getRows());
                return rsTableViewObj;
            }

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return rsTableViewObj;
    }


    /**
     * Creates a FormViewObj.
     * 
     * @param multiView the parent multiView
     * @param view the definition of the form view to be created
     * @param altName the name of the altView to be used (can be null - then it defaults to the default AltViewIFace)
     * @param data the data to be set into the form
     * @param options the options needed for creating the form
     * @return a new FormViewObj
     */
    public static Viewable createFormView(final MultiView multiView, 
                                          final ViewIFace      view, 
                                          final String    altName, 
                                          final Object    data,
                                          final int       options)
    {
        return createFormView(multiView, view, altName, data, options, null);
    }


    /**
     * Creates a FormViewObj.
     * 
     * @param multiView the parent multiView
     * @param view the definition of the form view to be created
     * @param altName the name of the altView to be used (can be null - then it defaults to the default AltViewIFace)
     * @param data the data to be set into the form
     * @param options the options needed for creating the form
     * @param bgColor the background color
     * @return a new FormViewObj
     */
    public static Viewable createFormView(final MultiView multiView, 
                                          final ViewIFace      view, 
                                          final String    altName, 
                                          final Object    data,
                                          final int       options,
                                          final Color     bgColor)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        AltViewIFace altView = view.getAltView(altName);

        if (altView != null)
        {
            Viewable viewable = instance.buildViewable(view, altView, multiView, options, bgColor);
            if (viewable != null)
            {
                if (data != null)
                {
                    viewable.setDataObj(data);
                    //viewable.setDataIntoUI();
                } else
                {
                    // This is bad to have when you don't have any items yet. - rods
                    //throw new RuntimeException("Form could be created because the data was null! ["+view.getName()+"]["+altView.getName()+"]");
                }
                return viewable;
            }
        }
        return null;
    }

    //-----------------------------------------------------------
    // Inner Class
    //-----------------------------------------------------------
    
    class BrowserLauncherBtn extends JButton implements GetSetValueIFace
    {
        protected String url     = null;
        protected Object dataObj = null;
        protected BrowserLauncherAction action = null;
        
        public BrowserLauncherBtn(final String text)
        {
            super(text);
            setEnabled(false);
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
         */
        public void setValue(Object value, String defaultValue)
        {
            if (value != null)
            {
                url = value.toString();
                dataObj = value;
                
                if (action != null)
                {
                    this.removeActionListener(action);
                }
                
                if (StringUtils.isNotEmpty(url) && url.startsWith("http"))
                {
                    action = new BrowserLauncherAction(url);
                    addActionListener(action);
                    setEnabled(true);
                } else
                {
                    setEnabled(false);
                }
            } else
            {
                setEnabled(false);
            }
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
         */
        public Object getValue()
        {
            return dataObj;
        }
    }
    
    class BrowserLauncherAction implements ActionListener
    {
        protected String url;
        
        public BrowserLauncherAction(final String url)
        {
            this.url = url;
        }
        
        public void actionPerformed(ActionEvent ae)
        {
            BrowserLauncher.openURL(url);
        }
    }
}
