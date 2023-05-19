package com.example.mybookapp.models

class ModelPdf {
    //variables
    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var categoryId: String = ""
    var url:String = ""
    var timestamp:Long = 0
    var viewsCount:Long = 0
    var downloadsCount:Long = 0
    var price: Int = 0
    var status: String = ""
    var isFavorite = false
    //parameter constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long,
        price: Int,
        status: String,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.price = price
        this.status = status
        this.isFavorite = isFavorite
    }
    //empty constructor
    constructor()


}