# Tests unitaires avec Mockito — Documentation
### Mathias La Rochelle & Marcelo Amarilla

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

## Les mocks
Dans la classe de test `NavigateResourceTest`, nous avons créé des *mocks* pour :

- `GraphHopper`
- `TranslationMap` 
- `EncodingManager`

En revanche, la configuration`GraphHopperConfig`, qui est l'un des paramètres du 
constructeur de `NavigateResource`, n'est pas un *mock*, contrairement aux deux 
premiers, car elle nécessite l'initialisation d'un attribut particulier. En fait, 
il est possible de le garder en tant que *mock*, mais nous aurions pu stubber la 
méthode `asPMap()`. Cependant, nous ne savions pas comment faire alors, nous avons 
préféré l'instancier.

## Les stubs
Les appels `when(...).thenReturn(...)` constituent des *stubs*, permettant de 
définir le comportement attendu des *mocks* que nous avons défini. Dans notre cas :

- Les *stubs* dans `setup()` définissent les comportements communs à tous les 
tests, notamment ceux de GraphHopper et EncodingManager.
- Le test `doGetTest()` ajoute un *stub* supplémentaire pour 
`httpRequest.getRequestURI()`, qui permet de simuler un appel HTTP valide.
- Le test `doPostTest()` ajoute deux *stubs* supplémentaire pour
`graphHopper.getEncodingManager()` et `encodingManager.hasEncodedValue(...)`, qui
permettent d'éviter des erreurs du style `NullPointerException`.

## Résumé du comportement des tests
- `doGetTest()` vérifie la logique de parsing de l’URL et s’assure que les coordonnées 
extraites du chemin URI sont correctes. Vérifie aussi qu'un appel GET valide retourne 
une réponse HTTP 200.
- `doPostTest()` vérifie qu’un appel POST valide retourne une réponse HTTP 200
    et que le contenu n’est pas nul.
