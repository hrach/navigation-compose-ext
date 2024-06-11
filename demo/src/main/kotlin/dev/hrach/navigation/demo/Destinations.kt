package dev.hrach.navigation.demo

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object Destinations {
	@Serializable
	data object Home

	@Serializable
	data object List

	@Serializable
	data object Profile

	@Serializable
	data object Modal1

	@Serializable
	data object Modal2

	@Serializable
	data object BottomSheet {
		@Serializable
		data class Result(
			val id: Int,
		)
	}

	@Serializable
	data class IssueNested(
		val nested: Nested,
	) {
		@Serializable
		data class Nested(
			val id: Int,
		)
	}

	@Serializable
	data class IssueEnum(
		val priority: Priority,
	) {
		enum class Priority { Top, Normal }
	}

	@Serializable
	data class IssueEncoding(
		val nested: Nested,
	) {
		@Serializable
		data class Nested(
			val review: String,
		)
	}

	class NestedNavType : NavType<IssueEncoding.Nested>(isNullableAllowed = false) {
		override fun get(bundle: Bundle, key: String): IssueEncoding.Nested {
			return IssueEncoding.Nested(bundle.getString(key)!!)
		}

		override fun parseValue(value: String): IssueEncoding.Nested {
			return IssueEncoding.Nested(value)
		}

		override fun put(bundle: Bundle, key: String, value: IssueEncoding.Nested) {
			bundle.putString(key, value.review)
		}

		override fun serializeAsValue(value: IssueEncoding.Nested): String {
			return Uri.encode(value.review)
		}
	}

	@Serializable
	data class IssueObjectThenClass(
		val email: String,
	)

	@Serializable
	data class IssueExternalType(
		@Serializable(with = CustomLocalDateSerializer::class)
		val born: LocalDate,
	)

	class CustomLocalDateSerializer : KSerializer<LocalDate> {
		override val descriptor: SerialDescriptor =
			PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

		override fun deserialize(decoder: Decoder): LocalDate =
			LocalDate.parse(decoder.decodeString())

		override fun serialize(encoder: Encoder, value: LocalDate) {
			encoder.encodeString(value.toString())
		}
	}

	class CustomLocalDateNavType : NavType<LocalDate>(isNullableAllowed = false) {
		override fun get(bundle: Bundle, key: String): LocalDate? =
			bundle.getString(key).let(LocalDate::parse)

		override fun parseValue(value: String): LocalDate =
			LocalDate.parse(value)

		override fun put(bundle: Bundle, key: String, value: LocalDate) {
			bundle.putString(key, value.toString())
		}

		override fun serializeAsValue(value: LocalDate): String {
			return Uri.encode(value.toString())
		}
	}

	@Serializable
	data class IssueEmptyString(
		val review: String,
	)
}
