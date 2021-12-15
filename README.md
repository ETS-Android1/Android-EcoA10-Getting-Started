# Instructions to migrate EcoA10 library to PaymentPOS module

1. Replace the EcoA10 AAR file module with the PaymentPOS AAR file.
2. Application build.gradle changes:
    - minSdkVersion needs to be 16 or greater.
    - Add "multiDexEnabled true" inside defaultConfig.
    - Add "coreLibraryDesugaringEnabled true" inside compileOptions.
    - Add dependencies (use most recent versions):
        - implementation "org.jetbrains.kotlinx:kotlinx-datetime:0.0.0"
        - implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:0.0.0"
        - implementation "io.ktor:ktor-client-android:0.0.0"
        - implementation "io.ktor:ktor-client-serialization:0.0.0"
        - coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:0.0.0"
3. Sync project with gradle files.
4. Replace all imports from "com.ecopaynet.ecoa10.\*" to "com.ecopaynet.module.paymentpos.\*".
5. Replace this related usages:
    1. From "com.ecopaynet.ecoa10.EcoA10" to "com.ecopaynet.module.paymentpos.PaymentPOS"
    2. From "com.ecopaynet.module.paymentpos.Status" to "com.ecopaynet.module.paymentpos.LibraryStatus"
    3. From "com.ecopaynet.module.paymentpos.EcoA10.getStatus()" to "com.ecopaynet.module.paymentpos.PaymentPOS.getLibraryStatus()"
6. Some classes have changed their proprety accessors from "myField" to "getMyField(). Change the way these properties are accessed.
7. Other specific changes:
    - PaymentPOS.initialize()
        - No longer needs to "Context".
    - PaymentPOS.sale(), refund(), etc.
        - Amount changed to "Long".
        - TransactionDate changed to "kotlinx.datetime.LocalDate".
    - PaymentPOS.resetConfiguration()
        - No longer needs to "Context".
    - PaymentPOS.generateTransactionTicketsBMP()
        - Needs a second parameter with a header Bitmap. Can be null.
        - Returns a list of "Bitmap" instead of an array.
    - PaymentPOS.generateTransactionTicketsPDF()
        - Needs a second parameter with a header Bitmap. Can be null.
        - Returns a list of "PdfDocument" instead of an array.
    - TransactionResult:
        - signatureBitmap changed to byte[]
    - SignatureView:
        - getSignatureBitmap() changed return to byte[]

