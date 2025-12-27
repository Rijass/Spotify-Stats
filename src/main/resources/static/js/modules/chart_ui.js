import { API_BASE } from './api.js';

const chartList = document.getElementById('chart-list');
const chartDateLabel = document.getElementById('chart-date');
const chartToggleButton = document.getElementById('chart-toggle');
const chartRefreshButton = document.getElementById('chart-refresh');

let showingAll = false;
let latestSnapshot = null;

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-DE', { weekday: 'long', year: 'numeric', month: '2-digit', day: '2-digit' });
};

const renderEntry = (entry) => {
    const li = document.createElement('li');
    li.className = 'chart-row';
    li.innerHTML = `
        <div class="pill">#${entry.position}</div>
        <div>
            <strong>${entry.title}</strong>
            <span class="muted">${entry.artist}</span>
        </div>
        <span class="muted">${entry.providerTrackId}</span>
    `;
    return li;
};

const renderChart = (snapshot) => {
    if (!chartList || !snapshot) return;
    chartList.innerHTML = '';

    snapshot.entries.forEach((entry) => {
        chartList.appendChild(renderEntry(entry));
    });

    if (chartDateLabel) {
        chartDateLabel.textContent = snapshot.chartDate
            ? `Snapshot vom ${formatDate(snapshot.chartDate)}`
            : 'Snapshot geladen';
    }

    if (chartToggleButton) {
        chartToggleButton.textContent = showingAll ? 'Weniger anzeigen' : 'Mehr anzeigen';
    }
};

export const loadChart = async (forceAll = false) => {
    if (!chartList) return;

    const limit = forceAll || showingAll ? 50 : 10;
    const response = await fetch(`${API_BASE}/charts/global-top-50?limit=${limit}`);
    if (response.status === 204) {
        chartList.innerHTML = '<li class="chart-row"><div class="pill">—</div><div><strong>Keine Daten</strong><span class="muted">Noch kein Snapshot verfügbar.</span></div><span class="muted"></span></li>';
        return;
    }
    if (!response.ok) {
        chartList.innerHTML = '<li class="chart-row"><div class="pill">!</div><div><strong>Fehler</strong><span class="muted">Chart konnte nicht geladen werden.</span></div><span class="muted"></span></li>';
        return;
    }

    latestSnapshot = await response.json();
    showingAll = limit > 10;
    renderChart(latestSnapshot);
};

export const initChartUi = () => {
    if (chartToggleButton) {
        chartToggleButton.addEventListener('click', () => {
            showingAll = !showingAll;
            loadChart(showingAll);
        });
    }

    if (chartRefreshButton) {
        chartRefreshButton.addEventListener('click', () => loadChart(showingAll));
    }

    loadChart(false);
};