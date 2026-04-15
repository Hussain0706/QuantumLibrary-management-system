# QuantumLibrary — Java Spring Boot Backend

## 🚀 Quick Start

### Step 1: Start the Backend
Double-click `start-backend.bat` **OR** open a terminal and run:
```bash
cd backend
mvn spring-boot:run
```

> First start downloads Maven dependencies (~2 minutes). Subsequent starts take ~5 seconds.

### Step 2: Open the Frontend
Open `index.html` (or use Live Server) in your browser.

---

## 🔑 Default Login Credentials

| Role   | Email                         | Password  |
|--------|-------------------------------|-----------|
| Admin  | `admin@quantumlibrary.com`    | `admin123` |
| Member | `hussain0706w@gmail.com`      | `member123` |

---

## 📧 Email Setup (Gmail SMTP)

1. Go to **Google Account → Security → 2-Step Verification**
2. Scroll down to **App Passwords**
3. Create a new app password (select "Mail" + "Windows Computer")
4. Copy the **16-character password**
5. Open `backend/src/main/resources/application.properties`
6. Replace `YOUR_GMAIL_APP_PASSWORD_HERE` with your password

```properties
spring.mail.password=abcd efgh ijkl mnop   ← your 16-char app password
```

---

## 🌐 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | None | Login → returns JWT |
| POST | `/api/auth/register` | None | Member registration |
| GET | `/api/books` | None | List books (`?search=` or `?genre=`) |
| GET | `/api/books/{id}` | None | Single book |
| POST | `/api/books` | Admin | Add book |
| PUT | `/api/books/{id}` | Admin | Update book |
| DELETE | `/api/books/{id}` | Admin | Delete book |
| POST | `/api/borrow` | Member | Borrow a book |
| POST | `/api/borrow/return/{id}` | Member | Return a book |
| GET | `/api/borrow/my` | Member | My active borrows |
| GET | `/api/borrow/all` | Admin | All borrow records |
| GET | `/api/borrow/overdue` | Admin | Overdue books |
| GET | `/api/fines/my` | Member | My fines |
| GET | `/api/fines/all` | Admin | All fines |
| POST | `/api/fines/pay/{id}` | Admin | Mark fine paid |
| GET | `/api/admin/stats` | Admin | Dashboard statistics |
| GET | `/api/admin/members` | Admin | All members |
| DELETE | `/api/admin/members/{id}` | Admin | Remove member |
| PUT | `/api/admin/members/{id}/toggle` | Admin | Activate/deactivate |

---

## 📧 Email Notifications (7 Types)

| Trigger | Recipient | Email Subject |
|---------|-----------|---------------|
| Member registers | Member | 📚 Welcome to QuantumLibrary! |
| Book borrowed | Member | 📖 Book Borrowed: [title] |
| Book returned | Member | ✅ Book Returned: [title] |
| 3 days before due | Member | ⏰ Reminder: Book Due Soon |
| Book overdue | Member | 🚨 OVERDUE: [title] |
| Fine paid at counter | Member | 🧾 Fine Payment Receipt |
| New book added | All Members | 📚 New Book Added: [title] |
| Weekly (every Sunday) | Admin | 📊 Weekly Digest |

---

## ⏰ Scheduled Jobs

| Time | Job |
|------|-----|
| Every day 08:00 AM | Due-soon reminder emails (3-day window) |
| Every day 09:00 AM | Overdue detection + fine calculation + alert emails |
| Every Sunday 10:00 AM | Admin weekly stats digest email |

---

## 🗄️ Database Console

Access the H2 database browser at:
**http://localhost:8080/h2-console**

- **JDBC URL:** `jdbc:h2:file:./quantumlibrary-db`
- **Username:** `sa`
- **Password:** *(leave empty)*

---

## 📁 Project Structure

```
33/
├── index.html          ← Landing page (frontend)
├── login.html          ← Login page (frontend)
├── dashboard.html      ← Member dashboard (frontend)
├── admin.html          ← Admin panel (frontend)
├── styles.css          ← All CSS styles
├── script.js           ← Frontend shared utilities
├── api.js              ← ← NEW: Frontend ↔ Backend bridge
├── start-backend.bat   ← ← NEW: One-click Windows launcher
└── backend/            ← ← NEW: Java Spring Boot backend
    ├── pom.xml
    └── src/main/java/com/quantumlibrary/
        ├── QuantumLibraryApplication.java
        ├── config/     ← JWT, Security, DataLoader, ExceptionHandler
        ├── dto/        ← Request/Response DTOs
        ├── entity/     ← JPA entities (User, Book, BorrowRecord, Fine)
        ├── repository/ ← Spring Data JPA repositories
        ├── service/    ← Business logic + Email
        ├── controller/ ← REST endpoints
        └── scheduler/  ← Scheduled jobs
```

---

## ⚙️ Requirements

- **Java 17+** → https://adoptium.net/
- **Maven 3.6+** → https://maven.apache.org/download.cgi
- **Gmail App Password** → for email notifications

---

## 🎯 Business Rules

| Rule | Value |
|------|-------|
| Max books per member | 3 |
| Loan period | 14 days |
| Fine rate | ₹5 per day after due date |
| Token validity | 24 hours |
