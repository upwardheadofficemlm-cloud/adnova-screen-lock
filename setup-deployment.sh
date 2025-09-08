#!/bin/bash

# AdNova Screen Lock - Deployment Setup Script
# This script helps you set up GitHub repository and Vercel deployment

echo "🚀 AdNova Screen Lock - Deployment Setup"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if git is initialized
if [ ! -d ".git" ]; then
    print_error "Git repository not initialized. Please run 'git init' first."
    exit 1
fi

# Check if we have commits
if ! git log --oneline -1 &> /dev/null; then
    print_error "No commits found. Please make an initial commit first."
    exit 1
fi

print_status "Git repository is ready!"

# Get GitHub username
echo ""
print_info "Let's set up your GitHub repository..."
read -p "Enter your GitHub username: " GITHUB_USERNAME

if [ -z "$GITHUB_USERNAME" ]; then
    print_error "GitHub username is required!"
    exit 1
fi

# Repository name
REPO_NAME="adnova-screen-lock"
REPO_URL="https://github.com/${GITHUB_USERNAME}/${REPO_NAME}.git"

echo ""
print_info "Repository details:"
echo "  Username: $GITHUB_USERNAME"
echo "  Repository: $REPO_NAME"
echo "  URL: $REPO_URL"

# Check if remote already exists
if git remote get-url origin &> /dev/null; then
    print_warning "Remote 'origin' already exists. Updating URL..."
    git remote set-url origin $REPO_URL
else
    print_info "Adding remote origin..."
    git remote add origin $REPO_URL
fi

print_status "Remote configured!"

# Push to GitHub
echo ""
print_info "Pushing to GitHub..."
if git push -u origin main; then
    print_status "Successfully pushed to GitHub!"
    echo ""
    print_info "Your repository is now available at:"
    echo "  🌐 https://github.com/${GITHUB_USERNAME}/${REPO_NAME}"
else
    print_error "Failed to push to GitHub. Please check your credentials and try again."
    echo ""
    print_info "You can manually push using:"
    echo "  git push -u origin main"
    exit 1
fi

# Vercel deployment instructions
echo ""
echo "🎉 GitHub setup complete!"
echo ""
print_info "Next steps for Vercel deployment:"
echo ""
echo "1. Go to https://vercel.com and sign up/login"
echo "2. Click 'New Project'"
echo "3. Import your GitHub repository: ${REPO_NAME}"
echo "4. Configure the project:"
echo "   - Framework Preset: Other"
echo "   - Root Directory: website"
echo "   - Build Command: (leave empty)"
echo "   - Output Directory: (leave empty)"
echo "5. Click 'Deploy'"
echo ""
print_info "Your website will be available at:"
echo "  🌐 https://${REPO_NAME}.vercel.app"
echo ""
print_info "Admin dashboard will be at:"
echo "  🔐 https://${REPO_NAME}.vercel.app/admin-dashboard.html"
echo ""
print_info "Admin credentials:"
echo "  📧 Email: admin@upwardmm.com"
echo "  🔑 Password: Upward103999@@"
echo ""

# Optional: Install Vercel CLI
read -p "Would you like to install Vercel CLI for easier deployment? (y/n): " INSTALL_VERCEL

if [[ $INSTALL_VERCEL =~ ^[Yy]$ ]]; then
    print_info "Installing Vercel CLI..."
    if command -v npm &> /dev/null; then
        npm install -g vercel
        print_status "Vercel CLI installed!"
        echo ""
        print_info "To deploy with CLI:"
        echo "  cd website"
        echo "  vercel"
    else
        print_warning "npm not found. Please install Node.js first."
    fi
fi

echo ""
print_status "Setup complete! 🎉"
echo ""
print_info "For detailed instructions, see DEPLOYMENT_GUIDE.md"
echo ""
print_info "Your project is now ready for deployment to Vercel!"
