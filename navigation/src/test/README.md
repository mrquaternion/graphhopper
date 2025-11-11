# Tests unitaires avec Mockito — Documentation
### Mathias La Rochelle & Marcelo Amarilla

## Explication du choix de la classe `NavigateResource`
Notre stratégie d'approche a été de chercher une classe dont l'initialisation 
dépend de plusieurs autres classes. Bien que beaucoup de classes respectent cette 
condition, cela n'est pas suffisant pour justifier l'utilisation du mockage. Il 
faut que l'instance de la classe testée soit créée par injection de dépendances ou 
qu'elle reçoive directement ses dépendances en paramètre de constructeur.

Ainsi, ces dépendances peuvent être remplacées par des mocks (ou des stubs pour 
certaines). L'indice qui nous a facilité cette tâche était l'annotation `@Inject` 
de Jakarta. Grâce à la fonctionnalité "Search Everywhere" de IntelliJ, nous avons 
pu facilement trouver les classes qui importent ce module.

## Les mocks
Dans la classe de test `NavigateResourceTest`, les mocks sont de la classe 
`GraphHopper`, `TranslationMap` et `EncodingManager`. En revanche, la configuration 
`GraphHopperConfig`, qui est l'un des paramètres du constructeur de 
`NavigateResource`, n'est pas un mock, contrairement aux deux premiers, car elle 
nécessite l'initialisation d'un attribut particulier. En fait, il est possible de 
le garder en tant que mock, mais nous aurions pu stubber la méthode `asPMap()`. 
Cependant, nous ne savions pas comment faire alors nous avons préféré l'instancier.

## Les stubs
Les appels `when(...).thenReturn(...)` constituent des stubbings, permettant de 
définir le comportement attendu des mocks que nous avons défini. Il y en a trois 
dans le `setup()` et une dans la méthode `doGetReturnsTest()`. En fait, quand la 
méthode `doPost()` est appelé sur la ressource (la classe qui reçoit l'injection 
des mocks, soit notre classe testée), plusieurs méthodes des mocks sont appelées et 
quelques-unes d'entre elles ne permettent pas un retour `null`. C'est pour ça que 
ces derniers sont là. 
