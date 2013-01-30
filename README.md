# Dokumentace k aplikaci Flight System
Pro předmět A4M36JEE byl realizován jednoduchý systém pro správu letů a online rezervací. Aplikace byla postavena na platformě JEE poskytující zázemí pro enterprise informační systémy. Systém by mohl sloužit například letecké společnosti, která svým zákazníkům nabízí možnost rezervace místa v plánovaném letu a online sledování letového plánu. Kromě funkcionality plynoucí z business náplně společnosti aplikace vystavuje uživatelské i programové rozhraní pro interakci se systémem.


## Funkcionální požadavky

- správa destinací, kam společnost létá
- správa plánovaných letů a aktualizace dat
- vytvoření/stornování rezervace na vybraný let
- platba rezervace VISA kartou
- zaslání finančních prostředků vložených na stornovanou rezervaci zpět na účet
- generování potvrzení o vytvoření, zrušení rezervace, přijetí platby a e-ticket
- veřejné REST API pro čtení aktuálních dat pro komunikaci se systémem (např. pro implementaci mobilních klientů pro online sledování letu)
- zabezpečené WS API pro aktualizaci letů, např. zasílání informací z letiště (aktualizace skutečného odletu, příletu a statusu letu)


## Ostatní požadavky

- bezpečnost systému rozlišuje čtyři úrovně oprávnění
- běžný uživatel - tvorba a správa vlastních rezervací
- flight manager -  aktualizuje plánované lety
- admin - plný přístup, vzhledem k roli flight manager smí spravovat destinace
- webservice - aktualizace plánovaných letů, určeno pro 3. strany a WS API
- přístup k rezervacím je zabezpečen heslem
- nasazení produktu na PaaS OpenShift
- používání CDI místo anotací ze standardu Java EE 5
- testovatelnost s použitím testovací platformy Arquillian
- specifikace validačních podmínek nad datovým modelem a využití Bean Validation


## Architektura a nasazení

### Datový model

![Datový model](A4M36JEE/raw/master/doc/DB.png)


### Architektura

Architektura aplikace je založena na standardní třívrstvé architektuře JEE aplikací. Business objekty slouží jako datová vrstva, business vrstva se skládá ze @Stateless služeb poskytujících vlastní funkcionalitu podle anti-patternu Transaction script a prezentační vrstva je tvořena 3 klienty, jedním pro každou z technologií JSF, JAX-RS a JAX-WS.
![Architektura aplikace](A4M36JEE/raw/master/doc/Architecture.png)


### Diagram nasazení

Diagram nasazení zobrazuje nasazení aplikace na standalone JBoss AS a zachycuje i očekávané připojení klientů. Diagram nasazení by bylo možné rozšířit na nasazení do clusteru přidáním load balanceru v podobě reverzní proxy a více instancí JBoss AS.

![Diagram nasazení](A4M36JEE/raw/master/doc/deployment.png)


## Testování

Aplikace využívá testovací framework TestNG a ve spolupráci s Arquillianem obsahuje přibližně 80 integračních testů ověřující jednotlivé uživatelské scénáře včetně neplatných, testování výjimek, testů zabezpečení a testů webových služeb. Kromě toho je k projektu přiloženo ještě asi 20 inicializačních testů pro nastavení očekávaných dat do databáze. 


## Technologie

- Aplikační server JBoss AS 7.1.1
- Maven 2
- JSF 2.1
- CDI 1.0
- EJB 3.1
- JPA 2.0
- JAX-RS
- JAX-WS s JBoss WS Native implementací 4.0.2
- SLF4J
- Hibernate Validator
- RichFaces 4.3.0 s rozšířením Twitter Bootstrap
- TestNG 6.8
- Arquillian 1.0.2


## Implementační omezení


### Standalone režim

Nasazení produktu na standalone JBoss AS 7 plně respektuje všechny požadavky na systém a všechny uživatelské scénáře fungují dle očekávání. 


### PaaS OpenShift

Výchozí konfigurace JBoss AS 7 využívá JBoss WS Native implementaci webových služeb JAX-WS. Pomocí této technologie jsme implementovali zabezpečení služby a kontrolu oprávnění klienta k vykonání dané operace. Paas OpenShift vynucuje použití implementace JAX-WS Apache CXF, která není kompatibilní s konfigurací od JBoss WS Native a server tak ignoruje požadavky na bezpečnost a přístup k metodě mají všichni klienti.
Pokoušeli jsme se převést JBoss AS 7 na Apache CXF implentaci a následně upravit i konfiguraci, ale všechny dostupné tutoriály od JBossu jsou nefunkční co se týče čtení klientova oprávnění. Služba je sice zabezpečená, ale není možné se úspěšně autentikovat. Po několika hodinách pokusů s oficiálními tutoriály od JBossu jsme následovali tutoriály od Apache CXF a napsali si vlastní adaptér, díky kterému se podařilo zprovoznit přihlašování a ověřování oprávnění. Bohužel interceptory ověřující zabezpečení volaných EJB tříd nedokázaly získat uživatelův security context, a tak mu nedovolily požadovanou metodu zavolat.

