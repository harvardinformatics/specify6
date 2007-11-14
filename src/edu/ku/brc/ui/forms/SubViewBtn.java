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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Set;

import javax.swing.JButton;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.ui.forms.persist.ViewIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 6, 2007
 *
 */
public class SubViewBtn extends JButton implements GetSetValueIFace
{
    public enum DATA_TYPE {IS_SET, IS_SINGLE, IS_THIS, IS_SINGLESET_ITEM}
    
    protected static final Logger log = Logger.getLogger(SubViewBtn.class);
    
    protected FormCellSubViewIFace  subviewDef;
    protected ViewIFace             view;
    protected DATA_TYPE             dataType;
    protected ViewBasedDisplayIFace frame      = null;
    protected MultiView             multiView  = null;
    protected MultiView             mvParent   = null;
    protected String                frameTitle = "";
    protected String                cellName;
    protected int                   options;
    protected String                baseLabel;
    
    protected Object                dataObj;
    protected Object                newDataObj;
    protected Class<?>              classObj;
    protected Object                parentObj;
    
    
    /**
     * @param subviewDef
     * @param isCollection
     * @param props
     */
    public SubViewBtn(final MultiView            mvParent,
                      final FormCellSubViewIFace subviewDef,
                      final ViewIFace            view,
                      final DATA_TYPE            dataType,
                      final int                  options,
                      final Properties           props)
    {
        this.mvParent     = mvParent;
        this.subviewDef   = subviewDef;
        this.dataType     = dataType;
        this.view         = view;
        this.options      = options;
        
        //log.debug("Editing "+MultiView.isOptionOn(options, MultiView.IS_EDITTING));
        //log.debug("IsNew "+MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT));

        cellName   = subviewDef.getName();
        frameTitle = props.getProperty("title");
        
        baseLabel = props.getProperty("label");
        if (baseLabel == null)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
            if (tableInfo != null)
            {
                baseLabel = tableInfo.getTitle();
            }
        }
        
        setText(baseLabel);
        
        try
        {
            classObj = Class.forName(view.getClassName());

        } catch (ClassNotFoundException ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }
        
        addActionListener(new ActionListener() {

            /* (non-Javadoc)
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showForm();
            }
        });
    }
    
    /**
     * @param pObj
     */
    public void setParentDataObj(final Object pObj)
    {
        this.parentObj = pObj;
    }
    
    /**
     * 
     */
    protected void showForm()
    {
        //boolean isParentNew = parentObj instanceof FormDataObjIFace ? ((FormDataObjIFace)parentObj).getId() == null : false;
        boolean isNewObject = MultiView.isOptionOn(options, MultiView.IS_NEW_OBJECT);
        boolean isEditing   = MultiView.isOptionOn(options, MultiView.IS_EDITTING) || isNewObject;
        
        String closeBtnTitle = isEditing ? getResourceString("Accept") : getResourceString("Close");
        
        frame = new ViewBasedDisplayDialog((Frame)null,
                subviewDef.getViewSetName(),
                subviewDef.getViewName(),
                "XXXX",                      // What is this argument???
                frameTitle,
                closeBtnTitle,
                view.getClassName(),
                cellName,  // idFieldName
                isEditing | isNewObject,
                false,
                cellName,
                mvParent,
                options | MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE);
        
        /*if (isNewObject)
        {
            FormDataObjIFace formDataObj = FormHelper.createAndNewDataObj(view.getClassName());
            formDataObj.initialize();
            newDataObj = formDataObj;
        }*/
        
        multiView = frame.getMultiView();
        multiView.setParentDataObj(mvParent.getData());
        multiView.setData(dataObj);
        
        multiView.addCurrentValidator();
        
        frame.showDisplay(true);
        
        mvParent.removeCurrentValidator();
        
        if (multiView != null && frame.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            if (frame.isEditMode())
            {
                FormViewObj fvo = frame.getMultiView().getCurrentViewAsFormViewObj();
                if (fvo != null)
                {
                    switch (dataType)
                    {
                        case IS_SET :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_SINGLE :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_THIS :
                            fvo.getDataFromUI();
                            break;
                            
                        case IS_SINGLESET_ITEM :
                            fvo.getDataFromUI();
                            break;
                    }
                    
                    updateBtnText();
                } 
            }
        } else
        {
            
        }
        frame.dispose();
        frame = null;
    }
    
    /**
     * 
     */
    protected void updateBtnText()
    {
        if (dataObj instanceof Set<?>)
        {
            int    size   = ((Set<?>)dataObj).size();
            String format = UIRegistry.getResourceString("SUBVIEW_BTN_TITLE_FORMAT");
            if (StringUtils.isEmpty(format))
            {
                format = "%s (%s) ...";
            }
            setText(String.format(format, baseLabel, size));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    @Override
    public Object getValue()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        
        // Retieve lazy object while in the context of a session (just like a subform would do
        if (dataObj != null)
        {
            if (dataObj instanceof FormDataObjIFace)
            {
                ((FormDataObjIFace)dataObj).getIdentityTitle();
                
            } else if (dataObj instanceof Set<?>)
            {
                updateBtnText(); // note: that by calling this, 'size' gets called and that loads the Set (this must be done).
                
                /*
                for (Object data : (Set<?>)dataObj)
                {
                    if (data instanceof FormDataObjIFace)
                    {
                        ((FormDataObjIFace)data).getIdentityTitle();
                    }
                }*/
            }
        }
    }

}
