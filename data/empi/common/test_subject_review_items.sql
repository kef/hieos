select * from subject_review_item

-- LEFT SIDE
SELECT * FROM subject_name, subject_identifier 
   where subject_identifier.subject_id=1825370 AND subject_name.subject_id=1825370
UNION
-- RIGHT SIDE
SELECT * FROM subject_name, subject_identifier 
   where subject_identifier.subject_id=1825364 AND subject_name.subject_id=1825364
   

SELECT subject_name.given_name, subject_name.family_name
  FROM subject_name  JOIN subject_review_item
  ON subject_review_item.subject_id_left=subject_name.subject_id;