# Gazelka

## Description
Gazelka is a web and mobile application designed to simplify the flat-to-flat moving process for both customers and drivers.  

The platform allows customers to quickly order cargo transportation services, while drivers can receive, manage, and complete orders within a single system.

> This project was developed as a Bachelor’s degree thesis in Information Technology (English) at Collegium da Vinci (Poznań, Poland) and received a 5.0 (very good) grade.

---

##  Design

The UI/UX of the application was designed in Figma.

 View design: https://www.figma.com/design/EWLLfTKqXs01303fshifli/Gazelka?node-id=0-1&t=7rhcebbBx8vg3lMS-1


## 💬 Real-time Chat

The application includes real-time messaging between customer and driver, powered by SignalR.

<p align="center">
  <img src="assets/chat-demo.gif" width="400"/>
</p>


## Features

### General
- Email verification via confirmation code during registration  
- Account creation, update, and deletion  

---

### Customer Interface (Web & Mobile)

#### Order Creation
- Select pickup and delivery locations using a map  
- Choose date and time for pickup  
- Select vehicle type  
- Define cargo parameters  
- Automatic real-time price calculation *(based on vehicle type, cargo, and distance)*  

#### Other Features
- Edit orders  
- Real-time order tracking  
- Order cancellation  
- Real-time chat with driver  
- Push notifications  
- Order history  

---

### Driver Interface (Mobile)

#### Core Features
- Set working city *(via Google Maps API)*  
- Smart order search based on:
  - Driver location  
  - Vehicle type  

#### Order Management
- View order details  
- Update order status during execution  
- Real-time chat with customer  
- Push notifications  
- Order history  

---

## Tech Stack

### Mobile (Android)
- Kotlin  
- Jetpack Compose  
- Google Maps API  
- SignalR  
- Firebase Cloud Messaging  

### Web
- HTML5  
- CSS3  
- JavaScript (ES6+)  
- Google Maps API  
- SignalR  

### Backend
- C# (.NET)  
- JWT Authentication  
- Swagger  
- Firebase Cloud Messaging  
- SQLite  
- MailKit  
- bcrypt  

---
### Project Structure

GazelkaWeb/  -  Web application

GazelkaServer/  -  Server-side application

GazelkaMobile/  -    Mobile application

---
### Team

Artem Dzhula — Backend

Oleksii Fronin — Mobile App, UI/UX Design

Vitalii Sakhno — Web App

---

### Project Status 
Bachelor’s Thesis Project (completed)
