import { Router } from "../router.js";
import { AuthService } from "../auth/auth.service.js";
import { TokenService } from "../auth/token.service.js";
import { OrderService } from "../services/order.service.js";
import { ChatService } from "../services/chat.service.js";
import { openMapOverlay, initMapButtons } from "../map.js";
import { initReviewsSlider } from "../reviews.js";

let connection = null;
let myId = null;
let activeChatId = null;
let activeOrderId = null;
let currentOrderData = null;
let previewTimeout = null;
let tempEmail = null;


// =================== Landing Page ===================
export const LandingPage = {
  init() {
    const becomeUserBtn = document.getElementById("become-user-btn");
    if (becomeUserBtn) {
      becomeUserBtn.onclick = async () => {
        await Router.navigate("login");
      };
    }

    const becomeDriverBtn = document.querySelector(".cta--driver .btn");
    if (becomeDriverBtn) {
      becomeDriverBtn.onclick = async () => {
        await Router.navigate("install");
      };
    }

    if (typeof initReviewsSlider === "function") initReviewsSlider();
  }
};

// =================== Menu (верхнее/сайд) ===================
export const Menu = {
  init() {
    document.addEventListener("click", (e) => {
      const toggleBtn = document.getElementById("menu-toggle-btn");
      const menu = document.getElementById("side-menu");
      if (!toggleBtn || !menu) return;

      // Клик по кнопке меню — открываем/закрываем
      if (e.target === toggleBtn || toggleBtn.contains(e.target)) {
        e.stopPropagation();
        menu.classList.toggle("active");
      }
      // Клик вне меню — закрываем
      else if (!menu.contains(e.target)) {
        menu.classList.remove("active");
      }

      // Клик по пункту меню
      const menuItem = e.target.closest(".section-menu-item, .summary-menu-item");
      if (menuItem && menu.contains(menuItem)) {
        menu.classList.remove("active");
        switch (menuItem.id) {
          case "menu-new-order":
            Router.navigate("order");
            break;
          case "menu-chat":
            Router.navigate("chat");
            break;
          case "menu-history":
            Router.navigate("order-summary");
            break;
          case "menu-settings":
            Router.navigate("settings");
            break;
        }
      }
    });
  },
};


// =================== Back Buttons ===================
export const BackButtons = {
  init() {
    const backLanding = document.getElementById("back-to-landing");
    if (backLanding) backLanding.onclick = () => Router.loadLanding();

    const backOrders = document.getElementById("back-to-orders");
    if (backOrders) backOrders.onclick = async () => {
      sessionStorage.removeItem("tempOrderData");
      sessionStorage.removeItem("isEditMode");
      await Router.navigate("order");
    };
  }
};

// =================== Login Page ===================
export const LoginPage = {
  init() {
    const btn = document.getElementById("login-submit-btn");
    if (!btn) return;

    const toRegister = document.getElementById("to-register-link");
    if (toRegister) {
      toRegister.onclick = async () => {
        await Router.navigate("register");
      };
    }

    const forgotPassword = document.getElementById("forgot-password-link");
    if (forgotPassword) {
      forgotPassword.onclick = async () => {
        await Router.navigate("recovery");
      };
    }

    // Toggle password visibility
    const togglePassword = document.getElementById("toggle-password");
    const passwordInput = document.getElementById("login-password");
    const passwordIcon = document.getElementById("password-icon");

    if (togglePassword && passwordInput && passwordIcon) {
      togglePassword.onclick = () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        passwordIcon.src = isPassword
          ? "assets/icons/ic_visibility_on.svg"
          : "assets/icons/ic_visibility_off.svg";
      };
    }

    btn.onclick = async () => {
      const email = document.getElementById("login-email")?.value.trim();
      const password = document.getElementById("login-password")?.value.trim();
      if (!email || !password) return alert("Enter email and password");

      const res = await AuthService.login(email, password);
      if (!res.success) return alert(res.error);

      await Router.navigate("order-summary");
    };
  }
};

