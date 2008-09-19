//----------------------------------------------------------------------------//
//                                                                            //
//                       S h a p e F o c u s B o a r d                        //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.glyph.ui;

import omr.glyph.Glyph;
import omr.glyph.GlyphModel;
import omr.glyph.Shape;

import omr.selection.GlyphEvent;
import omr.selection.GlyphIdEvent;
import omr.selection.SelectionHint;
import omr.selection.UserEvent;

import omr.sheet.Sheet;

import omr.ui.Board;
import omr.ui.Board.Tag;
import omr.ui.field.SpinnerUtilities;
import static omr.ui.field.SpinnerUtilities.*;
import omr.ui.util.Panel;

import omr.util.Implement;
import omr.util.Logger;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import org.bushe.swing.event.EventSubscriber;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Class <code>ShapeFocusBoard</code> handles the shape that receives current
 * focus, and all glyphs whose shape corresponds to the focus (for example all
 * treble clefs glyphs if such is the focus)
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
class ShapeFocusBoard
    extends Board
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(
        ShapeFocusBoard.class);

    /** Events this board is interested in */
    private static final Collection<Class<?extends UserEvent>> eventClasses = new ArrayList<Class<?extends UserEvent>>();

    static {
        eventClasses.add(GlyphEvent.class);
    }

    //~ Enumerations -----------------------------------------------------------

    /** Filter on which symbols should be displayed */
    private static enum Filter {
        //~ Enumeration constant initializers ----------------------------------


        /** Display all symbols */
        ALL,
        /** Display only known symbols */
        KNOWN,
        /** Display only unknown symbols */
        UNKNOWN,
        /** Display only translated symbols */
        TRANSLATED,
        /** Display only untranslated symbols */
        UNTRANSLATED;
    }

    //~ Instance fields --------------------------------------------------------

    private final GlyphLagView view;
    private final GlyphModel   glyphModel;
    private final Sheet        sheet;

    /** Counter on symbols assigned to the current shape */
    private Counter assignedCounter = new Counter();

    /** Button to select the shape focus */
    private JButton selectButton = new JButton();

    /** Filter for known / unknown symbol display */
    private JComboBox filterButton = new JComboBox(Filter.values());

    /** Popup menu to allow shape selection */
    private JPopupMenu pm = new JPopupMenu();

    //~ Constructors -----------------------------------------------------------

    //-----------------//
    // ShapeFocusBoard //
    //-----------------//
    /**
     * Create the instance to handle the shape focus, with pointers to needed
     * companions
     *
     * @param sheet the related sheet
     * @param view the displayed lag view
     * @param glyphModel the related glyph model
     * @param filterListener the action linked to filter button
     */
    public ShapeFocusBoard (Sheet          sheet,
                            GlyphLagView   view,
                            GlyphModel     glyphModel,
                            ActionListener filterListener)
    {
        super(
            Tag.CUSTOM,
            sheet.getRadix() + "-ShapeFocusBoard",
            glyphModel.getLag().getEventService(),
            eventClasses);

        this.sheet = sheet;
        this.view = view;
        this.glyphModel = glyphModel;

        // Tool Tips
        selectButton.setToolTipText("Select candidate shape");
        selectButton.setHorizontalAlignment(SwingConstants.LEFT);
        selectButton.addActionListener(
            new ActionListener() {
                    public void actionPerformed (ActionEvent e)
                    {
                        pm.show(
                            selectButton,
                            selectButton.getX(),
                            selectButton.getY());
                    }
                });

        // Filter
        filterButton.addActionListener(filterListener);
        filterButton.setToolTipText(
            "Select displayed glyphs according to their current state");

        // Popup menu for shape selection
        JMenuItem noFocus = new JMenuItem("No Focus");
        noFocus.setToolTipText("Cancel any focus");
        noFocus.addActionListener(
            new ActionListener() {
                    public void actionPerformed (ActionEvent e)
                    {
                        setCurrentShape(null);
                    }
                });
        pm.add(noFocus);
        Shape.addShapeItems(
            pm,
            new ActionListener() {
                    public void actionPerformed (ActionEvent e)
                    {
                        JMenuItem source = (JMenuItem) e.getSource();
                        setCurrentShape(Shape.valueOf(source.getText()));
                    }
                });

        defineLayout();

        // Initially, no focus
        setCurrentShape(null);
    }

    //~ Methods ----------------------------------------------------------------

    //-----------------//
    // setCurrentShape //
    //-----------------//
    /**
     * Define the new current shape
     *
     * @param currentShape the shape to be considered as current
     */
    public void setCurrentShape (Shape currentShape)
    {
        assignedCounter.resetIds();

        if (currentShape != null) {
            // Update the shape button
            selectButton.setText(currentShape.toString());
            selectButton.setIcon(currentShape.getIcon());

            // Count the number of glyphs assigned to current shape
            for (Glyph glyph : sheet.getActiveGlyphs()) {
                if (glyph.getShape() == currentShape) {
                    assignedCounter.addId(glyph.getId());
                }
            }
        } else {
            // Void the shape button
            selectButton.setText("- No Focus -");
            selectButton.setIcon(null);
        }

        assignedCounter.refresh();
    }

    //-------------//
    // isDisplayed //
    //-------------//
    /**
     * Report whether the glyph at hand is to be displayed, according to the
     * current filter
     * @param glyph the glyph at hande
     * @return true if to be displayed
     */
    public boolean isDisplayed (Glyph glyph)
    {
        switch ((Filter) filterButton.getSelectedItem()) {
        case ALL :
            return true;

        case KNOWN :
            return glyph.isKnown();

        case UNKNOWN :
            return !glyph.isKnown();

        case TRANSLATED :
            return glyph.isKnown() && glyph.isTranslated();

        case UNTRANSLATED :
            return glyph.isKnown() && !glyph.isTranslated();
        }

        // To please the compiler
        return true;
    }

    //---------//
    // onEvent //
    //---------//
    /**
     * Notification about selection objects (the shape of a just modified glyph,
     * if not null, is used as the new shape focus)
     *
     * @param event the notified event
     */
    @Implement(EventSubscriber.class)
    public void onEvent (UserEvent event)
    {
        if (event instanceof GlyphEvent) {
            GlyphEvent glyphEvent = (GlyphEvent) event;

            if (glyphEvent.hint == SelectionHint.GLYPH_MODIFIED) {
                // Use glyph assigned shape as current shape, if not null
                Glyph glyph = glyphEvent.getData();

                if ((glyph != null) && (glyph.getShape() != null)) {
                    setCurrentShape(glyph.getShape());
                }
            }
        }
    }

    //--------------//
    // defineLayout //
    //--------------//
    private void defineLayout ()
    {
        final String buttonWidth = Panel.getButtonWidth();
        final String fieldInterval = Panel.getFieldInterval();
        final String fieldInterline = Panel.getFieldInterline();

        FormLayout   layout = new FormLayout(
            buttonWidth + "," + fieldInterval + "," + buttonWidth + "," +
            fieldInterval + "," + buttonWidth + "," + fieldInterval + "," +
            buttonWidth,
            "pref," + fieldInterline + "," + "pref");

        PanelBuilder builder = new PanelBuilder(layout, getComponent());
        builder.setDefaultDialogBorder();

        CellConstraints cst = new CellConstraints();

        int             r = 1; // --------------------------------
        builder.addSeparator("Focus", cst.xyw(1, r, 1));
        builder.add(selectButton, cst.xyw(3, r, 5));

        r += 2; // --------------------------------
        builder.add(filterButton, cst.xy(1, r));

        builder.add(assignedCounter.val, cst.xy(5, r));
        builder.add(assignedCounter.spinner, cst.xy(7, r));
    }

    //~ Inner Classes ----------------------------------------------------------

    //---------//
    // Counter //
    //---------//
    private class Counter
        implements ChangeListener
    {
        //~ Instance fields ----------------------------------------------------

        // Spinner on these glyphs
        ArrayList<Integer> ids = new ArrayList<Integer>();

        // Number of glyphs
        JLabel   val = new JLabel("", SwingConstants.RIGHT);
        JSpinner spinner = new JSpinner(new SpinnerListModel());

        //~ Constructors -------------------------------------------------------

        //---------//
        // Counter //
        //---------//
        public Counter ()
        {
            resetIds();
            spinner.addChangeListener(this);
            SpinnerUtilities.setList(spinner, ids);
            refresh();
        }

        //~ Methods ------------------------------------------------------------

        //-------//
        // addId //
        //-------//
        public void addId (int id)
        {
            ids.add(id);
        }

        //---------//
        // refresh //
        //---------//
        public void refresh ()
        {
            if (ids.size() > 1) { // To skip first NO_VALUE item
                val.setText(Integer.toString(ids.size() - 1));
                spinner.setEnabled(true);
            } else {
                val.setText("");
                spinner.setEnabled(false);
            }

            spinner.setValue(NO_VALUE);
        }

        //----------//
        // resetIds //
        //----------//
        public void resetIds ()
        {
            ids.clear();
            ids.add(NO_VALUE);
        }

        //--------------//
        // stateChanged //
        //--------------//
        @Implement(ChangeListener.class)
        public void stateChanged (ChangeEvent e)
        {
            int id = (Integer) spinner.getValue();

            if (id != NO_VALUE) {
                glyphModel.getLag()
                          .publish(
                    new GlyphIdEvent(this, SelectionHint.GLYPH_INIT, null, id));
            }
        }
    }
}
