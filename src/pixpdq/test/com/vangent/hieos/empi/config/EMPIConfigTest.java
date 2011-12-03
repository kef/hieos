/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.empi.config;

import com.vangent.hieos.empi.match.MatchAlgorithm;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import java.util.Date;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.hl7v3util.model.subject.Address;
import com.vangent.hieos.empi.match.ScoredRecord;
import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.Field;
import java.util.ArrayList;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.FRILMatchAlgorithm;
import com.vangent.hieos.empi.distance.LevenshteinDistanceFunction;
import com.vangent.hieos.empi.transform.SoundexTransformFunction;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thumbe
 */
public class EMPIConfigTest {

    public EMPIConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class EMPIConfig.
     */
    @Test
    public void testGetInstance() throws Exception {
        System.out.println("getInstance");
        EMPIConfig empiConfig = EMPIConfig.getInstance();
        assertNotNull(empiConfig);

        MatchConfig matchConfig = empiConfig.getMatchConfig();
        assertNotNull(matchConfig);
        //assertEquals(matchConfig.getAcceptThreshold(), 0.60, 0);
        assertTrue(matchConfig.getAcceptThreshold() >= matchConfig.getRejectThreshold());

        List<MatchFieldConfig> matchFieldConfigs = matchConfig.getMatchFieldConfigs();
        assertTrue(matchFieldConfigs.size() > 0);
        for (MatchFieldConfig matchFieldConfig : matchFieldConfigs) {
            System.out.println("match-field = " + matchFieldConfig.getName());
            System.out.println("  .. acceptThreshold = " + matchFieldConfig.getAcceptThreshold());
            System.out.println("  .. rejectThreshold = " + matchFieldConfig.getRejectThreshold());
            System.out.println("  .. weight = " + matchFieldConfig.getWeight());
            assertTrue(matchFieldConfig.getAcceptThreshold() >= matchFieldConfig.getRejectThreshold());
            MatchFieldConfig compareFieldConfig = matchConfig.getMatchFieldConfig(matchFieldConfig.getName());
            assertEquals(matchFieldConfig, compareFieldConfig);
            assertEquals(matchFieldConfig.getName(), compareFieldConfig.getName());
        }
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testSoundex() throws Exception {
        System.out.println("testSoundex");
        SoundexTransformFunction soundexTransformFunction = new SoundexTransformFunction();
        Object result = soundexTransformFunction.transform("Smith");
        System.out.println("result = " + result.toString());
        result = soundexTransformFunction.transform("Jones");
        System.out.println("result = " + result.toString());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testLevenshtein() throws Exception {
        System.out.println("testLevenshtein");
        LevenshteinDistanceFunction levenshteinDistanceFunction = new LevenshteinDistanceFunction();
        double result = levenshteinDistanceFunction.getDistance("Smith", "Smith");
        System.out.println("result = " + result);
        assertTrue(result == 1.0);
        result = levenshteinDistanceFunction.getDistance("Smith", "Smyth");
        assertTrue(result == 0.80);
        System.out.println("result = " + result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testFRILAlgorithm() throws Exception {
        System.out.println("testFRILAlgorithm");
        FRILMatchAlgorithm algo = new FRILMatchAlgorithm();
        Record searchRecord = new Record();
        searchRecord.addField(new Field("givenName", "Bernie"));
        searchRecord.addField(new Field("familyName", "ARON"));
        List<Record> records = new ArrayList<Record>();
        Record record1 = new Record();
        record1.addField(new Field("givenName", "Bernie"));
        record1.addField(new Field("familyName", "AARON"));
        records.add(record1);

        MatchResults matchResults = algo.findMatches(searchRecord, records, MatchAlgorithm.MatchType.NOMATCH_EMPTY_FIELDS);
        if (matchResults.getMatches().size() > 0) {
            System.out.println("MATCHES: ");
        }
        for (ScoredRecord scoredRecord : matchResults.getMatches()) {
            System.out.println("score = " + scoredRecord.getScore());
        }
        if (matchResults.getNonMatches().size() > 0) {
            System.out.println("NON MATCHES: ");
        }
        for (ScoredRecord scoredRecord : matchResults.getNonMatches()) {
            System.out.println("score = " + scoredRecord.getScore());
        }
        if (matchResults.getPossibleMatches().size() > 0) {
            System.out.println("POSSIBLE MATCHES: ");
        }
        for (ScoredRecord scoredRecord : matchResults.getPossibleMatches()) {
            System.out.println("score = " + scoredRecord.getScore());
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testRecordBuilder() throws Exception {
        // Subject.
        Subject subject = new Subject();
        subject.setBirthTime(new Date());

        // Subject name(s).
        SubjectName subjectName = new SubjectName();
        subjectName.setGivenName("Bernie");
        subjectName.setFamilyName("Thuman");
        //subjectName.setPrefix("Mr");
        subjectName.setSuffix("III");
        subjectName.setMiddleName("Henry");
        subject.getSubjectNames().add(subjectName);

        // Subject address(es).
        Address subjectAddress = new Address();
        subjectAddress.setStreetAddressLine1("999 Mockingbird Lane");
        subjectAddress.setCity("Dallas");
        subjectAddress.setState("Texas");
        subjectAddress.setPostalCode("12345");
        subject.getAddresses().add(subjectAddress);

        // Gender.
        CodedValue gender = new CodedValue();
        gender.setCode("M");
        subject.setGender(gender);

        RecordBuilder recordBuilder = new RecordBuilder();
        Record record = recordBuilder.build(subject);
        System.out.println("Record = " + record);
    }
}
