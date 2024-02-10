package net.time4j.tz.repo;

import net.time4j.Moment;
import net.time4j.PlainDate;
import net.time4j.PlainTimestamp;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.Iso8601Format;
import net.time4j.format.expert.PatternType;
import net.time4j.scale.LeapSecondProvider;
import net.time4j.tz.OffsetSign;
import net.time4j.tz.Timezone;
import net.time4j.tz.TransitionHistory;
import net.time4j.tz.ZonalOffset;
import net.time4j.tz.ZonalTransition;
import net.time4j.tz.ZoneModelProvider;
import net.time4j.tz.olson.EUROPE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(JUnit4.class)
public class RepositoryTest {

    static {
        TZDATA.init();
    }

    private static final String STD_VERSION = "2024a";
    private static final ChronoFormatter<Moment> PARSER = Iso8601Format.EXTENDED_DATE_TIME_OFFSET;

    private String propertyValue = null;

    @Before
    public void setUp() {
        String propertyKey = "net.time4j.tz.repository.version";
        this.propertyValue = System.getProperty(propertyKey);
        System.setProperty(propertyKey, STD_VERSION);
        System.setProperty("test.environment", "true");
    }

    @After
    public void tearDown() {
        String propertyKey = "net.time4j.tz.repository.version";
        if (this.propertyValue == null) {
            System.clearProperty(propertyKey);
        } else {
            System.setProperty(propertyKey, this.propertyValue);
        }
    }

    @Test
    public void alias() throws ParseException {
        ChronoFormatter<Moment> f =
            ChronoFormatter.ofMomentPattern(
                "uuuu-MM-dd HH:mm:ss VV", PatternType.CLDR, Locale.ROOT, ZonalOffset.UTC);
        Moment m = f.parse("2016-07-01 00:00:00 Asia/Calcutta");
        assertThat(
            m,
            is(PlainTimestamp.of(2016, 7, 1, 0, 0).at(ZonalOffset.ofHoursMinutes(OffsetSign.AHEAD_OF_UTC, 5, 30))));
    }

    @Test
    public void findRepositoryStdVersion() throws IOException {
        assertThat(Timezone.getVersion("TZDB"), is(STD_VERSION));
        Timezone.of("Pacific/Fiji").dump(System.out);
        Timezone.of("Asia/Jerusalem").dump(System.out);
        Timezone.of("Europe/Volgograd").dump(System.out);
    }

    @Test
    public void loadAll() {
        ZoneModelProvider repo = new TimezoneRepositoryProviderSPI();
        assertThat(repo.getVersion(), is(STD_VERSION));
        for (String tzid : repo.getAvailableIDs()) {
            assertThat(repo.load(tzid), notNullValue());
        }
    }

    @Test
    public void tzAsiaAden() throws IOException {
        String version = "2020a";
        use(version);
        ZoneModelProvider repo = new TimezoneRepositoryProviderSPI();
        assertThat(repo.getVersion(), is(version));
        System.out.println("Asia/Aden => " + version);
        assertThat(
            repo.getAliases().get("Asia/Aden"),
            is("Asia/Riyadh"));
        repo.load("Asia/Riyadh").dump(System.out);
    }

    @Test
    public void tzAfricaCairo() throws IOException {
        String version = "2016h";
        use(version);
        ZoneModelProvider repo = new TimezoneRepositoryProviderSPI();
        assertThat(repo.getVersion(), is(version));
        System.out.println("Africa/Cairo => " + version);
        repo.load("Africa/Cairo").dump(System.out);
    }

