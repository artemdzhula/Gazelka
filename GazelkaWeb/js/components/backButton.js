import { Router } from "../router.js";

export function initBackButton() {
  const btn = document.getElementById("back-to-landing");
  if (!btn) return;

  btn.onclick = () => {
    Router.loadLanding();
  };
}
export function initBackToOrdersButton() {
  const btn = document.getElementById("back-to-orders");
  if (!btn) return;

  btn.onclick = async () => {
    sessionStorage.removeItem("tempOrderData");
    sessionStorage.removeItem("isEditMode");

    await Router.navigate("order");
  };
}