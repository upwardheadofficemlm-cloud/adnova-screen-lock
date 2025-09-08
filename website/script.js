// Firebase Configuration
const firebaseConfig = {
    apiKey: "AIzaSyAz8P_dX5uTI3zF0lW2CwPh4yqFvwfUn2o",
    authDomain: "adnova-screen-lock-90521.firebaseapp.com",
    projectId: "adnova-screen-lock-90521",
    storageBucket: "adnova-screen-lock-90521.firebasestorage.app",
    messagingSenderId: "914767381345",
    appId: "1:914767381345:android:22ee0a507344a202977b20"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();

// DOM Elements
const navToggle = document.querySelector('.nav-toggle');
const navMenu = document.querySelector('.nav-menu');
const adminForm = document.getElementById('adminForm');
const contactForm = document.getElementById('contactForm');

// Mobile Navigation
navToggle.addEventListener('click', () => {
    navMenu.classList.toggle('active');
    navToggle.classList.toggle('active');
});

// Close mobile menu when clicking on a link
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
        navMenu.classList.remove('active');
        navToggle.classList.remove('active');
    });
});

// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Password toggle functionality
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleButton = document.querySelector('.password-toggle i');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleButton.classList.remove('fa-eye');
        toggleButton.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        toggleButton.classList.remove('fa-eye-slash');
        toggleButton.classList.add('fa-eye');
    }
}

// Admin Login Form
adminForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const submitButton = adminForm.querySelector('button[type="submit"]');
    
    // Show loading state
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Signing In...';
    submitButton.disabled = true;
    
    try {
        // Sign in with Firebase Auth
        const userCredential = await auth.signInWithEmailAndPassword(email, password);
        const user = userCredential.user;
        
        // Store user session
        localStorage.setItem('adminUser', JSON.stringify({
            uid: user.uid,
            email: user.email,
            loginTime: Date.now()
        }));
        
        // Show success message
        showMessage('Login successful! Redirecting to admin dashboard...', 'success');
        
        // Redirect to admin dashboard (you can create this page)
        setTimeout(() => {
            window.location.href = 'admin-dashboard.html';
        }, 2000);
        
    } catch (error) {
        console.error('Login error:', error);
        let errorMessage = 'Login failed. Please try again.';
        
        switch (error.code) {
            case 'auth/user-not-found':
                errorMessage = 'No account found with this email address.';
                break;
            case 'auth/wrong-password':
                errorMessage = 'Incorrect password. Please try again.';
                break;
            case 'auth/invalid-email':
                errorMessage = 'Invalid email address.';
                break;
            case 'auth/too-many-requests':
                errorMessage = 'Too many failed attempts. Please try again later.';
                break;
        }
        
        showMessage(errorMessage, 'error');
    } finally {
        // Reset button state
        submitButton.innerHTML = '<i class="fas fa-sign-in-alt"></i> Sign In';
        submitButton.disabled = false;
    }
});

// Contact Form
contactForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const formData = new FormData(contactForm);
    const submitButton = contactForm.querySelector('button[type="submit"]');
    
    // Show loading state
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
    submitButton.disabled = true;
    
    try {
        // Simulate form submission (replace with actual backend)
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // Show success message
        showMessage('Message sent successfully! We\'ll get back to you soon.', 'success');
        contactForm.reset();
        
    } catch (error) {
        console.error('Contact form error:', error);
        showMessage('Failed to send message. Please try again.', 'error');
    } finally {
        // Reset button state
        submitButton.innerHTML = '<i class="fas fa-paper-plane"></i> Send Message';
        submitButton.disabled = false;
    }
});

// Show message function
function showMessage(message, type) {
    // Remove existing messages
    const existingMessages = document.querySelectorAll('.message');
    existingMessages.forEach(msg => msg.remove());
    
    // Create new message
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    // Insert message at the top of the form
    const form = type === 'success' ? adminForm : contactForm;
    form.insertBefore(messageDiv, form.firstChild);
    
    // Auto-remove message after 5 seconds
    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

// Check if user is already logged in
function checkAuthState() {
    auth.onAuthStateChanged((user) => {
        if (user) {
            // User is signed in
            const adminUser = localStorage.getItem('adminUser');
            if (adminUser) {
                // Redirect to admin dashboard if already logged in
                if (window.location.pathname.includes('admin-dashboard.html')) {
                    return; // Already on admin page
                }
                // Show admin link as active
                const adminLink = document.querySelector('a[href="#admin"]');
                if (adminLink) {
                    adminLink.innerHTML = '<i class="fas fa-user-shield"></i> Admin Dashboard';
                    adminLink.href = 'admin-dashboard.html';
                }
            }
        } else {
            // User is signed out
            localStorage.removeItem('adminUser');
        }
    });
}

// Initialize auth state check
checkAuthState();

// Download tracking
function trackDownload() {
    // Track download event (you can integrate with analytics)
    console.log('APK download initiated');
    
    // You can add analytics tracking here
    // gtag('event', 'download', {
    //     'event_category': 'app',
    //     'event_label': 'AdNovaScreenLock-v1.0.0.apk'
    // });
}

// Add download tracking to download button
document.addEventListener('DOMContentLoaded', () => {
    const downloadButton = document.querySelector('.btn-download');
    if (downloadButton) {
        downloadButton.addEventListener('click', trackDownload);
    }
});

// Scroll animations
function animateOnScroll() {
    const elements = document.querySelectorAll('.feature-card, .contact-item');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });
    
    elements.forEach(element => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        element.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(element);
    });
}

// Initialize scroll animations
document.addEventListener('DOMContentLoaded', animateOnScroll);

// Navbar scroll effect
window.addEventListener('scroll', () => {
    const navbar = document.querySelector('.navbar');
    if (window.scrollY > 100) {
        navbar.style.background = 'rgba(255, 255, 255, 0.98)';
        navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
    } else {
        navbar.style.background = 'rgba(255, 255, 255, 0.95)';
        navbar.style.boxShadow = 'none';
    }
});

// Form validation
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required], textarea[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = '#dc2626';
            isValid = false;
        } else {
            input.style.borderColor = '#e5e7eb';
        }
    });
    
    return isValid;
}

// Add form validation to forms
[adminForm, contactForm].forEach(form => {
    if (form) {
        form.addEventListener('submit', (e) => {
            if (!validateForm(form)) {
                e.preventDefault();
                showMessage('Please fill in all required fields.', 'error');
            }
        });
    }
});

// Auto-fill admin email for demo
document.addEventListener('DOMContentLoaded', () => {
    const emailInput = document.getElementById('email');
    if (emailInput && !emailInput.value) {
        emailInput.value = 'admin@upwardmm.com';
    }
});

// Copy to clipboard functionality
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showMessage('Copied to clipboard!', 'success');
    }).catch(err => {
        console.error('Failed to copy: ', err);
        showMessage('Failed to copy to clipboard', 'error');
    });
}

// Add copy functionality to contact info
document.addEventListener('DOMContentLoaded', () => {
    const contactItems = document.querySelectorAll('.contact-item p');
    contactItems.forEach(item => {
        item.style.cursor = 'pointer';
        item.title = 'Click to copy';
        item.addEventListener('click', () => {
            copyToClipboard(item.textContent);
        });
    });
});

// Error handling for Firebase
window.addEventListener('error', (e) => {
    console.error('Global error:', e.error);
});

// Service Worker registration (for PWA features)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(registration => {
                console.log('SW registered: ', registration);
            })
            .catch(registrationError => {
                console.log('SW registration failed: ', registrationError);
            });
    });
}