    @Test
    public void tzAfricaCasablanca2015a() throws ParseException {
        use("2015a"); // this version with ramadan modification
        String zoneID = "Africa/Casablanca";
        int start = 2015;
        int end = 2015;
        Object[][] data = {
            {"2015-03-29T02:00+00:00", 0, 1, 1},
            {"2015-06-13T03:00+01:00", 1, 0, 0},
            {"2015-07-18T02:00+00:00", 0, 1, 1},
            {"2015-10-25T03:00+01:00", 1, 0, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAfricaSaoTome2018i() throws ParseException {
        use("2018i");
        String zoneID = "Africa/Sao_Tome";
        int start = 2018;
        int end = 2019;
        Object[][] data = {
            {"2018-01-01T01:00+00:00", 0, 1, 0},
            {"2019-01-01T02:00+01:00", 1, 0, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAfricaSudan() throws ParseException {
        use("2017c"); // splitting of zones Africa/Khartoum and Africa/Juba
        String zoneID1 = "Africa/Khartoum";
        String zoneID2 = "Africa/Juba";
        int start = 2000;
        int end = 2017;
        Object[][] data1 = {
            {"2000-01-15T12:00+02:00", 2, 3, 0},
            {"2017-11-01T00:00+03:00", 3, 2, 0},
        };
        Object[][] data2 = {
            {"2000-01-15T12:00+02:00", 2, 3, 0},
        };
        checkTransitions(zoneID1, start, end, data1);
        checkTransitions(zoneID2, start, end, data2);
    }

    @Test
    public void tzAmericaNewYork() throws ParseException {
        String zoneID = "America/New_York";
        int start = 1940;
        int end = 1946;
        Object[][] data = {
            {"1940-04-28T02:00-05:00", -5, -4, 1},
            {"1940-09-29T02:00-04:00", -4, -5, 0},
            {"1941-04-27T02:00-05:00", -5, -4, 1},
            {"1941-09-28T02:00-04:00", -4, -5, 0},
            {"1942-02-09T02:00-05:00", -5, -4, 1},
            {"1945-09-30T02:00-04:00", -4, -5, 0},
            {"1946-04-28T02:00-05:00", -5, -4, 1},
            {"1946-09-29T02:00-04:00", -4, -5, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaLosAngeles() throws ParseException {
        String zoneID = "America/Los_Angeles";
        int start = 2018; // future test of last rules
        int end = 2018;
        Object[][] data = {
            {"2018-03-11T02:00-08:00", -8, -7, 1},
            {"2018-11-04T02:00-07:00", -7, -8, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaKentuckyLouisville() throws ParseException {
        use("2019c");
        String zoneID = "America/Kentucky/Louisville";
        int start = 1942;
        int end = 1950;
        Object[][] data = {
            {"1942-02-09T02:00-06:00", -6, -5, 1},
            {"1945-09-30T02:00-05:00", -5, -6, 0},
            {"1946-04-28T00:01-06:00", -6, -5, 1},
            {"1946-06-02T02:00-05:00", -5, -6, 0},
            {"1950-04-30T02:00-06:00", -6, -5, 1},
            {"1950-09-24T02:00-05:00", -5, -6, 0}
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaAnchorage() throws ParseException {
        String zoneID = "America/Anchorage";
        int start = 1982;
        int end = 1984;
        Object[][] data = {
            {"1982-04-25T02:00-10:00", -10, -9, 1},
            {"1982-10-31T02:00-09:00", -9, -10, 0},
            {"1983-04-24T02:00-10:00", -10, -9, 1},
            {"1983-10-30T02:00-09:00", -9, -9, 0},
            {"1984-04-29T02:00-09:00", -9, -8, 1},
            {"1984-10-28T02:00-08:00", -8, -9, 0}
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaJuneauNoLMT() throws ParseException {
        use("2015a"); // without LMT
        String zoneID = "America/Juneau";
        int start = 1850; // test of LMT-filter
        int end = 1942;
        Object[][] data = {
            {"1942-02-09T02:00-08:00", -8, -7, 1}
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaJuneauWithLMT() throws ParseException {
        use("_lmt");
        String zoneID = "America/Juneau";
        int start = 1850;
        int end = 1942;
        ZoneModelProvider repo = new TimezoneRepositoryProviderSPI();
        TransitionHistory history = repo.load(zoneID);
        assertThat(history, notNullValue());
        List<ZonalTransition> transitions =
            history.getTransitions(
                atStartOfYear(start),
                atStartOfYear(end + 1));
        int n = transitions.size();
        assertThat(n, is(3));
        for (int i = 0; i < n; i++) {
            ZonalTransition zt = transitions.get(i);
            switch (i) {
                case 0:
                    assertThat(
                        zt.getPosixTime(),
                        is(parse("1867-10-18T00:00+15:02:19")));
                    assertThat(
                        zt.getPreviousOffset(),
                        is(15 * 3600 + 2 * 60 + 19));
                    assertThat(
                        zt.getTotalOffset(),
                        is(-8 * 3600 - 57 * 60 - 41));
                    assertThat(
                        zt.getDaylightSavingOffset(),
                        is(0));
                    break;
                case 1:
                    assertThat(
                        zt.getPosixTime(),
                        is(parse("1900-08-20T12:00-8:57:41")));
                    assertThat(
                        zt.getPreviousOffset(),
                        is(-8 * 3600 - 57 * 60 - 41));
                    assertThat(
                        zt.getTotalOffset(),
                        is(-8 * 3600));
                    assertThat(
                        zt.getDaylightSavingOffset(),
                        is(0));
                    break;
                case 2:
                    assertThat(
                        zt.getPosixTime(),
                        is(parse("1942-02-09T02:00-08:00")));
                    assertThat(
                        zt.getPreviousOffset(),
                        is(-8 * 3600));
                    assertThat(
                        zt.getTotalOffset(),
                        is(-7 * 3600));
                    assertThat(
                        zt.getDaylightSavingOffset(),
                        is(3600));
            }
        }
    }

    @Test
    public void tzAmericaAsuncion() throws ParseException {
        String zoneID = "America/Asuncion";
        int start = 1974;
        int end = 1975;
        Object[][] data = {
            {"1974-04-01T00:00-03:00", -3, -4, 0},
            {"1975-10-01T00:00-04:00", -4, -3, 1},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaSantoDomingo() throws ParseException {
        String zoneID = "America/Santo_Domingo";
        int start = 2000;
        int end = 2004;
        Object[][] data = {
            {"2000-10-29T02:00-04:00", -4, -5, 0},
            {"2000-12-03T01:00-05:00", -5, -4, 0}
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaSaoPaulo() throws ParseException {
        String zoneID = "America/Sao_Paulo";
        int start = 1962;
        int end = 1965;
        Object[][] data = {
            {"1963-10-23T00:00-03:00", -3, -2, 1},
            {"1964-03-01T00:00-02:00", -2, -3, 0},
            {"1965-01-31T00:00-03:00", -3, -2, 1},
            {"1965-03-31T00:00-02:00", -2, -3, 0},
            {"1965-12-01T00:00-03:00", -3, -2, 1},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAmericaArgentinaJujuy() throws ParseException {
        String zoneID = "America/Argentina/Jujuy";
        int start = 1991;
        int end = 1995;
        Object[][] data = {
            {"1991-03-17T00:00-03:00", -3, -4, 0},
            {"1991-10-06T00:00-04:00", -4, -2, 1},
            {"1992-03-01T00:00-02:00", -2, -3, 0},
            {"1992-10-18T00:00-03:00", -3, -2, 1},
            {"1993-03-07T00:00-02:00", -2, -3, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzEuropeLondon() throws ParseException {
        String zoneID = "Europe/London";
        int start = 1967;
        int end = 1972;
        Object[][] data = {
            {"1967-03-19T02:00+00:00", 0, 1, 1},
            {"1967-10-29T03:00+01:00", 1, 0, 0},
            {"1968-02-18T02:00+00:00", 0, 1, 1},
            {"1968-10-27T00:00+01:00", 1, 1, 0},
            {"1971-10-31T03:00+01:00", 1, 0, 0},
            {"1972-03-19T02:00+00:00", 0, 1, 1},
            {"1972-10-29T03:00+01:00", 1, 0, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

// old data now only available in backzone which is not involved in tz-repo
//    @Test
//    public void tzEuropeOslo() throws ParseException {
//        String zoneID = "Europe/Oslo";
//        int start = 1940;
//        int end = 1942;
//        Object[][] data = {
//            {"1940-08-10T23:00+01:00", 1, 2, 1},
//            {"1942-11-02T03:00+02:00", 2, 1, 0},
//        };
//        checkTransitions(zoneID, start, end, data);
//    }

    @Test
    public void tzEuropeBrussels() throws ParseException {
        String zoneID = "Europe/Brussels";
        int start = 1940;
        int end = 1945;
        Object[][] data = {
            {"1940-02-25T02:00+00:00", 0, 1, 1},
            {"1940-05-20T03:00+01:00", 1, 2, 1},
            {"1942-11-02T03:00+02:00", 2, 1, 0},
            {"1943-03-29T02:00+01:00", 1, 2, 1},
            {"1943-10-04T03:00+02:00", 2, 1, 0},
            {"1944-04-03T02:00+01:00", 1, 2, 1},
            {"1944-09-17T03:00+02:00", 2, 1, 0},
            {"1945-04-02T02:00+01:00", 1, 2, 1},
            {"1945-09-16T03:00+02:00", 2, 1, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzEuropeBerlin() throws ParseException {
        String zoneID = "Europe/Berlin";
        int start = 1937;
        int end = 1981;
        Object[][] data = {
            {"1940-04-01T02:00+01:00", 1, 2, 1},
            {"1942-11-02T03:00+02:00", 2, 1, 0},
            {"1943-03-29T02:00+01:00", 1, 2, 1},
            {"1943-10-04T03:00+02:00", 2, 1, 0},
            {"1944-04-03T02:00+01:00", 1, 2, 1},
            {"1944-10-02T03:00+02:00", 2, 1, 0},
            {"1945-04-02T02:00+01:00", 1, 2, 1},
            {"1945-05-24T02:00+02:00", 2, 3, 2},
            {"1945-09-24T03:00+03:00", 3, 2, 1},
            {"1945-11-18T03:00+02:00", 2, 1, 0},
            {"1946-04-14T02:00+01:00", 1, 2, 1},
            {"1946-10-07T03:00+02:00", 2, 1, 0},
            {"1947-04-06T03:00+01:00", 1, 2, 1},
            {"1947-05-11T03:00+02:00", 2, 3, 2},
            {"1947-06-29T03:00+03:00", 3, 2, 1},
            {"1947-10-05T03:00+02:00", 2, 1, 0},
            {"1948-04-18T02:00+01:00", 1, 2, 1},
            {"1948-10-03T03:00+02:00", 2, 1, 0},
            {"1949-04-10T02:00+01:00", 1, 2, 1},
            {"1949-10-02T03:00+02:00", 2, 1, 0},
            {"1980-04-06T02:00+01:00", 1, 2, 1},
            {"1980-09-28T03:00+02:00", 2, 1, 0},
            {"1981-03-29T02:00+01:00", 1, 2, 1},
            {"1981-09-27T03:00+02:00", 2, 1, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzEuropeBucharest() throws ParseException {
        String zoneID = "Europe/Bucharest";
        int start = 1977;
        int end = 1994;
        Object[][] data = {
            {"1979-05-27T00:00+02:00", 2, 3, 1},
            {"1979-09-30T00:00+03:00", 3, 2, 0},
            {"1980-04-05T23:00+02:00", 2, 3, 1},
            {"1980-09-28T01:00+03:00", 3, 2, 0},
            {"1981-03-29T02:00+02:00", 2, 3, 1},
            {"1981-09-27T03:00+03:00", 3, 2, 0},
            {"1982-03-28T02:00+02:00", 2, 3, 1},
            {"1982-09-26T03:00+03:00", 3, 2, 0},
            {"1983-03-27T02:00+02:00", 2, 3, 1},
            {"1983-09-25T03:00+03:00", 3, 2, 0},
            {"1984-03-25T02:00+02:00", 2, 3, 1},
            {"1984-09-30T03:00+03:00", 3, 2, 0},
            {"1985-03-31T02:00+02:00", 2, 3, 1},
            {"1985-09-29T03:00+03:00", 3, 2, 0},
            {"1986-03-30T02:00+02:00", 2, 3, 1},
            {"1986-09-28T03:00+03:00", 3, 2, 0},
            {"1987-03-29T02:00+02:00", 2, 3, 1},
            {"1987-09-27T03:00+03:00", 3, 2, 0},
            {"1988-03-27T02:00+02:00", 2, 3, 1},
            {"1988-09-25T03:00+03:00", 3, 2, 0},
            {"1989-03-26T02:00+02:00", 2, 3, 1},
            {"1989-09-24T03:00+03:00", 3, 2, 0},
            {"1990-03-25T02:00+02:00", 2, 3, 1},
            {"1990-09-30T03:00+03:00", 3, 2, 0},
            {"1991-03-31T00:00+02:00", 2, 3, 1},
            {"1991-09-29T01:00+03:00", 3, 2, 0},
            {"1992-03-29T00:00+02:00", 2, 3, 1},
            {"1992-09-27T01:00+03:00", 3, 2, 0},
            {"1993-03-28T00:00+02:00", 2, 3, 1},
            {"1993-09-26T01:00+03:00", 3, 2, 0},
            {"1994-03-27T00:00+02:00", 2, 3, 1},
            {"1994-09-25T00:00+03:00", 3, 2, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzEuropeTallinn() throws ParseException {
        String zoneID = "Europe/Tallinn";
        int start = 1999;
        int end = 2002;
        Object[][] data = {
            {"1999-03-28T03:00+02:00", 2, 3, 1},
            {"1999-10-31T04:00+03:00", 3, 2, 0},
            {"2002-03-31T03:00+02:00", 2, 3, 1},
            {"2002-10-27T04:00+03:00", 3, 2, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAustraliaBrokenHill() throws ParseException {
        String zoneID = "Australia/Broken_Hill";
        int start = 1999;
        int end = 2001;
        int winter = 9 * 60 + 30;
        int summer = 10 * 60 + 30;
        Object[][] data = {
            {"1999-03-28T03:00+10:30", summer, winter, 0},
            {"1999-10-31T02:00+09:30", winter, summer, 60},
            {"2000-03-26T03:00+10:30", summer, winter, 0},
            {"2000-10-29T02:00+09:30", winter, summer, 60},
            {"2001-03-25T03:00+10:30", summer, winter, 0},
            {"2001-10-28T02:00+09:30", winter, summer, 60},
        };
        checkTransitions(zoneID, start, end, data, true);
    }

    @Test
    public void tzPacificApia() throws ParseException {
        use("2015a"); // this version with dateline change
        String zoneID = "Pacific/Apia";
        int start = 2000;
        int end = 2013;
        Object[][] data = {
            {"2010-09-26T00:00-11:00", -11, -10, 1},
            {"2011-04-02T04:00-10:00", -10, -11, 0},
            {"2011-09-24T03:00-11:00", -11, -10, 1},
            {"2011-12-29T24:00-10:00", -10, 14, 1},
            {"2012-04-01T04:00+14:00", 14, 13, 0},
            {"2012-09-30T03:00+13:00", 13, 14, 1},
            {"2013-04-07T04:00+14:00", 14, 13, 0},
            {"2013-09-29T03:00+13:00", 13, 14, 1},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzPacificEaster() throws ParseException {
        String zoneID = "Pacific/Easter";
        int start = 1981;
        int end = 1983;
        Object[][] data = {
            {"1981-03-15T03:00Z", -6, -7, 0},
            {"1981-10-11T04:00Z", -7, -6, 1},
            {"1982-03-14T03:00Z", -6, -6, 0},
            {"1982-10-10T04:00Z", -6, -5, 1},
            {"1983-03-13T03:00Z", -5, -6, 0},
            {"1983-10-09T04:00Z", -6, -5, 1},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAtlanticStanley() throws ParseException {
        String zoneID = "Atlantic/Stanley";
        int start = 1980;
        int end = 1986;
        Object[][] data = {
            {"1983-05-01T00:00-04:00", -4, -3, 0},
            {"1983-09-25T00:00-03:00", -3, -2, 1},
            {"1984-04-29T00:00-02:00", -2, -3, 0},
            {"1984-09-16T00:00-03:00", -3, -2, 1},
            {"1985-04-28T00:00-02:00", -2, -3, 0},
            {"1985-09-15T00:00-03:00", -3, -3, 1},
            {"1986-04-20T00:00-03:00", -3, -4, 0},
            {"1986-09-14T00:00-04:00", -4, -3, 1},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAsiaGaza() throws ParseException {
        String zoneID = "Asia/Gaza";
        int start = 1967;
        int end = 1977;
        Object[][] data = {
            {"1967-05-01T01:00+02:00", 2, 3, 1},
            {"1967-06-05T00:00+03:00", 3, 2, 0},
            {"1974-07-07T00:00+02:00", 2, 3, 1},
            {"1974-10-13T00:00+03:00", 3, 2, 0},
            {"1975-04-20T00:00+02:00", 2, 3, 1},
            {"1975-08-31T00:00+03:00", 3, 2, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAsiaDhaka() throws ParseException {
        use("2015a"); // this version with T24:00
        String zoneID = "Asia/Dhaka";
        int start = 2009;
        int end = 2011;
        Object[][] data = {
            {"2009-06-19T23:00+06:00", 6, 7, 1},
            {"2009-12-31T24:00+07:00", 7, 6, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAsiaKolkata() throws ParseException {
        String zoneID = "Asia/Kolkata";
        int start = 1942;
        int end = 2000;
        int burma = 6 * 60 + 30;
        int india = 5 * 60 + 30;
        int indiaDST = 6 * 60 + 30;
        Object[][] data = {
            {"1942-05-15T00:00+06:30", burma, india, 0},
            {"1942-09-01T00:00+05:30", india, indiaDST, 60},
            {"1945-10-15T00:00+06:30", indiaDST, india, 0},
        };
        checkTransitions(zoneID, start, end, data, true);
    }

    @Test
    public void tzEuropeIstanbul() throws IOException {
        Moment moment = PlainTimestamp.of(2016, 9, 6, 21, 0, 1).atUTC();
        ZonalTransition zt =
            Timezone.of(EUROPE.ISTANBUL).getHistory().findStartTransition(
            moment
        ).get();
        assertThat(zt.getPosixTime(), is(moment.getPosixTime() - 1));
        assertThat(zt.getPreviousOffset(), is(10800));
        assertThat(zt.getTotalOffset(), is(10800));
        assertThat(zt.getDaylightSavingOffset(), is(0));
        Timezone.of(EUROPE.ISTANBUL).dump(System.out);
    }

    @Test
    public void tzEuropeSaratov() {
        Moment moment = PlainTimestamp.of(2016, 12, 3, 23, 0, 1).atUTC();
        ZonalTransition zt =
            Timezone.of("Europe/Saratov").getHistory().findStartTransition(
                moment
            ).get();
        assertThat(zt.getPosixTime(), is(moment.getPosixTime() - 1));
        assertThat(zt.getPreviousOffset(), is(10800));
        assertThat(zt.getTotalOffset(), is(14400));
        assertThat(zt.getDaylightSavingOffset(), is(0));
    }

    @Test
    public void tzEuropeDublin() throws IOException {
        Timezone tz = Timezone.of("Europe/Dublin");

        Moment winter = PlainTimestamp.of(2018, 1, 16, 0, 0).atUTC();
        assertThat(tz.getStandardOffset(winter).getIntegralAmount(), is(3600));
        assertThat(tz.getDaylightSavingOffset(winter).getIntegralAmount(), is(-3600));
        assertThat(tz.getOffset(winter).getIntegralAmount(), is(0));
        assertThat(tz.isDaylightSaving(winter), is(false));

        Moment summer = PlainTimestamp.of(2018, 7, 16, 0, 0).atUTC();
        assertThat(tz.getStandardOffset(summer).getIntegralAmount(), is(3600));
        assertThat(tz.getDaylightSavingOffset(summer).getIntegralAmount(), is(0));
        assertThat(tz.getOffset(summer).getIntegralAmount(), is(3600));
        assertThat(tz.isDaylightSaving(summer), is(true));

        Moment m1970 = Moment.UNIX_EPOCH;
        assertThat(tz.getStandardOffset(m1970).getIntegralAmount(), is(3600));
        assertThat(tz.getDaylightSavingOffset(m1970).getIntegralAmount(), is(0));
        assertThat(tz.getOffset(m1970).getIntegralAmount(), is(3600));
        assertThat(tz.isDaylightSaving(m1970), is(true));

        tz.dump(System.out);
    }
    
    @Test
    public void tzEuropeKiev() {
        Timezone tz1 = Timezone.of("Europe/Kiev");
        Timezone tz2 = Timezone.of("Europe/Kyiv");
        assertThat(tz1.getHistory(), is(tz2.getHistory()));
    }

    @Test
    public void leapSecondAtEndOf2016() {
        use("2018f");
        LeapSecondProvider repo = new TimezoneRepositoryProviderSPI();
        assertThat(
            repo.getLeapSecondTable().size(),
            is(27));
        assertThat(
            repo.getDateOfExpiration().toString(),
            is("2019-06-28"));
        assertThat(
            repo.getLeapSecondTable().get(PlainDate.of(2016, 12, 31)).intValue(),
            is(1));
    }

    @Test
    public void tzAsiaYangon() throws IOException {
        Timezone.of("Asia/Yangon").dump(System.out); // fine in tzdb 2018b
    }

    @Test
    public void tzEuropePrague() throws ParseException {
        use("2018e");
        String zoneID = "Europe/Prague";
        int start = 1946;
        int end = 1947;
        Object[][] data = {
            {"1946-05-06T01:00:00Z", 1, 2, 1},
            {"1946-10-06T01:00:00Z", 2, 1, 0},
            {"1946-12-01T02:00:00Z", 1, 0, -1},
            {"1947-04-20T01:00:00Z", 0, 2, 1},
            {"1947-10-05T01:00:00Z", 2, 1, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void tzAsiaTokyo() throws ParseException {
        use("2018g");
        String zoneID = "Asia/Tokyo";
        int start = 1950;
        int end = 1950;
        Object[][] data = {
            {"1950-05-06T15:00:00Z", 9, 10, 1},
            {"1950-09-09T15:00:00Z", 10, 9, 0},
        };
        checkTransitions(zoneID, start, end, data);
    }

    @Test
    public void normalize() {
        assertThat(
            Timezone.normalize("Asia/Calcutta").canonical(),
            is("Asia/Kolkata"));
        assertThat(
            Timezone.normalize("Asia/Kolkata").canonical(),
            is("Asia/Kolkata"));
        assertThat(
            Timezone.normalize("Asia/Rangoon").canonical(),
            is("Asia/Yangon"));
        assertThat(
            Timezone.normalize("Asia/Yangon").canonical(),
            is("Asia/Yangon"));
        assertThat(
            Timezone.normalize("Asia/Tel_Aviv").canonical(),
            is("Asia/Jerusalem"));
        assertThat(
            Timezone.normalize("Asia/Jerusalem").canonical(),
            is("Asia/Jerusalem"));
        assertThat(
            Timezone.normalize("America/Mendoza").canonical(),
            is("America/Argentina/Mendoza"));
        assertThat(
            Timezone.normalize("America/Argentina/Mendoza").canonical(),
            is("America/Argentina/Mendoza"));
    }

    private static void checkTransitions(
        String zoneID,
        int start,
        int end,
        Object[][] data
    ) throws ParseException {
        checkTransitions(zoneID, start, end, data, false);
    }

    private static void checkTransitions(
        String zoneID,
        int start,
        int end,
        Object[][] data,
        boolean minutes
    ) throws ParseException {
        ZoneModelProvider repo = new TimezoneRepositoryProviderSPI();
//        try {
//            repo.load(zoneID).dump(System.out);
//        } catch (IOException ex) {
//            // cannot happen
//        }
        TransitionHistory history = repo.load(zoneID);
        assertThat(history, notNullValue());
        List<ZonalTransition> transitions =
            history.getTransitions(
                atStartOfYear(start),
                atStartOfYear(end + 1));
        int n = transitions.size();
        assertThat(n, is(data.length));
        for (int i = 0; i < n; i++) {
            ZonalTransition zt = transitions.get(i);
            Object[] values = data[i];
            String time = (String) values[0];
            String reason = zoneID + " => index=" + i + ", time=" + time;
            assertThat(
                reason,
                zt.getPosixTime(),
                is(PARSER.parse(time).getPosixTime()));
            assertThat(
                reason,
                zt.getPreviousOffset(),
                is(((Integer) values[1]) * (minutes ? 60 : 3600)));
            assertThat(
                reason,
                zt.getTotalOffset(),
                is(((Integer) values[2]) * (minutes ? 60 : 3600)));
            assertThat(
                reason,
                zt.getDaylightSavingOffset(),
                is(((Integer) values[3]) * (minutes ? 60 : 3600)));
        }
    }

    private static Moment atStartOfYear(int year) {
        return PlainTimestamp.of(year, 1, 1, 0, 0).atUTC();
    }

    private static long parse(String time) throws ParseException {
        ChronoFormatter<Moment> p =
            ChronoFormatter.setUp(Moment.class, Locale.ROOT).addPattern(
                "uuuu-MM-dd'T'HH:mmXXXXX",
                PatternType.CLDR
            ).build();
        return p.parse(time).getPosixTime();
    }

    private static void use(String version) {
        String propertyKey = "net.time4j.tz.repository.version";
        System.setProperty(propertyKey, version);
    }
}