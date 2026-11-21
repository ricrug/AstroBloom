package com.ricrug.data

import androidx.compose.ui.graphics.Color
import com.ricrug.R

/**
 * Fotografia reale documentata ufficialmente da agenzie spaziali (NASA, ESA, Roscosmos, JAXA).
 */
data class OfficialPhotoRecord(
  val id: String,
  val planetId: String,
  val title: String,
  val mission: String,
  val agency: String,
  val captureDate: String,
  val imageUrl: String = "",
  val drawableRes: Int? = null,
  val caption: String,
  val scientificTip: String,
  val category: String,
  val credit: String,
)

/**
 * Punto di interesse storico o geologico sulla superficie planetaria.
 */
data class SurfacePoi(
  val id: String,
  val planetId: String,
  val name: String,
  val poiType: String,
  val posX: Float,
  val posZ: Float,
  val description: String,
  val associatedPhotoId: String? = null,
)

/**
 * Campione geologico e minerale raccoglibile sul suolo planetario.
 */
data class SurfaceSample(
  val id: String,
  val planetId: String,
  val name: String,
  val mineralClass: String,
  val posX: Float,
  val posZ: Float,
  val spectrometryFormula: String,
  val scientificDiscovery: String,
  val rarity: String,
  val xpValue: Int,
  val associatedPhotoId: String? = null,
)

/**
 * Configurazione fisica e visiva dell'ambiente planetario in prima persona.
 */
data class PlanetEnvironmentConfig(
  val planetId: String,
  val planetName: String,
  val gravityMs2: Float,
  val gravityRatio: Float,
  val avgTempCelsius: Int,
  val pressureAtm: Float,
  val atmosphereComposition: String,
  val skyTopColor: Color,
  val skyHorizonColor: Color,
  val terrainPrimaryColor: Color,
  val terrainSecondaryColor: Color,
  val rockColor: Color,
  val sunScale: Float,
  val dustDensity: Float,
  val visibilityMeters: Float,
  val landscapeFeature: String,
  val suitWarningText: String?,
)

/**
 * Repository dei dati scientifici, storici e fotografici ufficiali per l'esplorazione planetaria.
 */
object SurfaceExplorationRepository {

