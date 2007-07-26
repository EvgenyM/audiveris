//----------------------------------------------------------------------------//
//                                                                            //
//                                S c r i p t                                 //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.script;

import omr.sheet.Sheet;

import omr.step.StepException;

import omr.util.Logger;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.xml.bind.annotation.*;

/**
 * Class <code>Script</code> handles a complete script applied to a sheet. A
 * script is a sequence of tasks. A script can be recorded as the user interacts
 * with the sheet data. It can be stored, reloaded and replayed.
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "script")
public class Script
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(Script.class);

    //~ Instance fields --------------------------------------------------------

    /** Sheet to which the script is applied */
    private Sheet sheet;

    /** Full path to the Sheet file name */
    @XmlAttribute(name = "sheet")
    private final String sheetPath;

    /** Sequence of tasks that compose the script */
    @XmlElements({@XmlElement(name = "step", type = StepTask.class)
        , @XmlElement(name = "assign", type = AssignTask.class)
        , @XmlElement(name = "deassign", type = DeassignTask.class)
        , @XmlElement(name = "segment", type = SegmentTask.class)
        , @XmlElement(name = "export", type = ExportTask.class)
    })
    private final List<Task> tasks = new ArrayList<Task>();

    /** Nb of tasks when stored, if any */
    private int storedTasksNb = 0;

    //~ Constructors -----------------------------------------------------------

    //--------//
    // Script //
    //--------//
    /**
     * Create a script
     *
     * @param sheet the related sheet
     */
    public Script (Sheet sheet)
    {
        this.sheet = sheet;
        sheetPath = sheet.getPath();
    }

    //--------//
    // Script //
    //--------//
    /** No-arg constructor for JAXB */
    private Script ()
    {
        sheetPath = null;
    }

    //~ Methods ----------------------------------------------------------------

    //----------//
    // isStored //
    //----------//
    /**
     * Check whether the script is consistent with its backup on disk
     *
     * @return true if OK
     */
    public boolean isStored ()
    {
        return tasks.size() == storedTasksNb;
    }

    //-----------//
    // setStored //
    //-----------//
    /**
     * Flag the script as being currently consistent with its backup
     */
    public void setStored ()
    {
        storedTasksNb = tasks.size();
    }

    //----------//
    // getSheet //
    //----------//
    /**
     * Report the sheet this script is linked to
     *
     * @return the sheet concerned
     */
    public Sheet getSheet ()
    {
        return sheet;
    }

    //---------//
    // addTask //
    //---------//
    /**
     * Add a task to the script
     *
     * @param task the task to add at the end of the current sequence
     */
    public void addTask (Task task)
    {
        tasks.add(task);

        if (logger.isFineEnabled()) {
            logger.fine("Script: added task " + task);
        }
    }

    //------//
    // dump //
    //------//
    /**
     * Meant for debug, dumps the script to the output console
     */
    public void dump ()
    {
        System.out.println();
        System.out.println(toString());

        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    //-----//
    // run //
    //-----//
    /**
     * This methods runs sequentially and synchronously the various tasks of the
     * script. It is up to the caller to run this method in a separate thread.
     */
    public void run ()
    {
        if (logger.isFineEnabled()) {
            logger.fine(
                "Running " + this +
                ((sheet != null) ? (" on sheet " + sheet.getRadix()) : ""));
        }

        // Make sheet concrete
        if (sheet == null) {
            if (sheetPath == null) {
                logger.warning("No sheet defined in script");

                return;
            }

            try {
                sheet = new omr.sheet.Sheet(new java.io.File(sheetPath), false);
            } catch (StepException ex) {
                logger.warning("Cannot load sheet from " + sheetPath, ex);

                return;
            }
        }

        // Run the tasks in sequence
        try {
            for (Task task : tasks) {
                // Actually run this task
                if (logger.isFineEnabled()) {
                    logger.fine(
                        "Launching " + task + " on sheet " + sheet.getRadix());
                }

                task.run(sheet);
            }

            if (logger.isFineEnabled()) {
                logger.fine("All tasks launched on sheet " + sheet.getRadix());
            }

            // Kludge, to put the Glyphs tab on top of all others.
            SwingUtilities.invokeLater(
                new Runnable() {
                        public void run ()
                        {
                            sheet.getAssembly()
                                 .selectTab("Glyphs");
                        }
                    });

            // Flag the active script as up-to-date
            sheet.getScript()
                 .setStored();
        } catch (StepException ex) {
            logger.warning("Task aborted", ex);
        }
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{Script");

        if (sheetPath != null) {
            sb.append(" ")
              .append(sheetPath);
        } else if (sheet != null) {
            sb.append(" ")
              .append(sheet.getRadix());
        }

        if (tasks != null) {
            sb.append(" tasks:")
              .append(tasks.size());
        }

        sb.append("}");

        return sb.toString();
    }
}