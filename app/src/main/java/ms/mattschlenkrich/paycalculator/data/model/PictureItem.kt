package ms.mattschlenkrich.paycalculator.data.model

data class PictureItem(
    val pictureId: Long,
    val driveFileId: String?
) {
    val isUploaded: Boolean
        get() = !driveFileId.isNullOrBlank()
}