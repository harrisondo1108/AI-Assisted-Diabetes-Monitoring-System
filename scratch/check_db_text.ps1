[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$conn = New-Object System.Data.SqlClient.SqlConnection("Server=localhost;Database=Diabetes;Integrated Security=True;")
$conn.Open()
$cmd = $conn.CreateCommand()
$cmd.CommandText = "SELECT TOP 3 ClinicalExamID, MedicalHistory, DiagnosisNote FROM ClinicalExamination"
$r = $cmd.ExecuteReader()
while ($r.Read()) {
    Write-Host "ExamID:" $r["ClinicalExamID"]
    Write-Host "MedicalHistory:" $r["MedicalHistory"]
    Write-Host "DiagnosisNote:" $r["DiagnosisNote"]
    Write-Host "--------------------------------------------------"
}
$conn.Close()
