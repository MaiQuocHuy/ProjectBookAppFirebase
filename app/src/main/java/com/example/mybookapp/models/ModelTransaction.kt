package com.example.mybookapp.models

class ModelTransaction {
    var transactionID: Long = 0
    var bookID: String = ""
    var uid: String = ""
    var price: Int = 0
    var transactionDate: String = ""

    constructor()

    constructor(transactionID: Long, bookID: String, uid: String, price: Int,transactionDate: String) {
        this.transactionID = transactionID
        this.bookID = bookID
        this.uid = uid
        this.price = price
        this.transactionDate = transactionDate
    }
}

