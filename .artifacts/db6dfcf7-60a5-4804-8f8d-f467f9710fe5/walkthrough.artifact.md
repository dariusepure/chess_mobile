# Walkthrough - Comutator Dark/Light Mode

Am implementat un sistem complet de gestionare a temei vizuale, oferind utilizatorului control total asupra aspectului aplicației.

## Funcționalități Implementate

### 1. Control Manual al Temei
- **Selector în Meniu**: Am adăugat o secțiune nouă în Meniul Principal sub setările de joc.
- **Trei Opțiuni**:
    - **Auto**: Aplicația urmărește automat setările sistemului de operare (Dark/Light).
    - **Light**: Forțează aplicația pe tema deschisă, cu fundaluri luminoase și contrast ridicat.
    - **Dark**: Forțează tema premium întunecată (`#302E2B`), ideală pentru jocul pe timp de noapte.

### 2. Persistența Alegerii
- Tema selectată este acum salvată pe dispozitiv prin `UserManager`.
- La repornirea aplicației, Chess Mobile se va deschide direct cu tema preferată a utilizatorului.

### 3. Integrare Material 3
- Am actualizat wrapper-ul `ChessMobileTheme` pentru a injecta dinamic starea de temă selectată.
- Toate ecranele (Login, Friends, Game, Leaderboard) se adaptează instantaneu la schimbarea temei fără a necesita restartul aplicației.

## Detalii Tehnice
- Folosirea `mutableStateOf` în ViewModel pentru a asigura o actualizare reactivă a UI-ului.
- Stocarea preferinței ca întreg (0, 1, 2) în `SharedPreferences` pentru eficiență.
- Sincronizarea culorilor tablei de șah cu noul sistem de teme pentru a păstra vizibilitatea pieselor.

> [!TIP]
> Încearcă să comuți între Light și Dark în timp ce ești în Meniu pentru a vedea cât de fluid se schimbă întreaga interfață!

## Verificare
- Compilare și rulare cu succes (`assembleDebug`).
- Testarea persistenței prin închiderea forțată a aplicației după selectarea unei teme.
