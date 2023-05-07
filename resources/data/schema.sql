CREATE TABLE IF NOT EXISTS korisnik (
id_korisnika SERIAL,
facebook_user_id VARCHAR ( 30 ),
ime VARCHAR ( 30 ) NOT NULL,
prezime VARCHAR ( 50 ) NOT NULL,
phone_number VARCHAR ( 15 ),
email VARCHAR ( 40 ),
PRIMARY KEY(id_korisnika)
);

CREATE TABLE IF NOT EXISTS ad (
ad_id SERIAL,
id_korisnika INT,
created_on TIMESTAMP NOT NULL,
region VARCHAR ( 50 ) NOT NULL,
send_by_post BOOLEAN,
share_in_person BOOLEAN,
quantity INTEGER,
sharing_milk_type BOOLEAN,
sharing_water_type BOOLEAN,
sharing_kombucha BOOLEAN,
PRIMARY KEY(ad_id),
CONSTRAINT fk_korisnik
      FOREIGN KEY(id_korisnika)
	  REFERENCES korisnik(id_korisnika)
);
