# Free Tier Deployment Guide

This guide will help you deploy your E-Commerce API to **Render** (Free Tier) using **Upstash** for Kafka/Redis and **Neon** for PostgreSQL.

## Prerequisites
- [GitHub Account](https://github.com/) (to host your code)
- [Render Account](https://render.com/)
- [Upstash Account](https://upstash.com/)
- [Neon Account](https://neon.tech/)

---

## Step 1: Push Code to GitHub
Ensure this project is in a GitHub repository.
```bash
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

---

## Step 2: Set up Database (Neon.tech)
1. Log in to [Neon Console](https://console.neon.tech/).
2. Create a **New Project**.
3. Copy the **Connection String** (Postgres URL). It looks like:
   `postgres://user:password@ep-xyz.aws.neon.tech/neondb?sslmode=require`

---

## Step 3: Set up Kafka & Redis (Upstash)
### Kafka
1. Log in to [Upstash Console](https://console.upstash.com/).
2. Create a **Kafka Cluster** (Free Tier).
3. Go to the **Details** tab and copy:
   - **Bootstrap Server / Broker Endpoint** (e.g., `finer-gull-1234-us1-kafka.upstash.io:9092`)
   - **Username** (e.g., `finer-gull-1234`)
   - **Password**
   - **SASL Mechanism** (likely `SCRAM-SHA-256`)

### Redis
1. Create a **Redis Database** in Upstash.
2. In the **Details** tab, copy:
   - **Endpoint** (Host)
   - **Port** (usually `6379`)
   - **Password**

---

## Step 4: Deploy to Render
1. Log in to [Render](https://dashboard.render.com/).
2. Click **New +** -> **Web Service**.
3. Connect your **GitHub Repository**.
4. **Name**: `ecommerce-api` (or similar).
5. **Runtime**: **Docker**.
6. **Instance Type**: **Free**.
7. Scroll down to **Environment Variables** and add the following:

| Key | Value (Example) |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://ep-xyz.aws.neon.tech/neondb?sslmode=require` |
| `DB_USER` | (from Neon) |
| `DB_PASSWORD` | (from Neon) |
| `KAFKA_BROKER` | `finer-gull-1234-us1-kafka.upstash.io:9092` |
| `KAFKA_SECURITY_PROTOCOL` | `SASL_SSL` |
| `KAFKA_SASL_MECHANISM` | `SCRAM-SHA-256` |
| `KAFKA_JAAS_CONFIG` | `org.apache.kafka.common.security.scram.ScramLoginModule required username='<UPSTASH_USER>' password='<UPSTASH_PASS>';` |
| `REDIS_HOST` | `restless-deer-1234.upstash.io` |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | `<UPSTASH_REDIS_PASS>` |
| `REDIS_SSL_ENABLED` | `true` |

8. Click **Create Web Service**.

---

## Step 5: Verify
1. Wait for the build to finish (it might take a few minutes).
2. Once deployed, Render will verify the service is healthy.
3. Open the **URL** provided by Render + `/swagger-ui.html` to see your API.
   - Example: `https://ecommerce-api.onrender.com/swagger-ui.html`
