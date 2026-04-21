import {
  LandingPage,
  LoginPage,
  RegisterPage,
  OrderPage,
  SummaryPage,
  ChatPage,
  SettingsPage,
  Menu,
  BackButtons,
  RecoveryPage,
  InstallPage,
  EmailVerificationPage,
} from "./pages/index.js";

const app = document.getElementById("app");
const authContainer = document.getElementById("auth-container");

const landingSections = [
  "header.html",
  "hero.html",
  "features.html",
  "cta-driver.html",
  "cta-client.html",
  "footer.html",
];

export const Router = {
  async loadLanding() {
    app.innerHTML = "";
    authContainer.style.display = "none";

    const htmls = await Promise.all(
      landingSections.map(s =>
        fetch(`sections/${s}`).then(r => r.text())
      )
    );

    app.innerHTML = htmls.join("");
    app.style.display = "block";

    LandingPage.init();



    history.pushState({}, "", "/");
  },

  async navigate(page) {
    const html = await fetch(`sections/${page}.html`).then(r => r.text());

    app.style.display = "none";
    authContainer.style.display = "block";
    authContainer.innerHTML = html;


    Menu.init();
    BackButtons.init();

    // switch (page) {
    //   case "login":
    //     LoginPage.init();
    //     break;

    //   case "register":
    //     RegisterPage.init();
    //     break;

    //   case "order":
    //     OrderPage.init();
    //     break;

    //   case "order-summary":
    //     SummaryPage.init();
    //     break;

    //   case "chat":
    //     ChatPage.init();
    //     break;

    //   case "settings":
    //     SettingsPage.init();
    //     break;

    //   case "order-details":
    //     OrderDetailsPage.init();
    //     break;

    //   default:
    //     console.warn("Unknown page:", page);
    // }

    switch (page) {
      case "login":
        LoginPage.init();
        break;

      case "register":
        RegisterPage.init();
        break;

      case "order":
        OrderPage.init();
        break;

      case "order-summary":
        SummaryPage.init();
        break;

      case "chat":
        ChatPage.init();
        break;

      case "settings":
        SettingsPage.init();
        break;

      case "order-details":
        OrderDetailsPage.init();
        break;
      case "recovery":
        RecoveryPage.init();
        break;
      case "install":
        InstallPage.init();
        break;
      case "email-verification":
        EmailVerificationPage.init();
        break;

      default:
        console.warn("Unknown page:", page);
    }

    history.pushState({ page }, "", `#${page}`);
  }
};
