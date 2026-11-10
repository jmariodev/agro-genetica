package com.jmariodev.agrodigital.ui.admin.reports

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.jmariodev.agrodigital.R
import com.jmariodev.agrodigital.data.model.OrderStatus
import com.jmariodev.agrodigital.data.model.RankingItem
import com.jmariodev.agrodigital.data.model.ReportPeriod
import com.jmariodev.agrodigital.databinding.FragmentAdminReportsBinding
import com.jmariodev.agrodigital.databinding.ItemRankingBinding
import com.jmariodev.agrodigital.databinding.ItemReportBarBinding
import com.jmariodev.agrodigital.di.ServiceLocator
import com.jmariodev.agrodigital.ui.common.BindingFragment
import com.jmariodev.agrodigital.ui.common.orders.OrderAdapter
import com.jmariodev.agrodigital.ui.common.snack
import com.jmariodev.agrodigital.ui.common.toCompactCop
import com.jmariodev.agrodigital.ui.common.toCop

/** Reporte de ventas por periodo con gráfico de barras construido con vistas (sin librerías). */
class AdminReportsFragment : BindingFragment<FragmentAdminReportsBinding>(FragmentAdminReportsBinding::inflate) {

    private val repository = ServiceLocator.orderRepository
    private var period = ReportPeriod.MES

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val salesAdapter = OrderAdapter(sellerView = true) { order -> snack(getString(R.string.report_sale_info, order.code, order.sellerName)) }
        binding.rvRecentSales.adapter = salesAdapter
        salesAdapter.submitList(repository.getAll().take(RECENT_SALES))

        binding.chipsPeriod.setOnCheckedStateChangeListener { _, ids ->
            period = when (ids.firstOrNull()) {
                R.id.chipWeek -> ReportPeriod.SEMANA
                R.id.chipYear -> ReportPeriod.ANIO
                else -> ReportPeriod.MES
            }
            render()
        }
        binding.btnExport.setOnClickListener { exportCsv() }
        render()
    }

    private fun render() {
        val series = repository.salesSeries(period)
        val total = series.sumOf { it.amount }
        val orders = when (period) {
            ReportPeriod.SEMANA -> WEEK_ORDERS
            ReportPeriod.MES -> MONTH_ORDERS
            ReportPeriod.ANIO -> YEAR_ORDERS
        }
        binding.tvPeriodLabel.setText(
            when (period) {
                ReportPeriod.SEMANA -> R.string.report_label_week
                ReportPeriod.MES -> R.string.report_label_month
                ReportPeriod.ANIO -> R.string.report_label_year
            }
        )
        binding.tvChartTitle.setText(
            when (period) {
                ReportPeriod.SEMANA -> R.string.report_chart_week
                ReportPeriod.MES -> R.string.report_chart_month
                ReportPeriod.ANIO -> R.string.report_chart_year
            }
        )
        binding.tvTotal.text = total.toCop()
        binding.tvOrders.text = orders.toString()
        binding.tvAverage.text = (total / orders).toCop()
        binding.tvTrend.text = getString(R.string.report_trend, TREND_PERCENT)

        renderChart(series.map { it.label to it.amount })
        renderRanking(binding.containerTopProducts, repository.topProducts(period))
        renderRanking(binding.containerTopSellers, repository.topSellers(period))
    }

    /** Cada barra es una vista cuya altura es proporcional al valor máximo de la serie. */
    private fun renderChart(points: List<Pair<String, Long>>) {
        val container = binding.chartContainer
        container.removeAllViews()
        val max = points.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1L
        val maxHeight = resources.getDimensionPixelSize(R.dimen.chart_bar_max_height)
        val inflater = LayoutInflater.from(requireContext())
        points.forEach { (label, amount) ->
            val bar = ItemReportBarBinding.inflate(inflater, container, true)
            bar.tvLabel.text = label
            bar.tvValue.text = amount.toCompactCop()
            bar.bar.updateLayoutParams { height = (maxHeight * amount / max).toInt().coerceAtLeast(MIN_BAR_PX) }
            bar.bar.contentDescription = getString(R.string.cd_chart_bar, label, amount.toCop())
        }
    }

    private fun renderRanking(container: LinearLayout, items: List<RankingItem>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        items.forEachIndexed { index, item ->
            val row = ItemRankingBinding.inflate(inflater, container, true)
            row.tvRank.text = (index + 1).toString()
            row.tvName.text = item.name
            row.tvDetail.text = item.detail
            row.tvAmount.text = item.amount.toCompactCop()
            row.progress.setProgressCompat(item.percent, false)
        }
    }

    /** Exporta el reporte como texto CSV y lo comparte con otra app (correo, Drive, WhatsApp). */
    private fun exportCsv() {
        val csv = buildString {
            appendLine(getString(R.string.report_csv_header))
            repository.salesSeries(period).forEach { appendLine("${it.label};${it.amount}") }
            appendLine()
            appendLine(getString(R.string.report_csv_orders_header))
            repository.getAll().filter { it.status != OrderStatus.CANCELADO }.forEach {
                appendLine("${it.code};${it.date};${it.buyerName};${it.sellerName};${it.total}")
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_export_subject))
            .putExtra(Intent.EXTRA_TEXT, csv)
        startActivity(Intent.createChooser(intent, getString(R.string.report_export)))
    }

    private companion object {
        const val RECENT_SALES = 4
        const val WEEK_ORDERS = 34L
        const val MONTH_ORDERS = 128L
        const val YEAR_ORDERS = 1_296L
        const val TREND_PERCENT = 18
        const val MIN_BAR_PX = 6
    }
}