  /**
   * Archivio fotografico documentato con foto ufficiali NASA / ESA / Roscosmos.
   */
  val officialPhotos: List<OfficialPhotoRecord> = listOf(
    // === LUNA ===
    OfficialPhotoRecord(
      id = "moon_apollo11_bootprint",
      planetId = "moon",
      title = "La celebre impronta sulla regolite lunare",
      mission = "Apollo 11",
      agency = "NASA",
      captureDate = "20 Luglio 1969",
      imageUrl = "https://images-assets.nasa.gov/image/AS11-40-5878/AS11-40-5878~orig.jpg",
      drawableRes = R.drawable.photo_moon_apollo11_bootprint,
      caption = "Scattata da Buzz Aldrin per studiare la meccanica del suolo lunare. A causa della mancanza di vento e atmosfera, l'impronta resterà intatta per milioni di anni.",
      scientificTip = "Nota i granuli microscopici aguzzi: la regolite non è levigata da erosione acquatica o atmosferica, quindi è abrasiva come polvere di vetro.",
      category = "Geologia Lunare",
      credit = "NASA / Apollo 11 Crew",
    ),
    OfficialPhotoRecord(
      id = "moon_earthrise",
      planetId = "moon",
      title = "Earthrise: La Terra sorge dall'orizzonte lunare",
      mission = "Apollo 8 / LRO",
      agency = "NASA",
      captureDate = "24 Dicembre 1968",
      imageUrl = "https://images-assets.nasa.gov/image/as08-14-2383/as08-14-2383~orig.jpg",
      drawableRes = R.drawable.photo_moon_earthrise,
      caption = "Una delle fotografie più iconiche della storia umana. Mostra la fragilità del nostro pianeta azzurro immerso nel buio cosmico oltre la desolazione della Luna.",
      scientificTip = "Sulla Luna non c'è atmosfera che diffonde la luce: il cielo resta nero come la pece anche in pieno giorno quando il Sole è alto.",
      category = "Spazio Profondo",
      credit = "NASA / William Anders",
    ),
    OfficialPhotoRecord(
      id = "moon_apollo11_eagle",
      planetId = "moon",
      title = "Il modulo di discesa Eagle alla Tranquility Base",
      mission = "Apollo 11",
      agency = "NASA",
      captureDate = "20 Luglio 1969",
      imageUrl = "https://images-assets.nasa.gov/image/as11-40-5927/as11-40-5927~orig.jpg",
      drawableRes = R.drawable.photo_moon_apollo11_eagle,
      caption = "Il LEM Eagle posato sulla superficie lunare con la lamina d'oro termoriflettente e i cuscinetti delle zampe immersi nella regolite del Mare Tranquillitatis.",
      scientificTip = "L'oro e il mylar servivano a riflettere l'intensa radiazione solare: sul suolo lunare la temperatura diurna supera i +120°C per poi crollare a -130°C di notte.",
      category = "Missioni Storiche",
      credit = "NASA / Neil Armstrong",
    ),

    // === MARTE ===
    OfficialPhotoRecord(
      id = "mars_perseverance_jezero",
      planetId = "mars",
      title = "Panorama ad altissima definizione del Cratere Jezero",
      mission = "Mars 2020 Perseverance",
      agency = "NASA / JPL-Caltech",
      captureDate = "22 Febbraio 2021",
      imageUrl = "https://images-assets.nasa.gov/image/PIA24424/PIA24424~orig.jpg",
      drawableRes = R.drawable.photo_mars_perseverance_jezero,
      caption = "La fotocamera Mastcam-Z di Perseverance ha immortalato il delta fluviale fossile di Jezero, dove 3.5 miliardi di anni fa scorreva un fiume d'acqua liquida.",
      scientificTip = "Cerca le stratificazioni orizzontali nelle formazioni rocciose: testimoniano il deposito graduale di sedimenti lacustri ricchi di argille e carbonati.",
      category = "Sedimentologia Marziana",
      credit = "NASA / JPL-Caltech / MSSS / ASU",
    ),
    OfficialPhotoRecord(
      id = "mars_ingenuity_flight",
      planetId = "mars",
      title = "L'elicottero marziano Ingenuity in volo sul suolo rosso",
      mission = "Mars 2020 / Ingenuity",
      agency = "NASA / JPL-Caltech",
      captureDate = "19 Aprile 2021",
      imageUrl = "https://images-assets.nasa.gov/image/PIA24497/PIA24497~orig.jpg",
      drawableRes = R.drawable.photo_mars_ingenuity_flight,
      caption = "Il primo volo a motore controllato su un altro pianeta. Le pale in fibra di carbonio ruotano a 2.400 giri/min per sostenersi nell'atmosfera rarefatta all'1% di quella terrestre.",
      scientificTip = "La pressione atmosferica marziana è di soli 0.006 atm: volare su Marte equivale a volare sulla Terra a oltre 30.000 metri di altitudine!",
      category = "Ingegneria Spaziale",
      credit = "NASA / JPL-Caltech",
    ),
    OfficialPhotoRecord(
      id = "mars_curiosity_selfie",
      planetId = "mars",
      title = "Curiosity ai piedi del Monte Sharp (Gale Crater)",
      mission = "Mars Science Laboratory Curiosity",
      agency = "NASA / JPL-Caltech",
      captureDate = "15 Giugno 2018",
      imageUrl = "https://images-assets.nasa.gov/image/PIA22486/PIA22486~orig.jpg",
      drawableRes = R.drawable.photo_mars_curiosity_selfie,
      caption = "Autoritratto del rover Curiosity durante una tempesta di polvere globale che ha oscurato il cielo marziano con particelle di ossido di ferro in sospensione.",
      scientificTip = "Il cielo marziano appare salmone/ambrato a causa delle particelle di magnetite e ruggine sollevate dai turbini d'aria (dust devils).",
      category = "Esplorazione Rover",
      credit = "NASA / JPL-Caltech / MSSS",
    ),
    OfficialPhotoRecord(
      id = "mars_blue_sunset",
      planetId = "mars",
      title = "Il celebre Tramonto Blu su Marte",
      mission = "Curiosity Rover",
      agency = "NASA / JPL-Caltech",
      captureDate = "15 Aprile 2015",
      imageUrl = "https://images-assets.nasa.gov/image/PIA19400/PIA19400~orig.jpg",
      drawableRes = R.drawable.photo_mars_blue_sunset,
      caption = "A differenza della Terra, su Marte il cielo diurno è aranciato e il tramonto è azzurro! La polvere fine diffonde la luce blu in avanti verso l'osservatore.",
      scientificTip = "Il fenomeno è noto come scattering di Mie: le particelle di polvere hanno dimensioni simili alla lunghezza d'onda della luce blu, che penetra più facilmente.",
      category = "Ottica Atmosferica",
      credit = "NASA / JPL-Caltech / MSSS / Texas A&M Univ.",
    ),

    // === VENERE ===
    OfficialPhotoRecord(
      id = "venus_venera13_surface",
      planetId = "venus",
      title = "Prima foto a colori dal vero suolo di Venere",
      mission = "Venera 13",
      agency = "Roscosmos / IKI",
      captureDate = "1 Marzo 1982",
      imageUrl = "https://photojournal.jpl.nasa.gov/jpeg/PIA00107.jpg",
      drawableRes = R.drawable.photo_venus_venera13_surface,
      caption = "Il lander sovietico Venera 13 ha resistito 127 minuti all'inferno di Venere: 462°C di temperatura e 92 atmosfere di pressione. Mostra placche di basalto vulcanico scuro.",
      scientificTip = "La luce del Sole viene filtrata dalle spesse nubi di acido solforico, creando un'illuminazione diffusa dorata simile a una giornata coperta sul fondo di un oceano.",
      category = "Geologia Vulcanica",
      credit = "Soviet Academy of Sciences / NASA NSSDC",
    ),
    OfficialPhotoRecord(
      id = "venus_magellan_radar",
      planetId = "venus",
      title = "Cupole vulcaniche e canali di lava di Venere",
      mission = "Magellan",
      agency = "NASA / JPL",
      captureDate = "18 Settembre 1991",
      imageUrl = "https://images-assets.nasa.gov/image/PIA00201/PIA00201~orig.jpg",
      drawableRes = R.drawable.photo_venus_magellan_radar,
      caption = "Mappa radar ad altissima risoluzione che rivela vulcani a forma di 'frittella' (pancake domes) formati da colate viscose di lava basaltica ricca di silice.",
      scientificTip = "La pressione di 92 bar rende l'atmosfera simile a un fluido supercritico che rallenta il raffreddamento delle rocce fuse eruttate dai vulcani venusiani.",
      category = "Vulcanismo",
      credit = "NASA / JPL",
    ),

    // === MERCURIO ===
    OfficialPhotoRecord(
      id = "mercury_messenger_caloris",
      planetId = "mercury",
      title = "Il gigantesco bacino da impatto Caloris",
      mission = "MESSENGER",
      agency = "NASA / JHUAPL / CIW",
      captureDate = "14 Gennaio 2008",
      imageUrl = "https://images-assets.nasa.gov/image/PIA10383/PIA10383~orig.jpg",
      drawableRes = R.drawable.photo_mercury_messenger_caloris,
      caption = "Uno dei più imponenti crateri da impatto del Sistema Solare (1.550 km di diametro), circondato da creste concentriche alte oltre 2.000 metri formatesi 3.8 miliardi di anni fa.",
      scientificTip = "L'impatto fu così energetico che le onde sismiche hanno attraversato il nucleo metallico di Mercurio creando un terreno caotico e fratturato agli antipodi del pianeta.",
      category = "Crateri d'Impatto",
      credit = "NASA / Johns Hopkins University Applied Physics Laboratory / Carnegie Institution of Washington",
    ),
    OfficialPhotoRecord(
      id = "mercury_discovery_scarp",
      planetId = "mercury",
      title = "Le scarpate tettoniche di contrazione (Discovery Scarp)",
      mission = "MESSENGER / Mariner 10",
      agency = "NASA",
      captureDate = "29 Marzo 1974",
      imageUrl = "https://images-assets.nasa.gov/image/PIA11364/PIA11364~orig.jpg",
      drawableRes = R.drawable.photo_mercury_discovery_scarp,
      caption = "Faglie inverse alte chilometri prodotte dal raffreddamento e contrazione del nucleo ferroso di Mercurio: il pianeta si è letteralmente ristretto di circa 7 km nel raggio!",
      scientificTip = "Mercurio possiede un nucleo di ferro sproporzionato che occupa oltre il 70% del suo raggio totale.",
      category = "Tettonica Planetaria",
      credit = "NASA / JHUAPL / CIW",
    ),

    // === GIOVE ED EUROPA ===
    OfficialPhotoRecord(
      id = "jupiter_europa_cracks",
      planetId = "jupiter",
      title = "La banchisa ghiacciata e le 'lineae' di Europa",
      mission = "Galileo / Juno",
      agency = "NASA / JPL-Caltech",
      captureDate = "29 Settembre 2022",
      imageUrl = "https://images-assets.nasa.gov/image/PIA25327/PIA25327~orig.jpg",
      drawableRes = R.drawable.photo_jupiter_europa_cracks,
      caption = "Primo piano della crosta ghiacciata della luna Europa: le fratture rossastre sono causate dalle forze mareali di Giove che flettono il ghiaccio sopra un immenso oceano liquido sotterraneo.",
      scientificTip = "Sotto 20 km di crosta ghiacciata si cela un oceano salato profondo oltre 100 km contenente più del doppio dell'acqua di tutti gli oceani terrestri messi insieme!",
      category = "Astrobiologia",
      credit = "NASA / JPL-Caltech / SwRI / MSSS",
    ),
    OfficialPhotoRecord(
      id = "jupiter_great_red_spot",
      planetId = "jupiter",
      title = "La Grande Macchia Rossa ripresa da vicino da Juno",
      mission = "Juno",
      agency = "NASA / JPL-Caltech",
      captureDate = "11 Luglio 2017",
      imageUrl = "https://images-assets.nasa.gov/image/PIA21775/PIA21775~orig.jpg",
      drawableRes = R.drawable.photo_jupiter_great_red_spot,
      caption = "Un ciclone antiorario più grande della Terra, attivo da almeno 350 anni, con venti che soffiano a oltre 640 km/h nella densa troposfera gioviana.",
      scientificTip = "Il colore rosso mattone è dovuto alla fotolisi dei composti di fosforo e ammoniaca esposti ai raggi ultravioletti della luce solare.",
      category = "Meteo Planetario",
      credit = "NASA / JPL-Caltech / SwRI / MSSS / Gerald Eichstädt / Seán Doran",
    ),

    // === SATURNO E TITANO ===
    OfficialPhotoRecord(
      id = "saturn_huygens_titan",
      planetId = "saturn",
      title = "I ciottoli di ghiaccio sulla superficie di Titano",
      mission = "Cassini-Huygens",
      agency = "ESA / NASA / JPL",
      captureDate = "14 Gennaio 2005",
      imageUrl = "https://images-assets.nasa.gov/image/PIA07232/PIA07232~orig.jpg",
      drawableRes = R.drawable.photo_saturn_huygens_titan,
      caption = "L'unica immagine ravvicinata mai scattata sul suolo della luna Titano a -180°C. Quelli che sembrano sassi sono in realtà ciottoli di ghiaccio d'acqua duro come granito, levigati da fiumi di metano liquido.",
      scientificTip = "Titano possiede un ciclo idrologico completo, ma con metano ed etano al posto dell'acqua: nubi di metano, piogge criogeniche, fiumi e laghi liquidi!",
      category = "Criovulcanismo & Idrocarburi",
      credit = "ESA / NASA / JPL / University of Arizona",
    ),
    OfficialPhotoRecord(
      id = "saturn_enceladus_plumes",
      planetId = "saturn",
      title = "I geyser ghiacciati di Encelado",
      mission = "Cassini",
      agency = "NASA / ESA / ASI",
      captureDate = "27 Novembre 2005",
      imageUrl = "https://images-assets.nasa.gov/image/PIA07758/PIA07758~orig.jpg",
      drawableRes = R.drawable.photo_saturn_enceladus_plumes,
      caption = "La sonda Cassini ha fotografato spettacolari geyser di vapore acqueo, particelle di ghiaccio e molecole organiche che zampillano dalle fratture polari dette 'Tiger Stripes'.",
      scientificTip = "I geyser alimentano direttamente il maestoso anello E di Saturno e provano l'esistenza di bocche idrotermali sul fondale dell'oceano interno di Encelado.",
      category = "Oceanografia Spaziale",
      credit = "NASA / JPL / Space Science Institute",
    ),

    // === PLUTONE ===
    OfficialPhotoRecord(
      id = "pluto_sputnik_planitia",
      planetId = "pluto",
      title = "Il grande cuore ghiacciato di Plutone (Sputnik Planitia)",
      mission = "New Horizons",
      agency = "NASA / JHUAPL / SwRI",
      captureDate = "14 Luglio 2015",
      imageUrl = "https://images-assets.nasa.gov/image/PIA19952/PIA19952~orig.jpg",
      drawableRes = R.drawable.photo_pluto_sputnik_planitia,
      caption = "Una pianura sconfinata di ghiaccio di azoto, monossido di carbonio e metano a -230°C, suddivisa in celle convettive esagonali attive che rinnovano costantemente il suolo.",
      scientificTip = "Le montagne circostanti (Hillary Montes) sono costituite da blocchi di ghiaccio d'acqua alti oltre 3.500 metri che galleggiano sull'azoto solido più denso.",
      category = "Mondi Glaciali",
      credit = "NASA / Johns Hopkins University Applied Physics Laboratory / Southwest Research Institute",
    ),

    // === TERRA ===
    OfficialPhotoRecord(
      id = "earth_blue_marble",
      planetId = "earth",
      title = "The Blue Marble: Il gioiello vivente del Cosmo",
      mission = "Apollo 17",
      agency = "NASA",
      captureDate = "7 Dicembre 1972",
      imageUrl = "https://images-assets.nasa.gov/image/AS17-148-22727/AS17-148-22727~orig.jpg",
      drawableRes = R.drawable.photo_earth_blue_marble,
      caption = "Scattata dall'equipaggio dell'Apollo 17 a 29.000 km di distanza. Mostra l'Africa, l'Antartide e l'Oceano Indiano illuminati dal Sole in tutto il loro splendore.",
      scientificTip = "L'unico pianeta conosciuto con acqua liquida superficiale stabile, un campo magnetico protettivo e un'atmosfera ricca di ossigeno e azoto favorevole alla vita complessa.",
      category = "Pianeta Casa",
      credit = "NASA / Apollo 17 Crew",
    ),
  )

