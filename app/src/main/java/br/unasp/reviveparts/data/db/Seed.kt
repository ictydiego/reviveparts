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
            name = "Manivela do vidro",
            description = "Manivela de janela em ABS reforçado, compatível com VW Fusca/Kombi clássicos.",
            photoPath = "drawable://manivela_vidro",
            model3dAsset = "models/manivela_vidro.stl",
            priceCents = 3299,
            prototypeHours = 6,
            stockQty = 12,
            isReady = true
        ))
        p.insert(ProductEntity(0, "Maçaneta externa", "Maçaneta externa replicada em ABS preto.", "drawable://macaneta", "models/macaneta.stl", 7990, 8, 4, true))
        p.insert(ProductEntity(0, "Botão de ar Kombi", "Botão do painel da Kombi.", "drawable://botao", "models/botao_kombi.3mf", 2490, 3, 25, true))
        p.insert(ProductEntity(0, "Console central", "Console central para Fiat Uno 1998-2006.", "drawable://console_central", "models/manivela_vidro.stl", 10000, 10, 6, true))
        p.insert(ProductEntity(0, "Luz de teto", "Luz de teto retro.", "drawable://luz_de_teto", "models/manivela_vidro.stl", 9000, 5, 8, true))
        p.insert(ProductEntity(0, "Porta copos", "Porta copos para console.", "drawable://porta_copos", "models/manivela_vidro.stl", 2500, 3, 20, true))
    }
}
