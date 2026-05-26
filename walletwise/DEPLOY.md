# Beginner Deployment Guide for WalletWise

Follow these steps exactly. You do not need to install MySQL because this final version runs with H2 by default. You can add MySQL later if you want.

## 1. Extract the ZIP

Right click the ZIP and extract it. You should see a folder named `walletwise`.

## 2. Upload to GitHub

Open GitHub, create a new public repository, for example `walletwise-personal-finance-manager`.

Then open terminal inside the `walletwise` folder and run:

```bash
git init
git add .
git commit -m "Final WalletWise personal finance manager"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
git push -u origin main
```

Replace `YOUR_USERNAME` and `YOUR_REPO_NAME` with your real GitHub details.

## 3. Deploy on Render

1. Go to https://render.com
2. Sign in with GitHub.
3. Click **New +**.
4. Click **Web Service**.
5. Select your GitHub repo.
6. Use these settings:

```text
Name: walletwise
Runtime: Java
Branch: main
Root Directory: leave blank
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/pfm-1.0.0.jar
```

7. Add this environment variable:

```text
JAVA_VERSION = 17
```

8. Click **Create Web Service**.

Render will take a few minutes. When it is done, your API URL will look like:

```text
https://walletwise-xxxx.onrender.com
```

Your base API URL for the company script is:

```text
https://walletwise-xxxx.onrender.com/api
```

## 4. Check if deployment is alive

Open these in browser:

```text
https://walletwise-xxxx.onrender.com/
https://walletwise-xxxx.onrender.com/health
https://walletwise-xxxx.onrender.com/api
```

If `/health` shows `UP`, the app is running.

## 5. Run the assignment test script

From the folder where `financial_manager_tests.sh` is saved, run:

```bash
bash financial_manager_tests.sh https://walletwise-xxxx.onrender.com/api
```

Replace the URL with your Render URL.

## 6. Optional MySQL / Railway setup

The project works without MySQL. If you want MySQL, create a Railway MySQL database and add these Render environment variables:

```text
DB_URL=jdbc:mysql://HOST:PORT/railway
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
JAVA_VERSION=17
```

Then redeploy on Render.

## 7. What to submit

Submit:

1. GitHub repository link
2. Render deployed API URL ending with `/api`
3. Screenshot of the test script result

Example:

```text
GitHub: https://github.com/YOUR_USERNAME/walletwise-personal-finance-manager
API URL: https://walletwise-xxxx.onrender.com/api
```