// =================== Register Page ===================
export const RegisterPage = {
  init() {
    const btn = document.getElementById("next-step-btn");
    if (!btn) return;

    const toLogin = document.getElementById("to-login-link");
    if (toLogin) {
      toLogin.onclick = async () => {
        await Router.navigate("login");
      };
    }

    const togglePassword = document.getElementById("toggle-password");
    const passwordInput = document.getElementById("reg-password");
    const passwordIcon = document.getElementById("password-icon");

    if (togglePassword && passwordInput && passwordIcon) {
      togglePassword.onclick = () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        passwordIcon.src = isPassword
          ? "assets/icons/ic_visibility_on.svg"
          : "assets/icons/ic_visibility_off.svg";
      };
    }

    btn.onclick = async () => {
      const email = document.getElementById("reg-email").value.trim();
      const password = document.getElementById("reg-password").value.trim();
      const name = document.getElementById("reg-name").value.trim();
      const surname = document.getElementById("reg-surname").value.trim();
      const role = "customer";
      const carType = null;
      const carColor = null;
      const carNumber = null;
      const phoneNumber = document.getElementById("reg-phone").value.trim();
      const cityName = null;
      

      if (!email || !password || !name || !surname || !phoneNumber) {
        return alert("Please fill all fields");
      }

      sessionStorage.setItem("tempRegData", JSON.stringify({
        email, password, name, surname, role, carType, carColor, carNumber, phoneNumber, cityName
      }));

      const res = await AuthService.register({email, password, name, surname, role, carType, carColor, carNumber, phoneNumber, cityName});

      if (!res.success) {
        alert(res.error);
        return;
      }


      await Router.navigate("email-verification");
    };
  }
};

// =================== Email Verification Page ===================
export const EmailVerificationPage = {
  init() {
    const tempRegData = JSON.parse(sessionStorage.getItem("tempRegData") || "{}");
    if(tempEmail){
      tempRegData.email = tempEmail;
      tempEmail = null;
    }
    
    const emailInput = document.getElementById("verify-email");
    if (emailInput && tempRegData.email) {
      emailInput.value = tempRegData.email;
    }

    const toLogin = document.getElementById("to-login-link");
    if (toLogin) {
      toLogin.onclick = async () => {
        await Router.navigate("login");
      };
    }

    const resendLink = document.getElementById("resend-code-link");
    if (resendLink) {
      resendLink.onclick = async () => {
        if (!tempRegData.email) {
          return alert("Email not found. Please register again.");
        }
        await AuthService.sendVerificationCode(tempRegData.email);
        alert("Verification code has been resent to your email!");
      };
    }

    const verifyBtn = document.getElementById("verify-email-btn");
    if (!verifyBtn) return;

    verifyBtn.onclick = async () => {
      const code = document.getElementById("verification-code")?.value.trim();
      
      if (!code) {
        return alert("Please enter verification code");
      }

      if (!tempRegData.email) {
        return alert("Email not found. Please register again.");
      }

      const verifyRes = await AuthService.verifyEmail(tempRegData.email, code);
      if (!verifyRes.success) return alert(verifyRes.error);


      sessionStorage.removeItem("tempRegData");

      alert("Email verified successfully! Please login.");
      await Router.navigate("login");
    };
  }
};


