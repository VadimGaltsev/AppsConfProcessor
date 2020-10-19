package com.example.myapplication.activity

import com.example.lib.JsonObject

@JsonObject("CompanyDto")
val jsonString = """
    {
        "company" : "Alfa-Bank",
        "count" : 500,
        "isNice" : true,
        "values" : ["Android", "ios"]
    }
"""
