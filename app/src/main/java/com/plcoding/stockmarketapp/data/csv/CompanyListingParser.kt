package com.plcoding.stockmarketapp.data.csv

import com.opencsv.CSVReader
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CompanyListingParser @Inject constructor() : CSVParser<CompanyListing> {
    override suspend fun parse(stream: InputStream): List<CompanyListing> {
        val csvReader = CSVReader(InputStreamReader(stream));

        return withContext(Dispatchers.IO) {
            return@withContext csvReader.readAll().drop(1).mapNotNull { line ->
                val symbol = line.getOrNull(0)
                val name = line.getOrNull(1)
                val exchange = line.getOrNull(2)
                return@mapNotNull CompanyListing(
                    name ?: return@mapNotNull null,
                    symbol ?: return@mapNotNull null,
                    exchange ?: return@mapNotNull null
                )
            }.also {
                csvReader.close()
            }
        }
    }
}