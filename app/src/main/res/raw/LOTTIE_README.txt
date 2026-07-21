Fichiers Lottie requis — placer les fichiers .json dans ce dossier :

1. youme_logo.json
   Utilisation : AnimatedYouMeLogo.kt (mascotte animée sur écrans auth)
   Source : Créer via After Effects + plugin Bodymovin, ou LottieFiles.com
   Fallback : YouMeLogoFallback() utilise l'image statique ic_youme_logo

2. confetti.json (optionnel)
   Utilisation : DailyCatchupModal.kt (animation succès)
   Source : https://lottiefiles.com/search?q=confetti

3. loading.json (optionnel)
   Utilisation : écrans de chargement
   Source : https://lottiefiles.com/search?q=loading

En attendant les fichiers Lottie, les composants utilisent automatiquement
les fallbacks en Compose natif (icônes Material + animations simples).
