# ATM Android App

A simple ATM app for Android, written in Java and XML. Each user has a card that is a **QR code image** plus a password. The app talks directly to a **MySQL** database to store cards, balances and loans.

The app's interface is in Persian (Farsi). Amounts are in rials.

## Features

**Start screen**
- **Create a card:** enter a person's name and a password, tap *Generate QR* (تولید کد QR), then *Save QR* (ذخیره کد QR).
  - The QR code contains the name you entered.
  - The image is saved to the phone's **Downloads** folder as `<name>.png`.
  - A new card is added to the database with a balance of 0.
- **Read QR** (خواندن کد QR): pick a QR image and see the text inside it.
- **Login to ATM** (ورود به ATM): opens the login screen.

**Login**
- Pick your card's QR image and enter your password.

**Menu (after login)**

| Menu item | What it does |
|---|---|
| Balance (موجودی) | Shows your card balance. |
| Transfer to bank (انتقال پول به بانک) | Pays an amount to the bank. It is taken from your balance. |
| Transfer to user (انتقال پول به کاربر) | Pick another person's QR image and send them money. |
| Get a loan (دریافت وام) | Choose 500,000 / 1,000,000 / 5,000,000 rials and 12 / 24 / 36 months. The money is added to your balance. |
| Pay installment (پرداخت قسط) | Shows your loans. Red = not fully paid, green = fully paid. *Pay* takes one installment (loan ÷ months) from your balance. |
| Exit (خروج) | Back to the login screen. |

Transfers check that you have enough balance first.

## Tech

- Java, XML layouts
- Android Gradle Plugin 4.2.2, Gradle 6.7.1
- compileSdk / targetSdk 31, minSdk 16
- [ZXing](https://github.com/zxing/zxing) and [zxing-android-embedded](https://github.com/journeyapps/zxing-android-embedded) for making and reading QR codes
- MySQL Connector/J 5.1.49 (JDBC) to connect to MySQL

## Project structure

```
database.sql                  # Creates the database and tables
app/src/main/
├── AndroidManifest.xml
├── res/layout/               # Screen layouts (XML)
└── java/com/example/myapplication/
    ├── MainActivity.java     # Start screen: create card, save QR, read QR
    ├── Login.java            # Login with QR image + password
    ├── Menu.java             # Main menu
    ├── Balance.java          # Show balance
    ├── BankDeposit.java      # Transfer to bank
    ├── UserDeposit.java      # Transfer to another user
    ├── ReceiveLoan.java      # Get a loan
    ├── PayLoan.java          # Pay loan installments
    ├── MySQLConnection.java  # All database code (connection + queries)
    └── Card.java, Loan.java  # Data classes
```

## How to run

### What you need

- **Android Studio**
- **JDK 11** (Gradle 6.7.1 does not run on Java 16 or newer)
- **MySQL 8.0 or 8.4** on your computer (MySQL 9 does not work, see step 1)
- An **Android Emulator** (the app connects to `10.0.2.2`, which only works in the emulator)

### 1. Set up MySQL

The app uses an old MySQL driver (Connector/J 5.1). It **cannot log in** with MySQL 8's default login method (`caching_sha2_password`). You need to switch the `root` user to `mysql_native_password`.

**MySQL 8.4 only:** first turn on the old login method. Add this to your MySQL config file (`my.ini` on Windows, `my.cnf` on Linux/macOS) under `[mysqld]`, then restart MySQL:

```ini
[mysqld]
mysql_native_password=ON
```

(Skip this on MySQL 8.0. It is already on there.)

**Then, on MySQL 8.0 and 8.4,** open the MySQL client and run (use your own password):

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password';
FLUSH PRIVILEGES;
```

Now create the database and tables. From the project folder, run:

```bash
mysql -u root -p < database.sql
```

On Windows PowerShell, `<` does not work. Log in with `mysql -u root -p` and run this instead:

```sql
SOURCE database.sql;
```

This creates the database `atm` with two tables: `card` and `loan`.

### 2. Set the database password in the app

Open `app/src/main/java/com/example/myapplication/MySQLConnection.java` and put your MySQL root password here:

```java
private static final String DB_PASSWORD = "YOURPASSWORD";
```

The other settings can stay as they are:

```java
private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/atm";
private static final String DB_USER = "root";
```

`10.0.2.2` is how the Android Emulator reaches your computer. MySQL must be running on port `3306`.

### 3. Open the project in Android Studio

1. Clone the repo and open the folder in Android Studio.
2. Set the Gradle JDK to **11**:
   *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.
   If you don't have JDK 11, choose *Download JDK* in the same list.
3. Let Gradle sync. If Android Studio asks to install **SDK Platform 31** or **Build Tools 31.0.0**, accept.

### 4. Run the app

1. Create and start an emulator (*Device Manager* in Android Studio).
2. Press **Run**. The app appears on the emulator as **"My Application"**.

### 5. Try it

1. Open the app. Allow storage permission if it asks.
2. On the start screen, type a name and a password. Tap **Generate QR**, then **Save QR**.
3. Tap **Login to ATM**. Tap **Read QR** and pick `<name>.png` from the **Downloads** folder.
4. Enter the password and log in.

New cards start with a balance of 0. To get money, use **Get a loan**, or set a balance directly in MySQL:

```sql
UPDATE atm.card SET balance = 1000000 WHERE qr_value = 'your_name';
```

To test **Transfer to user**, create a second card the same way.

**Note:** the name is also the card's ID (it is the QR code value), so each name must be different.

## Troubleshooting

- **Login always fails / nothing happens:** check that MySQL is running, the password in `MySQLConnection.java` is correct, and `root` uses `mysql_native_password` (step 1). Database errors are printed in **Logcat**.
- **"Installed Build Tools revision 31.0.0 is corrupted":** this is a known problem with Build Tools 31 and older Android Gradle Plugin versions. In `app/build.gradle`, change `buildToolsVersion "31.0.0"` to `buildToolsVersion "30.0.3"` and sync again.
- **"Unsupported class file major version" during sync:** the Gradle JDK is too new. Set it to JDK 11 (step 3).

## Note

This is a learning project. Passwords are stored as plain text, and the app connects straight to the database with the root account. Don't use this setup for real money.