  /**
   * Punti di interesse storici e scientifici per ciascun pianeta.
   */
  val surfacePois: List<SurfacePoi> = listOf(
    // Luna
    SurfacePoi(
      id = "moon_poi_apollo11",
      planetId = "moon",
      name = "Tranquility Base (LEM Eagle & Bandiera)",
      poiType = "Sito Storico Umano",
      posX = 0f,
      posZ = 12f,
      description = "Il primo approdo umano extraterrestre del 20 luglio 1969. Qui giacciono il modulo di discesa Eagle, la bandiera americana e il sismografo EASEP.",
      associatedPhotoId = "moon_apollo11_eagle",
    ),
    SurfacePoi(
      id = "moon_poi_tycho_boulder",
      planetId = "moon",
      name = "Monolite da Impatto Cratere Tycho",
      poiType = "Roccia Espulsa da Impatto",
      posX = -22f,
      posZ = 34f,
      description = "Un masso gigantesco di anortosite bianca scagliato per centinaia di chilometri durante la formazione del giovane cratere Tycho.",
      associatedPhotoId = "moon_apollo11_bootprint",
    ),
    SurfacePoi(
      id = "moon_poi_earth_viewpoint",
      planetId = "moon",
      name = "Cresta di Osservazione 'Earthrise'",
      poiType = "Punto Panoramico Siderale",
      posX = 26f,
      posZ = -28f,
      description = "Punto sopraelevato che offre una visuale perfetta della Terra sospesa e luminosa sopra la linea frastagliata dell'orizzonte lunare.",
      associatedPhotoId = "moon_earthrise",
    ),

    // Marte
    SurfacePoi(
      id = "mars_poi_perseverance",
      planetId = "mars",
      name = "Sito di Campionamento Rover Perseverance & Ingenuity",
      poiType = "Laboratorio Robotico Attivo",
      posX = 4f,
      posZ = 16f,
      description = "Il rover Perseverance della NASA intento a raccogliere campioni sigillati di roccia nel cratere Jezero, con l'elicottero Ingenuity pronto al decollo.",
      associatedPhotoId = "mars_perseverance_jezero",
    ),
    SurfacePoi(
      id = "mars_poi_delta_sediments",
      planetId = "mars",
      name = "Falesia del Delta Lacustre di Jezero",
      poiType = "Formazione Sedimentaria Fossile",
      posX = -30f,
      posZ = 42f,
      description = "Pareti rocciose stratificate modellate da un antico corso d'acqua marziano oltre 3 miliardi di anni fa, ricche di argille e minerali carbonatici.",
      associatedPhotoId = "mars_curiosity_selfie",
    ),
    SurfacePoi(
      id = "mars_poi_dune_field",
      planetId = "mars",
      name = "Dune Nere di Basalto Vulcanico",
      poiType = "Campo Eolico Attivo",
      posX = 28f,
      posZ = -24f,
      description = "Dune ondulate di sabbia scura ferrosa modellate dai venti marziani e solcate da dust devils che lasciano tracce intricate sul terreno.",
      associatedPhotoId = "mars_blue_sunset",
    ),

    // Venere
    SurfacePoi(
      id = "venus_poi_venera13",
      planetId = "venus",
      name = "Relitto Storico Sonda Venera 13",
      poiType = "Monumento Esplorativo Sovietico",
      posX = 0f,
      posZ = 14f,
      description = "Il lander Venera 13 atterrato nel 1982 sulla pianura vulcanica Phoebe Regio a 462°C e 92 atmosfere di anidride carbonica.",
      associatedPhotoId = "venus_venera13_surface",
    ),
    SurfacePoi(
      id = "venus_poi_lava_channel",
      planetId = "venus",
      name = "Canale di Scorrimento Lavico Basaltico",
      poiType = "Fiume di Lava Solidificata",
      posX = -25f,
      posZ = 30f,
      description = "Un solco lavico lungo centinaia di chilometri scavato da magma ad altissima fluidità che scorreva sotto la coltre opprimente di nubi sulfuree.",
      associatedPhotoId = "venus_magellan_radar",
    ),

    // Mercurio
    SurfacePoi(
      id = "mercury_poi_caloris_ridge",
      planetId = "mercury",
      name = "Creste Montuose di Caloris Montes",
      poiType = "Anello Montuoso da Cataclisma",
      posX = 2f,
      posZ = 18f,
      description = "Vette frastagliate alte fino a 3 km che delimitano il bacino d'impatto Caloris, illuminate da un Sole immenso tre volte più grande che sulla Terra.",
      associatedPhotoId = "mercury_messenger_caloris",
    ),
    SurfacePoi(
      id = "mercury_poi_scarp",
      planetId = "mercury",
      name = "Scarpata Tettonica 'Discovery Rupes'",
      poiType = "Faglia di Compressione Globale",
      posX = -28f,
      posZ = 36f,
      description = "Un gradino geologico impressionante alto oltre 1.500 metri causato dal restringimento del nucleo planetario durante il suo raffreddamento.",
      associatedPhotoId = "mercury_discovery_scarp",
    ),

    // Giove / Europa
    SurfacePoi(
      id = "jupiter_poi_europa_geyser",
      planetId = "jupiter",
      name = "Crio-Geyser di Vapore d'Acqua di Europa",
      poiType = "Eruzione Criogenica Attiva",
      posX = 0f,
      posZ = 20f,
      description = "Una colonna zampillante di vapore d'acqua salata e ghiaccio che fuoriesce dalle crepe della banchisa, con la colossale sfera di Giove che domina il cielo.",
      associatedPhotoId = "jupiter_europa_cracks",
    ),

    // Saturno / Titano
    SurfacePoi(
      id = "saturn_poi_huygens_landing",
      planetId = "saturn",
      name = "Sito di Atterraggio Sonda Huygens (ESA)",
      poiType = "Sito Storico Europeo",
      posX = 0f,
      posZ = 15f,
      description = "Il lander Huygens dell'Agenzia Spaziale Europea atterrato nel 2005 tra ciottoli di ghiaccio e colline di idrocarburi sotto una fitta nebbia arancione.",
      associatedPhotoId = "saturn_huygens_titan",
    ),

    // Terra
    SurfacePoi(
      id = "earth_poi_spaceport",
      planetId = "earth",
      name = "Piattaforma di Lancio Spazioporto AstroBloom",
      poiType = "Base Aerospaziale",
      posX = 0f,
      posZ = 12f,
      description = "La torre di rifornimento e la rampa di decollo della missione terrestre, circondata da aria limpida, vegetazione e l'oceano azzurro all'orizzonte.",
      associatedPhotoId = "earth_blue_marble",
    ),

    // Plutone
    SurfacePoi(
      id = "pluto_poi_nitrogen_glacier",
      planetId = "pluto",
      name = "Ghiacciaio di Azoto di Sputnik Planitia",
      poiType = "Cella Convettiva Glaciale",
      posX = 0f,
      posZ = 16f,
      description = "Un mare di ghiaccio di azoto liscio e brillante a -230°C con montagne di ghiaccio d'acqua all'orizzonte e il Sole simile a una stella accecante nel buio.",
      associatedPhotoId = "pluto_sputnik_planitia",
    ),
  )

