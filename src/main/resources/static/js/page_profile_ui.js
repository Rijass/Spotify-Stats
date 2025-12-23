(() => {
    const heroTitle = document.querySelector('.hero-text h1');
    const followerStat = document.querySelector('[data-stat="followers"] strong');
    const avatarPlaceholder = document.querySelector('.avatar-placeholder');
    const planBadge = document.getElementById('account-plan');

    const formatPlanLabel = (product) => {
        if (!product) {
            return 'Spotify';
        }

        const normalized = product.toLowerCase();
        return normalized.charAt(0).toUpperCase() + normalized.slice(1);
    };

    const renderAvatar = (profile) => {
        if (!avatarPlaceholder) {
            return;
        }

        if (profile?.imageUrl) {
            avatarPlaceholder.style.backgroundImage = `url(${profile.imageUrl})`;
            avatarPlaceholder.textContent = '';
            avatarPlaceholder.classList.add('has-image');
            return;
        }

        if (profile?.displayName) {
            avatarPlaceholder.textContent = profile.displayName.substring(0, 2).toUpperCase();
            avatarPlaceholder.style.backgroundImage = '';
            avatarPlaceholder.classList.remove('has-image');
        }
    };

    const renderStats = (profile) => {
        if (typeof profile?.followers === 'number' && followerStat) {
            followerStat.textContent = profile.followers.toLocaleString('de-DE');
        }
    };

    const renderPlan = (product) => {
        if (!planBadge) {
            return;
        }

        planBadge.textContent = formatPlanLabel(product);
    };

    const renderSpotifyProfile = (profile) => {
        if (profile?.displayName && heroTitle) {
            heroTitle.textContent = profile.displayName;
        }

        renderAvatar(profile);
        renderStats(profile);
        renderPlan(profile?.product);
    };

    const fetchSpotifyProfile = async (apiBase) => {
        const accessToken = localStorage.getItem('spotify-tracker-token');
        if (!accessToken) {
            return null;
        }

        const response = await fetch(`${apiBase}/spotify/profile`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
        if (!response.ok) {
            return null;
        }

        return response.json();
    };

    const loadProfile = async (apiBase) => {
        if (!apiBase) {
            return;
        }

        try {
            const profile = await fetchSpotifyProfile(apiBase);
            if (profile) {
                renderSpotifyProfile(profile);
            }
        } catch (error) {
            console.error('Konnte Spotify-Profil nicht laden', error);
        }
    };

    window.SpotifyProfileUI = {
        renderProfile: renderSpotifyProfile,
        fetchProfile: fetchSpotifyProfile,
        loadProfile,
    };
})();