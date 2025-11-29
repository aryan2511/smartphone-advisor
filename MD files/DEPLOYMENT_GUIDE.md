# Deployment Checklist for SmartPick

## ✅ Pre-Deployment Tasks

### 1. Frontend Preparation
- [ ] Update API base URL to use environment variable
- [ ] Remove any console.logs
- [ ] Test production build locally
- [ ] Add .env.production file

### 2. Backend Preparation  
- [ ] Update CORS to allow production frontend domain
- [ ] Set up environment variables
- [ ] Remove development-only code
- [ ] Test with production database

### 3. Database
- [ ] Verify all data is imported
- [ ] Check indexes for performance
- [ ] Backup database

---

## 🌐 Deployment Steps

### FRONTEND (React) → Vercel

**Step 1: Create Vercel Account**
1. Go to https://vercel.com
2. Sign up with GitHub
3. Import your repository

**Step 2: Configure Build**
```bash
# Vercel will auto-detect these:
Build Command: npm run build
Output Directory: dist
Install Command: npm install
```

**Step 3: Add Environment Variables in Vercel**
```
VITE_API_URL=https://your-backend-url.railway.app/api
```

**Step 4: Deploy**
- Push to GitHub main branch
- Vercel auto-deploys ✅

---

### BACKEND (Spring Boot) → Railway

**Step 1: Create Railway Account**
1. Go to https://railway.app
2. Sign up with GitHub
3. Create new project

**Step 2: Add Spring Boot Service**
1. Click "New Service" → "GitHub Repo"
2. Select your backend folder
3. Railway auto-detects Java/Maven

**Step 3: Add Environment Variables**
```
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-withered-base-a1dbh9b8-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=npg_lF8YEzBJVd0A
YOUTUBE_API_KEY=AIzaSyCEDA569RFr0WXclCuN1fEjpo4QynQ3y0g
YOUTUBE_TRANSCRIPT_API_KEY=sk_rlCBQuIUlYB6OaQvEeDP5DxU4jk9YsSu1aUlYuigN2w
```

**Step 4: Deploy**
- Railway builds & deploys automatically ✅

---

### DATABASE (Neon) - Already Set Up! ✅

Your Neon database is already production-ready:
- Connection pooling enabled
- SSL encryption enabled
- Located in AWS Singapore (ap-southeast-1)

**Action Required:**
1. Go to Neon dashboard
2. Note your connection string
3. Use it in Railway environment variables

---

## 🔧 Required Code Changes

### 1. Frontend - Use Environment Variables

**Create: `frontend/.env.production`**
```
VITE_API_URL=https://your-backend.railway.app/api
```

**Update: `frontend/src/services/api.js`**
```javascript
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
```

### 2. Backend - Update CORS

**Update: `CorsConfig.java`**
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",  // Local development
                "https://smartpick.vercel.app"  // Production
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
}
```

### 3. Backend - Environment-Based Config

**Update: `application.properties`**
```properties
# Use environment variables in production
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/phones}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}
```

---

## 🧪 Testing Before Production

### Local Production Build Test

**Frontend:**
```bash
cd frontend
npm run build
npm run preview  # Test production build
```

**Backend:**
```bash
cd backend
mvn clean package
java -jar target/advisor-0.0.1-SNAPSHOT.jar
```

---

## 📊 Post-Deployment

### 1. Update DNS (Optional - Custom Domain)
- Buy domain from Namecheap/GoDaddy (~$10/year)
- Point to Vercel in DNS settings
- Add domain in Vercel dashboard

### 2. Monitor Performance
- Vercel Analytics (free tier)
- Railway logs & metrics
- Neon database metrics

### 3. Set Up CI/CD
Already done! ✅
- Push to GitHub → Auto-deploys to Vercel & Railway

---

## 💰 Cost Breakdown

### Free Tier Limits:
- **Vercel:** 100GB bandwidth, unlimited deploys
- **Railway:** $5 credit/month (~500 hours uptime)
- **Neon:** 3GB storage, 3 compute hours/day

### When to Upgrade:
- Traffic >100K visitors/month → Vercel Pro
- Backend needs 24/7 uptime → Railway Pro
- Database >3GB → Neon Pro

---

## 🆘 Troubleshooting

### Common Issues:

**1. CORS Errors**
- Add production frontend URL to backend CORS config
- Restart backend after changes

**2. Database Connection Failed**
- Verify Neon connection string in Railway
- Check SSL mode is `require`

**3. Build Failures**
- Check Node version (Railway needs 18+)
- Verify all dependencies in package.json
- Check Java version (needs 17+)

**4. API Not Found**
- Verify environment variable `VITE_API_URL`
- Check Railway deployment logs
- Ensure backend is running

---

## ✅ Final Checklist

Before going live:
- [ ] Test all user flows end-to-end
- [ ] Verify all 68 phones display correctly
- [ ] Test on mobile devices
- [ ] Check affiliate links work
- [ ] Test with different budget ranges
- [ ] Verify sentiment scores display
- [ ] Test with slow internet (throttling)
- [ ] Add loading states everywhere
- [ ] Add error boundaries
- [ ] Set up error tracking (Sentry - free tier)

---

## 🚀 Ready to Deploy?

Once checklist is complete:
1. Push frontend to GitHub
2. Connect to Vercel
3. Push backend to GitHub  
4. Connect to Railway
5. Test production URLs
6. Share with users! 🎉

---

**Estimated Time:** 1-2 hours for first deployment
**Difficulty:** Easy (mostly clicking through dashboards)
