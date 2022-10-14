package com.plcoding.stockmarketapp.data.repository

import com.plcoding.stockmarketapp.data.csv.CSVParser
import com.plcoding.stockmarketapp.data.local.StockDataBase
import com.plcoding.stockmarketapp.data.mapper.toCompanyListing
import com.plcoding.stockmarketapp.data.mapper.toCompanyListingEntity
import com.plcoding.stockmarketapp.data.remote.StockApi
import com.plcoding.stockmarketapp.domain.model.CompanyListing
import com.plcoding.stockmarketapp.domain.repository.StockRepository
import com.plcoding.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db: StockDataBase,
    val companyListingParser: CSVParser<CompanyListing>,
) : StockRepository {
    private val dao = db.dao;

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading())
            val localListing = dao.searchCompanyListing(query)
            emit(Resource.Success(localListing.map { it.toCompanyListing() }))
            if (!fetchFromRemote && localListing.isNotEmpty() && query.isNotEmpty()) {
                emit(Resource.Loading(loading = false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListings()
                companyListingParser.parse(response.byteStream());
            } catch (e: Exception) {
                emit(Resource.Error(e.localizedMessage))
                null
            }
            remoteListing?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListing(listings.map {
                    it.toCompanyListingEntity()
                })
                emit(Resource.Success(dao.searchCompanyListing("").map { it.toCompanyListing() }))
                emit(Resource.Loading(false))

            }
        }
    }
}