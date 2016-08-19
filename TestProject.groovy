def nunitStep() {
	bat 'nunit3-console TestApplication.Tests\\bin\\Release\\TestApplication.Tests.dll --result:nunit-result.xml;format=nunit2'
}

return this;