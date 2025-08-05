CREATE TABLE donor (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL
);

CREATE TABLE beneficiary (
                             id SERIAL PRIMARY KEY,
                             email VARCHAR(255) NOT NULL,
                             full_name VARCHAR(255) NOT NULL
);

CREATE TABLE payment (
                         id SERIAL PRIMARY KEY,
                         date TIMESTAMP WITH TIME ZONE NOT NULL,
                         amount NUMERIC(10,2) NOT NULL,
                         method VARCHAR(50) NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         external_id VARCHAR(255)
);

CREATE TABLE donation (
                          id SERIAL PRIMARY KEY,
                          donor_id INT REFERENCES donor(id),
                          payment_id INT REFERENCES payment(id)
);

CREATE TABLE help (
                      id SERIAL PRIMARY KEY,
                      beneficiary_id INT REFERENCES beneficiary(id),
                      payment_id INT REFERENCES payment(id),
                      description TEXT
);
