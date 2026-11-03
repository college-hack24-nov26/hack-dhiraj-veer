
package com.example.pikrakshak

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.Locale

data class ProduceDetection(
    val crop: CropType?,
    val isProduce: Boolean,
    val message: String
)

fun detectProduceFromPhoto(
    context: Context,
    photoPath: String,
    onResult: (ProduceDetection) -> Unit
) {
    val bitmap = BitmapFactory.decodeFile(photoPath)

    if (bitmap == null) {
        onResult(
            ProduceDetection(
                crop = null,
                isProduce = false,
                message = "Cannot read photo. Please take another photo."
            )
        )
        return
    }

    val image = InputImage.fromBitmap(bitmap, 0)
    val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.DEFAULT_OPTIONS
    )

    labeler.process(image)
        .addOnSuccessListener { labels ->

            Log.d(
                "PikRakshakAI",
                "Detected labels: ${
                    labels.map { "${it.text} (${it.confidence})" }
                }"
            )

            val names = labels.map {
                it.text.trim().lowercase(Locale.ROOT)
            }

            // Identify a supported crop when its exact label is available.
            val crop = names.firstNotNullOfOrNull { label ->
                when {
                    "tomato" in label -> CropType.TOMATO
                    "mango" in label -> CropType.MANGO
                    "banana" in label -> CropType.BANANA
                    "apple" in label -> CropType.APPLE
                    "onion" in label -> CropType.ONION
                    "potato" in label -> CropType.POTATO
                    "grape" in label -> CropType.GRAPES
                    "cucumber" in label -> CropType.CUCUMBER
                    "spinach" in label ||
                            "lettuce" in label ||
                            "kale" in label ||
                            "leafy vegetable" in label ||
                            "leaf vegetable" in label ->
                        CropType.LEAFY_VEGETABLES
                    else -> null
                }
            }

            // Accept general produce labels when ML Kit cannot identify
            // the exact crop. The user can then select the crop manually.
            val genericProduce = labels.any { label ->
                val name = label.text.trim().lowercase(Locale.ROOT)

                name in setOf(
                    "food",
                    "fruit",
                    "vegetable",
                    "produce",
                    "fresh produce",
                    "food item"
                ) && label.confidence >= 0.40f
            }

            val result = when {
                crop != null -> ProduceDetection(
                    crop = crop,
                    isProduce = true,
                    message = "Detected: ${crop.displayName}"
                )

                genericProduce -> ProduceDetection(
                    crop = null,
                    isProduce = true,
                    message = "Produce-like photo detected. Please select the crop manually."
                )

                else -> ProduceDetection(
                    crop = null,
                    isProduce = false,
                    message = "Photo not recognized. Try a clear, well-lit photo of one fruit or vegetable."
                )
            }

            labeler.close()
            onResult(result)
        }
        .addOnFailureListener { error ->
            Log.e("PikRakshakAI", "Image labeling failed", error)
            labeler.close()

            onResult(
                ProduceDetection(
                    crop = null,
                    isProduce = false,
                    message = "Photo checking failed. Please try again."
                )
            )
        }
}
