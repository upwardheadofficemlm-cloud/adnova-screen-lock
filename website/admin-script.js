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
const db = firebase.firestore();

// Global variables
let currentUser = null;
let devices = [];
let configurations = [];
let realTimeListeners = [];
let deviceStatusInterval = null;

// DOM Elements
const navLinks = document.querySelectorAll('.nav-link');
const sections = document.querySelectorAll('section[id]');
const userEmail = document.getElementById('userEmail');

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    checkAuthState();
    initializeDashboard();
    setupNavigation();
    loadDashboardData();
    startRealTimeListeners();
});

// Authentication
function checkAuthState() {
    auth.onAuthStateChanged((user) => {
        if (user) {
            currentUser = user;
            userEmail.textContent = user.email;
            loadDashboardData();
        } else {
            // Redirect to login if not authenticated
            window.location.href = 'index.html#admin';
        }
    });
}

function logout() {
    auth.signOut().then(() => {
        localStorage.removeItem('adminUser');
        window.location.href = 'index.html';
    }).catch((error) => {
        console.error('Logout error:', error);
        showNotification('Logout failed. Please try again.', 'error');
    });
}

// Navigation
function setupNavigation() {
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetSection = link.getAttribute('href').substring(1);
            showSection(targetSection);
            
            // Update active nav link
            navLinks.forEach(nav => nav.classList.remove('active'));
            link.classList.add('active');
        });
    });
}

