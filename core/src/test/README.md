
# Tests unitaires automatiques — Documentation

### Mathias La Rochelle & Marcelo Amarilla

## Choix du module

Nous avons choisis uniquement le module `core` car, c'est celui qui nous semblait pouvoir contenir le plus de code facile à comprendre. Pourtant, trouver des classes avec peu de couverture a été l'une des tâches les plus compliquée dans ce devoir. Avant de réfléchir et d'arriver à la conclusion "Oh, mais on peut utiliser JaCoCo pour une analyse efficace", nous faisions une évaluation de la correspondance entre les noms des classes de tests et les noms des classes du code source.

## Choix des classes

En surfant le rapport `JaCoCo` générer par `mvn verify`, nous avons pu faciliter la recherche des classes qui contenaient des méthodes non couvertes par les tests actuels. Avant d'arriver aux complications que nous avions eu avec pitest, les classes qui ont attirées notre attention étaient : `com.graphhopper.config.Profile` et `com.graphhopper.util.GHUtility`. Il y a vraiment aucune autre raison spéciale. Chacune de ces classes contenaient beaucoup de rouge alors cela a attiré notre attention et nous avons tenté notre chance.

---

## Documentation détaillée des tests

### Classe : Profile $\rightarrow$ ProfileTest

#### Test 1 : `testValidateProfileName()`

**Intention du test**

S'assurer que la méthode rejette les noms invalides par l'entremise d'une exception, mais accepte les noms valides. La méthode `validateProfileName(String profileName)` ne doit lancer une exception que si l'entrée contient un caractère qui ne respecte pas l'expression régulière `[a-z0-9_\-]+`.

**Données de test choisies**

- `"#not_ok?"` comme entrée invalide. Le premier (#) et dernier caractère (?) font que ce nom doit lancer une exception.
- `"ok_name"` comme entrée valide. Tous les caractères respectent l'expression régulière et ce nom ne doit pas lancer d'exception.

**Oracles**

Le code de `validateProfileName(...)` a un énoncé conditionnel qui vérifie si l'entrée ne correspond pas à l'expression régulière `[a-z0-9_\-]+`. Si l'énoncé est vrai (donc que l'expression contient des caractères spéciaux), alors le corps de l'énoncé conditionnel lance une exception de type `IllegalArgumentException`. Sinon, aucune exception n'est lancée (et le reste de la méthode est exécuté).

- `assertThrows(IllegalArgumentException.class, () -> Profile.validateProfileName("#not_ok?"))` doit lancer une exception car l'entrée fait que la branche if de l'énoncé est exécutée (et le corps du if lance une exception).
- `assertDoesNotThrow(() -> {Profile.validateProfileName("ok_name");})` ne doit pas lancer d'exception car l'entrée fait que l'énoncé conditionnel est faux. Le throw est donc sauté et le reste de la méthode est exécuté.

---

#### Test 2 : `testPutHint()`

**Intention du test**

Vérifier que les exceptions sont lancées lorsque la méthode est appelée avec des entrées qui doivent être rejetées. Deux types d'entrées doivent mener à une exception.

**Données de test choisies**

- `("u_turn_costs", "whatever")` comme entrée invalide en raison de `"u_turn_costs"` comme premier argument (doit lancer une exception).
- `("vehicle", "whatever")` comme entrée invalide en raison de `"vehicle"` comme premier argument (doit lancer une exception).

**Oracles**

Le code de `putHint(...)` contient deux expressions conditionnelles. Si leur énoncé est vrai, elles lancent chacune une exception de type `IllegalArgumentException`.

- `assertThrows(IllegalArgumentException.class, () -> {instance.putHint("u_turn_costs", "whatever");})` fait que l'énoncé du premier énoncé conditionnel est vrai car celui-ci vérifie si le premier argument de putHint est `"u_turn_costs"`. Il est attendu qu'une exception doit donc être lancée.
- `assertThrows(IllegalArgumentException.class, () -> {instance.putHint("vehicule", "whatever");})` fait que la condition du second énoncé conditionnel est vrai car ce dernier vérifie que `"vehicule"` est le premier argument. Il est attendu qu'une exception doit être lancée.

---

#### Test 3 : `testEquals()`

**Intention du test**

Vérifier que la fonction `equals()` reconnaît lorsque deux instances de Profile sont les mêmes selon des critères établis par les développeurs de la classe.

**Données de test choisies**

Un Profile ayant pour nom `"name"` est utilisé comme instance sur laquelle la méthode est appelée. La méthode accepte n'importe quel Object comme argument.

