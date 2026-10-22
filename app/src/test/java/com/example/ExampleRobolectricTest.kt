package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EduAdminRepository
import com.example.model.AttendanceStatus
import com.example.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EduAdmin", appName)
  }

  @Test
  fun `repository initial data and role login`() {
    val repo = EduAdminRepository()
    assertEquals(Role.ADMIN, repo.currentUser.value?.role)
    assertEquals(5, repo.classes.value.size)
    assertNotNull(repo.students.value.find { it.rollNumber == "1024" })

    // Test attendance recording
    repo.saveAttendance("cls_10a", "2026-10-22", mapOf("stu_10a_01" to AttendanceStatus.PRESENT))
    val saved = repo.attendanceRecords.value["cls_10a-2026-10-22"]
    assertEquals(AttendanceStatus.PRESENT, saved?.get("stu_10a_01"))
  }
}
