import { API } from "../api.js";

export const ChatService = {
  // список чатов
  async list() {
    return await API.get("chat/list");
  },

  async show(chatId) {
    return await API.get(`chat/history/${chatId}`);
  },

  // отправить сообщение
  async send(orderId, text) {
    return await API.post("chat/send", {
      orderId,
      text,
    });
  },
};
