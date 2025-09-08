#!/bin/bash

# AdNova Screen Lock Website Deployment Script
# This script helps deploy the website to various hosting platforms

echo "🚀 AdNova Screen Lock Website Deployment"
echo "========================================"

# Check if we're in the right directory
if [ ! -f "index.html" ]; then
    echo "❌ Error: index.html not found. Please run this script from the website directory."
    exit 1
fi

# Function to deploy to Firebase Hosting
deploy_firebase() {
    echo "🔥 Deploying to Firebase Hosting..."
    
    # Check if Firebase CLI is installed
    if ! command -v firebase &> /dev/null; then
        echo "❌ Firebase CLI not found. Installing..."
        npm install -g firebase-tools
    fi
    
    # Check if user is logged in
    if ! firebase projects:list &> /dev/null; then
        echo "🔐 Please login to Firebase..."
        firebase login
    fi
    
    # Initialize Firebase hosting if not already done
    if [ ! -f "firebase.json" ]; then
        echo "⚙️ Initializing Firebase hosting..."
        firebase init hosting --project adnova-screen-lock-90521
    fi
    
    # Deploy
    firebase deploy --only hosting
    echo "✅ Firebase deployment complete!"
}

# Function to deploy to GitHub Pages
deploy_github() {
    echo "🐙 Deploying to GitHub Pages..."
    
    # Check if git is initialized
    if [ ! -d ".git" ]; then
        echo "❌ Git repository not initialized. Please initialize git first."
        exit 1
    fi
    
    # Create gh-pages branch if it doesn't exist
    if ! git show-ref --verify --quiet refs/heads/gh-pages; then
        echo "📝 Creating gh-pages branch..."
        git checkout --orphan gh-pages
        git rm -rf .
        git add .
        git commit -m "Initial commit for GitHub Pages"
        git push origin gh-pages
    else
        echo "📝 Updating gh-pages branch..."
        git checkout gh-pages
        git merge main
        git push origin gh-pages
    fi
    
    echo "✅ GitHub Pages deployment complete!"
    echo "🌐 Your site will be available at: https://yourusername.github.io/your-repo-name"
}

# Function to create a simple HTTP server for testing
test_local() {
    echo "🧪 Starting local test server..."
    
    # Check if Python is available
    if command -v python3 &> /dev/null; then
        echo "🐍 Starting Python HTTP server on port 8000..."
        python3 -m http.server 8000
    elif command -v python &> /dev/null; then
        echo "🐍 Starting Python HTTP server on port 8000..."
        python -m SimpleHTTPServer 8000
    elif command -v node &> /dev/null; then
        echo "📦 Starting Node.js HTTP server on port 8000..."
        npx http-server -p 8000
    else
        echo "❌ No suitable HTTP server found. Please install Python or Node.js."
        exit 1
    fi
}

# Function to validate website files
validate() {
    echo "🔍 Validating website files..."
    
    # Check required files
    required_files=("index.html" "admin-dashboard.html" "styles.css" "script.js")
    
    for file in "${required_files[@]}"; do
        if [ -f "$file" ]; then
            echo "✅ $file found"
        else
            echo "❌ $file missing"
            exit 1
        fi
    done
    
    # Check if Firebase config is present in script.js
    if grep -q "adnova-screen-lock-90521" script.js; then
        echo "✅ Firebase configuration found"
    else
        echo "⚠️ Warning: Firebase configuration not found in script.js"
    fi
    
    echo "✅ Validation complete!"
}

# Function to show deployment options
show_options() {
    echo ""
    echo "📋 Deployment Options:"
    echo "1. Firebase Hosting (Recommended)"
    echo "2. GitHub Pages"
    echo "3. Test Locally"
    echo "4. Validate Files"
    echo "5. Exit"
    echo ""
}

# Main menu
while true; do
    show_options
    read -p "Select an option (1-5): " choice
    
    case $choice in
        1)
            deploy_firebase
            break
            ;;
        2)
            deploy_github
            break
            ;;
        3)
            test_local
            break
            ;;
        4)
            validate
            ;;
        5)
            echo "👋 Goodbye!"
            exit 0
            ;;
        *)
            echo "❌ Invalid option. Please select 1-5."
            ;;
    esac
done

echo ""
echo "🎉 Deployment process completed!"
echo "📖 For more information, check the website README.md"