- `String otherClass = "not null, just another class"` : Objet d'une autre classe pour tester la branche qui retourne false si la comparaison est faite avec un objet d'une classe qui n'hérite pas de Profile.
- `String nullObject = null` : Objet null (`nullObject == null`) pour tester la branche qui retourne false si l'object est null.
- `Profile anotherRef = instance` : pour tester si la branche qui retourne vrai si l'objet comparé est le même mais sous une autre référence.
- `Object sameName = new Profile("name")` : pour tester la branche qui retourne vrai si l'objet est un Profile différent, mais qui a le même nom.
- `Object diffName = new Profile("anotherName")` : pour tester la branche qui retourne false avec un nom différent.

**Oracles**

`assertEquals()` et `assertNotEquals()` exploitent la méthode `equals()` définie dans l'objet qui est passé en premier argument.

- `assertNotEquals(instance, otherClass)` : doit retourner false (autre classe)
- `assertNotEquals(instance, nullObject)` : doit retourner false (objet null)
- `assertNotEquals(instance, diffName)` : doit retourner false (même classe, différent nom)
- `assertEquals(instance, anotherRef)` : doit retourner vrai (même objet, différente référence)
- `assertEquals(instance, sameName)` : doit retourner vrai (différent objet, mais même nom)

---

### Classe : GHUtility $\rightarrow$ GHUtilityTest

#### Test 4 : `testGetProblems()`

**Intention du test**

Vérifier que la méthode `getProblems()` détecte correctement les problèmes dans un graphe, notamment lorsque des sommets possèdent des coordonnées erronées.

**Données de test choisies**

- Un graphe avec des sommets ayant des coordonnées valides pour vérifier qu'aucun problème n'est détecté.
- Un graphe avec des sommets ayant des coordonnées erronées pour vérifier que les problèmes sont bien identifiés et ajoutés à la liste.

**Oracles**

Le code de `getProblems(...)` parcourt les sommets du graphe et vérifie leurs coordonnées à travers des énoncés conditionnels. Si des coordonnées sont invalides, des problèmes sont ajoutés à une liste retournée par la méthode.

- Lorsque le graphe contient des coordonnées valides, la liste des problèmes retournée doit être vide.
- Lorsque le graphe contient des coordonnées erronées, la liste des problèmes ne doit pas être vide et doit contenir les erreurs détectées.

**Méthodes utilitaires:** `setNodes()` et `setEdges()` sont des fonctions réutilisables pour initialiser les graphes de test. Elles ne sont pas comprises dans les 7 tests unitaires requis.

---

#### Test 5 : `testPathsEqualExceptOneEdge()`

**Intention du test**

Vérifier que la méthode `comparePaths()` identifie correctement les différences entre deux chemins générés par différents algorithmes. La méthode doit détecter si un algorithme de référence renvoie le même chemin qu'un autre algorithme X.

**Données de test choisies**

- Deux chemins (Path) qui sont identiques sauf pour une arête, afin de tester la capacité de la méthode à détecter cette différence spécifique.
- Les chemins sont créés avec des structures de graphe appropriées incluant des sommets et arêtes définis.

**Oracles**

Le code de `comparePaths(...)` compare deux objets Path et génère une liste de violations/problèmes lorsque des différences sont détectées entre les chemins.

- Lorsque les chemins sont identiques, la liste des violations doit être vide.
- Lorsque les chemins diffèrent par une arête, la liste des violations doit contenir une entrée identifiant cette différence.

**Méthodes utilitaires:** `setNodes()` et `setEdges()`.

---

#### Test 6 : `testGetCommonNodes()`

**Intention du test**

Vérifier que la méthode `getCommonNodes()` identifie correctement le sommet commun entre deux arêtes dans un graphe.

**Données de test choisies**

- Deux arêtes qui partagent un sommet commun pour vérifier que la méthode retourne bien ce sommet.
- Les arêtes sont initialisées avec des sommets définis lors de la création du graphe de test.

**Oracles**

Le code de `getCommonNodes(...)` analyse deux arêtes et détermine si elles partagent un sommet commun en comparant leurs nœuds de départ et d'arrivée.

- Lorsque deux arêtes partagent un sommet, la méthode doit retourner l'identifiant de ce sommet commun.
- Le sommet retourné doit correspondre exactement au sommet qui a été défini comme commun lors de l'initialisation des arêtes dans le test.

**Méthodes utilitaires:** `setNodes()` et `setEdges()`.

---

#### Test 7 : `testGetCommonNodesShouldThrowExceptionAndAdjNode()`

**Intention du test**

Vérifier que la méthode `getCommonNodes()` lance une exception appropriée lorsque deux arêtes n'ont pas de sommet commun, et tester également le comportement avec des nœuds adjacents.

**Données de test choisies**

