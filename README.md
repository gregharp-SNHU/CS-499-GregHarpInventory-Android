Inventory App built for Android

## Project Overview
I chose to build an inventory management application for this project. My app supports a multi-user environment (although just local to the mobile device for now) which allows the users to track the quantity of each item in the inventory, generate reports of all items, low-stock items, and out-of-stock items, and receive SMS notifications of out-of-stock items.

## Requirements and Goals
- **Account management** - login and persistent session support.
- **Inventory tracking** - allow the users to add, edit, adjust quantities, and delete items.
- **SMS functionality** - send notification of out-of-stock items.
- **User-friendly navigation** - enable the user to move cleanly between major activities.

## Screens and Features
The primary screens in the app include:
- **Login Screen** Îndles authentication and saves user state with SharedPreferences.
- **Create Account Screen** Î#llows creation of new user accounts including specification of role and contact phone number.
- **Account Screen** Î CS-360 Final Project Îys the current user name and role, allows updating of SMS notification preferences.
- **Inventory Screen** Îys a scrolling list of inventory items using a RecyclerView, allowing users to adjust quantities and add, edit, or delete items.
- **Reports Screen** Îys a scrolling report of all items and quantities, or filtered reports showing low stock or out-of-stock items.

I made a point of keeping the UI design simple, touch-friendly, and speedy. All text was isolated into a strings.xml allowing easy localization of the application. A navigation bar allows direct access to the primary features from all points.

## Development Approach
I approached coding in an iterative and modular way. I began with basic sketches and descriptions of the primary functions. I proceeded from there to UI design in Android Studio followed by coding to support the UI. I then added the back-end and logic to provide the user and inventory databases. Coding was completed in small steps, testing each portion along the way. I focused on reusable and modular code wherever possible. This makes development more reliable as well, because recycling already tested code cuts down on technical debt.

One of the challenges I encountered initially was performance, because refreshing the display started taking too long as the inventory database grew. Android Studio offered a warning against using notifyDataSetChanged() which was the example from our text. With a little research, I found that DiffUtil improved performance by only updating the components that changed in the RecyclerView. Finding and implementing this solution involved independent research and experimentation, which I felt I successfully performed. I was able to learn enough on my own to improve the performance of my app substantially.

Naturally, I look forward to coming back to this app, which I most likely will as part of my capstone project. A multi-user inventory app would normally use a cloud-based database rather than local device storage. That, in and of itself, will be interesting to implement. However, I think the more interesting and new aspect of that will be developing the server-side notifications engine (whether that uses SMS or Android native notifications) to inform users of low and out-of-stock conditions. It should be fun!
