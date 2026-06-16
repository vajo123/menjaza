
---

## Faza 0 – Ideja i dogovor o strukturi

> **Prompt:** Treba mi projekat za faks. Ideja je aplikacija "Menjaza" – korisnici imaju sličice iz albuma (brojevi 1–99). Svako ima neke duplikate i neke koje mu fale. Aplikacija treba da spaja dva korisnika kojima se razmena isplati (ja njemu dam ono što njemu fali, on meni ono što meni fali). Kako da postavim strukturu? Treba server i više klijenata. Evo ti specifikacija projekta.

**Dogovoreno:**
- Arhitektura: jedan **server** + više **klijenata** preko soketa na portu `9000`.
- Komunikacija: slanje serijalizovanih objekata (`ObjectInputStream` / `ObjectOutputStream`).
- Paketi:
  - `model` – podaci koji putuju mrežom (`UserData`, `ExchangeInfo`, `ExchangeRequest`)
  - `server` – `ExchangeServer`, `ClientHandler`, `ClientRegistry`
  - `client` – `ClientFrame` (GUI), `StickerSelectionDialog`
  - `util` – `StickerGenerator`
- GUI: Swing (`JFrame`, paneli sa checkbox-ovima 1–99).

---

## Faza 1 – Model: korisnik i njegove sličice

> **Prompt:** Hajde prvo da napravimo klasu koja predstavlja korisnika. Treba da ima korisničko ime, skup sličica koje ima viška (duplikati) i skup onih koje mu fale. To će se slati preko mreže.

**Urađeno:** Napravljena klasa `UserData`.
- Polja: `username`, `Set<Integer> duplicates`, `Set<Integer> missing`.
- Korišćen `Set` da ne bi bilo ponavljanja brojeva.
- Klasa implementira `Serializable` jer mora da se šalje soketom.

> **Prompt:** Dodaj mi gettere i settere, i neka se setovi inicijalizuju u konstruktoru da ne pucaju `null`.

**Urađeno:** Dodati getteri/setteri, `duplicates` i `missing` inicijalizovani kao prazni `HashSet` u konstruktoru.

---

## Faza 2 – Generisanje sličica

> **Prompt:** Kad se korisnik prijavi, treba da dobije nasumično nekih 15 duplikata i 15 sličica koje mu fale. Bitno je da se duplikat i fali ne preklapaju – nema smisla da mi nešto i fali i da mi je višak.

**Urađeno:** Napravljena `util.StickerGenerator`:
- `generateDuplicates(count)` – nasumični brojevi 1–99 u `Set` dok ne dođe do `count`.
- `generateMissing(duplicates, count)` – nasumični brojevi, ali preskače sve što je već u `duplicates`.

> **Prompt:** Što koristiš `while` petlju a ne for? Hoću da budem siguran da ću dobiti tačno 15 različitih.

**Objašnjeno:** Pošto je `Set`, dodavanje već postojećeg broja ne povećava veličinu. `while (set.size() < count)` garantuje tačno `count` *različitih* vrednosti, dok bi `for` petlja sa 15 iteracija mogla dati manje od 15 zbog ponavljanja.

---

## Faza 3 – Server koji prihvata klijente

> **Prompt:** Sad server. Treba da za svakog klijenta koji se poveže pokrene zasebnu nit.

**Urađeno:** Napravljen `ExchangeServer`:
- `ServerSocket` na portu `9000`.
- Beskonačna petlja: `accept()` → novi `ClientHandler` → `new Thread(handler).start()`.
- Statički `ClientRegistry registry` dostupan svim handler-ima.

> **Prompt:** Napravi i klasu koja vodi evidenciju ko je sve povezan, da mogu kasnije da nađem nekog korisnika po imenu.

**Urađeno:** Napravljen `ClientRegistry`:
- `Map<String, ClientHandler>` (preko `Collections.synchronizedMap`) – ime → handler.
- Metode `addClient`, `removeClient`, `getClient`.

---

## Faza 4 – ClientHandler (nit po klijentu)

> **Prompt:** Napravi handler koji u petlji čita objekte od klijenta. Kad stigne `UserData`, neka ga registruje. Za sad samo to.

**Urađeno:** `ClientHandler implements Runnable`:
- U konstruktoru otvara `ObjectOutputStream` i `ObjectInputStream`.
- U `run()` petlja `in.readObject()`; ako je `UserData`, čuva ga i upisuje u registry.
- Dodata pomoćna metoda `sendObject(...)` za slanje nazad klijentu.

> **Prompt:** Kad se klijent diskonektuje (pukne veza), treba da se izbaci iz registra da ne ostaje "duh" korisnik.

**Urađeno:** U `catch` bloku `run()` metode poziva se `registry.removeClient(...)` ako `userData` nije `null`.

---

## Faza 5 – Klijent GUI (osnovni izgled)

> **Prompt:** Sad GUI. Treba polje za username, dugme "Poveži", i dva panela: levo "sličice koje imam", desno "sličice koje mi trebaju". Svaki panel ima checkbox-ove 1–99.

