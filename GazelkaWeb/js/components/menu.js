import { Router } from "../router.js";

export function initMenu() {
  const toggleBtn = document.getElementById("menu-toggle-btn");
  const menu = document.getElementById("side-menu");

  if (!toggleBtn || !menu) return;

  toggleBtn.onclick = () => {
    menu.classList.toggle("active");
  };

  menu.onclick = async (e) => {
    const item = e.target.closest("[data-page]");
    if (!item) return;

    const page = item.dataset.page;

    menu.classList.remove("active");

    await Router.navigate(page);
  };
}

export function initSideMenu() {
  const menuBtn = document.getElementById("menu-toggle-btn");
  const sideMenu = document.getElementById("side-menu");

  if (menuBtn && sideMenu) {
    const newBtn = menuBtn.cloneNode(true);
    menuBtn.parentNode.replaceChild(newBtn, menuBtn);

    newBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      sideMenu.classList.toggle("active");
      newBtn.classList.toggle("active");
    });

    document.addEventListener("click", (e) => {
      if (!sideMenu.contains(e.target) && !newBtn.contains(e.target)) {
        sideMenu.classList.remove("active");
        newBtn.classList.remove("active");
      }
    });
  }
}
