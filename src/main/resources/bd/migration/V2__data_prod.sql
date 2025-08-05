INSERT INTO donor(email, full_name) VALUES ('carol.prod@example.com','Carol Prod');
INSERT INTO donor(email, full_name) VALUES ('dan.prod@example.com','Dan Prod');

INSERT INTO beneficiary(email, full_name) VALUES ('ben.prod@example.com','Ben Prod');

INSERT INTO payment(date, amount, method, status, external_id)
VALUES (now()-interval '3 day', 10.00, 'MOBILE', 'SUCCEEDED', 'prod-ex-1');

INSERT INTO donation(donor_id, payment_id) VALUES (1,1);

INSERT INTO payment(date, amount, method, status, external_id)
VALUES (now()-interval '4 day', 200.00, 'CARD', 'SUCCEEDED', 'prod-ex-2');

INSERT INTO help(beneficiary_id, payment_id, description) VALUES (1,2,'Aide prod pour accident');
