/*
 * -----------------------------------------------------------------------
 * Copyright © 2013-2018 Meno Hochschild, <http://www.menodata.de/>
 * -----------------------------------------------------------------------
 * This file (TZDATA.java) is part of project Time4J.
 *
 * Time4J is free software: You can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Time4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Time4J. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------
 */

package net.time4j.tz.repo;

import net.time4j.base.ResourceLoader;
import net.time4j.scale.LeapSecondProvider;
import net.time4j.tz.ZoneModelProvider;
import net.time4j.tz.ZoneNameProvider;


/**
 * <p>Small helper class for registering this module in OSGi etc. </p>
 *
 * @author  Meno Hochschild
 * @since   3.0
 */
/*[deutsch]
 * <p>Kleine Hilfsklasse zur Registrierung dieses Moduls in OSGi usw. </p>
 *
 * @author  Meno Hochschild
 * @since   3.0
 */
public final class TZDATA {

    private TZDATA() {
        // no instantiation
    }

    /**
     * <p>Convenient short form for registering this module in the standard resource loader. </p>
     *
     * <p>Does not work on Android platforms by design and is mainly intended for OSGi-environments. </p>
     *
     * @see     ResourceLoader#registerService(Class, Object)
     */
    /*[deutsch]
     * <p>Bequeme Kurzform f&uuml;r die Registrierung dieses Moduls im Standard-{@code ResourceLoader}. </p>
     *
     * <p>Funktioniert nicht auf Android-Plattformen und ist haupts&auml;chlich f&uuml;r OSGi-Umgebungen
     * gedacht. </p>
     *
     * @see     ResourceLoader#registerService(Class, Object)
     */
    public static void init() {
        TimezoneRepositoryProviderSPI spi = new TimezoneRepositoryProviderSPI();
        ResourceLoader.getInstance().registerService(ZoneModelProvider.class, spi);
        ResourceLoader.getInstance().registerService(ZoneNameProvider.class, spi);
        ResourceLoader.getInstance().registerService(LeapSecondProvider.class, spi);
    }

}
