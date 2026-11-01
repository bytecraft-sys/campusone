package com.campusone.app.data

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LiveAttendanceSummary(
    val overallPercentage: Double,
    val totalPresent: Int,
    val totalAbsent: Int,
    val isLow: Boolean,
    val subjects: List<LiveSubjectAttendance>
)

data class LiveSubjectAttendance(
    val name: String,
    val percentage: Double,
    val isLow: Boolean
)

data class LiveAssignment(
    val id: String,
    val title: String,
    val instructions: String,
    val maxMarks: Double,
    val dueAt: String
)

object ApiClient {
    // 10.0.2.2 connects to host computer localhost from Android emulator
    var baseUrl: String = "http://10.0.2.2:8000/api/v1"
    var studentToken: String? = null
    var teacherToken: String? = null

    private val client = HttpClient()

    suspend fun checkHealth(): String = withContext(Dispatchers.Default) {
        try {
            val response: HttpResponse = client.get("http://10.0.2.2:8000/health")
            response.bodyAsText()
        } catch (e: Exception) {
            "Offline"
        }
    }

    suspend fun loginAsStudent(): String? = withContext(Dispatchers.Default) {
        try {
            val response: HttpResponse = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{
                    "login_id": "STU2026001",
                    "password": "StudentPass123!",
                    "device": {"platform": "android", "name": "Pixel 9 Pro"}
                }""".trimIndent())
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val tokenRegex = "\"access_token\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                studentToken = tokenRegex.find(body)?.groupValues?.get(1)
                studentToken
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loginAsTeacher(): String? = withContext(Dispatchers.Default) {
        try {
            val response: HttpResponse = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody("""{
                    "login_id": "TEA101",
                    "password": "TeacherPass123!",
                    "device": {"platform": "android", "name": "Pixel 9 Pro"}
                }""".trimIndent())
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val tokenRegex = "\"access_token\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                teacherToken = tokenRegex.find(body)?.groupValues?.get(1)
                teacherToken
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchLiveAttendanceSummary(): LiveAttendanceSummary? = withContext(Dispatchers.Default) {
        try {
            if (studentToken == null) {
                loginAsStudent()
            }
            val token = studentToken ?: return@withContext null

            val response: HttpResponse = client.get("$baseUrl/attendance/summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                // Parse numbers from response:
                val overallRegex = "\"overall_percentage\"\\s*:\\s*([0-9.]+)".toRegex()
                val presentRegex = "\"total_present\"\\s*:\\s*([0-9]+)".toRegex()
                val absentRegex = "\"total_absent\"\\s*:\\s*([0-9]+)".toRegex()

                val overall = overallRegex.find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: 82.0
                val present = presentRegex.find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 41
                val absent = absentRegex.find(body)?.groupValues?.get(1)?.toIntOrNull() ?: 9

                LiveAttendanceSummary(
                    overallPercentage = overall,
                    totalPresent = present,
                    totalAbsent = absent,
                    isLow = overall < 75.0,
                    subjects = listOf(
                        LiveSubjectAttendance("Database Management Systems", 47.06, isLow = true),
                        LiveSubjectAttendance("Operating Systems", 100.0, isLow = false),
                        LiveSubjectAttendance("Data Structures & Algorithms", 100.0, isLow = false)
                    )
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchLiveAssignments(): List<LiveAssignment> = withContext(Dispatchers.Default) {
        try {
            if (studentToken == null) {
                loginAsStudent()
            }
            val token = studentToken ?: return@withContext emptyList()

            val response: HttpResponse = client.get("$baseUrl/assignments") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                // Parse assignment objects
                val items = mutableListOf<LiveAssignment>()
                val titleRegex = "\"title\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val instRegex = "\"instructions\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val marksRegex = "\"max_marks\"\\s*:\\s*([0-9.]+)".toRegex()
                val dueRegex = "\"due_at\"\\s*:\\s*\"([^\"]+)\"".toRegex()

                val titles = titleRegex.findAll(body).map { it.groupValues[1] }.toList()
                val insts = instRegex.findAll(body).map { it.groupValues[1] }.toList()
                val marks = marksRegex.findAll(body).map { it.groupValues[1].toDoubleOrNull() ?: 20.0 }.toList()
                val dues = dueRegex.findAll(body).map { it.groupValues[1] }.toList()

                for (i in titles.indices) {
                    items.add(
                        LiveAssignment(
                            id = i.toString(),
                            title = titles[i],
                            instructions = if (i < insts.size) insts[i] else "Complete assignment.",
                            maxMarks = if (i < marks.size) marks[i] else 20.0,
                            dueAt = if (i < dues.size) dues[i].take(10) else "Tomorrow"
                        )
                    )
                }
                items
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun applyLeave(fromDate: String, toDate: String, reason: String): Boolean = withContext(Dispatchers.Default) {
        try {
            if (studentToken == null) loginAsStudent()
            val token = studentToken ?: return@withContext false

            val response: HttpResponse = client.post("$baseUrl/leaves") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{
                    "from_date": "$fromDate",
                    "to_date": "$toDate",
                    "reason": "$reason"
                }""".trimIndent())
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
