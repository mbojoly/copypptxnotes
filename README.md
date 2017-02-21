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

Known limitation: If two slides in the document have the same first string, the second one is ignored and a warning is emitted on the console output.





