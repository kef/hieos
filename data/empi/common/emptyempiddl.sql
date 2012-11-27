delete from subject_address;
delete from subject_telecom_address;
delete from subject_name;
--delete from subject_match_field;
delete from subject_match_fields;
delete from subject_xref;
delete from subject_identifier;
--delete from subject_other_identifier;
delete from subject_personal_relationship;
delete from subject_language;
delete from subject_citizenship;
delete from resource_lock;
delete from subject_review;
delete from subject;


-- clear stats
select pg_stat_reset();

-- generally do not remove the following table(s)
delete from subject_identifier_domain;


