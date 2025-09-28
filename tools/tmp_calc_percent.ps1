$p = 'e:\MyProjects\MyGitHubCopilot\talkingsmoke\tas-01\talkingsmoke_TASKS.md'

# Read file as lines so we can scope counting to TASK sections only
$rawLines = Get-Content $p -Encoding UTF8

$inTaskSection = $false
$total = 0
$completed = 0

foreach ($line in $rawLines) {
	# Trim leading/trailing whitespace to make heading detection resilient
	$trimmed = $line.Trim()

	# Detect top-level headings that denote TASK sections (contain the word 'TASK')
	if ($trimmed -match '^##\s+.*\bTASK\b') {
		$inTaskSection = $true
		continue
	}

	# Any other top-level '##' heading that does NOT include 'TASK' ends the task-section scope
	if ($trimmed -match '^##\s+' -and $trimmed -notmatch '\bTASK\b') {
		$inTaskSection = $false
		continue
	}

	if ($inTaskSection -and $trimmed -match '^[\-\*\s]*\[[ \~xX✓!\]]') {
		$total += 1
		if ($trimmed -match '^[\-\*\s]*\[(?:x|X|✓)\]') {
			$completed += 1
		}
	}
}

if ($total -eq 0) {
	Write-Output "0 0 0%"
	exit 0
}

Write-Output "$total $completed $([math]::Round(($completed/$total)*100,1))%"
exit 0
