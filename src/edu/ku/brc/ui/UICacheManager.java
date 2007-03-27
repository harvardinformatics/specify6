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
package edu.ku.brc.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.exceptions.UIException;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.FileCache;

/**
 * This class manages all things UI. It is the central clearing house for UI components.
 * Meaning you can register various UI components by name and then get them later.<br><br>
 * <br>
 * Using the <i>getMostRecentFrame</i> method<br>
 * You <i>SHOULD</i> set the RECENTFRAME ui component when you are in a dialog and then any error dialogs
 * can be parented to your dialog. But if you set it you MUST set it back to null. (NOte if you forget it will always return
 * the TOPFRAME, but don't rely on this)

 * @code_status Complete
 **
 * @author rods
 *
 */
public class UICacheManager
{
    // Static Data Members
    protected static final String MISSING_FACTORY_MSG = "The object has not been set for the ViewBasedDialogFactoryIFace. This class can be used without first setting a factory implementing this interface.";

    public static final String FRAME        = "frame";
    public static final String MENUBAR      = "menubar";
    public static final String TOOLBAR      = "toolbar";
    public static final String STATUSBAR    = "statusbar";
    public static final String TOPFRAME     = "topframe";
    public static final String GLASSPANE    = "glasspane";
    public static final String MAINPANE     = "mainpane";
    public static final String RECENTFRAME  = "recentframe";
    
    // Standard Actions
    public static final String UNDO         = "Undo";
    public static final String REDO         = "Redo";
    public static final String CUT          = "Cut";
    public static final String COPY         = "Copy";
    public static final String PASTE        = "Paste";
    public static final String FIND         = "Find";
    public static final String FINDREPLACE  = "FindReplace";
    public static final String INSERT       = "Insert";
    public static final String ADD          = "Add";
    public static final String DELETE       = "Delete";
    public static final String Clear        = "Clear";
    

    public static final String LONGTERM_CACHE_MAP = "longterm-cache-map.xml";

    private static final Logger           log      = Logger.getLogger(UICacheManager.class);
    protected static final UICacheManager instance = new UICacheManager();

    // Data Members
    protected Hashtable<String, Component> components = new Hashtable<String, Component>();

    protected Hashtable<String, Hashtable<String, JComponent>> uiItems = new Hashtable<String, Hashtable<String, JComponent>>();

    protected Font           baseFont           = null;
    
    protected ResourceBundle resourceBundle     = null;
    protected String         resourceName       = "resources";

    protected FileCache      longTermCache      = null;
    protected FileCache      shortTermCache     = null;

    protected String         defaultWorkingPath = null;
    protected String         appName            = null;
    protected boolean        useCurrentLocation = false;
    
    protected boolean        isRelease          = false;

    protected ViewBasedDialogFactoryIFace viewbasedFactory = null;
    
    protected Hashtable<String, Action> actionMap = new Hashtable<String, Action>();
    
    //------------------------------------------------
    // Undo / Redo Helpers
    //------------------------------------------------
    protected static UndoAction       undoAction; // these three are special
    protected static RedoAction       redoAction;
    protected static LaunchFindReplaceAction launchReplaceAction;
    
    protected HashMap<Object, Action> actions = null;
    
    static 
    {
        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager(); 
        focusManager.addPropertyChangeListener( 
            new PropertyChangeListener() { 
                public void propertyChange(PropertyChangeEvent e) 
                {
                    String prop = e.getPropertyName(); 
                    //System.out.println(prop+"  "+focusManager.getFocusOwner()+" "+focusManager.getFocusedWindow());
                    if (("focusOwner".equals(prop)) && undoAction != null && redoAction != null) 
                    { 
                        if (focusManager.getFocusOwner() instanceof UndoableTextIFace)
                        {
                            //System.out.println("Owner");

                            UndoableTextIFace undoableText = (UndoableTextIFace)focusManager.getFocusOwner();
                            if (undoableText != null)
                            {
                                //System.err.println("Hooking up undo manager for "+undoableText);
                                undoAction.setUndoManager(undoableText.getUndoManager());
                                redoAction.setUndoManager(undoableText.getUndoManager());
                            }
                        } else if (focusManager.getFocusOwner() instanceof JTextComponent)
                        {
                            undoAction.setUndoManager(null);
                            redoAction.setUndoManager(null);
                        }
                        undoAction.updateUndoState();
                        redoAction.updateRedoState();
                    }
                    

                } 
            } 
        );
    }
    

