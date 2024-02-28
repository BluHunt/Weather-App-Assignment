# Weather App Android Project

![Weather App Demo](logo.png)

This Android application fetches weather information using the OpenWeatherMap API and displays it for multiple locations. It supports fetching data for 'My Current Location,' as well as predefined locations like New York, Singapore, Mumbai, and more. The app utilizes Retrofit for network requests and SharedPreferences for local data storage.

## Features

- **Fetch Weather Data:** Fetches weather data by coordinates and location names using Retrofit.
- **Multiple Locations:** Displays weather information for multiple locations.
- **Offline Access:** Saves weather data in SharedPreferences for offline access.
- **Swipe-to-Refresh:** Supports swipe-to-refresh functionality for updating weather data.
- **Detailed Weather:** More details about weather can be seen by clicking on each location card.

## Technologies & Components

- **Retrofit:** Used for making API requests to fetch weather data.
- **SwipeRefreshLayout:** Implemented for updating weather data by swiping down.
- **SharedPreferences:** Utilized to store and retrieve weather data for offline access.
- **FusedLocationProviderClient:** Handles location services to fetch the device's current coordinates.

## Screenshots

Include screenshots of your app here, showcasing different features and UI elements.

## Usage

### Prerequisites

- Android Studio
- OpenWeatherMap API key

### Note:

Ensure to replace the API key for OpenWeatherMap (`apiKey = "YOUR_API_KEY"`) in the code with a valid key obtained from the OpenWeatherMap website.

Feel free to explore and contribute to enhance this weather app for Android!

#### Additional Resources:
- [Weather App Recording](https://drive.google.com/file/d/187JcfIy0Za38p3NEJ-JbVas1nFqs4Ce3/view?usp=sharing): Watch a recording of the weather app in action.
- [APK File](https://drive.google.com/file/d/10Uc1btdkAacRNjLsCFoJ_OlXYgFflM89/view?usp=drive_link): Download the APK file for installation.
