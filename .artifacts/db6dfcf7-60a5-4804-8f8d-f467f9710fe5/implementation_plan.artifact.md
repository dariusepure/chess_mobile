# Plan: Login Screen Modernizat și Profesional

Acest plan detaliază actualizarea ecranului de Login inspirat de proiectul `car_activity_log`, folosind cele mai bune practici de design Material 3 și un stil adaptat pentru un joc de șah premium.

## Modificări Propuse

### 1. UI Login Screen (`LoginScreen.kt`)
Vom reproiecta complet ecranul de login pentru a include:
- **Structură Profesională**: Utilizarea `Scaffold` și a unui `Column` centrat cu scroll.
- **Identitate Vizuală**: Adăugarea unui icon reprezentativ la început (ex: `Icons.Default.Extension` pentru a sugera piese/puzzle).
- **Tipografie Material 3**: Titluri mari (`displaySmall`) și texte de suport (`bodyLarge`) pentru o ierarhie vizuală clară.
- **Câmpuri de Text Îmbunătățite**: `OutlinedTextField` cu iconițe (Email, Lock), suport pentru tastatură specifică (Email, Password) și gestionarea stării prin `rememberSaveable`.
- **Feedback pentru Erori**: Implementarea unui `ErrorBanner` stilizat (fundal roșu șters, text clar) care apare deasupra formularului.
- **Butoane Moderne**:
    - Buton principal de Login cu stare de încărcare (`CircularProgressIndicator`).
    - Buton pentru "Guest Mode" (Continue Offline).
    - Link pentru comutarea între Login și Register.
- **Temă Întunecată**: Păstrarea fundalului `#302E2B` pentru o experiență imersivă de tip "Dark Mode".

### 2. Integrare Navigare (`MainActivity.kt`)
- Actualizarea apelurilor către `LoginScreen` pentru a suporta noile callback-uri dacă este necesar.

### 3. Simulare Stare "Submitting" (`ChessViewModel.kt`)
- Adăugarea unei mici întârzieri la login pentru a arăta animația de încărcare, oferind o senzație de aplicație "reală".

## Plan de Verificare

### Testare Manuală
1.  **Vizual**: Verificarea alinierii elementelor și a contrastului pe fundalul închis.
2.  **Validare**: Încercarea de a intra fără date și verificarea apariției bannerului de eroare.
3.  **Animare**: Verificarea apariției spinner-ului de încărcare la apăsarea butonului de Login.
4.  **Navigare**: Verificarea trecerii corecte în Meniul Principal după succes.

## Open Questions
- Dorești să adăugăm și opțiunea de "Continue with Google" (doar ca design momentan) pentru a completa look-ul profesional?
