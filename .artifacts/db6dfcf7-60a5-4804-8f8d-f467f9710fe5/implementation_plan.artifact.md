# Plan: Redenumire Entități Firestore (Naming Convention)

Acest plan detaliază redenumirea fișierelor și claselor care interacționează direct cu baza de date Cloud Firestore, folosind prefixul `Firestore` pentru o claritate sporită, conform modelului profesional din `car_activity_log`.

## Modificări Propuse

### 1. Modele de Date (Entities)
Vom redenumi clasele care reprezintă documentele stocate în Firestore:
- **[DELETE] [User.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/User.kt)**
- **[NEW] [FirestoreUser.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/FirestoreUser.kt)**: Redenumirea clasei `User` în `FirestoreUser`.
- **[MODIFY] [Game.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/Game.kt)**: Extragerea claselor `Match` și `GameStatus`.
- **[NEW] [FirestoreMatch.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/FirestoreMatch.kt)**: Conține noile clase `FirestoreMatch` și `FirestoreGameStatus`.

### 2. Repositories și Logica de Sincronizare
- **[DELETE] [GameRepository.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/GameRepository.kt)**
- **[NEW] [FirestoreGameRepository.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/FirestoreGameRepository.kt)**: Gestionarea partidelor salvate în Cloud sub noul nume.

### 3. Actualizare Referințe
Vom actualiza toate fișierele care foloseau vechile denumiri:
- `UserManager.kt` (referințe la `FirestoreUser` și `FirestoreMatch`).
- `ChessViewModel.kt` (referințe la `FirestoreMatch`, `FirestoreGameRepository`).
- `FriendsScreen.kt`, `LeaderboardScreen.kt`, `MainMenuScreen.kt`, `MainActivity.kt`.

## Plan de Verificare

### Testare
1.  **Compilare**: Verificarea lipsei erorilor de tip "Unresolved reference" după refactorizare.
2.  **Firebase Sync**: Verificarea faptului că datele continuă să fie salvate/citite corect din Firestore (Firestore folosește numele claselor pentru mapare automată dacă nu sunt specificate adnotări, deci trebuie să fim atenți la compatibilitatea cu documentele existente).

> [!WARNING]
> Schimbarea numelui clasei `User` în `FirestoreUser` ar putea afecta modul în care Firestore mapază automat obiectele dacă baza de date conține deja date. Vom folosi alias-uri sau ne vom asigura că proprietățile rămân identice.

**Ești de acord să începem această refactorizare a numelor?**
