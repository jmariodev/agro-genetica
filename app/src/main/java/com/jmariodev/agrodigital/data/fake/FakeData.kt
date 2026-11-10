package com.jmariodev.agrodigital.data.fake

import com.jmariodev.agrodigital.data.model.Order
import com.jmariodev.agrodigital.data.model.OrderItem
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.Product
import com.jmariodev.agrodigital.data.model.ProductStatus
import com.jmariodev.agrodigital.data.model.ProductType
import com.jmariodev.agrodigital.data.model.RankingItem
import com.jmariodev.agrodigital.data.model.ReportPeriod
import com.jmariodev.agrodigital.data.model.SalesPoint
import com.jmariodev.agrodigital.data.model.User
import com.jmariodev.agrodigital.data.model.UserRole
import com.jmariodev.agrodigital.data.model.UserStatus

/**
 * Datos de ejemplo tomados de los mockups. Solo los usan las implementaciones Fake*Repository;
 * ninguna pantalla los importa directamente.
 */
object FakeData {

    const val DEMO_PASSWORD = "Agro2026*"
    const val SHIPPING_FEE = 25_000L

    const val BUYER_ID = "u_buyer"
    const val SELLER_ID = "u_seller"
    const val ADMIN_ID = "u_admin"

    val users = listOf(
        User(BUYER_ID, "María González", "comprador@agro.co", "+57 310 555 1020", UserRole.COMPRADOR, UserStatus.ACTIVO, "Finca La Esperanza", "Vereda El Progreso, Km 15 · Montería, Córdoba", verified = true, createdLabel = "Hace 6 meses"),
        User(SELLER_ID, "Carlos Mendoza", "vendedor@agro.co", "+57 300 123 4567", UserRole.VENDEDOR, UserStatus.ACTIVO, "Hacienda El Progreso", "Vereda El Progreso, Km 15 · Montería, Córdoba", verified = true, createdLabel = "Hace 1 año"),
        User(ADMIN_ID, "Administrador", "admin@agro.co", "+57 601 555 0000", UserRole.ADMIN, UserStatus.ACTIVO, "AgroGenética Digital", "Bogotá D.C.", verified = true, createdLabel = "Hace 1 año"),
        User("u_sol", "Hacienda Sol", "contacto@haciendasol.co", "+57 315 444 2211", UserRole.VENDEDOR, UserStatus.ACTIVO, "Hacienda Sol", "Planeta Rica, Córdoba", verified = true, createdLabel = "Hace 8 meses"),
        User("u_pedro", "Pedro Ramírez", "pedro.ramirez@correo.co", "+57 312 765 4321", UserRole.COMPRADOR, UserStatus.INACTIVO, "Finca Los Almendros", "Sahagún, Córdoba", createdLabel = "Hace 3 meses"),
        User("u_roble", "Finca El Roble", "elroble@correo.co", "+57 318 222 3344", UserRole.COMPRADOR, UserStatus.ACTIVO, "Finca El Roble", "Cereté, Córdoba", createdLabel = "Hace 2 meses"),
        User("u_aurora", "Finca La Aurora", "laaurora@correo.co", "+57 311 909 8877", UserRole.VENDEDOR, UserStatus.PENDIENTE, "Finca La Aurora", "Lorica, Córdoba", createdLabel = "Hace 2 h"),
        User("u_elite", "Ganadería Elite S.A.", "info@ganaderiaelite.co", "+57 604 333 1212", UserRole.VENDEDOR, UserStatus.PENDIENTE, "Ganadería Elite S.A.", "Rionegro, Antioquia", createdLabel = "Ayer")
    )

