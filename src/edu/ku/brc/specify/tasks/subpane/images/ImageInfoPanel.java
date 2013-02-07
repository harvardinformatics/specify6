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
package edu.ku.brc.specify.tasks.subpane.images;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.tasks.subpane.lm.BlueMarbleFetcher;
import edu.ku.brc.specify.tasks.subpane.lm.BufferedImageFetcherIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 31, 2012
 *
 */
public class ImageInfoPanel extends ExpandShrinkPanel
{
    public static int IMG_SIZE = 300;

    // Image Members
    protected ImageDisplay               imgDisplay;
    protected ImageDataItem              imgDataItem = null;
    
    // Map Members
    protected BufferedImage              blueMarble      = null;
    protected BufferedImageFetcherIFace  blueMarbleListener;
    //protected BufferedImage              renderImage     = null;
    protected ImageIcon                  markerImg;
    
    protected ImageDisplay               blueMarbleDisplay;
    protected BlueMarbleFetcher          blueMarbleFetcher;
    protected CollectionDataFetcher      dataFetcher;
    
    protected JTable                     table;
    protected ImgInfoModel               model;
    protected ImagesPane                 imagesPane;
    
    /**
     * @param dataFetcher
     * @param imagesPane
     */
    public ImageInfoPanel(final CollectionDataFetcher dataFetcher, 
                          final ImagesPane imagesPane)
    {
        super(CONTRACTED, false);
        
        this.dataFetcher = dataFetcher;
        this.imagesPane  = imagesPane;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.images.ExpandShrinkPanel#createUI()
     */
    @Override
    public void createUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setBackground(Color.WHITE);
        setOpaque(true);
        
        model = new ImgInfoModel(null);
        table = new JTable(model);
        JScrollPane sp = UIHelper.createScrollPane(table, true);
        UIHelper.setVisibleRowCount(table, 8);
        table.getColumnModel().getColumn(0).setCellRenderer(new RightTableCellRenderer());
        table.setTableHeader(null);
        UIHelper.calcColumnWidths(table);
        
        blueMarbleDisplay = new ImageDisplay(IMG_SIZE, IMG_SIZE/2, false, false);
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p,8px,f:p:g,4px,p"));
        
        imgDisplay = new ImageDisplay(IMG_SIZE, IMG_SIZE, false, false);
        Dimension s = new Dimension(IMG_SIZE, IMG_SIZE);
        imgDisplay.setSize(s);
        imgDisplay.setPreferredSize(s);
        
        pb.add(imgDisplay,        cc.xy(2,  1));
        pb.add(sp,                cc.xyw(1, 3, 3));
        pb.add(blueMarbleDisplay, cc.xy(2,  5));
        
        imgDisplay.setBackground(Color.WHITE);
        imgDisplay.setOpaque(true);

        setBackground(Color.WHITE);
        setOpaque(true);
        
        blueMarbleFetcher = new BlueMarbleFetcher(IMG_SIZE, IMG_SIZE/2, new BufferedImageFetcherIFace() {
            @Override
            public void imageFetched(BufferedImage image)
            {
                if (image != null)
                {
                    blueMarble = image;
                    blueMarbleDisplay.setImage(blueMarble);
                    
                    ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
                    colorConvert.filter(blueMarble, blueMarble);
    
                    // 0.21 R + 0.71 G + 0.07 B
                }
            }

            @Override
            public void error()
            {
            }
        });
        blueMarbleFetcher.init();
        markerImg = blueMarbleFetcher.getMarkerImg();
        
        setLayout(new BorderLayout());
        
        JScrollPane sb = new JScrollPane(pb.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sb, BorderLayout.CENTER);
        
        super.doneBuilding();
    }
    
    /**
     * @param imgDataItem the imgDataItem to set
     */
    public void setImgDataItem(final ImageDataItem imgDataItem)
    {
        this.imgDataItem = imgDataItem;
        if (imgDataItem != null)
        {
            model.setItems(imagesPane.getImageDataValueList(imgDataItem));
            
            ImageIcon img = imgDataItem.getImgIcon();
            //System.out.println(String.format("%d,%d", img.getIconWidth(), ImageDataItem.STD_ICON_SIZE));
            if (img == null || img.getIconWidth() != IMG_SIZE)
            {
                imgDataItem.loadScaledImage(IMG_SIZE, new ImageLoaderListener()
                {
                    @Override
                    public void imagedLoaded(String imageName,
                                             String mimeType,
                                             boolean doLoadFullImage,
                                             int scale,
                                             boolean isError, 
                                             ImageIcon imgIcon,
                                             File localFile)
                    {
                        imgDisplay.setImage(imgIcon);
                    }
                });
                imgDisplay.setImage(IconManager.getImage("Loading"));
                imgDisplay.repaint();
            } else
            {
                //System.out.println(img);
                imgDisplay.setImage(img);
            }
        } else
        {
            imgDisplay.setImage((ImageIcon)null);
            model.setItems(null);
            blueMarbleDisplay.setImage(blueMarbleFetcher.getBlueMarbleImage());
        }
        
        if (imgDataItem != null)
        {
            boolean isPointSet = false;
            HashMap<String, Object> map = imgDataItem.getDataMap(); 
            if (map == null)
            {
                map = dataFetcher.getData(imgDataItem.getAttachmentId(), imgDataItem.getTableId());
                imgDataItem.setDataMap(map);
            }
            if (map != null)
            {
                BigDecimal lat = (BigDecimal)map.get("Latitude1");
                BigDecimal lon = (BigDecimal)map.get("Longitude1");
                if (lat != null && lon != null)
                {
                    BufferedImage bi = blueMarbleFetcher.plotPoint(lat.doubleValue(), lon.doubleValue());
                    blueMarbleDisplay.setImage((Image)null);  
                    blueMarbleDisplay.setImage(bi);  
                    isPointSet = true;
                }
            }
            if (!isPointSet)
            {
                blueMarbleDisplay.setImage(blueMarble);                
            }
        }
    }
    
    class RightTableCellRenderer extends DefaultTableCellRenderer
    {
        protected RightTableCellRenderer()
        {
            setHorizontalAlignment(JLabel.RIGHT);
        }

    }
    
    class ImgInfoModel extends DefaultTableModel
    {
        private Vector<Pair<String, Object>> items = null;
        
        public ImgInfoModel(final Vector<Pair<String, Object>> items)
        {
            super();
            this.items = items;
        }
        
        /**
         * @return the items
         */
        public Vector<Pair<String, Object>> getItems()
        {
            return items;
        }

        /**
         * @param items the items to set
         */
        public void setItems(Vector<Pair<String, Object>> items)
        {
            this.items = items;
            fireTableDataChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return items == null ? 0 : items.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return column == 0 ? "Attribute" : "Value";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            Pair<String, Object> item = items.get(row);
            return column == 0 ? item.first + " " : item.second;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int column)
        {
            return column == 0 ? String.class : Object.class;
        }
        
    }
}
