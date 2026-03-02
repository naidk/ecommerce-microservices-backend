# =============================================================
#  setup-aws-eb.ps1
#  Automates AWS Elastic Beanstalk Infrastructure Setup
#  Run: .\setup-aws-eb.ps1
# =============================================================

$config = @{
    # ── AWS Region & Credentials ──────────────────────────────────────────
    # These are picked up from your local environment/profile
    AWS_REGION            = "us-east-1"
    
    # App / Environment names (must match .elasticbeanstalk/config.yml)
    APP_NAME              = "ecommerce-api"
    ENV_NAME              = "ecommerce-api-prod"

    # ── Fill these in from your service providers ──────────────────────────
    DB_URL                = "<NEON_JDBC_URL>"
    DB_USER               = "<NEON_USER>"
    DB_PASSWORD           = "<NEON_PASSWORD>"

    KAFKA_BROKER          = "<AIVEN_KAFKA_BROKER>"
    KAFKA_SECURITY_PROTOCOL = "SASL_SSL"
    KAFKA_SASL_MECHANISM  = "SCRAM-SHA-256"
    KAFKA_JAAS_CONFIG     = "org.apache.kafka.common.security.scram.ScramLoginModule required username='avnadmin' password='<AIVEN_PASSWORD>';"

    REDIS_HOST            = "<UPSTASH_REDIS_HOST>"
    REDIS_PORT            = "6379"
    REDIS_PASSWORD        = "<UPSTASH_PASSWORD>"
    REDIS_SSL_ENABLED     = "true"

    AWS_ACCESS_KEY_ID     = "<YOUR_AWS_ACCESS_KEY_ID>"
    AWS_SECRET_ACCESS_KEY = "<YOUR_AWS_SECRET_ACCESS_KEY>"
    AWS_S3_BUCKET_NAME    = "naidu-ecommerce-products-images-v1"
    AWS_SES_SENDER        = "naidugudivada766@gmail.com"
    JWT_SECRET            = "MySuperSecretKeyForJwtSigningShouldBeLongEnough1234567890"

    EB_URL                = ""  # Will be filled automatically after env creation
}

# ── Helpers ────────────────────────────────────────────────────────────────
function Log($msg) { Write-Output "`n>>> $msg" }
function Ok($msg)  { Write-Output "    OK: $msg" }
function Err($msg) { Write-Output "    ERR: $msg" }

function Check-Placeholder($key, $value) {
    if ($value -match "<.+>") {
        Err "Config '$key' still has a placeholder value: $value"
        Err "Please edit this script and fill in your real credentials before running."
        exit 1
    }
}

# ── Validate no placeholders remain ───────────────────────────────────────
Log "Validating configuration..."
$skipKeys = @("EB_URL")
foreach ($key in $config.Keys) {
    if ($key -notin $skipKeys) { Check-Placeholder $key $config[$key] }
}
Ok "All credentials filled in"

# ── Configure AWS CLI ─────────────────────────────────────────────────────
Log "Configuring AWS CLI..."
aws configure set aws_access_key_id $config.AWS_ACCESS_KEY_ID
aws configure set aws_secret_access_key $config.AWS_SECRET_ACCESS_KEY
aws configure set default.region $config.AWS_REGION

$identity = aws sts get-caller-identity 2>&1
if ($LASTEXITCODE -ne 0) { Err "AWS credentials are invalid. Check your keys."; exit 1 }
Ok "AWS credentials verified"

# ── Step 1: Create IAM Service Role ───────────────────────────────────────
Log "Step 1: Setting up IAM roles..."