  /**
   * Campioni geologici e minerali alieni raccoglibili.
   */
  val surfaceSamples: List<SurfaceSample> = listOf(
    // Luna
    SurfaceSample(
      id = "sample_moon_regolith",
      planetId = "moon",
      name = "Carotaggio di Regolite Lunare",
      mineralClass = "Polvere Silicatica Abrasiva",
      posX = -6f,
      posZ = 8f,
      spectrometryFormula = "SiO₂ • FeO • TiO₂ (Ilmenite)",
      scientificDiscovery = "Contiene tracce di Elio-3 intrappolato dal vento solare in miliardi di anni, potenziale combustibile per la futura fusione nucleare pulita.",
      rarity = "Comune",
      xpValue = 50,
      associatedPhotoId = "moon_apollo11_bootprint",
    ),
    SurfaceSample(
      id = "sample_moon_anorthosite",
      planetId = "moon",
      name = "Frammento di Anortosite della Genesis Rock",
      mineralClass = "Plagioclasio Magmatico Primordiale",
      posX = 14f,
      posZ = 22f,
      spectrometryFormula = "CaAl₂Si₂O₈",
      scientificDiscovery = "Cristallizzatasi oltre 4.1 miliardi di anni fa dall'antico oceano di magma lunare: conferma l'impatto gigante di Theia con la proto-Terra.",
      rarity = "Leggendario",
      xpValue = 150,
      associatedPhotoId = "moon_apollo11_eagle",
    ),

    // Marte
    SurfaceSample(
      id = "sample_mars_hematite",
      planetId = "mars",
      name = "Sferula di Ematite ('Mirtillo di Marte')",
      mineralClass = "Ossido di Ferro Idrotermale",
      posX = -8f,
      posZ = 12f,
      spectrometryFormula = "Fe₂O₃ (Concrezione sferica)",
      scientificDiscovery = "Queste piccole sfere si formano esclusivamente in presenza di falde acquifere sature di ferro: prova irrefutabile dell'antico passato umido di Marte.",
      rarity = "Raro",
      xpValue = 100,
      associatedPhotoId = "mars_perseverance_jezero",
    ),
    SurfaceSample(
      id = "sample_mars_clay",
      planetId = "mars",
      name = "Argilla Smectite del Delta Lacustre",
      mineralClass = "Fillosilicato Idro-Sedimentario",
      posX = 16f,
      posZ = 26f,
      spectrometryFormula = "(Na,Ca)₀.₃₃(Al,Mg)₂(Si₄O₁₀)(OH)₂•nH₂O",
      scientificDiscovery = "Le argille intrappolano e proteggono molecole organiche e possibili biosignature microscopiche dalla radiazione ultravioletta.",
      rarity = "Leggendario",
      xpValue = 180,
      associatedPhotoId = "mars_perseverance_jezero",
    ),
    SurfaceSample(
      id = "sample_mars_basalt",
      planetId = "mars",
      name = "Basalto Vulcanico Vescicolare",
      mineralClass = "Roccia Magmatica Effusiva",
      posX = -18f,
      posZ = 20f,
      spectrometryFormula = "Piroxeni • Olivina • Magnetite",
      scientificDiscovery = "Roccia vulcanica scura ricca di ferro e magnesio eruttata durante le colossali fasi effusive dei vulcani di Tharsis.",
      rarity = "Comune",
      xpValue = 60,
      associatedPhotoId = "mars_curiosity_selfie",
    ),

    // Venere
    SurfaceSample(
      id = "sample_venus_sulfur_basalt",
      planetId = "venus",
      name = "Basalto Alcalino Fratturato dal Calore",
      mineralClass = "Silicato Vulcanico ad Alta Pressione",
      posX = -10f,
      posZ = 16f,
      spectrometryFormula = "SiO₂ • Al₂O₃ • CaO • FeS",
      scientificDiscovery = "Sottoposta a 92 bar e 462°C, la roccia mostra ricristallizzazioni con inclusioni di pirite e anidride solforosa supercritica.",
      rarity = "Leggendario",
      xpValue = 200,
      associatedPhotoId = "venus_venera13_surface",
    ),

    // Mercurio
    SurfaceSample(
      id = "sample_mercury_graphite",
      planetId = "mercury",
      name = "Crosta Scuro-Carboniosa di Grafite",
      mineralClass = "Carbonio Elementare Cromatico",
      posX = 12f,
      posZ = 18f,
      spectrometryFormula = "C (Allotropo planare)",
      scientificDiscovery = "I depositi scuri di Mercurio sono composti da grafite antica risalita dal galleggiamento della crosta di carbonio primordiale.",
      rarity = "Raro",
      xpValue = 120,
      associatedPhotoId = "mercury_messenger_caloris",
    ),

    // Giove / Europa
    SurfaceSample(
      id = "sample_europa_salts",
      planetId = "jupiter",
      name = "Sale Marino di Solfato di Magnesio di Europa",
      mineralClass = "Sale Idrotermale Oceanico",
      posX = -12f,
      posZ = 14f,
      spectrometryFormula = "MgSO₄ • 7H₂O (Epsomite)",
      scientificDiscovery = "Espulso dai geyser sottomarini e depositato sulle pareti delle fratture glaciali: riflette la composizione salina dell'oceano interno.",
      rarity = "Leggendario",
      xpValue = 200,
      associatedPhotoId = "jupiter_europa_cracks",
    ),

    // Saturno / Titano
    SurfaceSample(
      id = "sample_titan_ice_pebble",
      planetId = "saturn",
      name = "Ciottolo Levigato di Ghiaccio d'Acqua Criogenico",
      mineralClass = "Ghiaccio Silicato a -180°C",
      posX = 10f,
      posZ = 12f,
      spectrometryFormula = "H₂O amorfo + Toline organiche",
      scientificDiscovery = "A -180°C il ghiaccio d'acqua si comporta come roccia terrestre: è stato arrotondato dal fluire continuo di fiumi di metano liquido.",
      rarity = "Raro",
      xpValue = 140,
      associatedPhotoId = "saturn_huygens_titan",
    ),

    // Terra
    SurfaceSample(
      id = "sample_earth_granite",
      planetId = "earth",
      name = "Campione di Granito e Quarzo Continentale",
      mineralClass = "Roccia Intrusiva Continentale",
      posX = 8f,
      posZ = 14f,
      spectrometryFormula = "SiO₂ • KAlSi₃O₈",
      scientificDiscovery = "Il granito è la firma della tettonica a zolle e dell'abbondanza di acqua liquida: unico nel Sistema Solare per diffusione continentale.",
      rarity = "Comune",
      xpValue = 40,
      associatedPhotoId = "earth_blue_marble",
    ),

    // Plutone
    SurfaceSample(
      id = "sample_pluto_nitrogen_ice",
      planetId = "pluto",
      name = "Cristallo di Ghiaccio d'Azoto Esagonale",
      mineralClass = "Gas Congelato Criogenico (-230°C)",
      posX = -8f,
      posZ = 14f,
      spectrometryFormula = "N₂ (Solido alfa/beta)",
      scientificDiscovery = "Ghiaccio plastico che fluisce lentamente come i ghiacciai terrestri, alimentando celle convettive termiche nel cuore di Plutone.",
      rarity = "Leggendario",
      xpValue = 220,
      associatedPhotoId = "pluto_sputnik_planitia",
    ),
  )

