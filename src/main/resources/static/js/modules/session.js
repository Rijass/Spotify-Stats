const TOKEN_KEY = 'spotify-tracker-token';
const USER_ID_KEY = 'spotify-tracker-id';
const USER_NAME_KEY = 'spotify-tracker-user';

export const getAccessToken = () => localStorage.getItem(TOKEN_KEY);

export const persistSession = (data) => {
    if (data?.id) {
        localStorage.setItem(USER_ID_KEY, data.id);
    }
    if (data?.username) {
        localStorage.setItem(USER_NAME_KEY, data.username);
    }
    if (data?.accessToken) {
        localStorage.setItem(TOKEN_KEY, data.accessToken);
    }
};

export const clearSession = () => {
    localStorage.removeItem(USER_ID_KEY);
    localStorage.removeItem(USER_NAME_KEY);
    localStorage.removeItem(TOKEN_KEY);
};