# Check if service role exists
$roleExists = aws iam get-role --role-name "aws-elasticbeanstalk-service-role" 2>&1
if ($LASTEXITCODE -ne 0) {
    # Use a temp file for the policy document to avoid PowerShell escaping issues
    $policyFile = Join-Path $env:TEMP "eb-service-role-policy.json"
    '{
        "Version":"2012-10-17",
        "Statement":[{"Effect":"Allow","Principal":{"Service":"elasticbeanstalk.amazonaws.com"},"Action":"sts:AssumeRole"}]
    }' | Set-Content -Path $policyFile -Encoding UTF8

    aws iam create-role `
        --role-name "aws-elasticbeanstalk-service-role" `
        --assume-role-policy-document "file://$policyFile" | Out-Null
    
    Remove-Item $policyFile -ErrorAction SilentlyContinue
    
    Log "    Waiting for service role to propagate..."
    Start-Sleep -Seconds 10

    aws iam attach-role-policy `
        --role-name "aws-elasticbeanstalk-service-role" `
        --policy-arn "arn:aws:iam::aws:policy/service-role/AWSElasticBeanstalkEnhancedHealth" | Out-Null
    aws iam attach-role-policy `
        --role-name "aws-elasticbeanstalk-service-role" `
        --policy-arn "arn:aws:iam::aws:policy/AWSElasticBeanstalkManagedUpdatesCustomerRolePolicy" | Out-Null
    Ok "Service role created"
} else {
    Ok "Service role already exists"
}

# Check if instance profile exists
$profileExists = aws iam get-instance-profile --instance-profile-name "aws-elasticbeanstalk-ec2-role" 2>&1
if ($LASTEXITCODE -ne 0) {
    # Use a temp file for the policy document
    $policyFile = Join-Path $env:TEMP "eb-ec2-role-policy.json"
    '{
        "Version":"2012-10-17",
        "Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]
    }' | Set-Content -Path $policyFile -Encoding UTF8

    aws iam create-role `
        --role-name "aws-elasticbeanstalk-ec2-role" `
        --assume-role-policy-document "file://$policyFile" | Out-Null

    Remove-Item $policyFile -ErrorAction SilentlyContinue

    Log "    Waiting for EC2 role to propagate..."
    Start-Sleep -Seconds 10

    aws iam attach-role-policy `
        --role-name "aws-elasticbeanstalk-ec2-role" `
        --policy-arn "arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier" | Out-Null
    aws iam attach-role-policy `
        --role-name "aws-elasticbeanstalk-ec2-role" `
        --policy-arn "arn:aws:iam::aws:policy/AWSElasticBeanstalkMulticontainerDocker" | Out-Null
    aws iam create-instance-profile `
        --instance-profile-name "aws-elasticbeanstalk-ec2-role" | Out-Null
    
    Start-Sleep -Seconds 5
    
    aws iam add-role-to-instance-profile `
        --instance-profile-name "aws-elasticbeanstalk-ec2-role" `
        --role-name "aws-elasticbeanstalk-ec2-role" | Out-Null
    Ok "EC2 instance profile created"
} else {
    Ok "EC2 instance profile already exists"
}

# ── Step 2: Create EB Application ─────────────────────────────────────────
Log "Step 2: Creating Elastic Beanstalk application: $($config.APP_NAME)..."
$appExists = aws elasticbeanstalk describe-applications `
    --application-names $config.APP_NAME `
    --query "Applications[0].ApplicationName" --output text 2>&1

if ($appExists -eq $config.APP_NAME) {
    Ok "Application '$($config.APP_NAME)' already exists"
} else {
    aws elasticbeanstalk create-application `
        --application-name $config.APP_NAME `
        --description "Ecommerce API - Spring Boot + Kafka" | Out-Null
    Ok "Application '$($config.APP_NAME)' created"
}

# ── Step 3: Create EB Environment ─────────────────────────────────────────
Log "Step 3: Creating Elastic Beanstalk environment: $($config.ENV_NAME)..."

$envExists = aws elasticbeanstalk describe-environments `
    --application-name $config.APP_NAME `
    --environment-names $config.ENV_NAME `
    --query "Environments[0].Status" --output text 2>&1

