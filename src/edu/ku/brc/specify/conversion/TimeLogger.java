/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.conversion;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 21, 2009
 *
 */
public class TimeLogger
{

    private long startTime;
    private long endTime;
    /**
     * 
     */
    public TimeLogger()
    {
        super();
        start();
    }
    
    public void start()
    {
        startTime = System.currentTimeMillis();
        endTime   = 0;
    }
    
    public String end()
    {
        endTime = System.currentTimeMillis();
        
        double totalSeconds = (endTime - startTime) / 1000.0;
        
        int hours = (int)(totalSeconds / 3600.0);
        int mins  = (int)((totalSeconds - (hours * 3600)) / 60);
        int secs  = (int)(totalSeconds - (hours * 3600) - (mins * 60));
               
        String str = String.format("Elapsed Time: %02d:%02d:%02d (%8.4f)", hours, mins, secs, totalSeconds);
        System.out.println(str);
        return str;
    }
}
