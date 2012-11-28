DELETE FROM subject_match_fields;
DELETE FROM subject_identifier;
-- generally do not remove the following table(s)
-- DELETE FROM subject_identifier_domain;
DELETE FROM subject_xref;
DELETE FROM subject_address;
DELETE FROM subject_citizenship;
DELETE FROM subject_name;
DELETE FROM subject_language;
DELETE FROM subject_telecom_address;
DELETE FROM subject_personal_relationship;
DELETE FROM subject_review_item;
DELETE FROM resource_lock;
DELETE FROM subject;

-- clear stats
select pg_stat_reset();



