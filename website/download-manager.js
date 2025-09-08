// Download Manager for AdNova Screen Lock App
class DownloadManager {
    constructor() {
        this.downloadUrls = {
            'latest': '/downloads/AdNovaScreenLock-v1.0.0.apk',
            'beta': '/downloads/AdNovaScreenLock-beta.apk',
            'stable': '/downloads/AdNovaScreenLock-v1.0.0.apk'
        };
        
        this.appInfo = {
            name: 'AdNova Screen Lock',
            version: '1.0.0',
            size: '15.2 MB',
            minAndroid: 'Android 10+',
            requirements: '2GB RAM, 32GB Storage',
            lastUpdated: '2024-01-15'
        };
        
        this.init();
    }
    
    init() {
        this.setupDownloadButtons();
        this.setupVersionInfo();
        this.setupDownloadTracking();
    }
    
    setupDownloadButtons() {
        // Main download button
        const mainDownloadBtn = document.getElementById('mainDownloadBtn');
        if (mainDownloadBtn) {
            mainDownloadBtn.addEventListener('click', () => this.downloadApp('latest'));
        }
        
        // Download section buttons
        const downloadButtons = document.querySelectorAll('[data-download]');
        downloadButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const version = btn.getAttribute('data-download');
                this.downloadApp(version);
            });
        });
        
        // QR code download
        this.setupQRCode();
    }
    
    setupVersionInfo() {
        // Update version info in download section
        const versionElements = document.querySelectorAll('.app-version');
        versionElements.forEach(el => {
            el.textContent = this.appInfo.version;
        });
        
        const sizeElements = document.querySelectorAll('.app-size');
        sizeElements.forEach(el => {
            el.textContent = this.appInfo.size;
        });
        
        const androidElements = document.querySelectorAll('.app-android');
        androidElements.forEach(el => {
            el.textContent = this.appInfo.minAndroid;
        });
    }
    
    setupQRCode() {
        const qrContainer = document.getElementById('qrCodeContainer');
        if (qrContainer) {
            // Generate QR code for mobile download
            const qrData = {
                url: window.location.origin + '/download',
                title: 'Download AdNova Screen Lock',
                description: 'Scan to download the app on your mobile device'
            };
            
            qrContainer.innerHTML = `
                <div class="qr-code">
                    <div class="qr-placeholder">
                        <i class="fas fa-qrcode"></i>
                        <p>QR Code for Mobile Download</p>
                        <small>Scan with your phone camera</small>
                    </div>
                </div>
            `;
        }
    }
    
    async downloadApp(version = 'latest') {
        try {
            // Show download progress
            this.showDownloadProgress();
            
            // Track download event
            this.trackDownload(version);
            
            // Get download URL
            const downloadUrl = this.downloadUrls[version] || this.downloadUrls['latest'];
            
            // Create download link
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = `AdNova-Screen-Lock-${version}.apk`;
            link.style.display = 'none';
            
            // Add to DOM and trigger download
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            // Show success message
            this.showDownloadSuccess(version);
            
            // Hide progress after delay
            setTimeout(() => {
                this.hideDownloadProgress();
            }, 3000);
            
        } catch (error) {
            console.error('Download failed:', error);
            this.showDownloadError(error.message);
        }
    }
    
    showDownloadProgress() {
        // Create or show progress modal
        let progressModal = document.getElementById('downloadProgressModal');
        if (!progressModal) {
            progressModal = document.createElement('div');
            progressModal.id = 'downloadProgressModal';
            progressModal.className = 'download-progress-modal';
            progressModal.innerHTML = `
                <div class="progress-content">
                    <div class="progress-icon">
                        <i class="fas fa-download"></i>
                    </div>
                    <h3>Downloading AdNova Screen Lock</h3>
                    <div class="progress-bar">
                        <div class="progress-fill"></div>
                    </div>
                    <p class="progress-text">Preparing download...</p>
                </div>
            `;
            document.body.appendChild(progressModal);
        }
        
        progressModal.style.display = 'flex';
        
        // Animate progress bar
        const progressFill = progressModal.querySelector('.progress-fill');
        const progressText = progressModal.querySelector('.progress-text');
        
        let progress = 0;
        const interval = setInterval(() => {
            progress += Math.random() * 15;
            if (progress > 100) progress = 100;
            
            progressFill.style.width = progress + '%';
            
            if (progress < 30) {
                progressText.textContent = 'Preparing download...';
            } else if (progress < 70) {
                progressText.textContent = 'Downloading app...';
            } else if (progress < 100) {
                progressText.textContent = 'Almost ready...';
            } else {
                progressText.textContent = 'Download complete!';
                clearInterval(interval);
            }
        }, 200);
    }
    
    hideDownloadProgress() {
        const progressModal = document.getElementById('downloadProgressModal');
        if (progressModal) {
            progressModal.style.display = 'none';
        }
    }
    
    showDownloadSuccess(version) {
        this.showNotification(`AdNova Screen Lock ${version} downloaded successfully!`, 'success');
        
        // Show installation instructions
        setTimeout(() => {
            this.showInstallationInstructions();
        }, 2000);
    }
    
    showDownloadError(message) {
        this.showNotification(`Download failed: ${message}`, 'error');
        this.hideDownloadProgress();
    }
    
    showInstallationInstructions() {
        const instructionsModal = document.createElement('div');
        instructionsModal.className = 'instructions-modal';
        instructionsModal.innerHTML = `
            <div class="instructions-content">
                <div class="instructions-header">
                    <h3><i class="fas fa-mobile-alt"></i> Installation Instructions</h3>
                    <button class="close-btn" onclick="this.parentElement.parentElement.remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
                <div class="instructions-body">
                    <div class="step">
                        <div class="step-number">1</div>
                        <div class="step-content">
                            <h4>Enable Unknown Sources</h4>
                            <p>Go to Settings → Security → Unknown Sources and enable installation from unknown sources.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">2</div>
                        <div class="step-content">
                            <h4>Install the APK</h4>
                            <p>Open the downloaded APK file and follow the installation prompts.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">3</div>
                        <div class="step-content">
                            <h4>Grant Permissions</h4>
                            <p>The app will request necessary permissions. Grant all permissions for full functionality.</p>
                        </div>
                    </div>
                    <div class="step">
                        <div class="step-number">4</div>
                        <div class="step-content">
                            <h4>Start Using</h4>
                            <p>Launch the app and configure your screen locking preferences.</p>
                        </div>
                    </div>
                </div>
                <div class="instructions-footer">
                    <button class="btn btn-primary" onclick="this.parentElement.parentElement.remove()">
                        Got it!
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(instructionsModal);
    }
    
    trackDownload(version) {
        // Track download analytics
        if (typeof gtag !== 'undefined') {
            gtag('event', 'download', {
                'event_category': 'app',
                'event_label': version,
                'value': 1
            });
        }
        
        // Log to console for debugging
        console.log(`Download tracked: ${version} version`);
    }
    
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
                <span>${message}</span>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Show notification
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);
        
        // Hide notification after 5 seconds
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 5000);
    }
    
    // Get app information
    getAppInfo() {
        return this.appInfo;
    }
    
    // Check for updates
    async checkForUpdates() {
        try {
            // This would typically check against a version API
            const response = await fetch('/api/version');
            const data = await response.json();
            
            if (data.version !== this.appInfo.version) {
                this.showUpdateAvailable(data.version);
            }
        } catch (error) {
            console.log('No update check available');
        }
    }
    
    showUpdateAvailable(newVersion) {
        const updateBanner = document.createElement('div');
        updateBanner.className = 'update-banner';
        updateBanner.innerHTML = `
            <div class="update-content">
                <i class="fas fa-download"></i>
                <span>New version ${newVersion} available!</span>
                <button class="btn btn-sm btn-primary" onclick="downloadManager.downloadApp('latest')">
                    Update Now
                </button>
                <button class="btn btn-sm btn-secondary" onclick="this.parentElement.parentElement.remove()">
                    Later
                </button>
            </div>
        `;
        
        document.body.appendChild(updateBanner);
    }
}

// Initialize download manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.downloadManager = new DownloadManager();
});

// Add CSS styles for download components
const downloadStyles = `
    .download-progress-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.8);
        display: none;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    
    .progress-content {
        background: white;
        padding: 2rem;
        border-radius: 12px;
        text-align: center;
        max-width: 400px;
        width: 90%;
    }
    
    .progress-icon {
        font-size: 3rem;
        color: #007bff;
        margin-bottom: 1rem;
    }
    
    .progress-bar {
        width: 100%;
        height: 8px;
        background: #f0f0f0;
        border-radius: 4px;
        overflow: hidden;
        margin: 1rem 0;
    }
    
    .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #007bff, #0056b3);
        width: 0%;
        transition: width 0.3s ease;
    }
    
    .progress-text {
        color: #666;
        margin: 0;
    }
    
    .instructions-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.8);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    
    .instructions-content {
        background: white;
        border-radius: 12px;
        max-width: 600px;
        width: 90%;
        max-height: 80vh;
        overflow-y: auto;
    }
    
    .instructions-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem;
        border-bottom: 1px solid #eee;
    }
    
    .instructions-header h3 {
        margin: 0;
        color: #333;
    }
    
    .close-btn {
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        color: #666;
    }
    
    .instructions-body {
        padding: 1.5rem;
    }
    
    .step {
        display: flex;
        align-items: flex-start;
        margin: 1.5rem 0;
    }
    
    .step-number {
        background: #007bff;
        color: white;
        width: 30px;
        height: 30px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        margin-right: 1rem;
        flex-shrink: 0;
    }
    
    .step-content h4 {
        margin: 0 0 0.5rem 0;
        color: #333;
    }
    
    .step-content p {
        margin: 0;
        color: #666;
    }
    
    .instructions-footer {
        padding: 1.5rem;
        border-top: 1px solid #eee;
        text-align: center;
    }
    
    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        padding: 1rem;
        z-index: 1001;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    }
    
    .notification.show {
        transform: translateX(0);
    }
    
    .notification-success {
        border-left: 4px solid #28a745;
    }
    
    .notification-error {
        border-left: 4px solid #dc3545;
    }
    
    .notification-info {
        border-left: 4px solid #007bff;
    }
    
    .notification-content {
        display: flex;
        align-items: center;
        gap: 0.5rem;
    }
    
    .notification-success .notification-content i {
        color: #28a745;
    }
    
    .notification-error .notification-content i {
        color: #dc3545;
    }
    
    .notification-info .notification-content i {
        color: #007bff;
    }
    
    .update-banner {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        background: #007bff;
        color: white;
        padding: 1rem;
        z-index: 1002;
        text-align: center;
    }
    
    .update-content {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 1rem;
        flex-wrap: wrap;
    }
    
    .update-content i {
        font-size: 1.2rem;
    }
    
    .qr-code {
        text-align: center;
        padding: 1rem;
    }
    
    .qr-placeholder {
        background: #f8f9fa;
        border: 2px dashed #dee2e6;
        border-radius: 8px;
        padding: 2rem;
        color: #6c757d;
    }
    
    .qr-placeholder i {
        font-size: 3rem;
        margin-bottom: 1rem;
        display: block;
    }
`;

// Add styles to page
const styleSheet = document.createElement('style');
styleSheet.textContent = downloadStyles;
document.head.appendChild(styleSheet);
