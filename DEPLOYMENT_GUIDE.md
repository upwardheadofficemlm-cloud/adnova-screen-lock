# 🚀 AdNova Screen Lock - Deployment Guide

This guide will help you deploy your AdNova Screen Lock project to GitHub and Vercel.

## 📋 Prerequisites

- GitHub account
- Vercel account (free)
- Git installed on your system
- Node.js installed (for Vercel CLI)

## 🔧 Step 1: Create GitHub Repository

### Option A: Using GitHub Web Interface
1. Go to [GitHub.com](https://github.com) and sign in
2. Click the "+" icon in the top right corner
3. Select "New repository"
4. Fill in the details:
   - **Repository name**: `adnova-screen-lock`
   - **Description**: `Professional Android app for digital signage screen locking with Firebase backend`
   - **Visibility**: Public (recommended for open source)
   - **Initialize**: Don't check any boxes (we already have files)
5. Click "Create repository"

### Option B: Using GitHub CLI (if installed)
```bash
gh repo create adnova-screen-lock --public --description "Professional Android app for digital signage screen locking with Firebase backend"
```

## 📤 Step 2: Push to GitHub

Run these commands in your project directory:

```bash
# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/adnova-screen-lock.git

# Push to GitHub
git push -u origin main
```

## 🌐 Step 3: Deploy to Vercel

### Option A: Using Vercel Web Interface (Recommended)

1. **Go to Vercel**
   - Visit [vercel.com](https://vercel.com)
   - Sign up/Login with your GitHub account

2. **Import Project**
   - Click "New Project"
   - Select "Import Git Repository"
   - Choose your `adnova-screen-lock` repository
   - Click "Import"

3. **Configure Project**
   - **Project Name**: `adnova-screen-lock`
   - **Framework Preset**: Other
   - **Root Directory**: `website` (this is important!)
   - **Build Command**: Leave empty
   - **Output Directory**: Leave empty

4. **Environment Variables** (Optional - already configured in vercel.json)
   - The Firebase configuration is already set in the `vercel.json` file
   - No additional environment variables needed

5. **Deploy**
   - Click "Deploy"
   - Wait for deployment to complete
   - Your site will be available at: `https://adnova-screen-lock.vercel.app`

### Option B: Using Vercel CLI

```bash
# Install Vercel CLI
npm install -g vercel

# Login to Vercel
vercel login

# Deploy from project directory
vercel

# Follow the prompts:
# - Set up and deploy? Yes
# - Which scope? Your account
# - Link to existing project? No
# - Project name: adnova-screen-lock
# - Directory: website
# - Override settings? No
```

## 🔗 Step 4: Custom Domain (Optional)

1. **In Vercel Dashboard**
   - Go to your project settings
   - Click "Domains"
   - Add your custom domain
   - Follow DNS configuration instructions

2. **Update Firebase Auth Domain**
   - Go to Firebase Console
   - Authentication → Settings → Authorized domains
   - Add your custom domain

## 📱 Step 5: Test Your Deployment

### Test the Website
1. Visit your Vercel URL
2. Test the homepage and features
3. Test admin login with:
   - **Email**: `admin@upwardmm.com`
   - **Password**: `Upward103999@@`

### Test Firebase Integration
1. Try logging into the admin dashboard
2. Check if Firebase services are working
3. Verify device management features

## 🔧 Step 6: Update Firebase Configuration

If you need to update Firebase settings:

1. **In Firebase Console**
   - Go to Project Settings
   - Add your Vercel domain to authorized domains
   - Update any configuration as needed

2. **In Vercel**
   - Go to Project Settings → Environment Variables
   - Update any Firebase configuration if needed

## 📊 Step 7: Monitor and Maintain

### Vercel Analytics
- Enable Vercel Analytics in your project dashboard
- Monitor website performance and usage

### GitHub Actions (Optional)
Create `.github/workflows/deploy.yml` for automatic deployments:

```yaml
name: Deploy to Vercel
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
          working-directory: ./website
```

## 🚨 Troubleshooting

### Common Issues

1. **Build Fails**
   - Check that `website` is set as root directory
   - Verify all files are in the website folder
   - Check Vercel build logs

2. **Firebase Not Working**
   - Verify Firebase configuration in `vercel.json`
   - Check browser console for errors
   - Ensure Firebase project is properly configured

3. **Admin Login Issues**
   - Verify admin user exists in Firebase Console
   - Check Firebase Authentication settings
   - Ensure domain is authorized in Firebase

4. **Styling Issues**
   - Check if CSS files are loading correctly
   - Verify file paths in HTML
   - Check browser developer tools

### Getting Help

- **Vercel Documentation**: [vercel.com/docs](https://vercel.com/docs)
- **Firebase Documentation**: [firebase.google.com/docs](https://firebase.google.com/docs)
- **GitHub Documentation**: [docs.github.com](https://docs.github.com)

## 🎉 Success!

Once deployed, your AdNova Screen Lock project will be available at:

- **Website**: `https://adnova-screen-lock.vercel.app`
- **Admin Dashboard**: `https://adnova-screen-lock.vercel.app/admin-dashboard.html`
- **GitHub Repository**: `https://github.com/YOUR_USERNAME/adnova-screen-lock`

## 📋 Deployment Checklist

- [ ] GitHub repository created
- [ ] Code pushed to GitHub
- [ ] Vercel project created
- [ ] Website deployed successfully
- [ ] Admin login working
- [ ] Firebase integration working
- [ ] Custom domain configured (optional)
- [ ] Analytics enabled (optional)
- [ ] Monitoring set up

---

**Your AdNova Screen Lock project is now live and ready for the world!** 🌟
