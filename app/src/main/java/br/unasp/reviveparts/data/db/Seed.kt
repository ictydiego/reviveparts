package br.unasp.reviveparts.data.db

import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.domain.model.SeedIds

object Seed {
    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.productDao().count() > 0) return
        val u = db.userDao()
        u.insert(UserEntity(id = 0, name = "Dono Reviveparts", email = "dono@reviveparts.com", password = "dono123", role = Role.OWNER))

        val p = db.productDao()
        p.insert(ProductEntity(
            id = SeedIds.MANIVELA_VW,
            name = "Manivela de Vidro VW Fusca",
            description = "Manivela de janela em ABS reforçado, compatível com VW Fusca/Kombi clássicos.",
            photoPath = "drawable://manivela_vw",
            model3dAsset = "models/manivela_vw.glb",
            priceCents = 4990,
            prototypeHours = 6,
            stockQty = 12,
            isReady = true
        ))
        p.insert(ProductEntity(0, "Maçaneta Externa Fusca", "Maçaneta externa replicada em ABS preto.", "drawable://placeholder_part", "models/manivela_vw.glb", 7990, 8, 4, true))
        p.insert(ProductEntity(0, "Botão de Ar Kombi", "Botão do painel da Kombi.", "drawable://placeholder_part", "models/manivela_vw.glb", 2490, 3, 25, true))
        p.insert(ProductEntity(0, "Puxador de Porta", "Puxador interno.", "drawable://placeholder_part", "models/manivela_vw.glb", 3490, 5, 0, true))
    }
}
