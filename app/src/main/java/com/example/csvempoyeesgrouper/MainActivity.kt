package com.example.csvempoyeesgrouper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var job:Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job= Job()

        selectCSVbtn.setOnClickListener{
            openCSVFile()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let{ uri->
                launch {
                    val csvData = readFile(uri)
                    val listOfEmployees = ParseCSVData(csvData)
                    val groupedEmployees = groupEmplyeesByProjects(listOfEmployees)
                    val gridResult = getGroupedEmployeesAsGrid(groupedEmployees)
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = GridAdapter(gridResult)
//                    displayCSVDataAsString(csvData)
                }

            }
        }
    }

    private fun groupEmplyeesByProjects(employeesProjects: List<EmployeeProject>): List<GroupedEmployee>{
        val groupedEmployees = mutableListOf<GroupedEmployee>()
        val projects = employeesProjects.groupBy { it.projectId }

        for (project in projects) {
            val projectId = project.key
            val employeesOnProject = project.value

            for (i in 0 until employeesOnProject.size) {
                for (j in i + 1 until employeesOnProject.size) {
                    val employee1 = employeesOnProject[i].employeeId
                    val employee2 = employeesOnProject[j].employeeId
                    val daysWorked = ChronoUnit.DAYS.between(
                        maxOf(employeesOnProject[i].startDate, employeesOnProject[j].startDate),
                        minOf(employeesOnProject[i].endDate,employeesOnProject[j].endDate)
                    )

                    groupedEmployees.add(
                        GroupedEmployee(
                            pair = EmployeePair(employee1, employee2),
                            projectId = projectId,
                            daysWorked = daysWorked
                        )
                    )
                }
            }
        }

        return groupedEmployees
    }

    private fun getGroupedEmployeesAsGrid(employeesGrid: List<GroupedEmployee>): List<GridItem> {
        val employeeListGrid = mutableListOf<GridItem>()
            for (grid in employeesGrid) {
                var days = 0L
                if (grid.daysWorked < 0) {
                    days = 0
                } else {
                    days = grid.daysWorked
                }
                employeeListGrid.add(
                    GridItem(
                        employee1 = grid.pair.employee1,
                        employee2 = grid.pair.employee2,
                        projectId = grid.projectId,
                        daysWorked = days
                    )
                )
            }
        return employeeListGrid
    }

    private fun ParseCSVData(csvData: List<List<String>>): List<EmployeeProject> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return csvData.map{ row ->
            EmployeeProject(
                employeeId = row[0].toInt(),
                projectId = row[1].toInt(),
                startDate = LocalDate.parse(row[2], dateFormatter),
                endDate = if (row[3] == "NULL" || row[3].isNullOrBlank()) {
                    LocalDate.now()
                }else {
                    LocalDate.parse(row[3], dateFormatter)
                }
            )
        }
    }

    private fun openCSVFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values"))
        }
        filePickerLauncher.launch(intent)
    }

    private suspend fun readFile(uri: Uri): List<List<String>> {
        return withContext(Dispatchers.IO) {
            val contentResolver = applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val reader = inputStream?.bufferedReader()
            val result = mutableListOf<List<String>>()

            reader?.useLines { lines ->
                lines.forEach { line->
                    val row = line.split(','). map{ it.trim()}
                    result.add(row)
                }
            }
            result
        }
    }
}

data class EmployeeProject(
    val employeeId: Int,
    val projectId: Int,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class EmployeePair(
    val employee1: Int,
    val employee2: Int
)

data class GroupedEmployee(
    val pair: EmployeePair,
    val projectId: Int,
    val daysWorked: Long
)

data class GridItem(
    val employee1: Int,
    val employee2: Int,
    val projectId: Int,
    val daysWorked: Long
)