INSERT INTO donor(email, full_name) VALUES ('alice.pre@example.com','Alice Pre');
INSERT INTO donor(email, full_name) VALUES ('bob.pre@example.com','Bob Pre');

INSERT INTO beneficiary(email, full_name) VALUES ('ben.pre@example.com','Ben Pre');

INSERT INTO payment(date, amount, method, status, external_id)
VALUES (now()-interval '1 day', 50.00, 'CARD', 'SUCCEEDED', 'pre-ex-1');

INSERT INTO donation(donor_id, payment_id) VALUES (1,1);

INSERT INTO payment(date, amount, method, status, external_id)
VALUES (now()-interval '2 days', 100.00, 'BANK', 'SUCCEEDED', 'pre-ex-2');

INSERT INTO help(beneficiary_id, payment_id, description) VALUES (1,2,'Aide preprod pour accident');
