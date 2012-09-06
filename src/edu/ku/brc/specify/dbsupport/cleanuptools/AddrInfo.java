/* Copyright (C) 2012, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.dbsupport.cleanuptools;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 30, 2012
 *
 */
public class AddrInfo
{
    protected Integer id;
    protected boolean isPrimary;
    protected boolean isCurrent;
    protected boolean isIncluded;
    
    /**
     * @param id
     * @param isPrimary
     * @param isCurrent
     * @param isIncluded
     */
    public AddrInfo(Integer id, boolean isPrimary, boolean isCurrent, boolean isIncluded)
    {
        super();
        this.id = id;
        this.isPrimary = isPrimary;
        this.isCurrent = isCurrent;
        this.isIncluded = isIncluded;
    }
    
}