// =================== Order Page ===================
export const OrderPage = {
  init() {
    const fromInput = document.getElementById("from-location");
    const toInput = document.getElementById("to-location");
    const isEditMode = sessionStorage.getItem("isEditMode") === "true";
    const stored = JSON.parse(sessionStorage.getItem("tempOrderData") || "{}");
    
    if (fromInput) {
      fromInput.addEventListener("input", triggerPreview);
      fromInput.parentElement.onclick = () => openMapOverlay("from-location");
    }

    if (toInput) {
      toInput.addEventListener("input", triggerPreview);
      toInput.parentElement.onclick = () => openMapOverlay("to-location");
    }


    document.querySelectorAll('input[name="vehicle-type"]')
    .forEach(r => r.addEventListener("change", triggerPreview));

    document.querySelectorAll(".cargo-checkbox")
    .forEach(cb => cb.addEventListener("change", triggerPreview));
    // Карта
    if (fromInput) fromInput.parentElement.onclick = () => openMapOverlay("from-location");
    if (toInput) toInput.parentElement.onclick = () => openMapOverlay("to-location");
    initMapButtons();

    // Дата и время
    const dateInput = document.getElementById("order-date");
    const timeInput = document.getElementById("order-time");

    if (dateInput) {
      dateInput.min = new Date().toISOString().split("T")[0];
      dateInput.closest(".dt-input")?.addEventListener("click", () => dateInput.showPicker());
      dateInput.addEventListener("change", validateDateTime);
    }

    if (timeInput) {
      timeInput.closest(".dt-input")?.addEventListener("click", () => timeInput.showPicker());
      timeInput.addEventListener("change", validateDateTime);
    }

    function validateDateTime() {
      if (!dateInput.value) return;
      const today = new Date().toISOString().split("T")[0];
      if (dateInput.value === today && timeInput.value) {
        const now = new Date();
        const currentTime = `${String(now.getHours()).padStart(2,"0")}:${String(now.getMinutes()).padStart(2,"0")}`;
        if (timeInput.value <= currentTime) {
          alert("Cannot schedule order in the past.");
          timeInput.value = "";
        }
      }
    }
    
    // Восстановление данных заказа
    restoreOrderData();

    // Кнопка Create/Save
    const submitBtn = document.getElementById("submit-order-btn");
    if (submitBtn) {
      submitBtn.textContent = sessionStorage.getItem("isEditMode") === "true" ? "Save order" : "Create order";

      submitBtn.onclick = async () => {
        const from = document.getElementById("from-location")?.value.trim();
        const to = document.getElementById("to-location")?.value.trim();
        const date = document.getElementById("order-date")?.value;
        const time = document.getElementById("order-time")?.value;
        const vehicleType = document.querySelector('input[name="vehicle-type"]:checked')?.value;

        
        if (!from || !to || !date || !time) {
          return alert("Please fill all fields");
        }

        const dateTime = new Date(`${date}T${time}`).toISOString();
        const cargoCheckboxes = document.querySelectorAll(".cargo-checkbox");
        const payload = {
          pointA: from,
          pointB: to,
          vehicleType,
          dateTime,
          fragile: false,
          heavy: false,
          standard: false,
          valuable: false,
          furniture: false,
          perishable: false
        };

        cargoCheckboxes.forEach(cb => {
        const label = cb.closest(".cargo-option")?.querySelector("strong")?.innerText?.toLowerCase();
        if (cb.checked && label) {
        if (label.includes("standard")) payload.standard = true;
        if (label.includes("valuable")) payload.valuable = true;
        if (label.includes("fragile")) payload.fragile = true;
        if (label.includes("heavy")) payload.heavy = true;

        }
        });
        
        try {
          if (isEditMode && stored.orderId) {
            const updPayload = {
              orderId: stored.orderId,
              ...payload
            };
            await OrderService.update(updPayload);
            alert("Order updated successfully!");
          } else {
            await OrderService.createOrder(payload);
            alert("Order created successfully!");
          }
          sessionStorage.removeItem("tempOrderData");
          sessionStorage.removeItem("isEditMode");

          await Router.navigate("order-summary");
        } catch (err) {
            alert("Failed to save order: " + err.message);
        }
        };
    }


    Menu.init();
  }
};

function restoreOrderData() {
  const raw = sessionStorage.getItem("tempOrderData");
  const isEdit = sessionStorage.getItem("isEditMode") === "true";
  if (!raw || !isEdit) return;

  try {
    const data = JSON.parse(raw);
    if (data.from) document.getElementById("from-location").value = data.from;
    if (data.to) document.getElementById("to-location").value = data.to;
    if (data.date) document.getElementById("order-date").value = data.date;
    if (data.time) document.getElementById("order-time").value = data.time;

    if (data.vehicle) {
      const radio = document.querySelector(`input[name="vehicle-type"][value="${data.vehicle}"]`);
      if (radio) radio.checked = true;
    }

    if (data.cargo) {
      const cargoItems = data.cargo.split(",").map(s => s.trim().toLowerCase());
      document.querySelectorAll(".cargo-checkbox").forEach(cb => {
        const label = cb.closest(".cargo-option")?.querySelector("strong")?.innerText?.toLowerCase();
        if (label && cargoItems.includes(label)) cb.checked = true;
      });
    }
  } catch (err) {
    console.error("Restore order error:", err);
  }
}