**Urađeno:** `ClientFrame extends JFrame`:
- Gornja traka: `usernameField`, dugme "Povezi".
- Centar: dva `JPanel`-a u `GridLayout(11, 9)` (99 polja), svaki u `JScrollPane`.
- `initializeCheckBoxes()` pravi 99 checkbox-ova za svaki panel i pamti ih u `HashMap<Integer, JCheckBox>`.

> **Prompt:** Kad kliknem "Poveži", neka se generišu sličice (15 + 15), neka se prikažu i neka se pošalju serveru.

**Urađeno:** `connectUser()`:
- Pravi `UserData`, generiše duplikate i fali preko `StickerGenerator`.
- Poziva `refreshCheckBoxStates()` da obeleži koje sličice korisnik ima/fale.
- Šalje `UserData` serveru.

> **Prompt:** Dodaj povezivanje sa serverom pri pokretanju klijenta (`localhost:9000`).

**Urađeno:** `initializeConnection()` otvara soket i tokove. Ako server nije pokrenut, prikazuje `JOptionPane` grešku.

---

## Faza 6 – Logika "moguće razmene"

> **Prompt:** Najbitniji deo: server treba da nađe kome se isplati razmena sa mnom. Uslov je da JA imam viška nešto što NJEMU fali, i da ON ima viška nešto što MENI fali. Treba mi klasa koja opisuje takvu razmenu.

**Urađeno:** Napravljena `model.ExchangeInfo` (`Serializable`):
- Polja: `otherUser`, `List<Integer> iGive`, `List<Integer> heGives`.
- `toString()` prikazuje ime i `min(iGive, heGives)` sličica (jer toliko realno može da se razmeni).

> **Prompt:** Dodaj u registry metodu koja za zadatog korisnika prolazi kroz sve ostale i vraća listu mogućih razmena.

**Urađeno:** `ClientRegistry.getPossibleExchanges(current)`:
- Prolazi kroz sve klijente osim samog sebe.
- `iGive` = moji duplikati koji su drugom u "fali".
- `heGives` = njegovi duplikati koji su meni u "fali".
- Ako su obe liste neprazne → dodaje `ExchangeInfo`.

> **Prompt:** Sad veza klijent-server: dugme "Moguće razmene" šalje zahtev serveru, server vrati listu, klijent je ubaci u combo box.

**Urađeno:**
- U `ClientHandler` dodato prepoznavanje `String "GET_EXCHANGES"` → vraća listu `ExchangeInfo`.
- `ClientFrame.loadExchanges()` šalje `"GET_EXCHANGES"`.
- Nit za slušanje (`startListening()`) prima listu i puni `exchangeCombo`.
- Dodato dugme "Moguće razmene", combo box i `detailsArea` za prikaz detalja izabrane razmene.

---

## Faza 7 – Slanje i prihvatanje zahteva za razmenu

> **Prompt:** Treba klasa za zahtev razmene – ko šalje, kome, koje sličice daje, koje traži. I dugme "Pošalji zahtev".

**Urađeno:** Napravljena `model.ExchangeRequest` (`fromUser`, `toUser`, `fromUserStickers`, `toUserStickers`).
- `ClientFrame.sendExchangeRequest()` pravi zahtev i šalje ga serveru.
- `ClientHandler` rutira zahtev ka `toUser`-u preko registra.
- Druga strana dobija `JOptionPane` sa pitanjem "Prihvataš?".

---

## Faza 8 – Testiranje i pronađeni bugovi

Posle prvog testiranja sa dva klijenta isplivali su problemi.

### Bug 1 – `ObjectOutputStream` keširanje *(kritičan)*

> **Prompt:** Čudno – obrišem neku sličicu kod sebe, pošaljem ažurirano serveru, ali server i dalje vidi stare podatke. Kao da se ne primaju izmene.

**Uzrok:** `ObjectOutputStream` interno kešira poslate objekte. Kad se isti `user` objekat pošalje ponovo, šalje se referenca na već keširanu (staru) verziju, ne nova vrednost.

**Popravka:** Dodat `out.reset()` pre **svakog** `out.writeObject(...)` poziva u `ClientFrame`.

---

### Bug 2 – Korisnik A nikad nije obavešten da je razmena prihvaćena *(kritičan)*

> **Prompt:** Kad B prihvati razmenu, kod B se sve ažurira, ali kod A se ništa ne dešava – A i dalje "ima" sličice koje je dao. Kako da A sazna da je razmena prošla?

**Uzrok:** Originalni tok je bio: B prihvati → B ažurira svoje sličice → kraj. A nikad nije dobio povratnu informaciju.

**Popravka:**
- U `ExchangeRequest` dodat `boolean accepted` (sa dva konstruktora – podrazumevano `false`).
- Kad B prihvati, šalje serveru `ExchangeRequest(accepted=true)`.
- `ClientHandler` proverava `isAccepted()` i rutira poruku nazad ka `fromUser`-u (A).
- A prima potvrdu, ažurira svoje sličice i sinhronizuje stanje sa serverom.

