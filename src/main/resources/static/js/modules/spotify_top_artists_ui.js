import { API_BASE } from './api.js';
import { getAccessToken } from './session.js';

export const loadTopArtists = async (container) => {
    container.innerHTML = '';

    const appToken = getAccessToken();   // <-- dein JWT
    if (!appToken) {
        container.textContent = 'Nicht angemeldet.';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/spotify/top-artists`, {
            headers: { Authorization: `Bearer ${appToken}` }
        });

        if (!response.ok) throw new Error('Top Artists konnten nicht geladen werden.');

        const artists = await response.json();

        const grid = document.createElement('div');
        grid.classList.add('artists-grid');

        artists.forEach(artist => {
            const card = document.createElement('div');
            card.className = 'artist-card';

            card.innerHTML = `
                <img src="${artist.imageUrl}" alt="${artist.name}">
                <div class="artist-info">
                    <h4>${artist.name}</h4>
                    <p class="artist-meta">
                        🎵 ${artist.genres?.slice(0, 3).join(', ') || 'Keine Genres'}<br><br>
                        🎧 ${artist.followers?.toLocaleString() || 0} Follower
                    </p>
                </div>
            `;

            grid.appendChild(card);
        });

        container.appendChild(grid);

    } catch (err) {
        console.error(err);
        container.textContent = 'Fehler beim Laden der Top Artists.';
    }
};