export const SummaryPage = {
  async init() {
    const container = document.getElementById("orders-grid-container");
    if (!container) return;

    container.innerHTML = "<p style='text-align:center; padding:50px; color:#000;'>Loading orders...</p>";

    try {
      const orders = await OrderService.getCustomerScheduled();

      if (!orders.length) {
        container.innerHTML = "<p style='text-align:center; padding:50px; color:#000;'>No scheduled orders found.</p>";
        return;
      }

      container.innerHTML = orders
        .map((o) => this.getOrderCardHTML(o))
        .join("");
    } catch (err) {
      container.innerHTML = `<p style="color:red; text-align:center; padding:50px;">Error: ${err.message}</p>`;
    }
    initOrderDetailsModal();
    Menu.init();
  },

  getOrderCardHTML(data) {
    const vehicleName = this.getVehicleName(data.vehicleType);
    const cargoText = this.getCargoText(data);
    const dateFormatted = new Date(data.dateTime).toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });

    const statusMap = {
    0: "Pending",
    1: "Accepted",
    2: "InProgress",
    3: "Completed",
    4: "Cancelled",
    5: "Driver Coming",
    6: "Picking up",
    7: "Delivering"
  };
    return `
<div class="order-card"
     data-id="${data.orderId}"
     data-status="${data.status}"
     data-pointa="${data.pointA}"
     data-pointb="${data.pointB}"
     data-datetime="${data.dateTime}"
     data-price="${data.price || ''}"
     data-vehicle="${vehicleName}"
     data-cargo="${cargoText}">

  <div class="order-card-header">
    <div class="header-left">
      <span class="label-mini">NUMBER</span>
      <div class="order-number">Order #${data.orderId}</div>
    </div>

    <div class="order-price">
      <span>${data.price || "N/A"}</span> <small>USD</small>
    </div>
  </div>

  <div class="order-card-body">
    <div class="order-info-row">
      <div class="info-block">
        <span class="info-label">Date</span>
        <span class="info-value">${dateFormatted}</span>
      </div>
    </div>

    <div class="order-info-row split">
      <div class="info-block">
        <span class="info-label">From</span>
        <span class="info-value">${data.pointA}</span>
      </div>
      <div class="info-block">
        <span class="info-label">To</span>
        <span class="info-value">${data.pointB}</span>
      </div>
    </div>

    <div class="order-info-row split">
      <div class="info-block">
        <span class="info-label">Cargo</span>
        <span class="info-value">${cargoText}</span>
      </div>
      <div class="info-block">
        <span class="info-label">Vehicle</span>
        <span class="info-value">${vehicleName}</span>
      </div>
    </div>
  </div>

  <div class="order-card-status-bar">
    <span class="status-dot"></span>
    <span class="status-text">${statusMap[data.status]}</span>
  </div>

</div>
`;
  },

  getVehicleName(vehicleType) {
    if (!vehicleType) return "Standard Van";
    const name = vehicleType.charAt(0).toUpperCase() + vehicleType.slice(1);
    return name;
  },

  getCargoText(data) {
    if (data.cargo) return data.cargo;
    
    const cargoTypes = [];
    if (data.fragile) cargoTypes.push("Fragile");
    if (data.heavy) cargoTypes.push("Heavy");
    if (data.furniture) cargoTypes.push("Furniture");
    if (data.perishable) cargoTypes.push("Perishable");
    
    return cargoTypes.length > 0 ? cargoTypes.join(", ") : "Standard";
  }
};

