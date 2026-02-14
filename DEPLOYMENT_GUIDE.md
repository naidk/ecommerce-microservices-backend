# Free Tier Deployment Guide

This guide will help you deploy your E-Commerce API to **Render** (Free Tier) using **Aiven** for Kafka, **Upstash** for Redis, and **Neon** for PostgreSQL.

## Prerequisites
- [GitHub Account](https://github.com/) (to host your code)
- [Render Account](https://render.com/)
- [Upstash Account](https://upstash.com/) (for Redis)
- [Neon Account](https://neon.tech/) (for Database)
- [Aiven Account](https://aiven.io/) (for Kafka)

---

## Step 1: Push Code to GitHub
Ensure this project is in a GitHub repository.
```bash
git add .
git commit -m "Update deployment guide for Aiven"
git push origin main
```

---

## Step 2: Set up Database (Neon.tech)
1. Log in to [Neon Console](https://console.neon.tech/).
2. Create a **New Project**.
3. Copy the **Connection String** (Postgres URL). It looks like:
   `postgres://user:password@ep-xyz.aws.neon.tech/neondb?sslmode=require`

---

## Step 3: Set up Kafka (Aiven)
1. Log in to [Aiven Console](https://console.aiven.io/).
2. Click **Create Service**.
3. Select **Apache Kafka**.
4. Important: Select **Free Tier** (available in specific regions, look for the "Free Plan" label).
5. Give it a name (e.g., `ecommerce-kafka`) and create it.
6. Once running, go to the **Overview** tab.
7. **Copy Connection Info:**
   - **Service URI**: This acts as your Bootstrap Server (copy the host:port part).
   - **User**: `avnadmin` (default).
   - **Password**: Copy the password.
8. **Enable SASL/SCRAM:**
   - Aiven defaults to SSL certificates sometimes. Ensure you can use **SASL/SCRAM-SHA-256** or **PLAIN**.
   - If using the "Free Plan", you usually get `SASL_SSL` + `SCRAM-SHA-256` out of the box.

---

## Step 4: Set up Redis (Upstash)
1. Log in to [Upstash Console](https://console.upstash.com/).
2. Create a **Redis Database** (Free Tier).
3. In the **Details** tab, copy:
   - **Endpoint** (Host)
   - **Port** (usually `6379`)
   - **Password**

---

## Step 5: Deploy to Render
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
| `KAFKA_BROKER` | `kafka-123.aivencloud.com:12345` |
| `KAFKA_SECURITY_PROTOCOL` | `SASL_SSL` |
| `KAFKA_SASL_MECHANISM` | `SCRAM-SHA-256` |
| `KAFKA_JAAS_CONFIG` | `org.apache.kafka.common.security.scram.ScramLoginModule required username='avnadmin' password='<AIVEN_PASSWORD>';` |
| `REDIS_HOST` | `restless-deer-1234.upstash.io` |
| `REDIS_PORT` | `6379` |
| `REDIS_PASSWORD` | `<UPSTASH_REDIS_PASS>` |
| `REDIS_SSL_ENABLED` | `true` |

8. Click **Create Web Service**.

---

## Step 6: Verify
1. Wait for the build to finish (it might take a few minutes).
2. Once deployed, Render will verify the service is healthy.
3. Open the **URL** provided by Render + `/swagger-ui.html` to see your API.
   - Example: `https://ecommerce-api.onrender.com/swagger-ui.html`
