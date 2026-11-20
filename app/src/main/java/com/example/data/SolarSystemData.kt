package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.model.CelestialBody
import com.example.model.ChibiExpression
import com.example.model.ComicPanel
import com.example.model.CuriosityCategory
import com.example.model.CuriosityItem
import com.example.model.PlanetSurfaceStyle
import com.example.model.QuizQuestion
import com.example.model.SpaceBadge
import com.example.ui.theme.AuroraGreen
import com.example.ui.theme.CosmicAmber
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.NebulaMagenta

object SolarSystemData {

  val hairColors =
    listOf(
      Color(0xFF2B1E1A), // Nero-castano
      Color(0xFF6B3E26), // Castano caldo
      Color(0xFFE6B34C), // Biondo
      Color(0xFFC84B31), // Rosso rame
      Color(0xFF00B4D8), // Blu spaziale
    )

  val skinTones =
    listOf(
      Color(0xFFFFDFC4),
      Color(0xFFF3C6A5),
      Color(0xFFD99B72),
      Color(0xFFA66E4E),
      Color(0xFF6E432D),
    )

  val suitColors =
    listOf(
      Color(0xFFFF6B35), // Arancione Missione
      Color(0xFF00B4D8), // Blu Cometa
      Color(0xFF9B5DE5), // Viola Nebulosa
      Color(0xFF2CEAA3), // Verde Aurora
      Color(0xFFFF4D8D), // Magenta Supernova
    )

