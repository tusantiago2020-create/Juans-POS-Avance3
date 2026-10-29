Option Explicit
Dim fso, shell, carpetaScripts, carpetaProyecto, archivoBat
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")

carpetaScripts = fso.GetParentFolderName(WScript.ScriptFullName)
carpetaProyecto = fso.GetParentFolderName(carpetaScripts)
archivoBat = fso.BuildPath(carpetaScripts, "SERVIDOR-OCULTO.bat")

If Not fso.FileExists(fso.BuildPath(carpetaProyecto, "pom.xml")) Then
  MsgBox "Coloca los archivos en la carpeta scripts del proyecto Juan'S POS.", 48, "Juan'S POS"
  WScript.Quit 1
End If

If Not fso.FileExists(archivoBat) Then
  MsgBox "Falta SERVIDOR-OCULTO.bat en la misma carpeta.", 48, "Juan'S POS"
  WScript.Quit 1
End If

shell.CurrentDirectory = carpetaProyecto
shell.Run Chr(34) & archivoBat & Chr(34), 0, False
MsgBox "Se ha iniciado Juan'S POS en segundo plano." & vbCrLf & _
       "Abre http://localhost:8080 en Chrome." & vbCrLf & _
       "Si todavia no carga, revisa logs\juans-pos.log.", 64, "Juan'S POS"
