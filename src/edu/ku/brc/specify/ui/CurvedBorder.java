/* Filename:    $RCSfile: CurvedBorder.java,v $
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
package edu.ku.brc.specify.ui;

import java.awt.*;
import javax.swing.border.*;

/**
 * This Border class draws a curved border
 */
@SuppressWarnings("serial")
public class CurvedBorder extends AbstractBorder
{
    private Color borderColor = Color.gray;
    private int   borderWidth = 6;

    /**
     * Default Constructor
     *
     */
    public CurvedBorder()
    {
    }

    /**
     * 
     * @param borderWidth the border width
     */
    public CurvedBorder(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * 
     * @param borderColor the border color
     */
    public CurvedBorder(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    /**
     * Constructor
     * @param borderWidth the border width
     * @param borderColor the border color
     */
    public CurvedBorder(int borderWidth, Color borderColor)
    {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    {
        g.setColor(getBorderColor());
        g.drawRoundRect(x, y, w - 1, h - 1, borderWidth, borderWidth);

    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
     */
    public Insets getBorderInsets(Component c)
    {
        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
     */
    public Insets getBorderInsets(Component c, Insets i)
    {
        i.left = i.right = i.bottom = i.top = borderWidth;
        return i;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#isBorderOpaque()
     */
    public boolean isBorderOpaque()
    {
        return true;
    }

    /**
     * 
     * @return the border color
     */
     public Color getBorderColor()
    {
        return borderColor;
    }

    /**
     * 
     * @param borderColor the new color
     */
    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    /**
     * 
     * @return the border width
     */
    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * 
     * @param borderWidth the new width
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

}