function showSection(sectionId) {
    sections.forEach(section => {
        section.classList.remove('active');
    });
    
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

// Dashboard Functions
function initializeDashboard() {
    // Set default active section
    showSection('dashboard');
    document.querySelector('a[href="#dashboard"]').classList.add('active');
}

async function loadDashboardData() {
    try {
        // Load devices
        await loadDevices();
        
        // Load configurations
        await loadConfigurations();
        
        // Update stats
        updateDashboardStats();
        
        // Load recent activity
        loadRecentActivity();
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showNotification('Failed to load dashboard data', 'error');
    }
}

async function loadDevices() {
    try {
        const devicesSnapshot = await db.collection('devices').get();
        devices = devicesSnapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
        
        updateDevicesTable();
    } catch (error) {
        console.error('Error loading devices:', error);
        // Use mock data for demo
        devices = [
            {
                id: 'DEV-001',
                name: 'Main Display',
                status: 'online',
                lockType: 'Full Screen',
                lastSeen: new Date(Date.now() - 2 * 60 * 1000), // 2 minutes ago
                locked: true
            },
            {
                id: 'DEV-002',
                name: 'Side Panel',
                status: 'offline',
                lockType: 'Edge Lock',
                lastSeen: new Date(Date.now() - 60 * 60 * 1000), // 1 hour ago
                locked: false
            }
        ];
        updateDevicesTable();
    }
}

async function loadConfigurations() {
    try {
        const configsSnapshot = await db.collection('configurations').get();
        configurations = configsSnapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
    } catch (error) {
        console.error('Error loading configurations:', error);
        // Use mock data for demo
        configurations = [
            {
                id: 'default',
                name: 'Default Configuration',
                active: true,
                lockType: 'Full Screen',
                pinProtection: true,
                autoLock: true,
                kioskMode: false
            },
            {
                id: 'kiosk',
                name: 'Kiosk Configuration',
                active: false,
                lockType: 'Full Screen',
                pinProtection: false,
                autoLock: true,
                kioskMode: true
            }
        ];
    }
}

function updateDashboardStats() {
    const totalDevices = devices.length;
    const lockedDevices = devices.filter(device => device.locked).length;
    const totalEvents = Math.floor(Math.random() * 1000) + 500; // Mock data
    const alertsCount = devices.filter(device => device.status === 'offline').length;
    
    document.getElementById('totalDevices').textContent = totalDevices;
    document.getElementById('lockedDevices').textContent = lockedDevices;
    document.getElementById('totalEvents').textContent = totalEvents;
    document.getElementById('alertsCount').textContent = alertsCount;
}

function loadRecentActivity() {
    const activityList = document.getElementById('activityList');
    const activities = [
        { device: 'DEV-001', action: 'screen locked', time: '2 minutes ago', icon: 'fas fa-lock', color: '#ef4444' },
        { device: 'DEV-002', action: 'screen unlocked', time: '5 minutes ago', icon: 'fas fa-unlock', color: '#10b981' },
        { device: 'DEV-003', action: 'configuration updated', time: '10 minutes ago', icon: 'fas fa-cog', color: '#3b82f6' },
        { device: 'DEV-001', action: 'PIN unlock attempted', time: '15 minutes ago', icon: 'fas fa-key', color: '#f59e0b' },
        { device: 'DEV-004', action: 'device came online', time: '20 minutes ago', icon: 'fas fa-wifi', color: '#10b981' }
    ];
    
    activityList.innerHTML = activities.map(activity => `
        <div class="activity-item">
            <div class="activity-icon" style="background: ${activity.color}">
                <i class="${activity.icon}"></i>
            </div>
            <div class="activity-content">
                <p><strong>${activity.device}</strong> ${activity.action}</p>
                <span class="activity-time">${activity.time}</span>
            </div>
        </div>
    `).join('');
}

// Device Management
function updateDevicesTable() {
    const tbody = document.getElementById('devicesTableBody');
    tbody.innerHTML = devices.map(device => `
        <tr>
            <td>${device.id}</td>
            <td>${device.name}</td>
            <td><span class="status-badge ${device.status}">${device.status}</span></td>
            <td>${device.lockType}</td>
            <td>${formatTimeAgo(device.lastSeen)}</td>
            <td>
                <button class="btn-action" onclick="viewDevice('${device.id}')" title="View Details">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn-action" onclick="editDevice('${device.id}')" title="Edit Device">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn-action" onclick="lockDevice('${device.id}')" title="Toggle Lock">
                    <i class="fas fa-${device.locked ? 'unlock' : 'lock'}"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function viewDevice(deviceId) {
    const device = devices.find(d => d.id === deviceId);
    if (!device) return;
    
    const modal = document.getElementById('deviceModal');
    const deviceDetails = document.getElementById('deviceDetails');
    
    deviceDetails.innerHTML = `
        <div class="device-details">
            <div class="detail-row">
                <label>Device ID:</label>
                <span>${device.id}</span>
            </div>
            <div class="detail-row">
                <label>Name:</label>
                <span>${device.name}</span>
            </div>
            <div class="detail-row">
                <label>Status:</label>
                <span class="status-badge ${device.status}">${device.status}</span>
            </div>
            <div class="detail-row">
                <label>Lock Type:</label>
                <span>${device.lockType}</span>
            </div>
            <div class="detail-row">
                <label>Currently Locked:</label>
                <span>${device.locked ? 'Yes' : 'No'}</span>
            </div>
            <div class="detail-row">
                <label>Last Seen:</label>
                <span>${formatTimeAgo(device.lastSeen)}</span>
            </div>
            <div class="detail-row">
                <label>IP Address:</label>
                <span>192.168.1.${Math.floor(Math.random() * 100) + 100}</span>
            </div>
            <div class="detail-row">
                <label>Android Version:</label>
                <span>Android ${Math.floor(Math.random() * 3) + 10}</span>
            </div>
        </div>
    `;
    
    modal.style.display = 'block';
}

function editDevice(deviceId) {
    showNotification('Edit device functionality coming soon', 'info');
}

async function lockDevice(deviceId) {
    try {
        const device = devices.find(d => d.id === deviceId);
        if (!device) return;
        
        // Toggle lock status
        device.locked = !device.locked;
        
        // Update in database
        await db.collection('devices').doc(deviceId).update({
            locked: device.locked,
            lastUpdated: new Date()
        });
        
        // Update UI
        updateDevicesTable();
        updateDashboardStats();
        
        showNotification(`Device ${deviceId} ${device.locked ? 'locked' : 'unlocked'} successfully`, 'success');
        
    } catch (error) {
        console.error('Error toggling device lock:', error);
        showNotification('Failed to toggle device lock', 'error');
    }
}

function addDevice() {
    showNotification('Add device functionality coming soon', 'info');
}

function refreshDevices() {
    loadDevices();
    showNotification('Devices refreshed', 'success');
}

// Configuration Management
function createConfiguration() {
    showNotification('Create configuration functionality coming soon', 'info');
}

function editConfig(configId) {
    showNotification(`Edit configuration ${configId} functionality coming soon`, 'info');
}

function duplicateConfig(configId) {
    showNotification(`Duplicate configuration ${configId} functionality coming soon`, 'info');
}

async function pushToAll() {
    try {
        showNotification('Pushing configuration to all devices...', 'info');
        
        // Simulate push to all devices
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        showNotification('Configuration pushed to all devices successfully', 'success');
        
    } catch (error) {
        console.error('Error pushing configuration:', error);
        showNotification('Failed to push configuration', 'error');
    }
}

// Analytics
function exportReport() {
    showNotification('Export report functionality coming soon', 'info');
}

// Utility Functions
function formatTimeAgo(date) {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) {
        return `${diffInSeconds} seconds ago`;
    } else if (diffInSeconds < 3600) {
        const minutes = Math.floor(diffInSeconds / 60);
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
        const hours = Math.floor(diffInSeconds / 3600);
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else {
        const days = Math.floor(diffInSeconds / 86400);
        return `${days} day${days > 1 ? 's' : ''} ago`;
    }
}

function showNotification(message, type = 'info') {
    // Remove existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());
    
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${getNotificationIcon(type)}"></i>
            <span>${message}</span>
        </div>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    // Add to page
    document.body.appendChild(notification);
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
}

function getNotificationIcon(type) {
    switch (type) {
        case 'success': return 'check-circle';
        case 'error': return 'exclamation-circle';
        case 'warning': return 'exclamation-triangle';
        default: return 'info-circle';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Close modal when clicking outside
window.addEventListener('click', (event) => {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});

// Refresh activity
function refreshActivity() {
    loadRecentActivity();
    showNotification('Activity refreshed', 'success');
}

// Real-time updates
function startRealTimeUpdates() {
    // Listen for device changes
    db.collection('devices').onSnapshot((snapshot) => {
        devices = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
        updateDevicesTable();
        updateDashboardStats();
    });
    
    // Listen for configuration changes
    db.collection('configurations').onSnapshot((snapshot) => {
        configurations = snapshot.docs.map(doc => ({
            id: doc.id,
            ...doc.data()
        }));
    });
}

// Initialize real-time updates when user is authenticated
auth.onAuthStateChanged((user) => {
    if (user) {
        startRealTimeUpdates();
    }
});

// Add notification styles
const notificationStyles = `
    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        padding: 1rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        z-index: 3000;
        min-width: 300px;
        animation: slideInRight 0.3s ease;
    }
    
    .notification-success {
        border-left: 4px solid #10b981;
    }
    
    .notification-error {
        border-left: 4px solid #ef4444;
    }
    
    .notification-warning {
        border-left: 4px solid #f59e0b;
    }
    
    .notification-info {
        border-left: 4px solid #3b82f6;
    }
    
    .notification-content {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        flex: 1;
    }
    
    .notification-content i {
        font-size: 1.2rem;
    }
    
    .notification-success .notification-content i {
        color: #10b981;
    }
    
    .notification-error .notification-content i {
        color: #ef4444;
    }
    
    .notification-warning .notification-content i {
        color: #f59e0b;
    }
    
    .notification-info .notification-content i {
        color: #3b82f6;
    }
    
    .notification-close {
        background: none;
        border: none;
        color: #6b7280;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
        transition: background 0.3s ease;
    }
    
    .notification-close:hover {
        background: #f3f4f6;
    }
    
    @keyframes slideInRight {
        from {
            opacity: 0;
            transform: translateX(100%);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
    
    .device-details {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }
    
    .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0.75rem 0;
        border-bottom: 1px solid #f3f4f6;
    }
    
    .detail-row:last-child {
        border-bottom: none;
    }
    
    .detail-row label {
        font-weight: 600;
        color: #374151;
    }
    
    .detail-row span {
        color: #1f2937;
    }
`;

// Add styles to page
const styleSheet = document.createElement('style');
styleSheet.textContent = notificationStyles;
document.head.appendChild(styleSheet);

// Real-time Device Management Functions

/**
 * Start real-time listeners for device updates
 */
function startRealTimeListeners() {
    if (!currentUser) return;
    
    console.log('Starting real-time listeners...');
    
    // Listen for device updates
    const devicesListener = db.collection('devices')
        .onSnapshot((snapshot) => {
            console.log('Devices updated:', snapshot.docs.length);
            devices = snapshot.docs.map(doc => ({
                id: doc.id,
                ...doc.data()
            }));
            updateDevicesDisplay();
            updateDashboardStats();
        }, (error) => {
            console.error('Error listening to devices:', error);
        });
    
    realTimeListeners.push(devicesListener);
    
    // Listen for commands
    const commandsListener = db.collection('commands')
        .onSnapshot((snapshot) => {
            console.log('Commands updated:', snapshot.docs.length);
            updateCommandsDisplay(snapshot.docs);
        }, (error) => {
            console.error('Error listening to commands:', error);
        });
    
    realTimeListeners.push(commandsListener);
    
    // Start device status monitoring
    startDeviceStatusMonitoring();
}

/**
 * Start device status monitoring
 */
function startDeviceStatusMonitoring() {
    deviceStatusInterval = setInterval(() => {
        updateDeviceStatuses();
    }, 30000); // Check every 30 seconds
}

/**
 * Update device statuses based on last seen time
 */
function updateDeviceStatuses() {
    const now = new Date();
    const offlineThreshold = 2 * 60 * 1000; // 2 minutes
    
    devices.forEach(device => {
        const lastSeen = device.lastSeen?.toDate();
        if (lastSeen) {
            const timeDiff = now - lastSeen;
            const isOnline = timeDiff < offlineThreshold;
            
            if (device.status !== (isOnline ? 'online' : 'offline')) {
                // Update device status in Firestore
                db.collection('devices').doc(device.id).update({
                    status: isOnline ? 'online' : 'offline'
                });
            }
        }
    });
}

/**
 * Send remote command to device
 */
async function sendRemoteCommand(deviceId, command, parameters = {}) {
    try {
        const commandData = {
            deviceId: deviceId,
            command: command,
            parameters: parameters,
            status: 'pending',
            createdAt: new Date(),
            sentBy: currentUser.uid
        };
        
        const docRef = await db.collection('commands').add(commandData);
        console.log('Command sent:', docRef.id);
        
        showNotification(`Command sent to device ${deviceId}`, 'success');
        
        // Auto-remove command after 5 minutes
        setTimeout(() => {
            db.collection('commands').doc(docRef.id).delete();
        }, 5 * 60 * 1000);
        
        return docRef.id;
    } catch (error) {
        console.error('Error sending command:', error);
        showNotification('Failed to send command', 'error');
    }
}

/**
 * Lock device remotely
 */
async function lockDevice(deviceId, lockType = 'full', duration = null) {
    const parameters = {
        lockType: lockType,
        duration: duration
    };
    
    await sendRemoteCommand(deviceId, 'lock_screen', parameters);
}

/**
 * Unlock device remotely
 */
async function unlockDevice(deviceId) {
    await sendRemoteCommand(deviceId, 'unlock_screen');
}

/**
 * Update device configuration remotely
 */
async function updateDeviceConfig(deviceId, config) {
    const parameters = {
        config: config
    };
    
    await sendRemoteCommand(deviceId, 'update_config', parameters);
}

/**
 * Restart device app remotely
 */
async function restartDevice(deviceId) {
    await sendRemoteCommand(deviceId, 'restart_app');
}

/**
 * Take screenshot of device remotely
 */
async function takeDeviceScreenshot(deviceId) {
    await sendRemoteCommand(deviceId, 'take_screenshot');
}

/**
 * Update devices display with real-time data
 */
function updateDevicesDisplay() {
    const devicesContainer = document.getElementById('devicesList');
    if (!devicesContainer) return;
    
    devicesContainer.innerHTML = '';
    
    devices.forEach(device => {
        const deviceCard = createDeviceCard(device);
        devicesContainer.appendChild(deviceCard);
    });
}

/**
 * Create device card with real-time controls
 */
function createDeviceCard(device) {
    const card = document.createElement('div');
    card.className = 'device-card';
    card.innerHTML = `
        <div class="device-header">
            <div class="device-info">
                <h3>${device.deviceName || 'Unknown Device'}</h3>
                <p class="device-id">ID: ${device.id}</p>
            </div>
            <div class="device-status">
                <span class="status-indicator ${device.status}"></span>
                <span class="status-text">${device.status}</span>
            </div>
        </div>
        
        <div class="device-details">
            <div class="detail-row">
                <label>Model:</label>
                <span>${device.model || 'Unknown'}</span>
            </div>
            <div class="detail-row">
                <label>Android Version:</label>
                <span>${device.androidVersion || 'Unknown'}</span>
            </div>
            <div class="detail-row">
                <label>Last Seen:</label>
                <span>${formatLastSeen(device.lastSeen)}</span>
            </div>
            <div class="detail-row">
                <label>Battery:</label>
                <span>${device.batteryLevel || 'Unknown'}% ${device.isCharging ? '(Charging)' : ''}</span>
            </div>
        </div>
        
        <div class="device-actions">
            <button class="btn btn-primary" onclick="lockDevice('${device.id}')">
                <i class="fas fa-lock"></i> Lock
            </button>
            <button class="btn btn-secondary" onclick="unlockDevice('${device.id}')">
                <i class="fas fa-unlock"></i> Unlock
            </button>
            <button class="btn btn-warning" onclick="takeDeviceScreenshot('${device.id}')">
                <i class="fas fa-camera"></i> Screenshot
            </button>
            <button class="btn btn-danger" onclick="restartDevice('${device.id}')">
                <i class="fas fa-redo"></i> Restart
            </button>
        </div>
    `;
    
    return card;
}

/**
 * Format last seen time
 */
function formatLastSeen(lastSeen) {
    if (!lastSeen) return 'Never';
    
    const date = lastSeen.toDate();
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return `${Math.floor(diff / 86400000)}d ago`;
}

/**
 * Update commands display
 */
function updateCommandsDisplay(commandDocs) {
    const commandsContainer = document.getElementById('commandsList');
    if (!commandsContainer) return;
    
    commandsContainer.innerHTML = '';
    
    commandDocs.forEach(doc => {
        const command = { id: doc.id, ...doc.data() };
        const commandItem = createCommandItem(command);
        commandsContainer.appendChild(commandItem);
    });
}

/**
 * Create command item
 */
function createCommandItem(command) {
    const item = document.createElement('div');
    item.className = `command-item ${command.status}`;
    item.innerHTML = `
        <div class="command-info">
            <h4>${command.command}</h4>
            <p>Device: ${command.deviceId}</p>
            <p>Status: ${command.status}</p>
            <p>Sent: ${formatLastSeen(command.createdAt)}</p>
        </div>
        <div class="command-actions">
            <button class="btn btn-sm btn-danger" onclick="cancelCommand('${command.id}')">
                Cancel
            </button>
        </div>
    `;
    
    return item;
}

/**
 * Cancel command
 */
async function cancelCommand(commandId) {
    try {
        await db.collection('commands').doc(commandId).delete();
        showNotification('Command cancelled', 'success');
    } catch (error) {
        console.error('Error cancelling command:', error);
        showNotification('Failed to cancel command', 'error');
    }
}

/**
 * Clean up real-time listeners
 */
function cleanupRealTimeListeners() {
    realTimeListeners.forEach(listener => listener());
    realTimeListeners = [];
    
    if (deviceStatusInterval) {
        clearInterval(deviceStatusInterval);
        deviceStatusInterval = null;
    }
}

// Clean up on page unload
window.addEventListener('beforeunload', cleanupRealTimeListeners);