if ($envExists -ne "None" -and $envExists -ne $null -and $envExists -notmatch "Error") {
    Ok "Environment '$($config.ENV_NAME)' already exists (status: $envExists)"
} else {
    # Build option settings hash (without ConvertTo-Json yet)
    $optionSettings = @(
        @{ Namespace="aws:autoscaling:launchconfiguration"; OptionName="IamInstanceProfile"; Value="aws-elasticbeanstalk-ec2-role" }
        @{ Namespace="aws:elasticbeanstalk:environment"; OptionName="ServiceRole"; Value="aws-elasticbeanstalk-service-role" }
        @{ Namespace="aws:elasticbeanstalk:environment"; OptionName="EnvironmentType"; Value="SingleInstance" }
        @{ Namespace="aws:ec2:instances"; OptionName="InstanceTypes"; Value="t3.micro" }
        @{ Namespace="aws:elasticbeanstalk:healthreporting:system"; OptionName="SystemType"; Value="enhanced" }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="SERVER_PORT"; Value="8080" }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="DB_URL"; Value=$config.DB_URL }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="DB_USER"; Value=$config.DB_USER }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="DB_PASSWORD"; Value=$config.DB_PASSWORD }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="KAFKA_BROKER"; Value=$config.KAFKA_BROKER }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="KAFKA_SECURITY_PROTOCOL"; Value=$config.KAFKA_SECURITY_PROTOCOL }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="KAFKA_SASL_MECHANISM"; Value=$config.KAFKA_SASL_MECHANISM }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="KAFKA_JAAS_CONFIG"; Value=$config.KAFKA_JAAS_CONFIG }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="REDIS_HOST"; Value=$config.REDIS_HOST }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="REDIS_PORT"; Value=$config.REDIS_PORT }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="REDIS_PASSWORD"; Value=$config.REDIS_PASSWORD }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="REDIS_SSL_ENABLED"; Value=$config.REDIS_SSL_ENABLED }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="AWS_ACCESS_KEY_ID"; Value=$config.AWS_ACCESS_KEY_ID }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="AWS_SECRET_ACCESS_KEY"; Value=$config.AWS_SECRET_ACCESS_KEY }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="AWS_REGION"; Value=$config.AWS_REGION }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="AWS_S3_BUCKET_NAME"; Value=$config.AWS_S3_BUCKET_NAME }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="AWS_SES_SENDER"; Value=$config.AWS_SES_SENDER }
        @{ Namespace="aws:elasticbeanstalk:application:environment"; OptionName="JWT_SECRET"; Value=$config.JWT_SECRET }
    )

    # Write option settings to a temporary JSON file to avoid PowerShell CLI escaping hell
    $optionsFile = Join-Path $env:TEMP "eb-options.json"
    $optionSettings | ConvertTo-Json -Depth 3 | Set-Content -Path $optionsFile -Encoding UTF8

    # Get Docker solution stack
    $solutionStack = aws elasticbeanstalk list-available-solution-stacks `
        --query "SolutionStacks[?contains(@,'Docker') && contains(@,'Amazon Linux 2023')]" `
        --output text | ForEach-Object { $_ -split "`t" } | Where-Object { $_ -match "Docker" } | Select-Object -Last 1

    if (-not $solutionStack) {
        # Fallback to a known version if list fails
        $solutionStack = "64bit Amazon Linux 2023 v4.10.0 running Docker"
    }

    Write-Output "    Using platform: $solutionStack"

    $createResult = aws elasticbeanstalk create-environment `
        --application-name $config.APP_NAME `
        --environment-name $config.ENV_NAME `
        --solution-stack-name $solutionStack `
        --option-settings "file://$optionsFile" 2>&1

    Remove-Item $optionsFile -ErrorAction SilentlyContinue

    if ($LASTEXITCODE -ne 0) { 
        Err "Failed to create environment: $createResult"
        exit 1 
    }
    Ok "Environment '$($config.ENV_NAME)' creation started"
}

# ── Wait for DNS ──────────────────────────────────────────────────────────
Log "Waiting for environment to be Green and get endpoint..."
$envUrl = ""
while ($true) {
    $envData = aws elasticbeanstalk describe-environments `
        --application-name $config.APP_NAME `
        --environment-names $config.ENV_NAME `
        --query "Environments[0].[Status, Health, CNAME]" `
        --output text 2>&1

    if ($envData -match "Ready") {
        $parts = $envData -split "`t"
        $envUrl = $parts[2]
        if ($envUrl) { break }
    }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 10
}

Ok "Environment is Ready!"
Ok "URL: http://$envUrl"

# Save URL for next steps
"http://$envUrl" | Set-Content -Path ".eb_url.txt"
Ok "URL saved to .eb_url.txt"
