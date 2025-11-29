# 📱 Smartphone Advisor
> **Your AI-Powered Personal Tech Consultant**

![Project Status](https://img.shields.io/badge/Status-Active_Development-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)
![Tech Stack](https://img.shields.io/badge/Stack-Full_Stack-orange?style=for-the-badge)

---

## 🚀 Overview

**Smartphone Advisor** isn't just another comparison site. It's an intelligent decision-making engine designed to cut through the noise of the tech world. 

In an era where hundreds of smartphones launch every year, finding the "perfect" one is overwhelming. We solve this by combining **hard specs** with **real-world sentiment**. We don't just look at the spec sheet; we listen to what the world is saying.

By analyzing **YouTube reviews** from top tech channels and diving deep into **Reddit discussions**, our system generates a "Real-World Score" that tells you how a phone actually performs, not just what the box says.

---

## ✨ Key Features

### 🧠 Intelligent Recommendation Engine
*   **Budget-First Approach**: We respect your wallet. Recommendations are strictly tailored to your price range.
*   **Priority-Based Ranking**: Care more about **Camera** than **Battery**? Or is **Performance** your non-negotiable? Rank your priorities, and our algorithm adapts instantly.
*   **Top 5 Picks**: We don't overwhelm you. We give you the absolute best 5 options that match your unique profile.

### 🗣️ Sentiment Analysis (The "Ears" of the System)
*   **YouTube Intelligence**: We scrape and analyze transcripts from trusted tech reviewers to gauge professional sentiment.
*   **Reddit Hivemind**: We tap into the raw, unfiltered opinions of actual users on Reddit to uncover long-term issues and hidden gems.
*   **Sentiment Scoring**: Positive, Negative, and Neutral sentiments are weighed to adjust the final score of every device.

### 📊 Dynamic Scoring System
*   **Feature Scoring**: Every phone gets a granular score for Camera, Battery, Performance, Display, and Build Quality.
*   **Brand Value**: We factor in brand reliability and after-sales support.
*   **Weighted Algorithms**: Your preferences directly influence the weight of each parameter in the final calculation.

---

## 🛠️ The Tech Stack

Built with a modern, scalable, and robust architecture.

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=flat&logo=react&logoColor=61DAFB) ![TailwindCSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=flat&logo=tailwind-css&logoColor=white) | A responsive, high-performance UI with a glassmorphism design language. |
| **Backend** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=flat&logo=spring-boot&logoColor=green) ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white) | The core logic engine handling API requests, user sessions, and complex scoring algorithms. |
| **Data Services** | ![Node.js](https://img.shields.io/badge/Node.js-43853D?style=flat&logo=node.js&logoColor=white) | Microservices for scraping YouTube/Reddit data and syncing product catalogs. |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-005C84?style=flat&logo=mysql&logoColor=white) | Relational database for storing phone specs, user reviews, and sentiment data. |

---

## ⚙️ How It Works

1.  **Data Ingestion**:
    *   Our **Node.js services** continuously monitor tech channels and subreddits.
    *   New phones are added to the database with their raw specifications.

2.  **The "Crunch"**:
    *   **Sentiment Analysis** processes the text data to assign a "Reputation Score".
    *   **The Spring Boot Backend** combines this with the hardware specs (e.g., 108MP Camera + Positive Camera Reviews = High Camera Score).

3.  **User Interaction**:
    *   You tell us your **Budget** and **Priorities**.
    *   We run a real-time query, applying your unique weights to our pre-calculated scores.
    *   **Voila!** You get a curated list of the top 5 phones that fit *you*.

---

## 🚀 Getting Started

### Prerequisites
*   Node.js (v16+)
*   Java JDK 17+
*   MySQL

### Installation

1.  **Clone the repository**
    ```bash
    git clone https://github.com/yourusername/smartphone-advisor.git
    ```

2.  **Setup Backend**
    ```bash
    cd backend
    ./mvnw spring-boot:run
    ```

3.  **Setup Frontend**
    ```bash
    cd frontend
    npm install
    npm start
    ```

4.  **Run Data Services (Optional for local dev)**
    ```bash
    cd data-service
    node sync-phones.js
    ```

---

## 🔮 Future Roadmap

*   [ ] **Live Pricing**: Integration with Amazon/Flipkart APIs for real-time price updates.
*   [ ] **User Accounts**: Save your recommendations and track price drops.
*   [ ] **Visual Comparisons**: Side-by-side photo comparisons from reviews.
*   [ ] **AI Chatbot**: A conversational interface to ask specific questions like "Is the battery better than the iPhone 13?".

---

<div align="center">

**Crafted with ❤️ for Tech Enthusiasts**

[Report Bug](https://github.com/aryan2511/smartphone-advisor/issues) · [Request Feature](https://github.com/aryan2511/smartphone-advisor/issues)

</div>
