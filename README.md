Introduction

In this work, We are trying to build a fast file sharing system which include detection,data transmission.
Our problem can be simply described as that when 2 mobile devices encounter with each other, one need to detect quickly whether the other one has the file it need and futhermore, we can divide files into chunks and use some algorithm to identify the existense of the chunks. However, in the real case, it could be multiple devices share the files at the same time.

Architechture

There four main part of the work. They are Peer search, Query,Protocol and Data transmission.We will find peers via wifi direct. The wifi direct has many limitations, like only in android 4.0 device, and all the devices need open all the time to wait for connection. Since it is convenient, we will use it to simplify the work, and we will focus on developing the protocols and algorithm.  Before we search the peers, we need do some initialization work, such as read file list and set up data structure. After detecting the peers we need, we can connect with them and establish socket communication.Then we need use some protocols to check the files needed and availability and the exact chunk needed and availability. All this work will be done in the query process. Hash tables and Bloom filter will be used to detect whether an element is a member of a set.The last part is data transmission. After all the work set, we can transmit the data based on the requirement. Then the file status will be updated in the file list.

Reference

The WiFi Direct Connection function in this project followed the instruction from Android Developer Tutorial and some of the codes are referenced and modified from the WiFi Direct Demo Codes provided by the Android Developer Tutorial from the following link: http://developer.android.com/resources/samples/WiFiDirectDemo/index.html
