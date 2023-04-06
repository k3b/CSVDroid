# 📅🔍_CSVDroid for Android - View and Search content of CSV-File in a table view: 

## Features

* **Filemanager support:** If you select a CSV-File you can open it with 📅🔍_CSVDroid using the view/edit/send/sendto command.
* **Standalone mode:** If started without a CSV-File 📅🔍_CSVDroid will try to load the last used CSV-File. 
  * If loading fails it presents the system-filechooser 📂 to pick a CSV-File to be opend.
* **Search/Filter:** When using the toolbar-s "🔍-search" only rows that contain the search-string will be displayed.
* **Sorting:** Sort data by clicking on the corresponding column header.

## CSV Support

* **Linedelimiter:** Each CSV-line seperated by **<nl>-char** will become a row in the table. 
  * if the line is not empty
  * if the line is not a comment or 
  * if there no column in Textdelimiter-Mode.
* **Comment:** CSV may contain contain "comment lines" if the line starts with "# ". 
  * "comment lines" will not become rows oft it-s own.
* **Header:** The first non-empty non-comment-line will become the "table-header"
* **Columnseperator:** CSV columns must be seperated by <tabulator>-char or one of these chars: ,;:| 
  * The seperator is infered from the Header.
* **Textdelimiter:** If a CSV column starts and ends with the Textdelimiter-Char the column may contain any char.
  * If a Header-column starts with " or ' this will become the Textdelimiter-char. Default is " .
  * This way one column may have multi-line content or contain the Columnseperator-char.
  * The CSV-parser is in **Textdelimiter-Mode** while there was the Textdelimiter char at the column start and the finishing Textdelimiter has not been reached yet.
  * A double Textdelimiter ( **""** or **''** ) will be interpretet as column containing the Textdelimiter.
  * **Example CSV:** ...,"This column contain the **Textdelimiter** ( **""** ) and the **Columnseperator** ( **,** )",....
* **File Encoding**: CSV-File must be encoded in **"UTF-8"**. Other Encodings are currently not supported.
* **Currently there is no support to edit and save changes**. This may change in the future.