// =================== Chat Page ===================
export const ChatPage = {
  async init() {
    const listView = document.getElementById("chat-list-view");
    const convoView = document.getElementById("chat-conversation-view");
    const messagesBox = document.getElementById("chat-messages");
    const input = document.getElementById("chat-input");
    const sendBtn = document.getElementById("send-msg-btn");
    Menu.init();
    if (!listView) return;

    // ==========================
    // Получаем ID пользователя
    // ==========================
    try {
      const userInfo = await AuthService.userInfo(); 
      myId = userInfo.id;
      console.log("My ID:", myId);
    } catch (err) {
      alert("Cannot load chat: user not identified");
      return;
    }

    // ==========================
    // Инициализация SignalR
    // ==========================
    connection = new signalR.HubConnectionBuilder()
      .withUrl("https://localhost:7263/chatHub", { accessTokenFactory: () => TokenService.getAccess() })
      .withAutomaticReconnect()
      .build();

    connection.on(
  "ReceiveMessage",
  (id, chatId, senderId, text, sentAt) => {

    const msg = {
      id,
      chatId,
      senderId,
      text,
      sentAt
    };

    console.log("Incoming message:", msg);

    if (chatId == activeChatId) {
      addMessageToDOM(msg);
    }
  }
);

    try {
      await connection.start();
      console.log("SignalR connected");
    } catch (err) {
      console.error("SignalR connection failed:", err);
    }

    // ==========================
    // 1) Загрузка списка чатов
    // ==========================
    listView.innerHTML = "<p>Loading chats...</p>";

    let chats = [];
    try {
      chats = await ChatService.list();
    } catch (err) {
      listView.innerHTML = `<p style="color:red;">Error: ${err.message}</p>`;
      return;
    }

    if (!chats.length) {
      listView.innerHTML = "<p>No active chats found.</p>";
      return;
    }

    listView.innerHTML = chats
      .map(
        (c) => `
        <div class="chat-item" data-order-id="${c.orderId}" data-chat-id="${c.chatId}">
          <div class="chat-avatar">
            <img src="assets/icons/ic_user.svg"/>
          </div>
          <div class="chat-info">
            <div class="chat-name">Order #${c.orderId}</div>
            <div class="chat-preview">${c.lastMessage?.text || "No messages yet"}</div>
          </div>
        </div>
      `
      )
      .join("");

    // ==========================
    // 2) Открытие переписки
    // ==========================
    listView.onclick = async (e) => {
      const item = e.target.closest(".chat-item");
      if (!item) return;

      activeChatId = item.dataset.chatId;
      activeOrderId = item.dataset.orderId;

      listView.style.display = "none";
      convoView.style.display = "flex";

      await loadConversation(activeChatId);
    };

    // ==========================
    // 3) Загрузка истории
    // ==========================
    async function loadConversation(chatId) {
      messagesBox.innerHTML = "<p>Loading messages...</p>";

      let msgs = [];
      try {
        msgs = await ChatService.show(chatId); // /chat/history/{chatId}
      } catch (err) {
        messagesBox.innerHTML = `<p style="color:red;">${err.message}</p>`;
        return;
      }

      messagesBox.innerHTML = "";
      msgs.forEach(addMessageToDOM);
    }

    // ==========================
    // 4) Отправка сообщений
    // ==========================
    sendBtn.onclick = async () => {
      const text = input.value.trim();
      if (!text || !activeChatId) return;

      sendBtn.disabled = true;

      try {
        await ChatService.send(activeOrderId, text);
        input.value = "";
      } catch (err) {
        alert("Send error: " + err.message);
      }

      sendBtn.disabled = false;
    };

    // ==========================
    // 5) Функция для рендера сообщения
    // ==========================
    function addMessageToDOM(m) {
      const isMine = m.senderId === myId;
      const html = `
        <div class="message ${isMine ? "user" : "driver"}">
          <div class="message-bubble">${m.text}</div>
          <span class="message-time">${new Date(m.sentAt).toLocaleTimeString()}</span>
        </div>
      `;
      messagesBox.innerHTML += html;
      messagesBox.scrollTop = messagesBox.scrollHeight;
    }
  },
};


// =================== Settings Page ===================
export const SettingsPage = {
  async init() {

    // ===== DOM элементы =====
    const viewName = document.getElementById("view-name");
    const viewSurname = document.getElementById("view-surname");
    const viewEmail = document.getElementById("view-email");
    const viewPhone = document.getElementById("view-phone");

    const editBtn = document.getElementById("edit-profile-btn");
    const cngPasswordBtn = document.getElementById("change-password-btn")
    const deleteAccountBtn = document.getElementById("delete-account-btn")

    const profileView = document.getElementById("profile-view");
    const profileEdit = document.getElementById("profile-edit");

    const editNameInput = document.getElementById("edit-name");
    const editSurnameInput = document.getElementById("edit-surname");
    const editEmailInput = document.getElementById("edit-email");
    const editPhoneInput = document.getElementById("edit-phone");

    const saveBtn = document.getElementById("save-profile-btn");
    const cancelBtn = document.getElementById("cancel-edit-btn");

    Menu.init();

    // ===== 1) Загружаем реальный профиль =====
    let user = null;

    try {
      user = await AuthService.userInfo();

      viewName.textContent = user.name;
      viewSurname.textContent = user.surname;
      viewEmail.textContent = user.email;
      viewPhone.textContent = user.phoneNumber || "Not set";

      // Заполняем поля редактирования
      editNameInput.value = user.name;
      editSurnameInput.value = user.surname;
      editEmailInput.value = user.email;
      editPhoneInput.value = user.phoneNumber || "";

    } catch (err) {
      alert("Cannot load profile: " + err.message);
      return;
    }

    // ===== 2) Кнопка Edit =====
    editBtn.onclick = () => {
      profileView.style.display = "none";
      profileEdit.style.display = "block";
    };

    // ===== 3) Cancel =====
    cancelBtn.onclick = () => {
      profileEdit.style.display = "none";
      profileView.style.display = "block";

      // Вернуть старые значения
      editNameInput.value = user.name;
      editSurnameInput.value = user.surname;
      editEmailInput.value = user.email;
      editPhoneInput.value = user.phoneNumber;
    };

    // ===== 4) Save Changes =====
    saveBtn.onclick = async () => {

      const newName = editNameInput.value.trim();
      const newSurname = editSurnameInput.value.trim();
      const newEmail = editEmailInput.value.trim();
      const newPhone = editPhoneInput.value.trim();

      if (!newName) {
        return alert("Name cannot be empty");
      }

      try {
        await AuthService.updateProfile({
          email: newEmail,
          name: newName,
          surname: newSurname,
          phoneNumber: newPhone,
          carType: null,
          carColor: null,
          carNumber: null,
          cityName: null
        });

        // Обновляем локально
        user.name = newName;
        user.surname = newSurname;
        user.email = newEmail;
        user.phoneNumber = newPhone;

        // Обновляем UI
        viewName.textContent = newName;
        viewSurname.textContent = newSurname;
        viewEmail.textContent = newEmail;
        viewPhone.textContent = newPhone;

        tempEmail = newEmail;

        profileEdit.style.display = "none";
        profileView.style.display = "block";

        alert("Profile updated!");

        await Router.navigate("email-verification");

      } catch (err) {
        alert("Update failed: " + err.message);
      }
    };
    cngPasswordBtn.onclick = async () =>{
      await Router.navigate("recovery");
    }
    deleteAccountBtn.onclick = async () =>{
      if (confirm('Are you sure you want to cancel this order?')) {
        try{
          await AuthService.deleteProfile();
          await Router.navigate("login");
        }catch(err){
        alert("Failed to cancel order: " + err.message);
        }
      };
    }
  }

};

