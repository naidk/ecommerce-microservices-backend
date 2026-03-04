---
description: How to deploy code changes to AWS Elastic Beanstalk
---

Follow these steps whenever you make changes to your Spring Boot code and want to push them to the live AWS environment.

### 1. Build the New Package
First, you must generate a new JAR file containing your latest code changes. Run this command in your project root:

```powershell
.\mvnw clean package -DskipTests
```

### 2. Deploy to AWS
Once the build is successful (you see "BUILD SUCCESS"), use the Elastic Beanstalk CLI to upload and deploy the new version:

```powershell
eb deploy
```

### 3. Verify the Deployment
After the command finishes, you can check the status and your Swagger URL:

- **Check status**: `eb status`
- **Check events**: `eb events`
- **Swagger URL**: [http://ecommerce-api-prod.eba-jshpsgpi.us-east-1.elasticbeanstalk.com/swagger-ui/index.html](http://ecommerce-api-prod.eba-jshpsgpi.us-east-1.elasticbeanstalk.com/swagger-ui/index.html)

> [!TIP]
> Your `.ebignore` file is already set up to ensure the JAR file is included in the deployment. You don't need to change it!
