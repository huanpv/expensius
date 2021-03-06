/*
 * Copyright (C) 2016 Mantas Varnagiris.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.mvcoding.expensius.firebase.service

import com.mvcoding.expensius.firebase.archivedTransactionsDatabaseReference
import com.mvcoding.expensius.firebase.model.FirebaseMoney
import com.mvcoding.expensius.firebase.model.FirebaseTransaction
import com.mvcoding.expensius.firebase.transactionsDatabaseReference
import com.mvcoding.expensius.model.CreateTransaction
import com.mvcoding.expensius.model.ModelState.ARCHIVED
import com.mvcoding.expensius.model.ModelState.NONE
import com.mvcoding.expensius.model.Money
import com.mvcoding.expensius.model.Transaction
import com.mvcoding.expensius.service.AppUserService
import com.mvcoding.expensius.service.TransactionsWriteService
import rx.Observable

class FirebaseTransactionsWriteService(private val appUserService: AppUserService) : TransactionsWriteService {

    override fun createTransactions(createTransactions: Set<CreateTransaction>): Observable<Unit> = appUserService.appUser()
            .first()
            .map {
                val transactionsReference = transactionsDatabaseReference(it.userId)
                createTransactions.forEach {
                    val newTransactionReference = transactionsReference.push()
                    val firebaseTransaction = it.toFirebaseTransaction(newTransactionReference.key)
                    newTransactionReference.setValue(firebaseTransaction)
                }
            }

    override fun saveTransactions(updateTransactions: Set<Transaction>): Observable<Unit> = appUserService.appUser()
            .first()
            .map {
                val transactions = updateTransactions.associateBy({ it.transactionId.id }, { if (it.modelState == NONE) it.toMap() else null })
                val archivedTransactions = updateTransactions.associateBy({ it.transactionId.id }, { if (it.modelState == ARCHIVED) it.toMap() else null })

                val appUserId = it.userId
                if (transactions.isNotEmpty()) transactionsDatabaseReference(appUserId).updateChildren(transactions)
                if (archivedTransactions.isNotEmpty()) archivedTransactionsDatabaseReference(appUserId).updateChildren(archivedTransactions)
            }

    private fun Money.toFirebaseMoney() = FirebaseMoney(
            amount.toPlainString(),
            currency.code)

    private fun Money.toMap() = mapOf(
            "amount" to amount.toPlainString(),
            "currency" to currency.code)

    private fun CreateTransaction.toFirebaseTransaction(id: String) = FirebaseTransaction(
            id,
            transactionType.name,
            transactionState.name,
            timestamp.millis,
            -timestamp.millis,
            money.toFirebaseMoney(),
            tags.map { it.tagId.id },
            note.text)

    private fun Transaction.toMap() = mapOf(
            "id" to transactionId.id,
            "transactionType" to transactionType.name,
            "transactionState" to transactionState.name,
            "timestamp" to timestamp.millis,
            "timestampInverse" to -timestamp.millis,
            "money" to money.toMap(),
            "tags" to tags.map { it.tagId.id },
            "note" to note.text)
}