async function openOrderDetailsModal(order) {
  const modal = document.getElementById("order-details-modal");
  if (!modal) return;

  currentOrderData = order;
  // ===== STATUS перевод =====
  const statusMap = {
    0: "Pending",
    1: "Accepted",
    2: "InProgress",
    3: "Completed",
    4: "Cancelled",
    5: "Driver Coming",
    6: "Picking up",
    7: "Delivering"
  };

  // ===== Cargo =====
  const cargoTypes = [];
  if (order.standard) cargoTypes.push("Standard");
  if (order.valuable) cargoTypes.push("Valuable")
  if (order.fragile) cargoTypes.push("Fragile");
  if (order.heavy) cargoTypes.push("Heavy");

  const cargoText =
    cargoTypes.length > 0 ? cargoTypes.join(", ") : "Standard";

  // ===== Date/Time =====
  const dt = new Date(order.dateTime);

  // ===== Заполняем модалку =====
  document.getElementById("modal-order-number").textContent =
    `Order #${order.orderId}`;

  document.getElementById("modal-order-status").textContent =
    statusMap[order.status] || "Unknown";

  document.getElementById("modal-order-date").textContent =
    dt.toLocaleDateString();

  document.getElementById("modal-order-time").textContent =
    dt.toLocaleTimeString();

  document.getElementById("modal-location-from").textContent =
    order.pointA;

  document.getElementById("modal-location-to").textContent =
    order.pointB;

  document.getElementById("modal-distance").textContent =
    `${order.distance.toFixed(2)} km`;

  document.getElementById("modal-order-price").textContent =
    `${order.price} USD`;

  document.getElementById("modal-customer-additional").textContent =
    cargoText;
  
  document.getElementById("modal-order-cartype").textContent =
    `${order.vehicleType}`;

  if (order.driverId) {
    try {
      const driver = await AuthService.getUserInfoById(order.driverId);

      document.getElementById("modal-driver-name").textContent =
        `${driver.name} ${driver.surname}`;

      document.getElementById("modal-driver-email").textContent =
        `${driver.email}`;
      document.getElementById("modal-driver-phone").textContent =
        `${driver.phoneNumber}`;
      document.getElementById("modal-driver-carcolor").textContent =
        `${driver.carColor}`;
      document.getElementById("modal-driver-carnumber").textContent =
        `${driver.carNumber}`;
    } catch (err) {
      console.error("Driver load error:", err);

      document.getElementById("modal-driver-name").textContent =
        "Driver not found";

      document.getElementById("modal-driver-email").textContent =
        "-";
      document.getElementById("modal-driver-phone").textContent =
        "-";
      document.getElementById("modal-driver-carcolor").textContent =
        "-";
      document.getElementById("modal-driver-carnumber").textContent =
        "-";
    }
  } else {
    document.getElementById("modal-driver-name").textContent =
      "No driver assigned";

    document.getElementById("modal-driver-email").textContent =
        "-";
      document.getElementById("modal-driver-phone").textContent =
        "-";
  }
  // Показываем окно
  modal.classList.add("active");
}

function closeOrderDetailsModal() {
  const modal = document.getElementById('order-details-modal');
  if (modal) modal.classList.remove('active');
  currentOrderData = null;
}

