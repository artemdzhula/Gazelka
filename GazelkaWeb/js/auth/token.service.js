export const TokenService = {
  save(access, refresh) {
    localStorage.setItem("accessToken", access);
    localStorage.setItem("refreshToken", refresh);
  },

  getAccess() {
    return localStorage.getItem("accessToken");
  },

  clear() {
    localStorage.clear();
  },
  
};
