---
--- DDL to add status to the Patient Record and update existing data in the database
--- It sets the status for all existing patient records to Active "A"
--- This script can be used where data already exists in the database and needs to be kept 
---


ALTER TABLE patient 
  ADD status CHAR(1) NOT NULL DEFAULT 'A';