function initOrderDetailsModal() {
  const modal = document.getElementById('order-details-modal');
  if (!modal) return;

  // Закрытие модального окна
  const closeBtn = document.getElementById('close-order-modal');
  if (closeBtn) {
    closeBtn.addEventListener('click', closeOrderDetailsModal);
  }
  
  modal.addEventListener('click', (e) => {
    if (e.target === modal) closeOrderDetailsModal();
  });

  const editOrderBtn = document.getElementById('modal-edit-order-btn');
  if (editOrderBtn) {
    editOrderBtn.addEventListener('click', async () => {
  if (!currentOrderData) return;

  // сохраняем заказ
  sessionStorage.setItem(
    "tempOrderData",
    JSON.stringify({
      orderId: currentOrderData.orderId,
      from: currentOrderData.pointA,
      to: currentOrderData.pointB,
      date: currentOrderData.dateTime.split("T")[0],
      time: currentOrderData.dateTime.split("T")[1].slice(0,5),
      vehicle: currentOrderData.vehicleType,
      cargo: SummaryPage.getCargoText(currentOrderData)
    })
  );

  sessionStorage.setItem("isEditMode", "true");

  closeOrderDetailsModal();
  await Router.navigate("order");
});

  }

  // Кнопка "Cancel order"
  const cancelBtn = document.getElementById('modal-cancel-order-btn');
  if (cancelBtn) {
    cancelBtn.addEventListener('click', async () => {
      if (confirm('Are you sure you want to cancel this order?')) {
        try{
          await OrderService.cancel(currentOrderData.orderId);
          if (typeof SummaryPage.init === "function") {
          await SummaryPage.init();
        }
        }catch(err){
        alert("Failed to cancel order: " + err.message);
      }

        closeOrderDetailsModal();
        
      }
    });
  }

  // Кнопка "Chat"
  const chatBtn = document.getElementById('modal-chat-btn');
  if (chatBtn) {
    chatBtn.addEventListener('click', async () => {
      closeOrderDetailsModal();
      await Router.navigate('chat');
    });
  }

  // Обработчик клика на карточки заказов
  const ordersGrid = document.getElementById("orders-grid-container");
  if (ordersGrid) {
    ordersGrid.addEventListener('click', async (e) => {
      const card = e.target.closest('.order-card');
      if (!card) return;

      const orderId = card.dataset.id;
      try{
        const fullOrder = await OrderService.getById(orderId);
        openOrderDetailsModal(fullOrder);
      }catch(err){
        alert("Failed to load order details: " + err.message);
      }

    });
  }
}

// =================== Recovery Page ===================
export const RecoveryPage = {
  init() {
    const backBtn = document.getElementById("back-to-login");
    if (backBtn) {
      backBtn.onclick = async () => {
        await Router.navigate("login");
      };
    }
    const sendCodeBtn = document.getElementById("send-code-btn");
    const saveBtn = document.getElementById("save-password-btn");
    if (!saveBtn) return;
    if (!sendCodeBtn) return;
    const resendLink = document.getElementById("resend-code-link");
    if (resendLink) {
      resendLink.onclick = async () => {
        alert("Verification code has been resent to your email!");
      };
    }

    // Toggle password visibility
    const toggleNewPassword = document.getElementById("toggle-new-password");
    const newPasswordInput = document.getElementById("new-password");
    const newPasswordIcon = document.getElementById("new-password-icon");

    if (toggleNewPassword && newPasswordInput && newPasswordIcon) {
      toggleNewPassword.onclick = () => {
        const isPassword = newPasswordInput.type === "password";
        newPasswordInput.type = isPassword ? "text" : "password";
        newPasswordIcon.src = isPassword
          ? "assets/icons/ic_visibility_on.svg"
          : "assets/icons/ic_visibility_off.svg";
      };
    }

    sendCodeBtn.onclick = async () =>{
      const email = document.getElementById("recovery-email").value.trim();
      if (!email) {
        return alert("Please enter email");
      }
      const resp = await AuthService.requestPasswordReset({email});
      if (!resp.success) return alert(resp.error);
    }

    saveBtn.onclick = async () => {
      const code = document.getElementById("recovery-code")?.value.trim();
      const newPassword = newPasswordInput?.value.trim();
      const email = document.getElementById("recovery-email")?.value.trim();
      if (!code || !newPassword ) {
        return alert("Please fill all fields");
      }

      const res = await AuthService.resetPassword(email, code, newPassword);
      if (!res.success) return alert(res.error);

      alert("Password successfully changed!");
      await Router.navigate("login");
    };
  }
};

