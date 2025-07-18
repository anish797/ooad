# Collaborative Markdown Editor


![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen?style=for-the-badge&logo=spring&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue?style=for-the-badge&logo=postgresql&logoColor=white)

**Real-time collaborative markdown editor with JavaFX desktop client and Spring Boot backend**


---

## Features

- **Real-time Collaboration** - Multiple users edit simultaneously with live updates
- **Rich Markdown Editor** - Split-pane interface with live preview and dark theme
- **Version Control** - Track changes, create versions, and restore previous states
- **User Management** - Secure authentication and collaborator permissions
- **Multiple Exports** - Export to PDF, HTML, and DOCX formats
- **Undo/Redo** - Command pattern implementation for text operations
- **Auto-save** - Continuous saving with manual version creation

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL
### Setup & Run

1. **Clone**
   ```bash
   git clone https://github.com/anish797/ooad.git
   cd ooad
   ```
   
2. **Setup database**
   ```bash
   CREATE DATABASE markdowncollab;
   ```
3. **Start the backend**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Launch desktop app** *(new terminal)*
   ```bash
   mvn javafx:run
   ```
---

## Tech Stack

- **Backend**: Spring Boot 3.2.3, Spring Security
- **Frontend**: JavaFX 21.0.2 with WebView
- **Database**: PostgreSQL
- **Markdown**: CommonMark, Flexmark

---

</div>
