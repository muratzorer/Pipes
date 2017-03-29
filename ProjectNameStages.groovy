def NunitHtmlStage(){
	bat "NUnitHTMLReportGenerator \"C:\\Program Files (x86)\\Jenkins\\workspace\\denemeMultiBranch\\master\\nunit-result.xml\""
}

def nunitStep() {
	bat "nunit3-console TestApplication.Tests\\bin\\Release\\TestApplication.Tests.dll --result:nunit-result.xml;format=nunit2"
}

def sonarQubeAnalysis() {
	bat 'MSBuild.SonarQube.Runner begin /k:\"TestApplication\" /n:\"Test Application\" /v:1.0.0.%BUILD_NUMBER% /d:sonar.log.level=DEBUG /d:sonar.cs.nunit.reportsPaths=\"%CD%\\nunit-result.xml\" /d:sonar.cs.msbuild.testProjectPattern=.*[Tt]est[s]?.* /d:sonar.cs.vscoveragexml.reportsPaths="%CD%\\VisualStudioTransformed.coveragexml'
	//bat "call \"C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\vcvarsall.bat\""
	//bat "\"${tool 'msbuild'}\" TestApplication.sln /t:rebuild /p:VisualStudioVersion=12.0"
	bat	"\"C:\\Program Files (x86)\\MSBuild\\14.0\\Bin\\MSBuild.exe\" TestApplication.sln /t:rebuild /p:VisualStudioVersion=12.0"
	//bat "\"C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\Team Tools\\Dynamic Code Coverage Tools\\CodeCoverage.exe\" collect /output:%CD%\\VisualStudio.coverage"
	bat "\"C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\Common7\\IDE\\CommonExtensions\\Microsoft\\TestWindow\\vstest.console.exe\" \"TestApplication.Tests\\bin\\Release\\TestApplication.Tests.dll\" /InIsolation /EnableCodeCoverage /Settings:tests.runsettings /TestAdapterPath:\".\"" // /logger:TfsPublisher;
	bat "\"C:\\Coverage Converter\\CoverageConverter.exe\" \"%CD%\\TestApplication.Tests\\bin\\Release\\TestApplication.Tests.dll\""
	bat "nunit3-console TestApplication.Tests\\bin\\Release\\TestApplication.Tests.dll --result=nunit-result.xml;format=nunit2"
	//bat "\"C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\Team Tools\\Dynamic Code Coverage Tools\\CodeCoverage.exe\" analyze /output:%CD%\\VisualStudio.coveragexml %CD%\\VisualStudio.coverage"
	bat 'MSBuild.SonarQube.Runner end'
}

def generateCoverageReports() {
	bat "\"C:\\Report Generator\\ReportGenerator.exe\" -reports:VisualStudio.coveragexml -targetdir:\"%CD%\" -reporttypes:Latex;HtmlSummary"
}

def uploadTestsToALM() {
	// (ROBOCOPY "C:\Program Files (x86)\Jenkins\workspace\denemeMultiBranch\master" "C:\Program Files (x86)\Jenkins\jobs\ALMJob\builds\%BUILD_NUMBER%" nunit-result.xml) ^& IF %ERRORLEVEL% LEQ 4 exit /B 0
	build 'ALMJob'
}

def runCucumberTestsWithReport() {
	nunit3-console "D:\Git Repos\TestProject\TestApplication.Integration.Tests\bin\Debug\TestApplication.Integration.Tests.dll" --labels:on --output:nunit-result.txt --result:nunit-result.xml;format=nunit2
	"D:\Git Repos\TestProject\packages\SpecFlow.2.1.0\tools\specflow.exe" nunitexecutionreport "D:\Git Repos\TestProject\TestApplication.Integration.Tests\TestApplication.Integration.Tests.csproj" /xmlTestResult:nunit-result.xml /testOutput:nunit-result.txt /out:MyResult.html
	"D:\Git Repos\TestProject\packages\SpecFlow.2.1.0\tools\specflow.exe" stepdefinitionreport "D:\Git Repos\TestProject\TestApplication.Integration.Tests\TestApplication.Integration.Tests.csproj"
}