    /**
     * Default private constructor for singleton.
     *
     */
    private UICacheManager()
    {
        if (resourceBundle == null)
        {
            try {
                // Get the resource bundle for the default locale
                resourceBundle = ResourceBundle.getBundle(resourceName);

            } catch (MissingResourceException ex) {
                log.error("Couldn't find Resource Bundle Name["+resourceName+"]", ex);
            }
        }
    }

    /**
     * Returns the singleton.
     * @return the singleton instance
     */
    public static UICacheManager getInstance()
    {
        return instance;
    }

    /**
     *  Return the current ResourceBundle.
     * @return the current ResourceBundle
     */
    protected static ResourceBundle getResourceBundleInternal()
    {
        return instance.getResourceBundle();
    }


    /**
     * Returns whether this is a release.
     * @return whether this is a release.
     */
    public static boolean isRelease()
    {
        return instance.isRelease;
    }

    /**
     * Sets whether this is a release.
     * @param isRelease the isRelease to set
     */
    public static void setRelease(boolean isRelease)
    {
        instance.isRelease = isRelease;
    }

    /**
     * Returns the "working" directory which is platform specific. It will create one if one is not created
     * <b>NOTE: The application name must be set first.</b><br>
     * One Windows it is ...\<i>&lt;user name&gt;</i>\Application Data\&lt;application name&gt;<br>
     * On Unix based platforms it will create a directory of the "application name" with a "." in front. 
     * For example: <code>/home/john/.specify</code>
     * @param useCurrent whether to use the current directory or the user home directory
     * @return Returns the "working" directory which is platform specific.
     */
    public static String getDefaultWorkingPath(final boolean useCurrent)
    {
        if (instance.defaultWorkingPath == null)
        {
            instance.defaultWorkingPath = getUserDataDir(useCurrent);
        }
        return instance.defaultWorkingPath;
    }

    /**
     * Returns the "working" directory which is platform specific. It will create one if one is not created
     * <b>NOTE: The application name must be set first.</b><br>
     * One Windows it is ...\<i>&lt;user name&gt;</i>\Application Data\&lt;application name&gt;<br>
     * On Unix based platforms it will create a directory of the "application name" with a "." in front. 
     * For example: <code>/home/john/.specify</code>
     * @return Returns the "working" directory which is platform specific.
     */
    public static String getDefaultWorkingPath()
    {
        return getDefaultWorkingPath(instance.useCurrentLocation);
    }

    /**
     * Creates a File object for a Sub-Directory inside the Default Working Path Directory.
     * If the directory doesn't exist you can have it created.  
     * @param dirName the new or existing directory name
     * @param createIt true to create it if it doesn't exist, false doesn't create it
     * @return a File object to the the directory
     */
    public static File getDefaultWorkingPathSubDir(final String dirName, final boolean createIt)
    {
        File newDir = new File(getDefaultWorkingPath() + File.separator + dirName);
        if (!newDir.exists() && createIt)
        {
            if (!newDir.mkdirs())
            {
                return null;
            }
        }
        return newDir;
    }

    /**
     * Set the working directory. It is not recommended to use this because the working directory will automatically be created.
     * @param defaultWorkingPath the new and different working directory.
     */
    public static void setDefaultWorkingPath(String defaultWorkingPath)
    {
        instance.defaultWorkingPath = defaultWorkingPath;
    }
    
    /**
     * Get the "user" based working directory that is platform specific and requires the "application name" be set first. 
     * @return the string to a platform specify user data directory for the application name.
     */
    public static String getUserDataDir()
    {
        return getUserDataDir(instance.useCurrentLocation);
    }
    
