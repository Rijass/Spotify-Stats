import { API_BASE } from './api.js';
import { getAccessToken } from './session.js';

export const loadTopTracks = async (container) => {
    container.innerHTML = ''; // alten Inhalt entfernen

    const accessToken = getAccessToken();
    if (!accessToken) {
        container.textContent = 'Nicht angemeldet.';
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/spotify/top-tracks`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        if (!response.ok) throw new Error('Top Tracks konnten nicht geladen werden.');

        const tracks = await response.json();

        const grid = document.createElement('div');
        grid.classList.add('tracks-grid');

        tracks.forEach(track => {
            const card = document.createElement('div');
            card.className = 'track-card';
            card.innerHTML = `
            <img src="${track.imageUrl}" alt="">
            <h4>${track.title}</h4>
            <p>${track.artists.join(', ')}</p>
        `;
            grid.appendChild(card);
        });

        container.appendChild(grid);
    } catch (err) {
        console.error(err);
        container.textContent = 'Fehler beim Laden der Top Tracks.';
    }
};
