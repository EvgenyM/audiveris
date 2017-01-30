//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                       A u d i v e r i s                                        //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Hervé Bitteur and others 2000-2017. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>

/**
 * Class {@code Audiveris} is simply a convenient entry point to OMR, which
 * delegates the call to {@link org.audiveris.omr.Main#main}.
 *
 * @author Hervé Bitteur
 */
public final class Audiveris
{
    //~ Constructors -------------------------------------------------------------------------------

    /** To avoid instantiation. */
    private Audiveris ()
    {
    }

    //~ Methods ------------------------------------------------------------------------------------
    //------//
    // main //
    //------//
    /**
     * The main entry point, which just calls {@link org.audiveris.omr.Main#main}.
     *
     * @param args These arguments are simply passed to Main
     */
    public static void main (final String[] args)
    {
        org.audiveris.omr.Main.main(args);
    }
}
