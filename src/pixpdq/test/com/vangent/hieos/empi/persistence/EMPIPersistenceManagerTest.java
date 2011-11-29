/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vangent.hieos.empi.persistence;

import com.vangent.hieos.empi.match.MatchResults;
import com.vangent.hieos.empi.match.FRILMatchAlgorithm;
import com.vangent.hieos.empi.match.Field;
import java.util.List;
import com.vangent.hieos.empi.match.Record;
import com.vangent.hieos.empi.match.RecordBuilder;
import com.vangent.hieos.hl7v3util.model.subject.Address;
import com.vangent.hieos.hl7v3util.model.subject.CodedValue;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.Connection;
import com.vangent.hieos.hl7v3util.model.subject.SubjectName;
import com.vangent.hieos.hl7v3util.model.subject.Subject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author thumbe
 */
public class EMPIPersistenceManagerTest {

    public EMPIPersistenceManagerTest() {
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
     * Test of store method, of class PersistenceManager.
     */
    @Test
    public void testStore() throws Exception {
        System.out.println("store");

        // Subject.
        Subject subject = new Subject();
        subject.setBirthTime(new Date());

        // Subject name(s).
        SubjectName subjectName = new SubjectName();
        subjectName.setGivenName("Bernie");
        subjectName.setFamilyName("Thuuman");
        subjectName.setPrefix("Mr");
        //subjectName.setSuffix("");
        //subjectName.setMiddleName("");
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

        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost/empi", "empi", "empi");
        PersistenceManager pm = new PersistenceManager();
        pm.setConnection(connection);
        pm.insertSubject(subject);

        RecordBuilder recordBuilder = new RecordBuilder();
        Record searchRecord = recordBuilder.build(subject);
        System.out.println("Search Record = " + searchRecord);
        pm.insertSubjectMatchRecord(searchRecord);

        List<Record> candidateRecords = pm.findCandidates(searchRecord);
        System.out.println("Candidate record count = " + candidateRecords.size());

        // Run full matching process.
        FRILMatchAlgorithm algo = new FRILMatchAlgorithm();
        algo.setPersistenceManager(pm);
        MatchResults matchResults = algo.findMatches(searchRecord);

        searchRecord = new Record();
        searchRecord.addField(new Field("givenNameDoubleMetaphone", "MR"));
        searchRecord.addField(new Field("familyNameDoubleMetaphone", "0MN"));
        searchRecord.addField(new Field("birthTime", "20111022"));
        searchRecord.addField(new Field("gender", "F"));
        candidateRecords = pm.findCandidates(searchRecord);
        System.out.println("Candidate record count = " + candidateRecords.size());
        connection.close();
    }
}
