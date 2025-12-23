document.addEventListener('DOMContentLoaded', () => {
    const spotifyButtons = Array.from(document.querySelectorAll('.spotify-login-trigger'));
    const root = document.body;
    const gates = Array.from(document.querySelectorAll('.gate-overlay'));
    const navPills = Array.from(document.querySelectorAll('.nav-pill'));
    const panel = document.querySelector('.main-panel');
    const panelTitle = document.getElementById('panel-title');
    const panelLede = document.getElementById('panel-lede');
    const panelPlaceholder = document.getElementById('panel-placeholder');
    const panelBadge = document.getElementById('panel-badge');
    const logoutButton = document.getElementById('logout-button');

    const tabContent = {
        welcome: {
            title: 'Willkommen',
            lede: 'Starte hier, um dein Spotify-Profil und deine Statistiken einzusehen.',
            placeholder: 'Sobald du verbunden bist, siehst du hier dein persönliches Dashboard.',
            badge: 'Übersicht'
        },
        quicksearch: {
            title: 'Schnellsuche',
            lede: 'Finde schnell Playlists, Tracks oder Künstler, sobald du angemeldet bist.',
            placeholder: 'Nutze die Suche, um Inhalte aus deinem Spotify-Konto zu laden.',
            badge: 'Suche'
        },
        links: {
            title: 'Links & Aktionen',
            lede: 'Verwalte deine Shortcuts und Aktionen, die du häufig brauchst.',
            placeholder: 'Hier erscheinen deine Aktionen, sobald du sie hinterlegt hast.',
            badge: 'Aktionen'
        },
        explore: {
            title: 'Entdecken',
            lede: 'Lass dir Empfehlungen und Analysen anzeigen, wenn dein Konto verbunden ist.',
            placeholder: 'Empfehlungen und Trends warten hier auf dich.',
            badge: 'Explore'
        }
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
        if (panelPlaceholder) {
            panelPlaceholder.querySelector('.muted').textContent = content.placeholder;
        }
        if (panelBadge) {
            panelBadge.textContent = content.badge;
        }

        if (updateUrl) {
            const url = new URL(window.location.href);
            url.searchParams.set('tab', tabKey);
            window.history.replaceState({}, '', url.toString());
        }
    };

    const apiBase = 'http://127.0.0.1:8080/api';

    const tokenKey = 'spotify-tracker-token';

    const getAccessToken = () => localStorage.getItem(tokenKey);

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

        const response = await fetch(`${apiBase}/users/session`, {
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

        const response = await fetch(`${apiBase}/spotify/status`, {
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
            await window.SpotifyProfileUI?.loadProfile(apiBase);
            return true;
        } else {
            keepLocked();
            return false;
        }
    };

    const initialTab = new URLSearchParams(window.location.search).get('tab') || 'welcome';
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

            fetch(`${apiBase}/spotify/login`, {
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
        const accessToken = getAccessToken();
        try {
            await fetch(`${apiBase}/users/logout`, {
                method: 'POST',
                headers: accessToken
                    ? { Authorization: `Bearer ${accessToken}` }
                    : {}
            });
        } finally {
            localStorage.removeItem('spotify-tracker-id');
            localStorage.removeItem('spotify-tracker-user');
            localStorage.removeItem(tokenKey);
            window.location.href = 'index.html';
        }
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
            window.SpotifyProfileUI?.loadProfile(apiBase);
            urlParams.delete('connected');
            const url = new URL(window.location.href);
            url.search = urlParams.toString();
            window.history.replaceState({}, '', url.toString());
        } else {
            checkSpotifyStatus();
        }
    });
});