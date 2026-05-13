package br.unasp.reviveparts.data.db

import br.unasp.reviveparts.data.db.entities.ProductEntity
import br.unasp.reviveparts.data.db.entities.UserEntity
import br.unasp.reviveparts.domain.model.Role
import br.unasp.reviveparts.domain.model.SeedIds

object Seed {
    suspend fun seedIfEmpty(db: AppDatabase) {
        val p = db.productDao()
        if (p.count() == 0) {
            val u = db.userDao()
            u.insert(
                UserEntity(
                    id = 0,
                    name = "Dono Reviveparts",
                    email = "dono@reviveparts.com",
                    password = "dono123",
                    role = Role.OWNER
                )
            )
        }

        insertIfMissing(
            p,
            ProductEntity(
                id = SeedIds.MANIVELA_VW,
                name = "Manivela do vidro",
                description = "Manivela de janela em ABS reforcado, compativel com VW Fusca/Kombi classicos.",
                photoPath = "drawable://manivela_vidro",
                model3dAsset = "models/manivela_vidro.stl",
                priceCents = 3299,
                prototypeHours = 6,
                stockQty = 12,
                isReady = true,
                carBrand = "Volkswagen"
            )
        )
        insertIfMissing(p, ProductEntity(0, "Macaneta externa", "Macaneta externa replicada em ABS preto.", "drawable://macaneta", "models/macaneta.stl", 7990, 8, 4, true, "Volkswagen"))
        insertIfMissing(p, ProductEntity(0, "Botao de ar Kombi", "Botao do painel da Kombi.", "drawable://botao", "models/botao_kombi.3mf", 2490, 3, 25, true, "Volkswagen"))
        insertIfMissing(p, ProductEntity(0, "Console central", "Console central para Fiat Uno 1998-2006.", "drawable://console_central", "models/manivela_vidro.stl", 10000, 10, 6, true, "Fiat"))
        insertIfMissing(p, ProductEntity(0, "Luz de teto", "Luz de teto retro.", "drawable://luz_de_teto", "models/manivela_vidro.stl", 9000, 5, 8, true, "Chevrolet"))
        insertIfMissing(p, ProductEntity(0, "Porta copos", "Porta copos para console.", "drawable://porta_copos", "models/manivela_vidro.stl", 2500, 3, 20, true, "Universal"))
        insertIfMissing(p, ProductEntity(0, "Subaru WRX washer cap", "Tampa do radiador para Subaru WRX, pronta para impressao 3D.", "drawable://tamparadiador", "models/wrx_washer_cap.STL", 4590, 4, 10, true, "Subaru"))
        insertIfMissing(p, ProductEntity(0, "Retrovisor Fiat mirror green", "Espelho retrovisor esquerdo da Fiat baseado no modelo mirror_green.", "drawable://retrovisor_fiat", "models/mirror_green_left.stl", 12990, 12, 3, true, "Fiat"))
    }

    private suspend fun insertIfMissing(dao: br.unasp.reviveparts.data.db.dao.ProductDao, product: ProductEntity) {
        if (dao.findByName(product.name) == null) dao.insert(product)
    }
}
