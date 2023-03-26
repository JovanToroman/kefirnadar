CREATE TABLE IF NOT EXISTS ad (
ad_id SERIAL,
created_on TIMESTAMP NOT NULL,
first_name VARCHAR ( 30 ) NOT NULL,
last_name VARCHAR ( 50 ) NOT NULL,
region VARCHAR ( 50 ) NOT NULL,
send_by_post BOOLEAN,
share_in_person BOOLEAN,
quantity INTEGER,
phone_number VARCHAR ( 15 ),
email VARCHAR ( 40 ),
sharing_milk_type BOOLEAN,
sharing_water_type BOOLEAN,
sharing_kombucha BOOLEAN
);
