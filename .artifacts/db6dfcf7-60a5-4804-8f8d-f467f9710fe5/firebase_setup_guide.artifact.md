# Ghid de Configurare Firebase pentru Chess Mobile

Acest document te va ghida prin pașii necesari în Consola Firebase pentru a activa funcționalitățile online ale aplicației tale.

## 1. Crearea Proiectului
1. Mergi la [Consola Firebase](https://console.firebase.google.com/).
2. Apasă pe **"Add project"** și urmează pașii pentru a-i da un nume (ex: "Chess Mobile").
3. (Opțional) Dezactivează Google Analytics pentru simplitate în această etapă.

## 2. Înregistrarea Aplicației Android
1. În centrul paginii de prezentare a proiectului, apasă pe iconița **Android**.
2. **Android package name**: Introdu `com.dariusepure.chessmobile` (trebuie să fie identic cu cel din `build.gradle.kts`).
3. **App nickname**: Chess Mobile.
4. **Debug signing certificate SHA-1**:
   - În Android Studio, deschide panoul **Gradle** (în dreapta).
   - Mergi la `ChessMobile` -> `Tasks` -> `android` -> `signingReport`.
   - Rulează-l (dublu click). În consola `Run` de jos vei vedea codul SHA-1. Copiază-l și pune-l în Firebase.
5. Apasă **"Register app"**.

## 3. Fișierul de Configurare
1. Descarcă fișierul `google-services.json`.
2. Mută fișierul în folderul `app/` al proiectului tău (ex: `/home/darius/AndroidStudioProjects/ChessMobile/app/`).
3. În Firebase Console, apasă "Next" până finalizezi wizard-ul.

## 4. Activarea Autentificării (Authentication)
1. În meniul din stânga, mergi la **Build** -> **Authentication**.
2. Apasă **"Get Started"**.
3. În tab-ul **Sign-in method**, activează:
   - **Email/Password**: Activează prima opțiune și apasă Save.
   - **Google**: Activează, alege un email pentru suport și apasă Save.
4. **IMPORTANT**: După ce activezi Google, mergi la rotita de setări a proiectului (Project Settings) -> **General**. În partea de jos, la aplicația Android, vei vedea "Web client ID". Copiază-l!

## 5. Configurarea Bazei de Date (Firestore)
1. În meniul din stânga, mergi la **Build** -> **Firestore Database**.
2. Apasă **"Create database"**.
3. Alege locația (ex: eur3 pentru Europa).
4. Alege **"Start in test mode"** (pentru a permite scrierea/citirea imediată) și apasă Create.

## 6. Actualizare Cod (Pas Final)
1. Deschide [UserManager.kt](file:///home/darius/AndroidStudioProjects/ChessMobile/app/src/main/java/com/dariusepure/chessmobile/logic/UserManager.kt).
2. Caută linia `.setServerClientId("YOUR_WEB_CLIENT_ID")`.
3. Înlocuiește `"YOUR_WEB_CLIENT_ID"` cu codul pe care l-ai copiat la pasul 4.

> [!CAUTION]
> Nu uita să adaugi SHA-1 în Firebase, altfel Google Sign-In va returna eroare imediat ce apeși butonul.

> [!TIP]
> Dacă ai nevoie de ajutor cu regulile Firestore mai târziu (pentru securitate), anunță-mă!