// add credentials to vault script
/*
"C:\Program Files (x86)\IIS\Microsoft Web Deploy V3\msdeploy.exe" ^
  -verb:dump ^
  -source:iisapp="Default Web Site/",computername="",storecredentials="Server",username="Administrator",password="LDAP_PASS",authtype='Basic' ^
  -whatif
*/


// local deploy script - contentpath creates app under iis if not exists
// cannot deploy to empty folders at first. so we first create a dummy placeholder (temp) file.
/*
msdeploy ^
  -verb:sync ^
  -source:contentPath="C:\Program Files (x86)\Jenkins\workspace\denemeMultiBranch\master\MvcApplication\bin" ^
  -dest:contentPath="MvcApplicationSite/MvcApplication/Temp" ^
  -enableRule:DoNotDeleteRule ^
  -enablerule:AppOffline ^
  -disableLink:AppPoolExtension ^
  -disableLink:ContentExtension ^
  -disableLink:CertificateExtension

msdeploy ^
  -verb:sync ^
  -source:contentPath="C:\Program Files (x86)\Jenkins\workspace\denemeMultiBranch\master\MvcApplication\bin" ^
  -dest:contentPath="MvcApplicationSite/MvcApplication" ^
  -enableRule:DoNotDeleteRule ^
  -enablerule:AppOffline ^
  -disableLink:AppPoolExtension ^
  -disableLink:ContentExtension ^
  -disableLink:CertificateExtension
*/

// remote deploy script
/*
"C:\Program Files (x86)\IIS\Microsoft Web Deploy V3\msdeploy.exe" ^
  -verb:sync ^
  -source:contentPath="C:\Program Files (x86)\Jenkins\workspace\denemeMultiBranch\master\MvcApplication\bin" ^
  -dest:contentPath="NewSite/NewApp",ComputerName="https://WIN-PCIITINITOC:8172/msdeploy.axd?site=newsite",GetCredentials='Server',AuthType='Basic' ^
  -enableRule:DoNotDeleteRule ^
  -enablerule:AppOffline ^
  -disableLink:AppPoolExtension ^
  -disableLink:ContentExtension ^
  -disableLink:CertificateExtension ^
  -allowuntrusted  ^
  -whatif
*/

// server automation script for configuring web deploy
/*
webpicmd /install /Products:"Recommended Server Configuration for Web Hosting Providers"
*/

// HPE ALM - curl commands for auth and get entity
/*
curl --cookie cookies.txt --cookie-jar cookies.txt -i -u "IS96705:PWD" "http://hpalm.isbank/qcbin/authentication-point/authenticate"

curl --cookie cookies.txt --cookie-jar cookies.txt -i -X POST "http://hpalm.isbank/qcbin/rest/site-session"

curl --cookie cookies.txt --cookie-jar cookies.txt -o "response.txt" -i -H "Accept: application/json" "http://hpalm.isbank/qcbin/rest/domains/DOMAINS/projects/KurumsalUyg_Varlik_Fin_Yon/defects/2218"
curl --cookie cookies.txt --cookie-jar cookies.txt -o "response.txt" -i -H "Accept: application/json" "http://hpalm.isbank/qcbin/rest/domains/DOMAINS/projects/KurumsalUyg_Varlik_Fin_Yon/tests/22880"
curl --cookie cookies.txt --cookie-jar cookies.txt -o "response.txt" -i -H "Accept: application/json" "http://hpalm.isbank/qcbin/rest/domains/DOMAINS/projects/KurumsalUyg_Varlik_Fin_Yon/customization/entities/test/fields?required=true"
*/

// HPE ALM - To discover the required fields for a customized project you can query the following URL (entity types are singular: run/test/test-set/test-config/defect/test-instance)
// http://{SERVER}/qcbin/rest/domains/{DOMAIN}/projects/{PROJECT}/customization/entities/run/fields?required=true

return this;
