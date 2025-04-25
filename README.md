# Dad's Ad-Free File Manager

## Description
Dad's Ad-Free File Manager is a simple, lightweight, and ad-free file management application for Android. Designed to provide an intuitive and efficient way to browse, manage, and organize files on your device, this app offers a clean user interface and powerful features without any advertisements. It’s perfect for users who need a straightforward file manager without unnecessary bloat.

## Features
- **File Browsing**:
  - Navigate through your device's external storage with a clean, folder-based interface.
  - Supports navigation to parent directories using the back button.
  - Displays files and folders in a `RecyclerView` with icons for easy identification.

- **File Operations**:
  - **Copy/Move Files**: Copy or move files and folders to different directories using a context menu.
  - **Delete Files**: Delete files or folders with a confirmation dialog to prevent accidental deletion.
  - **Rename Files**: Rename files or folders directly from the context menu.
  - **File Details**: View detailed information about a file, including its name, path, size, and last modified date (available as a fallback when a file cannot be opened).

- **Sorting**:
  - Sort files and folders by multiple criteria: name, size, date modified, or file type.
  - Supports ascending and descending order for each sorting criterion.
  - Directories are always displayed before files for better organization.

- **Search Functionality**:
  - **Recursive Search**: Search for files not only in the current directory but also in all subdirectories.
  - **Advanced Filters**:
    - Filter search results by file type (e.g., "txt", "pdf", "jpg").
    - Filter by file size (e.g., "< 1 MB", "1-10 MB", "> 10 MB").
    - Filter by date modified (e.g., "Today", "This Week", "Older").
  - **Highlight Matches**: The matching portion of the file name is highlighted in orange in the search results.
  - **Debounced Search**: A 300ms delay is applied to search filtering to improve performance during rapid typing.
  - Displays the full file path for search results from subdirectories to help identify file locations.

- **File Opening**:
  - Tap on a file to open it in its default application (e.g., PDFs in a PDF viewer, images in a gallery app, text files in a text editor).
  - Falls back to showing file details with a Toast message if no app can open the file.

- **Permissions Handling**:
  - Requests necessary storage permissions to access files.
  - Supports Android 11+ scoped storage with "All Files Access" permission.

- **Ad-Free Experience**:
  - No advertisements, ensuring a distraction-free user experience.

## Screenshots


## Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/dads-ad-free-file-manager.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **File > Open** and navigate to the cloned repository folder.
   - Click **OK** to open the project.
3. **Sync and Build**:
   - Click **Sync Project with Gradle Files** to download dependencies.
   - Click **Build > Rebuild Project** to ensure the project builds successfully.
4. **Run the App**:
   - Connect an Android device or start an emulator.
   - Click **Run > Run 'app'** to install and launch the app on your device/emulator.
   - Grant the necessary storage permissions when prompted.

## Usage
1. **Launch the App**:
   - Open the app to see the `HomeActivity` with quick access buttons for "Main Storage" and "Downloads".
2. **Browse Files**:
   - Tap on "Main Storage" or "Downloads" to view the file list in `MainActivity`.
   - Tap on a folder to navigate into it, or use the back button to go to the parent directory.
3. **Sort Files**:
   - Tap the sort icon in the toolbar to choose a sorting criterion (name, size, date, type) and order (ascending/descending).
4. **Search for Files**:
   - Tap the search icon in the toolbar to start searching.
   - Enter a query to search recursively through all subdirectories.
   - Tap the filter icon to apply advanced filters (file type, size, date modified).
   - Matching file names are highlighted, and full paths are shown for files in subdirectories.
5. **Manage Files**:
   - Long-press on a file or folder to open the context menu.
   - Choose to copy, move, delete, or rename the file/folder.
   - For copy/move, navigate to the destination folder and tap it to complete the operation.
6. **Open Files**:
   - Tap on a file to open it in its default application.
   - If the file cannot be opened, a Toast message will appear, and the file details will be shown in a dialog.

## Contributing
Contributions are welcome! If you’d like to contribute:
1. Fork the repository.
2. Create a new branch for your feature or bug fix (`git checkout -b feature/your-feature-name`).
3. Make your changes and commit them (`git commit -m "Add your feature"`).
4. Push to your branch (`git push origin feature/your-feature-name`).
5. Open a pull request with a detailed description of your changes.

## License
This project is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives License - see the [LICENSE](LICENSE) file for details.