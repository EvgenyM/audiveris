//----------------------------------------------------------------------------//
//                                                                            //
//                           P i c t u r e V i e w                            //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.sheet.picture;

import omr.log.Logger;

import omr.score.ui.PagePainter;
import omr.score.ui.PagePhysicalPainter;
import omr.score.ui.PaintingParameters;

import omr.selection.SheetLocationEvent;

import omr.sheet.*;

import omr.ui.view.RubberPanel;
import omr.ui.view.ScrollView;

import omr.util.Implement;
import omr.util.WeakPropertyChangeListener;

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Class <code>PictureView</code> defines the view dedicated to the display of
 * the picture bitmap of a music sheet.
 *
 * @author Hervé Bitteur
 */
public class PictureView
    extends ScrollView
    implements PropertyChangeListener
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(PictureView.class);

    //~ Instance fields --------------------------------------------------------

    /** Link with sheet */
    private final Sheet sheet;

    //~ Constructors -----------------------------------------------------------

    //-------------//
    // PictureView //
    //-------------//
    /**
     * Create a new <code>PictureView</code> instance, dedicated to a sheet.
     *
     * @param sheet the related sheet
     */
    public PictureView (Sheet sheet)
    {
        this.sheet = sheet;

        view = new MyView();
        view.setName("Picture-View");

        // Inject dependency of pixel location
        view.setLocationService(
            sheet.getSelectionService(),
            SheetLocationEvent.class);

        // Listen to painting parameters
        PaintingParameters.getInstance()
                          .addPropertyChangeListener(
            PaintingParameters.LAYER_PAINTING,
            new WeakPropertyChangeListener(this));

        // Insert view
        setView(view);
    }

    //~ Methods ----------------------------------------------------------------

    //----------------//
    // propertyChange //
    //----------------//
    @Implement(PropertyChangeListener.class)
    public void propertyChange (PropertyChangeEvent evt)
    {
        view.repaint();
    }

    //~ Inner Classes ----------------------------------------------------------

    //--------//
    // MyView //
    //--------//
    private class MyView
        extends RubberPanel
    {
        //~ Methods ------------------------------------------------------------

        //--------//
        // render //
        //--------//
        @Override
        public void render (Graphics2D g)
        {
            PaintingParameters painting = PaintingParameters.getInstance();

            // Render the picture image
            if (painting.isInputPainting()) {
                sheet.getPicture()
                     .render(g);
            } else {
                // Use a white background
                Color oldColor = g.getColor();
                g.setColor(Color.WHITE);

                Rectangle rect = g.getClipBounds();

                g.fill(rect);
                g.setColor(oldColor);
            }

            // Render the recognized score entities?
            if (painting.isOutputPainting()) {
                Color color = PagePainter.musicColor;
                sheet.getPage()
                     .accept(new PagePhysicalPainter(g, color, false));

                sheet.getStaffManager()
                     .render(g);
            } else {
                if (sheet.getTargetBuilder() != null) {
                    sheet.getTargetBuilder()
                         .renderDewarpGrid(g, true);
                }
            }
        }
    }
}
