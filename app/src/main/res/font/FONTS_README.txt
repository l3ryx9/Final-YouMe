Polices requises — placer les fichiers .ttf dans ce dossier :

1. dancing_script_bold.ttf
   Source : https://fonts.google.com/specimen/Dancing+Script
   Utilisation : Logo "YouMe", titres principaux

2. nunito_regular.ttf
   Source : https://fonts.google.com/specimen/Nunito
   Utilisation : Corps de texte

3. nunito_semibold.ttf
   Utilisation : Sous-titres, étiquettes

4. nunito_bold.ttf
   Utilisation : Titres de sections, boutons

Commande rapide (curl depuis Google Fonts) :
  curl -o dancing_script_bold.ttf "https://fonts.gstatic.com/s/dancingscript/v25/If2cXTr6YS-zF4S-kcSWSVi_sxjsohD9F50Ruu7BMSo3Rep8.ttf"
  curl -o nunito_regular.ttf "https://fonts.gstatic.com/s/nunito/v25/XRXI3I6Li01BKofiOc5wtlZ2di8HDDshdTQ3j6zbXWjgeg.woff2"

Alternative : utiliser les polices système (sans téléchargement) en remplaçant
DancingScriptFamily et NunitoFamily par FontFamily.Default dans Typography.kt
