import { TokenService } from "./auth/token.service.js";

const BASE_URL = "https://localhost:7263/api/";

export const API = {
  async post(path, body) {
    const token = TokenService.getAccess();
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const res = await fetch(BASE_URL + path, {
      method: "POST",
      headers: headers,
      body: JSON.stringify(body),
    });

    let data = null;
    const text = await res.text();
    if (text) {
    try {
      data = JSON.parse(text);
    } catch (e) {
      console.warn("Response is not JSON:", text);
      data = null;
    }
  }

  if (!res.ok) {
    // если сервер вернул ошибку, берём сообщение из JSON, если есть
    throw new Error(data?.error || `Server error: ${res.status}`);
  }

  return data; // может быть null
},

  async get(endpoint) {
    const token = TokenService.getAccess();

    const res = await fetch(BASE_URL + endpoint, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });

    if (!res.ok) throw new Error("Failed to fetch: " + res.status);
    return await res.json();
  },

  async delete(endpoint){
    const token = TokenService.getAccess();

    const res = await fetch(BASE_URL + endpoint, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      }
    });

    if (!res.ok) throw new Error("Failed to fetch: " + res.status);
    return await res.json();
  }
};
