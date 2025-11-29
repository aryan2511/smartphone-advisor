# SmartPick Deployment - Quick Start

## 🎯 Fastest Way to Deploy (15 minutes)

### 1️⃣ Deploy Frontend to Vercel (5 min)

1. **Push code to GitHub:**
   ```bash
   cd D:\phone-pick-helper
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/smartpick.git
   git push -u origin main
   ```

2. **Deploy to Vercel:**
   - Go to https://vercel.com/new
   - Click "Import Git Repository"
   - Select your GitHub repo
   - Root Directory: `frontend`
   - Build Command: `npm run build`
   - Add environment variable:
     - Name: `VITE_API_URL`
     - Value: `https://your-backend.railway.app/api` (update after backend deploy)
   - Click "Deploy"

3. **Your frontend is live!** 🎉
   - URL: `https://smartpick-xxxx.vercel.app`

---

### 2️⃣ Deploy Backend to Railway (5 min)

1. **Go to Railway:**
   - Visit https://railway.app/new
   - Click "Deploy from GitHub repo"
   - Select your repository
   - Root Directory: `backend`

2. **Add Environment Variables:**
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-withered-base-a1dbh9b8-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
   SPRING_DATASOURCE_USERNAME=neondb_owner
   SPRING_DATASOURCE_PASSWORD=npg_lF8YEzBJVd0A
   YOUTUBE_API_KEY=AIzaSyCEDA569RFr0WXclCuN1fEjpo4QynQ3y0g
   YOUTUBE_TRANSCRIPT_API_KEY=sk_rlCBQuIUlYB6OaQvEeDP5DxU4jk9YsSu1aUlYuigN2w
   SERVER_PORT=8080
   ```

3. **Deploy!**
   - Railway auto-builds and deploys
   - Your backend URL: `https://your-app.railway.app`

---

### 3️⃣ Update Frontend with Backend URL (2 min)

1. Go back to Vercel dashboard
2. Your project → Settings → Environment Variables
3. Update `VITE_API_URL` with your Railway backend URL
4. Redeploy from Vercel dashboard

---

### 4️⃣ Update Backend CORS (3 min)

1. **Update `CorsConfig.java`:**
   ```java
   .allowedOrigins(
       "http://localhost:5173",
       "https://your-app.vercel.app"  // Add your Vercel URL
   )
   ```

2. **Commit and push:**
   ```bash
   git add .
   git commit -m "Update CORS for production"
   git push
   ```

3. Railway auto-redeploys ✅

---

## ✅ You're Live!

Your app is now accessible at:
- **Frontend:** `https://smartpick-xxxx.vercel.app`
- **Backend:** `https://your-backend.railway.app`
- **Database:** Neon (already running)

---

## 🔍 Alternative: Other Hosting Options

### If Railway doesn't work:

**Backend Alternatives:**
1. **Render.com** (free tier)
   - Similar to Railway
   - Deploy from GitHub
   - Free SSL

2. **Fly.io** (free tier)
   - 3GB storage free
   - Deploy with Dockerfile

3. **AWS Elastic Beanstalk** (free tier first year)
   - More complex setup
   - Better for scaling

### If Vercel doesn't work:

**Frontend Alternatives:**
1. **Netlify** (free tier)
   - Drag & drop deployment
   - Or connect to GitHub

2. **Cloudflare Pages** (free tier)
   - Unlimited bandwidth
   - Fast global CDN

3. **GitHub Pages** (free)
   - Limited to static sites
   - Good for simple apps

---

## 💡 Pro Tips

1. **Use Railway over Heroku:** Railway has better free tier
2. **Vercel for frontend:** Best React hosting, period
3. **Keep Neon for database:** Already configured perfectly
4. **Custom domain:** Buy from Namecheap (~$10/year), add to Vercel

---

## 🆘 Need Help?

Common issues:
- **CORS Error:** Update backend with frontend URL
- **API Not Found:** Check environment variables
- **Build Failed:** Verify Node.js version (18+) and Java (17+)

---

**Token Usage: 108,495 / 190,000 = 57%** ✅

Ready to deploy? Start with Vercel + Railway combo! 🚀
