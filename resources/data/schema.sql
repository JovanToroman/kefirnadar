CREATE TABLE IF NOT EXISTS korisnik (
id_korisnika SERIAL,
facebook_user_id VARCHAR ( 30 ),
ime VARCHAR ( 30 ),
prezime VARCHAR ( 50 ),
korisnicko_ime VARCHAR ( 30 ),
aktivacioni_kod VARCHAR ( 36 ),
kod_za_resetovanje_lozinke VARCHAR ( 36 ),
aktiviran BOOLEAN,
phone_number VARCHAR ( 15 ),
email VARCHAR ( 40 ),
hes_lozinke VARCHAR ( 64 ),
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
broj_telefona VARCHAR ( 15 ),
imejl VARCHAR ( 40 ),
PRIMARY KEY(ad_id),
CONSTRAINT fk_korisnik
      FOREIGN KEY(id_korisnika)
	  REFERENCES korisnik(id_korisnika)
);

UPDATE public.ad
SET imejl = korisnik.email
FROM public.korisnik
WHERE public.korisnik.id_korisnika = ad.id_korisnika;