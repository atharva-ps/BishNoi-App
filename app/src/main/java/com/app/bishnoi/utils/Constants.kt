package com.app.bishnoi.utils

object Constants {
    // ✅ FOR EMULATOR (Android Emulator uses 10.0.2.2)
//    const val BASE_URL = "http://10.0.2.2:5001/justbaat-debug/asia-south1/bishnoiapi/"

    // ✅ FOR PHYSICAL DEVICE (Same WiFi Network)
//     const val BASE_URL = "http://192.168.1.7:5001/justbaat-debug/asia-south1/bishnoiapi/"
    // Example: const val BASE_URL = "http://192.168.1.100:5001/justbaat-debug/asia-south1/bishnoiapi/"

    // ✅ FOR PRODUCTION (After deployment)
     const val BASE_URL = "https://asia-south1-justbaat-debug.cloudfunctions.net/bishnoiapi/"

    const val TIMEOUT_SECONDS = 30L
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 2
}