    val products = listOf(
        Product("p_brahman", "Brahman Elite 2024", "Brahman", ProductType.PAJILLA, 450_000, 180, 25, 4.8f, SELLER_ID, "Hacienda El Progreso", "Montería, Córdoba", "ICA-2024-0181",
            premium = true, description = "Pajilla de toro Brahman de élite con excelente conformación, alta fertilidad y adaptación al trópico bajo.",
            genotype = "AA/BB", concentration = "20M/ml", dose = "0.5 ml", father = "Rex Prime", mother = "Luna Star"),
        Product("p_angus", "Angus Premium", "Angus", ProductType.PAJILLA, 520_000, 165, 18, 4.9f, SELLER_ID, "Hacienda El Progreso", "Montería, Córdoba", "ICA-2024-0192",
            premium = true, description = "Genética Angus negra para cruces de carne con facilidad de parto y marmoleo superior.",
            genotype = "AA/AB", concentration = "25M/ml", dose = "0.5 ml", father = "Black Titan", mother = "Duquesa", darkCoat = true),
        Product("p_simmental", "Simmental Pro", "Simmental", ProductType.PAJILLA, 380_000, 150, 30, 4.7f, "u_sol", "Hacienda Sol", "Planeta Rica, Córdoba", "ICA-2024-0207",
            description = "Doble propósito con gran ganancia de peso y habilidad materna.", genotype = "AB/BB", concentration = "18M/ml", dose = "0.5 ml", father = "Alpen King", mother = "Gretel"),
        Product("p_holstein", "Holstein Gen+", "Holstein", ProductType.PAJILLA, 400_000, 145, 22, 4.6f, "u_sol", "Hacienda Sol", "Planeta Rica, Córdoba", "ICA-2024-0215",
            description = "Línea lechera de alta producción y longevidad productiva.", genotype = "BB/BB", concentration = "22M/ml", dose = "0.25 ml", father = "Milk Master", mother = "Blanca Nieves"),
        Product("p_embrion", "Angus Elite 2024", "Angus", ProductType.EMBRION, 750_000, 172, 6, 4.8f, SELLER_ID, "Hacienda El Progreso", "Montería, Córdoba", "ICA-2024-0230",
            premium = true, description = "Embrión congelado grado 1 de padres Angus élite, listo para transferencia.",
            genotype = "AA/AA", concentration = "Grado 1", dose = "Mórula", father = "Black Titan", mother = "Reina Negra", darkCoat = true),
        Product("p_rex", "Toro Brahman Rex V", "Brahman", ProductType.EJEMPLAR, 0, 195, 1, 4.9f, SELLER_ID, "Hacienda El Progreso", "Montería, Córdoba", "ASO-2024-XXX",
            premium = true, description = "Ejemplar reproductor de élite, campeón regional, con registro genealógico completo.",
            father = "Rex Prime", mother = "Imperial", ageYears = 4, weightKg = 850),
        Product("p_gyr", "Vaca Gyr Lechera Dalia", "Gyr", ProductType.EJEMPLAR, 0, 120, 1, 4.5f, "u_sol", "Hacienda Sol", "Planeta Rica, Córdoba", "ASO-2023-112",
            description = "Hembra Gyr de alta producción lechera, tercera lactancia.", father = "Sultán", mother = "Dalia", ageYears = 6, weightKg = 520),
        Product("p_maxpro", "Brahman Max Pro", "Brahman", ProductType.PAJILLA, 480_000, 170, 12, 4.4f, "u_aurora", "Finca La Aurora", "Lorica, Córdoba", "ICA-2024-0301",
            status = ProductStatus.PENDIENTE, genotype = "AA/BB", concentration = "20M/ml", dose = "0.5 ml", father = "Max", mother = "Perla", createdLabel = "Hoy"),
        Product("p_holstein_p", "Holstein Premium", "Holstein", ProductType.PAJILLA, 420_000, 140, 15, 4.3f, "u_elite", "Ganadería Elite", "Rionegro, Antioquia", "ICA-2024-0310",
            status = ProductStatus.PENDIENTE, genotype = "BB/BB", concentration = "21M/ml", dose = "0.25 ml", father = "Lucero", mother = "Nieve", createdLabel = "Ayer"),
        Product("p_xyz", "Brahman XYZ", "Brahman", ProductType.PAJILLA, 350_000, 110, 40, 3.9f, "u_pedro", "Usuario123", "Sahagún, Córdoba", "ICA-0000-0000",
            status = ProductStatus.REPORTADO, reportReason = "Certificado inválido", father = "Desconocido", mother = "Desconocida", createdLabel = "Hace 3 días", hasCertificate = false)
    )

    private fun item(p: Product, qty: Int) = OrderItem(p.id, p.name, p.breed, p.type, qty, p.price, p.darkCoat)
    private fun p(id: String) = products.first { it.id == id }

    private const val ADDRESS = "Finca La Esperanza · Vereda El Progreso, Km 15 · Montería, Córdoba"

