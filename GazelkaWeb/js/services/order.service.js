import { API } from "../api.js";

export const OrderService = {
  async getCustomerScheduled() {
    return await API.get("Orders/customerScheduled");
  },

  async createOrder(orderData) {
    return await API.post("Orders/create", orderData);
  },
  
  async getById(orderId) {
    return await API.get(`Orders/${orderId}`);
  },
  
  async cancel(orderId){
    return await API.post(`Orders/cancel`, { orderId })
  },
  
  async update(orderData){
    return await API.post(`Orders/edit`, orderData)
  }
};
