# Deployment Guide

This guide will help you deploy your LeetCode reminder service so it can be accessed by the GitHub Actions workflow.

## Option 1: Deploy to Railway (Recommended for Free Tier)

### Prerequisites
- GitHub account
- Railway account (free tier available)

### Steps
1. **Fork/Clone your repository** to GitHub if not already done
2. **Go to [Railway](https://railway.app)** and sign in with GitHub
3. **Click "New Project"** → "Deploy from GitHub repo"
4. **Select your repository** and click "Deploy Now"
5. **Add environment variables**:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
6. **Wait for deployment** (usually 2-5 minutes)
7. **Copy the generated URL** (e.g., `https://your-app-name.railway.app`)
8. **Use this URL** as your `LEETCODE_MS_BASE_URL` in GitHub secrets

## Option 2: Deploy to Render (Free Tier)

### Steps
1. **Go to [Render](https://render.com)** and sign in
2. **Click "New"** → "Web Service"
3. **Connect your GitHub repository**
4. **Configure the service**:
   - **Name**: `leetcode-reminder-service`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package`
   - **Start Command**: `java -jar target/Daily-Questions-MS-0.0.1-SNAPSHOT.jar`
5. **Add environment variables**:
   ```
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
6. **Click "Create Web Service"**
7. **Wait for deployment** and copy the URL
8. **Use this URL** in your GitHub secrets

## Option 3: Deploy to Heroku

### Prerequisites
- Heroku account
- Heroku CLI installed

### Steps
1. **Install Heroku CLI**:
   ```bash
   # macOS
   brew tap heroku/brew && brew install heroku
   
   # Windows
   # Download from https://devcenter.heroku.com/articles/heroku-cli
   ```

2. **Login to Heroku**:
   ```bash
   heroku login
   ```

3. **Create Heroku app**:
   ```bash
   heroku create your-app-name
   ```

4. **Set environment variables**:
   ```bash
   heroku config:set GEMINI_API_KEY=your_gemini_api_key_here
   ```

5. **Deploy**:
   ```bash
   git push heroku main
   ```

6. **Open the app**:
   ```bash
   heroku open
   ```

## Option 4: Deploy to Your Own Server

### Prerequisites
- VPS or server with Java 17+
- Domain name (optional)

### Steps
1. **Build the JAR file**:
   ```bash
   ./mvnw clean package
   ```

2. **Upload to server**:
   ```bash
   scp target/Daily-Questions-MS-0.0.1-SNAPSHOT.jar user@your-server:/path/to/app/
   ```

3. **Create systemd service** (Linux):
   ```bash
   sudo nano /etc/systemd/system/leetcode-reminder.service
   ```

   Add this content:
   ```ini
   [Unit]
   Description=LeetCode Reminder Service
   After=network.target

   [Service]
   Type=simple
   User=your-username
   WorkingDirectory=/path/to/app
   ExecStart=/usr/bin/java -jar Daily-Questions-MS-0.0.1-SNAPSHOT.jar
   Environment=GEMINI_API_KEY=your_gemini_api_key_here
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```

4. **Enable and start the service**:
   ```bash
   sudo systemctl enable leetcode-reminder
   sudo systemctl start leetcode-reminder
   ```

5. **Configure firewall** to allow port 8080

## Option 5: Use Docker (Advanced)

### Create Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/Daily-Questions-MS-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### Build and Run
```bash
# Build
docker build -t leetcode-reminder .

# Run
docker run -p 8080:8080 -e GEMINI_API_KEY=your_key leetcode-reminder
```

## Environment Variables

Make sure to set these environment variables in your deployment:

| Variable | Description | Required |
|----------|-------------|----------|
| `GEMINI_API_KEY` | Your Google Gemini API key | Yes |
| `SERVER_PORT` | Port to run on (default: 8080) | No |

## Testing Your Deployment

After deployment, test your endpoints:

```bash
# Health check
curl https://your-app-url/api/daily-question/health

# Check user status
curl https://your-app-url/api/daily-question/check-user-status/Abdul1028

# Get daily question
curl https://your-app-url/api/daily-question
```

## Troubleshooting Deployment

### Common Issues

1. **Port binding errors**: Ensure the port is available and not blocked by firewall
2. **Memory issues**: Java apps need sufficient memory (at least 512MB)
3. **API key errors**: Verify your Gemini API key is correct
4. **Network timeouts**: Check if your service can reach LeetCode and Gemini APIs

### Debugging

1. **Check logs** in your deployment platform
2. **Test locally** first to ensure everything works
3. **Verify environment variables** are set correctly
4. **Check network connectivity** from your deployment environment

## Security Considerations

1. **Never commit API keys** to your repository
2. **Use environment variables** for sensitive information
3. **Enable HTTPS** for production deployments
4. **Set up monitoring** to detect unusual activity

## Cost Optimization

### Free Tier Options
- **Railway**: $5/month free tier
- **Render**: Free tier available
- **Heroku**: Free tier available (with limitations)

### Paid Options
- **AWS/GCP/Azure**: Pay-as-you-use, very cost-effective for low traffic
- **DigitalOcean**: $5/month droplets
- **Linode**: $5/month instances

---

**Choose the deployment option that best fits your needs and budget! 🚀**
