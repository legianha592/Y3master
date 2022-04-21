alter table milestone drop column milestone_status;
ALTER TABLE milestone
   DROP INDEX UNQ_Milestone_1,
   ADD UNIQUE KEY `UNQ_Milestone_1` (`tenant_id`,`milestone_code`);