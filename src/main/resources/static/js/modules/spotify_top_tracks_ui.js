import { getAccessToken } from './session.js';
import { API_BASE } from './api.js';

export async function loadTopTracks() {
    const accessToken = getAccessToken();
    if (!accessToken) return;

    const panelBody = document.querySelector('.panel-body');
    const panelPlaceholder = document.getElementById('panel-placeholder');

    panelPlaceholder.style.display = 'none';

    panelBody.innerHTML = `<div class="tracks-grid" id="tracks-grid"></div>`;

    const response = await fetch(`${API_BASE}/spotify/top-tracks`, {
        headers: {
            Authorization: `Bearer ${accessToken}`
        }
    });

    if (!response.ok) {
        panelBody.innerHTML = `<p class="muted">Tracks konnten nicht geladen werden.</p>`;
        return;
    }

    const tracks = await response.json();
    const grid = document.getElementById('tracks-grid');

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
}