    /**
     * Get the "user" based working directory that is platform specific and requires the "application name" be set first. 
     * @param useCurrentLoc true use current
     * @return the string to a platform specify user data directory for the application name.
     */
    public static String getUserDataDir(final boolean useCurrentLoc)
    {
        if (instance.appName == null)
        {
            throw new RuntimeException("The AppName has not been set into the UICacheManger!");
        }
        
        String base;
        if (useCurrentLoc)
        {
            File   file     = new File(".");
            String fullPath = file.getAbsolutePath();
            fullPath        = fullPath.substring(0, fullPath.length()-1);
            if (System.getProperty("os.name").indexOf("Windows") > -1)
            {
                base = fullPath + instance.appName;
            } else
            {
                base = fullPath + "." + instance.appName;
            }
            
        } else
        {
            if (System.getProperty("os.name").indexOf("Windows") > -1)
            {
                base = System.getenv("APPDATA") + File.separator + instance.appName;
            } else
            {
                base = System.getProperty("user.home") + File.separator + "." + instance.appName;
            }
            File baseDir = new File(base);
            if (!baseDir.exists())
            {
                if (!baseDir.mkdir())
                {
                    throw new RuntimeException("Couldn't create data directory for "+instance.appName+" ["+baseDir.getAbsolutePath()+"]");
                }
            }
        }
        return base;
    }

    /**
     * Returns the current application name.
     * @return the current application name.
     */
    public static String getAppName()
    {
        return instance.appName;
    }

    /**
     * Sets the application name and this name cannot be changed (meaning it can only be set once).
     * @param appName the application name (it is best if it doesn't have a space in the middle)
     */
    public static void setAppName(final String appName)
    {
        if (StringUtils.isNotEmpty(appName) && StringUtils.isEmpty(instance.appName))
        {
            instance.appName = appName;
        } else
        {
            throw new RuntimeException("You cannot set the app name twice or with an empty string!");
        }
    }

    /**
     * Registers a uicomp.
     * @param category the category to be registered
     * @param name the name
     * @param uiComp the ui component
     * @throws UIException throws exception if it is already registered
     */
    public static void registerUI(final String category, final String name, final JComponent uiComp) throws UIException
    {
        Hashtable<String, JComponent> compsHash = instance.uiItems.get(category);
        if (compsHash == null)
        {
            compsHash = new Hashtable<String, JComponent>();
            instance.uiItems.put(category, compsHash);
        }
        if (compsHash.containsKey(name))
        {
           throw new UIException("UI component with Name["+name+"] has already been registered to ["+category+"].");
        }
        compsHash.put(name, uiComp);
    }

    /**
     * Unregisters a uicomp.
     * @param category the category to be registered
     * @param name the name
     * @throws UIException throws exception if it is not registered
     */
    public static void unregisterUI(final String category, final String name) throws UIException
    {
        Hashtable<String, JComponent> compsHash = instance.uiItems.get(category);
        if (compsHash == null)
        {
            throw new UIException("Couldn't find UI Category with Name["+category+"].");
        }
        JComponent comp = compsHash.get(name);
        if (comp == null)
        {
           throw new UIException("Couldn't find UI component with Name["+name+"].");
        }
        compsHash.remove(comp);
    }

    /**
     * Returns a UI component by name.
     * @param name the name of the component to be retrieved
     * @return a UI component by name
     */
    public static Component get(final String name)
    {
        return instance.components.get(name);
    }

    /**
     * Returns the main ResourceBundle.
     * @return Returns the main ResourceBundle
     */
    public ResourceBundle getResourceBundle()
    {
        return resourceBundle;
    }

