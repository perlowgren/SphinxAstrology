
INSERT INTO Database VALUES (1,0,0,0,0,0);

INSERT INTO User VALUES (0,NULL,NULL,NULL,NULL,NULL,NULL,0,0,0);
INSERT INTO User VALUES (1,4294967296,'admin@spirangle.net','Spirangle Admin','Spirangle Admin','en','',4096,0,0);
INSERT INTO User VALUES (2,4294967297,'support@spirangle.net','Spirangle Support','Spirangle Support','en','',4096,0,0);
INSERT INTO User VALUES (3,4294967298,'info@spirangle.net','Spirangle Info','Spirangle Info','en','',4096,0,0);
INSERT INTO User VALUES (4,4294967299,'per.lowgren@spirangle.net','Per Löwgren','Per Löwgren','en','',4096,0,0);

UPDATE Database SET created=strftime('%s','now');
UPDATE User SET created=strftime('%s','now');



