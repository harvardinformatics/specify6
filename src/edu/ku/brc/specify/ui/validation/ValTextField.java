/* Filename:    $RCSfile: ValidationListener.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
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

package edu.ku.brc.specify.ui.validation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JTextField;
import javax.swing.text.Document;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;

/**
 * A JTextControl that implements UIValidatable for participating in validation
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValTextField extends JTextField implements UIValidatable
{
    protected boolean isInError  = false;
    protected boolean isRequired = false;
    protected Color   bgColor    = null;
    
    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    

    public ValTextField()
    {
        super();
        init();
    }

    public ValTextField(String arg0)
    {
        super(arg0);       
        init();
    }

    public ValTextField(int arg0)
    {
        super(arg0);     
        init();
    }

    public ValTextField(String arg0, int arg1)
    {
        super(arg0, arg1);   
        init();
    }

    public void init()
    {
        bgColor = getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
    }
    
    public boolean isNotEmpty()
    {
        return getText().length() > 0;
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (isInError() && isEnabled())
        {
            Dimension dim = getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(1, 1, dim.width-2, dim.height-2);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return isInError;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setInError(boolean)
     */
    public void setInError(boolean isInError)
    {
        this.isInError = isInError;
        repaint();

    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        this.isRequired = isRequired;
    }

}
