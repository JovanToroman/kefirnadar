# Kefir na dar - veb aplikacija za razmenu kefirnih zrnaca

Ovo je veb aplikacija za razmenu kefirnih zrnaca.

## Pokretanje

Ovde opisujemo korake za pokretanje aplikacije za potrebe razvoja u lokalnom okruženju.

### Frontend

Frontend aplikacije pokrećemo komandom `shadow-cljs watch dev`. Pre toga je neophodno da instaliramo shadow-cljs alat
korišćenjem komande `npm install shadow-cljs`. Ukoliko imamo problema sa npm paketima koji fale, možemo ih sve
instalirati komandom `npm install` pokrenutom iz korena projekta.

### Bekend

Pre pokretanja servera, moramo pokrenuti dev-local Datomic bazu podataka. Neophodno je da preuzmemo i podesimo
dev-local. Uputstva možemo naći [ovde](https://docs.datomic.com/cloud/dev-local.html). Nakon toga možemo pokrenuti
aplikaciju.

```bash
bin/run -m datomic.peer-server -h localhost -p 8998 -a kefirnadar,kefirnadar -d kefirnadar,datomic:mem://kefirnadar
```

Nakon toga možemo pokrenuti server.

Bekend je napisan u Clojuru, sa korišćenjem deps.edn fajla za upravljanje zavisnostima.
Server pokrećemo komandom `clj A:local`.

Kada smo pokrenuli frontend, bazu i server, trebalo bi da možemo da pristupimo aplikaciji na adresi
http://localhost:8060.