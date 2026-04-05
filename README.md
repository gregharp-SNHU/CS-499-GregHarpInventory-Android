# Harp Inventory Management App

An Android inventory management application originally developed for CS-360 
Mobile Architecture and Programming and enhanced across three milestones as 
part of the CS-499 Computer Science Capstone.

## Project Overview
This app supports a true multi-user environment backed by Firebase Cloud
Firestore, allowing authenticated users to track inventory quantities, generate
reports, and receive SMS notifications for out-of-stock items. User
authentication and role-based access control are handled by Firebase
Authentication and Firestore, replacing the original local SQLite
implementation.

## Features
- **Firebase Authentication** — secure login, persistent sessions, and
  password reset via email
- **Role-based access control** — three roles (User, Manager, Owner) with
  different levels of access
- **Cloud-hosted inventory** — inventory data stored in Firebase Cloud
  Firestore and shared across all authenticated users in real time
- **Inventory management** — add, edit, adjust quantities, and delete items
- **Search, filter, and sort** — real-time name search, stock status filter
  with adjustable low-stock threshold, quantity range filter, and four sort
  options
- **Inventory reports** — all items, low stock, and out-of-stock reports with
  adjustable threshold
- **User management** — Owners can view, change roles of, and delete other
  users
- **SMS notifications** — optional SMS alerts when items reach zero stock
- **Sample data loader** — Owners can seed the inventory with sample items

## Requirements and Goals
- **Account management** - login and persistent session support.
- **Inventory tracking** - allow the users to add, edit, adjust quantities, and delete items.
- **SMS functionality** - send notification of out-of-stock items.
- **User-friendly navigation** - enable the user to move cleanly between major activities.

## Screens and Features
- **Login** — handles and saves user state
- **Create Account** — creates account and basic user profile
- **Account** — displays username and role, manages phone number and SMS
  preferences, provides access to user management and sample data loading
- **Inventory** — scrolling RecyclerView of inventory items with real-time
  filtering and sorting; supports add, edit, quantity adjustment, and delete
- **Reports** — filtered inventory reports by stock status with adjustable
  low-stock threshold available to Managers and Owners
- **Manage Users** — Owner-only screen for managing user roles and accounts

## Architecture

The app follows an MVVM architecture using Android Jetpack components:

- **Model** — `InventoryItem`, `ReportRow`, `DbKeys`, `Roles`, `Prefs`
- **Data layer** — `ItemRepository` backed by a Firestore real-time snapshot
  listener
- **ViewModels** — `ItemViewModel`, `ReportsViewModel` expose LiveData to the
  UI
- **UI** — Activities and RecyclerView adapters use `DiffUtil` for efficient
  list updates
- **Utilities** — `InventoryFilter` (static filtering and sorting with
  Timsort), `SMSNotifier`, `Toaster`

## Security

Firestore security rules enforce that inventory writes require both a valid
Firebase Authentication credential and a provisioned role document in the
users collection. This ensures that authentication alone is insufficient to
modify data as explicit role provisioning by an Owner is required.