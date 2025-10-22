# Learn my lines

An app to help actors learn their lines.

## Description
<a href='https://play.google.com/store/apps/details?id=com.datoh.learnlines&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=200/></a>

Learn Lines is the essential application for actors, students, and anyone who needs to memorize text. Designed to make learning your lines simple, efficient, and enjoyable, this app transforms your script into an interactive learning tool.

Say goodbye to cumbersome paper scripts and the endless search for a rehearsal partner. With Learn Lines, you have everything you need to master your roles right in your pocket.

Core Features:

- Import Your Scripts: Easily import any play or text from a simple .txt file. The app's intelligent parser automatically detects characters and their lines, allowing you to get started in seconds.

- Read Mode: Browse your entire script in a clean, uncluttered interface. Effortlessly navigate through scenes and dialogues to familiarize yourself with the material at your own pace.

- Interactive Learn Mode: This is where you'll build confidence and perfect your performance. Select your character and the app will hide your lines, presenting you only with your cues. Challenge yourself by reciting your line, then tap the screen to reveal the text and verify your accuracy. It's a simple and effective way to test your memory.
- Works Entirely Offline: All your scripts are stored directly on your device. Learn your lines anytime, anywhere, without needing an internet connection. Perfect for commuting, waiting for an audition, or last-minute backstage practice.

Whether you're a professional actor preparing for a major role, a drama student tackling a classic play, or simply someone who loves theatre, Learn Lines is the tool you need to walk into any performance feeling prepared and confident.

Download Learn Lines today and revolutionize the way you learn.

## How to format a play file

To format a plain text file for the "Learn my lines" application, the app's intelligent parser expects a simple, structured layout that clearly delineates the play's title, acts, scenes, characters, and their dialogue.

Here's a short description of the expected format:

*   **File Type**: The file should be a plain text file (e.g., `.txt`).
*   **Play Title**: The play's title should be on the first line, prefixed with a single `#` (e.g., `# My Wonderful Play`).
*   **Acts**: Indicate a new act by starting a line with `##` followed by its name (e.g., `## Acte 1`).
*   **Scenes**: Indicate a new scene by starting a line with `###` followed by its name (e.g., `### Scene 1`). Scenes should be nested within acts.
*   **Character Lines**: Each character's dialogue should begin with the character's name, followed by a colon, and then their line. The line can't span multiple physical lines in the file. Empty lines are ignored.
*   **Stage Directions**: If you have stage directions that appear between lines of dialogue, they should be added to the beginning of the following line, often enclosed in parentheses.

You can find a full example of a formatted play file [here](https://github.com/Datoh/LearnLines/blob/main/app/src/main/res/raw/le_jeu_de_l_amour_et_du_hasard.txt).

Here's an example of how a play file should be formatted:

```
# My Wonderful Play
## Acte 1
### Scene 1
John: (entering) Good morning, world! What a beautiful day to learn my lines.
Mary: (happy) Indeed, John. The sun is shining brightly.
John: I hope I remember all my cues.

### Scene 2
Waiter: Can I get you anything?
Mary: Just a coffee, please.
```
