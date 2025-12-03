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
            // Hier würdest du den echten Spotify-OAuth-Flow starten.
            // Beispiel: window.location.href = '/api/spotify/authorize';
            root.classList.remove('locked');
            gates.forEach((gate) => gate.classList.add('dismissed'));
        });
    });
});