# Tests d'intégration
### Mathias La Rochelle & Marcelo Amarilla

## Modification de la Github Action

Pour forcer l'échec du workflow "Build" dans le cas où le score de mutation baisse, nous utilisons sept "steps" (étapes) :
1. *Build external modules* : Nécessaire pour éviter les erreurs liées à l'interdépendance des modules
2. *Fetch full history* : Nécessaire pour avoir accès aux versions précédentes (lié à l'étape suivante)
3. *Checkout before push* : Retourne à la version précédente
4. *Pitest report : initially* : Calcule le score de mutation avant le commit.
5. *Checkout after push* : Revient à la version actuelle (après le push)
6. *Pitest report : after* : Calcule le score de mutation après le commit.
7. *Comparison* : Compare les scores de mutation et lance une erreur si le score de la sixième étape est plus petit que celui de la quatrième étape.

### Modifications apportées et justifications
Nous avons commencé par ajouter pitest à tous les modules de Graphhopper (il faut analyser le score de mutation par module). Il y a quelques problèmes que nous avons identifiés :
- On ne peut pas lancer pitest pour un module entier comme core : il y a une limite de 6 heures pour un workflow (nous l'avons appris en essayant). Les tests de mutations consomment beaucoup de ressources. Il faut viser des classes précises
- Identifier quelles classes viser n'est pas toujours évident. Il y a quelques cas à considérer.
    - si le fichier modifié est une classe de production (pas un fichier test), alors on ne test (test de mutation) que cette classe.
    - si le fichier modifié est une classe test avec comme nom {production_class_name}Test.java, alors on peut facilement trouver la classe associée en retirant "Test". Mais il faut quand même (par sécurité) s'assurer que cette classe existe, faute de quoi on a recours à la solution du cas suivant.
    - si le fichier modifié est une classe test qui ne se nomme pas "{production_class_name}Test.java" **OU** si ce *pattern* de nom est respecté, mais qu'il n'existe aucune classe de production avec ce nom, alors on ne peut pas identifier à coup sûr à quelle classe de production cette classe test est associée. On lance donc pitest sur toutes les classes de ce module. Ce troisième cas est notamment pertinent pour les classes de tests d'intégration (IT); et on en retrouve notamment dans reader-gtfs.
    - Il y a aussi le cas des fichiers non `.java`. Aucune analyse de mutation n'est exécutée pour ces fichiers modifiés (ex: les readme).

#### Exécution de pitest
Pour les étapes 4 et 6, on fait exactement les mêmes commandes.

On identifie les fichiers modifiés, et on se sert d'une hashmap où la clé correspond au module et la valeur correspond aux classes à viser par l'analyse de mutation. La détermination de cette valeur (une *string*) dépend des cas énoncés plus haut (classe visée facilement identifiable ou impossible à identifier). Cette valeur servira par la suite comme argument de la commande pour lancer pitest dans le module associé (la clé associée à cette valeur).
S'il est impossible d'identifier au moins une classe dans ce module, l'argument (la valeur) sera "com.graphhopper.*" (ce qui signifie "vise toutes les classes de ce module").

On lance ensuite pitest sur chacun des modules (ayant des fichiers changés) et le résultat (intercepté en analysant le rapport de mutation par *parsing*) est stocké dans une variable ayant comme identifiant le nom du module en question.  

#### Comparaison
Chacun des modules est comparé. Aussitôt qu'un seul module enregistre une baisse du score de mutation, le build échoue.

### Validation
Pour valider, nous nous sommes servis de notre travail effectué dans le cadre de la tâche 2. Lors de cette tâche, nous avions à ajouter des cas de tests qui augmentaient le score de mutation. Pour valider la Github Action, il suffit donc de retirer (ou mettre en commentaire) les tests qu'on a ajouté (et qu'on sait qu'ils font augmenter le score de mutation). On s'attend alors à ce que le workflow échoue. En ajoutant les tests à nouveau, le workflow doit passer car le score de mutation augmente.
## Tests unitaires avec Mockito — Documentation


## Explication du choix de la classe `NavigateResource`
Notre stratégie d'approche a été de chercher une classe dont l'initialisation 
dépend de plusieurs autres classes. Bien que beaucoup de classes respectent cette 
condition, cela n'est pas suffisant pour justifier l'utilisation du mockage. Il 
faut que l'instance de la classe testée soit créée par injection de dépendances ou 
qu'elle reçoive directement ses dépendances en paramètre de constructeur.

Ainsi, ces dépendances peuvent être remplacées par des *mocks* (ou des stubs pour 
certaines). L'indice qui nous a facilité cette tâche était l'annotation `@Inject` 
de Jakarta. Grâce à la fonctionnalité "Search Everywhere" de IntelliJ, nous avons 
pu facilement trouver les classes qui importent ce module.

### Les mocks
Dans la classe de test `NavigateResourceTest`, nous avons créé des *mocks* pour :

- `GraphHopper`
- `TranslationMap` 
- `EncodingManager`
- `HttpServletRequest`

En revanche, la configuration `GraphHopperConfig`, qui est l'un des paramètres du 
constructeur de `NavigateResource`, n'est pas un *mock*, contrairement aux deux 
premiers, car elle nécessite l'initialisation d'un attribut particulier. En fait, 
il est possible de le garder en tant que *mock*, mais nous aurions pu stubber la 
méthode `asPMap()`. Cependant, ayant de la difficulté à comprendre son usage, 
nous avons préféré l'instancier.

### Les stubs
Les appels `when(...).thenReturn(...)` constituent des *stubs*, permettant de 
définir le comportement attendu des *mocks* que nous avons défini. Dans notre cas :

- Les *stubs* dans `setup()` définissent les comportements communs à tous les 
tests, notamment ceux de GraphHopper et EncodingManager.
- Le test `doGetTest()` ajoute un *stub* supplémentaire pour 
`httpRequest.getRequestURI()`, qui permet de simuler un appel HTTP valide.
- Le test `doPostTest()` ajoute deux *stubs* supplémentaire pour
`graphHopper.getEncodingManager()` et `encodingManager.hasEncodedValue(...)`, qui
permettent d'éviter des erreurs du style `NullPointerException`.

### Résumé du comportement des tests
- `doGetTest()` vérifie la logique de parsing de l’URL et s’assure que les coordonnées 
extraites du chemin URI sont correctes. Vérifie aussi qu'un appel GET valide retourne 
une réponse HTTP 200.
- `doPostTest()` vérifie qu’un appel POST valide retourne une réponse HTTP 200
    et que le contenu n’est pas nul.