    val orders = listOf(
        Order("o1", "ORD-2024-001", "15 abr 2026", OrderStatus.ENTREGADO, listOf(item(p("p_brahman"), 2), item(p("p_angus"), 1)), SHIPPING_FEE, BUYER_ID, "María González", SELLER_ID, "Hacienda El Progreso", ADDRESS, "MercadoPago"),
        Order("o2", "ORD-2024-002", "10 abr 2026", OrderStatus.ENVIADO, listOf(item(p("p_angus"), 1)), 0, BUYER_ID, "María González", SELLER_ID, "Hacienda El Progreso", ADDRESS, "PSE"),
        Order("o3", "ORD-2024-003", "05 abr 2026", OrderStatus.PAGADO, listOf(item(p("p_brahman"), 1), item(p("p_simmental"), 1)), 0, BUYER_ID, "María González", "u_sol", "Hacienda Sol", ADDRESS, "ePayco"),
        Order("o4", "ORD-2024-004", "01 abr 2026", OrderStatus.ENTREGADO, listOf(item(p("p_embrion"), 2), item(p("p_brahman"), 1)), 0, BUYER_ID, "María González", SELLER_ID, "Hacienda El Progreso", ADDRESS, "MercadoPago"),
        Order("o5", "ORD-2024-005", "18 abr 2026", OrderStatus.PENDIENTE, listOf(item(p("p_brahman"), 5)), 0, "u_roble", "Finca El Roble", SELLER_ID, "Hacienda El Progreso", "Finca El Roble · Cereté, Córdoba", "PSE"),
        Order("o6", "ORD-2024-006", "17 abr 2026", OrderStatus.ENVIADO, listOf(item(p("p_angus"), 3)), 0, "u_sol", "Hacienda Sol", SELLER_ID, "Hacienda El Progreso", "Hacienda Sol · Planeta Rica, Córdoba", "MercadoPago")
    )

    fun salesSeries(period: ReportPeriod): List<SalesPoint> = when (period) {
        ReportPeriod.SEMANA -> listOf(
            SalesPoint("Lun", 2_100_000), SalesPoint("Mar", 3_400_000), SalesPoint("Mié", 1_800_000),
            SalesPoint("Jue", 4_200_000), SalesPoint("Vie", 5_600_000), SalesPoint("Sáb", 3_900_000), SalesPoint("Dom", 1_200_000)
        )
        ReportPeriod.MES -> listOf(
            SalesPoint("Nov", 28_000_000), SalesPoint("Dic", 34_500_000), SalesPoint("Ene", 22_300_000),
            SalesPoint("Feb", 31_800_000), SalesPoint("Mar", 38_200_000), SalesPoint("Abr", 45_000_000)
        )
        ReportPeriod.ANIO -> listOf(
            SalesPoint("2022", 180_000_000), SalesPoint("2023", 265_000_000), SalesPoint("2024", 342_000_000),
            SalesPoint("2025", 410_000_000), SalesPoint("2026", 455_000_000)
        )
    }

    fun topProducts(period: ReportPeriod): List<RankingItem> {
        val factor = when (period) { ReportPeriod.SEMANA -> 1L; ReportPeriod.MES -> 4L; ReportPeriod.ANIO -> 40L }
        return listOf(
            RankingItem("Brahman Elite 2024", "Pajilla · 156 unidades", 4_200_000 * factor, 100),
            RankingItem("Angus Premium", "Pajilla · 98 unidades", 3_100_000 * factor, 74),
            RankingItem("Angus Elite 2024", "Embrión · 24 unidades", 2_250_000 * factor, 54),
            RankingItem("Simmental Pro", "Pajilla · 61 unidades", 1_520_000 * factor, 36)
        )
    }

    fun topSellers(period: ReportPeriod): List<RankingItem> {
        val factor = when (period) { ReportPeriod.SEMANA -> 1L; ReportPeriod.MES -> 4L; ReportPeriod.ANIO -> 40L }
        return listOf(
            RankingItem("Hacienda El Progreso", "Montería · 24 productos", 6_800_000 * factor, 100),
            RankingItem("Hacienda Sol", "Planeta Rica · 15 productos", 4_100_000 * factor, 60),
            RankingItem("Ganadería Elite S.A.", "Rionegro · 9 productos", 1_900_000 * factor, 28)
        )
    }
}
