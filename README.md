# Musicify

Musicify is an Android application that allows users to explore, play, and manage their favorite music tracks and playlists. The app integrates with Firebase for authentication and Firestore for data storage, and it uses Retrofit for API calls to fetch music data.

## Features

- User Authentication: Register, login, and manage user profiles.
- Explore Music: Browse and search for songs, albums, and artists.
- Play Music: Stream music with a built-in media player.
- Manage Playlists: Create, update, and delete playlists.
- Liked Songs: Add or remove songs from the liked songs list.
- Lyrics: View lyrics for the currently playing song.
- Feedback: Send feedback and error reports.

## Project Structure

```
.
├── app/                       # Main application module
│   └── src/
│       └── main/
│           ├── java/         # Java source files
│           │   └── com/example/musicapp/
│           │       ├── activities/    # Activity classes
│           │       ├── adapter/       # RecyclerView adapters
│           │       ├── fragment/      # Fragment classes
│           │       ├── manager/       # Manager classes
│           │       ├── model/         # Data models
│           │       └── viewmodel/     # ViewModels
│           ├── res/          # Resource files
│           └── AndroidManifest.xml
├── gradle/                   # Gradle wrapper files
├── .gitignore               # Git ignore rules
├── build.gradle.kts         # Project build script
├── gradle.properties        # Gradle configuration
├── settings.gradle.kts      # Gradle settings
└── local.properties         # Local SDK configuration
```


## Getting Started

### Prerequisites

- Android Studio
- Java 8 or higher
- Firebase account

### Installation

1. Clone the repository:
   ```sh
   git clone https://github.com/sloweyyy/musicify.git
   ```
2. Open the project in Android Studio.
3. Add your `google-services.json` file to the `app` directory.
4. Sync the project with Gradle files.

### Running the App

1. Connect an Android device or start an emulator.
2. Click on the "Run" button in Android Studio.

## Usage

- Register or log in to your account.
- Browse and search for music.
- Play songs and view lyrics.
- Create and manage playlists.
- Add songs to your liked songs list and view them in the favorite songs tab.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgements

- [Firebase](https://firebase.google.com/)
- [Retrofit](https://square.github.io/retrofit/)
- [Glide](https://github.com/bumptech/glide)
- [Spotify Web API](https://developer.spotify.com/documentation/web-api/)