export const InstallPage = {
  currentSlide: 0,
  totalSlides: 3,

  init() {
    this.currentSlide = 0;
    this.setupCarousel();
    this.updateCarousel();
  },

  setupCarousel() {
    const prevBtn = document.getElementById("carousel-prev");
    const nextBtn = document.getElementById("carousel-next");

    if (prevBtn) {
      prevBtn.onclick = () => this.prevSlide();
    }

    if (nextBtn) {
      nextBtn.onclick = () => this.nextSlide();
    }

    // Auto-play carousel
    this.startAutoPlay();
  },

  prevSlide() {
    this.currentSlide = (this.currentSlide - 1 + this.totalSlides) % this.totalSlides;
    this.updateCarousel();
  },

  nextSlide() {
    this.currentSlide = (this.currentSlide + 1) % this.totalSlides;
    this.updateCarousel();
  },

  updateCarousel() {
    const screens = document.querySelectorAll(".screen");
    
    screens.forEach((screen, index) => {
      screen.classList.remove("active", "prev", "next");
      
      if (index === this.currentSlide) {
        screen.classList.add("active");
      } else if (index === (this.currentSlide - 1 + this.totalSlides) % this.totalSlides) {
        screen.classList.add("prev");
      } else if (index === (this.currentSlide + 1) % this.totalSlides) {
        screen.classList.add("next");
      }
    });
  },

  startAutoPlay() {
    setInterval(() => {
      this.nextSlide();
    }, 4000); // Change slide every 4 seconds
  }
};

function triggerPreview() {
  if (previewTimeout) clearTimeout(previewTimeout);
  previewTimeout = setTimeout(updatePricePreview, 600);
}

async function updatePricePreview() {
  const from = document.getElementById("from-location")?.value.trim();
  const to = document.getElementById("to-location")?.value.trim();
  const vehicleType = document.querySelector('input[name="vehicle-type"]:checked')?.value;

  if (!from || !to || !vehicleType) return;

  const cargoCheckboxes = document.querySelectorAll(".cargo-checkbox");
  let standard = false;
  let valuable = false;
  let fragile = false;
  let heavy = false;

  cargoCheckboxes.forEach(cb => {
    const label = cb.closest(".cargo-option")?.querySelector("strong")?.innerText?.toLowerCase();
    if (cb.checked && label) {
      if (label.includes("standard")) standard = true;
      if (label.includes("valuable")) valuable = true;
      if (label.includes("fragile")) fragile = true;
      if (label.includes("heavy")) heavy = true;
    }
  });

  try {
    const distance = await getDistanceKm(from, to);
    console.log(distance);
    const price = calculatePrice(distance, vehicleType, standard, valuable, fragile, heavy);

    const priceElement = document.getElementById("price");
    if (priceElement) {
      priceElement.innerText ="Price:" + price + "$";
    }
  } catch (e) {
    console.error("Distance error:", e);
  }
}


function getDistanceKm(pointA, pointB) {
  return new Promise((resolve, reject) => {
    const service = new google.maps.DistanceMatrixService();

    service.getDistanceMatrix(
      {
        origins: [pointA],
        destinations: [pointB],
        travelMode: "DRIVING",
        unitSystem: google.maps.UnitSystem.METRIC
      },
      (response, status) => {
        if (status !== "OK") {
          reject(status);
          return;
        }

        const element = response.rows[0].elements[0];

        if (element.status !== "OK") {
          reject(element.status);
          return;
        }

        const meters = element.distance.value;
        resolve(meters / 1000);
      }
    );
  });
}

function calculatePrice(distanceKm, vehicleType, standard, valuable, fragile, heavy) {
  const vehicleRates = {
    "Small Van": 3.0,
    "Medium Van": 4.0,
    "Large Van": 5.0,
    "Luton Van": 6.0
  };

  const standardFee = 20.0;
  const valuableFee = 10.0;
  const fragileFee = 10.0;
  const heavyFee = 15.0;
  const minPrice = 50.0;
  
  const rate = vehicleRates[vehicleType];
  if (!rate) throw new Error("Unknown vehicle type");

  let price = distanceKm * rate;

  if (standard) price += standardFee;
  if (valuable) price += valuableFee;
  if (fragile) price += fragileFee;
  if (heavy) price += heavyFee;

  price = Math.max(minPrice, price);
  console.log(price);
  return Math.ceil(price);
}
