## Wie starte ich yaprnn mit GUI? ##
Über die Main-Klasse in /yaprnn. Dafür stehen yaprnn.sh (für Unix), yaprnn.bat (für Windows) und yaprnn.jar an sich zur Verfügung.

## ~~Ich bekomme eine "NullPointerException"~~~~, was soll ich machen?~~ ##
~~Die GUI lädt, noch bevor die MainView sichtbar wird, die Icons aus den png-Dateien. Sie liegen im src/yaprnn/gui/view Verzeichnis und müssen leider manuell in build/yaprnn/gui/view kopiert werden.~~ fixed, die png-Dateien werden nun automatisch durch build.xml in das richtige Verzeichniss kopiert.

## Was funktioniert noch nicht? ##
Folgende Features gehen noch nicht:
"Save dataset" - Buttons sind zur Zeit nicht sichtbar aber vorhanden
"Load dataset"