Následně jsme se ještě pokusili realizovat webové služby jako endpointy EJB tříd místo POJO tříd. Tento způsob nakonec vedl k úspěšnému cíli, získali jsme zabezpečené webové služby s BASIC autentizací, ale jelikož služby jsou deployované ve stejném modulu jako JSF, došlo ke kolizi způsobu přihlašování. JSF vyžadovalo metodu FORM, zatímco JAX-WS metodu BASIC. Tyto dvě různé metody neumí koexistovat ve stejném modulu a převádět deployment na EAR soubor se nám nechtělo, protože jsme viděli mnoho reportovaných bugů při práci s EAR deploymentem.
Nakonec jsme zůstali u implementace JBoss WS Native s vědomím, že služba není na PaaS OpenShift zabezpečena.


### Cluster

Tento produkt není možné spouštět v clusteru kvůli dvěma bugům, které jsme popsali a demonstrovali v projektu [ClusteredJSF](http://github.com/KarelCemus/ClusteredJSF). 

Ačkoliv několik tutoriálů a oficiální dokumentace říkají, že SingleSignOn je ve výchozí konfiguraci JBoss AS 7 zapnuté, tak se nám nepodařilo SSO zprovoznit, což lze snadno ověřit na odkazované aplikaci. Bez předávání aktuálního uživatelova oprávnění nemá smysl nasazovat aplikaci do clusteru, protože by se musel znovu přihlašovat na každý server a mohl by tak ztratit část své práce.


Závažnějším problémem je nefunkční `@ViewScoped` anotace při použití CDI rozšíření a jejím převodu na CDI context. Z důvodu neserializovatelnosti org.jboss.weld.bean.ManagedBean generuje Infinispan desítky výjimek každou vteřinu. Jedná se o výjimky několika typů z nichž všechny mají tu společnou příčinu. To způsobuje nestabilitu a sekání všech serverů v clusteru společně se ztrátou kontextu spravovaných bean. Tento bug již byl reportován [jako WELD-927](https://issues.jboss.org/browse/WELD-927) a na [JBoss fóru](https://community.jboss.org/message/754900). Naše aplikace silně využívá `@ViewScoped` bean, a to je hlavní příčina toho, proč ji není možné nasadit do clusteru.


## Poznámky a zajímavosti

- silné používání HTTP GET parametrů pro předávání mezi stránkami
- CDI extension ze SeamFaces 3 pro převod JSF anotace `@ViewScoped` na CDI context
- žádné JSF-specific anotace pro convertory ani beany, vše CDI
- typově bezpečná injection `@PersistenceContext` - `@Qualifier` a `@Inject`
- RichFaces bean validation
- MultiPage Messages - JSF FacesMessage přežijí redirect a zobrazí se i pak
- zapezpečení `@WebService`
- vlastní řešení bugu Arquillianu pro TestNG @DataProvider (viz níže)
- TestNG suite pro plné otestování aplikace, testy rozděleny do skupin, spustitelné Mavenem
- build řízený mavenem, profily pro testování i openshift, parametrizace konfiguračních souborů, např. deployment context pro OpenShift nastavit na /
- žádná data se z aplikace neodstraňují, používá se atribut “validUntil”
- dvojité hashování hesel v databázi
- rozdělení kontextů aplikace na /faces, /rest, /ws
- zabezpečení přístupu ke zdrojovým souborům stránek (bez /faces), vytvoření chybových stránek


## Nalezené problémy

- CDI context `@ViewScoped` není možné použít v clusteru
- SingleSignOn v clusteru nefunguje
- JBoss AS 7 používá JBoss WS Native, ale OpenShift vynucuje Apache CXF
- Tutoriály na autorizace/autentizaci pomocí Apache CXF na JBossu nefungují a dle debugování kódu ani nemohou, klient i server pracují s úplně jinými parametry
- SeamFaces 3 vynucuje závislost na PrettyFaces i když je nechci použít
- přítomnost PrettyFaces na classpath znemožňuje nasazení aplikace do clusteru, způsobují ztrátu session při jejím předávání mezi servery
- Arquillian nesprávně pracuje s TestNG anotací `@DataProvider`, reprodukováno  a zdokumentováno v projektu [BugReporter](https://github.com/KarelCemus/BugReporter)
- Arquillian občas padá při spouštění test suite obsahující provázané metody (dependsOn). Problém zdokumentován v projektu BugReporter.
- Seam má silný coupling, při použití jednoho modulu je potřeba importovat téměř celý projekt pokud se chceme zbavit výjimek ClassNotFound
- RichFaces bean validation někdy umí číst javax.validation anotace z atributů tříd, ale někdy je ignoruje a čte pouze gettery.
- Arquillian polyká výjimky generované v `@BeforeMethod` atd., neřeší jestli metoda prošla nebo spadla
- Arquillian spouští inicializační metody `@Before...` a `@After...` dvakrát, jednou na klientovi a jednou na serveru, ale např. `@Inject` se vykonal jen na serveru a práce s takovým atributem na klientu generuje `NullPointerException`


## Nasazení

- Aplikace je dostupná na [https://flightsystem-ctu.rhcloud.com/](https://flightsystem-ctu.rhcloud.com/)
- hesla pro přístup
 - karel/cemus jako admin
 - lubos/matl jako flight-manager
 - petr/praus jako webservice
- [REST API na OpenShift](https://flightsystem-ctu.rhcloud.com/rest/)
- [WebService na OpenShift](https://flightsystem-ctu.rhcloud.com/ws/update?wsdl)
- zdrojové kódy dostupné na [GitHubu](https://github.com/KarelCemus/A4M36JEE)
- poznámka: OpenShift je často velice pomalý, při testování nutná trpělivost nebo např. noční hodiny, kdy je výrazně rychlejší

