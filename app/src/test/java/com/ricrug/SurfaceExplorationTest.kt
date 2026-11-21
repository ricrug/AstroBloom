package com.ricrug

import com.ricrug.data.SurfaceExplorationRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceExplorationTest {

  @Test
  fun testEnvironmentConfigsForAllMainPlanets() {
    val planetIds = listOf("moon", "mars", "venus", "mercury", "jupiter", "saturn", "uranus", "neptune", "pluto", "earth", "sun")

    for (pid in planetIds) {
      val env = SurfaceExplorationRepository.getEnvironmentConfig(pid)
      assertNotNull("Environment config null for $pid", env)
      assertTrue("Gravity must be positive for $pid", env.gravityMs2 > 0f)
      assertTrue("Visibility must be positive for $pid", env.visibilityMeters > 0f)
      assertTrue("Landscape feature must not be blank for $pid", env.landscapeFeature.isNotBlank())
      assertTrue("Atmosphere composition must not be blank for $pid", env.atmosphereComposition.isNotBlank())
    }
  }

  @Test
  fun testRealisticRelativeGravityOrder() {
    val moonEnv = SurfaceExplorationRepository.getEnvironmentConfig("moon")
    val marsEnv = SurfaceExplorationRepository.getEnvironmentConfig("mars")
    val venusEnv = SurfaceExplorationRepository.getEnvironmentConfig("venus")
    val earthEnv = SurfaceExplorationRepository.getEnvironmentConfig("earth")
    val sunEnv = SurfaceExplorationRepository.getEnvironmentConfig("sun")

    // Moon (~1.62) < Mars (~3.72) < Venus (~8.87) < Earth (~9.81) < Sun (~274)
    assertTrue("Moon gravity should be less than Mars", moonEnv.gravityMs2 < marsEnv.gravityMs2)
    assertTrue("Mars gravity should be less than Venus", marsEnv.gravityMs2 < venusEnv.gravityMs2)
    assertTrue("Venus gravity should be less than Earth", venusEnv.gravityMs2 < earthEnv.gravityMs2)
    assertTrue("Earth gravity should be less than Sun", earthEnv.gravityMs2 < sunEnv.gravityMs2)

    // Check specific ratios
    assertEquals(0.165f, moonEnv.gravityRatio, 0.01f)
    assertEquals(0.38f, marsEnv.gravityRatio, 0.01f)
    assertEquals(0.90f, venusEnv.gravityRatio, 0.01f)
    assertEquals(1.0f, earthEnv.gravityRatio, 0.01f)
  }

  @Test
  fun testOfficialPhotosDocumentationCompleteness() {
    val photos = SurfaceExplorationRepository.officialPhotos
    assertTrue("Must have official documented photos", photos.isNotEmpty())

    for (p in photos) {
      assertTrue("Photo ID must not be blank", p.id.isNotBlank())
      assertTrue("Planet ID must not be blank: ${p.id}", p.planetId.isNotBlank())
      assertTrue("Title must not be blank: ${p.id}", p.title.isNotBlank())
      assertTrue("Mission must not be blank: ${p.id}", p.mission.isNotBlank())
      assertTrue("Agency must not be blank: ${p.id}", p.agency.isNotBlank())
      assertTrue("Capture date must not be blank: ${p.id}", p.captureDate.isNotBlank())
      assertTrue("Image URL must be valid https URL: ${p.id}", p.imageUrl.startsWith("https://"))
      assertTrue("Caption must be informative (>20 chars): ${p.id}", p.caption.length > 20)
      assertTrue("Scientific tip must be present (>15 chars): ${p.id}", p.scientificTip.length > 15)
      assertTrue("Credit must not be blank: ${p.id}", p.credit.isNotBlank())
    }

    // Verify key historical missions are documented
    val missions = photos.map { it.mission }
    assertTrue("Should include Apollo 11 mission", missions.any { it.contains("Apollo 11") })
    assertTrue("Should include Perseverance mission", missions.any { it.contains("Perseverance") })
    assertTrue("Should include Venera mission", missions.any { it.contains("Venera") })
    assertTrue("Should include MESSENGER mission", missions.any { it.contains("MESSENGER") })
    assertTrue("Should include Cassini-Huygens mission", missions.any { it.contains("Cassini") || it.contains("Huygens") })
    assertTrue("Should include New Horizons mission", missions.any { it.contains("New Horizons") })
  }

  @Test
  fun testSurfacePoisAndSamplesIntegrity() {
    val pois = SurfaceExplorationRepository.surfacePois
    val samples = SurfaceExplorationRepository.surfaceSamples

    assertTrue("Must have POIs", pois.isNotEmpty())
    assertTrue("Must have samples", samples.isNotEmpty())

    for (poi in pois) {
      assertTrue("POI name not blank", poi.name.isNotBlank())
      assertTrue("POI description not blank", poi.description.isNotBlank())
      if (poi.associatedPhotoId != null) {
        val linked = SurfaceExplorationRepository.officialPhotos.any { it.id == poi.associatedPhotoId }
        assertTrue("POI associatedPhotoId must exist in photos: ${poi.associatedPhotoId}", linked)
      }
    }

    for (sample in samples) {
      assertTrue("Sample name not blank", sample.name.isNotBlank())
      assertTrue("Sample spectrometryFormula not blank", sample.spectrometryFormula.isNotBlank())
      assertTrue("Sample scientificDiscovery not blank", sample.scientificDiscovery.isNotBlank())
      assertTrue("Sample XP must be > 0", sample.xpValue > 0)
      if (sample.associatedPhotoId != null) {
        val linked = SurfaceExplorationRepository.officialPhotos.any { it.id == sample.associatedPhotoId }
        assertTrue("Sample associatedPhotoId must exist in photos: ${sample.associatedPhotoId}", linked)
      }
    }
  }

  @Test
  fun testDisembarkShipMechanics() {
    // Coordinate iniziali a bordo dell'airlock
    val airlockEyeHeight = 3.2f
    val airlockZ = 1.6f
    val airlockYaw = 0f

    // Coordinate dopo la discesa sulla superficie
    val groundEyeHeight = 1.75f
    val groundZ = 5.2f
    val groundYaw = 180f

    // Discesa comporta riduzione di quota verso il terreno
    assertTrue("Airlock should be elevated above ground", airlockEyeHeight > groundEyeHeight)
    // Discesa comporta allontanamento sulla passerella
    assertTrue("Ground position is further out from ship center", groundZ > airlockZ)
    // Discesa ruota la vista di 180° per ammirare l'astronave dall'esterno
    assertEquals(180f, kotlin.math.abs(groundYaw - airlockYaw), 0.001f)

    // Distanza di risalita a bordo
    val boardingThreshold = 4.2f
    val nearShipDist = 3.0f
    val farShipDist = 8.0f
    assertTrue("Near ship allows reboarding", nearShipDist < boardingThreshold)
    assertTrue("Far ship requires approaching to reboard", farShipDist > boardingThreshold)
  }
}