    /**
     * Returns the reourceName.
     * @return Returns the reourceName.
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * Sets the resource name.
     * @param resourceName The reourceName to set.
     */
    public void setResourceName(final String resourceName)
    {
        this.resourceName = resourceName;
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    protected String getResourceStringInternal(final String key)
    {
        try 
        {
            //log.error("["+key+"]["+resourceBundle.getString(key)+"]");
            return resourceBundle.getString(key);
            
        } catch (MissingResourceException ex) 
        {
            log.error("Couldn't find key["+key+"] in resource bundle.");
            return key;
        }
    }

    /**
     * Returns a localized string from the resource bundle (masks the thrown expecption).
     * @param key the key to look up
     * @return  Returns a localized string from the resource bundle
     */
    public static String getResourceString(final String key)
    {
        return instance.getResourceStringInternal(key);
    } 
    
    /**
     * Formats an Internationalized string with a variable argument list.
     * @param key the I18N key
     * @param args the list args
     * @return a formatted string
     */
    public static String getLocalizedMessage(final String key, final Object ... args)
    {
        return String.format(getResourceString(key), args);
    }

    /**
     * Returns the ViewBasedFacory for the application.
     * @return the ViewBasedFacory for the application
     */
    public static ViewBasedDialogFactoryIFace getViewbasedFactory()
    {
        if (instance.viewbasedFactory == null)
        {
            String className = System.getProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", null);
            if (StringUtils.isNotEmpty(className))
            {
                try 
                {
                    instance.viewbasedFactory = (ViewBasedDialogFactoryIFace)Class.forName(className).newInstance();
                   
                } catch (Exception e) 
                {
                    InternalError error = new InternalError("Can't instantiate ViewBasedDialogFactoryIFace factory " + className);
                    error.initCause(e);
                    throw error;
                }
                
            } else
            {
                throw new InternalError(MISSING_FACTORY_MSG);
            }
        }

        return instance.viewbasedFactory;
    }

    /**
     * Sets the ViewBasedFacory for the application.
     * @param viewbasedFactory the factory
     */
    /*public static void setViewbasedFactory(ViewBasedDialogFactoryIFace viewbasedFactory)
    {
        instance.viewbasedFactory = viewbasedFactory;
    }*/

    /**
     * Registers a uiComp into the applications.
     * @param name the name of the UI component to be registered
     * @param uiComp the UI component to be registered
     */
    public static void register(final String name, final Component uiComp)
    {
        if (uiComp != null)
        {
            if (instance.components.get(name) == null)
            {
                instance.components.put(name, uiComp);
            } else
            {
                throw new RuntimeException("Registering a uiComp with an existing name["+ name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to register a null UI Component!");
        }
    }

    /**
     * Unregisters a uiComp from the application.
     * @param name the name of the UI component to be unregistered
     */
    public static void unregister(final String name)
    {
        if (name != null)
        {
            if (instance.components.get(name) != null)
            {
                instance.components.remove(name);
            } else
            {
                throw new RuntimeException("Unregistering a uiComp that has been registered ["+name+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to unregister with a null name!");
        }
    }

    /**
     * Displays a message in the status bar.
     * @param text the text to be displayed
     */
    public static void displayStatusBarText(final String text)
    {
        JStatusBar statusBar = ((JStatusBar)instance.components.get(STATUSBAR));
        if (statusBar != null)
        {
            statusBar.setText(text == null ? "" : text);
        }
    }

    /**
     * Displays a message in the status bar.
     * @param key the key of the string that is to appear in the status bar. The resource string will be looked up
     */
    public static void displayLocalizedStatusBarText(final String key)
    {
        if (key == null) throw new NullPointerException("Call to displayLocalizedStatusBarText cannot be null!");

        String localizedStr = instance.getResourceStringInternal(key);
        assert localizedStr != null : "Localized String for key["+key+"]";

        displayStatusBarText(localizedStr);


    }

    /**
     * Repaints the top most frame.
     *
     */
    public static void forceTopFrameRepaint()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                JFrame frame = ((JFrame)instance.components.get(TOPFRAME));
                assert frame != null : "The top frame has not been registered";
                frame.repaint();
            }
          });
    }

    /**
     * Returns the glass pane from the mgr.
     * @return Returns the glass pane from the mgr
     */
    public static GhostGlassPane getGlassPane()
    {
        return ((GhostGlassPane)instance.components.get(GLASSPANE));
    }

    /**
     * Returns the most recent frame to be used, but note that there is no magic here. You
     * must set the the most recent frame in order for it to be used by someone else. The one
     * thing it does do for you, is that if you forgot to set it and someone else uses it it does
     * check to make sure it is not null AND visible. If either of these are true then it returns the TOPFRAME.
     * 
     * @return Returns the most recent frame to be used, but note that there is no magic here. You
     * must set the the most recent frame in order for it to be used by someone else. The one
     * thing it does do for you, is that if you forgot to set it and someone else uses it it does
     * check to make sure it is not null AND visible. If either of these are true then it returns the TOPFRAME.
     */
    public static Component getMostRecentFrame()
    {
        Component recent = instance.components.get(RECENTFRAME);
        return recent == null || !recent.isVisible() ? instance.components.get(TOPFRAME) : recent;
    }

    /**
     * Display an Error dialog.
     * @param msg the message to be displayed
     */
    public static void displayErrorDlg(final String msg)
    {
         JOptionPane.showMessageDialog(getMostRecentFrame(), msg, getResourceString("error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an Error dialog that gets its string from the resource bundle.
     * @param msg the message to be displayed
     */
    public static void displayErrorDlgLocalized(final String key)
    {
         JOptionPane.showMessageDialog(getMostRecentFrame(), getResourceString(key), getResourceString("error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display an Confirmation Dialog where everything comes ffrom the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param iconOption the icon to show
     * @return true is YES false if NO
     */
    public static boolean displayConfirm(final String title, 
                                         final String msg,
                                         final String keyBtn1,
                                         final String keyBtn2,
                                         final int    iconOption)
    {
        // Custom button text
        Object[] options = { keyBtn1, keyBtn2 };
        
        return JOptionPane.showOptionDialog(getMostRecentFrame(), msg, 
                title, JOptionPane.YES_NO_OPTION,
                iconOption, null, options, options[1]) == JOptionPane.YES_OPTION;
    }

    /**
     * Display an Confirmation Dialog where everything comes ffrom the bundle.
     * @param titleKey the key to the dialog title
     * @param msgKey the key to the dialog message
     * @param keyBtn1 the key to the first button 
     * @param keyBtn2 the key to the second button
     * @param iconOption the icon to show
     * @return true is YES false if NO
     */
    public static boolean displayConfirmLocalized(final String titleKey, 
                                                  final String msgKey,
                                                  final String keyBtn1,
                                                  final String keyBtn2,
                                                  final int    iconOption)
    {
        return displayConfirm(getResourceString(titleKey), getResourceString(msgKey), getResourceString(keyBtn1), getResourceString(keyBtn1), iconOption);
    }

    //----------------------------------------------------------------------------------
    // File Cache Section
    //----------------------------------------------------------------------------------

    /**
     * Returns the longTermCache.
	 * @return the longTermCache.
	 */
	public static FileCache getLongTermFileCache()
	{
        if (instance.longTermCache == null)
        {
            try
            {
                instance.longTermCache = new FileCache("longTerm.Cache");
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
		return instance.longTermCache;
	}

	/**
     * Sets the longTermCache.
	 * @param longTermCache The longTermCache to set.
	 */
	public static void setLongTermFileCache(FileCache longTermCache)
	{
		instance.longTermCache = longTermCache;
	}

	/**
     * Gets the shortTermCache.
	 * @return the shortTermCache.
	 */
	public static FileCache getShortTermFileCache()
	{
        if (instance.shortTermCache == null)
        {
            try
            {
                instance.shortTermCache = new FileCache("shortTermCache");
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
		return instance.shortTermCache;
	}

	/**
     * Sets the shortTermCache.
	 * @param shortTermCache The shortTermCache to set.
	 */
	public static void setShortTermFileCache(FileCache shortTermCache)
	{
		instance.shortTermCache = shortTermCache;
	}

    /**
     * Creates the initial font mapping from the base font size to the other sizes.
     * @param clazz the class of the component
     * @param baseFontArg the base font size
     */
    protected static void adjustAllFonts(final Font oldBaseFont, final Font baseFontArg)
    {
        int    fontSize    = baseFontArg.getSize();
        int    oldFontSize = oldBaseFont.getSize();
        String family   = baseFontArg.getFamily();
        
        UIDefaults uiDefaults = UIManager.getDefaults();
        Enumeration<Object> e = uiDefaults.keys();
        while (e.hasMoreElements())
        {
            Object key = e.nextElement();
            if (key.toString().endsWith(".font"))
            {
                FontUIResource fontUIRes = (FontUIResource)uiDefaults.get(key);
                if (fontSize != fontUIRes.getSize() || !family.equals(fontUIRes.getFamily()))
                {
                    UIManager.put(key, new FontUIResource(new Font(family, fontUIRes.getStyle(), fontSize + (fontUIRes.getSize() - oldFontSize))));
                }
            }
        }
    }
    
    /**
     * Return the base font for the UI component.
     * @return the base font for the UI.
     */
    public static Font getBaseFont()
    {
        return instance.baseFont;
    }
    
    /**
     * Sets the base font and builds all the control's fonts.
     * @param newBaseFont the new font
     */
    public static void setBaseFont(final Font newBaseFont)
    {
        if (instance.baseFont != null && instance.baseFont != newBaseFont)
        {
            adjustAllFonts(instance.baseFont, newBaseFont);
        }
        instance.baseFont = newBaseFont;
    }

    /**
     * Indicates whether we should use the current location as the default working path.
     * @param useCurrentLocation true/false.
     */
    public static void setUseCurrentLocation(boolean useCurrentLocation)
    {
        instance.useCurrentLocation = useCurrentLocation;
    }
    
    //---------------------------------------------------------
    //-- Undo / Redo and Other Action Stuff
    //---------------------------------------------------------

    /**
     * Returns an Action by name.
     * @param name the name of the action
     */
    public static Action getAction(final String name)
    {
        return instance.actionMap.get(name);
    }
    
    /**
     * Register's an action.
     * @param name thee name of the action
     * @param action the action 
     * @return the action passed in
     */
    public static Action registerAction(final String name, final Action action)
    {
        if (instance.actionMap.get(name) == null)
        {
            instance.actionMap.put(name, action);
            
        } else
        {
            log.error("Action with name["+name+"] is already registered.");
        }
        return action;
    }
    
    /**
     * Adds Key navigation bindings to a component. (Is this needed?)
     * @param comp the component
     */
    public void addNavBindings(JComponent comp) 
    {
        InputMap inputMap = comp.getInputMap();

        //Ctrl-b to go backward one character
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.backwardAction);

        //Ctrl-f to go forward one character
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.forwardAction);

        //Ctrl-p to go up one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.upAction);

        //Ctrl-n to go down one line
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        inputMap.put(key, DefaultEditorKit.downAction);
    }
    
    // Create the edit menu
    public JMenu createEditMenu()
    {
       JMenu menu = new JMenu(getResourceString("Edit"));
       menu.setMnemonic(KeyEvent.VK_E);
       // Undo and redo are actions of our own creation.
       undoAction = (UndoAction) makeAction(UndoAction.class,
                                            this,
                                            "Undo",
                                            null,
                                            null,
                                            new Integer(KeyEvent.VK_U),
                                            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(undoAction);
       actionMap.put(UNDO, undoAction);
       redoAction = (RedoAction) makeAction(RedoAction.class,
                                            this,
                                            "Redo",
                                            null,
                                            null,
                                            new Integer(KeyEvent.VK_R),
                                            KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(redoAction);
       actionMap.put(REDO, redoAction);
       
       menu.addSeparator();
       // These actions come from the default editor kit.  Get the ones we want
       // and stick them in the menu.
       Action cutAction = makeAction(DefaultEditorKit.CutAction.class,
                                     null,
                                     "Cut",
                                     null,
                                     "Cut selection to clipboard",
                                     new Integer(KeyEvent.VK_X),
                                     KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(cutAction);
       actionMap.put(CUT, cutAction);
       
       Action copyAction = makeAction(DefaultEditorKit.CopyAction.class,
                                      null,
                                      "Copy",
                                      null,
                                      "Copy selection to clipboard",
                                      new Integer(KeyEvent.VK_C),
                                      KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(copyAction);
       actionMap.put(COPY, copyAction);
       
       Action pasteAction = makeAction(DefaultEditorKit.PasteAction.class,
                                       null,
                                       "Paste",
                                       null,
                                       "Paste contents of clipboard",
                                       new Integer(KeyEvent.VK_P),
                                       KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(pasteAction);
       actionMap.put(PASTE, pasteAction);
       /*
       menu.addSeparator();
       Action selectAllAction = makeAction(SelectAllAction.class,
                                           this,
                                           "Select All",
                                           null,
                                           "Select all text",
                                           new Integer(KeyEvent.VK_A),
                                           KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
       menu.add(selectAllAction);
       */
       
       launchReplaceAction = (LaunchFindReplaceAction) makeAction(LaunchFindReplaceAction.class,
               this,
               "Find",
               null,
               null,
               new Integer(KeyEvent.VK_F),
               KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(launchReplaceAction);
        actionMap.put(FINDREPLACE, launchReplaceAction);

       return menu;
    }

    @SuppressWarnings("unchecked")
    protected Action makeAction(Class actionClass,
                                Object owner,
                                String name,
                                ImageIcon icon,
                                String toolTip,
                                Integer mnemonicKeyCode,
                                KeyStroke acceleratorKey)
    {
        Action a = null;
        try
        {
            Constructor c;
            if (owner != null)
            {
                // If the class is an inner class, its constuctor takes a hidden
                // parameter, an objext of it enclosing class.
                c = actionClass.getConstructor(new Class[] { owner.getClass() });
                a = (Action) c.newInstance(new Object[] { owner });
            } else
            {
                c = actionClass.getConstructor((Class[]) null);
                a = (Action) c.newInstance((Object[])null);
            }
            
            if (name != null)
                a.putValue(Action.NAME, name);
            
            if (icon != null)
                a.putValue(Action.SMALL_ICON, icon);
            
            if (toolTip != null)
                a.putValue(Action.SHORT_DESCRIPTION, toolTip);
            
            if (mnemonicKeyCode != null)
                a.putValue(Action.MNEMONIC_KEY, mnemonicKeyCode);
            
            if (acceleratorKey != null)
                a.putValue(Action.ACCELERATOR_KEY, acceleratorKey);
            
        } catch (ClassCastException e)
        {
            System.err.println("actionClass argument " + actionClass + " does not implement Action");
            //System.exit(-1);
            
        } catch (Exception e)
        {
            System.err.println(e + " -- while trying to make an instance of " + actionClass);
            try
            {
                // Output a list of constructors available for this class.
                Constructor[] cc = actionClass.getConstructors();
                for (int i = 0; i < cc.length; i++)
                {
                    System.err.println(cc[i].toString());
                }
            } catch (Exception ee)
            {
                log.error(ee);
            }
        }
        return a;
    }

    // This nested class is the child of desperation. If SelectAllAction was a
    // public static nested class of DefaultEditorKit as CopyAction is, this
    // hack wouldn't be needed.
    /*public class SelectAllAction extends AbstractAction
    {
        private final Action realAction = getActionByName(DefaultEditorKit.selectAllAction);

        public SelectAllAction()
        {
            super();
        }

        public SelectAllAction(final UICacheManager uic)
        {
            super();
        }

        public void actionPerformed(ActionEvent e)
        {
            realAction.actionPerformed(e);
        }

    }*/
    
    // The following two methods allow us to find an
    // action provided by the editor kit by its name.
    /*public HashMap<Object, Action> createActionTable(JTextComponent textComponent) 
    {
        actions = new HashMap<Object, Action>();
        Action[] actionsArray = textComponent.getActions();
        for (int i = 0; i < actionsArray.length; i++) 
        {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
        return actions;
    }*/


    /**
     * Returns an action name.
     * @param name
     * @return
     */
    /*private Action getActionByName(String name) 
    {
        return actions.get(name);
    }*/
    
    //------------------------------------------------------
    //-- An interface for all those wanting to play nice with
    //-- the undo mechanism
    //------------------------------------------------------
    public interface UndoableTextIFace
    {
        /**
         * @return the UndoManager
         */
        public UndoManager getUndoManager();
        
        /**
         * @return the JTextComponent that does undo
         */
        public JTextComponent getTextComponent();
    }

    //------------------------------------------------------
    //-- The UndoAction
    //------------------------------------------------------
    public class UndoAction extends AbstractAction
    {
        protected UndoManager undoManager = null;

        public UndoAction()
        {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (undoManager.canUndo())
                {
                    undoManager.undo();
                }
                
            } catch (CannotUndoException ex)
            {
                ex.printStackTrace();
            }
            updateUndoState();
            undoAction.updateUndoState();
        }

        protected void updateUndoState()
        {
            if (undoManager != null && undoManager.canUndo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getUndoPresentationName());
            } else
            {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }

        /**
         * @return the undo
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }

        /**
         * @param undo the undo to set
         */
        public void setUndoManager(UndoManager undo)
        {
            this.undoManager = undo;
            setEnabled(undo != null);
        }
    }

    //------------------------------------------------------
    //-- The RedoAction
    //------------------------------------------------------
    public class RedoAction extends AbstractAction
    {
        protected UndoManager undoManager = null;

        public RedoAction()
        {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if (undoManager.canRedo())
                {
                    undoManager.redo();
                }
            } catch (CannotRedoException ex)
            {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            updateRedoState();
            undoAction.updateUndoState();
        }

        protected void updateRedoState()
        {
            if (undoManager != null && undoManager.canRedo())
            {
                setEnabled(true);
                putValue(Action.NAME, undoManager.getRedoPresentationName());
            } else
            {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }

        /**
         * @return the undo
         */
        public UndoManager getUndoManager()
        {
            return undoManager;
        }

        /**
         * @param undo
         *            the undo to set
         */
        public void setUndoManager(UndoManager undo)
        {
            this.undoManager = undo;
            setEnabled(undo != null);
        }
    }
    
    //------------------------------------------------------
    //-- The LaunchFindAction  Action
    //------------------------------------------------------
    public class LaunchFindReplaceAction extends AbstractAction
    {
        protected SearchReplacePanel searchReplacePanel = null;
        //protected SearchableJXTable searchTable = null;
        public LaunchFindReplaceAction()
        {
            super("Find");
            setEnabled(false);
        }       

        public void actionPerformed(ActionEvent e)
        {
            log.debug("Ctrl-f hit from with UICacheManager - passing action onto the SearchReplacePanel");
            if(this.isEnabled())
            {
                if(searchReplacePanel != null)searchReplacePanel.getLaunchFindAction().actionPerformed(e);
                else log.error("search panel is null");
            }
        }

        public void removeSearchPanel()
        {
            this.searchReplacePanel = null;
            setEnabled(false);
        }

        /**
         * @return the undo
         */
        public SearchReplacePanel getSearchReplacePanel()
        {
            return searchReplacePanel;
        }

        /**
         */
        public void setSearchReplacePanel(SearchReplacePanel panel)
        {
            if (panel==null)log.error("Search panel is null but shouldn't be");
            this.searchReplacePanel = panel;
            setEnabled(true);
        }
    }
    //------------------------------------------------------
    //--  Listens for edits that can be undone.
    //------------------------------------------------------
    public class UICUndoableEditListener implements UndoableEditListener 
    {
        protected UndoManager undoManager;
        
        public UICUndoableEditListener(final UndoManager undoManager)
        {
            this.undoManager = undoManager;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
         */
        public void undoableEditHappened(UndoableEditEvent e) 
        {
            //Remember the edit and update the menus.
            if (undoManager != null)
            {
                undoManager.addEdit(e.getEdit());
            }
            if (undoAction != null)
            {
                undoAction.updateUndoState();
            }
            if (redoAction != null)
            {
                redoAction.updateRedoState();
            }
        }
    }
    
    /**
     * @return the redoAction
     */
    public static RedoAction getRedoAction()
    {
        return redoAction;
    }

    /**
     * @return the undoAction
     */
    public static UndoAction getUndoAction()
    {
        return undoAction;
    }

    /**
     * @return the launchReplaceAction
     */
    public static LaunchFindReplaceAction getLaunchFindReplaceAction()
    {
        return launchReplaceAction;
    }
    
    public static void disableFindFromEditMenu()
    {
        getLaunchFindReplaceAction().setEnabled(false);
    }
    
    public static void enableFindinEditMenu(SearchReplacePanel findPanel)
    {
        getLaunchFindReplaceAction().setSearchReplacePanel(findPanel);
    }
    /**
     * @param undoableText
     */
    public void hookUpUndoableEditListener(final UndoableTextIFace undoableText)
    {
        undoableText.getTextComponent().getDocument().addUndoableEditListener(new UICUndoableEditListener(undoableText.getUndoManager()));
    }
}