---

### Bug 3 – `StickerSelectionDialog` se otvarao za tuđe sličice *(srednji)*

> **Prompt:** Kad druga osoba ima više sličica nego što treba, otvara mi se dijalog da JA biram koje od NJENIH sličica da primim. To nema smisla – ja biram šta dajem, ne šta primam.

**Uzrok:** Dijalog se otvarao na osnovu `his.size() > count` (tuđe sličice) umesto `mine.size() > count` (moje sličice).

**Popravka:** Dijalog se otvara samo kada `mine.size() > count` – korisnik bira **koje svoje** sličice nudi. Za drugu stranu se uvek uzima prvih `count` sličica bez dijaloga.

---

### Bug 4 – `NullPointerException` i dupla prijava *(minor)*

> **Prompt:** Ako kliknem "Obriši" ili "Moguće razmene" pre nego što se povežem, aplikacija pukne. A ako kliknem "Poveži" dvaput, generišu mi se nove nasumične sličice i izgubim stare.

**Uzrok:**
- Event handler-i (`deleteSelected`, `loadExchanges`, `sendExchangeRequest`) koristili su `user` koji je još `null`.
- Drugi klik na "Poveži" pravio je novi `UserData`.

**Popravka:**
- Dodat `null` check za `user` u svim handler-ima sa porukom "Niste prijavljeni".
- Blokirana ponovna prijava uz poruku "Već ste prijavljeni kao …".
- Dodata poruka ako server nije pokrenut.

---

## Faza 9 – UX doterivanje

### Dugme "Obriši" briše sve odjednom

> **Prompt:** Zadatak kaže da korisnik **selektovanjem** sličica bira koje briše. Kod mene `refreshCheckBoxStates()` automatski čekira sve, pa klik na "Obriši" obriše baš sve. Kako da bude da korisnik ručno bira?

**Popravka:** Promenjen `refreshCheckBoxStates()`:
- Sličice koje korisnik ima/fale prikazuju se **bold plavom bojom**.
- Sličice van liste su sive.
- Checkbox uvek ostaje odčekiran, da korisnik može ručno da označi šta želi da obriše.

---

### Stari podaci u tekstualnom polju posle razmene

> **Prompt:** Posle uspešne razmene, u donjem tekstualnom polju i dalje piše stara lista sličica, uključujući i one koje sam upravo razmenio.

**Popravka:** Posle svake uspešne razmene (i kod A i kod B):
```java
exchangeCombo.removeAllItems();
detailsArea.setText("");
```

---

## Faza 10 – Pitanja i pojašnjenja

> **Pitanje:** Da li može da se menja više sličica odjednom? Imala je 4 za drugu korisnicu, a ona samo 1 – je li to uvek tako?

**Odgovor:** Broj razmenjenih sličica uvek je `min(moje, njene)`. Kod 4 naspram 1, menja se 1 za 1. Dijalog pita koju od 4 ponuditi. Za veću razmenu potrebno je više korisnika u sistemu ili "srećnija" nasumična generacija.

---

> **Pitanje:** Zašto posle prve razmene više ne vidim drugog korisnika kad kliknem "Moguće razmene"? Moram da restartujem aplikaciju.

**Odgovor:** To je očekivano. Posle razmene 1 za 1 (korisnik je imao 4, Ana 1):
- Korisnik i dalje ima 3 sličice koje Ani trebaju ✓
- Ali Ana više nema nijednu sličicu koja korisniku treba ✗

Drugi uslov za razmenu nije ispunjen → Ana se ne pojavljuje u listi. Nije bug.

---

## Rezime stanja na kraju

| Komponenta | Uloga |
|---|---|
| `Main` | Pokreće klijenta (`ClientFrame`) |
| `UserData` | Korisnik: ime, duplikati, fali (Serializable) |
| `StickerGenerator` | Nasumično generisanje duplikata i fali bez preklapanja |
| `ExchangeServer` | Sluša port 9000, pravi nit po klijentu |
| `ClientHandler` | Nit po klijentu: čita objekte, rutira zahteve |
| `ClientRegistry` | Evidencija povezanih korisnika + računanje mogućih razmena |
| `ExchangeInfo` | Opis jedne moguće razmene (ko, šta dajem, šta dobijam) |
| `ExchangeRequest` | Zahtev za razmenu + status `accepted` |
| `ClientFrame` | GUI: prijava, prikaz sličica, slanje/prihvatanje razmena |
| `StickerSelectionDialog` | Izbor kojih sličica korisnik nudi kad ima viška |

**Glavne ispravke:** `out.reset()` zbog keširanja, obaveštavanje pošiljaoca o prihvaćenoj razmeni, ispravan trigger dijaloga, null-check-ovi i blokada duple prijave, ručna selekcija za brisanje, čišćenje GUI-ja posle razmene.
