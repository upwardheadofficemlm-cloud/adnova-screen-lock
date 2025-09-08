# 🚀 Deployment Commands

## After creating the GitHub repository, run these commands:

### 1. Push to GitHub
```bash
git push -u origin main
```

### 2. Verify the push
```bash
git remote -v
```

### 3. Check repository status
```bash
git status
```

## 🌐 Vercel Deployment Steps

### Option A: Web Interface (Recommended)
1. Go to [vercel.com](https://vercel.com)
2. Sign up/Login with GitHub
3. Click "New Project"
4. Import repository: `adnova-screen-lock`
5. Configure:
   - **Root Directory**: `website`
   - **Framework Preset**: Other
   - **Build Command**: (leave empty)
   - **Output Directory**: (leave empty)
6. Click "Deploy"

### Option B: Vercel CLI
```bash
# Install Vercel CLI
npm install -g vercel

# Login to Vercel
vercel login

# Deploy
vercel

# Follow prompts:
# - Set up and deploy? Yes
# - Which scope? Your account
# - Link to existing project? No
# - Project name: adnova-screen-lock
# - Directory: website
# - Override settings? No
```

## 📱 Your Live URLs
- **Website**: https://adnova-screen-lock.vercel.app
- **Admin Dashboard**: https://adnova-screen-lock.vercel.app/admin-dashboard.html
- **GitHub Repository**: https://github.com/upwardheadofficemlm-cloud/adnova-screen-lock

## 🔐 Admin Credentials
- **Email**: admin@upwardmm.com
- **Password**: Upward103999@@
