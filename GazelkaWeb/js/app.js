import { Router } from "./router.js";

async function startApp() {
  await Router.loadLanding();
}

startApp();

window.addEventListener("popstate", async () => {
  const page = location.hash.replace("#", "");
  if (!page) {
    await Router.loadLanding();
  } else {
    await Router.navigate(page);
  }
});
