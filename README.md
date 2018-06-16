# CopyPptxNotes : A tool to copy and merge notes between two PPTX files

[![Build Status](https://travis-ci.org/mbojoly/copypptxnotes.svg)](https://travis-ci.org/mbojoly/copypptxnotes)

## Description
CopyNotes allows to extract, copy and merge notes of pptx files. 
Notes can be merged with the notes of an existing files
Notes can be exported to a xml file with a custom format or imported from this xml format and merged into an existing pptx document.
The target file is updated.

## Usage
```
java -jar copypptxnotes-<version>-jar-with-dependencies.jar  -s <source> -t <target>
```

## Getting started
CopyPptxNotes requires Java8+ and maven 3.3.3+ to be built.

## Documentation
Allowed source formats are xml and pptx.
Allowed target formats are xml for a new or existing file, pptx only for an existing file only.

Notes are extracted both from souce and target document. The first string of the slide is used as a key. When slides in the source and target document have the same first string they are considered as the same slide. Notes from the source slides are then added above the existing notes of the target slide's notes. Source and target notes are separated by such line `=== Original comments from <path of target document===`.

If two slides in the document have the same first string, an approaching matcher algorithm is used. For example, if both in the source and target document, there are two slides with the title "Title", the relative position is used to copy the notes. Relative position is added to the key. So for those slides, logs print key in the forme "<first String> + <relative position>".
If in the target document some slide have no corresponding first string (named missing string), the closest string in the first strings of the source slides is identified. Close is defined according to  the levenstein measure. The the levenstein distance between this identified string and all the first string of the target slides is computed. If the closest string matches the missing string, then the notes are copied. In the opposite the slide with the missing string is left without not and a warning is emitted.    

Known limitation:
- If a slide has no string, its notes are ignored.
- Approaching algorithm can lead to notes copied to the wrong slides in some very rare cases.



