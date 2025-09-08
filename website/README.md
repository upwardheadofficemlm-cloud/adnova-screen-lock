# AdNova Screen Lock - Public Website

A modern, responsive website for the AdNova Screen Lock digital signage solution, featuring admin login and app download functionality.

## 🌐 Website Features

### **Public Pages**
- **Homepage**: Product showcase with features and benefits
- **Download Section**: APK download with installation instructions
- **Admin Login**: Firebase authentication for device management
- **Contact Form**: Support and inquiry handling

### **Admin Dashboard**
- **Device Management**: View, monitor, and control devices
- **Configuration Management**: Create and push device configurations
- **Analytics**: Device usage and event tracking
- **System Settings**: Firebase and security configuration

## 🚀 Quick Start

### **Local Development**
1. Open `index.html` in a web browser
2. For admin features, use the login credentials:
   - **Email**: `admin@upwardmm.com`
   - **Password**: `Upward103999@@`

### **Deployment Options**

#### **Option 1: Firebase Hosting (Recommended)**
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize hosting
firebase init hosting

# Deploy
firebase deploy
```

#### **Option 2: GitHub Pages**
1. Push website files to GitHub repository
2. Enable GitHub Pages in repository settings
3. Select source branch (usually `main` or `gh-pages`)

#### **Option 3: Netlify**
1. Connect GitHub repository to Netlify
2. Set build command: `echo "Static site"`
3. Set publish directory: `website/`
4. Deploy automatically on git push

#### **Option 4: Vercel**
1. Connect GitHub repository to Vercel
2. Set root directory: `website/`
3. Deploy automatically on git push

## 📁 File Structure

```
website/
├── index.html              # Main homepage
├── admin-dashboard.html    # Admin dashboard
├── styles.css             # Main stylesheet
├── admin-styles.css       # Admin dashboard styles
├── script.js              # Main JavaScript
├── admin-script.js        # Admin dashboard JavaScript
├── assets/                # Images, icons, etc.
├── docs/                  # Documentation links
└── README.md              # This file
```

## 🔧 Configuration

### **Firebase Integration**
The website is pre-configured with your Firebase project:
- **Project ID**: `adnova-screen-lock-90521`
- **Authentication**: Email/password enabled
- **Firestore**: Device and configuration storage
- **Storage**: File uploads and logs

### **Customization**
1. **Branding**: Update colors, fonts, and logos in `styles.css`
2. **Content**: Modify text and images in HTML files
3. **Features**: Add new functionality in JavaScript files
4. **Styling**: Customize appearance with CSS

## 📱 Mobile Responsive

The website is fully responsive and optimized for:
- **Desktop**: Full-featured experience
- **Tablet**: Adapted layout and navigation
- **Mobile**: Touch-friendly interface

## 🔐 Security Features

- **Firebase Authentication**: Secure admin login
- **Session Management**: Auto-logout and timeout
- **Input Validation**: Form validation and sanitization
- **HTTPS Ready**: Secure communication

## 🎨 Design Features

- **Modern UI**: Clean, professional design
- **Smooth Animations**: CSS transitions and effects
- **Interactive Elements**: Hover effects and feedback
- **Accessibility**: Screen reader friendly

## 📊 Admin Dashboard Features

### **Device Management**
- Real-time device status monitoring
- Remote lock/unlock control
- Device configuration management
- Activity tracking and logs

### **Configuration Management**
- Create and edit device configurations
- Push configurations to multiple devices
- Version control and rollback
- Template management

### **Analytics & Reporting**
- Device usage statistics
- Event tracking and analysis
- Performance monitoring
- Export capabilities

### **System Administration**
- Firebase connection status
- Security settings management
- User session control
- Notification preferences

## 🚀 Deployment Checklist

- [ ] Update Firebase configuration if needed
- [ ] Test admin login functionality
- [ ] Verify all links and downloads work
- [ ] Test responsive design on mobile
- [ ] Configure custom domain (optional)
- [ ] Set up SSL certificate
- [ ] Test contact form functionality
- [ ] Verify Firebase services are enabled

## 🔧 Troubleshooting

### **Common Issues**

1. **Admin Login Not Working**
   - Check Firebase Authentication is enabled
   - Verify admin user exists in Firebase Console
   - Check browser console for errors

2. **Firebase Connection Issues**
   - Verify Firebase configuration
   - Check internet connection
   - Ensure Firebase services are enabled

3. **Mobile Display Issues**
   - Test on different devices
   - Check viewport meta tag
   - Verify responsive CSS

### **Browser Support**
- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 📞 Support

For website support and customization:
- **Email**: admin@upwardmm.com
- **Documentation**: Check the main project README
- **Issues**: Create GitHub issues for bugs

## 🔄 Updates

To update the website:
1. Make changes to HTML, CSS, or JavaScript files
2. Test locally in browser
3. Deploy to hosting platform
4. Verify functionality on live site

---

**AdNova Screen Lock Website** - Professional digital signage management solution.
