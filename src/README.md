# 🚗 Vehicle Quotation System

Sistema di preventivazione per veicoli full-stack con backend Java Spring Boot, frontend React + Tailwind CSS, deployato su Render con Docker e database MySQL su phpMyAdmin.

## 📋 Indice

- [Caratteristiche](#caratteristiche)
- [Stack Tecnologico](#stack-tecnologico)
- [Architettura](#architettura)
- [Struttura del Progetto](#struttura-del-progetto)
- [Logica di Calcolo Prezzi](#logica-di-calcolo-prezzi)
- [Endpoints API](#endpoints-api)
- [Setup e Installazione](#setup-e-installazione)
- [Deployment](#deployment)

## ✨ Caratteristiche

- **Gestione Preventivi**: Creazione, lettura e aggiornamento di preventivi personalizzati
- **Calcolo Prezzi Dinamico**: Sistema avanzato di pricing con molteplici fattori
- **Gestione Utenti**: Associazione preventivi a utenti con email tracking
- **Optional e Varianti**: Supporto per configurazioni veicolo personalizzate
- **Sconti Automatici**: Sistema intelligente di sconti basato su regole multiple
- **UI Responsive**: Interfaccia moderna e mobile-friendly con Tailwind CSS

## 🛠 Stack Tecnologico

### Backend
- **Java 21** con Spring Boot
- **Spring Data JPA** per la persistenza
- **Maven** per la gestione dipendenze
- **MySQL** database relazionale
- **BigDecimal** per calcoli finanziari precisi

### Frontend
- **React** (biblioteca UI)
- **Tailwind CSS** (styling utility-first)
- **Axios** (HTTP client)

### DevOps & Deployment
- **Docker** (containerizzazione)
- **Render** (hosting backend)
- **phpMyAdmin** (gestione database MySQL online)

## 🏗 Architettura

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   React + TW    │  HTTP   │  Spring Boot    │  JDBC   │  MySQL (online) │
│    Frontend     │◄───────►│     Backend     │◄───────►│   phpMyAdmin    │
│                 │   API   │                 │         │                 │
└─────────────────┘         └─────────────────┘         └─────────────────┘
        │                           │
        │                           │
        └───────── Render ──────────┘
              (Docker Deploy)
```

## 📁 Struttura del Progetto

```
vehicle-quotation-system/
├── backend/
│   ├── src/
│   │   └── main/
│   │       └── java/org/lessons/vehicles/java/
│   │           ├── quoted/          # Gestione preventivi
│   │           │   ├── model/       # Entità Quoted
│   │           │   ├── dto/         # Data Transfer Objects
│   │           │   ├── repository/  # Repository JPA
│   │           │   └── service/     # Business Logic
│   │           ├── vehicle/         # Gestione veicoli
│   │           ├── vehicleVariation/# Varianti veicoli
│   │           ├── optionals/       # Optional aggiuntivi
│   │           └── user/            # Gestione utenti
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── services/
│   ├── tailwind.config.js
│   └── package.json
└── docker-compose.yml
```

💰 Logica di Calcolo Prezzi
Il sistema calcola il prezzo finale attraverso un processo sequenziale che traccia ogni aggiustamento tramite il DTO PriceAdjustment, fornendo trasparenza completa sul calcolo.
Struttura del Calcolo
Il prezzo viene calcolato in due fasi principali gestite da servizi separati:

PriceCalculatorService: Calcola il prezzo base del veicolo con maggiorazioni
QuotedService: Applica optional e sconti finali

Fase 1: Calcolo Prezzo Veicolo (PriceCalculatorService)
1. Prezzo Base
Parte dal basePrice del veicolo selezionato.
2. Maggiorazione Cilindrata (CC)

Base: 1000cc (nessuna maggiorazione)
Incremento: +5% ogni 500cc oltre i 1000cc
Formula: extraBlocks = max(0, (cc - 1000) / 500) → moltiplicatore = 1 + (0.05 × extraBlocks)
Esempio:

1800cc → (1800-1000)/500 = 1.6 blocchi → moltiplicatore 1.08 → +8%
2500cc → (2500-1000)/500 = 3 blocchi → moltiplicatore 1.15 → +15%


Tracking: Salva "Maggiorazione Cilindrata (XXXXcc)" con differenza di prezzo

3. Fattore Anzianità (Anno Immatricolazione)

Anno corrente: nessuna maggiorazione (moltiplicatore 1.0)
0-2 anni fa: +4% (moltiplicatore 1.04)
3-4 anni fa: +8% (moltiplicatore 1.08)
5+ anni fa: +12% (moltiplicatore 1.12)
Tracking: Salva "Fattore Anzianità (anno)" solo se diverso dall'anno corrente

4. Supplemento Alimentazione

Benzina/Gasoline: nessuna maggiorazione (moltiplicatore 1.0)
Diesel: +3% (moltiplicatore 1.03)
Elettrico/Electric: +10% (moltiplicatore 1.10)
Ibrido/Hybrid: +5% (moltiplicatore 1.05)
GPL: +5% (moltiplicatore 1.05)
Tracking: Salva "Supplemento Alimentazione (tipo)" solo se diverso da benzina
Nota: Supporta sia italiano che inglese per i nomi carburanti

Fase 2: Optional e Sconti Finali (QuotedService)
5. Costo Optional
Somma del prezzo di tutti gli optional selezionati dall'utente.
6. Sconti Applicati
Sconto Multiple Optional

Condizione: 3 o più optional selezionati
Sconto: -3% sul totale corrente
Moltiplicatore: 0.97

Sconto Veicolo Nuovo

Condizione: Anno immatricolazione = anno corrente
Sconto: -2% sul totale corrente
Moltiplicatore: 0.98
Nota: Questo sconto è cumulabile con altri

Sconto Fascia Alta

Condizione: Prezzo totale > €20.000
Sconto: -5% solo sulla parte eccedente i €20.000
Formula: prezzoFinale = 20000 + ((totale - 20000) × 0.95)
Esempio:

€25.000 → €20.000 + (€5.000 × 0.95) = €24.750 (risparmio €250)
€30.000 → €20.000 + (€10.000 × 0.95) = €29.500 (risparmio €500)



Combo Climatizzatore + Navigatore

Condizione: Entrambi gli optional presenti (riconosciuti sia in italiano che inglese)
Sconto: -€100 fissi (detrazione diretta dal totale)
Keywords riconosciute:

IT: "climatizzatore" / EN: "air conditioning"
IT: "navigatore" / EN: "navigator"



Esempio Completo di Calcolo
Configurazione:

Veicolo: Fiat 500 (prezzo base: €15.000)
Cilindrata: 1200cc
Anno: 2023 (2 anni fa)
Alimentazione: Benzina
Optional: Climatizzatore (€800), Navigatore (€600), Cruise Control (€400)

Calcolo Step-by-Step:

Prezzo base: €15.000
Maggiorazione cilindrata (1200cc): €15.000 × 1.02 = €15.300 (+€300)
Fattore anzianità (2 anni): €15.300 × 1.04 = €15.912 (+€612)
Supplemento alimentazione (benzina): nessuna maggiorazione
Subtotale veicolo: €15.912
Optional totali: €800 + €600 + €400 = €1.800
Totale pre-sconti: €17.712
Sconto 3+ optional: €17.712 × 0.97 = €17.181 (-€531)
Combo clima+nav: €17.181 - €100 = €17.081 (-€100)
Prezzo finale: €17.081

Sistema di Tracking (PriceAdjustment DTO)
Ogni aggiustamento viene tracciato tramite il record PriceAdjustment:
javarecord PriceAdjustment(String description, BigDecimal amount)
Questo permette di:

Mostrare all'utente il dettaglio completo del calcolo
Generare report trasparenti sui preventivi
Facilitare debugging e audit dei prezzi

## 🔌 Endpoints API

### GET `/quotation`
Recupera tutti i preventivi del sistema.

**Response**: `List<QuotedDTO>`

### GET `/quotation/user/{email}`
Recupera tutti i preventivi associati a una email utente.

**Parameters**: 
- `email` - Email dell'utente

**Response**: `List<QuotedDTO>`

### GET `/quotation/{id}`
Recupera un preventivo specifico per ID.

**Parameters**: 
- `id` - ID del preventivo

**Response**: `QuotedDTO`

### POST `/quotation`
Crea un nuovo preventivo.

**Request Body**: `QuotedDTO`

**Response**: `QuotedDTO` (con prezzo finale calcolato)

### PUT `/quotation/{id}`
Aggiorna un preventivo esistente.

**Parameters**: 
- `id` - ID del preventivo

**Request Body**: `QuotedDTO`

**Response**: `QuotedDTO` (con prezzo finale ricalcolato)

## 🚀 Setup e Installazione

### Prerequisiti
- Java 21 
- Node.js 16+ e npm
- Maven 3.6+
- Docker e Docker Compose
- Account Render (per deployment)
- MySQL database online con phpMyAdmin

### Installazione Locale

#### 1. Clone del Repository
```bash
git clone [repository-url]
cd vehicle-quotation-system
```

#### 2. Configurazione Backend

**application.properties** (o application.yml):
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://[your-mysql-host]:3306/vehicles_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server Configuration
server.port=8080

# CORS Configuration (per frontend locale)
cors.allowed.origins=http://localhost:3000
```

**Build Backend**:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### 3. Configurazione Frontend

**Installa dipendenze**:
```bash
cd frontend
npm install
```

**File .env**:
```env
REACT_APP_API_URL=http://localhost:8080/api
```

**Avvio frontend**:
```bash
npm start
```

L'applicazione sarà disponibile su:
- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`

## 🐳 Deployment

### Configurazione Docker

**Dockerfile (Backend)**:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://[mysql-host]:3306/vehicles_db
      - SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
    restart: always
```

### Deploy su Render

#### 1. Preparazione Database MySQL
- Crea un database MySQL su un servizio cloud (es. Railway, PlanetScale, o hosting con phpMyAdmin)
- Configura phpMyAdmin per gestione database
- Annota le credenziali (host, username, password, database name)

#### 2. Deploy Backend su Render

1. Crea nuovo **Web Service** su Render
2. Connetti il repository GitHub
3. Configura:
   - **Build Command**: `mvn clean package`
   - **Start Command**: `java -jar target/*.jar`
   - **Environment**: Docker
4. Aggiungi variabili d'ambiente:
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://[host]:3306/vehicles_db
   SPRING_DATASOURCE_USERNAME=your_username
   SPRING_DATASOURCE_PASSWORD=your_password
   ```

#### 3. Deploy Frontend

1. Build di produzione:
   ```bash
   cd frontend
   npm run build
   ```

2. Deploy su Render come **Static Site** oppure su:
   - Vercel
   - Netlify
   - GitHub Pages

3. Configura variabile d'ambiente:
   ```
   REACT_APP_API_URL=https://vehicle-backend-v1ur.onrender.com/api/quotation
   ```

### Verifica Deployment

1. Testa gli endpoint API:
   ```bash
   curl https://your-backend.onrender.com/api/quoted
   ```

2. Verifica frontend:
   - Apri l'URL del frontend
   - Testa creazione preventivo
   - Verifica connessione al backend

## 📊 Gestione Database

### Accesso phpMyAdmin

1. Accedi al pannello phpMyAdmin del tuo hosting
2. Seleziona il database `vehicles_db`
3. Monitora le tabelle:
   - `quoted` - Preventivi
   - `vehicle` - Veicoli
   - `vehicle_variation` - Varianti
   - `optionals` - Optional
   - `user` - Utenti

### Backup Database

```bash
# Export via phpMyAdmin
# Oppure da terminale:
mysqldump -h [host] -u [username] -p vehicles_db > backup.sql
```

### Restore Database

```bash
mysql -h [host] -u [username] -p vehicles_db < backup.sql
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📝 Esempio Utilizzo API

### Creazione Preventivo

```bash
curl -X POST https://your-backend.onrender.com/api/quoted \
  -H "Content-Type: application/json" \
  -d '{
  "userName": "Mario",
  "userSurname": "Rossi",
  "userMail": "mario.rossi@example.com",
"userEmail": "mario.rossi@example.com",

  "vehicleDTOToQuoted": [
    { "id": 103 }
  ],
  "vehicleVariationId": 1,
  "optionalDTOtoQuoted": [
    { "id": 301 },
    { "id": 302 }
  ]
}'
```

## 🔧 Troubleshooting

### Problemi Comuni

**Errore di connessione al database**:
- Verifica le credenziali in `application.properties`
- Controlla che il database sia online
- Verifica le regole del firewall

**CORS errors**:
- Configura `@CrossOrigin` nei controller Spring
- Imposta `cors.allowed.origins` correttamente

**Build Docker fallita**:
- Verifica che `mvn package` funzioni localmente
- Controlla i log di Render per errori specifici

## 🤝 Contributi

Per contribuire al progetto:

1. Fork del repository
2. Crea un branch (`git checkout -b feature/NuovaFeature`)
3. Commit delle modifiche (`git commit -m 'Add NuovaFeature'`)
4. Push del branch (`git push origin feature/NuovaFeature`)
5. Apri una Pull Request

## 📄 Licenza

Questo progetto è distribuito sotto licenza MIT.

## 👥 Team

Back-end sviluppato da Marco Vacchi e Enrico Ciccolini, front-end Simone Burrai e Carlo costa

## 📧 Contatti

Per supporto e informazioni: [your-email@example.com]

---

**Note di Produzione**:
- Il sistema utilizza BigDecimal per garantire precisione nei calcoli finanziari
- Il database MySQL è hostato online e gestibile via phpMyAdmin
- Backend deployato su Render con Docker per scalabilità
- Frontend React ottimizzato con Tailwind CSS per performance e UX