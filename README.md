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

- **Storage Analysis**:
  - Analyze storage usage with a detailed breakdown by file type (Images, Audio, Videos, Documents, Archives, Others).
  - View total storage, free space, and usage percentage.
  - List large files (>10 MB) with their sizes.
  - Placeholder for Recycle Bin size (to be implemented).

- **Media Library**:
  - Access a "Library" tab to quickly view media files (Images, Audio, Videos, Documents, New Files) from the entire device.
  - Tap on a media type to see all files of that type in a search-like interface.

- **Apps List**:
  - View a list of installed apps with their sizes.
  - Toggle between "Downloaded" and "All" apps using tabs.

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
   - Click

## License
This project is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives License - see the [LICENSE](LICENSE) file for details.