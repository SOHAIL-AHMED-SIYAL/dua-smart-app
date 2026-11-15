package com.duasmartapp.utils

import android.content.Context
import com.duasmartapp.model.Student
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipOutputStream
object XlsxWriter {
    private val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Generates a month-by-month Excel attendance register (.xlsx).
     *
     * Columns: Roll No, Name, Class, Day 1..31, Total Present, Total Absent, Total Leave, % Attendance.
     * Cells show P / A / L / SUN / HOL. Blank = not yet marked.
     */
    fun generateRegister(
        context: Context,
        year: Int,
        month: Int,
        students: List<Student>,
        attendance: Map<Pair<Int, String>, String>, // (studentId, "yyyy-MM-dd") -> status
        holidays: Set<LocalDate>
    ): File {
        val yearMonth = "%04d-%02d".format(Locale.ROOT, year, month)
        val monthDays = LocalDate.parse("$yearMonth-01", df).lengthOfMonth()
        val file = File(context.cacheDir, "DuaSmartApp_Register_$yearMonth.xlsx")

        val out = ZipOutputStream(FileOutputStream(file))
        out.setLevel(java.util.zip.Deflater.BEST_SPEED)
        try {
            writeEntry(out, "[Content_Types].xml", contentTypes())
            writeEntry(out, "_rels/.rels", relsRoot())
            writeEntry(out, "xl/_rels/workbook.xml.rels", relsWorkbook())
            writeEntry(out, "xl/workbook.xml", workbookXml())
            writeEntry(out, "xl/worksheets/sheet1.xml", sheetXml(year, month, monthDays, students, attendance, holidays))
        } finally {
            out.close()
        }
        return file
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(java.util.zip.ZipEntry(name))
        zip.write(content.toByteArray())
        zip.closeEntry()
    }

    private fun contentTypes(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet+xml"/>
</Types>
""".trimEnd()

    private fun relsRoot(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>
""".trimEnd()

    private fun relsWorkbook(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>
""".trimEnd()

    private fun workbookXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="Attendance Register" sheetId="1" r:id="rId1"/></sheets>
</workbook>
""".trimEnd()

    private fun sheetXml(
        year: Int,
        month: Int,
        monthDays: Int,
        students: List<Student>,
        attendance: Map<Pair<Int, String>, String>,
        holidays: Set<LocalDate>
    ): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>""")

        // Header row
        val headerCells = mutableListOf<String>()
        headerCells += cell("A1", "Roll No", inline = true)
        headerCells += cell("B1", "Student Name", inline = true)
        headerCells += cell("C1", "Class", inline = true)
        val firstDayCol = 'D'.code - 'A'.code // 0-based column index for day 1
        for (d in 1..monthDays) {
            val col = columnName(firstDayCol + (d - 1))
            val label = "$d${weekdayLabel(LocalDate.parse("$yearMonth-%02d-%02d".format(Locale.ROOT, year, month, d), df).dayOfWeek)}"
            headerCells += cell("$col$1", label, inline = true)
        }
        val summaryStart = columnName(firstDayCol + monthDays)
        headerCells += cell("${summaryStart}1", "Total Present", inline = true)
        headerCells += cell("${columnName(firstDayCol + monthDays + 1)}1", "Total Absent", inline = true)
        headerCells += cell("${columnName(firstDayCol + monthDays + 2)}1", "Total Leave", inline = true)
        headerCells += cell("${columnName(firstDayCol + monthDays + 3)}1", "% Attendance", inline = true)
        sb.append("    <row r=\"1\">${headerCells.joinToString("")}</row>\n")

        // Data rows
        var row = 2
        for (s in students) {
            val cells = mutableListOf<String>()
            cells += cell("A$row", s.rollNo, inline = true)
            cells += cell("B$row", s.fullName, inline = true)
            cells += cell("C$row", s.classGrade, inline = true)
            var present = 0; var absent = 0; var leave = 0
            for (d in 1..monthDays) {
                val date = LocalDate.parse("$yearMonth-%02d-%02d".format(Locale.ROOT, year, month, d), df)
                val col = columnName(firstDayCol + (d - 1))
                val status: String
                if (date.dayOfWeek == DayOfWeek.SUNDAY) status = "SUN"
                else if (holidays.contains(date)) status = "HOL"
                else status = attendance[Pair(s.id, date.format(df))] ?: ""
                cells += cell("$col$row", status, inline = true)
                when (status) {
                    "P" -> present++
                    "A" -> absent++
                    "L" -> leave++
                }
            }
            val totalMarked = present + absent + leave
            val pct = if (totalMarked == 0) 0 else (present * 1000 / totalMarked) / 10.0
            val summaryStart = columnName(firstDayCol + monthDays)
            cells += cell("${summaryStart}$row", present.toString())
            cells += cell("${columnName(firstDayCol + monthDays + 1)}$row", absent.toString())
            cells += cell("${columnName(firstDayCol + monthDays + 2)}$row", leave.toString())
            cells += cell("${columnName(firstDayCol + monthDays + 3)}$row", "$pct", inline = true)
            sb.append("    <row r=\"$row\">${cells.joinToString("")}</row>\n")
            row++
        }
        sb.append("  </sheetData>\n</worksheet>\n")
        return sb.toString()
    }

    private fun weekdayLabel(dow: DayOfWeek): String = when (dow) {
        DayOfWeek.SUNDAY -> " (Sun)"
        DayOfWeek.SATURDAY -> " (Sat)"
        else -> ""
    }

    private fun cell(ref: String, value: String, inline: Boolean = false): String {
        val escaped = escapeXml(value)
        return if (inline) {
            "<c r=\"$ref\" t=\"inlineStr\"><is><t>$escaped</t></is></c>"
        } else {
            "<c r=\"$ref\"><v>$escaped</v></c>"
        }
    }

    private fun escapeXml(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}

/** Excel column name for a 0-based index: 0=A, 25=Z, 26=AA, 27=AB ... */
private fun columnName(index0: Int): String {
    var n = index0
    val sb = StringBuilder()
    while (true) {
        sb.insert(0, ('A'.code + n % 26).toChar())
        n = n / 26 - 1
        if (n < 0) break
    }
    return sb.toString()
}
