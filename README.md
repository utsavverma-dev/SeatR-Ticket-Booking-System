<div align="center">

<img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" alt="Status" />
<img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
<img src="https://img.shields.io/badge/React-18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" alt="React" />
<img src="https://img.shields.io/badge/Vite-B73BFE?style=for-the-badge&logo=vite&logoColor=FFD62E" alt="Vite" />

<br/>

# 🎟️ SeatR

### ✨ Premium Ticket Booking System ✨

**Secure. Role-driven. Built to feel effortless.**

<p>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License" />
  <img src="https://img.shields.io/badge/PRs-Welcome-brightgreen.svg?style=flat-square" alt="PRs Welcome" />
  <img src="https://img.shields.io/badge/Made%20with-%E2%9D%A4-red?style=flat-square" alt="Made with love" />
</p>

</div>

---

## 📖 About The Project

**SeatR** is a modern, full-stack event and ticket booking platform designed with a sleek aesthetic and a secure, role-aware backend. It pairs a hardened **Spring Boot** REST API with a fast, responsive **React + Vite** frontend to deliver an immersive booking experience from browsing events to checkout.

This repository contains the complete, submission-ready source code — cleanly separated into a backend and a frontend, with every unnecessary file stripped out.

---

## 📂 Project Structure

```
SeatR/
├── backend/     → Spring Boot (Java) REST API
└── frontend/    → React (Vite) Single Page Application
```

> 🧹 **Clean by design** — this repository strictly adheres to submission guidelines. No `node_modules`, `.env` files, build artifacts, or editor configs are committed. Only the essential application files needed to run the project are included.

---

## 🏗️ Architecture & Tech Stack

<table>
<tr>
<td width="50%" valign="top">

### ⚙️ Backend — Spring Boot

The secure RESTful engine behind SeatR — handling business logic, database operations, authentication, and email services.

| | |
|---|---|
| **Language** | Java 17+ |
| **Framework** | Spring Boot 3.x |
| **Database** | MySQL (Spring Data JPA / Hibernate) |
| **Security** | Spring Security + JWT |

**Core Features**
- 🔐 Role-based Access Control (`Admin`, `Organiser`, `Customer`)
- 🛡️ Secure endpoints for Venues, Events, Tickets & Users
- 📧 Help Centre integration with automated email delivery

</td>
<td width="50%" valign="top">

### 🎨 Frontend — React + Vite

A fast, responsive Single Page Application delivering an immersive, role-aware UI.

| | |
|---|---|
| **Framework** | React 18 |
| **Bundler** | Vite |
| **Styling** | Tailwind CSS |
| **Routing** | React Router DOM |

**Core Features**
- 📊 Dynamic dashboards tailored to user role
- 💬 Interactive popup modals (e.g. Help Centre)
- 🔔 Toast notifications & Hook-based state management

</td>
</tr>
</table>

---

## 🚀 Getting Started

To run SeatR locally, you'll start both the backend and frontend servers. Follow the steps below in order — **the app should come up cleanly with no errors.**

### ✅ Prerequisites

- ☕ **Java 17 or 21** installed (`JAVA_HOME` configured)
- 📦 **Maven** installed
- 🟢 **Node.js** (v18+) and **npm** installed
- 🐬 **MySQL Server** installed and running

---

### 🖥️ Part 1 — Running the Backend

**1. Configure the Database**

- Ensure your MySQL server is running locally
- Create a database named `ticket_booking` *(or update `application.properties` to match your local setup)*
- Verify the credentials in `backend/src/main/resources/application.properties` are correct (default is usually `root` / `password`)

**2. Start the Application**

```bash
cd backend
mvn spring-boot:run
```

<div align="center">

**🟢 Backend running at → `http://localhost:8080`**

</div>

---

### 🌐 Part 2 — Running the Frontend

**1. Install Dependencies**

Open a **new** terminal window:

```bash
cd frontend
npm install
```

> 📌 The `node_modules` folder generated here is intentionally excluded from the repository per submission guidelines.

**2. Start the Dev Server**

```bash
npm run dev
```

<div align="center">

**🟢 Frontend running at → `http://localhost:5173`**

### 🎉 Open the URL above in your browser to start using SeatR!

</div>

---

## ✅ Submission Checklist Verification

| Status | Requirement |
| :---: | :--- |
| ✅ | Application runs without errors (backend & frontend both start successfully) |
| ✅ | Code is cleanly structured and named (`/backend` and `/frontend` separation) |
| ✅ | No unnecessary/temporary files committed — `.gitignore` covers `.idea`, `.vscode`, `node_modules`, `dist`, `.env` |
| ✅ | Branch name is set to `main` |
| ✅ | Repository is publicly accessible and downloadable |
| ✅ | Proper documentation provided (this README) |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. 🍴 Fork the project
2. 🌿 Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. 💾 Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push to the branch (`git push origin feature/AmazingFeature`)
5. 🔀 Open a Pull Request

---

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for more information.

---

<div align="center">

### ⭐ If you found this project useful, consider giving it a star!

Made with ❤️ and a lot of ☕

</div>