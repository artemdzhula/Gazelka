import { API } from "../api.js";
import { TokenService } from "./token.service.js";

export const AuthService = {
  async login(email, password) {
    try {
      const res = await API.post("Auth/login", { email, password });

      TokenService.save(res.accessToken, res.refreshToken);

      return { success: true };
    } catch (err) {
      return { success: false, error: err.message };
    }
  },

  async register(data) {
    try{
      await API.post("Auth/register", data);
      return { success: true };
    }catch(err){
      return { success: false, error: err.message };
    }
    
  },

  async userInfo(){
    return await API.get("Auth/userinfo");
  },

  async updateProfile(data) {
    return await API.post("Auth/updateProfile", data);
  },
  
  async getUserInfoById(userId) {
    return await API.get(`Auth/userinfo/${userId}`);
  },
  async deleteProfile(){
    return await API.delete(`Auth/deleteAccount`);
  },
  async sendVerificationCode(email){
    return await API.post(`Auth/resendEmailCode`, email);
  },
  async verifyEmail(email, code){
    try{
      await API.post(`Auth/verifyEmail`, {email, code});
      return { success: true };
    }catch(err){
      return {success: false, erroe: err.message};
    }
  },
  async requestPasswordReset(email){
    try{
      await API.post(`Auth/requestPasswordReset`, email);
      return { success: true };
    }catch(err){
      return {success: false, erroe: err.message};
    }
  },
  async resetPassword(email, code, newPassword){
    try{
      await API.post(`Auth/resetPassword`, {email, code, newPassword});
      return { success: true };
    }catch(err){
      return {success: false, error: err.message};
    }
  },
};