- Deux arêtes qui ne partagent aucun sommet commun pour tester le lancement d'exception.
- Différentes configurations d'arêtes adjacentes pour vérifier les cas limites du comportement de la méthode.

**Oracles**

Le code de `getCommonNodes(...)` doit détecter l'absence de sommet commun et lancer une exception de type approprié.

- `assertThrows(...)` doit confirmer qu'une exception est lancée lorsque les arêtes n'ont pas de sommet commun.
- Les cas d'arêtes adjacentes doivent être traités correctement selon la logique de la méthode.

**Méthodes utilitaires:** `setNodes()` et `setEdges()`.

## Pitest et ses mutants

### Vue d'ensemble globale

| Métrique | Avant | Après | Évolution |
|----------|-------|-------|-----------|
| **Line Coverage** | 61% (272/445) | 77% (345/449) | + 16% |
| **Mutation Coverage** | 45% (144/319) | 56% (179/321) | + 11% |

### Détail par package

#### `com.graphhopper.config`

| Métrique | Avant | Après | Évolution |
|----------|-------|-------|-----------|
| **Line Coverage** | 84% (43/51) | 98% (50/51) | + 14% |
| **Mutation Coverage** | 53% (16/30) | 68% (21/31) | + 15% |

#### `com.graphhopper.routing.util`

| Métrique | Avant | Après | Évolution |
|----------|-------|-------|-----------|
| **Line Coverage** | 0% (0/39) | 70% (30/43) | + 70% |
| **Mutation Coverage** | 0% (0/26) | 19% (5/27) | + 19% |

#### `com.graphhopper.util`

| Métrique | Avant | Après | Évolution |
|----------|-------|-------|-----------|
| **Line Coverage** | 65% (229/355) | 75% (265/355) | + 10% |
| **Mutation Coverage** | 49% (128/263) | 58% (153/263) | + 9% |

---

### Détails
Comme vous pouvez voir dans la vue d'ensemble globale, plus précisement dans la section _Mutation Coverage_, le dénominateur a augmenté de 2 unités. Ceci correspond exactement à la découverte de 2 mutants. 

**<u>Cependant</u>**, initialement, nous n'avions qu'un seul nouveau mutant détecté. Nous avons réussi à en créer un nouveau grâce à la classe `UrbanDensityCalculator` dans le package listé ci-dessus (le 2e). 

La démarche visée a été, encore une fois, de fouiller à travers le rapport `JaCoCo` du module `core`. La classe au complet était non-couverte (voir le "avant" ci-dessus) et malgré sa structure intimidante, a suscité notre intérêt et ce, principalement à cause de la documentation qui était fournie (comparativement aux autres classes).

À l'occurence, sa classe de test correspondante est `UrbanDensityCalculatorTest` et la méthode créée est `testTrackEdgeIsRural()`. Selon nous, l'apparition de ce nouveau mutant est arrivée car le code était absolument pas couvert. Donc, il y avait de grandes chances que PIT détecte une ligne où un mutant supplémentaire pouvait être. Ce mutant nouvellement détecté se trouve dans une expression lambda à la ligne 92 de `UrbanDensityCalculator`. Le mutant concerne le cas où sa valeur
retournée, qui doit être un double, est 0.0d. Puisque cette ligne n'était pas du tout couverte, il est normal que notre dernier test le découvre (et que le test précédant ne le considère jamais).

En ce qui concerne le tout premier mutant nouvellement détecté, il se trouvait dans la classe `Profile`. ProfileTest n'existait pas au moment où nous avons analysé le code et il est donc également naturel qu'on en ait détecté un nouveau. Celui-ci concerne la méthode "hasTurnCosts()" qui retourne *true* si l'attribut "turnCosts" de Profile est non *null*. Puisque nous n'avons pas initialisé cet attribut dans le cadre des `ProfileTest`, pitest a détecté le mutant pour lequel le retour soit *false*.



## Java Faker
Au départ, nous avons tenté de trouver une classe qui utilisait des adresses réelles. Nous avons trouvé `NameSimilarityEdgeFilter`, cependant des tests avaient déjà été configurés. En fait, grâce à cette classe, il était possible de vérifier la similarité entre le nom associé à deux noeuds et de déterminer s'il était valable d'accepter ou non.

Après plusieurs recherches, nous sommes tombés (encore grâce à `JaCoCo`) sur la méthode `parse2DJSON()` de la classe `PointList` dans le package `util` du module `web-api`. Elle était non-couverte alors nous l'avons choisie. La méthode est assez triviale; on passe une chaîne de charactères qui est supposé simuler des données géospatiales, et elle décortique le string de sorte à ajouter les tuples à l'objet créé juste avant, `PointList list`.
