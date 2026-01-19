import { API_BASE } from './api.js';
import { getAccessToken } from './session.js';

/**
 * Lädt die global beliebtesten/featured Playlists von Spotify und zeigt sie in einem Grid an
 * @param {HTMLElement} container - das DOM-Element, in das die Playlists gerendert werden
 */
export const loadFeaturedPlaylists = async (container) => {
    container.innerHTML = ''; // Alten Inhalt entfernen

    const accessToken = getAccessToken();
    if (!accessToken) {
        container.textContent = 'Nicht angemeldet.';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/spotify/featured-playlists`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        if (!response.ok) throw new Error('Featured Playlists konnten nicht geladen werden.');

        const playlists = await response.json();

        if (!playlists.length) {
            container.textContent = 'Keine Playlists verfügbar.';
            return;
        }

        const grid = document.createElement('div');
        grid.classList.add('playlists-grid'); // CSS-Grid für die Cards

        playlists.forEach(playlist => {
            const card = document.createElement('div');
            card.className = 'playlist-card';

            card.innerHTML = `
                <img src="${playlist.imageUrl || ''}" alt="${playlist.name}">
                <div class="playlist-info">
                    <h4>${playlist.name}</h4>
                    <p class="playlist-description">
                        ${playlist.description || 'Keine Beschreibung verfügbar'}
                    </p>
                </div>
            `;

            grid.appendChild(card);
        });

        container.appendChild(grid);
    } catch (err) {
        console.error(err);
        container.textContent = 'Fehler beim Laden der Featured Playlists.';
    }
};