  /**
   * Restituisce la configurazione ambientale di un corpo celeste.
   */
  fun getEnvironmentConfig(planetId: String): PlanetEnvironmentConfig {
    return when (planetId.lowercase()) {
      "moon" -> PlanetEnvironmentConfig(
        planetId = "moon",
        planetName = "Luna",
        gravityMs2 = 1.62f,
        gravityRatio = 0.165f,
        avgTempCelsius = -20,
        pressureAtm = 0.0f,
        atmosphereComposition = "Vuoto cosmico (Tracce di He, Ne, Ar)",
        skyTopColor = Color(0xFF010206),
        skyHorizonColor = Color(0xFF030712),
        terrainPrimaryColor = Color(0xFF5A6275),
        terrainSecondaryColor = Color(0xFF383E4C),
        rockColor = Color(0xFF7F889B),
        sunScale = 1.0f,
        dustDensity = 0.05f,
        visibilityMeters = 500f,
        landscapeFeature = "Crateri a raggiera e mari di basalto grigio",
        suitWarningText = "VUOTO ASSOLUTO • Radiazione solare non schermata",
      )
      "mars" -> PlanetEnvironmentConfig(
        planetId = "mars",
        planetName = "Marte",
        gravityMs2 = 3.72f,
        gravityRatio = 0.38f,
        avgTempCelsius = -63,
        pressureAtm = 0.006f,
        atmosphereComposition = "95.3% CO₂, 2.6% N₂, 1.9% Ar",
        skyTopColor = Color(0xFF542416),
        skyHorizonColor = Color(0xFFC26E4D),
        terrainPrimaryColor = Color(0xFFA74323),
        terrainSecondaryColor = Color(0xFF6B250F),
        rockColor = Color(0xFF421508),
        sunScale = 0.72f,
        dustDensity = 0.45f,
        visibilityMeters = 380f,
        landscapeFeature = "Pianure di ossido di ferro, dune e canyon di Jezero",
        suitWarningText = "ATMOSFERA IRRESPIRABILE • Freddo estremo e pulviscolo abrasivo",
      )
      "venus" -> PlanetEnvironmentConfig(
        planetId = "venus",
        planetName = "Venere",
        gravityMs2 = 8.87f,
        gravityRatio = 0.90f,
        avgTempCelsius = 465,
        pressureAtm = 92.0f,
        atmosphereComposition = "96.5% CO₂, 3.5% N₂, Nubi di H₂SO₄",
        skyTopColor = Color(0xFF422802),
        skyHorizonColor = Color(0xFFB87814),
        terrainPrimaryColor = Color(0xFF4A3416),
        terrainSecondaryColor = Color(0xFF2E1F0A),
        rockColor = Color(0xFF1E1508),
        sunScale = 1.35f,
        dustDensity = 0.60f,
        visibilityMeters = 240f,
        landscapeFeature = "Pianura basaltica scura fratturata sotto coltre sulfurea",
        suitWarningText = "⚠️ ALLARME TERMICO CRITICO (+465°C) • Pressione 92 ATM",
      )
      "mercury" -> PlanetEnvironmentConfig(
        planetId = "mercury",
        planetName = "Mercurio",
        gravityMs2 = 3.70f,
        gravityRatio = 0.38f,
        avgTempCelsius = 167,
        pressureAtm = 0.0f,
        atmosphereComposition = "Esosfera effimera (Ossigeno, Sodio, Idrogeno)",
        skyTopColor = Color(0xFF000000),
        skyHorizonColor = Color(0xFF04060E),
        terrainPrimaryColor = Color(0xFF4E4B49),
        terrainSecondaryColor = Color(0xFF2E2D2C),
        rockColor = Color(0xFF6E6B68),
        sunScale = 2.80f,
        dustDensity = 0.08f,
        visibilityMeters = 600f,
        landscapeFeature = "Scarpate compressive e crateri giganti di Caloris",
        suitWarningText = "ESPOSIZIONE SOLARE ESTREMA • Escursione termica 600°C",
      )
      "jupiter" -> PlanetEnvironmentConfig(
        planetId = "jupiter",
        planetName = "Giove (Banchisa di Europa)",
        gravityMs2 = 1.31f,
        gravityRatio = 0.134f,
        avgTempCelsius = -160,
        pressureAtm = 0.0f,
        atmosphereComposition = "Tracce di Ossigeno molecolare (O₂)",
        skyTopColor = Color(0xFF060B1E),
        skyHorizonColor = Color(0xFF162544),
        terrainPrimaryColor = Color(0xFF7FA1C3),
        terrainSecondaryColor = Color(0xFF43607E),
        rockColor = Color(0xFFB66649),
        sunScale = 0.22f,
        dustDensity = 0.15f,
        visibilityMeters = 420f,
        landscapeFeature = "Banchisa di ghiaccio azzurro solcata da lineae rossastre e geyser",
        suitWarningText = "CINTURE DI RADIAZIONE GIOVIANE • Schermatura EVA attiva",
      )
      "saturn" -> PlanetEnvironmentConfig(
        planetId = "saturn",
        planetName = "Saturno (Dune di Titano)",
        gravityMs2 = 1.35f,
        gravityRatio = 0.138f,
        avgTempCelsius = -179,
        pressureAtm = 1.45f,
        atmosphereComposition = "95% Azoto, 5% Metano (CH₄)",
        skyTopColor = Color(0xFF422806),
        skyHorizonColor = Color(0xFFA46A1A),
        terrainPrimaryColor = Color(0xFF5E4B28),
        terrainSecondaryColor = Color(0xFF3B2E15),
        rockColor = Color(0xFF86929F),
        sunScale = 0.12f,
        dustDensity = 0.50f,
        visibilityMeters = 290f,
        landscapeFeature = "Foschia arancio densa, fiumi e ciottoli di ghiaccio duro",
        suitWarningText = "CRIO-TEMPERATURE METANICHE (-179°C) • Pressione 1.5 bar",
      )
      "uranus" -> PlanetEnvironmentConfig(
        planetId = "uranus",
        planetName = "Urano (Lidi di Ariel)",
        gravityMs2 = 0.27f,
        gravityRatio = 0.03f,
        avgTempCelsius = -213,
        pressureAtm = 0.0f,
        atmosphereComposition = "Tracce di CO₂ e H₂O ghiacciata",
        skyTopColor = Color(0xFF031622),
        skyHorizonColor = Color(0xFF0E4356),
        terrainPrimaryColor = Color(0xFF436B77),
        terrainSecondaryColor = Color(0xFF233B42),
        rockColor = Color(0xFF6B9EA9),
        sunScale = 0.06f,
        dustDensity = 0.05f,
        visibilityMeters = 450f,
        landscapeFeature = "Canyon glaciali profondi e faglie crio-vulcaniche",
        suitWarningText = "MICRO-GRAVITÀ ESTREMA • Tute ad ancoraggio mag-boots",
      )
      "neptune" -> PlanetEnvironmentConfig(
        planetId = "neptune",
        planetName = "Nettuno (Geyser di Tritone)",
        gravityMs2 = 0.78f,
        gravityRatio = 0.08f,
        avgTempCelsius = -235,
        pressureAtm = 0.00001f,
        atmosphereComposition = "Azoto rarefatto e Metano congelato",
        skyTopColor = Color(0xFF030D29),
        skyHorizonColor = Color(0xFF0D2566),
        terrainPrimaryColor = Color(0xFF38558A),
        terrainSecondaryColor = Color(0xFF1B2E52),
        rockColor = Color(0xFF5879BA),
        sunScale = 0.04f,
        dustDensity = 0.08f,
        visibilityMeters = 480f,
        landscapeFeature = "Crosta di azoto rosa e geyser di azoto scuro alti 8 km",
        suitWarningText = "ZERO ASSOLUTO QUASI RAGGIUNTO (-235°C)",
      )
      "pluto" -> PlanetEnvironmentConfig(
        planetId = "pluto",
        planetName = "Plutone",
        gravityMs2 = 0.62f,
        gravityRatio = 0.063f,
        avgTempCelsius = -229,
        pressureAtm = 0.00001f,
        atmosphereComposition = "N₂, CH₄, CO allo stato solido e sublimato",
        skyTopColor = Color(0xFF01040A),
        skyHorizonColor = Color(0xFF081427),
        terrainPrimaryColor = Color(0xFF5D544C),
        terrainSecondaryColor = Color(0xFF3A342E),
        rockColor = Color(0xFF91867A),
        sunScale = 0.03f,
        dustDensity = 0.05f,
        visibilityMeters = 500f,
        landscapeFeature = "Ghiacciai di azoto convettivi e montagne di ghiaccio d'acqua",
        suitWarningText = "CRIO-DESERTO DELLA FASCIA DI KUIPER",
      )
      "sun" -> PlanetEnvironmentConfig(
        planetId = "sun",
        planetName = "Sole (Stazione Orbitale Helios)",
        gravityMs2 = 274.0f,
        gravityRatio = 27.9f,
        avgTempCelsius = 5500,
        pressureAtm = 1000.0f,
        atmosphereComposition = "73.4% Idrogeno, 25% Elio in plasma ionizzato",
        skyTopColor = Color(0xFF5A1E04),
        skyHorizonColor = Color(0xFFF59E0B),
        terrainPrimaryColor = Color(0xFF78350F),
        terrainSecondaryColor = Color(0xFF451A03),
        rockColor = Color(0xFFFBBF24),
        sunScale = 5.0f,
        dustDensity = 0.90f,
        visibilityMeters = 150f,
        landscapeFeature = "Scudi termici deflettori e filamenti magnetici coronali",
        suitWarningText = "🔥 SCUDO MAGNETICO AL 100% • Pericolo tempesta di plasma",
      )
      else -> PlanetEnvironmentConfig(
        planetId = "earth",
        planetName = "Terra",
        gravityMs2 = 9.81f,
        gravityRatio = 1.0f,
        avgTempCelsius = 15,
        pressureAtm = 1.0f,
        atmosphereComposition = "78% Azoto, 21% Ossigeno, 0.9% Argon",
        skyTopColor = Color(0xFF0284C7),
        skyHorizonColor = Color(0xFF7DD3FC),
        terrainPrimaryColor = Color(0xFF15803D),
        terrainSecondaryColor = Color(0xFF166534),
        rockColor = Color(0xFF52525B),
        sunScale = 1.0f,
        dustDensity = 0.02f,
        visibilityMeters = 800f,
        landscapeFeature = "Atmosfera blu, colline verdi e oceano all'orizzonte",
        suitWarningText = null,
      )
    }
  }

  /**
   * Restituisce le foto documentate ufficialmente per un pianeta.
   */
  fun getPhotosForPlanet(planetId: String): List<OfficialPhotoRecord> {
    val norm = planetId.lowercase()
    val matches = officialPhotos.filter { it.planetId.equals(norm, ignoreCase = true) }
    return if (matches.isNotEmpty()) matches else officialPhotos.filter { it.planetId == "moon" }
  }

  /**
   * Restituisce i punti di interesse per un pianeta.
   */
  fun getPoisForPlanet(planetId: String): List<SurfacePoi> {
    val norm = planetId.lowercase()
    val matches = surfacePois.filter { it.planetId.equals(norm, ignoreCase = true) }
    return if (matches.isNotEmpty()) matches else surfacePois.filter { it.planetId == "moon" }
  }

  /**
   * Restituisce i campioni geologici per un pianeta.
   */
  fun getSamplesForPlanet(planetId: String): List<SurfaceSample> {
    val norm = planetId.lowercase()
    val matches = surfaceSamples.filter { it.planetId.equals(norm, ignoreCase = true) }
    return if (matches.isNotEmpty()) matches else surfaceSamples.filter { it.planetId == "moon" }
  }
}