  val celestialBodies: List<CelestialBody> =
    listOf(
      CelestialBody(
        id = "sun",
        name = "Il Sole",
        nickname = "La Stella Motore",
        category = "Stella Nana Gialla",
        orderIndex = 0,
        diameterKm = 1_392_700,
        earthSizeRatio = 109f,
        distanceFromSunLabel = "Centro del Sistema",
        dayLengthLabel = "25 giorni (equatore)",
        yearLengthLabel = "230 milioni d'anni (nella galassia)",
        yearLengthEarthYears = 1f,
        temperatureLabel = "5.500 °C in superficie",
        moonsCount = 0,
        notableMoons = emptyList(),
        gravityRatio = 27.9f,
        axialTiltDeg = 7.25f,
        orbitDistanceScene = 0f,
        orbitSpeedDegPerSec = 0f,
        sceneVisualRadius = 38f,
        primaryColor = Color(0xFFFFB703),
        secondaryColor = Color(0xFFFB5607),
        accentColor = Color(0xFFFFF3B0),
        atmosphereColor = Color(0xFFFF9E00),
        surfaceStyle = PlanetSurfaceStyle.SUN,
        quickHook = "Contiene il 99,86% di tutta la materia del Sistema Solare!",
        sizeAnalogy =
          "Se il Sole fosse grande come un pallone da basket, la Terra sarebbe piccola come un granello di pepe!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Una palla di plasma bollente!",
              dialogue =
                "Ehi esploratore! Il Sole non è fatto di roccia come la Terra, ma di gas caldissimo chiamato plasma. Nel suo centro la temperatura arriva a 15 milioni di gradi!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "La fabbrica della luce",
              dialogue =
                "Ogni secondo, il Sole trasforma milioni di tonnellate di idrogeno in elio. È grazie a questa energia gigante che sulla Terra abbiamo luce, calore e vita!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "La luce viaggia veloce...",
              dialogue =
                "La luce del Sole viaggia a 300.000 km al secondo, eppure impiega ben 8 minuti e 20 secondi per arrivare fino ai tuoi occhi!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Che forza di gravità!",
              dialogue =
                "Il Sole è così gigantesco che la sua gravità tiene legati tutti gli 8 pianeti come una gigantesca giostra invisibile nello spazio!",
              expression = ChibiExpression.ROCKET,
            ),
          ),
      ),
      CelestialBody(
        id = "mercury",
        name = "Mercurio",
        nickname = "Il Velocista Pieno di Crateri",
        category = "Pianeta Roccioso",
        orderIndex = 1,
        diameterKm = 4_879,
        earthSizeRatio = 0.38f,
        distanceFromSunLabel = "58 milioni km",
        dayLengthLabel = "59 giorni terrestri",
        yearLengthLabel = "88 giorni terrestri",
        yearLengthEarthYears = 0.24f,
        temperatureLabel = "Da -180 °C a +430 °C",
        moonsCount = 0,
        notableMoons = emptyList(),
        gravityRatio = 0.38f,
        axialTiltDeg = 0.03f,
        orbitDistanceScene = 75f,
        orbitSpeedDegPerSec = 42f,
        sceneVisualRadius = 11f,
        primaryColor = Color(0xFFA5A5A5),
        secondaryColor = Color(0xFF6E6B68),
        accentColor = Color(0xFFD6CFC7),
        atmosphereColor = null,
        surfaceStyle = PlanetSurfaceStyle.MERCURY,
        quickHook = "Il pianeta più piccolo e più veloce: sfreccia intorno al Sole in 88 giorni!",
        sizeAnalogy =
          "È poco più grande della nostra Luna: se la Terra fosse una mela, Mercurio sarebbe un mirtillo!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Campione di velocità",
              dialogue =
                "Indossa le cinture! Mercurio è il pianeta più vicino al Sole e corre a quasi 170.000 km all'ora! Il suo anno dura meno di tre mesi sulla Terra.",
              expression = ChibiExpression.ROCKET,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Forno di giorno, freezer di notte!",
              dialogue =
                "Siccome non ha quasi atmosfera che trattenga il calore, di giorno si cuoce a 430 °C, ma appena cala la notte si scende a -180 °C!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Somiglia tantissimo alla Luna",
              dialogue =
                "Guarda quanti crateri! Ogni buca è il segno di un antico meteorite caduto miliardi di anni fa. Non essendoci vento o pioggia, restano lì per sempre!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Un giorno lunghissimo",
              dialogue =
                "Su Mercurio succede una cosa pazzesca: gira su se stesso così piano che tra un'alba e l'altra passano 176 giorni terrestri!",
              expression = ChibiExpression.HAPPY,
            ),
          ),
      ),
      CelestialBody(
        id = "venus",
        name = "Venere",
        nickname = "Il Pianeta Serra Luminoso",
        category = "Pianeta Roccioso",
        orderIndex = 2,
        diameterKm = 12_104,
        earthSizeRatio = 0.95f,
        distanceFromSunLabel = "108 milioni km",
        dayLengthLabel = "243 giorni terrestri",
        yearLengthLabel = "225 giorni terrestri",
        yearLengthEarthYears = 0.62f,
        temperatureLabel = "+465 °C (il più caldo!)",
        moonsCount = 0,
        notableMoons = emptyList(),
        gravityRatio = 0.91f,
        axialTiltDeg = 177.4f,
        orbitDistanceScene = 110f,
        orbitSpeedDegPerSec = 30f,
        sceneVisualRadius = 15f,
        primaryColor = Color(0xFFE9C46A),
        secondaryColor = Color(0xFFD48C46),
        accentColor = Color(0xFFFFF1C9),
        atmosphereColor = Color(0xFFFFD166),
        surfaceStyle = PlanetSurfaceStyle.VENUS,
        quickHook = "È il pianeta più caldo di tutti e gira al contrario rispetto agli altri!",
        sizeAnalogy =
          "È chiamato il gemello della Terra perché ha quasi la stessa identica grandezza!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Più caldo di Mercurio!",
              dialogue =
                "Sorpresa! Anche se è più lontano dal Sole di Mercurio, Venere è il pianeta più bollente (465 °C) perché ha una coperta spessa di nuvole che intrappola il calore!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Il pianeta ribelle",
              dialogue =
                "Quasi tutti i pianeti girano in un senso, ma Venere gira al contrario! Se vivessi lì, vedresti il Sole sorgere a Ovest e tramontare a Est!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Un giorno più lungo di un anno!",
              dialogue =
                "Venere gira su se stessa con una lentezza record: ci mette 243 giorni a fare un giro su sé stessa, ma solo 225 giorni a girare intorno al Sole!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "La stella della sera",
              dialogue =
                "Le sue nuvole gialle riflettono così tanta luce solare che dalla Terra Venere brilla più di qualsiasi stella al tramonto o all'alba!",
              expression = ChibiExpression.CHEERING,
            ),
          ),
      ),
      CelestialBody(
        id = "earth",
        name = "La Terra",
        nickname = "La Nostra Oasi Blu",
        category = "Pianeta Roccioso",
        orderIndex = 3,
        diameterKm = 12_742,
        earthSizeRatio = 1.0f,
        distanceFromSunLabel = "150 milioni km",
        dayLengthLabel = "24 ore",
        yearLengthLabel = "365 giorni",
        yearLengthEarthYears = 1f,
        temperatureLabel = "Media +15 °C",
        moonsCount = 1,
        notableMoons = listOf("La Luna"),
        gravityRatio = 1.0f,
        axialTiltDeg = 23.4f,
        orbitDistanceScene = 152f,
        orbitSpeedDegPerSec = 23f,
        sceneVisualRadius = 16.5f,
        primaryColor = Color(0xFF1D7AFC),
        secondaryColor = Color(0xFF2EC4B6),
        accentColor = Color(0xFF52B788),
        atmosphereColor = Color(0xFF4CC9F0),
        surfaceStyle = PlanetSurfaceStyle.EARTH,
        quickHook = "L'unico mondo conosciuto con acqua liquida in superficie e vita!",
        sizeAnalogy =
          "Il nostro metro di misura spaziale: circa il 71% della superficie è coperto da oceani azzurri!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Nella zona perfetta!",
              dialogue =
                "Benvenuti a casa! La Terra si trova alla distanza giusta dal Sole: né troppo calda da far bollire gli oceani, né troppo fredda da congelarli tutti!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Uno scudo invisibile",
              dialogue =
                "Intorno alla Terra c'è un campo magnetico che fa da scudo contro il vento solare pericoloso, creando ai poli le magiche aurore boreali!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Perché esistono le stagioni?",
              dialogue =
                "Vedi come la Terra è un po' inclinata? Proprio grazie a quell'inclinazione di 23,4 gradi abbiamo primavera, estate, autunno e inverno!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "La nostra compagna Luna",
              dialogue =
                "La Luna gira intorno a noi da oltre 4 miliardi di anni e con la sua gravità crea le maree negli oceani di tutto il pianeta!",
              expression = ChibiExpression.CHEERING,
            ),
          ),
      ),
      CelestialBody(
        id = "moon",
        name = "La Luna",
        nickname = "La Compagna della Terra",
        category = "Satellite Naturale",
        orderIndex = 4,
        diameterKm = 3_474,
        earthSizeRatio = 0.27f,
        distanceFromSunLabel = "384.400 km dalla Terra",
        dayLengthLabel = "27,3 giorni terrestri",
        yearLengthLabel = "27,3 giorni (intorno alla Terra)",
        yearLengthEarthYears = 0.075f,
        temperatureLabel = "Da -170 °C a +120 °C",
        moonsCount = 0,
        notableMoons = emptyList(),
        gravityRatio = 0.165f,
        axialTiltDeg = 6.7f,
        orbitDistanceScene = 24f,
        orbitSpeedDegPerSec = 90f,
        sceneVisualRadius = 6.8f,
        primaryColor = Color(0xFFD0D5DD),
        secondaryColor = Color(0xFF7D8597),
        accentColor = Color(0xFFF8FAFC),
        atmosphereColor = null,
        surfaceStyle = PlanetSurfaceStyle.MOON,
        quickHook = "L'unico mondo oltre la Terra su cui gli esseri umani hanno camminato!",
        sizeAnalogy =
          "Se la Terra fosse un pallone da basket, la Luna sarebbe grande quanto una palla da tennis!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "La nostra vicina di casa",
              dialogue =
                "Ciao! La Luna è il nostro unico satellite naturale! È piena di crateri scavati da antichi meteoriti e grandi macchie scure di lava solidificata chiamate 'mari'!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Perché cambia forma nel cielo?",
              dialogue =
                "La Luna non brilla di luce propria: riflette quella del Sole! Mentre gira intorno alla Terra vediamo illuminata una fetta diversa ogni sera: ecco le fasi lunari!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Salti da supereroe!",
              dialogue =
                "Sulla Luna la gravità è 6 volte più debole della Terra! Un astronauta con tutta la tuta pesante riesce comunque a fare balzi altissimi come un canguro!",
              expression = ChibiExpression.CHEERING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "La regista delle maree",
              dialogue =
                "Anche se è lontana quasi 400.000 km, la gravità della Luna attira l'acqua dei nostri oceani facendo alzare e abbassare le maree ogni giorno!",
              expression = ChibiExpression.AMAZED,
            ),
          ),
      ),
      CelestialBody(
        id = "mars",
        name = "Marte",
        nickname = "Il Pianeta Rosso",
        category = "Pianeta Roccioso",
        orderIndex = 5,
        diameterKm = 6_779,
        earthSizeRatio = 0.53f,
        distanceFromSunLabel = "228 milioni km",
        dayLengthLabel = "24 ore e 37 min",
        yearLengthLabel = "687 giorni terrestri",
        yearLengthEarthYears = 1.88f,
        temperatureLabel = "Media -63 °C",
        moonsCount = 2,
        notableMoons = listOf("Phobos", "Deimos"),
        gravityRatio = 0.38f,
        axialTiltDeg = 25.2f,
        orbitDistanceScene = 195f,
        orbitSpeedDegPerSec = 17f,
        sceneVisualRadius = 13f,
        primaryColor = Color(0xFFE76F51),
        secondaryColor = Color(0xFF9E2A2B),
        accentColor = Color(0xFFF4A261),
        atmosphereColor = Color(0xFFFF8866),
        surfaceStyle = PlanetSurfaceStyle.MARS,
        quickHook = "Ospita il vulcano più alto del Sistema Solare: il Monte Olimpo!",
        sizeAnalogy =
          "È grande circa la metà della Terra: se la Terra fosse una palla da pallavolo, Marte sarebbe una palla da tennis!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Perché è tutto rosso?",
              dialogue =
                "Marte è pieno di ferro nella sua polvere: col tempo si è arrugginito! Proprio così, il Pianeta Rosso è ricoperto da un deserto di ruggine gigante!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Il vulcano dei record",
              dialogue =
                "Il Monte Olimpo su Marte è alto 22 chilometri: quasi tre volte il Monte Everest! È il vulcano più grande dell'intero Sistema Solare!",
              expression = ChibiExpression.ROCKET,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Rover robotici in giro",
              dialogue =
                "Su Marte lavorano veri robot su ruote come Curiosity e Perseverance! Scattano foto, analizzano rocce e cercano tracce di acqua antica.",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Due piccole lune a forma di patata",
              dialogue =
                "Marte ha due lune minuscole e bitorzolute: Phobos e Deimos. Sembrano due asteroidi catturati dalla sua gravità!",
              expression = ChibiExpression.THINKING,
            ),
          ),
      ),
      CelestialBody(
        id = "jupiter",
        name = "Giove",
        nickname = "Il Re Gigante",
        category = "Gigante Gassoso",
        orderIndex = 6,
        diameterKm = 139_820,
        earthSizeRatio = 10.97f,
        distanceFromSunLabel = "778 milioni km",
        dayLengthLabel = "9 ore e 55 min",
        yearLengthLabel = "12 anni terrestri",
        yearLengthEarthYears = 11.86f,
        temperatureLabel = "-110 °C nelle nuvole",
        moonsCount = 95,
        notableMoons = listOf("Io", "Europa", "Ganimede", "Callisto"),
        gravityRatio = 2.53f,
        axialTiltDeg = 3.1f,
        orbitDistanceScene = 258f,
        orbitSpeedDegPerSec = 10f,
        sceneVisualRadius = 29f,
        primaryColor = Color(0xFFE09F67),
        secondaryColor = Color(0xFFA45A3F),
        accentColor = Color(0xFFF6E3C5),
        atmosphereColor = Color(0xFFF4A261),
        surfaceStyle = PlanetSurfaceStyle.JUPITER,
        quickHook = "È così grande che dentro potrebbero entrarci più di 1.300 Terre!",
        sizeAnalogy =
          "Se Giove fosse una grande anguria, la Terra sarebbe un piccolo acino d'uva!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Il gigante buono... di gas!",
              dialogue =
                "Eccoci davanti al Re dei Pianeti! Giove è un gigante gassoso: non c'è un suolo solido dove atterrare, ma solo strati infiniti di idrogeno ed elio!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "La Grande Macchia Rossa",
              dialogue =
                "Vedi quell'ovale rosso? È un mega uragano grande quanto due Terre messe insieme che soffia senza sosta da oltre 300 anni!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Una trottola super veloce",
              dialogue =
                "Nonostante sia enorme, Giove gira su se stesso più veloce di tutti: un giorno intero dura meno di 10 ore!",
              expression = ChibiExpression.ROCKET,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Una squadra di 95 lune!",
              dialogue =
                "Galileo scoprì le 4 lune più famose di Giove: Io ha centinaia di vulcani, Europa nasconde un oceano sotto il ghiaccio e Ganimede è più grande di Mercurio!",
              expression = ChibiExpression.CHEERING,
            ),
          ),
      ),
      CelestialBody(
        id = "saturn",
        name = "Saturno",
        nickname = "Il Signore degli Anelli",
        category = "Gigante Gassoso",
        orderIndex = 7,
        diameterKm = 116_460,
        earthSizeRatio = 9.14f,
        distanceFromSunLabel = "1,4 miliardi km",
        dayLengthLabel = "10 ore e 33 min",
        yearLengthLabel = "29 anni terrestri",
        yearLengthEarthYears = 29.45f,
        temperatureLabel = "-140 °C",
        moonsCount = 146,
        notableMoons = listOf("Titano", "Encelado", "Mimas"),
        gravityRatio = 1.06f,
        axialTiltDeg = 26.7f,
        orbitDistanceScene = 326f,
        orbitSpeedDegPerSec = 7f,
        sceneVisualRadius = 25f,
        primaryColor = Color(0xFFF3D58D),
        secondaryColor = Color(0xFFC99E52),
        accentColor = Color(0xFFFFF6CC),
        atmosphereColor = Color(0xFFFFE599),
        hasRings = true,
        ringColor = Color(0xFFE9D8A6),
        surfaceStyle = PlanetSurfaceStyle.SATURN,
        quickHook = "I suoi spettacolari anelli sono fatti di miliardi di pezzi di ghiaccio e roccia!",
        sizeAnalogy =
          "È così leggero che se esistesse una vasca da bagno abbastanza grande, Saturno galleggerebbe sull'acqua!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Di cosa sono fatti gli anelli?",
              dialogue =
                "Gli anelli di Saturno sembrano dischi solidi, ma sono fatti da miliardi di blocchi di ghiaccio e polvere: alcuni piccoli come briciole, altri grandi come case!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Un pianeta leggerissimo!",
              dialogue =
                "Saturno è fatto soprattutto di gas leggeri. Ha una densità minore dell'acqua: in teoria è l'unico pianeta che potrebbe galleggiare!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "L'esagono misterioso",
              dialogue =
                "Al polo nord di Saturno soffia una gigantesca tempesta a forma di esagono quasi perfetto! La natura ama davvero la geometria!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Il record delle lune",
              dialogue =
                "Saturno ha oltre 140 lune! La più grande si chiama Titano e ha fiumi e laghi fatti non di acqua, ma di metano liquido!",
              expression = ChibiExpression.CHEERING,
            ),
          ),
      ),
      CelestialBody(
        id = "uranus",
        name = "Urano",
        nickname = "Il Gigante Sdraiato",
        category = "Gigante Ghiacciato",
        orderIndex = 8,
        diameterKm = 50_724,
        earthSizeRatio = 3.98f,
        distanceFromSunLabel = "2,9 miliardi km",
        dayLengthLabel = "17 ore e 14 min",
        yearLengthLabel = "84 anni terrestri",
        yearLengthEarthYears = 84f,
        temperatureLabel = "-195 °C",
        moonsCount = 28,
        notableMoons = listOf("Titania", "Oberon", "Miranda"),
        gravityRatio = 0.89f,
        axialTiltDeg = 97.8f,
        orbitDistanceScene = 388f,
        orbitSpeedDegPerSec = 4.6f,
        sceneVisualRadius = 18.5f,
        primaryColor = Color(0xFF72EFDD),
        secondaryColor = Color(0xFF48BFE3),
        accentColor = Color(0xFFCFFAFE),
        atmosphereColor = Color(0xFF80FFDB),
        hasRings = true,
        ringColor = Color(0xFFADE8F4),
        surfaceStyle = PlanetSurfaceStyle.URANUS,
        quickHook = "Rotola lungo la sua orbita sdraiato su un fianco con un'inclinazione di 98 gradi!",
        sizeAnalogy =
          "È grande quasi 4 volte la Terra: come un pallone da calcio rispetto a una pallina da golf!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Perché rotola sdraiato?",
              dialogue =
                "Guarda l'asse di Urano! Gli astronomi pensano che miliardi di anni fa un oggetto enorme l'abbia colpito facendolo ribaltare su un fianco!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Un colore azzurro ghiaccio",
              dialogue =
                "Urano è azzurro-acqua grazie al gas metano nella sua atmosfera, che assorbe la luce rossa e riflette quella blu-verde verso di noi!",
              expression = ChibiExpression.HAPPY,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Stagioni lunghe 21 anni!",
              dialogue =
                "Siccome è sdraiato e impiega 84 anni per fare il giro del Sole, su Urano ogni polo ha 42 anni di luce continua e poi 42 anni di buio!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Anelli sottili e verticali",
              dialogue =
                "Anche Urano ha 13 anelli sottili e scuri! Ma invece di essere orizzontali come quelli di Saturno, sembrano un bersaglio verticale!",
              expression = ChibiExpression.ROCKET,
            ),
          ),
      ),
      CelestialBody(
        id = "neptune",
        name = "Nettuno",
        nickname = "Il Regno dei Venti Supersonici",
        category = "Gigante Ghiacciato",
        orderIndex = 9,
        diameterKm = 49_244,
        earthSizeRatio = 3.86f,
        distanceFromSunLabel = "4,5 miliardi km",
        dayLengthLabel = "16 ore",
        yearLengthLabel = "165 anni terrestri",
        yearLengthEarthYears = 164.8f,
        temperatureLabel = "-200 °C",
        moonsCount = 16,
        notableMoons = listOf("Tritone", "Proteo"),
        gravityRatio = 1.14f,
        axialTiltDeg = 28.3f,
        orbitDistanceScene = 446f,
        orbitSpeedDegPerSec = 3.2f,
        sceneVisualRadius = 18f,
        primaryColor = Color(0xFF2B59FF),
        secondaryColor = Color(0xFF1A2B88),
        accentColor = Color(0xFF72A8FF),
        atmosphereColor = Color(0xFF4361EE),
        surfaceStyle = PlanetSurfaceStyle.NEPTUNE,
        quickHook = "I suoi venti soffiano a oltre 2.000 km/h: più veloci di un aereo supersonico!",
        sizeAnalogy =
          "È il gemello blu di Urano, lontano dal Sole 30 volte più della Terra!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Scoperto con la matematica!",
              dialogue =
                "Incredibile ma vero: Nettuno fu scoperto nel 1846 prima coi calcoli su carta che col telescopio! Gli scienziati capirono che qualcosa tirava Urano con la sua gravità!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Tempeste super veloci",
              dialogue =
                "Tieniti forte il casco! Su Nettuno soffiano i venti più veloci di tutto il Sistema Solare: superano i 2.100 chilometri all'ora!",
              expression = ChibiExpression.ROCKET,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Un anno lunghissimo",
              dialogue =
                "Nettuno impiega 165 anni terrestri per completare un giro intorno al Sole. Dalla sua scoperta ha fatto appena un giro completo!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "Tritone e i geyser di azoto",
              dialogue =
                "La sua luna principale, Tritone, gira al contrario e ha veri geyser che sparano ghiaccio e gas nello spazio a chilometri di altezza!",
              expression = ChibiExpression.CHEERING,
            ),
          ),
      ),
      CelestialBody(
        id = "pluto",
        name = "Plutone",
        nickname = "Il Pianeta Nano dal Cuore di Ghiaccio",
        category = "Pianeta Nano",
        orderIndex = 10,
        diameterKm = 2_377,
        earthSizeRatio = 0.18f,
        distanceFromSunLabel = "5,9 miliardi km",
        dayLengthLabel = "6,4 giorni terrestri",
        yearLengthLabel = "248 anni terrestri",
        yearLengthEarthYears = 248f,
        temperatureLabel = "-230 °C",
        moonsCount = 5,
        notableMoons = listOf("Caronte", "Stige", "Notte"),
        gravityRatio = 0.06f,
        axialTiltDeg = 122.5f,
        orbitDistanceScene = 500f,
        orbitSpeedDegPerSec = 2.1f,
        sceneVisualRadius = 9.5f,
        primaryColor = Color(0xFFD8B4A0),
        secondaryColor = Color(0xFF7D5A50),
        accentColor = Color(0xFFF5E6E0),
        atmosphereColor = Color(0xFF90E0EF),
        surfaceStyle = PlanetSurfaceStyle.PLUTO,
        quickHook = "Ha un gigantesco ghiacciaio a forma di cuore grande quanto mezza Europa!",
        sizeAnalogy =
          "È più piccolo della nostra Luna: su Plutone faresti salti alti 15 metri con facilità!",
        comicStories =
          listOf(
            ComicPanel(
              badge = "CAPITOLO 1",
              title = "Perché è un Pianeta Nano?",
              dialogue =
                "Nel 2006 gli astronomi hanno creato la famiglia dei Pianeti Nani: Plutone è rotondo e gira intorno al Sole, ma condivide la sua zona con tanti corpi ghiacciati!",
              expression = ChibiExpression.THINKING,
            ),
            ComicPanel(
              badge = "CAPITOLO 2",
              title = "Un cuore gigante di ghiaccio!",
              dialogue =
                "La sonda New Horizons nel 2015 ci ha regalato una foto fantastica: su Plutone c'è un'enorme pianura luminosa di azoto ghiacciato a forma di cuore!",
              expression = ChibiExpression.AMAZED,
            ),
            ComicPanel(
              badge = "CAPITOLO 3",
              title = "Salti da supereroe!",
              dialogue =
                "La gravità di Plutone è piccolissima: se sulla Terra pesi 35 chili, su Plutone peseresti appena 2 chili e faresti balzi giganteschi!",
              expression = ChibiExpression.CHEERING,
            ),
            ComicPanel(
              badge = "CAPITOLO 4",
              title = "La coppia con Caronte",
              dialogue =
                "La sua luna Caronte è così grande rispetto a Plutone che ballano insieme nello spazio guardandosi sempre con la stessa faccia!",
              expression = ChibiExpression.HAPPY,
            ),
          ),
      ),
    )

  val curiosities: List<CuriosityItem> =
    listOf(
      CuriosityItem(
        id = "cur_1",
        title = "Su Venere il compleanno arriva prima di domani!",
        category = CuriosityCategory.WEIRD_SPACE,
        badgeTag = "TEMPO PAZZO",
        summary = "Un giorno su Venere (243 giorni terrestri) dura più di un suo intero anno (225 giorni)!",
        deepDive =
          "Venere ruota su se stessa lentissimamente e al contrario! Quindi fa prima a completare tutto il giro attorno al Sole che a fare un solo giro completo su se stessa. Immagina di festeggiare Capodanno prima ancora che tramonti il Sole!",
        chibiReaction = "Il mio orologio spaziale è andato completamente in tilt su Venere!",
        relatedPlanetId = "venus",
        accentColor = CosmicAmber,
      ),
      CuriosityItem(
        id = "cur_2",
        title = "Piove diamanti su Urano e Nettuno?",
        category = CuriosityCategory.EXTREME_PLANETS,
        badgeTag = "PIOGGIA PREZIOSA",
        summary = "Nelle profondità di questi giganti ghiacciati la pressione trasforma il carbonio in cristalli di diamante!",
        deepDive =
          "Sotto le nuvole blu di Urano e Nettuno c'è così tanta pressione e calore che gli atomi di carbonio si schiacciano insieme formando veri diamanti grandi come sassolini che cadono verso il centro del pianeta!",
        chibiReaction = "Altro che ombrello, qui serve uno scudo spaziale super resistente!",
        relatedPlanetId = "neptune",
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_3",
        title = "Il vulcano alto quasi tre Everest!",
        category = CuriosityCategory.COSMIC_RECORDS,
        badgeTag = "RECORD ALTEZZA",
        summary = "Il Monte Olimpo su Marte è alto 22 chilometri: è la montagna più alta di tutto il Sistema Solare.",
        deepDive =
          "La sua base è così larga che coprirebbe l'intera Italia settentrionale! È cresciuto così tanto perché su Marte la crosta non si sposta come sulla Terra, quindi la lava si è accumulata sempre nello stesso punto per milioni di anni.",
        chibiReaction = "Anche con i razzi agli scarponi ci metterei giorni a scalarlo!",
        relatedPlanetId = "mars",
        accentColor = NebulaMagenta,
      ),
      CuriosityItem(
        id = "cur_4",
        title = "Un pianeta così leggero che galleggia!",
        category = CuriosityCategory.WEIRD_SPACE,
        badgeTag = "FISICA STRANA",
        summary = "Saturno ha una densità più bassa dell'acqua: 0,69 g/cm³ contro 1,0 dell'acqua.",
        deepDive =
          "Naturalmente non esiste una piscina abbastanza grande da contenerlo, ma essendo composto soprattutto da idrogeno ed elio gassosi, Saturno è il pianeta meno denso di tutti!",
        chibiReaction = "Un gigantesco salvagente con gli anelli!",
        relatedPlanetId = "saturn",
        accentColor = CosmicAmber,
      ),
      CuriosityItem(
        id = "cur_5",
        title = "Le impronte sulla Luna restano milioni di anni",
        category = CuriosityCategory.MOONS,
        badgeTag = "TRACCE ETERNE",
        summary = "Sulla Luna non ci sono vento, pioggia né atmosfera che possano cancellare le orme degli astronauti.",
        deepDive =
          "Le impronte lasciate dagli astronauti delle missioni Apollo nel 1969 sono ancora esattamente lì nella polvere lunare e resteranno intatte per milioni di anni!",
        chibiReaction = "Quando lascerò la mia impronta lì, ci metterò un bel timbro a stella!",
        relatedPlanetId = "moon",
        accentColor = AuroraGreen,
      ),
      CuriosityItem(
        id = "cur_6",
        title = "Nello spazio c'è silenzio assoluto",
        category = CuriosityCategory.MYSTERIES,
        badgeTag = "SILENZIO COSMICO",
        summary = "I suoni hanno bisogno di aria per viaggiare: nel vuoto spaziale neanche un'esplosione farebbe rumore!",
        deepDive =
          "Ecco perché gli astronauti nelle passeggiate spaziali comunicano tramite radio: le onde radio viaggiano anche nel vuoto, mentre le onde sonore non possono spostarsi senza molecole d'aria.",
        chibiReaction = "Per fortuna la radio del mio casco funziona alla grande!",
        relatedPlanetId = null,
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_7",
        title = "La tempesta più lunga della storia",
        category = CuriosityCategory.COSMIC_RECORDS,
        badgeTag = "SUPER URAGANO",
        summary = "La Grande Macchia Rossa su Giove infuria da almeno 350 anni senza mai fermarsi!",
        deepDive =
          "Sulla Terra gli uragani si indeboliscono quando incontrano la terraferma. Ma Giove non ha continenti solidi che li frenino, quindi questa gigantesca tempesta continua a girare da secoli!",
        chibiReaction = "È larga più di due Terre affiancate! Che spettacolo spaventoso!",
        relatedPlanetId = "jupiter",
        accentColor = NebulaMagenta,
      ),
      CuriosityItem(
        id = "cur_8",
        title = "Un milione di Terre dentro il Sole!",
        category = CuriosityCategory.COSMIC_RECORDS,
        badgeTag = "MEGA STELLA",
        summary = "Il volume del Sole è talmente enorme da poter contenere circa 1,3 milioni di pianeti Terra.",
        deepDive =
          "E pensare che nell'Universo il Sole è considerato una stella di medie dimensioni! Esistono stelle supergiganti centinaia di volte più grandi del nostro Sole.",
        chibiReaction = "Eppure da qui sembra grande solo come una moneta nel cielo!",
        relatedPlanetId = "sun",
        accentColor = CosmicAmber,
      ),
      CuriosityItem(
        id = "cur_9",
        title = "La Cintura degli Asteroidi non è un labirinto stretto",
        category = CuriosityCategory.WEIRD_SPACE,
        badgeTag = "SPAZIO APERTO",
        summary = "Nei film le navicelle fanno slalom tra rocce vicinissime, ma nella realtà sono lontanissime tra loro!",
        deepDive =
          "Tra Marte e Giove ci sono milioni di asteroidi, ma lo spazio è così immenso che la distanza media tra un asteroide grande e l'altro è di centinaia di migliaia di chilometri. Le sonde spaziali ci passano attraverso senza quasi vederne uno!",
        chibiReaction = "Meno male, così non devo fare slalom col razzo!",
        relatedPlanetId = "mars",
        accentColor = AuroraGreen,
      ),
      CuriosityItem(
        id = "cur_10",
        title = "Tramonti blu su Marte!",
        category = CuriosityCategory.EXTREME_PLANETS,
        badgeTag = "COLORI INVERTITI",
        summary = "Sulla Terra il cielo è blu e il tramonto rosso; su Marte il cielo è rosato e il tramonto è blu!",
        deepDive =
          "La polvere sottile sospesa nell'aria marziana diffonde la luce rossa durante il giorno, ma vicino al tramonto lascia passare meglio la luce blu attorno al disco solare!",
        chibiReaction = "Un tramonto blu alieno: perfetto per una foto ricordo!",
        relatedPlanetId = "mars",
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_11",
        title = "Europa nasconde un oceano segreto",
        category = CuriosityCategory.MOONS,
        badgeTag = "LUNA DI GIOVE",
        summary = "Sotto la crosta di ghiaccio di Europa, luna di Giove, c'è più acqua liquida che in tutti gli oceani terrestri!",
        deepDive =
          "Gli scienziati invieranno sonde speciali per studiare Europa perché dove c'è acqua liquida ed energia potrebbe esserci qualche forma di vita microscopica!",
        chibiReaction = "Chissà se nuotano batteri alieni sotto tutto quel ghiaccio!",
        relatedPlanetId = "jupiter",
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_12",
        title = "Un cucchiaino di stella di neutroni",
        category = CuriosityCategory.EXTREME_PLANETS,
        badgeTag = "SUPER PESO",
        summary = "Anche se non è nel nostro Sistema Solare, un solo cucchiaino di una stella di neutroni peserebbe miliardi di tonnellate!",
        deepDive =
          "Quando stelle molto più grandi del Sole finiscono il carburante, il loro nucleo si comprime così tanto che tutta la materia viene schiacciata in una sfera larga solo 20 km ma pesantissima!",
        chibiReaction = "Neanche dieci razzi potrebbero sollevarne un granello!",
        relatedPlanetId = null,
        accentColor = NebulaMagenta,
      ),
      CuriosityItem(
        id = "cur_13",
        title = "La Luna ci mostra sempre la stessa faccia!",
        category = CuriosityCategory.MOONS,
        badgeTag = "NOSTRA LUNA",
        summary = "Dalla Terra vediamo sempre lo stesso lato della Luna perché ruota su se stessa nello stesso tempo in cui gira intorno a noi!",
        deepDive =
          "Questo fenomeno si chiama rotazione sincrona: la faccia nascosta della Luna non è mai buia (riceve il Sole come l'altra), ma gli umani l'hanno vista per la prima volta solo nel 1959 grazie a una sonda spaziale!",
        chibiReaction = "È come se la Luna stesse ballando il girotondo guardandoci sempre negli occhi!",
        relatedPlanetId = "moon",
        accentColor = CosmicAmber,
      ),
      CuriosityItem(
        id = "cur_14",
        title = "La Luna si allontana piano piano",
        category = CuriosityCategory.MOONS,
        badgeTag = "NOSTRA LUNA",
        summary = "Ogni anno la Luna si sposta circa 3,8 centimetri più lontano dalla Terra, quanto crescono le unghie!",
        deepDive =
          "È colpa (o merito!) delle maree negli oceani terrestri: l'attrito dell'acqua frena leggermente la Terra e spinge la Luna su un'orbita un pochino più larga. Miliardi di anni fa era molto più vicina!",
        chibiReaction = "Tra milioni di anni sembrerà un po' più piccolina nel cielo!",
        relatedPlanetId = "moon",
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_15",
        title = "Io, la luna piena di vulcani!",
        category = CuriosityCategory.MOONS,
        badgeTag = "LUNA DI GIOVE",
        summary = "Io è il mondo più vulcanico di tutto il Sistema Solare, con oltre 400 vulcani attivi che le danno l'aspetto di una pizza!",
        deepDive =
          "La gigantesca gravità di Giove e delle lune vicine strizza continuamente Io come una spugna, riscaldando il suo interno fino a farlo eruttare fontane di lava alte centinaia di chilometri!",
        chibiReaction = "Troppo bollente per atterrarci senza scarponi ignifughi!",
        relatedPlanetId = "jupiter",
        accentColor = NebulaMagenta,
      ),
      CuriosityItem(
        id = "cur_16",
        title = "Encelado spara fontane di ghiaccio!",
        category = CuriosityCategory.MOONS,
        badgeTag = "LUNA DI SATURNO",
        summary = "La piccola luna bianca Encelado ha geyser giganti al polo sud che sparano acqua e ghiaccio nello spazio!",
        deepDive =
          "Questi getti spettacolari alimentano uno degli anelli esterni di Saturno! Le sonde spaziali ci hanno volato attraverso scoprendo che sotto il ghiaccio c'è un oceano salato tiepido.",
        chibiReaction = "Una luna che fabbrica gli anelli di Saturno con i suoi geyser!",
        relatedPlanetId = "saturn",
        accentColor = CosmicCyan,
      ),
      CuriosityItem(
        id = "cur_17",
        title = "Su Titano piove metano invece di acqua",
        category = CuriosityCategory.MOONS,
        badgeTag = "LUNA DI SATURNO",
        summary = "Titano è l'unica luna con un'atmosfera densa e ha fiumi, nuvole e laghi fatti di metano liquido!",
        deepDive =
          "Su Titano fa così freddo (-180 °C) che il ghiaccio è duro come la roccia, mentre il gas metano diventa liquido e scorre proprio come l'acqua dei fiumi sulla Terra!",
        chibiReaction = "Un mondo alieno gemello della Terra... ma tutto ghiacciato!",
        relatedPlanetId = "saturn",
        accentColor = AuroraGreen,
      ),
      CuriosityItem(
        id = "cur_18",
        title = "Phobos sorge a Ovest e sfreccia su Marte!",
        category = CuriosityCategory.MOONS,
        badgeTag = "LUNA DI MARTE",
        summary = "La luna Phobos gira intorno a Marte così velocemente da compiere più di tre giri al giorno!",
        deepDive =
          "Mentre la nostra Luna impiega quasi un mese per fare un giro, Phobos orbita vicinissima a Marte in appena 7 ore e 39 minuti, sorgendo a Ovest e tramontando a Est!",
        chibiReaction = "Sembra di vedere una stella cadente gigante ogni giorno su Marte!",
        relatedPlanetId = "mars",
        accentColor = CosmicAmber,
      ),
    )

  val quizQuestions: List<QuizQuestion> =
    listOf(
      QuizQuestion(
        id = "q1",
        planetId = "sun",
        question = "Quanto tempo impiega la luce del Sole per raggiungere la Terra?",
        options = listOf("Circa 8 minuti", "Circa 8 ore", "É istantanea", "Circa 2 giorni"),
        correctIndex = 0,
        chibiRightComment = "Esatto! Viaggia a 300.000 km al secondo e arriva in 8 minuti e 20 secondi!",
        chibiWrongHint = "Quasi! Anche alla velocità della luce servono circa 8 minuti per fare 150 milioni di km!",
      ),
      QuizQuestion(
        id = "q2",
        planetId = "mercury",
        question = "Qual è il pianeta più veloce a fare un giro intorno al Sole?",
        options = listOf("Marte", "Mercurio", "Terra", "Nettuno"),
        correctIndex = 1,
        chibiRightComment = "Grande! Mercurio è vicinissimo al Sole e completa l'orbita in soli 88 giorni!",
        chibiWrongHint = "Ricorda: più sei vicino al Sole, più vai veloce! È proprio Mercurio!",
      ),
      QuizQuestion(
        id = "q3",
        planetId = "venus",
        question = "Qual è il pianeta più caldo del Sistema Solare?",
        options = listOf("Mercurio", "Marte", "Venere", "Giove"),
        correctIndex = 2,
        chibiRightComment = "Super! La spessa atmosfera di Venere fa effetto serra e porta il pianeta a 465 °C!",
        chibiWrongHint = "Attenzione al tranello! Mercurio è più vicino, ma Venere intrappola il calore ed è più caldo!",
      ),
      QuizQuestion(
        id = "q4",
        planetId = "earth",
        question = "Cosa causa le quattro stagioni sulla Terra?",
        options = listOf(
          "Le nuvole che coprono il Sole",
          "L'inclinazione dell'asse terrestre",
          "Le maree della Luna",
          "Il vento solare",
        ),
        correctIndex = 1,
        chibiRightComment = "Perfetto! L'asse della Terra è inclinato di 23,4° e cambia quanta luce arriva in ogni stagione!",
        chibiWrongHint = "Guarda la Terra inclinata in 3D: è proprio quell'inclinazione di 23,4° a creare le stagioni!",
      ),
      QuizQuestion(
        id = "q5",
        planetId = "mars",
        question = "Perché Marte ha quel caratteristico colore rosso?",
        options = listOf(
          "È coperto di lava bollente",
          "È pieno di polvere ricca di ferro arrugginito",
          "Ha oceani di succo rosso",
          "Riflette il colore del Sole",
        ),
        correctIndex = 1,
        chibiRightComment = "Esatto! La superficie di Marte è piena di ossido di ferro, cioè ruggine!",
        chibiWrongHint = "Pensa a un ferro lasciato fuori: si arrugginisce! Proprio come la polvere di Marte!",
      ),
      QuizQuestion(
        id = "q6",
        planetId = "mars",
        question = "Come si chiama il vulcano gigantesco su Marte?",
        options = listOf("Monte Etna", "Monte Olimpo", "Picco Rosso", "Cratere Phobos"),
        correctIndex = 1,
        chibiRightComment = "Bravissimo! Il Monte Olimpo è alto 22 km, quasi tre volte l'Everest!",
        chibiWrongHint = "Prende il nome dalla casa degli antichi dèi greci: il Monte Olimpo!",
      ),
      QuizQuestion(
        id = "q7",
        planetId = "jupiter",
        question = "Cos'è la Grande Macchia Rossa di Giove?",
        options = listOf(
          "Un gigantesco continente di pietra",
          "Un enorme uragano che dura da secoli",
          "Un lago di lava",
          "L'ombra di una sua luna",
        ),
        correctIndex = 1,
        chibiRightComment = "Centro pieno! È una tempesta più grande della Terra che soffia da oltre 300 anni!",
        chibiWrongHint = "Su Giove non c'è terreno solido: è un gigantesco uragano che soffia da centinaia d'anni!",
      ),
      QuizQuestion(
        id = "q8",
        planetId = "jupiter",
        question = "Qual è il pianeta più grande del Sistema Solare?",
        options = listOf("Saturno", "Nettuno", "Giove", "Urano"),
        correctIndex = 2,
        chibiRightComment = "Giove è il re! Potrebbe contenere più di 1.300 pianeti grandi come la Terra!",
        chibiWrongHint = "Il re indiscusso per grandezza è Giove!",
      ),
      QuizQuestion(
        id = "q9",
        planetId = "saturn",
        question = "Di cosa sono fatti principalmente gli anelli di Saturno?",
        options = listOf(
          "Anelli di metallo solido",
          "Miliardi di frammenti di ghiaccio e roccia",
          "Nuvole di fuoco",
          "Acqua liquida",
        ),
        correctIndex = 1,
        chibiRightComment = "Esatto! Tanti blocchi di ghiaccio e polvere che brillano riflettendo la luce del Sole!",
        chibiWrongHint = "Sembrano solidi da lontano, ma da vicino sono miliardi di pezzi di ghiaccio e roccia!",
      ),
      QuizQuestion(
        id = "q10",
        planetId = "saturn",
        question = "Quale pianeta ha una densità così bassa che in teoria galleggerebbe sull'acqua?",
        options = listOf("Marte", "Mercurio", "Saturno", "Terra"),
        correctIndex = 2,
        chibiRightComment = "Geniale! Saturno è leggerissimo rispetto alle sue dimensioni enormi!",
        chibiWrongHint = "Il gigante gassoso con gli anelli è meno denso dell'acqua: è Saturno!",
      ),
      QuizQuestion(
        id = "q11",
        planetId = "uranus",
        question = "Qual è la stranezza più famosa di Urano?",
        options = listOf(
          "È quadrato",
          "Ruota completamente sdraiato su un fianco",
          "Non ha nessuna luna",
          "È fatto di oro puro",
        ),
        correctIndex = 1,
        chibiRightComment = "Proprio così! Urano rotola lungo la sua orbita inclinato di quasi 98 gradi!",
        chibiWrongHint = "Urano sembra una palla che rotola di fianco: è inclinato di quasi 98 gradi!",
      ),
      QuizQuestion(
        id = "q12",
        planetId = "neptune",
        question = "Come fu scoperto Nettuno nel 1846?",
        options = listOf(
          "Con calcoli matematici prima ancora di vederlo",
          "Ci atterrò un razzo per caso",
          "Si vedeva enorme a occhio nudo",
          "Attraverso un meteorite caduto",
        ),
        correctIndex = 0,
        chibiRightComment = "Fantastico! Gli scienziati usarono la matematica per capire dove doveva trovarsi!",
        chibiWrongHint = "La matematica è superpotere! Nettuno venne previsto coi calcoli prima del telescopio!",
      ),
      QuizQuestion(
        id = "q13",
        planetId = "pluto",
        question = "Cosa c'è di famoso disegnato dal ghiaccio su Plutone?",
        options = listOf("Una stella a 5 punte", "Un gigantesco cuore", "Un triangolo nero", "Una spirale"),
        correctIndex = 1,
        chibiRightComment = "Che dolcezza spaziale! Una pianura luminosa di azoto ghiacciato proprio a forma di cuore!",
        chibiWrongHint = "La sonda New Horizons fotografò un enorme cuore bianco di ghiaccio su Plutone!",
      ),
      QuizQuestion(
        id = "q14",
        planetId = null,
        isTrueFalse = true,
        question = "Vero o Falso: nello spazio aperto le astronavi fanno un rumore assordante quando passano?",
        options = listOf("Vero", "Falso"),
        correctIndex = 1,
        chibiRightComment = "Bravissimo! Nel vuoto dello spazio non c'è aria, quindi c'è silenzio totale!",
        chibiWrongHint = "È un trucco dei film! Senza aria nello spazio il suono non può viaggiare: è Falso!",
      ),
      QuizQuestion(
        id = "q15",
        planetId = null,
        isTrueFalse = true,
        question = "Vero o Falso: Venere ruota su se stessa nel senso opposto rispetto alla Terra?",
        options = listOf("Vero", "Falso"),
        correctIndex = 0,
        chibiRightComment = "Vero! Su Venere il Sole sorge a Ovest e tramonta a Est!",
        chibiWrongHint = "In realtà è Vero! Venere è uno dei pochissimi corpi che gira al contrario!",
      ),
      QuizQuestion(
        id = "q16",
        planetId = null,
        isTrueFalse = true,
        question = "Vero o Falso: tra Marte e Giove si trova la Cintura degli Asteroidi?",
        options = listOf("Vero", "Falso"),
        correctIndex = 0,
        chibiRightComment = "Esatto! Divide i pianeti rocciosi interni dai giganti esterni!",
        chibiWrongHint = "Proprio tra Marte e Giove orbita la fascia principale degli asteroidi: è Vero!",
      ),
      QuizQuestion(
        id = "q17",
        planetId = null,
        isTrueFalse = true,
        question = "Vero o Falso: il pianeta con più lune conosciute nel Sistema Solare è la Terra?",
        options = listOf("Vero", "Falso"),
        correctIndex = 1,
        chibiRightComment = "Falso! La Terra ha 1 sola Luna, mentre Saturno e Giove ne hanno decine e decine!",
        chibiWrongHint = "Noi abbiamo solo la nostra fedele Luna! Saturno ne ha oltre 140!",
      ),
      QuizQuestion(
        id = "q18",
        planetId = "earth",
        question = "Qual è l'ordine esatto dei primi quattro pianeti partendo dal Sole?",
        options = listOf(
          "Mercurio, Venere, Terra, Marte",
          "Venere, Mercurio, Marte, Terra",
          "Terra, Marte, Venere, Mercurio",
          "Mercurio, Terra, Venere, Marte",
        ),
        correctIndex = 0,
        chibiRightComment = "Perfetto! Me-Ve-Te-Ma: Mercurio, Venere, Terra e Marte!",
        chibiWrongHint = "Ricorda il trucco: Me-Ve-Te-Ma! Mercurio, Venere, Terra, Marte!",
      ),
      QuizQuestion(
        id = "q19",
        planetId = "moon",
        question = "Perché sulla Luna faresti salti altissimi con molta facilità?",
        options = listOf(
          "Perché il suolo è elastico",
          "Perché la gravità è circa 6 volte più debole della Terra",
          "Perché ti spinge il vento lunare",
          "Perché la Luna gira velocissima",
        ),
        correctIndex = 1,
        chibiRightComment = "Esatto! Con 1/6 della gravità terrestre ti sentiresti leggero come una piuma!",
        chibiWrongHint = "Sulla Luna non c'è vento! È proprio la gravità ridotta a un sesto che fa fare super salti!",
      ),
      QuizQuestion(
        id = "q20",
        planetId = "moon",
        question = "Come fa la Luna a splendere nel cielo notturno?",
        options = listOf(
          "Produce luce propria come il Sole",
          "Riflette la luce del Sole come uno specchio",
          "È fatta di rocce fosforescenti",
          "Brilla grazie alle stelle vicine",
        ),
        correctIndex = 1,
        chibiRightComment = "Bravissimo! La Luna riflette la luce solare e per questo vediamo le fasi lunari!",
        chibiWrongHint = "La Luna è roccia fredda: splende solo perché riflette la luce del Sole!",
      ),
    )

  val badges: List<SpaceBadge> =
    listOf(
      SpaceBadge(
        id = "badge_first_planet",
        title = "Primo Decollo",
        description = "Hai esplorato il tuo primo pianeta in 3D!",
        accentColor = CosmicCyan,
      ),
      SpaceBadge(
        id = "badge_inner_system",
        title = "Esploratore Roccioso",
        description = "Hai studiato tutti i pianeti rocciosi interni!",
        accentColor = CosmicAmber,
      ),
      SpaceBadge(
        id = "badge_all_planets",
        title = "Comandante Solare",
        description = "Hai visitato tutti i corpi del Sistema Solare!",
        accentColor = AuroraGreen,
      ),
      SpaceBadge(
        id = "badge_quiz_star",
        title = "Asso dei Quiz",
        description = "Hai totalizzato almeno 6 risposte giuste in una sfida!",
        accentColor = NebulaMagenta,
      ),
    )

  fun getPlanetById(id: String): CelestialBody =
    celestialBodies.firstOrNull { it.id == id } ?: celestialBodies.first()
}
