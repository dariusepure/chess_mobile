# Plan: Comutator Dark/Light Mode

Acest plan detaliază implementarea unui comutator manual pentru temă (Dark, Light sau System Default), permițând utilizatorului să aleagă aspectul preferat indiferent de setările telefonului.

## Modificări Propuse

### 1. Persistență Setări (`logic/UserManager.kt`)
- **[MODIFY] [UserManager.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/UserManager.kt)**:
    - Adăugarea unei chei noi în `SharedPreferences` pentru `theme_mode`.
    - Funcții pentru a salva și a citi preferința de temă (0 = System, 1 = Light, 2 = Dark).

### 2. State Management (`ui/ChessViewModel.kt`)
- **[MODIFY] [ChessViewModel.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/ui/ChessViewModel.kt)**:
    - Adăugarea unei stări `themeMode` observabile.
    - Metodă `updateThemeMode(mode: Int)` care persistă alegerea și actualizează UI-ul.

### 3. Integrare Temă (`ui/theme/Theme.kt` & `MainActivity.kt`)
- **[MODIFY] [Theme.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/ui/theme/Theme.kt)**:
    - Actualizarea `ChessMobileTheme` pentru a accepta un parametru de tip `ThemeMode` și a forța culorile dacă este necesar.
- **[MODIFY] [MainActivity.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/MainActivity.kt)**:
    - Pasarea stării de temă din ViewModel către wrapper-ul de temă Compose.

### 4. UI Toggle (`ui/MainMenuScreen.kt`)
- **[MODIFY] [MainMenuScreen.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/ui/MainMenuScreen.kt)**:
    - Adăugarea unei secțiuni de "Display" în setări.
    - Un rând cu un icon de Soare/Lună și un `Switch` sau un set de `FilterChip`-uri pentru a alege: **System**, **Light**, **Dark**.

## Plan de Verificare

### Testare Manuală
1.  **Light Mode**: Selectarea "Light" și verificarea dacă fundalul devine deschis chiar dacă sistemul e pe Dark.
2.  **Dark Mode**: Selectarea "Dark" și verificarea aspectului premium închis.
3.  **Persistență**: Schimbarea temei, închiderea aplicației și verificarea dacă setarea s-a păstrat la redeschidere.
4.  **Auto**: Revenirea la "System" și verificarea dacă urmărește setările Android.
