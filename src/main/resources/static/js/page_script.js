import { API_BASE, USERS_API_BASE } from './modules/api.js';
import { clearSession, getAccessToken } from './modules/session.js';
import { loadProfile } from './modules/spotify_profile_ui.js';
import { initChartUi } from './modules/chart_ui.js';

document.addEventListener('DOMContentLoaded', () => {
    const spotifyButtons = Array.from(document.querySelectorAll('.spotify-login-trigger'));
    const root = document.body;
    const gates = Array.from(document.querySelectorAll('.gate-overlay'));
    const navPills = Array.from(document.querySelectorAll('.nav-pill'));
    const panel = document.querySelector('.main-panel');
    const panelTitle = document.getElementById('panel-title');
    const panelLede = document.getElementById('panel-lede');
    const panelBadge = document.getElementById('panel-badge');
    const chartSection = document.getElementById('chart-section');
    const overviewPlaceholder = document.getElementById('overview-placeholder');
    const logoutButton = document.getElementById('logout-button');

    const tabContent = {
        charts: {
            title: 'Global Top 50',
            lede: 'Die aktuellsten 50 Songs der globalen Spotify Charts.',
            placeholder: '',
            badge: 'Charts'
        },
        welcome: {
            title: 'Willkommen',
            lede: 'Starte hier, um dein Spotify-Profil und deine Statistiken einzusehen.',
            placeholder: 'Sobald du verbunden bist, siehst du hier dein persönliches Dashboard.',
            badge: 'Übersicht'
        },
    };

    const setActiveTab = (tabKey, updateUrl = true) => {
        const content = tabContent[tabKey] || tabContent.welcome;
        navPills.forEach((pill) => {
            const isActive = pill.dataset.tab === tabKey;
            pill.classList.toggle('active', isActive);
        });

        if (panel) {
            panel.dataset.activeTab = tabKey;
        }

        if (panelTitle) {
            panelTitle.textContent = content.title;
        }
        if (panelLede) {
            panelLede.textContent = content.lede;
        }
        if (panelBadge) {
            panelBadge.textContent = content.badge;
        }

        if (chartSection) {
            chartSection.hidden = tabKey !== 'charts';
        }
        if (overviewPlaceholder) {
            overviewPlaceholder.hidden = tabKey !== 'welcome';
        }

        if (updateUrl) {
            const url = new URL(window.location.href);
            url.searchParams.set('tab', tabKey);
            window.history.replaceState({}, '', url.toString());
        }
    };

    const unlockDashboard = () => {
        root.classList.remove('locked');
        gates.forEach((gate) => gate.classList.add('dismissed'));
    };

    const keepLocked = () => {
        root.classList.add('locked');
        gates.forEach((gate) => gate.classList.remove('dismissed'));
    };

    const ensureSession = async () => {
        const accessToken = getAccessToken();
        if (!accessToken) {
            window.location.href = 'index.html';
            return false;
        }

        const response = await fetch(`${USERS_API_BASE}/session`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
        if (!response.ok) {
            window.location.href = 'index.html';
            return false;
        }
        return true;
    };

    const checkSpotifyStatus = async () => {
        const accessToken = getAccessToken();
        if (!accessToken) {
            keepLocked();
            return false;
        }

        const response = await fetch(`${API_BASE}/spotify/status`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
        if (!response.ok) {
            keepLocked();
            return false;
        }

        const { connected } = await response.json();
        if (connected) {
            unlockDashboard();
            await loadProfile(API_BASE);
            return true;
        } else {
            keepLocked();
            return false;
        }
    };

    const initialTab = new URLSearchParams(window.location.search).get('tab') || 'charts';
    setActiveTab(initialTab, false);

    navPills.forEach((pill) => {
        pill.addEventListener('click', () => {
            const tabKey = pill.dataset.tab;
            setActiveTab(tabKey);
        });
    });

    spotifyButtons.forEach((button) => {
        button.addEventListener('click', () => {
            const accessToken = getAccessToken();
            if (!accessToken) {
                window.location.href = 'index.html';
                return;
            }

            fetch(`${API_BASE}/spotify/login`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error('Spotify Login fehlgeschlagen.');
                    }
                    return response.json();
                })
                .then((data) => {
                    if (data?.authorizationUrl) {
                        window.location.href = data.authorizationUrl;
                    }
                })
                .catch((error) => {
                    console.error(error);
                    keepLocked();
                });
        });
    });

    const handleLogout = async () => {
        clearSession();
        window.location.href = 'index.html';
    };

    if (logoutButton) {
        logoutButton.addEventListener('click', handleLogout);
    }

    ensureSession().then((authenticated) => {
        if (!authenticated) {
            return;
        }

        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('connected') === 'spotify') {
            unlockDashboard();
            loadProfile(API_BASE);
            urlParams.delete('connected');
            const url = new URL(window.location.href);
            url.search = urlParams.toString();
            window.history.replaceState({}, '', url.toString());
        } else {
            checkSpotifyStatus();
        }

        initChartUi();
    });
});